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

    public TransactionSteps clickHiddenElement(SelenideElement element) {
        Selenide.executeJavaScript("arguments[0].click()", element);
        return this;
    }

    public TransactionSteps verifyDebitTransactionDetails() {
        try {
                // ვაკლიკებთ კრედიტის ელემენტზე დებეტის მეთოდში
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

            // ვეძებთ სწორ ტრანზაქციას ბაზიდან
            // დებეტის ტრანზაქციისთვის (გასავალი) debit არის შევსებული და credit არის null
            Transaction transaction = transactions.stream()
                    .filter(t -> t.getDebit() != null && t.getDebit() > 0)
                    .findFirst()
                    .orElse(null);

            if (transaction != null) {
                System.out.println("\nDB დებეტის ტრანზაქციის დეტალები:");
                System.out.println("გამგზავნი ანგარიში: " + transaction.getAccountNumberApi());
                System.out.println("მიმღები ანგარიში: " + transaction.getContragentAccount());
                System.out.println("თანხა: " + transaction.getAmountApi());
                System.out.println("ვალუტა: " + transaction.getCurrencyApi());
                System.out.println("თანხა ფორმატირებული: " + transaction.getFormattedAmountWithCurrency());
                System.out.println("ოპერაციის ტიპი: " + transaction.getOperationTypeApi());

                // გამგზავნი ანგარიშის შემოწმება
                if (!senderAccountClean.equals(transaction.getAccountNumberApi())) {
                    System.err.println("გამგზავნი ანგარიშის ნომრები არ ემთხვევა:");
                    System.err.println("UI: [" + senderAccountClean + "]");
                    System.err.println("DB: [" + transaction.getAccountNumberApi() + "]");
                    Assert.fail("გამგზავნი ანგარიშის ნომრები არ ემთხვევა!");
                }

                // მიმღები ანგარიშის შემოწმება
                if (!receiverAccountClean.equals(transaction.getContragentAccount())) {
                    System.err.println("მიმღები ანგარიშის ნომრები არ ემთხვევა:");
                    System.err.println("UI: [" + receiverAccountClean + "]");
                    System.err.println("DB: [" + transaction.getContragentAccount() + "]");
                    Assert.fail("მიმღები ანგარიშის ნომრები არ ემთხვევა!");
                }

                // თანხის შემოწმება - გათვალისწინებით დებეტის ნიშნის
                // UI-ში შეიძლება იყოს ან წინ მინუსით, ან სუფთა რიცხვი
                // DB-ში დებეტის შემთხვევაში formattedAmountWithCurrency-ს აქვს მინუსი
                String expectedAmount = amountWithCurrencyUI;
                String actualAmount = transaction.getFormattedAmountWithCurrency();

                // თუ UI-ზე არ არის მინუს ნიშანი, მაგრამ DB-ში ფორმატირებულ თანხას აქვს მინუსი
                if (!amountWithCurrencyUI.startsWith("-") && actualAmount.startsWith("-")) {
                    // აქ უნდა შევადაროთ UI თანხა და DB თანხა მინუსის გარეშე
                    String dbAmountWithoutMinus = actualAmount.substring(1);  // მინუსის მოჭრა DB თანხიდან

                    if (!expectedAmount.equals(dbAmountWithoutMinus)) {
                        System.err.println("თანხა და ვალუტა არ ემთხვევა (მინუს ნიშნის გარეშე):");
                        System.err.println("UI: [" + expectedAmount + "]");
                        System.err.println("DB (მინუსის გარეშე): [" + dbAmountWithoutMinus + "]");
                        Assert.fail("თანხა და ვალუტა არ ემთხვევა!");
                    }
                } else {
                    // თუ ორივე ერთნაირი ფორმატისაა (ორივეს აქვს მინუსი ან არცერთს არ აქვს)
                    if (!expectedAmount.equals(actualAmount)) {
                        System.err.println("თანხა და ვალუტა არ ემთხვევა:");
                        System.err.println("UI: [" + expectedAmount + "]");
                        System.err.println("DB: [" + actualAmount + "]");
                        Assert.fail("თანხა და ვალუტა არ ემთხვევა!");
                    }
                }

                // ოპერაციის ტიპის შემოწმება
                boolean operationTypeMatches = operationTypeUIText.contains(transaction.getOperationTypeApi()) ||
                        transaction.getOperationTypeApi().contains(operationTypeUIText);

                if (!operationTypeMatches) {
                    System.err.println("ოპერაციის ტიპი არ ემთხვევა:");
                    System.err.println("UI: [" + operationTypeUIText + "]");
                    System.err.println("DB: [" + transaction.getOperationTypeApi() + "]");
                    Assert.fail("ოპერაციის ტიპი არ ემთხვევა!");
                }

                System.out.println("ყველა შემოწმება წარმატებით დასრულდა!");
            } else {
                Assert.fail("მონაცემთა ბაზაში ვერ მოიძებნა დებეტის ტრანზაქცია!");
            }

            CloseDetails.click();
            return this;
        } catch (Exception e) {
            Assert.fail("დებეტის ტრანზაქციის შემოწმებისას მოხდა შეცდომა: " + e.getMessage());
            return this;
        }
    }

    public TransactionSteps verifyCreditTransactionDetails() {
        try {
            clickHiddenElement(DebitTransactionUI);

            String senderAccountUIText = SenderDetailsUI.shouldBe(visible, Duration.ofSeconds(30)).getText().trim();
            String receiverAccountUIText = ReceiverDetailsUI.shouldBe(visible, Duration.ofSeconds(30)).getText().trim();
            String amountWithCurrencyUI = AmountAndCurrencyUI.shouldBe(visible, Duration.ofSeconds(30)).getText().trim();
            String operationTypeUIText = OperationTypeUI.shouldBe(visible, Duration.ofSeconds(30)).getText().trim();

            System.out.println("\nUI კრედიტის ტრანზაქციის დეტალები:");
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

            // ვეძებთ ტრანზაქციას სადაც credit არის შევსებული და debit არის null
            Transaction transaction = transactions.stream()
                    .filter(t -> t.getCredit() != null && t.getCredit() > 0)
                    .findFirst()
                    .orElse(null);

            if (transaction != null) {
                System.out.println("\nDB კრედიტის ტრანზაქციის დეტალები:");
                System.out.println("გამგზავნი ანგარიში: " + transaction.getContragentAccount());
                System.out.println("მიმღები ანგარიში: " + transaction.getAccountNumberApi());
                System.out.println("თანხა: " + transaction.getAmountApi());
                System.out.println("ვალუტა: " + transaction.getCurrencyApi());
                System.out.println("თანხა ფორმატირებული: " + transaction.getFormattedAmountWithCurrency());
                System.out.println("ოპერაციის ტიპი: " + transaction.getOperationTypeApi());

                // ვადარებთ ანგარიშებს
                if (!receiverAccountClean.equals(transaction.getAccountNumberApi())) {
                    System.err.println("მიმღები ანგარიშის ნომრები არ ემთხვევა:");
                    System.err.println("UI: [" + receiverAccountClean + "]");
                    System.err.println("DB: [" + transaction.getAccountNumberApi() + "]");
                    Assert.fail("მიმღები ანგარიშის ნომრები არ ემთხვევა!");
                }

                if (!senderAccountClean.equals(transaction.getContragentAccount())) {
                    System.err.println("გამგზავნი ანგარიშის ნომრები არ ემთხვევა:");
                    System.err.println("UI: [" + senderAccountClean + "]");
                    System.err.println("DB: [" + transaction.getContragentAccount() + "]");
                    Assert.fail("გამგზავნი ანგარიშის ნომრები არ ემთხვევა!");
                }

                // თანხის შემოწმება კრედიტის ტრანზაქციისთვის
                // კრედიტის შემთხვევაში ჩვეულებრივ დადებითი თანხა უნდა იყოს ორივეგან
                String expectedAmount = amountWithCurrencyUI;
                String actualAmount = transaction.getFormattedAmountWithCurrency();

                // თანხაზე მინუსის მოშორება თუ UI-ზე არის, მაგრამ არ არის საჭირო კრედიტისთვის
                if (expectedAmount.startsWith("-")) {
                    expectedAmount = expectedAmount.substring(1);
                }

                if (!expectedAmount.equals(actualAmount)) {
                    System.err.println("თანხა და ვალუტა არ ემთხვევა:");
                    System.err.println("UI: [" + expectedAmount + "]");
                    System.err.println("DB: [" + actualAmount + "]");
                    Assert.fail("თანხა და ვალუტა არ ემთხვევა!");
                }

                // ვადარებთ ოპერაციის ტიპს
                // შევცვალოთ ჩარიცხვის შემოწმება, რომ გაითვალისწინოს "საკუთარ ანგარიშებს შორის გადარიცხვა"
                boolean operationTypeMatches = operationTypeUIText.equals(transaction.getOperationTypeApi()) ||
                        operationTypeUIText.contains(transaction.getOperationTypeApi()) ||
                        transaction.getOperationTypeApi().contains(operationTypeUIText);

                if (!operationTypeMatches) {
                    System.err.println("ოპერაციის ტიპი არ ემთხვევა:");
                    System.err.println("UI: [" + operationTypeUIText + "]");
                    System.err.println("DB: [" + transaction.getOperationTypeApi() + "]");
                    Assert.fail("ოპერაციის ტიპი არ ემთხვევა!");
                }

                System.out.println("ყველა შემოწმება წარმატებით დასრულდა!");
            } else {
                Assert.fail("მონაცემთა ბაზაში ვერ მოიძებნა კრედიტის ტრანზაქცია!");
            }

            CloseDetails.click();
            return this;
        } catch (Exception e) {
            Assert.fail("კრედიტის ტრანზაქციის შემოწმებისას მოხდა შეცდომა: " + e.getMessage());
            return this;
        }
    }
}
