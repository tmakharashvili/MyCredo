package steps;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import dataController.DataControllerTransfer;
import elements.MyCredoWebElements;
import models.cardModule.Transaction;
import org.testng.Assert;

import java.time.Duration;
import java.util.List;

import static com.codeborne.selenide.Condition.visible;


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

            System.out.println("\nUI დებეტის ტრანზაქციის დეტალები:");
            System.out.println("გამგზავნი: " + senderAccountUIText);
            System.out.println("მიმღები: " + receiverAccountUIText);
            System.out.println("თანხა და ვალუტა: " + amountWithCurrencyUI);
            System.out.println("ოპერაციის ტიპი: " + operationTypeUIText);

            // ანგარიშის ნომრებიდან მოვაშოროთ GEL სუფიქსი
            String senderAccountClean = senderAccountUIText;
            if (senderAccountUIText.endsWith("GEL")) {
                senderAccountClean = senderAccountUIText.substring(0, senderAccountUIText.length() - 3).trim();
            }

            String receiverAccountClean = receiverAccountUIText;
            if (receiverAccountUIText.endsWith("GEL")) {
                receiverAccountClean = receiverAccountUIText.substring(0, receiverAccountUIText.length() - 3).trim();
            }

            List<Transaction> transactions = DataControllerTransfer.getTransactionDetailsFromDB(
                    senderAccountClean, receiverAccountClean);

            // ბაზიდან მონაცემების მიღება
            Transaction transaction = transactions.stream()
                    .filter(t -> t.getDebit() != null && t.getDebit() > 0)
                    .findFirst()
                    .orElse(null);

            if (transaction != null) {
                System.out.println("\nAPI დებეტის ტრანზაქციის დეტალები:");
                System.out.println("გამგზავნი ანგარიში: " + transaction.getAccountNumberApi());
                System.out.println("მიმღები ანგარიში: " + transaction.getContragentAccount());
                System.out.println("თანხა: " + transaction.getAmountApi());
                System.out.println("ვალუტა: " + transaction.getCurrencyApi());
                System.out.println("თანხა ფორმატირებული: " + transaction.getFormattedAmountWithCurrency());
                System.out.println("ოპერაციის ტიპი: " + transaction.getOperationTypeApi());

                // გამგზავნი ანგარიშის შემოწმება
                // TestNG-სთვის: Assert.assertEquals(actual, expected, message);
                Assert.assertEquals(transaction.getAccountNumberApi(), senderAccountClean,
                        "გამგზავნი ანგარიშის ნომრები არ ემთხვევა!");

                // მიმღები ანგარიშის შემოწმება
                Assert.assertEquals(transaction.getContragentAccount(), receiverAccountClean,
                        "მიმღები ანგარიშის ნომრები არ ემთხვევა!");

                // თანხის შემოწმება
                String expectedAmount = amountWithCurrencyUI;
                String actualAmount = transaction.getFormattedAmountWithCurrency();

                // კორექტული შედარებისთვის: ან ორივეს უნდა ჰქონდეს მინუსი, ან არცერთს
                if (expectedAmount.startsWith("-") && !actualAmount.startsWith("-")) {
                    actualAmount = "-" + actualAmount;  // დავამატოთ მინუსი
                } else if (!expectedAmount.startsWith("-") && actualAmount.startsWith("-")) {
                    actualAmount = actualAmount.substring(1);  // მოვაშოროთ მინუსი
                }

                Assert.assertEquals(actualAmount, expectedAmount, "თანხა და ვალუტა არ ემთხვევა!");

                // ოპერაციის ტიპის შემოწმება
                boolean operationTypeMatches = operationTypeUIText.contains(transaction.getOperationTypeApi()) ||
                        transaction.getOperationTypeApi().contains(operationTypeUIText);

                // TestNG-სთვის assertTrue-ში პირველი პარამეტრია ბულეანი, შემდეგ შეტყობინება
                Assert.assertTrue(operationTypeMatches, "ოპერაციის ტიპი არ ემთხვევა!");

                System.out.println("ყველა შემოწმება წარმატებით დასრულდა!");
            } else {
                Assert.fail("მონაცემთა ბაზაში ვერ მოიძებნა დებეტის ტრანზაქცია!");
            }

            CloseDetails.click();
            return this;
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("დებეტის ტრანზაქციის შემოწმებისას მოხდა შეცდომა: " + e.getMessage());
            return this;
        }
    }

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
            String senderAccountClean = senderAccountUIText;
            if (senderAccountUIText.endsWith("GEL")) {
                senderAccountClean = senderAccountUIText.substring(0, senderAccountUIText.length() - 3).trim();
            }

            String receiverAccountClean = receiverAccountUIText;
            if (receiverAccountUIText.endsWith("GEL")) {
                receiverAccountClean = receiverAccountUIText.substring(0, receiverAccountUIText.length() - 3).trim();
            }

            List<Transaction> transactions = DataControllerTransfer.getTransactionDetailsFromDB(
                    senderAccountClean, receiverAccountClean);

            // ბაზიდან მონაცემების მიღება
            Transaction transaction = transactions.stream()
                    .filter(t -> t.getCredit() != null && t.getCredit() > 0)
                    .findFirst()
                    .orElse(null);

            if (transaction != null) {
                System.out.println("\nAPI ჩარიცხვის ტრანზაქციის დეტალები:");
                System.out.println("გამგზავნი ანგარიში: " + transaction.getContragentAccount());
                System.out.println("მიმღები ანგარიში: " + transaction.getAccountNumberApi());
                System.out.println("თანხა: " + transaction.getAmountApi());
                System.out.println("ვალუტა: " + transaction.getCurrencyApi());
                System.out.println("თანხა ფორმატირებული: " + transaction.getFormattedAmountWithCurrency());
                System.out.println("ოპერაციის ტიპი: " + transaction.getOperationTypeApi());

                // მიმღები ანგარიშის შემოწმება - TestNG სინტაქსით
                Assert.assertEquals(transaction.getAccountNumberApi(), receiverAccountClean,
                        "მიმღები ანგარიშის ნომრები არ ემთხვევა!");

                // გამგზავნი ანგარიშის შემოწმება - TestNG სინტაქსით
                Assert.assertEquals(transaction.getContragentAccount(), senderAccountClean,
                        "გამგზავნი ანგარიშის ნომრები არ ემთხვევა!");

                // თანხის შემოწმება
                String expectedAmount = amountWithCurrencyUI;
                String actualAmount = transaction.getFormattedAmountWithCurrency();

                if (expectedAmount.startsWith("-")) {
                    expectedAmount = expectedAmount.substring(1);
                }

                Assert.assertEquals(actualAmount, expectedAmount, "თანხა და ვალუტა არ ემთხვევა!");

                // ოპერაციის ტიპის შემოწმება - TestNG სინტაქსით
                boolean operationTypeMatches = operationTypeUIText.equals(transaction.getOperationTypeApi()) ||
                        operationTypeUIText.contains(transaction.getOperationTypeApi()) ||
                        transaction.getOperationTypeApi().contains(operationTypeUIText);

                Assert.assertTrue(operationTypeMatches, "ოპერაციის ტიპი არ ემთხვევა!");

                System.out.println("ყველა შემოწმება წარმატებით დასრულდა!");
            } else {
                Assert.fail("მონაცემთა ბაზაში ვერ მოიძებნა კრედიტის ტრანზაქცია!");
            }

            CloseDetails.click();
        } catch (Exception e) {
            e.printStackTrace(); // დავამატე stack trace-ის ბეჭდვა
            Assert.fail("ტრანზაქციის შემოწმებისას მოხდა შეცდომა: " + e.getMessage());
        }
    }
}
