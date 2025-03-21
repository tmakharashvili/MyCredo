package steps;

import elements.MyCredoWebElements;
import org.testng.Assert;

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
}
