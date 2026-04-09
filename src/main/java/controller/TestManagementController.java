package com.mycompany.mytestframework.controller;

import com.mycompany.mytestframework.entity.TestCase;
import com.mycompany.mytestframework.entity.TestResult;
import com.mycompany.mytestframework.entity.TestSuite;
import com.mycompany.mytestframework.repository.TestCaseRepository;
import com.mycompany.mytestframework.repository.TestResultRepository;
import com.mycompany.mytestframework.repository.TestSuiteRepository;
import com.mycompany.mytestframework.service.TestExecutionService;
import com.mycompany.mytestframework.service.ParallelExecutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/tests")
public class TestManagementController {

    @Autowired
    private TestSuiteRepository testSuiteRepository;

    @Autowired
    private TestCaseRepository testCaseRepository;

    @Autowired
    private TestResultRepository testResultRepository;

    @Autowired
    private TestExecutionService testExecutionService;

    @Autowired
    private ParallelExecutionService parallelExecutionService;

    // Create a new test suite
    @PostMapping("/suite")
    public TestSuite createTestSuite(@RequestBody TestSuite testSuite) {
        testSuite.setCreatedAt(LocalDateTime.now());
        testSuite.setStatus("ACTIVE");
        return testSuiteRepository.save(testSuite);
    }

    // Get all test suites
    @GetMapping("/suites")
    public List<TestSuite> getAllTestSuites() {
        return testSuiteRepository.findAll();
    }

    // Get test suite by ID
    @GetMapping("/suite/{id}")
    public TestSuite getTestSuite(@PathVariable Long id) {
        return testSuiteRepository.findById(id).orElse(null);
    }

    // GET /api/tests/{id}  — get a single test case by ID
    @GetMapping("/{id}")
    public TestCase getTestCaseById(@PathVariable Long id) {
        return testCaseRepository.findById(id).orElse(null);
    }

    // POST /api/tests/integrate  — create and immediately validate a test case
    @PostMapping("/integrate")
    public TestResult integrateAndExecute(@RequestBody TestCase testCase) {
        TestCase saved = testCaseRepository.save(testCase);
        return testExecutionService.executeTest(saved);
    }

    // Create a new test case
    @PostMapping("/case")
    public TestCase createTestCase(@RequestBody TestCase testCase) {
        return testCaseRepository.save(testCase);
    }

    // 👇 NEW: Get all test cases
    @GetMapping("/cases")
    public List<TestCase> getAllTestCases() {
        return testCaseRepository.findAll();
    }

    // Get test cases by suite
    @GetMapping("/suite/{suiteId}/cases")
    public List<TestCase> getTestCasesBySuite(@PathVariable Long suiteId) {
        return testCaseRepository.findByTestSuiteId(suiteId);
    }

    // 👇 NEW: Delete a test case by ID
    @DeleteMapping("/case/{id}")
    public String deleteTestCase(@PathVariable Long id) {
        testCaseRepository.deleteById(id);
        return "Test case " + id + " deleted successfully";
    }

    // 👇 NEW: Delete all test cases (use carefully!)
    @DeleteMapping("/cases")
    public String deleteAllTestCases() {
        testCaseRepository.deleteAll();
        return "All test cases deleted successfully";
    }

    // Execute a test case
    @PostMapping("/case/{caseId}/execute")
    public TestResult executeTestCase(@PathVariable Long caseId) {
        TestCase testCase = testCaseRepository.findById(caseId)
                .orElseThrow(() -> new RuntimeException("Test case not found"));
        return testExecutionService.executeTest(testCase);
    }

    // Execute all tests in a suite
    @PostMapping("/suite/{suiteId}/execute")
    public List<TestResult> executeTestSuite(@PathVariable Long suiteId) {
        List<TestCase> testCases = testCaseRepository.findByTestSuiteId(suiteId);
        return testCases.stream()
                .map(testCase -> testExecutionService.executeTest(testCase))
                .toList();
    }

    // Execute all tests in a suite in parallel
    @PostMapping("/suite/{suiteId}/execute/parallel")
    public List<TestResult> executeTestSuiteParallel(
            @PathVariable Long suiteId,
            @RequestParam(defaultValue = "3") int threads) {

        System.out.println("🔁 Received request to run suite " + suiteId + " with " + threads + " parallel threads");

        List<TestCase> testCases = testCaseRepository.findByTestSuiteId(suiteId);
        List<Long> testCaseIds = new ArrayList<>();

        for (TestCase tc : testCases) {
            testCaseIds.add(tc.getId());
        }

        return parallelExecutionService.executeTestsInParallel(testCaseIds, threads);
    }

    // Get results for a test case
    @GetMapping("/case/{caseId}/results")
    public List<TestResult> getTestResults(@PathVariable Long caseId) {
        return testResultRepository.findByTestCaseIdOrderByExecutionTimeDesc(caseId);
    }

    // Get all results
    @GetMapping("/results")
    public List<TestResult> getAllResults() {
        return testResultRepository.findAll();
    }
    // Delete all test results
    @DeleteMapping("/results")
    public String deleteAllTestResults() {
        testResultRepository.deleteAll();
        return "All test results deleted successfully";
    }

    // GET /api/tests/suites/view — styled HTML page for all test suites
    @GetMapping(value = "/suites/view", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> getSuitesHtml() {
        List<TestSuite> suites = testSuiteRepository.findAll();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        long activeCount = suites.stream().filter(s -> "ACTIVE".equals(s.getStatus())).count();

        StringBuilder rows = new StringBuilder();
        if (suites.isEmpty()) {
            rows.append("<tr><td colspan='6' style='text-align:center;color:#94a3b8;padding:24px'>No test suites found. Create one via POST /api/tests/suite</td></tr>");
        } else {
            for (TestSuite s : suites) {
                int caseCount = testCaseRepository.findByTestSuiteId(s.getId()).size();
                String badgeClass = "ACTIVE".equals(s.getStatus()) ? "badge-active" : "badge-inactive";
                rows.append("<tr>")
                    .append("<td style='font-weight:700;color:#3b82f6'>").append(s.getId()).append("</td>")
                    .append("<td style='font-weight:600;color:#1e293b'>").append(s.getName()).append("</td>")
                    .append("<td style='color:#64748b'>").append(s.getDescription() != null ? s.getDescription() : "-").append("</td>")
                    .append("<td><span class='badge ").append(badgeClass).append("'>").append(s.getStatus()).append("</span></td>")
                    .append("<td style='font-weight:600;color:#8b5cf6'>").append(caseCount).append("</td>")
                    .append("<td style='color:#64748b;font-size:0.88em'>").append(s.getCreatedAt() != null ? s.getCreatedAt().format(fmt) : "-").append("</td>")
                    .append("</tr>");
            }
        }

        String css = "* { margin:0; padding:0; box-sizing:border-box; font-family:'Segoe UI',sans-serif; }" +
            "body { background:linear-gradient(135deg,#e0f2fe 0%,#f0fdf4 50%,#fef9c3 100%); min-height:100vh; padding:24px; }" +
            ".container { max-width:1200px; margin:0 auto; }" +
            ".topbar { display:flex; align-items:center; gap:16px; background:white; border-radius:16px; padding:20px 28px; margin-bottom:24px; box-shadow:0 4px 20px rgba(0,0,0,0.08); border-left:6px solid #22c55e; }" +
            ".topbar h1 { color:#1e293b; font-size:1.8em; font-weight:700; }" +
            ".topbar p { color:#64748b; font-size:0.92em; margin-top:3px; }" +
            ".stats-grid { display:grid; grid-template-columns:repeat(3,1fr); gap:16px; margin-bottom:24px; }" +
            ".stat-card { background:white; border-radius:14px; padding:20px; text-align:center; box-shadow:0 4px 16px rgba(0,0,0,0.07); position:relative; overflow:hidden; transition:transform 0.2s; }" +
            ".stat-card:hover { transform:translateY(-3px); }" +
            ".stat-card::before { content:''; position:absolute; top:0; left:0; right:0; height:4px; }" +
            ".sc1::before{background:#3b82f6} .sc2::before{background:#22c55e} .sc3::before{background:#8b5cf6}" +
            ".stat-icon { font-size:1.8em; margin-bottom:6px; }" +
            ".stat-value { font-size:2.2em; font-weight:800; margin:4px 0; }" +
            ".sc1 .stat-value{color:#3b82f6} .sc2 .stat-value{color:#22c55e} .sc3 .stat-value{color:#8b5cf6}" +
            ".stat-label { color:#64748b; font-size:0.82em; font-weight:600; text-transform:uppercase; letter-spacing:0.5px; }" +
            ".card { background:white; border-radius:14px; padding:24px; box-shadow:0 4px 16px rgba(0,0,0,0.07); }" +
            ".card-title { display:flex; align-items:center; gap:10px; color:#1e293b; font-size:1.1em; font-weight:700; margin-bottom:16px; padding-bottom:12px; border-bottom:2px solid #f1f5f9; }" +
            "table { width:100%; border-collapse:collapse; }" +
            "th { background:#f8fafc; color:#475569; padding:12px 14px; text-align:left; font-size:0.82em; font-weight:700; text-transform:uppercase; letter-spacing:0.5px; border-bottom:2px solid #e2e8f0; }" +
            "td { padding:13px 14px; border-bottom:1px solid #f1f5f9; }" +
            "tr:hover td { background:#f8fafc; }" +
            ".badge { display:inline-block; padding:4px 12px; border-radius:20px; font-size:0.82em; font-weight:700; }" +
            ".badge-active { background:#dcfce7; color:#166534; } .badge-inactive { background:#fee2e2; color:#991b1b; }" +
            ".footer { text-align:center; color:#64748b; font-size:0.85em; margin-top:20px; }";

        String html = "<!DOCTYPE html><html lang='en'><head><meta charset='UTF-8'>" +
            "<meta name='viewport' content='width=device-width,initial-scale=1'>" +
            "<title>Test Suites</title><style>" + css + "</style></head><body>" +
            "<div class='container'>" +
            "<div class='topbar'>" +
            "<svg width='48' height='48' viewBox='0 0 48 48' fill='none'><rect width='48' height='48' rx='12' fill='#f0fdf4'/>" +
            "<rect x='10' y='12' width='28' height='6' rx='3' fill='#22c55e'/>" +
            "<rect x='10' y='22' width='28' height='6' rx='3' fill='#86efac'/>" +
            "<rect x='10' y='32' width='18' height='6' rx='3' fill='#bbf7d0'/></svg>" +
            "<div><h1>Test Suites</h1><p>Generated: " + java.time.LocalDateTime.now().format(fmt) + " &nbsp;|&nbsp; Regression Test Suite Framework</p></div></div>" +
            "<div class='stats-grid'>" +
            "<div class='stat-card sc1'><div class='stat-icon'>🗂️</div><div class='stat-value'>" + suites.size() + "</div><div class='stat-label'>Total Suites</div></div>" +
            "<div class='stat-card sc2'><div class='stat-icon'>✅</div><div class='stat-value'>" + activeCount + "</div><div class='stat-label'>Active</div></div>" +
            "<div class='stat-card sc3'><div class='stat-icon'>🧪</div><div class='stat-value'>" + testCaseRepository.count() + "</div><div class='stat-label'>Total Test Cases</div></div>" +
            "</div>" +
            "<div class='card'><div class='card-title'>" +
            "<svg width='18' height='18' viewBox='0 0 18 18' fill='#22c55e'><path d='M2 3h14v2H2zm0 5h14v2H2zm0 5h10v2H2z'/></svg>All Test Suites</div>" +
            "<div style='overflow-x:auto'><table><thead><tr><th>ID</th><th>Name</th><th>Description</th><th>Status</th><th>Test Cases</th><th>Created At</th></tr></thead>" +
            "<tbody>" + rows + "</tbody></table></div></div>" +
            "<div class='footer'>🚀 Automated Regression Test Suite Framework &nbsp;|&nbsp; Spring Boot + Selenium + REST-Assured &nbsp;|&nbsp; © 2026</div>" +
            "</div></body></html>";

        return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(html);
    }
}
