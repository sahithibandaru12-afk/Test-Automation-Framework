package com.mycompany.mytestframework.service;

import com.mycompany.mytestframework.entity.TestResult;
import com.mycompany.mytestframework.repository.TestResultRepository;
import com.mycompany.mytestframework.repository.TestCaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    @Autowired
    private TestResultRepository testResultRepository;

    @Autowired
    private TestCaseRepository testCaseRepository;

    public Map<String, Object> getOverallStats() {
        List<TestResult> all = testResultRepository.findAll();
        long total = all.size();
        long passed = all.stream().filter(r -> "PASS".equals(r.getStatus())).count();
        long failed = all.stream().filter(r -> "FAIL".equals(r.getStatus())).count();
        double passRate = total > 0 ? Math.round((passed * 100.0 / total) * 10.0) / 10.0 : 0.0;
        OptionalDouble avgDuration = all.stream()
                .filter(r -> r.getDurationMs() != null)
                .mapToLong(TestResult::getDurationMs)
                .average();

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalTests", total);
        stats.put("passed", passed);
        stats.put("failed", failed);
        stats.put("passRate", passRate);
        stats.put("avgDurationMs", avgDuration.isPresent() ? Math.round(avgDuration.getAsDouble()) : 0);
        stats.put("totalTestCases", testCaseRepository.count());
        return stats;
    }

    public Map<String, Object> getTrends(int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        List<TestResult> all = testResultRepository.findAll().stream()
                .filter(r -> r.getExecutionTime() != null && r.getExecutionTime().isAfter(since))
                .collect(Collectors.toList());

        // Group by date
        Map<LocalDate, List<TestResult>> byDate = all.stream()
                .collect(Collectors.groupingBy(r -> r.getExecutionTime().toLocalDate()));

        List<Map<String, Object>> trends = new ArrayList<>();
        byDate.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    List<TestResult> dayResults = entry.getValue();
                    long total = dayResults.size();
                    long passed = dayResults.stream().filter(r -> "PASS".equals(r.getStatus())).count();
                    long failed = total - passed;
                    double rate = total > 0 ? Math.round((passed * 100.0 / total) * 10.0) / 10.0 : 0.0;

                    Map<String, Object> day = new LinkedHashMap<>();
                    day.put("date", entry.getKey().toString());
                    day.put("total", total);
                    day.put("passed", passed);
                    day.put("failed", failed);
                    day.put("passRate", rate);
                    trends.add(day);
                });

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("days", days);
        result.put("trends", trends);
        return result;
    }

    public Map<String, Object> getSuiteAnalytics(Long suiteId) {
        List<TestResult> results = testResultRepository.findAll().stream()
                .filter(r -> r.getTestCase() != null
                        && r.getTestCase().getTestSuite() != null
                        && suiteId.equals(r.getTestCase().getTestSuite().getId()))
                .collect(Collectors.toList());

        long total = results.size();
        long passed = results.stream().filter(r -> "PASS".equals(r.getStatus())).count();
        long failed = total - passed;
        double passRate = total > 0 ? Math.round((passed * 100.0 / total) * 10.0) / 10.0 : 0.0;

        Map<String, Object> analytics = new LinkedHashMap<>();
        analytics.put("suiteId", suiteId);
        analytics.put("totalRuns", total);
        analytics.put("passed", passed);
        analytics.put("failed", failed);
        analytics.put("passRate", passRate);
        return analytics;
    }

    public List<Map<String, Object>> getFlakyTests() {
        List<TestResult> all = testResultRepository.findAll();

        // Group results by test case id
        Map<Long, List<TestResult>> byTestCase = all.stream()
                .filter(r -> r.getTestCase() != null)
                .collect(Collectors.groupingBy(r -> r.getTestCase().getId()));

        List<Map<String, Object>> flaky = new ArrayList<>();

        byTestCase.forEach((testCaseId, results) -> {
            if (results.size() < 2) return;
            long passed = results.stream().filter(r -> "PASS".equals(r.getStatus())).count();
            long failed = results.size() - passed;
            // Flaky = has both passes and failures
            if (passed > 0 && failed > 0) {
                double flakinessRate = Math.round((failed * 100.0 / results.size()) * 10.0) / 10.0;
                String testName = results.get(0).getTestCase().getName();

                Map<String, Object> entry = new LinkedHashMap<>();
                entry.put("testCaseId", testCaseId);
                entry.put("testName", testName);
                entry.put("totalRuns", results.size());
                entry.put("passed", passed);
                entry.put("failed", failed);
                entry.put("flakinessRate", flakinessRate);
                flaky.add(entry);
            }
        });

        flaky.sort((a, b) -> Double.compare(
                (double) b.get("flakinessRate"), (double) a.get("flakinessRate")));
        return flaky;
    }
}
