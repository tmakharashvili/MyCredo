package utils;

import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeSuite;
import com.codeborne.selenide.Configuration;

import static com.codeborne.selenide.Selenide.closeWebDriver;
import static com.codeborne.selenide.Selenide.open;

public class SetUpClass {
    @BeforeSuite
    public static void setUp (){
        Configuration.browser="chrome";
        Configuration.timeout=10000; // Changed from 10 to 10000 (10 seconds)
        open("https://testmycredo.credo.ge/landing/main/auth");
    }
    @AfterTest
    public static void tearDown(){
        closeWebDriver();
    }
}
