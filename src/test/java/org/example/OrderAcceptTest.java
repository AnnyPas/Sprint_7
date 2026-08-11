package org.example;

import com.google.gson.Gson;
import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class OrderAcceptTest {

    private static final String BASE_URI =
            "https://qa-scooter.praktikum-services.ru";

    private static final String COURIER_PATH =
            "/api/v1/courier";

    private static final String LOGIN_PATH =
            "/api/v1/courier/login";

    private static final String ORDERS_PATH =
            "/api/v1/orders";

    private static final String ORDER_TRACK_PATH =
            "/api/v1/orders/track";

    private static final String ORDER_ACCEPT_PATH =
            "/api/v1/orders/accept";

    private static final String CONTENT_TYPE =
            "application/json";

    private static final String PASSWORD =
            "123456";

    private static final int NONEXISTENT_COURIER_ID =
            999999999;

    private static final int NONEXISTENT_ORDER_ID =
            999999999;

    private Gson gson;

    @BeforeEach
    public void setUp() {
        RestAssured.baseURI = BASE_URI;
        gson = new Gson();
    }

    @Test
    @DisplayName("Successful order acceptance")
    @Description("Проверка успешного принятия заказа курьером")
    void acceptOrderSuccess() {

        Integer track = createOrderAndGetTrack();
        Integer orderId = getOrderIdByTrack(track);

        String login = generateLogin();

        createCourier(login, PASSWORD);

        Integer courierId =
                loginCourierAndGetId(login, PASSWORD);

        try {
            acceptOrderAndCheckSuccess(
                    orderId,
                    courierId
            );

        } finally {
            deleteCourier(courierId);
        }
    }

    @Test
    @DisplayName("Accept order without courier id")
    @Description("Проверка ошибки при принятии заказа без courierId")
    void acceptOrderWithoutCourierId() {

        Integer track = createOrderAndGetTrack();
        Integer orderId = getOrderIdByTrack(track);

        acceptOrderWithoutCourierIdAndCheckError(
                orderId
        );
    }

    @Test
    @DisplayName("Accept order with incorrect courier id")
    @Description("Проверка ошибки при передаче несуществующего courierId")
    void acceptOrderWithIncorrectCourierId() {

        Integer track = createOrderAndGetTrack();
        Integer orderId = getOrderIdByTrack(track);

        acceptOrderWithIncorrectCourierIdAndCheckError(
                orderId,
                NONEXISTENT_COURIER_ID
        );
    }

    @Test
    @DisplayName("Accept order without order id")
    @Description("Проверка ошибки при принятии заказа без id заказа")
    void acceptOrderWithoutOrderId() {

        String login = generateLogin();

        createCourier(login, PASSWORD);

        Integer courierId =
                loginCourierAndGetId(login, PASSWORD);

        try {
            acceptOrderWithoutOrderIdAndCheckError(
                    courierId
            );

        } finally {
            deleteCourier(courierId);
        }
    }

    @Test
    @DisplayName("Accept order with incorrect order id")
    @Description("Проверка ошибки при передаче несуществующего id заказа")
    void acceptOrderWithIncorrectOrderId() {

        String login = generateLogin();

        createCourier(login, PASSWORD);

        Integer courierId =
                loginCourierAndGetId(login, PASSWORD);

        try {
            acceptOrderWithIncorrectOrderIdAndCheckError(
                    NONEXISTENT_ORDER_ID,
                    courierId
            );

        } finally {
            deleteCourier(courierId);
        }
    }

    @Step("Create order and get track")
    private Integer createOrderAndGetTrack() {

        Order order = new Order(
                "Naruto",
                "Uchiha",
                "Konoha, 142 apt.",
                4,
                "+7 800 355 35 35",
                5,
                "2026-08-20",
                "Test order",
                List.of("BLACK")
        );

        return given()
                .header("Content-type", CONTENT_TYPE)
                .body(gson.toJson(order))
                .when()
                .post(ORDERS_PATH)
                .then()
                .statusCode(201)
                .body("track", notNullValue())
                .extract()
                .path("track");
    }

    @Step("Get order id by track")
    private Integer getOrderIdByTrack(Integer track) {

        return given()
                .queryParam("t", track)
                .when()
                .get(ORDER_TRACK_PATH)
                .then()
                .statusCode(200)
                .body("order.id", notNullValue())
                .extract()
                .path("order.id");
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

    @Step("Accept order successfully")
    private void acceptOrderAndCheckSuccess(
            Integer orderId,
            Integer courierId) {

        given()
                .queryParam("courierId", courierId)
                .when()
                .put(
                        ORDER_ACCEPT_PATH + "/{id}",
                        orderId
                )
                .then()
                .statusCode(200)
                .body("ok", equalTo(true));
    }

    @Step("Accept order without courier id and check error")
    private void acceptOrderWithoutCourierIdAndCheckError(
            Integer orderId) {

        given()
                .when()
                .put(
                        ORDER_ACCEPT_PATH + "/{id}",
                        orderId
                )
                .then()
                .statusCode(400)
                .body("message", notNullValue());
    }

    @Step("Accept order with nonexistent courier id and check error")
    private void acceptOrderWithIncorrectCourierIdAndCheckError(
            Integer orderId,
            Integer courierId) {

        given()
                .queryParam("courierId", courierId)
                .when()
                .put(
                        ORDER_ACCEPT_PATH + "/{id}",
                        orderId
                )
                .then()
                .statusCode(404)
                .body("message", notNullValue());
    }

    @Step("Accept order without order id and check error")
    private void acceptOrderWithoutOrderIdAndCheckError(
            Integer courierId) {

        given()
                .queryParam("courierId", courierId)
                .when()
                .put(ORDER_ACCEPT_PATH + "/")
                .then()
                .statusCode(400)
                .body("message", notNullValue());
    }

    @Step("Accept nonexistent order and check error")
    private void acceptOrderWithIncorrectOrderIdAndCheckError(
            Integer orderId,
            Integer courierId) {

        given()
                .queryParam("courierId", courierId)
                .when()
                .put(
                        ORDER_ACCEPT_PATH + "/{id}",
                        orderId
                )
                .then()
                .statusCode(404)
                .body("message", notNullValue());
    }

    @Step("Delete courier")
    private void deleteCourier(Integer courierId) {

        given()
                .when()
                .delete(
                        COURIER_PATH + "/{id}",
                        courierId
                )
                .then()
                .statusCode(200)
                .body("ok", equalTo(true));
    }

    private String generateLogin() {
        return "courier"
                + new Random().nextInt(1000000);
    }
}