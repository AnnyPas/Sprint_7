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

public class CourierCreateTest {

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

    private Gson gson;

    @BeforeEach
    public void setUp() {
        RestAssured.baseURI = BASE_URI;
        gson = new Gson();
    }

    @Test
    @DisplayName("Successful courier creation")
    @Description("Проверка успешного создания нового курьера")
    void createCourierSuccess() {

        String login = generateLogin();

        Courier courier =
                new Courier(login, PASSWORD, "Ivan");

        Integer courierId = null;

        try {
            createCourierAndCheckSuccess(courier);

            courierId = loginCourierAndGetId(
                    login,
                    PASSWORD
            );

        } finally {
            if (courierId != null) {
                deleteCourier(courierId);
            }
        }
    }

    @Test
    @DisplayName("Create courier without login")
    @Description("Проверка ошибки при создании курьера без login")
    void createCourierWithoutLogin() {

        Courier courier =
                new Courier(null, PASSWORD, "Ivan");

        createCourierAndCheckError(courier, 400);
    }

    @Test
    @DisplayName("Create courier without password")
    @Description("Проверка ошибки при создании курьера без password")
    void createCourierWithoutPassword() {

        Courier courier =
                new Courier(generateLogin(), null, "Ivan");

        createCourierAndCheckError(courier, 400);
    }

    @Test
    @DisplayName("Create duplicate courier")
    @Description("Проверка невозможности создать двух одинаковых курьеров")
    void createDuplicateCourier() {

        String login = generateLogin();

        Courier courier =
                new Courier(login, PASSWORD, "Ivan");

        Integer courierId = null;

        try {
            createCourierAndCheckSuccess(courier);

            courierId = loginCourierAndGetId(
                    login,
                    PASSWORD
            );

            createCourierAndCheckError(courier, 409);

        } finally {
            if (courierId != null) {
                deleteCourier(courierId);
            }
        }
    }

    @Test
    @DisplayName("Create courier with existing login")
    @Description("Проверка невозможности создать курьера с существующим login")
    void createCourierWithExistingLogin() {

        String login = generateLogin();

        Courier firstCourier =
                new Courier(login, PASSWORD, "Ivan");

        Courier secondCourier =
                new Courier(login, "999999", "Petr");

        Integer courierId = null;

        try {
            createCourierAndCheckSuccess(firstCourier);

            courierId = loginCourierAndGetId(
                    login,
                    PASSWORD
            );

            createCourierAndCheckError(secondCourier, 409);

        } finally {
            if (courierId != null) {
                deleteCourier(courierId);
            }
        }
    }

    @Test
    @DisplayName("Create courier without first name")
    @Description("Проверка создания курьера без необязательного firstName")
    void createCourierWithoutFirstName() {

        String login = generateLogin();

        Courier courier =
                new Courier(login, PASSWORD, null);

        Integer courierId = null;

        try {
            createCourierAndCheckSuccess(courier);

            courierId = loginCourierAndGetId(
                    login,
                    PASSWORD
            );

        } finally {
            if (courierId != null) {
                deleteCourier(courierId);
            }
        }
    }

    @Step("Create courier successfully")
    private void createCourierAndCheckSuccess(Courier courier) {

        given()
                .header("Content-type", CONTENT_TYPE)
                .body(gson.toJson(courier))
                .when()
                .post(COURIER_PATH)
                .then()
                .statusCode(201)
                .body("ok", equalTo(true));
    }

    @Step("Create courier and check error {expectedStatusCode}")
    private void createCourierAndCheckError(
            Courier courier,
            int expectedStatusCode) {

        given()
                .header("Content-type", CONTENT_TYPE)
                .body(gson.toJson(courier))
                .when()
                .post(COURIER_PATH)
                .then()
                .statusCode(expectedStatusCode)
                .body("message", notNullValue());
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

    @Step("Delete courier with id {courierId}")
    private void deleteCourier(Integer courierId) {

        given()
                .when()
                .delete(COURIER_PATH + "/{id}", courierId)
                .then()
                .statusCode(200)
                .body("ok", equalTo(true));
    }

    private String generateLogin() {
        return "courier" + new Random().nextInt(1000000);
    }
}