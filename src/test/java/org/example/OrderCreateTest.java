package org.example;

import com.google.gson.Gson;
import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

public class OrderCreateTest {

    private static final String BASE_URI =
            "https://qa-scooter.praktikum-services.ru";

    private static final String ORDERS_PATH =
            "/api/v1/orders";

    private static final String CONTENT_TYPE =
            "application/json";

    private Gson gson;

    @BeforeEach
    public void setUp() {
        RestAssured.baseURI = BASE_URI;
        gson = new Gson();
    }

    @ParameterizedTest(name = "Create order: {0}")
    @MethodSource("orderColors")
    @DisplayName("Create order with different colors")
    @Description("Проверка создания заказа с разными вариантами цвета")
    void createOrderWithDifferentColors(
            String testName,
            List<String> color) {

        Order order = new Order(
                "Naruto",
                "Uchiha",
                "Konoha, 142 apt.",
                4,
                "+7 800 355 35 35",
                5,
                "2026-08-20",
                "Test order",
                color
        );

        createOrderAndCheckTrack(order);
    }

    @Step("Create order and check track")
    private void createOrderAndCheckTrack(Order order) {

        given()
                .header("Content-type", CONTENT_TYPE)
                .body(gson.toJson(order))
                .when()
                .post(ORDERS_PATH)
                .then()
                .statusCode(201)
                .body("track", notNullValue());
    }

    static Stream<Arguments> orderColors() {

        return Stream.of(
                Arguments.of(
                        "BLACK",
                        List.of("BLACK")
                ),
                Arguments.of(
                        "GREY",
                        List.of("GREY")
                ),
                Arguments.of(
                        "BLACK and GREY",
                        List.of("BLACK", "GREY")
                ),
                Arguments.of(
                        "without color",
                        null
                )
        );
    }
}