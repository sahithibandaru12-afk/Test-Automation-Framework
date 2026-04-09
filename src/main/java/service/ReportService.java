package com.mycompany.mytestframework.service;

import com.mycompany.mytestframework.entity.TestResult;
import com.mycompany.mytestframework.repository.TestResultRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ReportService {

    @Autowired
    private TestResultRepository testResultRepository;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private String sharedStyle() {
        return "* { margin:0; padding:0; box-sizing:border-box; font-family:'Segoe UI',sans-serif; }" +
            "body { background: linear-gradient(135deg,#e0f2fe 0%,#f0fdf4 50%,#fef9c3 100%); min-height:100vh; padding:24px; }" +
            ".container { max-width:1200px; margin:0 auto; }" +
            ".topbar { display:flex; align-items:center; gap:16px; background:white; border-radius:16px; padding:20px 28px; margin-bottom:24px; box-shadow:0 4px 20px rgba(0,0,0,0.08); border-left:6px solid #3b82f6; }" +
            ".topbar svg { flex-shrink:0; }" +
            ".topbar h1 { color:#1e293b; font-size:1.8em; font-weight:700; }" +
            ".topbar p { color:#64748b; font-size:0.92em; margin-top:3px; }" +
            ".stats-grid { display:grid; grid-template-columns:repeat(auto-fit,minmax(200px,1fr)); gap:16px; margin-bottom:24px; }" +
            ".stat-card { background:white; border-radius:14px; padding:22px 20px; text-align:center; box-shadow:0 4px 16px rgba(0,0,0,0.07); transition:transform 0.2s,box-shadow 0.2s; position:relative; overflow:hidden; }" +
            ".stat-card:hover { transform:translateY(-4px); box-shadow:0 8px 24px rgba(0,0,0,0.12); }" +
            ".stat-card::before { content:''; position:absolute; top:0; left:0; right:0; height:4px; }" +
            ".stat-card.total::before { background:#3b82f6; } .stat-card.passed::before { background:#22c55e; }" +
            ".stat-card.failed::before { background:#ef4444; } .stat-card.rate::before { background:#f59e0b; }" +
            ".stat-card.avg::before { background:#8b5cf6; } .stat-card.cases::before { background:#06b6d4; }" +
            ".stat-icon { font-size:2em; margin-bottom:8px; }" +
            ".stat-value { font-size:2.2em; font-weight:800; margin:4px 0; }" +
            ".stat-card.total .stat-value { color:#3b82f6; } .stat-card.passed .stat-value { color:#22c55e; }" +
            ".stat-card.failed .stat-value { color:#ef4444; } .stat-card.rate .stat-value { color:#f59e0b; }" +
            ".stat-card.avg .stat-value { color:#8b5cf6; } .stat-card.cases .stat-value { color:#06b6d4; }" +
            ".stat-label { color:#64748b; font-size:0.88em; font-weight:500; text-transform:uppercase; letter-spacing:0.5px; }" +
            ".card { background:white; border-radius:14px; padding:24px; margin-bottom:20px; box-shadow:0 4px 16px rgba(0,0,0,0.07); }" +
            ".card-title { display:flex; align-items:center; gap:10px; color:#1e293b; font-size:1.15em; font-weight:700; margin-bottom:18px; padding-bottom:12px; border-bottom:2px solid #f1f5f9; }" +
            "table { width:100%; border-collapse:collapse; }" +
            "th { background:#f8fafc; color:#475569; padding:12px 14px; text-align:left; font-size:0.82em; font-weight:700; text-transform:uppercase; letter-spacing:0.5px; border-bottom:2px solid #e2e8f0; }" +
            "td { padding:12px 14px; border-bottom:1px solid #f1f5f9; color:#334155; font-size:0.93em; }" +
            "tr:hover td { background:#f8fafc; }" +
            ".badge { display:inline-block; padding:4px 12px; border-radius:20px; font-size:0.82em; font-weight:700; }" +
            ".badge-pass { background:#dcfce7; color:#166534; } .badge-fail { background:#fee2e2; color:#991b1b; }" +
            ".badge-api { background:#dbeafe; color:#1e40af; } .badge-ui { background:#ede9fe; color:#5b21b6; }" +
            ".badge-active { background:#dcfce7; color:#166534; } .badge-inactive { background:#fee2e2; color:#991b1b; }" +
            ".footer { text-align:center; color:#64748b; font-size:0.85em; margin-top:20px; padding:12px; }";
    }

    public String generateHtmlReport() {
        List<TestResult> results = testResultRepository.findAll();
        long totalTests = results.size();
        long passedTests = results.stream().filter(r -> "PASS".equals(r.getStatus())).count();
        long failedTests = results.stream().filter(r -> "FAIL".equals(r.getStatus())).count();
        double passRate = totalTests > 0 ? Math.round((passedTests * 100.0 / totalTests) * 10.0) / 10.0 : 0;

        StringBuilder rows = new StringBuilder();
        for (TestResult result : results) {
            String testName = result.getTestCase() != null ? result.getTestCase().getName() : "N/A";
            String testType = result.getTestCase() != null ? result.getTestCase().getType() : "N/A";
            String errorMsg = result.getErrorMessage() != null ? result.getErrorMessage() : "-";
            String executionTime = result.getExecutionTime() != null ? result.getExecutionTime().format(formatter) : "N/A";
            String typeBadge = "API".equals(testType) ? "badge-api" : "badge-ui";
            String statusBadge = "PASS".equals(result.getStatus()) ? "badge-pass" : "badge-fail";
            rows.append("<tr>")
                .append("<td>").append(result.getId()).append("</td>")
                .append("<td style='font-weight:600'>").append(testName).append("</td>")
                .append("<td><span class='badge ").append(typeBadge).append("'>").append(testType).append("</span></td>")
                .append("<td><span class='badge ").append(statusBadge).append("'>").append(result.getStatus()).append("</span></td>")
                .append("<td>").append(executionTime).append("</td>")
                .append("<td>").append(result.getDurationMs()).append(" ms</td>")
                .append("<td style='color:#94a3b8;font-size:0.85em'>").append(errorMsg).append("</td>")
                .append("</tr>");
        }

        return "<!DOCTYPE html><html lang='en'><head><meta charset='UTF-8'>" +
            "<meta name='viewport' content='width=device-width,initial-scale=1'>" +
            "<title>Test Automation Report</title><style>" + sharedStyle() + "</style></head><body>" +
            "<div class='container'>" +
            "<div class='topbar'>" +
            "<svg width='48' height='48' viewBox='0 0 48 48' fill='none'><rect width='48' height='48' rx='12' fill='#eff6ff'/>" +
            "<path d='M14 34V20l10-8 10 8v14H28v-8h-8v8z' fill='#3b82f6'/><rect x='20' y='26' width='8' height='8' rx='1' fill='#bfdbfe'/></svg>" +
            "<div><h1>Test Automation Report</h1><p>Generated: " + java.time.LocalDateTime.now().format(formatter) + " &nbsp;|&nbsp; Regression Test Suite Framework</p></div></div>" +
            "<div class='stats-grid'>" +
            "<div class='stat-card total'><div class='stat-icon'>🧪</div><div class='stat-value'>" + totalTests + "</div><div class='stat-label'>Total Tests</div></div>" +
            "<div class='stat-card passed'><div class='stat-icon'>✅</div><div class='stat-value'>" + passedTests + "</div><div class='stat-label'>Passed</div></div>" +
            "<div class='stat-card failed'><div class='stat-icon'>❌</div><div class='stat-value'>" + failedTests + "</div><div class='stat-label'>Failed</div></div>" +
            "<div class='stat-card rate'><div class='stat-icon'>📈</div><div class='stat-value'>" + passRate + "%</div><div class='stat-label'>Pass Rate</div></div>" +
            "</div>" +
            "<div class='card'><div class='card-title'>" +
            "<svg width='20' height='20' viewBox='0 0 20 20' fill='#3b82f6'><path d='M3 4h14v2H3zm0 5h14v2H3zm0 5h10v2H3z'/></svg>Test Results</div>" +
            "<div style='overflow-x:auto'><table><thead><tr><th>ID</th><th>Test Name</th><th>Type</th><th>Status</th><th>Execution Time</th><th>Duration</th><th>Error</th></tr></thead>" +
            "<tbody>" + rows + "</tbody></table></div></div>" +
            "<div class='footer'>🚀 Automated Regression Test Suite Framework &nbsp;|&nbsp; Spring Boot + Selenium + REST-Assured &nbsp;|&nbsp; © 2026</div>" +
            "</div></body></html>";
    }

    public String generateCsvReport() {
        List<TestResult> results = testResultRepository.findAll();
        StringBuilder csv = new StringBuilder();

        // Header
        csv.append("ID,Test Name,Type,Status,Execution Time,Duration (ms),Error Message\n");

        // Data rows
        for (TestResult result : results) {
            String testName = result.getTestCase() != null ? result.getTestCase().getName() : "N/A";
            String testType = result.getTestCase() != null ? result.getTestCase().getType() : "N/A";
            String errorMsg = result.getErrorMessage() != null ? result.getErrorMessage().replace(",", ";") : "";
            String executionTime = result.getExecutionTime() != null ? result.getExecutionTime().toString() : "N/A";

            csv.append(result.getId()).append(",")
                    .append("\"").append(testName).append("\",")
                    .append(testType).append(",")
                    .append(result.getStatus()).append(",")
                    .append(executionTime).append(",")
                    .append(result.getDurationMs()).append(",")
                    .append("\"").append(errorMsg).append("\"")
                    .append("\n");
        }

        return csv.toString();
    }

    // JUnit XML report (standard format compatible with CI tools like Jenkins/GitHub Actions)
    public String generateJUnitXmlReport() {
        List<TestResult> results = testResultRepository.findAll();

        long total = results.size();
        long failures = results.stream().filter(r -> "FAIL".equals(r.getStatus())).count();
        long errors = results.stream().filter(r -> "ERROR".equals(r.getStatus())).count();
        double totalTime = results.stream()
                .filter(r -> r.getDurationMs() != null)
                .mapToLong(TestResult::getDurationMs)
                .sum() / 1000.0;

        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append(String.format(
                "<testsuite name=\"RegressionTestSuite\" tests=\"%d\" failures=\"%d\" errors=\"%d\" time=\"%.3f\" timestamp=\"%s\">\n",
                total, failures, errors, totalTime, java.time.LocalDateTime.now().format(formatter)));

        for (TestResult result : results) {
            String testName = result.getTestCase() != null ? result.getTestCase().getName() : "UnknownTest";
            String testType = result.getTestCase() != null ? result.getTestCase().getType() : "Unknown";
            double time = result.getDurationMs() != null ? result.getDurationMs() / 1000.0 : 0.0;

            xml.append(String.format("  <testcase name=\"%s\" classname=\"%s\" time=\"%.3f\"",
                    escapeXml(testName), escapeXml(testType), time));

            if ("FAIL".equals(result.getStatus())) {
                String msg = result.getErrorMessage() != null ? result.getErrorMessage() : "Test assertion failed";
                xml.append(">\n");
                xml.append(String.format("    <failure message=\"%s\">%s</failure>\n",
                        escapeXml(msg), escapeXml(msg)));
                xml.append("  </testcase>\n");
            } else if ("ERROR".equals(result.getStatus())) {
                String msg = result.getErrorMessage() != null ? result.getErrorMessage() : "Test execution error";
                xml.append(">\n");
                xml.append(String.format("    <error message=\"%s\">%s</error>\n",
                        escapeXml(msg), escapeXml(msg)));
                xml.append("  </testcase>\n");
            } else {
                xml.append("/>\n");
            }
        }

        xml.append("</testsuite>");
        return xml.toString();
    }

    // Collect and return failure logs as structured text
    public String collectFailureLogs() {
        List<TestResult> failures = testResultRepository.findByStatus("FAIL");
        failures.addAll(testResultRepository.findByStatus("ERROR"));

        if (failures.isEmpty()) {
            return "No failures or errors found.";
        }

        StringBuilder logs = new StringBuilder();
        logs.append("=== FAILURE LOG REPORT ===\n");
        logs.append("Generated: ").append(java.time.LocalDateTime.now().format(formatter)).append("\n");
        logs.append("Total failures: ").append(failures.size()).append("\n\n");

        for (TestResult r : failures) {
            logs.append("------------------------------------------\n");
            logs.append("Test ID     : ").append(r.getId()).append("\n");
            logs.append("Test Name   : ").append(r.getTestCaseName()).append("\n");
            logs.append("Type        : ").append(r.getTestCaseType()).append("\n");
            logs.append("Status      : ").append(r.getStatus()).append("\n");
            logs.append("Time        : ").append(r.getExecutionTime() != null ? r.getExecutionTime().format(formatter) : "N/A").append("\n");
            logs.append("Duration    : ").append(r.getDurationMs()).append("ms\n");
            if (r.getErrorMessage() != null) {
                logs.append("Error       : ").append(r.getErrorMessage()).append("\n");
            }
            if (r.getScreenshotPath() != null) {
                logs.append("Screenshot  : ").append(r.getScreenshotPath()).append("\n");
            }
        }

        return logs.toString();
    }

    private String escapeXml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&apos;");
    }
}