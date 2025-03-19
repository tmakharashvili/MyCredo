package steps;

import com.codeborne.selenide.Condition;
import elements.MyCredoWebElements;
import org.testng.asserts.SoftAssert;

import java.time.Duration;

import static com.codeborne.selenide.Condition.*;
import static data.AuthorizationData.OTP;
import static data.AuthorizationData.WrongOTP;

public class PinResetSteps extends MyCredoWebElements {
    private SoftAssert softAssert = new SoftAssert();

    public PinResetSteps closePinPopupWithX() {
        PinResetButton.click();
        ClosePinPopupX.click();

        return this;
    }

    public PinResetSteps closePinPopupWithCancel() {
        PinResetButton.click();
        ClosePinPopupCancel.click();

        return this;
    }

    public PinResetSteps checkTimer() {
        PinResetButton.click();
        COnfirmResetPin.click();
        OTPpage.isDisplayed();
        String initialTimer = Timer.getText();
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String updatedTimer = Timer.getText();
        softAssert.assertNotEquals(initialTimer, updatedTimer, "ტაიმერი არ იცვლება");

        softAssert.assertAll();

        return this;
    }

    public PinResetSteps verifyConfirmButtonDisabledUntilFullOTP() {
        ConfirmOTP.shouldBe(Condition.disabled);

        String fullOTP = OTP;
        int i = 0;
        while (i < fullOTP.length() - 1) {
            OTPcodeInput.setValue(fullOTP.substring(0, i + 1));
            ConfirmOTP.shouldBe(Condition.disabled);
            i++;
        }
        OTPcodeInput.setValue(fullOTP);
        ConfirmOTP.shouldBe(Condition.enabled);

        return this;
    }

    public PinResetSteps verifyResendOTPFunctionality() {
        String initialTimerText = Timer.getText();
        try {
            Thread.sleep(130000);
            ResendCodeButton.shouldBe(visible);
            ResendCodeButton.shouldBe(enabled);
            ResendCodeButton.click();
            Timer.shouldBe(visible);

            String newTimerText = Timer.getText();
            softAssert.assertNotEquals(initialTimerText, newTimerText,
                    "ტაიმერი არ განახლდა კოდის თავიდან მიღების შემდეგ");

        } catch (Exception e) {
            System.err.println("შეცდომა: " + e.getMessage());
            softAssert.fail("კოდის თავიდან მიღების ღილაკი არ გამოჩნდა 2 წუთის შემდეგ");
        }
        softAssert.assertAll();

        return this;
    }

        public PinResetSteps checkErrorMessageForWrongOTP () {
            ClosePinPopupX.click();
            PinResetButton.click();
            COnfirmResetPin.click();

            OTPcodeInput.setValue(WrongOTP);
            ConfirmOTP.click();
            ErrorTextMessage.shouldBe(visible, Duration.ofSeconds(5));

            String actualErrorMessage = ErrorTextMessage.getText();
            String expectedErrorMessage = "მონაცემები არასწორია";

            softAssert.assertEquals(actualErrorMessage, expectedErrorMessage,
                    "შეცდომის შეტყობინება არ ემთხვევა მოსალოდნელს");

            System.out.println("არასწორი OTP-ის შეყვანისას გამოჩნდა შეტყობინება: " + actualErrorMessage);

            return this;
        }

        public PinResetSteps confirmResetPin () {
            COnfirmResetPin.click();
            OTPcodeInput.setValue(OTP);
            ConfirmOTP.click();
            OTPpage.should(disappear);

            String actualMessage = SuccessMessage.getText();
            String expectedSuccessMessage = "ახალი პინ კოდი sms-ით გამოგიგზავნეთ";

            softAssert.assertEquals(actualMessage, expectedSuccessMessage,
                    "შეტყობინება არ შეიცავს მოსალოდნელ ტექსტს. მიმდინარე: '" +
                            actualMessage + "', მოსალოდნელი: '" + expectedSuccessMessage + "'");

            System.out.println("შეტყობინების შემოწმება: მიმდინარე - '" + actualMessage +
                    "', მოსალოდნელი - '" + expectedSuccessMessage + "'");

            return this;
        }

        public void assertAll () {
            softAssert.assertAll();
        }
    }


