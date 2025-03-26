package dataController;
import com.fasterxml.jackson.databind.JsonNode;
import dataBaseAccessSQL.DataBaseAccessSQL;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.response.Response;
import models.cardModule.MoneyTransfer;
import models.cardModule.Transaction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DataControllerTransfer {
    private static final String QUERY_TRANSFER_DETAILS = """
                SELECT TOP 1 Request 
            FROM LogDB.dbo.MyCredoExternalApiLog 
            WHERE ActionUrl LIKE '%/api/v1/payments/MoneyTransfer%' 
            AND Request LIKE ? 
            ORDER BY CreateDate DESC
            """;

    public static List<MoneyTransfer> getTransferDetailsFromDB(String senderAccount, String receiverAccount) throws SQLException {
        List<MoneyTransfer> transferList = new ArrayList<>();
        try (Connection dataBaseAccessSql = DataBaseAccessSQL.getConnectionSMS();
             PreparedStatement preparedStatement = dataBaseAccessSql.prepareStatement(QUERY_TRANSFER_DETAILS)) {

            preparedStatement.setString(1, "%\"SenderAccountNumber\":\"" + senderAccount +
                    "%\"ReceiverAccountNumber\":\"" + receiverAccount + "%");

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String request = resultSet.getString("Request");
                MoneyTransfer transfer = extractTransferDetails(request, senderAccount, receiverAccount);
                if (transfer != null && transfer.getSenderAccountNumberApi() != null) {
                    transferList.add(transfer);
                }
            }
        }
        return transferList;
    }

    private static MoneyTransfer extractTransferDetails(String request, String senderAccount, String receiverAccount) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonRequest = objectMapper.readTree(request);
            JsonNode transferList = jsonRequest.get("MoneyTransferList");

            if (transferList != null && transferList.isArray()) {
                for (JsonNode transfer : transferList) {
                    String sender = transfer.get("SenderAccountNumber").asText();
                    String receiver = transfer.get("ReceiverAccountNumber").asText();

                    if (senderAccount.equals(sender) && receiverAccount.equals(receiver)) {
                        MoneyTransfer moneyTransfer = new MoneyTransfer();
                        moneyTransfer.setSenderAccountNumberApi(sender);
                        moneyTransfer.setReceiverAccountNumberApi(receiver);
                        moneyTransfer.setAmountApi(transfer.get("Amount").asDouble());
                        moneyTransfer.setCurrencyApi(transfer.get("CurrencyType").asText());
                        return moneyTransfer;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("მონაცემების ამოღებისას დაფიქსირდა შეცდომა: " + e.getMessage());
        }
        return new MoneyTransfer();
    }

    private static final String QUERY_TRANSACTION_DETAILS = """
            SELECT TOP 2 Request 
            FROM LogDB.dbo.MyCredoExternalApiLog 
            WHERE ActionUrl LIKE '%/api/v1/payments/MoneyTransfer%' 
            AND Request LIKE ? 
            ORDER BY CreateDate DESC
            """;

    public static List<Transaction> getTransactionDetailsFromDB(String senderAccount, String receiverAccount) throws SQLException {
        List<Transaction> transactionList = new ArrayList<>();
        try (Connection dataBaseAccessSql = DataBaseAccessSQL.getConnectionSMS();
             PreparedStatement preparedStatement = dataBaseAccessSql.prepareStatement(QUERY_TRANSACTION_DETAILS)) {

            preparedStatement.setString(1, "%\"SenderAccountNumber\":\"" + senderAccount +
                    "%\"ReceiverAccountNumber\":\"" + receiverAccount + "%");

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String request = resultSet.getString("Request");

                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    JsonNode jsonRequest = objectMapper.readTree(request);
                    JsonNode transferList = jsonRequest.get("MoneyTransferList");

                    if (transferList != null && transferList.isArray()) {
                        for (JsonNode transfer : transferList) {
                            String sender = transfer.get("SenderAccountNumber").asText();
                            String receiver = transfer.get("ReceiverAccountNumber").asText();

                            if (senderAccount.equals(sender) && receiverAccount.equals(receiver)) {
                                // ეს არის გადარიცხვის (დებეტის) ტრანზაქცია
                                Transaction debitTransaction = new Transaction();
                                debitTransaction.setAccountNumberApi(sender);
                                debitTransaction.setContragentAccount(receiver);
                                debitTransaction.setAmountApi(transfer.get("Amount").asDouble());
                                debitTransaction.setCurrencyApi(transfer.get("CurrencyType").asText());
                                debitTransaction.setDebit(transfer.get("Amount").asDouble());
                                debitTransaction.setOperationTypeApi("საკუთარ ანგარიშებს შორის გადარიცხვა");

                                // ეს არის ჩარიცხვის (კრედიტის) ტრანზაქცია
                                Transaction creditTransaction = new Transaction();
                                creditTransaction.setAccountNumberApi(receiver);
                                creditTransaction.setContragentAccount(sender);
                                creditTransaction.setAmountApi(transfer.get("Amount").asDouble());
                                creditTransaction.setCurrencyApi(transfer.get("CurrencyType").asText());
                                creditTransaction.setCredit(transfer.get("Amount").asDouble());
                                creditTransaction.setOperationTypeApi("საკუთარ ანგარიშებს შორის გადარიცხვა");

                                transactionList.add(debitTransaction);
                                transactionList.add(creditTransaction);
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println("მონაცემების ამოღებისას დაფიქსირდა შეცდომა: " + e.getMessage());
                }
            }
        }
        return transactionList;
    }

    public static List<Transaction> parseJsonTransactions(String jsonData) {
        List<Transaction> transactionList = new ArrayList<>();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(jsonData);

            // ვამუშავებთ JSON მასივს
            if (root.isArray()) {
                for (JsonNode node : root) {
                    if (node.has("data")) {
                        addTransactionFromData(node.get("data"), transactionList);
                    }
                }
            }
            // ვამუშავებთ ერთ JSON ობიექტს
            else if (root.isObject() && root.has("data")) {
                addTransactionFromData(root.get("data"), transactionList);
            }
        } catch (Exception e) {
            System.err.println("JSON მონაცემების დამუშავებისას დაფიქსირდა შეცდომა: " + e.getMessage());
        }

        return transactionList;
    }

    private static void addTransactionFromData(JsonNode data, List<Transaction> transactionList) {
        if (data == null) return;

        // ვკითხულობთ საჭირო ველებს JSON-დან
        String accountNumber = getTextValue(data, "accountNumber");
        String contragentAccount = getTextValue(data, "contragentAccount");
        String currency = getTextValue(data, "currency");
        String operationType = getTextValue(data, "operationType");

        Double amount = null;
        if (data.has("amount")) {
            amount = data.get("amount").asDouble();
        }

        // თუ გვაქვს დებეტი ტრანზაქციის ველი
        if (data.has("debit") && !data.get("debit").isNull()) {
            Transaction transaction = new Transaction();
            transaction.setAccountNumberApi(accountNumber);
            transaction.setContragentAccount(contragentAccount);
            transaction.setAmountApi(amount);
            transaction.setCurrencyApi(currency);
            transaction.setOperationTypeApi(operationType);
            transaction.setDebit(data.get("debit").asDouble());
            transactionList.add(transaction);

            System.out.println("დებეტის ტრანზაქცია (JSON-დან):");
            System.out.println("ანგარიშის ნომერი: " + accountNumber);
            System.out.println("კონტრაგენტი: " + contragentAccount);
            System.out.println("თანხა: " + amount);
            System.out.println("ვალუტა: " + currency);
            System.out.println("ოპერაციის ტიპი: " + operationType);
        }

        // თუ გვაქვს კრედიტის ტრანზაქციის ველი
        if (data.has("credit") && !data.get("credit").isNull()) {
            Transaction transaction = new Transaction();
            transaction.setAccountNumberApi(accountNumber);
            transaction.setContragentAccount(contragentAccount);
            transaction.setAmountApi(amount);
            transaction.setCurrencyApi(currency);
            transaction.setOperationTypeApi(operationType);
            transaction.setCredit(data.get("credit").asDouble());
            transactionList.add(transaction);

            System.out.println("კრედიტის ტრანზაქცია (JSON-დან):");
            System.out.println("ანგარიშის ნომერი: " + accountNumber);
            System.out.println("კონტრაგენტი: " + contragentAccount);
            System.out.println("თანხა: " + amount);
            System.out.println("ვალუტა: " + currency);
            System.out.println("ოპერაციის ტიპი: " + operationType);
        }
    }

    private static String getTextValue(JsonNode node, String fieldName) {
        return node.has(fieldName) && !node.get(fieldName).isNull() ? node.get(fieldName).asText() : null;
    }

    public static List<Transaction> getTransactionsFromResponse(Response response) {
        try {
            if (response != null) {
                return parseJsonTransactions(response.asString());
            }
        } catch (Exception e) {
            System.err.println("API პასუხის დამუშავებისას დაფიქსირდა შეცდომა: " + e.getMessage());
        }
        return new ArrayList<>();
    }
}