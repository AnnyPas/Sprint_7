package org.example;

import com.google.gson.Gson;
import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class OrderGetByTrackTest {

    private static final String BASE_URI =
            "https://qa-scooter.praktikum-services.ru";

    private static final String ORDERS_PATH =
            "/api/v1/orders";

    private static final String ORDER_TRACK_PATH =
            "/api/v1/orders/track";

    private static final String CONTENT_TYPE =
            "application/json";

    private static final int NONEXISTENT_TRACK =
            999999999;

    private Gson gson;

    @BeforeEach
    public void setUp() {
        RestAssured.baseURI = BASE_URI;
        gson = new Gson();
    }

    @Test
    @DisplayName("Get order by track")
    @Description("Проверка успешного получения заказа по его номеру")
    void getOrderByTrackSuccess() {

        Integer track = createOrderAndGetTrack();

        getOrderByTrackAndCheckSuccess(track);
    }

    @Test
    @DisplayName("Get order without track")
    @Description("Проверка ошибки при запросе заказа без номера")
    void getOrderWithoutTrack() {

        getOrderWithoutTrackAndCheckError();
    }

    @Test
    @DisplayName("Get order with nonexistent track")
    @Description("Проверка ошибки при запросе несуществующего заказа")
    void getOrderWithNonexistentTrack() {

        getNonexistentOrderAndCheckError(
                NONEXISTENT_TRACK
        );
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

    @Step("Get order by track")
    private void getOrderByTrackAndCheckSuccess(
            Integer track) {

        given()
                .queryParam("t", track)
                .when()
                .get(ORDER_TRACK_PATH)
                .then()
                .statusCode(200)
                .body("order", notNullValue())
                .body("order.track", equalTo(track));
    }

    @Step("Get order without track and check error")
    private void getOrderWithoutTrackAndCheckError() {

        given()
                .when()
                .get(ORDER_TRACK_PATH)
                .then()
                .statusCode(400)
                .body("message", notNullValue());
    }

    @Step("Get nonexistent order and check error")
    private void getNonexistentOrderAndCheckError(
            Integer track) {

        given()
                .queryParam("t", track)
                .when()
                .get(ORDER_TRACK_PATH)
                .then()
                .statusCode(404)
                .body("message", notNullValue());
    }
}