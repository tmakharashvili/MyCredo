package steps;

import elements.MyCredoWebElements;
import static com.codeborne.selenide.Condition.visible;

public class TransferSteps extends MyCredoWebElements {

    public void checkTransferNavigation(){
        TransferButton.click();
        TransfersHeader.shouldBe(visible);
    }

    public void moneyTransfer(){
        OwnAccounts.click();
        WhereInput.click();
        AnotherAccount.click();
        AnotherAccountsCurrency.click();
        AmountInput.setValue("10");
        Transfer.click();
        CloseSuccessPage.click();
    }

    public void checkTransactions(){
        TransactionsButton.click();
    }
}
