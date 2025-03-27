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
            Assert.assertFalse(cardNameFromAPI.isEmpty(), "API-დან დაბრუნდა ცარიელი ბარათის სახელი!");

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

            // ყველა არა რიცხვითი სიმბოლოს მოშორება, წერტილი შევინარჩუნოთ
            String cleanedText = uiTotalBalanceText.replaceAll("[^0-9.]", "");

            double uiTotalBalance = Double.parseDouble(cleanedText);
            System.out.println("დამუშავებული UI ჯამური ბალანსი: " + uiTotalBalance);

            // ანგარიშების და კურსის ინფორმაციის მიღება ბაზიდან
            List<AccountDetails> accounts = getAllAccountBalances(accountNumberFromUI);

            // კურსის პოვნა
            double buyRate = 1.0;
            for (AccountDetails account : accounts) {
                if ("USD".equals(account.getCurrency())) {
                    buyRate = account.getBuyRate();
                    break;
                }
            }

            // ჯამური ბალანსის გამოთვლა
            double dbTotalBalance = 0.0;
            for (AccountDetails account : accounts) {
                if ("GEL".equals(account.getCurrency())) {
                    dbTotalBalance += account.getAvailableBalance();
                } else if ("USD".equals(account.getCurrency())) {
                    dbTotalBalance += account.getAvailableBalance() * buyRate;
                }
            }

            // დავამრგვალოთ ორივე მნიშვნელობა ათწილადამდე
            uiTotalBalance = Math.round(uiTotalBalance * 100.0) / 100.0;
            dbTotalBalance = Math.round(dbTotalBalance * 100.0) / 100.0;

            System.out.println("BuyRate (USD -> GEL): " + buyRate);
            System.out.println("ბაზის ჯამური ბალანსი: " + dbTotalBalance);

            boolean isMatch = Math.abs(dbTotalBalance - uiTotalBalance) <= 0.1;
            System.out.println("ჯამური ბალანსი ემთხვევა: " + isMatch);

            Assert.assertEquals(uiTotalBalance, dbTotalBalance, 0.1,
                    "ჯამური ბალანსი არ ემთხვევა! UI: " + uiTotalBalance + ", ბაზა: " + dbTotalBalance);

        } catch (Exception e) {
            System.err.println("შეცდომა ჯამური ბალანსის შემოწმებისას: " + e.getMessage());
            System.err.println("შეცდომის მიზეზი: " + e.getCause());
            Assert.fail("შეცდომა ჯამური ბალანსის შემოწმებისას: " + e.getMessage());
        }

        return this;
    }

    public void checkBalancesByCurrency() {
        try {
            // UI-დან ინფორმაციის წამოღება
            String accountNumberFromUI = AccountNumberUI.getText().trim();

            // GEL ბალანსის დამუშავება
            String gelText = AvailableBalanceGEL.getText().trim();

            // ათასეულის და ათწილადის გამყოფების გათვალისწინება
            String cleanedGelText = gelText.replaceAll("[^0-9.]", "");


            double uiBalanceGel = Double.parseDouble(cleanedGelText);

            // USD ბალანსის დამუშავება
            String usdText = AvailableBalanceUSD.getText().trim();

            // იგივე მეთოდი USD-სთვის
            String cleanedUsdText = usdText.replaceAll("[^0-9.]", "");

            double uiBalanceUsd = Double.parseDouble(cleanedUsdText);

            System.out.println("დამუშავებული UI GEL ბალანსი: " + uiBalanceGel);
            System.out.println("დამუშავებული UI USD ბალანსი: " + uiBalanceUsd);

            // მივიღოთ ბაზის მონაცემები
            List<AccountDetails> accounts = getAllAccountBalances(accountNumberFromUI);

            // ბაზიდან მიღებული ბალანსების გამოთვლა
            double dbBalanceGel = 0.0;
            double dbBalanceUsd = 0.0;

            for (AccountDetails account : accounts) {
                if ("GEL".equals(account.getCurrency())) {
                    dbBalanceGel += account.getAvailableBalance();
                } else if ("USD".equals(account.getCurrency())) {
                    dbBalanceUsd += account.getAvailableBalance();
                }
            }
            // დავამრგვალოთ ყველა მნიშვნელობა ათწილადამდე
            uiBalanceGel = Math.round(uiBalanceGel * 100.0) / 100.0;
            uiBalanceUsd = Math.round(uiBalanceUsd * 100.0) / 100.0;
            dbBalanceGel = Math.round(dbBalanceGel * 100.0) / 100.0;
            dbBalanceUsd = Math.round(dbBalanceUsd * 100.0) / 100.0;

            System.out.println("ბაზის GEL ბალანსი: " + dbBalanceGel);
            System.out.println("ბაზის USD ბალანსი: " + dbBalanceUsd);

            boolean gelMatch = Math.abs(dbBalanceGel - uiBalanceGel) <= 0.01;
            boolean usdMatch = Math.abs(dbBalanceUsd - uiBalanceUsd) <= 0.01;

            System.out.println("GEL ბალანსი ემთხვევა: " + gelMatch);
            System.out.println("USD ბალანსი ემთხვევა: " + usdMatch);

            // შევადაროთ ბალანსები
            Assert.assertEquals(uiBalanceGel, dbBalanceGel, 0.01,
                    "GEL ბალანსი არ ემთხვევა! UI: " + uiBalanceGel + ", ბაზა: " + dbBalanceGel);

            Assert.assertEquals(uiBalanceUsd, dbBalanceUsd, 0.01,
                    "USD ბალანსი არ ემთხვევა! UI: " + uiBalanceUsd + ", ბაზა: " + dbBalanceUsd);

        } catch (Exception e) {
            System.err.println("შეცდომა ვალუტების ბალანსების შემოწმებისას: " + e.getMessage());
            System.err.println("შეცდომის მიზეზი: " + e.getCause());
            Assert.fail("შეცდომა ვალუტების ბალანსების შემოწმებისას: " + e.getMessage());
        }
    }
}

