package com.resttest.MainRestAPI;


import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.simple.JSONObject;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.relevantcodes.extentreports.ExtentReports;
import com.relevantcodes.extentreports.ExtentTest;
import com.relevantcodes.extentreports.LogStatus;

import gherkin.deps.com.google.gson.JsonElement;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;

/**
 * Unit test for simple App.
 */
public class AppTest 
{
    static ExtentReports extent;
    static ExtentTest logger;


    @BeforeTest
    public void startReport(){
        extent = new ExtentReports ("/Users/shdayananda/eclipse-workspace/MainRestAPI/data/extentReport.html",true);
        extent
                .addSystemInfo("Host Name", "app_test")
                .addSystemInfo("Environment", "Automation Testing")
                .addSystemInfo("User Name", "SD");
    }

    @Test
    public static void checkNumberOfItems(){
        logger = extent.startTest("checkNumberOfItems");
        ValidatableResponse response =
                given()
                        .header("Accept","application/json")
                        .when()
                        .get("https://fakestoreapi.com/products")
                        .then();
        int statusCode= given().when().get("https://fakestoreapi.com/products").getStatusCode();
        System.out.println("The response status is "+statusCode);
        System.out.println(response.extract().jsonPath().getList("$").size());
        int size = given()
                .header("Accept","application/json")
                .when()
                .get("https://fakestoreapi.com/products")
                .then().extract().jsonPath().getList("$").size();
        Assert.assertEquals(size ,20);
        logger.log(LogStatus.PASS, "Test Case Passed is checkNumberOfItems");
    }

    @Test
    public static void checkUniqueKeys(){
        logger = extent.startTest("checkUniqueKeys");
        Response response =
                given().headers("Content-Type", ContentType.JSON, "Accept", ContentType.JSON).
                        when().get("https://fakestoreapi.com/products").
                        then().contentType(ContentType.JSON).extract().response();
        String[] ids = response.jsonPath().getString("id").split(",");
        Set<String> set = new HashSet<>(Arrays.asList(ids));
        System.out.println(Arrays.asList(ids).size());
        System.out.println(set.size());
        boolean duplicates = false;
        System.out.println(duplicates);
        if(set.size() < Arrays.asList(ids).size()){
            duplicates = true;
        }
        Assert.assertFalse(duplicates);
        logger.log(LogStatus.PASS, "Test Case Passed is checkUniqueKeys");
    }

    @Test
    public void testPostRequest()
    {
        logger = extent.startTest("testPostRequest");
        RestAssured.baseURI ="https://fakestoreapi.com/products";
        RequestSpecification request = RestAssured.given();

        String excelPath = "/Users/shdayananda/eclipse-workspace/MainRestAPI/data/Data.xlsx";
        XSSFWorkbook xssfWorkbook = null;
        try {
            xssfWorkbook = new XSSFWorkbook(excelPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert xssfWorkbook != null;
        XSSFSheet xssfSheet = xssfWorkbook.getSheet("product");
        String title = xssfSheet.getRow(1).getCell(0).getStringCellValue();
        double price = xssfSheet.getRow(1).getCell(1).getNumericCellValue();
        String description = xssfSheet.getRow(1).getCell(2).getStringCellValue();
        String image = xssfSheet.getRow(1).getCell(3).getStringCellValue();
        String category = xssfSheet.getRow(1).getCell(4).getStringCellValue();

        JSONObject requestParams = new JSONObject();
        requestParams.put("title", title);// Cast
        requestParams.put("price", price);
        requestParams.put("description", description);
        requestParams.put("image", image);
        requestParams.put("category",  category);
        System.out.println(requestParams.toJSONString());
        request.contentType("application/json");
        request.body(requestParams.toString());
        Response response = request.post("");
        //ValidatableResponse validatableResponse = response.then();
        int statusCode = response.getStatusCode();
        System.out.println(response.getBody().peek().toString());
        String responseTitle = response.jsonPath().getString("title");
        double responsePrice = Double.parseDouble(response.jsonPath().getString("price"));
        String responseDescription = response.jsonPath().getString("description");
        String responseImage = response.jsonPath().getString("image");
        String responseCategory = response.jsonPath().getString("category");
        Assert.assertEquals(title, responseTitle);
        Assert.assertEquals(price, responsePrice);
        Assert.assertEquals(description, responseDescription);
        Assert.assertEquals(image, responseImage);
        Assert.assertEquals(category, responseCategory);
        Assert.assertEquals(statusCode, 200);
        response.then().assertThat().body(matchesJsonSchemaInClasspath("JsonSchema.json"));
        logger.log(LogStatus.PASS, "Test Case Passed is testPostRequest");


    }

    @Test
    public static void checkUserDetails(){
        logger = extent.startTest("checkUserDetails");
        Response response =
                given().headers("Content-Type", ContentType.JSON, "Accept", ContentType.JSON).
                        when().get("https://fakestoreapi.com/users").
                        then().contentType(ContentType.JSON).extract().response();
        String users = response.jsonPath().getString("username");
        System.out.println(users);
        Assert.assertTrue(users.contains("david_r"));
        Assert.assertTrue(users.contains("donero"));
        Assert.assertTrue(users.contains("snyder"));
        logger.log(LogStatus.PASS, "Test Case Passed is checkUserDetails");
    }

    @Test
    public static void checkLatLongs(){
        logger = extent.startTest("checkLatLongs");
        Response response =
                given().headers("Content-Type", ContentType.JSON, "Accept", ContentType.JSON).
                        when().get("https://fakestoreapi.com/users").
                        then().contentType(ContentType.JSON).extract().response();
        ArrayList<JsonElement> jsonElement = response.path("address");

        //System.out.println(jsonElement.get(0).toString());
        for(int i=0;i<jsonElement.size();i++) {
            HashMap<String, JsonElement> map = new HashMap<>();
            map.put("Object", jsonElement.get(i));
            String jsonString = new JSONObject(map).get("Object").toString();
           int latStartIndex =  jsonString.lastIndexOf("lat=") + 4;
            int longStartIndex =  jsonString.lastIndexOf("long=") +5;
            int latEndIndex = latStartIndex+7;
            int longEndIndex = longStartIndex+7;
           String  lat = jsonString.substring(latStartIndex,latEndIndex);
            String lon = jsonString.substring(longStartIndex,longEndIndex);
            Assert.assertNotNull(lat);
            Assert.assertNotNull(lon);
        }
        logger.log(LogStatus.PASS, "Test Case Passed is checkLatLongs");
    }

    public static boolean isValidPassword(String password)
    {

        // Regex to check valid password.
        String regex = "^(?=.*[0-9])"
                + "(?=.*[a-z][A-Z])"
                + "(?=.*[@#$%^&+=])";

        // Compile the ReGex
        Pattern p = Pattern.compile(regex);

        // If the password is empty
        // return false
        if (password == null) {
            return false;
        }

        // Pattern class contains matcher() method
        // to find matching between given password
        // and regular expression.
        Matcher m = p.matcher(password);

        // Return if the password
        // matched the ReGex
        return m.matches();
    }
    @Test
    public static void checkPassword(){
        logger = extent.startTest("checkPassword");
        Response response =
                given().headers("Content-Type", ContentType.JSON, "Accept", ContentType.JSON).
                        when().get("https://fakestoreapi.com/users").
                        then().contentType(ContentType.JSON).extract().response();
        ArrayList<JsonElement> jsonElement = response.path("password");
        System.out.println(jsonElement.toString());
        //System.out.println(jsonElement.get(0).toString());
        for(int i=0;i<jsonElement.size();i++) {
            HashMap<String, JsonElement> map = new HashMap<>();
            map.put("Password", jsonElement.get(i));
            String jsonString = new JSONObject(map).get("Password").toString();
            System.out.println(jsonString);
            Assert.assertTrue(isValidPassword(jsonString));
            logger.log(LogStatus.FAIL, "Test Case checkPassword Status is failed");
        }
    }

    @Test
    public void validateCartJson()
    {
        logger = extent.startTest("validateCartJson");
        Response response =
                given().headers("Content-Type", ContentType.JSON, "Accept", ContentType.JSON).
                        when().get("https://fakestoreapi.com/carts").
                        then().contentType(ContentType.JSON).extract().response();
        response.then().assertThat().body(matchesJsonSchemaInClasspath("CartJsonSchema.json"));
        logger.log(LogStatus.PASS, "Test Case validateCartJson Status is pass");
    }

    @Test
    public void validateProducts()
    {
        logger = extent.startTest("validateProducts");
        Response response =
                given().headers("Content-Type", ContentType.JSON, "Accept", ContentType.JSON).
                        when().get("https://fakestoreapi.com/carts").
                        then().contentType(ContentType.JSON).extract().response();
        ArrayList<JsonElement> jsonElement = response.path("products");
        System.out.println(jsonElement.get(0));
        System.out.println(jsonElement);
        int noOfproducts = jsonElement.size();
        boolean areProductsPresent = false;
        if(noOfproducts>=1){
            areProductsPresent = true;
        }
        Assert.assertTrue(areProductsPresent);

        for(int i=0;i<jsonElement.size();i++) {
            HashMap<String, JsonElement> map = new HashMap<>();
            map.put("products", jsonElement.get(i));
            String jsonString = new JSONObject(map).get("products").toString();
            int quantityStartIndex =  jsonString.indexOf("quantity=") + 9;
            int productIdStartIndex =  jsonString.indexOf("productId=") +10;
            int quantityEndIndex = quantityStartIndex+1;
            int productIdEndIndex = productIdStartIndex+1;
            String  quantity = jsonString.substring(quantityStartIndex,quantityEndIndex);
            String productId = jsonString.substring(productIdStartIndex,productIdEndIndex);
            Assert.assertNotNull(quantity);
            Assert.assertNotNull(productId);
        }
        logger.log(LogStatus.PASS, "Test Case validateProducts Status is pass");
    }

    @AfterMethod
    public void getResult(ITestResult result){
        if(result.getStatus() == ITestResult.FAILURE){
            logger.log(LogStatus.FAIL, "Test Case Failed is "+result.getName());
            logger.log(LogStatus.FAIL, "Test Case Failed is "+result.getThrowable());
        }else if(result.getStatus() == ITestResult.SKIP){
            logger.log(LogStatus.SKIP, "Test Case Skipped is "+result.getName());
        }
        // ending test
        //endTest(logger) : It ends the current test and prepares to create HTML report
        extent.endTest(logger);
    }
    @AfterTest
    public void endReport(){
        extent.flush();
        extent.close();
    }
}




