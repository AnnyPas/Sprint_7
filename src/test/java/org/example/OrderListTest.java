package org.example;

import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;

public class OrderListTest {

    private static final String BASE_URI =
            "https://qa-scooter.praktikum-services.ru";

    private static final String ORDERS_PATH =
            "/api/v1/orders";

    @BeforeEach
    public void setUp() {
        RestAssured.baseURI = BASE_URI;
    }

    @Test
    @DisplayName("Get orders list")
    @Description("Проверка получения списка заказов")

    void getOrdersListTest() {

        getOrdersList();
    }

    @Step("Get orders list")
    private void getOrdersList() {

        given()
                .when()
                .get(ORDERS_PATH)
                .then()
                .statusCode(200)
                .body("orders", notNullValue())
                .body("orders", instanceOf(List.class));
    }
}