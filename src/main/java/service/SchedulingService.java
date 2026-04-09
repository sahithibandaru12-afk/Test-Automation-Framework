package com.mycompany.mytestframework.service;

import com.mycompany.mytestframework.entity.TestCase;
import com.mycompany.mytestframework.entity.TestResult;
import com.mycompany.mytestframework.repository.TestCaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class SchedulingService {

    @Autowired
    private TestCaseRepository testCaseRepository;

    @Autowired
    private TestExecutionService testExecutionService;

    @Autowired
    private ParallelExecutionService parallelExecutionService;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Run every day at 9:00 AM — executes all test cases sequentially
    @Scheduled(cron = "0 0 9 * * *")
    public void runDailyTests() {
        System.out.println("==========================================");
        System.out.println("⏰ DAILY SCHEDULED TESTS STARTED at " + LocalDateTime.now().format(formatter));
        System.out.println("==========================================");
        List<TestCase> allTests = testCaseRepository.findAll();
        if (allTests.isEmpty()) {
            System.out.println("⚠️  No test cases found for daily run.");
            return;
        }
        long passed = 0, failed = 0;
        for (TestCase tc : allTests) {
            TestResult result = testExecutionService.executeTest(tc);
            if ("PASS".equals(result.getStatus())) passed++;
            else failed++;
        }
        System.out.println("✅ Daily run complete — Passed: " + passed + " | Failed: " + failed);
    }

    // Run every hour — parallel health check across all test cases
    @Scheduled(cron = "0 0 * * * *")
    public void runHourlyHealthCheck() {
        System.out.println("🕐 HOURLY HEALTH CHECK at " + LocalDateTime.now().format(formatter));
        List<TestCase> allTests = testCaseRepository.findAll();
        if (allTests.isEmpty()) {
            System.out.println("⚠️  No test cases found for hourly check.");
            return;
        }
        List<Long> ids = allTests.stream().map(TestCase::getId).toList();
        List<TestResult> results = parallelExecutionService.executeTestsInParallel(ids, 3);
        long passed = results.stream().filter(r -> "PASS".equals(r.getStatus())).count();
        System.out.println("🕐 Hourly check done — Passed: " + passed + "/" + results.size());
    }

    // Run every Monday at 8:00 AM — full parallel regression suite
    @Scheduled(cron = "0 0 8 * * MON")
    public void runWeeklyRegressionTests() {
        System.out.println("==========================================");
        System.out.println("📅 WEEKLY REGRESSION TESTS STARTED at " + LocalDateTime.now().format(formatter));
        System.out.println("==========================================");
        List<TestCase> allTests = testCaseRepository.findAll();
        if (allTests.isEmpty()) {
            System.out.println("⚠️  No test cases found for weekly regression.");
            return;
        }
        List<Long> ids = allTests.stream().map(TestCase::getId).toList();
        List<TestResult> results = parallelExecutionService.executeTestsInParallel(ids, 5);
        long passed = results.stream().filter(r -> "PASS".equals(r.getStatus())).count();
        long failed = results.size() - passed;
        System.out.println("📅 Weekly regression done — Passed: " + passed + " | Failed: " + failed);
    }
}
