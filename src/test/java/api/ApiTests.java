package api;

import org.testng.Assert;
import org.testng.annotations.Test;
import utils.ApiClient;
import utils.UserData;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.datafaker.Faker;
import io.restassured.response.Response;

public class ApiTests {
    public static Faker faker = new Faker();
    public static ObjectMapper mapper = new ObjectMapper();
    public static UserData user = new UserData();

    @Test(priority = 1)
    public void createUser() throws Exception {
        user.userName = faker.name().username();
        user.password = faker.internet().password(8, 12, true, true);
        String body = String.format("{\"userName\":\"%s\",\"password\":\"%s\"}", user.userName, user.password);
        Response res = ApiClient.post("/Account/v1/User", body);
        Assert.assertEquals(res.statusCode(), 201);
        user.userID = res.jsonPath().getString("userID");
    }

    @Test(priority = 2, dependsOnMethods = "createUser")
    public void generateToken() {
        String body = String.format("{\"userName\":\"%s\",\"password\":\"%s\"}", user.userName, user.password);
        Response res = ApiClient.post("/Account/v1/GenerateToken", body);
        Assert.assertEquals(res.statusCode(), 200);
        user.token = res.jsonPath().getString("token");
    }

    @Test(priority = 3, dependsOnMethods = "generateToken")
    public void getUserBooks() {
        Response res = ApiClient.get("/Account/v1/User/" + user.userID, user.token);
        Assert.assertEquals(res.statusCode(), 200);
        Assert.assertTrue(res.jsonPath().getList("books").isEmpty(), "Books should be empty for new user");
    }
}
