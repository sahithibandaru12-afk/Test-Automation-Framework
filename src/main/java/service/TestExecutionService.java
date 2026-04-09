package com.mycompany.mytestframework.service;

import java.io.File;
import java.nio.file.Files;
import java.time.LocalDateTime;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.mycompany.mytestframework.entity.TestCase;
import com.mycompany.mytestframework.entity.TestResult;
import com.mycompany.mytestframework.repository.TestResultRepository;

@Service
public class TestExecutionService {

    @Autowired
    private TestResultRepository testResultRepository;

    @Autowired
    private EmailService emailService;

    @Value("${alert.email:your-email@gmail.com}")
    private String alertEmail;

    public TestResult executeTest(TestCase testCase) {
        TestResult result = new TestResult();
        result.setTestCase(testCase);
        result.setExecutionTime(LocalDateTime.now());

        long startTime = System.currentTimeMillis();

        try {
            if ("API".equals(testCase.getType())) {
                // For API tests, we need to create a runAPITestWithPath method
                // For now, use existing runAPITest
                boolean testPassed = runAPITest(testCase);
                result.setStatus(testPassed ? "PASS" : "FAIL");

                // Send alert if test failed
                if (!testPassed) {
                    sendFailureAlert(testCase, result);
                }
            } else if ("UI".equals(testCase.getType())) {
                Object[] testResult = runSeleniumTestWithPath(testCase);
                boolean testPassed = (boolean) testResult[0];
                String screenshotPath = (String) testResult[1];

                result.setStatus(testPassed ? "PASS" : "FAIL");
                result.setScreenshotPath(screenshotPath);

                // Send alert if test failed
                if (!testPassed) {
                    sendFailureAlert(testCase, result);
                }
            }
        } catch (Exception e) {
            result.setStatus("FAIL");
            result.setErrorMessage(e.getMessage());
            sendFailureAlert(testCase, result); // Alert on exception too
            e.printStackTrace();
        }

        long endTime = System.currentTimeMillis();
        result.setDurationMs(endTime - startTime);

        return testResultRepository.save(result);
    }

    private Object[] runSeleniumTestWithPath(TestCase testCase) {
    WebDriver driver = null;
    String screenshotPath = null;
    boolean testPassed = false;

    try {
        System.setProperty("webdriver.chrome.driver", "C:\\chromedriver\\chromedriver-win64\\chromedriver.exe");

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--disable-blink-features=AutomationControlled");

        driver = new ChromeDriver(options);

        System.out.println("🌐 Testing URL: " + testCase.getUrl());
        driver.get(testCase.getUrl());

        String pageTitle = driver.getTitle();
        System.out.println("📄 Page title: " + pageTitle);
        System.out.println("✅ Expected result: " + testCase.getExpectedResult());

        testPassed = pageTitle != null && pageTitle.contains(testCase.getExpectedResult());
        System.out.println("📊 Test passed: " + testPassed);

        // 👇 THIS IS THE KEY CHANGE - Show failure page before screenshot
        if (!testPassed) {
            System.out.println("📄 Showing custom failure page...");
            showFailurePage(driver, testCase);  // Navigate to failure page
            Thread.sleep(1000); // Wait for page to load
            screenshotPath = takeScreenshot(driver, testCase); // Take screenshot of failure page
        }

    } catch (Exception e) {
        System.out.println("❌ Error during Selenium test: " + e.getMessage());
        // Try to show failure page even on exception
        try {
            if (driver != null) {
                showFailurePage(driver, testCase);
                Thread.sleep(1000);
            }
        } catch (Exception ex) {
            // Ignore
        }
        screenshotPath = takeScreenshot(driver, testCase);
        e.printStackTrace();
    } finally {
        if (driver != null) {
            driver.quit();
        }
    }

    return new Object[]{testPassed, screenshotPath};
}
    private String takeScreenshot(WebDriver driver, TestCase testCase) {
        try {
            if (driver != null) {
                System.out.println("📸 Taking screenshot for test ID: " + testCase.getId());

                File screenshotDir = new File("screenshots");
                if (!screenshotDir.exists()) {
                    screenshotDir.mkdirs();
                    System.out.println("📁 Created screenshots directory");
                }

                File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
                String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
                String screenshotPath = "screenshots/failure_" + testCase.getId() + "_" + timestamp + ".png";

                Files.copy(screenshot.toPath(), new File(screenshotPath).toPath());
                System.out.println("📸 Screenshot saved to: " + screenshotPath);
                return screenshotPath;
            } else {
                System.out.println("❌ Driver is null, cannot take screenshot");
            }
        } catch (Exception e) {
            System.out.println("❌ Failed to capture screenshot: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    private boolean runAPITest(TestCase testCase) {
        try {
            System.out.println("🔵 Testing API: " + testCase.getUrl());
            System.out.println("🔵 Method: " + testCase.getMethod());

            io.restassured.response.Response response = null;

            if ("GET".equalsIgnoreCase(testCase.getMethod())) {
                response = io.restassured.RestAssured.get(testCase.getUrl());
            } else if ("POST".equalsIgnoreCase(testCase.getMethod())) {
                response = io.restassured.RestAssured
                        .given()
                        .contentType("application/json")
                        .body(testCase.getRequestBody() != null ? testCase.getRequestBody() : "{}")
                        .post(testCase.getUrl());
            }

            int statusCode = response.getStatusCode();
            String responseBody = response.getBody().asString();

            System.out.println("🔵 Status Code: " + statusCode);
            System.out.println("🔵 Response Body: " + responseBody);
            System.out.println("✅ Expected result: " + testCase.getExpectedResult());

            boolean testPassed = false;

            try {
                int expectedStatusCode = Integer.parseInt(testCase.getExpectedResult());
                testPassed = (statusCode == expectedStatusCode);
                System.out.println("📊 Comparing status code: " + statusCode + " == " + expectedStatusCode + " ? " + testPassed);
            } catch (NumberFormatException e) {
                testPassed = responseBody.contains(testCase.getExpectedResult());
                System.out.println("📊 Searching for '" + testCase.getExpectedResult() + "' in response: " + testPassed);
            }

            return testPassed;

        } catch (Exception e) {
            System.out.println("❌ Error during API test: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Method to send alert on failure
    private void sendFailureAlert(TestCase testCase, TestResult result) {
        String subject = "🚨 TEST FAILURE ALERT: " + testCase.getName();

        StringBuilder content = new StringBuilder();
        content.append("Test Failure Details:\n");
        content.append("========================\n");
        content.append("Test ID: ").append(testCase.getId()).append("\n");
        content.append("Test Name: ").append(testCase.getName()).append("\n");
        content.append("Test Type: ").append(testCase.getType()).append("\n");
        content.append("URL: ").append(testCase.getUrl()).append("\n");
        content.append("Expected: ").append(testCase.getExpectedResult()).append("\n");
        content.append("Status: ").append(result.getStatus()).append("\n");
        content.append("Duration: ").append(result.getDurationMs()).append("ms\n");

        if (result.getErrorMessage() != null) {
            content.append("Error: ").append(result.getErrorMessage()).append("\n");
        }

        if (result.getScreenshotPath() != null) {
            content.append("Screenshot: ").append(result.getScreenshotPath()).append("\n");
        }

        content.append("Time: ").append(result.getExecutionTime()).append("\n");

        emailService.sendAlertEmail(alertEmail, subject, content.toString());
    }
    private void showFailurePage(WebDriver driver, TestCase testCase) {
    // Create HTML content using string concatenation (no text blocks)
    String failureHtml = "<!DOCTYPE html>\n" +
        "<html>\n" +
        "<head>\n" +
        "<title>TEST FAILED</title>\n" +
        "<style>\n" +
        "body {\n" +
        "    font-family: Arial, sans-serif;\n" +
        "    text-align: center;\n" +
        "    padding: 50px;\n" +
        "    background: linear-gradient(135deg, #ff6b6b 0%, #c92a2a 100%);\n" +
        "    color: white;\n" +
        "}\n" +
        ".container {\n" +
        "    max-width: 800px;\n" +
        "    margin: 0 auto;\n" +
        "    background: rgba(0,0,0,0.7);\n" +
        "    border-radius: 20px;\n" +
        "    padding: 40px;\n" +
        "}\n" +
        ".fail-icon {\n" +
        "    font-size: 80px;\n" +
        "    margin-bottom: 20px;\n" +
        "}\n" +
        "h1 {\n" +
        "    font-size: 48px;\n" +
        "    margin-bottom: 20px;\n" +
        "}\n" +
        ".details {\n" +
        "    text-align: left;\n" +
        "    background: white;\n" +
        "    color: #333;\n" +
        "    padding: 20px;\n" +
        "    border-radius: 10px;\n" +
        "    margin-top: 30px;\n" +
        "    font-family: monospace;\n" +
        "}\n" +
        ".details p {\n" +
        "    margin: 10px 0;\n" +
        "}\n" +
        ".label {\n" +
        "    font-weight: bold;\n" +
        "    color: #c92a2a;\n" +
        "}\n" +
        "hr {\n" +
        "    border: 1px solid #ddd;\n" +
        "}\n" +
        "</style>\n" +
        "</head>\n" +
        "<body>\n" +
        "<div class='container'>\n" +
        "<div class='fail-icon'>❌</div>\n" +
        "<h1>TEST FAILED!</h1>\n" +
        "<div class='details'>\n" +
        "<p><span class='label'>Test ID:</span> " + testCase.getId() + "</p>\n" +
        "<p><span class='label'>Test Name:</span> " + escapeHtml(testCase.getName()) + "</p>\n" +
        "<p><span class='label'>Test Type:</span> " + testCase.getType() + "</p>\n" +
        "<p><span class='label'>URL Tested:</span> " + escapeHtml(testCase.getUrl()) + "</p>\n" +
        "<p><span class='label'>Expected Result:</span> " + escapeHtml(testCase.getExpectedResult()) + "</p>\n" +
        "<hr>\n" +
        "<p><span class='label'>Status:</span> FAIL</p>\n" +
        "<p><span class='label'>Time:</span> " + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "</p>\n" +
        "<p><span class='label'>Message:</span> Expected " + escapeHtml(testCase.getExpectedResult()) + " was not found</p>\n" +
        "</div>\n" +
        "<p style='margin-top: 20px;'>📸 Screenshot captured automatically at failure time</p>\n" +
        "</div>\n" +
        "</body>\n" +
        "</html>";
    
    // Write the HTML to a temporary file
    try {
        String tempFilePath = "failure_page_" + testCase.getId() + ".html";
        java.nio.file.Files.write(java.nio.file.Paths.get(tempFilePath), failureHtml.getBytes());
        
        // Navigate to the failure page
        String absolutePath = new File(tempFilePath).getAbsolutePath();
        String failureUrl = "file:///" + absolutePath.replace("\\", "/");
        driver.get(failureUrl);
        Thread.sleep(1000); // Wait for page to load
        
        // Delete the temp file after use
        new File(tempFilePath).deleteOnExit();
        
    } catch (Exception e) {
        System.out.println("❌ Failed to create failure page: " + e.getMessage());
        e.printStackTrace();
    }
}

// Helper method to escape HTML special characters
private String escapeHtml(String text) {
    if (text == null) return "";
    return text.replace("&", "&amp;")
               .replace("<", "&lt;")
               .replace(">", "&gt;")
               .replace("\"", "&quot;")
               .replace("'", "&#39;");
}
}