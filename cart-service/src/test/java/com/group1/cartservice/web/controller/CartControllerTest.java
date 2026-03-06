package com.group1.cartservice.web.controller;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.group1.cartservice.AbstractIT;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;


import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@Sql("/test-data.sql")
class CartControllerTest extends AbstractIT {

    @Nested
    class AddItemToCartTest {

        @Test
        void shouldAddItemToCartSuccessfully() {
            //1. Set the wiremock
            String productCode = "B002";
            mockGetProductByCode(productCode, "Some Book", new BigDecimal("3.44"));
            mockCheckAvailability(productCode, 100);
            mockReserveStockSuccess(productCode);

            // 2. Call the API to add item to cart
            String requestBody = """
                    {
                      "productCode": "B002",
                      "quantity": 2
                    }
                    """;

            RestAssured.given()
                    .contentType(ContentType.JSON)
                    .header("X-User-Id", "user-123")
                    .body(requestBody)
                    .when()
                    .post("/api/carts/items")
                    .then()
                    .statusCode(201)
                    .body("userId", Matchers.is("user-123"))
                    .body("status", Matchers.is("ACTIVE"))
                    .body("totalItems", Matchers.is(2))
                    .body("totalPrice", Matchers.is(6.88f))
                    .body("items", Matchers.hasSize(1))
                    .body("items[0].productCode", Matchers.is("B002"))
                    .body("items[0].quantity", Matchers.is(2));
        }

        @Test
        void shouldReturnBadRequestWhenProductNotFound() {

            wireMockServer.stubFor(WireMock.get(WireMock.urlMatching("/api/products/INVALID_CODE"))
                    .willReturn(WireMock.aResponse()
                            .withStatus(404)));

            String requestBody = """
                    {
                      "productCode": "INVALID_CODE",
                      "quantity": 2
                    }
                    """;

            RestAssured.given()
                    .contentType(ContentType.JSON)
                    .header("X-User-Id", "user-123")
                    .body(requestBody)
                    .when()
                    .post("/api/carts/items")
                    .then()
                    .statusCode(400);
        }

        @Test
        void shouldReturnConflictWhenStockIsInsufficient() {

            String productCode = "B003";
            mockGetProductByCode(productCode, "Another Book", new BigDecimal("5.00"));
            mockCheckAvailability(productCode, 1); // Only 1 item available

            // Attempt to add 5 items to cart, which exceeds available stock
            String requestBody = """
                    {
                      "productCode": "B003",
                      "quantity": 5
                    }
                    """;

            RestAssured.given()
                    .contentType(ContentType.JSON)
                    .header("X-User-Id", "user-123")
                    .body(requestBody)
                    .when()
                    .post("/api/carts/items")
                    .then()
                    .statusCode(409);
        }

        @Test
        void shouldReturnServiceUnavailableWhenCatalogServiceIsDown() {

            wireMockServer.stubFor(WireMock.get(WireMock.urlMatching("/api/products/ANY_CODE"))
                    .willReturn(WireMock.aResponse()
                            .withHeader("Content-Type", "application/json")
                            .withStatus(503)));

            String requestBody = """
                    {
                      "productCode": "ANY_CODE",
                      "quantity": 1 
                     }
                    """;

            RestAssured.given()
                    .contentType(ContentType.JSON)
                    .header("X-User-Id", "user-123")
                    .body(requestBody)
                    .when()
                    .post("/api/carts/items")
                    .then()
                    .statusCode(503);

        }

        @Test
        void shouldReturn400_whenProductCodeIsBlank() {
            String requestBody = """
                    {
                      "productCode": "",
                      "quantity": 1 
                     }
                    """;

            RestAssured.given()
                    .contentType(ContentType.JSON)
                    .header("X-User-Id", "user-1")
                    .body(requestBody)
                    .when().post("/api/carts/items")
                    .then().statusCode(400)
                    .body("title", Matchers.is("Bad Request"));
        }

        @Test
        void shouldReturn400_whenQuantityIsNegative() {

            String requestBody = """
                    {
                      "productCode": "B001",
                      "quantity": -1 
                     }
                    """;

            RestAssured.given()
                    .contentType(ContentType.JSON)
                    .header("X-User-Id", "user-1")
                    .body(requestBody)
                    .when().post("/api/carts/items")
                    .then().statusCode(400);
        }

    }


        @Nested
        class UpdateCartItemTest {

            @Test
            void shouldIncreaseQuantity_andReserveDifference() {
                // From our SQL, user-1 owns item 101 (B001), which currently has qty 2.
                // We want to increase it to 5. This means we need to reserve 3 more.
                mockCheckAvailability("B001", 10);
                mockReserveStockSuccess("B001");

                String requestBody = """
                 {
                  "quantity": 5
                 }
                 """;

                RestAssured.given()
                        .contentType(ContentType.JSON)
                        .header("X-User-Id", "user-1") // Matches the SQL data!
                        .body(requestBody)
                        .when()
                        .put("/api/carts/items/{itemId}", 101) // Matches the SQL data!
                        .then()
                        .statusCode(200)
                        .body("items.find { it.id == 101 }.quantity", Matchers.is(5));
            }

            @Test
            void shouldDecreaseQuantity_andReleaseDifference() {

                mockReleaseStockSuccess("B001");
                mockCheckAvailability("B001", 10);

                String requestBody = """
                 {
                  "quantity": 1
                 }
                 """;

                RestAssured.given()
                        .contentType(ContentType.JSON)
                        .header("X-User-Id", "user-1")
                        .body(requestBody)
                        .when()
                        .put("/api/carts/items/{itemId}", 101)
                        .then()
                        .statusCode(200)
                        .body("items.find { it.id == 101 }.quantity", Matchers.is(1));
            }
            @Test
            void shouldReturn404_whenItemDoesNotBelongToCart() {

                String requestBody = """
                 {
                  "quantity": 3
                 }
                 """;


                RestAssured.given()
                        .contentType(ContentType.JSON)
                        .header("X-User-Id", "user-1")
                        .body(requestBody)
                        .when().put("/api/carts/items/99999")
                        .then().statusCode(404)
                        .body("title", Matchers.is("Resource Not Found"));
            }
        }

        @Nested
        class RemoveItemFromCartTest {

            @Test
            void shouldRemoveItemAndReleaseStock() {
                // We are deleting item 102 (B003, qty 1). The system should release the stock.
                mockReleaseStockSuccess("B003");

                RestAssured.given()
                        .header("X-User-Id", "user-1")
                        .when()
                        .delete("/api/carts/items/{itemId}", 102)
                        .then()
                        .statusCode(204);

                // Verify it's actually gone
                RestAssured.given()
                        .header("X-User-Id", "user-1")
                        .when()
                        .get("/api/carts")
                        .then()
                        .statusCode(200)
                        .body("items", Matchers.hasSize(1));
            }
        }

        @Nested
        class GetCartTest {

            @Test
            void shouldReturnExistingCart() {
                // Tests that the SQL data loaded correctly and our total calculations work
                RestAssured.given()
                        .header("X-User-Id", "user-1")
                        .when()
                        .get("/api/carts")
                        .then()
                        .statusCode(200)
                        .body("userId", Matchers.is("user-1"))
                        .body("items", Matchers.hasSize(2))
                        .body("totalItems", Matchers.is(3)) // 2 of B001 + 1 of B003
                        .body("totalPrice", Matchers.is(35.00f)); // (2 * 10.00) + (1 * 15.00)
            }

        }

    @Nested
    class ClearCartTest {

        @Test
        void shouldClearAllItemsFromCart() {
            mockReleaseStockSuccess("B001");
            mockReleaseStockSuccess("B003");

            RestAssured.given()
                    .header("X-User-Id", "user-1")
                    .when().delete("/api/carts")
                    .then().statusCode(204);

            RestAssured.given()
                    .header("X-User-Id", "user-1")
                    .when().get("/api/carts")
                    .then().statusCode(200)
                    .body("items", Matchers.hasSize(0))
                    .body("totalItems", Matchers.is(0));
        }

        @Test
        void shouldReturn404_whenNoActiveCart() {
            // brand-new-user has never created a cart — clearCart throws CartNotFoundException
            RestAssured.given()
                    .header("X-User-Id", "no-cart-user")
                    .when().delete("/api/carts")
                    .then().statusCode(404);
        }
    }




}