package dataController;
import com.fasterxml.jackson.databind.JsonNode;
import dataBaseAccessSQL.DataBaseAccessSQL;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.cardModule.MoneyTransfer;

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
}
