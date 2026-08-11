package org.example;

import com.google.gson.Gson;
import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class CourierDeleteTest {

    private static final String BASE_URI =
            "https://qa-scooter.praktikum-services.ru";

    private static final String COURIER_PATH =
            "/api/v1/courier";

    private static final String LOGIN_PATH =
            "/api/v1/courier/login";

    private static final String CONTENT_TYPE =
            "application/json";

    private static final String PASSWORD =
            "123456";

    private static final int NONEXISTENT_COURIER_ID =
            999999999;

    private Gson gson;

    @BeforeEach
    public void setUp() {
        RestAssured.baseURI = BASE_URI;
        gson = new Gson();
    }

    @Test
    @DisplayName("Successful courier deletion")
    @Description("Проверка успешного удаления курьера")
    void deleteCourierSuccess() {

        String login = generateLogin();

        createCourier(login, PASSWORD);

        Integer courierId =
                loginCourierAndGetId(login, PASSWORD);

        deleteCourierAndCheckSuccess(courierId);
    }

    @Test
    @DisplayName("Delete courier without id")
    @Description("Проверка ошибки при удалении курьера без id")
    void deleteCourierWithoutId() {

        deleteCourierWithoutIdAndCheckError();
    }

    @Test
    @DisplayName("Delete courier with nonexistent id")
    @Description("Проверка ошибки при удалении курьера с несуществующим id")
    void deleteCourierWithNonexistentId() {

        deleteNonexistentCourierAndCheckError(
                NONEXISTENT_COURIER_ID
        );
    }

    @Step("Create courier")
    private void createCourier(
            String login,
            String password) {

        Courier courier =
                new Courier(login, password, "Ivan");

        given()
                .header("Content-type", CONTENT_TYPE)
                .body(gson.toJson(courier))
                .when()
                .post(COURIER_PATH)
                .then()
                .statusCode(201)
                .body("ok", equalTo(true));
    }

    @Step("Login courier and get id")
    private Integer loginCourierAndGetId(
            String login,
            String password) {

        CourierLogin courierLogin =
                new CourierLogin(login, password);

        return given()
                .header("Content-type", CONTENT_TYPE)
                .body(gson.toJson(courierLogin))
                .when()
                .post(LOGIN_PATH)
                .then()
                .statusCode(200)
                .body("id", notNullValue())
                .extract()
                .path("id");
    }

    @Step("Delete courier successfully")
    private void deleteCourierAndCheckSuccess(
            Integer courierId) {

        given()
                .when()
                .delete(COURIER_PATH + "/{id}", courierId)
                .then()
                .statusCode(200)
                .body("ok", equalTo(true));
    }

    @Step("Delete courier without id and check error")
    private void deleteCourierWithoutIdAndCheckError() {

        given()
                .when()
                .delete(COURIER_PATH + "/")
                .then()
                .statusCode(400)
                .body("message", notNullValue());
    }

    @Step("Delete nonexistent courier and check error")
    private void deleteNonexistentCourierAndCheckError(
            Integer courierId) {

        given()
                .when()
                .delete(COURIER_PATH + "/{id}", courierId)
                .then()
                .statusCode(404)
                .body("message", notNullValue());
    }

    private String generateLogin() {
        return "courier"
                + new Random().nextInt(1000000);
    }
}