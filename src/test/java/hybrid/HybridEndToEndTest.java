package hybrid;

import base.BaseTest;
import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.annotations.Test;
import utils.ApiClient;
import utils.UserData;
import net.datafaker.Faker;
import io.restassured.response.Response;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

public class HybridEndToEndTest extends BaseTest {
    static Faker faker = new Faker();
    static UserData user = new UserData();
    static String bookIsbn;
    static String bookTitle;

    @Test(priority = 1)
    public void createUserAndGenerateToken() {
        // Create user via API
    user.userName = faker.name().username();
    // Password: at least 1 uppercase, 1 lowercase, 1 digit, 1 special char, min 8 chars
    user.password = faker.regexify("[A-Z]{1}[a-z]{1}[0-9]{1}[!@#$%^&*]{1}[A-Za-z0-9!@#$%^&*]{4,}");
        String createBody = String.format("{\"userName\":\"%s\",\"password\":\"%s\"}", user.userName, user.password);
        Response createRes = ApiClient.post("/Account/v1/User", createBody);
    System.out.println("[ASSERT] Create user status: expected=201, actual=" + createRes.statusCode());
    Assert.assertEquals(createRes.statusCode(), 201);
        user.userID = createRes.jsonPath().getString("userID");

        // Generate token via API
        String tokenBody = String.format("{\"userName\":\"%s\",\"password\":\"%s\"}", user.userName, user.password);
        Response tokenRes = ApiClient.post("/Account/v1/GenerateToken", tokenBody);
    System.out.println("[ASSERT] Generate token status: expected=200, actual=" + tokenRes.statusCode());
    Assert.assertEquals(tokenRes.statusCode(), 200);
        user.token = tokenRes.jsonPath().getString("token");
    }

    @Test(priority = 2, dependsOnMethods = "createUserAndGenerateToken")
    public void addBookToUserViaApi() {
        // Get all books
        Response booksRes = ApiClient.get("/BookStore/v1/Books", user.token);
    System.out.println("[ASSERT] Get all books status: expected=200, actual=" + booksRes.statusCode());
    Assert.assertEquals(booksRes.statusCode(), 200);
        bookIsbn = booksRes.jsonPath().getString("books[0].isbn");
        bookTitle = booksRes.jsonPath().getString("books[0].title");
    System.out.println("[ASSERT] Book ISBN not null: actual=" + bookIsbn);
    Assert.assertNotNull(bookIsbn);
    System.out.println("[ASSERT] Book title not null: actual=" + bookTitle);
    Assert.assertNotNull(bookTitle);
        // Add book to user
        String addBookBody = String.format("{\"userId\":\"%s\",\"collectionOfIsbns\":[{\"isbn\":\"%s\"}]}", user.userID, bookIsbn);
        Response addBookRes = ApiClient.post("/BookStore/v1/Books", user.token, addBookBody);
    System.out.println("[ASSERT] Add book status: expected=201, actual=" + addBookRes.statusCode());
    Assert.assertEquals(addBookRes.statusCode(), 201);
    }

    @Test(priority = 3, dependsOnMethods = "addBookToUserViaApi")
    public void listBooksOfUserViaApi() {
        System.out.println("[API] Listing books for user: " + user.userID);
        Response res = ApiClient.get("/Account/v1/User/" + user.userID, user.token);
        System.out.println("[API REQUEST] GET /Account/v1/User/" + user.userID);
        System.out.println("[API RESPONSE] Status: " + res.statusCode());
        System.out.println("[API RESPONSE] Body: " + res.getBody().asString());
        Assert.assertEquals(res.statusCode(), 200);
    }

    @Test(priority = 4, dependsOnMethods = "listBooksOfUserViaApi")
    public void loginAndValidateBookInProfileUI() throws InterruptedException {
        driver.get("https://demoqa.com/login");
        System.out.println("[LOGIN] Attempting login with username: '" + user.userName + "', password: '" + user.password + "'");
        driver.findElement(By.id("userName")).sendKeys(user.userName);
        driver.findElement(By.id("password")).sendKeys(user.password);
        driver.findElement(By.id("login")).click();
        Thread.sleep(2000);
        String currentUrl = driver.getCurrentUrl();
        System.out.println("[ASSERT] User redirected to profile: expected to contain 'profile', actual url=" + currentUrl);
        if (!currentUrl.contains("profile")) {
            // Try to log any error message on the login page
            try {
                String errorMsg = driver.findElement(By.id("name")).getText();
                System.out.println("[LOGIN ERROR] Message on login page: '" + errorMsg + "'");
            } catch (Exception e) {
                System.out.println("[LOGIN ERROR] No error message found on login page.");
            }
        }
        Assert.assertTrue(currentUrl.contains("profile"), "User should be redirected to profile page");
        // Wait for the books-wrapper to be visible
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("books-wrapper")));
        // Advanced wait: wait up to 15s for a book link with the expected ISBN in href
        WebDriverWait advWait = new WebDriverWait(driver, Duration.ofSeconds(15));
        boolean found = false;
        try {
            found = advWait.until(d -> {
                java.util.List<WebElement> links = d.findElements(By.cssSelector("#books-wrapper .rt-tbody .rt-tr-group a"));
                for (WebElement link : links) {
                    String href = link.getAttribute("href");
                    System.out.println("[DEBUG] Checking link: text='" + link.getText() + "', href='" + href + "'");
                    if (href != null && href.endsWith(bookIsbn)) {
                        System.out.println("[DEBUG] MATCHED: href '" + href + "' ends with bookIsbn '" + bookIsbn + "'");
                        return true;
                    }
                }
                return false;
            });
        } catch (Exception e) {
            System.out.println("[DEBUG] Advanced wait timed out: " + e.getMessage());
        }
        System.out.println("[ASSERT] Book present in profile: expected ISBN='" + bookIsbn + "', found=" + found);
        Assert.assertTrue(found, "Book with ISBN '" + bookIsbn + "' should be listed in user profile");
    }

    @Test(priority = 4, dependsOnMethods = "loginAndValidateBookInProfileUI")
    public void deleteBookAndValidateUI() throws InterruptedException {
        // Delete book via API
        String delBody = String.format("{\"userId\":\"%s\",\"isbn\":\"%s\"}", user.userID, bookIsbn);
        Response delRes = ApiClient.delete("/BookStore/v1/Book", user.token, delBody);
    System.out.println("[ASSERT] Delete book status: expected=204, actual=" + delRes.statusCode());
    Assert.assertEquals(delRes.statusCode(), 204);
        // Refresh UI and check book is gone
        driver.navigate().refresh();
        Thread.sleep(2000);
        boolean found = driver.findElements(By.cssSelector("#books-wrapper .rt-tbody .rt-tr-group a"))
            .stream().anyMatch(e -> e.getText().equals(bookTitle));
    System.out.println("[ASSERT] Book removed from profile: expected title='" + bookTitle + "', found=" + found);
    Assert.assertFalse(found, "Book with title '" + bookTitle + "' should be removed from user profile");
    }
}
