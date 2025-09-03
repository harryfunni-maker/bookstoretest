package ui;

import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.annotations.Test;
import utils.UserData;
import base.BaseTest;
import api.ApiTests;

public class UiTests extends BaseTest {
    static UserData user = ApiTests.user; // Reference the user created in API tests

    @Test(priority = 1)
    public void loginUser() throws InterruptedException {
        driver.get("https://demoqa.com/login");
        driver.findElement(By.id("userName")).sendKeys(user.userName);
        driver.findElement(By.id("password")).sendKeys(user.password);
        driver.findElement(By.id("login")).click();
        Thread.sleep(2000); // Wait for login
        Assert.assertTrue(driver.getCurrentUrl().contains("profile"), "User should be redirected to profile page");
    }

    @Test(priority = 2, dependsOnMethods = "loginUser")
    public void validateNoBooksInProfile() {
        Assert.assertTrue(driver.findElements(By.cssSelector("#books-wrapper .rt-tr-group")).isEmpty(), "No books should be listed");
    }
}
