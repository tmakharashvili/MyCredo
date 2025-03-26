package steps;

import elements.MyCredoWebElements;
import org.testng.asserts.SoftAssert;

public class BlockCardModuleSteps extends MyCredoWebElements {
    private final SoftAssert softAssert = new SoftAssert();

    public BlockCardModuleSteps closePopupWithX(){
        BlockCardButton.click();
        ClosePopupX.click();

        return this;
    }
    public BlockCardModuleSteps closePopupWithCancel(){
        BlockCardButton.click();
        ClosePopupCancel.click();

        return this;
    }
    public BlockCardModuleSteps blockCardAndCheckStatus() {
        BlockCardButton.click();
        ConfirmBlock.click();

        softAssert.assertEquals(getCardStatus(), "დაბლოკილი",
                "ბარათის სტატუსი არ შეიცვალა 'დაბლოკილი'-ზე");

        return this;
    }

    public BlockCardModuleSteps unblockCardAndCheckStatus() {
        UnblockButton.click();
        ConfirmUnblock.click();

        softAssert.assertEquals(getCardStatus(), "აქტიური",
                "ბარათის სტატუსი არ შეიცვალა 'აქტიური'-ზე");

        return this;
    }

    public String getCardStatus() {
        return CardStatus.getText();

    }

    public void assertAll() {
        softAssert.assertAll();
    }

}
