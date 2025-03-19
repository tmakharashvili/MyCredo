import org.testng.annotations.Test;
import steps.AuthorizationSteps;
import steps.BlockCardModuleSteps;
import steps.CardBalanceSteps;
import steps.PinResetSteps;
import utils.SetUpClass;

public class CardPageTest extends SetUpClass {
    AuthorizationSteps authorizationSteps = new AuthorizationSteps();
    CardBalanceSteps cardBalanceSteps = new CardBalanceSteps();
    BlockCardModuleSteps blockCardModuleSteps = new BlockCardModuleSteps();
    PinResetSteps pinResetSteps = new PinResetSteps();

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
}
