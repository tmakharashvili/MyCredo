package steps;

import dataController.DataControllerCardModule;
import elements.MyCredoWebElements;
import models.cardModule.AccountDetails;
import org.testng.Assert;
import java.util.List;

import static com.codeborne.selenide.Selenide.sleep;
import static dataController.DataControllerCardModule.*;

import models.cardModule.CardDetails;

public class CardBalanceSteps extends MyCredoWebElements {
    public CardBalanceSteps goToCards() {
        ProductsButton.click();
        AccountAndCards.click();
        EmigrantCard.click();
        sleep(3000);

        return this;
    }

    public CardBalanceSteps checkSelectedCardName() {
        try {
            // UI-დან წამოღებული ბარათის სახელი
            String cardNameFromUI = CardName.getText().trim();
            String accountNumberFromUI = AccountNumberUI.getText().trim();

            // API-დან წამოღებული ბარათის სახელი
            List<CardDetails> cardDetailsList = DataControllerCardModule.getCardDetailsFromDB(accountNumberFromUI);
            String cardNameFromAPI = cardDetailsList.isEmpty() ? "" : cardDetailsList.get(0).getCardNickName().trim();

            System.out.println("UI-დან მიღებული ბარათის სახელი: " + cardNameFromUI);
            System.out.println("API-დან მიღებული ბარათის სახელი: " + cardNameFromAPI);

            Assert.assertEquals(cardNameFromUI, cardNameFromAPI, "ბარათის სახელები არ ემთხვევა!");
            Assert.assertTrue(!cardNameFromAPI.isEmpty(), "API-დან დაბრუნდა ცარიელი ბარათის სახელი!");

        } catch (Exception e) {
            Assert.fail("ტესტის შესრულებისას მოხდა შეცდომა: " + e.getMessage());
        }
        return this;
    }


    public CardBalanceSteps checkTotalBalance() {
        try {
            // UI-დან ინფორმაციის წამოღება
            String accountNumberFromUI = AccountNumberUI.getText().trim();
            String uiTotalBalanceText = AvailableBalance.getText().trim();
            double uiTotalBalance = Double.parseDouble(uiTotalBalanceText.replaceAll("[^0-9.,]", "").replace(",", "."));

            System.out.println("UI ჯამური ბალანსი: " + uiTotalBalance);

            // ანგარიშების და კურსის ინფორმაციის მიღება
            List<AccountDetails> accounts = getAllAccountBalances(accountNumberFromUI);

            // კურსის პოვნა
            double buyRate = 1.0;
            for (AccountDetails account : accounts) {
                if ("USD".equals(account.getCurrency())) {
                    buyRate = account.getBuyRate();
                    break;
                }
            }

            // ჯამური API ბალანსის გამოთვლა
            double apiTotalBalance = 0.0;
            for (AccountDetails account : accounts) {
                if ("GEL".equals(account.getCurrency())) {
                    apiTotalBalance += account.getAvailableBalance();
                } else if ("USD".equals(account.getCurrency())) {
                    apiTotalBalance += account.getAvailableBalance() * buyRate;
                }
            }

            System.out.println("BuyRate (USD -> GEL): " + buyRate);
            System.out.println("API ჯამური ბალანსი: " + apiTotalBalance);

            // ბალანსების შედარება
            boolean isMatch = Math.abs(apiTotalBalance - uiTotalBalance) <= 0.1;
            System.out.println("ჯამური ბალანსი ემთხვევა: " + isMatch);

            // ბალანსების დადასტურება
            if (!isMatch) {
                Assert.fail("ჯამური ბალანსი არ ემთხვევა! UI: " + uiTotalBalance + ", API: " + apiTotalBalance);
            }

        } catch (Exception e) {
            System.err.println("შეცდომა ჯამური ბალანსის შემოწმებისას: " + e.getMessage());
            Assert.fail("შეცდომა ჯამური ბალანსის შემოწმებისას: " + e.getMessage());
        }

        return this;
    }

    public CardBalanceSteps checkBalancesByCurrency() {
        try {
            // UI-დან ინფორმაციის წამოღება
            String accountNumberFromUI = AccountNumberUI.getText().trim();

            String gelText = AvailableBalanceGEL.getText().trim().replaceAll("[^0-9.,]", "").replace(",", ".");
            String usdText = AvailableBalanceUSD.getText().trim().replaceAll("[^0-9.,]", "").replace(",", ".");
            double uiBalanceGel = Float.valueOf(gelText).doubleValue();
            double uiBalanceUsd = Float.valueOf(usdText).doubleValue();

            System.out.println("UI GEL ბალანსი: " + uiBalanceGel);
            System.out.println("UI USD ბალანსი: " + uiBalanceUsd);

            List<AccountDetails> accounts = getAllAccountBalances(accountNumberFromUI);

            // API-დან მიღებული ბალანსების გამოთვლა
            double apiBalanceGel = 0.0;
            double apiBalanceUsd = 0.0;

            for (AccountDetails account : accounts) {
                if ("GEL".equals(account.getCurrency())) {
                    apiBalanceGel += account.getAvailableBalance();
                } else if ("USD".equals(account.getCurrency())) {
                    apiBalanceUsd += account.getAvailableBalance();
                }
            }

            System.out.println("API GEL ბალანსი: " + apiBalanceGel);
            System.out.println("API USD ბალანსი: " + apiBalanceUsd);

            double gelDifference = apiBalanceGel - uiBalanceGel;
            if (gelDifference < 0) {
                gelDifference = -gelDifference;
            }

            double usdDifference = apiBalanceUsd - uiBalanceUsd;
            if (usdDifference < 0) {
                usdDifference = -usdDifference;
            }

            boolean gelMatch = gelDifference <= 0.01;
            boolean usdMatch = usdDifference <= 0.01;

            System.out.println("GEL ბალანსი ემთხვევა: " + gelMatch);
            System.out.println("USD ბალანსი ემთხვევა: " + usdMatch);

            // ბალანსების დადასტურება
            if (!gelMatch) {
                Assert.fail("GEL ბალანსი არ ემთხვევა UI: " + uiBalanceGel + ", API: " + apiBalanceGel);
            }

            if (!usdMatch) {
                Assert.fail("USD ბალანსი არ ემთხვევა UI: " + uiBalanceUsd + ", API: " + apiBalanceUsd);
            }

        } catch (Exception e) {
            System.err.println("შეცდომა ვალუტების ბალანსების შემოწმებისას: " + e.getMessage());
            Assert.fail("შეცდომა ვალუტების ბალანსების შემოწმებისას: " + e.getMessage());
        }

        return this;
    }
}

