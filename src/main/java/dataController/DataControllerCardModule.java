package dataController;

import com.fasterxml.jackson.databind.JsonNode;
import dataBaseAccessSQL.DataBaseAccessSQL;
import io.restassured.response.Response;
import models.cardModule.AccountDetails;
import models.cardModule.CardDetails;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONObject;
import utils.ApiRequestSpec;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DataControllerCardModule {
    private static final String QUERY_CARD_DETAILS = """
        SELECT TOP 1 Response 
        FROM LogDB.dbo.MyCredoExternalApiLog 
        WHERE ActionUrl LIKE '%/api/Card/CardList%' 
        AND Response LIKE ?
        ORDER BY CreateDate DESC
        """;

    public static List<CardDetails> getCardDetailsFromDB(String accountNumber) throws SQLException {
        List<CardDetails> cardDetailsList = new ArrayList<>();
        try (Connection dataBaseAccessSql = DataBaseAccessSQL.getConnectionSMS();
             PreparedStatement preparedStatement = dataBaseAccessSql.prepareStatement(QUERY_CARD_DETAILS)) {

            preparedStatement.setString(1, "%" + accountNumber + "%");
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String response = resultSet.getString("Response");
                String cardNickName = extractCardNickName(response, accountNumber);

                CardDetails cardDetails = new CardDetails();
                cardDetails.setAccountNumber(accountNumber);
                cardDetails.setCardNickName(cardNickName);
                cardDetailsList.add(cardDetails);
            }
        }
        return cardDetailsList;
    }

    private static String extractCardNickName(String response, String accountNumber) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonResponse = objectMapper.readTree(response);
            JsonNode resultArray = jsonResponse.get("Result");

            if (resultArray != null && resultArray.isArray()) {
                for (JsonNode cardObj : resultArray) {
                    if (accountNumber.equals(cardObj.get("AccountNumber").asText().trim())) {
                        return cardObj.has("CardNickName") ? cardObj.get("CardNickName").asText().trim() : "";
                    }
                }
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        return "";
    }

    private static final String QUERY_ACCOUNT_DETAILS = """
        SELECT TOP 1 Response 
        FROM LogDB.dbo.MyCredoExternalApiLog 
        WHERE ActionUrl LIKE '%/api/Account/AccountList%' 
        AND Response LIKE ? 
        ORDER BY CreateDate DESC
        """;

    private static final String QUERY_CURRENCY_RATE = """
        SELECT TOP 1 Response 
        FROM LogDB.dbo.MyCredoExternalApiLog 
        WHERE ActionUrl LIKE '%/api/Core/CurrencyRateList%' 
        AND UserId = '792247'
        ORDER BY CreateDate DESC
        """;

    private static double cachedBuyRate = 0.0;  //

    // ანგარიშის ვალუტების წამოღება მონაცემთა ბაზიდან
    public static List<AccountDetails> getAllAccountBalances(String accountNumber) throws SQLException {
        List<AccountDetails> accountDetailsList = new ArrayList<>();

        try (Connection dataBaseAccessSql = DataBaseAccessSQL.getConnectionSMS();
             PreparedStatement preparedStatement = dataBaseAccessSql.prepareStatement(QUERY_ACCOUNT_DETAILS)) {

            preparedStatement.setString(1, "%" + accountNumber + "%");
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String response = resultSet.getString("Response");
                accountDetailsList.addAll(extractAccountBalances(response, accountNumber));
            }
        }

        //  BuyRate-ის დამატება მხოლოდ USD ვალუტისთვის
        double buyRate = getCorrectBuyRate(); //
        for (AccountDetails account : accountDetailsList) {
            if ("USD".equals(account.getCurrency())) {
                account.setBuyRate(buyRate);
            } else {
                account.setBuyRate(1.0);
            }
        }

        return accountDetailsList;
    }

    // ანგარიშის ვალუტების წამოღება პირდაპირ API-დან
    public static List<AccountDetails> getAccountBalancesFromApi(String accountNumber) {
        List<AccountDetails> accountDetailsList = new ArrayList<>();

        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("AccountNumber", accountNumber);

            // API მოთხოვნის გაგზავნა
            Response response = ApiRequestSpec.postRequestSpec()
                    .body(requestBody.toString())
                    .when()
                    .post("/api/Account/AccountList")
                    .then()
                    .spec(ApiRequestSpec.postResponseSpec())
                    .extract()
                    .response();

            // პასუხის დამუშავება
            String responseBody = response.getBody().asString();
            accountDetailsList.addAll(extractAccountBalances(responseBody, accountNumber));

            //  BuyRate-ის დამატება მხოლოდ USD ვალუტისთვის
            double buyRate = getCorrectBuyRateFromApi();
            for (AccountDetails account : accountDetailsList) {
                if ("USD".equals(account.getCurrency())) {
                    account.setBuyRate(buyRate);
                } else {
                    account.setBuyRate(1.0);
                }
            }

        } catch (Exception e) {
            System.err.println("API-დან ანგარიშის ბალანსების მიღების შეცდომა: " + e.getMessage());
        }

        return accountDetailsList;
    }

    // JSON-დან ბალანსების ამოღება
    private static List<AccountDetails> extractAccountBalances(String response, String accountNumber) {
        List<AccountDetails> accountDetailsList = new ArrayList<>();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonResponse = objectMapper.readTree(response);
            JsonNode resultArray = jsonResponse.get("Result");

            if (resultArray != null && resultArray.isArray()) {
                for (JsonNode accountObj : resultArray) {
                    if (accountNumber.equals(accountObj.get("AccountNumber").asText().trim())) {

                        // AvailableBalance დამუშავება
                        double availableBalance = 0.0;
                        if (accountObj.has("AvailableBalance") && !accountObj.get("AvailableBalance").isNull()) {
                            String balanceText = accountObj.get("AvailableBalance").asText().trim();
                            if (!balanceText.isEmpty() && balanceText.matches("-?\\d+(\\.\\d+)?")) {
                                availableBalance = Double.parseDouble(balanceText);
                            } else {
                                System.err.println("AvailableBalance არ არის რიცხვი: " + balanceText);
                            }
                        }
                        // Currency წამოღება
                        String currency = "UNKNOWN";
                        if (accountObj.has("Currency") && !accountObj.get("Currency").isNull()) {
                            currency = accountObj.get("Currency").asText().trim();
                        }

                        // ვქმნით AccountDetails ობიექტს
                        AccountDetails accountDetails = new AccountDetails(
                                accountNumber,
                                currency,
                                availableBalance,
                                1.0 // BuyRate, რომელიც შემდეგ განახლდება
                        );

                        accountDetailsList.add(accountDetails);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("JSON Parsing-ის შეცდომა: " + e.getMessage());
        }
        return accountDetailsList;
    }

    // ვალუტის კურსის მიღება მონაცემთა ბაზიდან
    public static double getCorrectBuyRate() {
        if (cachedBuyRate > 0) {
            return cachedBuyRate;
        }

        try (Connection dataBaseAccessSql = DataBaseAccessSQL.getConnectionSMS();
             PreparedStatement preparedStatement = dataBaseAccessSql.prepareStatement(QUERY_CURRENCY_RATE)) {

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                String response = resultSet.getString("Response");

                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonResponse = objectMapper.readTree(response);
                JsonNode resultArray = jsonResponse.get("Result");

                if (resultArray != null && resultArray.isArray()) {
                    for (JsonNode rateObj : resultArray) {
                        if (rateObj.has("Currency") && "GEL".equals(rateObj.get("Currency").asText().trim())) {
                            if (rateObj.has("BuyRate") && !rateObj.get("BuyRate").isNull()) {
                                double buyRate = rateObj.get("BuyRate").asDouble();
                                cachedBuyRate = buyRate;
                                return buyRate;
                            }
                        }
                    }
                }
            }

            // თუ აქამდე მოვედით, ვერ ვიპოვეთ კურსი
            throw new SQLException("GEL ვალუტის კურსი ვერ მოიძებნა");

        } catch (Exception e) {
            System.err.println("ვალუტის კურსის მიღების შეცდომა: " + e.getMessage());
            throw new RuntimeException("კურსის მიღება ვერ მოხერხდა: " + e.getMessage());
        }
    }

    // ვალუტის კურსის მიღება პირდაპირ API-დან
    public static double getCorrectBuyRateFromApi() {
        if (cachedBuyRate > 0) {
            return cachedBuyRate;
        }

        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("Currency", "USD");
            requestBody.put("ChannelTypeId", 8);
            requestBody.put("ChanelId", 98);

            // API მოთხოვნის გაგზავნა
            Response response = ApiRequestSpec.postRequestSpec()
                    .body(requestBody.toString())
                    .when()
                    .post("/api/Core/CurrencyRateList")
                    .then()
                    .spec(ApiRequestSpec.postResponseSpec())
                    .extract()
                    .response();

            String responseBody = response.getBody().asString();

            // JSON პასუხის დამუშავება
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonResponse = objectMapper.readTree(responseBody);
            JsonNode resultArray = jsonResponse.get("Result");

            if (resultArray != null && resultArray.isArray()) {
                for (JsonNode rateObj : resultArray) {
                    if (rateObj.has("Currency") && "GEL".equals(rateObj.get("Currency").asText().trim())) {
                        if (rateObj.has("BuyRate") && !rateObj.get("BuyRate").isNull()) {
                            double buyRate = rateObj.get("BuyRate").asDouble();
                            cachedBuyRate = buyRate;
                            return buyRate;
                        }
                    }
                }
            }

            // თუ აქამდე მოვედით, ვერ ვიპოვეთ კურსი
            throw new RuntimeException("GEL ვალუტის კურსი ვერ მოიძებნა API პასუხში");

        } catch (Exception e) {
            System.err.println("API-დან ვალუტის კურსის მიღების შეცდომა: " + e.getMessage());
            throw new RuntimeException("კურსის მიღება ვერ მოხერხდა: " + e.getMessage());
        }
    }
}
