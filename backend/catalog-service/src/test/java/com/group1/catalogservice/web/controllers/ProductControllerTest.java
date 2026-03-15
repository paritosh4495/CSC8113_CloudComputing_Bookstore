package com.group1.catalogservice.web.controllers;

import com.group1.catalogservice.AbstractIT;
import com.group1.catalogservice.SecurityTestConfig;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;

import static org.hamcrest.Matchers.*;

@Sql("/test-data.sql")
class ProductControllerTest extends AbstractIT {



    @Nested
    class CreateProductTest {

        @Test
        void shouldCreateProductSuccessfully() {
            var payload = """
                    {
                      "name": "The Test Gatsby", "author": "F. Scott Fitzgerald",
                      "isbn": "978-0743273563",
                      "description": "A novel set in the Jazz Age on Long Island.",
                      "imageUrl": "https://example.com/images/gatsby.jpg", "price": 15.99,
                      "genre": "Classic Literature", "publisher": "Scribner",
                      "publicationYear": 1925, "stockQuantity": 50, "status": "AVAILABLE"
                    }
                    """;

            RestAssured.given()
                    .header("Authorization", "Bearer " + SecurityTestConfig.adminToken())
                    .contentType(ContentType.JSON).body(payload)
                    .when().post("/api/products")
                    .then().statusCode(201)
                    .body("code", notNullValue())
                    .body("name", is("The Test Gatsby"))
                    .body("author", is("F. Scott Fitzgerald"))
                    .body("isbn", is("978-0743273563"))
                    .body("description", is("A novel set in the Jazz Age on Long Island."))
                    .body("imageUrl", is("https://example.com/images/gatsby.jpg"))
                    .body("price", is(15.99f));
        }

        @Test
        void shouldReturnForbiddenWhenCreatingProductWithoutAdminRole() {
            var payload = """
                    {
                      "name": "The Test Gatsby", "author": "F. Scott Fitzgerald",
                      "isbn": "978-0743273563", "description": "A novel.",
                      "imageUrl": "https://example.com/images/gatsby.jpg", "price": 15.99,
                      "genre": "Classic Literature", "publisher": "Scribner",
                      "publicationYear": 1925, "stockQuantity": 50, "status": "AVAILABLE"
                    }
                    """;

            RestAssured.given()
                    .header("Authorization", "Bearer " + SecurityTestConfig.userToken())
                    .contentType(ContentType.JSON).body(payload)
                    .when().post("/api/products")
                    .then().statusCode(403);
        }

        @Test
        void shouldReturnConflictForDuplicateIsbn() {
            var payload = """
                    {
                      "name": "The Test Gatsby", "author": "F. Scott Fitzgerald",
                      "isbn": "9780061120084", "description": "A novel.",
                      "imageUrl": "https://example.com/images/gatsby.jpg", "price": 15.99,
                      "genre": "Classic Literature", "publisher": "Scribner",
                      "publicationYear": 1925, "stockQuantity": 50, "status": "AVAILABLE"
                    }
                    """;

            RestAssured.given()
                    .header("Authorization", "Bearer " + SecurityTestConfig.adminToken())
                    .contentType(ContentType.JSON).body(payload)
                    .when().post("/api/products")
                    .then().statusCode(409);
        }

        @Test
        void shouldReturnBadRequestForInvalidPayload() {
            var invalidPayload = """
                    {
                      "name": "", "description": "Missing required fields.",
                      "author": "", "genre": "Software",
                      "isbn": "invalid-isbn", "price": -5.00, "stockQuantity": -1
                    }
                    """;

            RestAssured.given()
                    .header("Authorization", "Bearer " + SecurityTestConfig.adminToken())
                    .contentType(ContentType.JSON).body(invalidPayload)
                    .when().post("/api/products")
                    .then().statusCode(400).body("errors", notNullValue());
        }
    }

    @Nested
    class UpdateProductTest {

        @Test
        void shouldUpdateProductSuccessfully() {
            var payload = """
                    {
                      "name": "Clean Code Updated", "description": "Updated edition.",
                      "author": "Robert C. Martin", "genre": "Software",
                      "isbn": "9780132350884", "price": 44.99, "stockQuantity": 15
                    }
                    """;

            RestAssured.given()
                    .header("Authorization", "Bearer " + SecurityTestConfig.adminToken())
                    .contentType(ContentType.JSON).body(payload)
                    .when().put("/api/products/B001")
                    .then().statusCode(200)
                    .body("code", is("B001"))
                    .body("name", is("Clean Code Updated"))
                    .body("price", is(44.99f))
                    .body("stockQuantity", is(15));
        }

        @Test
        void shouldReturnForbiddenWhenUpdatingProductWithoutAdminRole() {
            var payload = """
                    {
                      "name": "Clean Code Updated", "description": "Updated edition.",
                      "author": "Robert C. Martin", "genre": "Software",
                      "isbn": "9780132350884", "price": 44.99, "stockQuantity": 15
                    }
                    """;

            RestAssured.given()
                    .header("Authorization", "Bearer " + SecurityTestConfig.userToken())
                    .contentType(ContentType.JSON).body(payload)
                    .when().put("/api/products/BOOK-001")
                    .then().statusCode(403);
        }

        @Test
        void shouldReturnNotFoundWhenUpdatingNonExistentProduct() {
            var payload = """
                    {
                      "name": "Ghost Book", "description": "Does not exist.",
                      "author": "Nobody", "genre": "Fiction",
                      "isbn": "9999999999999", "price": 9.99, "stockQuantity": 1
                    }
                    """;

            RestAssured.given()
                    .header("Authorization", "Bearer " + SecurityTestConfig.adminToken())
                    .contentType(ContentType.JSON).body(payload)
                    .when().put("/api/products/NONEXISTENT-CODE")
                    .then().statusCode(404);
        }

        @Test
        void shouldUpdateProductPriceSuccessfully() {
            RestAssured.given()
                    .header("Authorization", "Bearer " + SecurityTestConfig.adminToken())
                    .when().patch("/api/products/B001/price?newPrice=29.99")
                    .then().statusCode(200)
                    .body("code", is("B001"))
                    .body("price", is(29.99f));
        }

        @Test
        void shouldReturnForbiddenWhenUpdatingPriceWithoutAdminRole() {
            RestAssured.given()
                    .header("Authorization", "Bearer " + SecurityTestConfig.userToken())
                    .when().patch("/api/products/BOOK-001/price?newPrice=29.99")
                    .then().statusCode(403);
        }

        @Test
        void shouldReturnBadRequestWhenPriceParamIsMissing() {
            RestAssured.given()
                    .header("Authorization", "Bearer " + SecurityTestConfig.adminToken())
                    .when().patch("/api/products/BOOK-001/price")
                    .then().statusCode(400);
        }

        @Test
        void shouldReturnNotFoundWhenUpdatingPriceForNonExistentProduct() {
            RestAssured.given()
                    .header("Authorization", "Bearer " + SecurityTestConfig.adminToken())
                    .when().patch("/api/products/NONEXISTENT-CODE/price?newPrice=19.99")
                    .then().statusCode(404);
        }
    }

    @Nested
    class DeleteProductTest {

        @Test
        void shouldDeleteProductSuccessfully() {
            RestAssured.given()
                    .header("Authorization", "Bearer " + SecurityTestConfig.adminToken())
                    .when().delete("/api/products/B001")
                    .then().statusCode(204);

            RestAssured.given() // GET is public — no token needed
                    .when().get("/api/products/B001")
                    .then().statusCode(404);
        }

        @Test
        void shouldReturnForbiddenWhenDeletingProductWithoutAdminRole() {
            RestAssured.given()
                    .header("Authorization", "Bearer " + SecurityTestConfig.userToken())
                    .when().delete("/api/products/B001")
                    .then().statusCode(403);

            RestAssured.given()
                    .when().get("/api/products/B001")
                    .then().statusCode(200);
        }

        @Test
        void shouldReturn401WhenDeletingProductWithNoToken() {
            // No token at all → 401 Unauthenticated (not 403 — Spring knows nothing about who you are)
            RestAssured.given()
                    .when().delete("/api/products/B001")
                    .then().statusCode(401);
        }

        @Test
        void shouldReturnNotFoundWhenDeletingNonExistentProduct() {
            RestAssured.given()
                    .header("Authorization", "Bearer " + SecurityTestConfig.adminToken())
                    .when().delete("/api/products/NONEXISTENT-CODE")
                    .then().statusCode(404);
        }
    }

    @Nested
    class GetProductTest {

        // All GET tests are completely unchanged — no token needed, all public
        @Test
        void shouldGetProductByCodeSuccessfully() {
            RestAssured.given()
                    .when().get("/api/products/B001")
                    .then().statusCode(200)
                    .body("code", is("B001"))
                    .body("name", is("The Great Gatsby"))
                    .body("author", is("F. Scott Fitzgerald"))
                    .body("genre", is("Fiction"))
                    .body("isbn", is("9780743273565"));
        }

        @Test
        void shouldReturnNotFoundForNonExistentProductCode() {
            RestAssured.given()
                    .when().get("/api/products/NONEXISTENT-CODE")
                    .then().statusCode(404);
        }

        @Test
        void shouldGetAllProductsWithDefaultPagination() {
            RestAssured.given()
                    .when().get("/api/products")
                    .then().statusCode(200)
                    .body("data", not(empty()))
                    .body("pageNumber", is(1))
                    .body("totalElements", is(20))
                    .body("totalPages", is(2));
        }

        @Test
        void shouldGetAllProductsOnPageTwo() {
            RestAssured.given()
                    .queryParam("page", 2)
                    .when().get("/api/products")
                    .then().statusCode(200)
                    .body("data", not(empty()))
                    .body("pageNumber", is(2));
        }

        @Test
        void shouldCheckProductAvailabilityWhenInStock() {
            RestAssured.given()
                    .when().get("/api/products/B006/availability")
                    .then().statusCode(200)
                    .body("code", is("B006"))
                    .body("available", is(true));
        }

        @Test
        void shouldReturnNotFoundWhenCheckingAvailabilityForNonExistentProduct() {
            RestAssured.given()
                    .when().get("/api/products/NONEXISTENT-CODE/availability")
                    .then().statusCode(404);
        }

        @Nested
        class SearchProductsTest {

            @Test
            void shouldSearchProductsByQueryKeyword() {
                RestAssured.given()
                        .queryParam("query", "Catcher")
                        .when().get("/api/products/search")
                        .then().statusCode(200).body("data", hasSize(2));
            }

            @Test
            void shouldSearchProductsByGenre() {
                RestAssured.given()
                        .queryParam("genre", "Dystopian")
                        .when().get("/api/products/search")
                        .then().statusCode(200).body("data", hasSize(4));
            }

            @Test
            void shouldSearchProductsByAuthor() {
                RestAssured.given()
                        .queryParam("author", "George Orwell")
                        .when().get("/api/products/search")
                        .then().statusCode(200)
                        .body("data", hasSize(1))
                        .body("data[0].code", is("B002"))
                        .body("data[0].name", is("1984"));
            }

            @Test
            void shouldSearchProductsByName() {
                RestAssured.given()
                        .queryParam("name", "The Hobbit")
                        .when().get("/api/products/search")
                        .then().statusCode(200)
                        .body("data", hasSize(1))
                        .body("data[0].code", is("B006"));
            }

            @Test
            void shouldSearchProductsByIsbn() {
                RestAssured.given()
                        .queryParam("isbn", "9780451524935")
                        .when().get("/api/products/search")
                        .then().statusCode(200)
                        .body("data", hasSize(1))
                        .body("data[0].code", is("B002"));
            }

            @Test
            void shouldSearchProductsByPriceRange() {
                RestAssured.given()
                        .queryParam("minPrice", "9.99")
                        .queryParam("maxPrice", "11.99")
                        .when().get("/api/products/search")
                        .then().statusCode(200)
                        .body("data.price", everyItem(
                                allOf(greaterThanOrEqualTo(9.99f), lessThanOrEqualTo(11.99f))));
            }

            @Test
            void shouldSearchProductsByMultipleCriteria() {
                RestAssured.given()
                        .queryParam("genre", "Horror")
                        .queryParam("author", "Stephen King")
                        .when().get("/api/products/search")
                        .then().statusCode(200)
                        .body("data", hasSize(1))
                        .body("data[0].code", is("B013"))
                        .body("data[0].name", is("The Shining"));
            }

            @Test
            void shouldReturnEmptyResultForSearchWithNoMatches() {
                RestAssured.given()
                        .queryParam("name", "NonExistentBookTitle12345")
                        .when().get("/api/products/search")
                        .then().statusCode(200).body("data", empty());
            }

            @Test
            void shouldReturnAllProductsWhenSearchHasNoFilters() {
                RestAssured.given()
                        .when().get("/api/products/search")
                        .then().statusCode(200).body("totalElements", is(20));
            }
        }

        @Nested
        class StockManagementTest {

            @Test
            void shouldReserveStockSuccessfully() {
                int initialStock = RestAssured.given()
                        .when().get("/api/products/B001")
                        .then().statusCode(200).extract().path("stockQuantity");

                RestAssured.given()
                        .header("Authorization", "Bearer " + SecurityTestConfig.serviceToken())
                        .contentType(ContentType.JSON)
                        .body("""
{ "quantity": 2 }""")
                        .when().post("/api/products/B001/reserve")
                        .then().statusCode(200);

                RestAssured.given()
                        .when().get("/api/products/B001")
                        .then().statusCode(200)
                        .body("stockQuantity", is(initialStock - 2));
            }

            @Test
            void shouldReturnConflictWhenReservingMoreThanAvailableStock() {
                RestAssured.given()
                        .header("Authorization", "Bearer " + SecurityTestConfig.serviceToken())
                        .contentType(ContentType.JSON)
                        .body("""
{ "quantity": 99999 }""")
                        .when().post("/api/products/B001/reserve")
                        .then().statusCode(409)
                        .body("title", containsString("Insufficient"));
            }

            @Test
            void shouldReleaseStockSuccessfully() {
                int initialStock = RestAssured.given()
                        .when().get("/api/products/B001")
                        .then().statusCode(200).extract().path("stockQuantity");

                RestAssured.given()
                        .header("Authorization", "Bearer " + SecurityTestConfig.serviceToken())
                        .contentType(ContentType.JSON)
                        .body("""
{ "quantity": 5 }""")
                        .when().post("/api/products/B001/release")
                        .then().statusCode(200);

                RestAssured.given()
                        .when().get("/api/products/B001")
                        .then().statusCode(200)
                        .body("stockQuantity", is(initialStock + 5));
            }

            @Test
            void shouldReturnBadRequestForInvalidQuantity() {
                RestAssured.given()
                        .header("Authorization", "Bearer " + SecurityTestConfig.serviceToken())
                        .contentType(ContentType.JSON)
                        .body("""
{ "quantity": 0 }""")
                        .when().post("/api/products/B001/reserve")
                        .then().statusCode(400);
            }
        }
    }
}