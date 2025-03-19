package steps;

import com.codeborne.selenide.ClickOptions;
import com.codeborne.selenide.Condition;
import elements.MyCredoWebElements;
import org.testng.Assert;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.sleep;
import static data.AuthorizationData.*;

public class AuthorizationSteps extends MyCredoWebElements {
    public void authorization(){
        UsernameInput.setValue(UsernameValue);
        PasswordInput.setValue(PasswordValue);
        LoginButton.click();
        OTPCodeInput.setValue(OTP);

        ConfirmButton.click();

        while(ClosePopup.isDisplayed()){
            sleep(3000);
            ClosePopup.click(ClickOptions.usingJavaScript());
        }
        Assert.assertTrue(User.isDisplayed());
    }
}
