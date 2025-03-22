package elements;

import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Selectors.byXpath;
import static com.codeborne.selenide.Selenide.$;

public class MyCredoWebElements {
    public SelenideElement
            UsernameInput = $("#userName"),
            PasswordInput = $("#newPass"),
            LoginButton = $("#submitAuth"),
            OTPCodeInput = $("#otpCodeInput"),
            ConfirmButton = $(byXpath("//*[@id=\"otpSend\"]/form/button")),
            ClosePopup = $(byXpath("//app-popup-container//div[@class='icon close-black grey-010 pointer']")),
            ProductsButton = $(byXpath("/html/body/app-root/app-home/div/div/app-home-header/header/div/div/nav/a[3]")),
            User = $(".user-menu-header"),
            AccountAndCards = $(byXpath("//*[@id=\"checkProdList\"]/div/div[1]/p[2]")),
            EmigrantCard = $(byXpath("//div[@id='navToAccDetails' and contains(@style, 'https://testpublicapi.mycredo.ge/Images/CardDesign/54.png')]\n")),
            CardName = $(byXpath("/html/body/app-root/app-home/div/div/app-accounts-and-cards-details/div/div[2]/div[1]/div[1]/p")),
            AccountNumberUI = $(byXpath("/html/body/app-root/app-home/div/div/app-accounts-and-cards-details/div/div[2]/div[2]/div[1]/div/div[3]/div/p")),
            AvailableBalance = $(byXpath("/html/body/app-root/app-home/div/div/app-accounts-and-cards-details/div/div[2]/div[1]/div[1]/div[1]/p")),
            AvailableBalanceGEL = $(byXpath("/html/body/app-root/app-home/div/div/app-accounts-and-cards-details/div/div[2]/div[1]/div[1]/div[1]/div[1]/p[1]")),
            AvailableBalanceUSD = $(byXpath("/html/body/app-root/app-home/div/div/app-accounts-and-cards-details/div/div[2]/div[1]/div[1]/div[1]/div[1]/p[2]")),
            BlockCardButton = $("#showBlockOrUnblock"),
            ClosePopupX = $(byXpath("//*[@id=\"nameAccount\"]/div/div/div[1]/div[2]")),
            ClosePopupCancel = $("#popupCard"),
            ConfirmBlock = $("#lockCard"),
            CardStatus = $(byXpath("//*[@id=\"accHasCard\"]/div/div[5]/div/p")),
            UnblockButton = $("#showBlockOrUnblockCardPopup"),
            ConfirmUnblock = $("#unblock"),
            PinResetButton = $("#pinReset"),
            ClosePinPopupX = $(byXpath("/html/body/app-root/app-home/div/div/app-accounts-and-cards-details/app-popup-container/div/div/div[1]/div[2]")),
            ClosePinPopupCancel = $("#closePinReset"),
            COnfirmResetPin = $("#resetPin"),
            OTPpage = $(".popup-wrapper"),
            OTPcodeInput = $("#otpCodeInput"),
            ConfirmOTP = $(byXpath("/html/body/app-root/app-home/div/div/app-accounts-and-cards-details/app-popup-container/div/div/div[2]/app-pin-reset/div/div/app-otp/form/button")),
            SuccessMessage = $(byXpath("//*[@id=\"growlText\"]")),
            Timer = $(byXpath("/html/body/app-root/app-home/div/div/app-accounts-and-cards-details/app-popup-container/div/div/div[2]/app-pin-reset/div/div/app-otp/form/p[2]")),
            ResendCodeButton = $(byXpath("/html/body/app-root/app-home/div/div/app-accounts-and-cards-details/app-popup-container/div/div/div[2]/app-pin-reset/div/div/app-otp/form/p[2]")),
            ErrorTextMessage = $(byXpath("//*[@id=\"growlText\"]")),
            GoToPreviousCard = $("#selectPreviousProduct"),
            GoToNextCard = $("#selectNextProduct"),
            TransferButton = $("#transfer"),
            TransfersHeader = $(byXpath("/html/body/app-root/app-home/div/div/app-financial-operations/div/div/div/app-transfers/div/div[1]")),
            OwnAccounts = $(byXpath("/html/body/app-root/app-home/div/div/app-financial-operations/div/div/div/app-transfers/div/div[2]/div/div[1]/div[2]")),
            FromInput = $(byXpath("//p[@_ngcontent-ng-c322433402 and contains(@class, 'selected-accounts')]")),
            WhereInput = $(byXpath("(//div[contains(@class, 'advanced-accounts-select')])[2]")),
            AccountsPopup = $(".popup-wrapper"),
            AnotherAccount = $(byXpath("//*[@id=\"accountItem\"]/div[1]")),
            AnotherAccountsCurrency = $("#accountCurrency_00"),
            AmountInput = $(byXpath("/html/body/app-root/app-home/div/div/app-financial-operations/div/div/div/app-transfers/div/div[2]/app-transfers-to-own-account/form/div[5]/input")),
            Transfer = $(byXpath("/html/body/app-root/app-home/div/div/app-financial-operations/div/div/div/app-transfers/div/div[2]/app-transfers-to-own-account/form/button")),
            RequisiteButton = $("#downloadRequisite"),
            SenderNumberUI = $(byXpath("/html/body/app-root/app-home/div/div/app-financial-operations/div/div/div/app-transfers/div/div[2]/app-transfers-to-own-account/app-popup-container/div/div/div[2]/div/div[2]/div[1]/div/p[2]")),
            ReceiverNumberUI = $(byXpath("/html/body/app-root/app-home/div/div/app-financial-operations/div/div/div/app-transfers/div/div[2]/app-transfers-to-own-account/app-popup-container/div/div/div[2]/div/div[2]/div[2]/div/p[2]")),
            AmountUI = $(byXpath("/html/body/app-root/app-home/div/div/app-financial-operations/div/div/div/app-transfers/div/div[2]/app-transfers-to-own-account/app-popup-container/div/div/div[2]/div/div[2]/div[3]/div/p[2]"));
}
