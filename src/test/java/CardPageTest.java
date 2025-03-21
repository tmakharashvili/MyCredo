import org.testng.annotations.Test;
import steps.*;
import utils.SetUpClass;

public class CardPageTest extends SetUpClass {
    AuthorizationSteps authorizationSteps = new AuthorizationSteps();
    CardBalanceSteps cardBalanceSteps = new CardBalanceSteps();
    BlockCardModuleSteps blockCardModuleSteps = new BlockCardModuleSteps();
    PinResetSteps pinResetSteps = new PinResetSteps();
    CardNavigationSteps cardNavigationSteps = new CardNavigationSteps();
    TransferSteps transferSteps = new TransferSteps();

    @Test
    public void checkCardNameAndBalance(){
        authorizationSteps.authorization();
        cardBalanceSteps
                .goToCards()
                .checkSelectedCardName()
                .checkTotalBalance()
                .checkBalancesByCurrency();
    }

    @Test
    public void blockCardModuleTest(){
        authorizationSteps.authorization();
        cardBalanceSteps.goToCards();
        blockCardModuleSteps
                .closePopupWithX()
                .closePopupWithCancel()
                .blockCardAndCheckStatus()
                .unblockCardAndCheckStatus()
                .assertAll();
    }

    @Test
    public void pinReset() {
        authorizationSteps.authorization();
        cardBalanceSteps.goToCards();
        pinResetSteps
                .closePinPopupWithX()
                .closePinPopupWithCancel()
                .checkTimer()
                .verifyConfirmButtonDisabledUntilFullOTP()
                .verifyResendOTPFunctionality()
                .checkErrorMessageForWrongOTP()
                .confirmResetPin()
                .assertAll();
    }

    @Test
    public void cardNavigation(){
        authorizationSteps.authorization();
        cardBalanceSteps.goToCards();
        cardNavigationSteps.checkCardNavigation();
    }

    @Test
    public void TransferAmount(){
        authorizationSteps.authorization();
        cardBalanceSteps.goToCards();
        transferSteps.checkTransferNavigation();
        transferSteps.moneyTransfer();
        transferSteps.checkTransactions();
    }

}
