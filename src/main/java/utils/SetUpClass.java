package utils;

import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeSuite;
import com.codeborne.selenide.Configuration;

import static com.codeborne.selenide.Configuration.browser;
import static com.codeborne.selenide.Selenide.closeWebDriver;
import static com.codeborne.selenide.Selenide.open;

public class SetUpClass {
    @BeforeSuite
    public static void setUp (){
        browser="chrome";
        Configuration.timeout=15000;
        open("https://testmycredo.credo.ge/landing/main/auth");
    }

    @AfterTest
    public static void tearDown(){
        closeWebDriver();
    }
}
