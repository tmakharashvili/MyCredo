package steps;

import com.codeborne.selenide.Condition;
import dataController.DataControllerTransfer;
import elements.MyCredoWebElements;
import io.restassured.response.Response;
import models.cardModule.MoneyTransfer;
import models.cardModule.Transaction;
import org.testng.Assert;

import java.sql.SQLException;
import java.time.Duration;
import java.util.List;

import static com.codeborne.selenide.Condition.visible;
import static org.testng.Assert.assertEquals;

public class TransferSteps extends MyCredoWebElements {
    private String actualAccountNumber;


    public TransferSteps checkTransferNavigation() {
        actualAccountNumber = AccountNumberUI.getText().trim();

        TransferButton.click();
        TransfersHeader.shouldBe(visible);

        return this;
    }

    public TransferSteps moneyTransfer() {
        OwnAccounts.click();
        String expectedAccountNumber = FromInput.getText().trim();
        System.out.println(expectedAccountNumber);

        assertEquals(actualAccountNumber, expectedAccountNumber, "ანგარიშის ნომრები არ ემთხვევა!");

        WhereInput.click();
        AccountsPopup.shouldBe(visible, Duration.ofSeconds(20));
        AnotherAccount.click();
        AnotherAccountsCurrency.click();
        AmountInput.setValue("11");
        Transfer.click();

        return this;
    }

    public TransferSteps verifyTransferDetails() {
        try {
            // UI-დან მონაცემების აღება
            String senderAccountUI = SenderNumberUI.getText().trim();
            String receiverAccountUI = ReceiverNumberUI.getText().trim();
            String amountWithCurrencyUI = AmountUI.getText().trim();

            // API/DB-დან მონაცემების აღება
            List<MoneyTransfer> transfers = DataControllerTransfer.getTransferDetailsFromDB(senderAccountUI, receiverAccountUI);

            if (!transfers.isEmpty()) {
                MoneyTransfer transfer = transfers.get(0);

                String currencySymbol = transfer.getCurrencyApi().equalsIgnoreCase("GEL") ? "₾" : transfer.getCurrencyApi();

                // ფორმატირებული თანხა ვალუტის სიმბოლოთი
                String formattedAmountWithSymbol = String.format("%.2f %s", transfer.getAmountApi(), currencySymbol);

                // დეტალების გამოტანა ლოგში
                System.out.println("UI გადარიცხვის დეტალები:");
                System.out.println("გამგზავნი ანგარიში: " + senderAccountUI);
                System.out.println("მიმღები ანგარიში: " + receiverAccountUI);
                System.out.println("თანხა და ვალუტა: " + amountWithCurrencyUI);

                System.out.println("\nAPI გადარიცხვის დეტალები:");
                System.out.println("გამგზავნი ანგარიში: " + transfer.getSenderAccountNumberApi());
                System.out.println("მიმღები ანგარიში: " + transfer.getReceiverAccountNumberApi());
                System.out.println("თანხა: " + transfer.getAmountApi());
                System.out.println("ვალუტა: " + transfer.getCurrencyApi());
                System.out.println("ფორმატირებული თანხა და ვალუტა: " + formattedAmountWithSymbol);

                assertEquals(senderAccountUI, transfer.getSenderAccountNumberApi(), "გამგზავნი ანგარიშის ნომრები არ ემთხვევა!");
                assertEquals(receiverAccountUI, transfer.getReceiverAccountNumberApi(), "მიმღები ანგარიშის ნომრები არ ემთხვევა!");
                assertEquals(amountWithCurrencyUI, formattedAmountWithSymbol, "თანხა და ვალუტა არ ემთხვევა!");
            } else {
                Assert.fail("მონაცემთა ბაზაში ვერ მოიძებნა გადარიცხვა მითითებული ანგარიშებით!");
            }
            CloseSuccessPage.click();
            HomePageLogo.click();

            return this;
        } catch (SQLException e) {
            Assert.fail("გადარიცხვის შემოწმებისას მოხდა შეცდომა: " + e.getMessage());
            return this;
        }
    }
}
