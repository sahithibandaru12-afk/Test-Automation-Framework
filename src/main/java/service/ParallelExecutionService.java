package com.mycompany.mytestframework.service;

import com.mycompany.mytestframework.entity.TestCase;
import com.mycompany.mytestframework.entity.TestResult;
import com.mycompany.mytestframework.repository.TestCaseRepository;
import com.mycompany.mytestframework.repository.TestResultRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
public class ParallelExecutionService {

    @Autowired
    private TestCaseRepository testCaseRepository;

    @Autowired
    private TestResultRepository testResultRepository;

    @Autowired
    private TestExecutionService testExecutionService;

    public List<TestResult> executeTestsInParallel(List<Long> testCaseIds, int threadCount) {
        List<TestResult> results = new ArrayList<>();

        // Create a thread pool with specified number of threads
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        System.out.println("==========================================");
        System.out.println("🚀 STARTING PARALLEL EXECUTION");
        System.out.println("📊 Thread count: " + threadCount);
        System.out.println("📋 Tests to run: " + testCaseIds.size());
        System.out.println("==========================================");

        long startTime = System.currentTimeMillis();

        // Submit each test to the thread pool
        for (Long id : testCaseIds) {
            executor.submit(() -> {
                long testStartTime = System.currentTimeMillis();

                // Get the test case
                TestCase testCase = testCaseRepository.findById(id).get();

                // Run the test
                TestResult result = testExecutionService.executeTest(testCase);

                long testEndTime = System.currentTimeMillis();
                long testDuration = testEndTime - testStartTime;

                // Add to results list (thread-safe)
                synchronized(results) {
                    results.add(result);
                    System.out.println("✅ Test ID " + id + " completed in " + testDuration + "ms | Status: " + result.getStatus());
                }
            });
        }

        // Shutdown the executor and wait for all tests to complete
        executor.shutdown();
        try {
            executor.awaitTermination(30, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            System.err.println("❌ Parallel execution interrupted: " + e.getMessage());
        }

        long endTime = System.currentTimeMillis();
        long totalDuration = endTime - startTime;

        System.out.println("==========================================");
        System.out.println("🎉 PARALLEL EXECUTION COMPLETED");
        System.out.println("⏱️  Total time: " + totalDuration + "ms");
        System.out.println("✅ Tests passed: " + results.stream().filter(r -> "PASS".equals(r.getStatus())).count());
        System.out.println("❌ Tests failed: " + results.stream().filter(r -> "FAIL".equals(r.getStatus())).count());
        System.out.println("==========================================");

        return results;
    }
}