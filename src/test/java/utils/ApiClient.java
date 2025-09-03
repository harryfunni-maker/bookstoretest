package utils;

import io.restassured.response.Response;
import static io.restassured.RestAssured.*;

public class ApiClient {
    private static final String BASE_URL = "https://demoqa.com";

    public static Response post(String endpoint, Object body) {
        return given()
                .baseUri(BASE_URL)
                .header("Content-Type", "application/json")
                .body(body)
                .log().all()
                .when()
                .post(endpoint)
                .then().log().all().extract().response();
    }

    public static Response post(String endpoint, String token, Object body) {
        return given()
                .baseUri(BASE_URL)
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .body(body)
                .log().all()
                .when()
                .post(endpoint)
                .then().log().all().extract().response();
    }

    public static Response get(String endpoint, String token) {
        return given()
                .baseUri(BASE_URL)
                .header("Authorization", "Bearer " + token)
                .log().all()
                .when()
                .get(endpoint)
                .then().log().all().extract().response();
    }

    public static Response delete(String endpoint, String token, Object body) {
        return given()
                .baseUri(BASE_URL)
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .body(body)
                .log().all()
                .when()
                .delete(endpoint)
                .then().log().all().extract().response();
    }
}
