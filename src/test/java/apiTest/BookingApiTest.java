package apiTest;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class BookingApiTest {

    Integer bookingId;
    String accessToken;

    //Login as precondition

    @BeforeClass
    public void testLoginToApp() {
        String endpoint = "https://restful-booker.herokuapp.com/auth";
        String body = """
                {
                    "username" : "admin",
                    "password" : "password123"
                }
                """;
        Response response = given().header("Content-Type", "application/json")
                .body(body)
                .when()
                .post(endpoint)
                .then().extract().response();

        JsonPath jsonPath = response.jsonPath();
        accessToken = jsonPath.getString("token");

    }

    //Create Booking

    @Test(priority = 0)
    public void testCreateBooking() {
        String endpoint = "https://restful-booker.herokuapp.com/booking";
        String body = """
                {
                    "firstname" : "Jim",
                    "lastname" : "Brown",
                    "totalprice" : 111,
                    "depositpaid" : true,
                    "bookingdates" : {
                        "checkin" : "2018-01-01",
                        "checkout" : "2019-01-01"
                    },
                    "additionalneeds" : "Breakfast"
                }
                """;

        var responseToValidate = given().body(body).header("Content-Type", "application/json")
                .when()
                .post(endpoint)
                .then();

        responseToValidate.statusCode(200)
                .assertThat().body("booking.firstname", equalTo("Jim"))
                .body("booking.lastname", equalTo("Brown"))
                .body("booking.totalprice", equalTo(111))
                .body("booking.depositpaid", equalTo(true))
                .body("booking.bookingdates.checkin", equalTo("2018-01-01"))
                .body("booking.bookingdates.checkout", equalTo("2019-01-01"))
                .body("booking.additionalneeds", equalTo("Breakfast"))
                .log().all();


        Response response = responseToValidate.extract().response();
        JsonPath jsonPath = response.jsonPath();
        bookingId = jsonPath.getInt("bookingid");
    }


    //Edit Booking

    @Test(priority = 1 , dependsOnMethods = "testCreateBooking")
    public void testEditBooking() {
        String endpoint = "https://restful-booker.herokuapp.com/booking/" + bookingId;
        String body = """
                {
                    "firstname" : "James",
                    "lastname" : "Brown",
                    "totalprice" : 111,
                    "depositpaid" : true,
                    "bookingdates" : {
                        "checkin" : "2018-01-01",
                        "checkout" : "2019-01-01"
                    },
                    "additionalneeds" : "Breakfast"
                }
                """;
        var responseToValidate = given().body(body)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Cookie", "token=" + accessToken)
                .when()
                .put(endpoint)
                .then();
        responseToValidate.statusCode(200)
                .assertThat().body("firstname", equalTo("James"))
                .body("lastname", equalTo("Brown"))
                .body("totalprice", equalTo(111))
                .body("depositpaid", equalTo(true))
                .body("bookingdates.checkin", equalTo("2018-01-01"))
                .body("bookingdates.checkout", equalTo("2019-01-01"))
                .body("additionalneeds", equalTo("Breakfast"))
                .log().all();
    }

    //Read Booking
    @Test(priority = 2 , dependsOnMethods = "testEditBooking")
    public void testGetBooking() {
        String endpoint = "https://restful-booker.herokuapp.com/booking/" + bookingId;

        var responseToValidate = given()
                .header("Content-Type", "application/json")
                .when()
                .get(endpoint)
                .then();

        responseToValidate.statusCode(200)
                .assertThat().body("firstname", equalTo("James"))
                .body("lastname", equalTo("Brown"))
                .body("totalprice", equalTo(111))
                .body("depositpaid", equalTo(true))
                .body("bookingdates.checkin", equalTo("2018-01-01"))
                .body("bookingdates.checkout", equalTo("2019-01-01"))
                .body("additionalneeds", equalTo("Breakfast"))
                .log().all();
    }

    //Delete Booking
    @Test(priority = 3 , dependsOnMethods = "testGetBooking")
    public void testDeleteBooking() {
        String endpoint = "https://restful-booker.herokuapp.com/booking/" + bookingId;

        var responseToValidate = given()
                .header("Content-Type", "application/json")
                .header("Cookie", "token=" + accessToken)
                .when()
                .delete(endpoint)
                .then();

        responseToValidate.assertThat().statusCode(201);


        Response response = responseToValidate.extract().response();


        Assert.assertEquals(response.asString(), "Created");
    }
}
