package steps;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import dataController.DataControllerTransfer;
import elements.MyCredoWebElements;
import models.cardModule.Transaction;

import java.time.Duration;
import java.util.List;

import static com.codeborne.selenide.Condition.visible;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.FileAssert.fail;

public class TransactionSteps extends MyCredoWebElements {

    public void clickHiddenElement(SelenideElement element) {
        Selenide.executeJavaScript("arguments[0].click()", element);
    }

    public TransactionSteps verifyDebitTransactionDetails() {
        try {
            clickHiddenElement(CreditTransactionUI);

            String senderAccountUIText = SenderDetailsUI.shouldBe(visible, Duration.ofSeconds(30)).getText().trim();
            String receiverAccountUIText = ReceiverDetailsUI.shouldBe(visible, Duration.ofSeconds(30)).getText().trim();
            String amountWithCurrencyUI = AmountAndCurrencyUI.shouldBe(visible, Duration.ofSeconds(30)).getText().trim();
            String operationTypeUIText = OperationTypeUI.shouldBe(visible, Duration.ofSeconds(30)).getText().trim();

            System.out.println("\nUI გადარიცხვის ტრანზაქციის დეტალები:");
            System.out.println("გამგზავნი: " + senderAccountUIText);
            System.out.println("მიმღები: " + receiverAccountUIText);
            System.out.println("თანხა და ვალუტა: " + amountWithCurrencyUI);
            System.out.println("ოპერაციის ტიპი: " + operationTypeUIText);

            // ანგარიშის ნომრებიდან მოვაშოროთ GEL სუფიქსი
            String senderAccountClean = cleanAccountNumber(senderAccountUIText);
            String receiverAccountClean = cleanAccountNumber(receiverAccountUIText);

            // ტრანზაქციის მოძებნა ბაზაში
            List<Transaction> transactions = DataControllerTransfer.getTransactionDetailsFromDB(
                    senderAccountClean, receiverAccountClean);

            // დებეტის ტრანზაქციის პოვნა
            Transaction transaction = findTransactionInList(transactions, true);

            if (transaction == null) {
                fail("მონაცემთა ბაზაში ვერ მოიძებნა დებეტის ტრანზაქცია!");
            }

            printTransactionDetails(transaction, "გადარიცხვის");

            // ანგარიშების შემოწმება
            verifyAccountNumbers(transaction, senderAccountClean, receiverAccountClean, true);

            // თანხის შემოწმება
            verifyAmount(transaction.getFormattedAmountWithCurrency(), amountWithCurrencyUI);

            // ოპერაციის ტიპის შემოწმება
            verifyOperationType(transaction.getOperationTypeApi(), operationTypeUIText);

            System.out.println("ყველა შემოწმება წარმატებით დასრულდა!");

            CloseDetails.click();
            return this;

        } catch (Exception e) {
            System.err.println("შეცდომა დებეტის ტრანზაქციის შემოწმებისას: " + e.getMessage());
            System.err.println("შეცდომის მიზეზი: " + e.getCause());
            if (e.getCause() != null) {
                e.getCause().printStackTrace();
            }
            fail("შეცდომა ტრანზაქციის შემოწმებისას: " + e.getMessage());
            return this;
        }
    }

     //  ამოწმებს კრედიტის ტრანზაქციის დეტალებს UI-სა და ბაზას შორის

    public void verifyCreditTransactionDetails() {
        try {
            clickHiddenElement(DebitTransactionUI);

            String senderAccountUIText = SenderDetailsUI.shouldBe(visible, Duration.ofSeconds(30)).getText().trim();
            String receiverAccountUIText = ReceiverDetailsUI.shouldBe(visible, Duration.ofSeconds(30)).getText().trim();
            String amountWithCurrencyUI = AmountAndCurrencyUI.shouldBe(visible, Duration.ofSeconds(30)).getText().trim();
            String operationTypeUIText = OperationTypeUI.shouldBe(visible, Duration.ofSeconds(30)).getText().trim();

            System.out.println("\nUI ჩარიცხვის ტრანზაქციის დეტალები:");
            System.out.println("გამგზავნი: " + senderAccountUIText);
            System.out.println("მიმღები: " + receiverAccountUIText);
            System.out.println("თანხა და ვალუტა: " + amountWithCurrencyUI);
            System.out.println("ოპერაციის ტიპი: " + operationTypeUIText);

            // ანგარიშის ნომრებიდან მოვაშოროთ GEL სუფიქსი
            String senderAccountClean = cleanAccountNumber(senderAccountUIText);
            String receiverAccountClean = cleanAccountNumber(receiverAccountUIText);

            // ტრანზაქციის მოძებნა ბაზაში
            List<Transaction> transactions = DataControllerTransfer.getTransactionDetailsFromDB(
                    senderAccountClean, receiverAccountClean);

            // კრედიტის ტრანზაქციის პოვნა
            Transaction transaction = findTransactionInList(transactions, false);

            if (transaction == null) {
                fail("მონაცემთა ბაზაში ვერ მოიძებნა კრედიტის ტრანზაქცია!");
            }

            printTransactionDetails(transaction, "ჩარიცხვის");

            // ანგარიშების შემოწმება (კრედიტის შემთხვევაში პირიქით)
            verifyAccountNumbers(transaction, senderAccountClean, receiverAccountClean, false);

            // თანხის შემოწმება
            String expectedAmount = amountWithCurrencyUI;
            if (expectedAmount.startsWith("-")) {
                expectedAmount = expectedAmount.substring(1);
            }
            assertEquals(transaction.getFormattedAmountWithCurrency(), expectedAmount,
                    "თანხა და ვალუტა არ ემთხვევა!");

            // ოპერაციის ტიპის შემოწმება
            verifyOperationType(transaction.getOperationTypeApi(), operationTypeUIText);

            System.out.println("ყველა შემოწმება წარმატებით დასრულდა!");

            CloseDetails.click();

        } catch (Exception e) {
            System.err.println("შეცდომა კრედიტის ტრანზაქციის შემოწმებისას: " + e.getMessage());
            System.err.println("შეცდომის მიზეზი: " + e.getCause());
            if (e.getCause() != null) {
                e.getCause().printStackTrace();
            }
            fail("შეცდომა ტრანზაქციის შემოწმებისას: " + e.getMessage());
        }
    }


     // ანგარიშის ნომრიდან აშორებს GEL სუფიქსს თუ არსებობს

    private String cleanAccountNumber(String accountText) {
        if (accountText != null && accountText.endsWith("GEL")) {
            return accountText.substring(0, accountText.length() - 3).trim();
        }
        return accountText;
    }


     // ტრანზაქციების სიიდან პოულობს კონკრეტული ტიპის ტრანზაქციას
     // isDebit - true დებეტისთვის, false კრედიტისთვის

    private Transaction findTransactionInList(List<Transaction> transactions, boolean isDebit) {
        if (isDebit) {
            return transactions.stream()
                    .filter(t -> t.getDebit() != null && t.getDebit() > 0)
                    .findFirst()
                    .orElse(null);
        } else {
            return transactions.stream()
                    .filter(t -> t.getCredit() != null && t.getCredit() > 0)
                    .findFirst()
                    .orElse(null);
        }
    }

     // API-დან წამოსული ტრანზაქციის დეტალების ბეჭდვა
     // transactionType - ტრანზაქციის ტიპი: "გადარიცხვის" ან "ჩარიცხვის"

    private void printTransactionDetails(Transaction transaction, String transactionType) {
        System.out.println("\nAPI " + transactionType + " ტრანზაქციის დეტალები:");

        if (transaction.isDebitTransaction()) {
            System.out.println("გამგზავნი ანგარიში: " + transaction.getAccountNumberApi());
            System.out.println("მიმღები ანგარიში: " + transaction.getContragentAccount());
        } else {
            System.out.println("გამგზავნი ანგარიში: " + transaction.getContragentAccount());
            System.out.println("მიმღები ანგარიში: " + transaction.getAccountNumberApi());
        }

        System.out.println("თანხა: " + transaction.getAmountApi());
        System.out.println("ვალუტა: " + transaction.getCurrencyApi());
        System.out.println("თანხა ფორმატირებული: " + transaction.getFormattedAmountWithCurrency());
        System.out.println("ოპერაციის ტიპი: " + transaction.getOperationTypeApi());
    }

     // ანგარიშის ნომრების შემოწმება
     // isDebit - true დებეტისთვის, false კრედიტისთვის

    private void verifyAccountNumbers(Transaction transaction, String senderAccount,
                                      String receiverAccount, boolean isDebit) {
        if (isDebit) {
            verifyAccountNumber(transaction.getAccountNumberApi(), senderAccount, "გამგზავნი ანგარიშის");
            verifyAccountNumber(transaction.getContragentAccount(), receiverAccount, "მიმღები ანგარიშის");
        } else {
            verifyAccountNumber(transaction.getAccountNumberApi(), receiverAccount, "მიმღები ანგარიშის");
            verifyAccountNumber(transaction.getContragentAccount(), senderAccount, "გამგზავნი ანგარიშის");
        }
    }
     // ერთი ანგარიშის ნომრის შემოწმება მოქნილი ალგორითმით

    private void verifyAccountNumber(String actual, String expected, String accountType) {
        boolean matches = actual.equals(expected) ||
                actual.contains(expected) ||
                expected.contains(actual);

        assertTrue(matches, accountType + " ნომრები არ ემთხვევა! მოსალოდნელი: " +
                expected + ", რეალური: " + actual);
    }

     // თანხების შედარება მინუს ნიშნის გათვალისწინებით

    private void verifyAmount(String actualAmount, String expectedAmount) {
        // თანხის შემოწმება მინუს ნიშნის გათვალისწინებით
        if (expectedAmount.startsWith("-") && !actualAmount.startsWith("-")) {
            actualAmount = "-" + actualAmount;
        } else if (!expectedAmount.startsWith("-") && actualAmount.startsWith("-")) {
            actualAmount = actualAmount.substring(1);
        }

        assertEquals(actualAmount, expectedAmount, "თანხა და ვალუტა არ ემთხვევა!");
    }

     // ოპერაციის ტიპების შედარება

    private void verifyOperationType(String dbOperationType, String uiOperationType) {
        boolean operationTypeMatches = uiOperationType.equals(dbOperationType) ||
                uiOperationType.contains(dbOperationType) ||
                dbOperationType.contains(uiOperationType);

        assertTrue(operationTypeMatches, "ოპერაციის ტიპი არ ემთხვევა! მოსალოდნელი: " +
                uiOperationType + ", რეალური: " + dbOperationType);
    }
}
