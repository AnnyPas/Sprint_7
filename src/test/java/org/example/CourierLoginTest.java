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

public class CourierLoginTest {

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
    @DisplayName("Successful courier login")
    @Description("Проверка успешной авторизации курьера")
    void loginCourierSuccess() {

        String login = generateLogin();

        createCourier(login, PASSWORD);

        Integer courierId = null;

        try {
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
    @DisplayName("Login courier without login")
    @Description("Проверка ошибки авторизации без login")
    void loginCourierWithoutLogin() {

        CourierLogin courierLogin =
                new CourierLogin(null, PASSWORD);

        loginCourierAndCheckError(
                courierLogin,
                400
        );
    }

    @Test
    @DisplayName("Login courier without password")
    @Description("Проверка ошибки авторизации без password")
    void loginCourierWithoutPassword() {

        CourierLogin courierLogin =
                new CourierLogin(generateLogin(), null);

        loginCourierAndCheckError(
                courierLogin,
                400
        );
    }

    @Test
    @DisplayName("Login courier with wrong login")
    @Description("Проверка ошибки авторизации с неверным login")
    void loginCourierWithWrongLogin() {

        String login = generateLogin();

        createCourier(login, PASSWORD);

        Integer courierId = null;

        try {
            courierId = loginCourierAndGetId(
                    login,
                    PASSWORD
            );

            CourierLogin courierLogin =
                    new CourierLogin(
                            "wrong" + login,
                            PASSWORD
                    );

            loginCourierAndCheckError(
                    courierLogin,
                    404
            );

        } finally {
            if (courierId != null) {
                deleteCourier(courierId);
            }
        }
    }

    @Test
    @DisplayName("Login courier with wrong password")
    @Description("Проверка ошибки авторизации с неверным password")
    void loginCourierWithWrongPassword() {

        String login = generateLogin();

        createCourier(login, PASSWORD);

        Integer courierId = null;

        try {
            courierId = loginCourierAndGetId(
                    login,
                    PASSWORD
            );

            CourierLogin courierLogin =
                    new CourierLogin(
                            login,
                            "wrongPassword"
                    );

            loginCourierAndCheckError(
                    courierLogin,
                    404
            );

        } finally {
            if (courierId != null) {
                deleteCourier(courierId);
            }
        }
    }

    @Test
    @DisplayName("Login nonexistent courier")
    @Description("Проверка ошибки авторизации несуществующего курьера")
    void loginNonexistentCourier() {

        CourierLogin courierLogin =
                new CourierLogin(
                        generateLogin(),
                        PASSWORD
                );

        loginCourierAndCheckError(
                courierLogin,
                404
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

    @Step("Login courier and check error {expectedStatusCode}")
    private void loginCourierAndCheckError(
            CourierLogin courierLogin,
            int expectedStatusCode) {

        given()
                .header("Content-type", CONTENT_TYPE)
                .body(gson.toJson(courierLogin))
                .when()
                .post(LOGIN_PATH)
                .then()
                .statusCode(expectedStatusCode)
                .body("message", notNullValue());
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