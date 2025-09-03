package api;

import org.testng.Assert;
import org.testng.annotations.Test;
import utils.ApiClient;
import utils.UserData;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.response.Response;

public class BookApiTests {
    public static UserData user = ApiTests.user;
    public static String bookIsbn;

    @Test(priority = 4, dependsOnMethods = "api.ApiTests.generateToken")
    public void getAllBooks() {
        Response res = ApiClient.get("/BookStore/v1/Books", user.token);
        Assert.assertEquals(res.statusCode(), 200);
        bookIsbn = res.jsonPath().getString("books[0].isbn");
        Assert.assertNotNull(bookIsbn);
    }

    @Test(priority = 5, dependsOnMethods = "getAllBooks")
    public void addBookToUser() {
        String body = String.format("{\"userId\":\"%s\",\"collectionOfIsbns\":[{\"isbn\":\"%s\"}]}", user.userID, bookIsbn);
        Response res = ApiClient.post("/BookStore/v1/Books", user.token, body);
        Assert.assertEquals(res.statusCode(), 201);
    }

    @Test(priority = 6, dependsOnMethods = "addBookToUser")
    public void deleteBooksOfUser() {
        String body = String.format("{\"userId\":\"%s\",\"isbn\":\"%s\"}", user.userID, bookIsbn);
        Response res = ApiClient.delete("/BookStore/v1/Book", user.token, body);
        Assert.assertEquals(res.statusCode(), 204);
    }
}
