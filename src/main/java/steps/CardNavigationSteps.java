package steps;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.FileDownloadMode;
import com.codeborne.selenide.Selenide;
import elements.MyCredoWebElements;
import org.testng.Assert;

import java.io.File;

public class CardNavigationSteps extends MyCredoWebElements {

    public void checkCardNavigation(){
        String initialAccountNumber = AccountNumberUI.getText().trim();
        System.out.println("საწყისი ანგარიშის ნომერი: " + initialAccountNumber);

        GoToNextCard.click();
        String nextAccountNumber = AccountNumberUI.getText().trim();
        System.out.println("შემდეგი ანგარიშის ნომერი: " + nextAccountNumber);

        Assert.assertNotEquals(initialAccountNumber, nextAccountNumber,
                "წინ ღილაკზე დაჭერა არ ცვლის ანგარიშს");

        GoToPreviousCard.click();
        String previousAccountNumber = AccountNumberUI.getText().trim();
        System.out.println("წინა ანგარიშის ნომერი: " + previousAccountNumber);

        Assert.assertEquals(initialAccountNumber, previousAccountNumber,
                "უკან ღილაკზე დაჭერა არ აბრუნებს საწყის ანგარიშზე");
    }

    public void checkRequisite(){
        String downloadPath = "C:\\Users\\tamarmakharashvili\\Downloads";
        Configuration.downloadsFolder = downloadPath;
        Configuration.fileDownload = FileDownloadMode.CDP;

        RequisiteButton.click();

        File downloadedFile = new File(downloadPath + "\\Requisites.pdf");

        long startTime = System.currentTimeMillis();
        while (!downloadedFile.exists() && System.currentTimeMillis() - startTime < 10000) {
            Selenide.sleep(500);
        }

        Assert.assertTrue(downloadedFile.exists(), "ფაილი არ არსებობს");

    }
}
