package com.mycompany.mytestframework.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "test_results")
public class TestResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime executionTime;
    private String status; // "PASS", "FAIL", "ERROR"
    private Long durationMs;
    private String errorMessage;
    private String screenshotPath; // for UI test failures

    @ManyToOne
    @JoinColumn(name = "test_case_id")
    @JsonIgnore
    private TestCase testCase;

    // Constructors
    public TestResult() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getExecutionTime() { return executionTime; }
    public void setExecutionTime(LocalDateTime executionTime) { this.executionTime = executionTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getDurationMs() { return durationMs; }
    public void setDurationMs(Long durationMs) { this.durationMs = durationMs; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public String getScreenshotPath() { return screenshotPath; }
    public void setScreenshotPath(String screenshotPath) { this.screenshotPath = screenshotPath; }

    public TestCase getTestCase() { return testCase; }
    public void setTestCase(TestCase testCase) { this.testCase = testCase; }

    // Helper methods for reports
    public String getTestCaseName() {
        return testCase != null ? testCase.getName() : "Unknown Test";
    }

    public String getTestCaseType() {
        return testCase != null ? testCase.getType() : "Unknown";
    }
}