package ui;

import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.annotations.Test;
import utils.UserData;
import base.BaseTest;
import api.BookApiTests;
import api.ApiTests;

public class BookUiTests extends BaseTest {
    static UserData user = ApiTests.user;
    static String bookIsbn = BookApiTests.bookIsbn;

    @Test(priority = 3, dependsOnMethods = "api.BookApiTests.addBookToUser")
    public void validateBookInProfile() throws InterruptedException {
        driver.get("https://demoqa.com/profile");
        Thread.sleep(1000);
        if (!driver.getCurrentUrl().contains("profile")) {
            driver.get("https://demoqa.com/login");
            driver.findElement(By.id("userName")).sendKeys(user.userName);
            driver.findElement(By.id("password")).sendKeys(user.password);
            driver.findElement(By.id("login")).click();
            Thread.sleep(2000);
        }
        if (bookIsbn == null) {
            Assert.fail("Book ISBN is null. Book was not added to user profile in API test.");
        }
        boolean found = driver.getPageSource().contains(bookIsbn);
        Assert.assertTrue(found, "Book should be listed in user profile");
    }
}
