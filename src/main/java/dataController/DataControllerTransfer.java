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
            FROM LogDB.dbo.MyCredoExternalApiLog (nolock)
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
            SELECT TOP 2 Response
            FROM LogDB.dbo.MyCredoExternalApiLog (nolock)
            WHERE ActionUrl LIKE '%transaction/%' AND ActionUrl LIKE '%detail%'
            ORDER BY CreateDate DESC
            """;


    public static List<Transaction> getTransactionDetailsFromDB(String senderAccount, String receiverAccount) {
        List<Transaction> transactionList = new ArrayList<>();

        try {
            Connection dataBaseAccessSql = DataBaseAccessSQL.getConnectionSMS();
            PreparedStatement preparedStatement = dataBaseAccessSql.prepareStatement(QUERY_TRANSACTION_DETAILS);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String response = resultSet.getString("Response");

                List<Transaction> parsedTransactions = parseJsonTransactions(response);

                for (Transaction transaction : parsedTransactions) {
                    if (matchesAccounts(transaction, senderAccount, receiverAccount)) {
                        transactionList.add(transaction);
                    }
                }
            }

            // ბაზის დახურვა
            resultSet.close();
            preparedStatement.close();
            dataBaseAccessSql.close();

            System.out.println("სულ ნაპოვნია " + transactionList.size() + " ტრანზაქცია");

            if (transactionList.isEmpty()) {
                System.out.println("ტრანზაქციები ვერ მოიძებნა მითითებული ანგარიშებისთვის: " +
                        senderAccount + " -> " + receiverAccount);
            }

            return transactionList;

        } catch (Exception e) {
            System.err.println("შეცდომა ბაზიდან მონაცემების ამოღებისას: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private static boolean matchesAccounts(Transaction transaction, String senderAccount, String receiverAccount) {
        if (transaction.isDebitTransaction()) {
            boolean senderMatch = transaction.getAccountNumberApi() != null &&
                    (transaction.getAccountNumberApi().equals(senderAccount) ||
                            senderAccount.contains(transaction.getAccountNumberApi()) ||
                            transaction.getAccountNumberApi().contains(senderAccount));

            boolean receiverMatch = transaction.getContragentAccount() != null &&
                    (transaction.getContragentAccount().equals(receiverAccount) ||
                            receiverAccount.contains(transaction.getContragentAccount()) ||
                            transaction.getContragentAccount().contains(receiverAccount));

            return senderMatch && receiverMatch;
        }
        else if (transaction.isCreditTransaction()) {
            boolean receiverMatch = transaction.getAccountNumberApi() != null &&
                    (transaction.getAccountNumberApi().equals(receiverAccount) ||
                            receiverAccount.contains(transaction.getAccountNumberApi()) ||
                            transaction.getAccountNumberApi().contains(receiverAccount));

            boolean senderMatch = transaction.getContragentAccount() != null &&
                    (transaction.getContragentAccount().equals(senderAccount) ||
                            senderAccount.contains(transaction.getContragentAccount()) ||
                            transaction.getContragentAccount().contains(senderAccount));

            return senderMatch && receiverMatch;
        }
        return false;
    }

    //json მონაცემების დამუშავება ტრანზაქციების სიად

    public static List<Transaction> parseJsonTransactions(String jsonData) {
        List<Transaction> transactionList = new ArrayList<>();

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(jsonData);

            // JSON მასივის დამუშავება
            if (root.isArray()) {
                for (JsonNode node : root) {
                    if (node.has("data")) {
                        addTransactionFromData(node.get("data"), transactionList);
                    }
                }
            }
            // JSON ობიექტის დამუშავება
            else if (root.isObject() && root.has("data")) {
                addTransactionFromData(root.get("data"), transactionList);
            }
        } catch (Exception e) {
            System.err.println("JSON მონაცემების დამუშავებისას დაფიქსირდა შეცდომა: " + e.getMessage());
        }

        return transactionList;
    }

    // JSON data ობიექტიდან ტრანზაქციის შექმნა და სიაში დამატება

    private static void addTransactionFromData(JsonNode data, List<Transaction> transactionList) {
        if (data == null) return;

        // JSON-დან საჭირო ველების წაკითხვა
        String accountNumber = getTextValue(data, "accountNumber");
        String contragentAccount = getTextValue(data, "contragentAccount");
        String currency = getTextValue(data, "currency");
        String operationType = getTextValue(data, "operationType");

        Double amount = null;
        if (data.has("amount")) {
            amount = data.get("amount").asDouble();
        }

        // დავაგენერიროთ დებეტის ტრანზაქცია
        if (data.has("debit") && !data.get("debit").isNull() && data.get("debit").asDouble() > 0) {
            Transaction transaction = new Transaction();
            transaction.setAccountNumberApi(accountNumber);
            transaction.setContragentAccount(contragentAccount);
            transaction.setAmountApi(amount);
            transaction.setCurrencyApi(currency);
            transaction.setOperationTypeApi(operationType);
            transaction.setDebit(data.get("debit").asDouble());
            transaction.setCredit(null);
            transactionList.add(transaction);
        }

        // დავაგენერიროთ კრედიტის ტრანზაქცია
        if (data.has("credit") && !data.get("credit").isNull() && data.get("credit").asDouble() > 0) {
            Transaction transaction = new Transaction();
            transaction.setAccountNumberApi(accountNumber);
            transaction.setContragentAccount(contragentAccount);
            transaction.setAmountApi(amount);
            transaction.setCurrencyApi(currency);
            transaction.setOperationTypeApi(operationType);
            transaction.setCredit(data.get("credit").asDouble());
            transaction.setDebit(null);
            transactionList.add(transaction);
        }
    }

    // JSON ობიექტიდან ტექსტური მნიშვნელობის ამოღება

    private static String getTextValue(JsonNode node, String fieldName) {
        return node.has(fieldName) && !node.get(fieldName).isNull() ? node.get(fieldName).asText() : null;
    }

    // API პასუხიდან ტრანზაქციების სიის გენერაცია

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