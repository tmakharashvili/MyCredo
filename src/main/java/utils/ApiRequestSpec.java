package utils;

import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

import static io.restassured.RestAssured.given;

public class ApiRequestSpec {
    private static final String BASE_URL = "http://test.api.css.credo.ge";

    // POST request specification
    public static RequestSpecification postRequestSpec() {
        return given()
                .baseUri(BASE_URL)
                .header("Content-Type", "application/json")
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON);
    }

    // POST response specification
    public static ResponseSpecification postResponseSpec() {
        return new ResponseSpecBuilder()
                .expectStatusCode(200)
                .expectContentType(ContentType.JSON)
                .build();
    }

    // Money transfer API request specification with path
    public static RequestSpecification moneyTransferEndpointSpec() {
        return postRequestSpec()
                .basePath("/api/v1/payments/MoneyTransfer");
    }
}
