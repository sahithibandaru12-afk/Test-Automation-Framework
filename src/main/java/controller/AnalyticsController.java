package com.mycompany.mytestframework.controller;

import com.mycompany.mytestframework.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    // GET /api/analytics/stats
    @GetMapping("/stats")
    public Map<String, Object> getOverallStats() {
        return analyticsService.getOverallStats();
    }

    // GET /api/analytics/trends?days=30
    @GetMapping("/trends")
    public Map<String, Object> getTrends(@RequestParam(defaultValue = "30") int days) {
        return analyticsService.getTrends(days);
    }

    // GET /api/analytics/suite/{suiteId}  (maps to GET /results/{suiteId} requirement)
    @GetMapping("/suite/{suiteId}")
    public Map<String, Object> getSuiteAnalytics(@PathVariable Long suiteId) {
        return analyticsService.getSuiteAnalytics(suiteId);
    }

    // GET /api/analytics/results/{suiteId}
    @GetMapping("/results/{suiteId}")
    public Map<String, Object> getResultsBySuite(@PathVariable Long suiteId) {
        return analyticsService.getSuiteAnalytics(suiteId);
    }

    // GET /api/analytics/flaky
    @GetMapping("/flaky")
    public List<Map<String, Object>> getFlakyTests() {
        return analyticsService.getFlakyTests();
    }

    // GET /api/analytics/overview — styled HTML stats page
    @GetMapping(value = "/overview", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> getStatsHtml() {
        Map<String, Object> stats = analyticsService.getOverallStats();
        Map<String, Object> trends = analyticsService.getTrends(30);
        List<Map<String, Object>> flaky = analyticsService.getFlakyTests();

        Object total = stats.get("totalTests");
        Object passed = stats.get("passed");
        Object failed = stats.get("failed");
        Object passRate = stats.get("passRate");
        Object avgDuration = stats.get("avgDurationMs");
        Object totalCases = stats.get("totalTestCases");

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> trendList = (List<Map<String, Object>>) trends.get("trends");

        StringBuilder trendRows = new StringBuilder();
        if (trendList == null || trendList.isEmpty()) {
            trendRows.append("<tr><td colspan='5' style='color:#94a3b8;padding:16px'>No trend data available yet.</td></tr>");
        } else {
            for (Map<String, Object> day : trendList) {
                double rate = day.get("passRate") instanceof Number ? ((Number) day.get("passRate")).doubleValue() : 0;
                String rateColor = rate >= 80 ? "#22c55e" : "#ef4444";
                trendRows.append("<tr>")
                    .append("<td>").append(day.get("date")).append("</td>")
                    .append("<td style='color:").append(rateColor).append(";font-weight:700'>").append(day.get("passRate")).append("%</td>")
                    .append("<td>").append(day.get("total")).append("</td>")
                    .append("<td style='color:#22c55e;font-weight:600'>").append(day.get("passed")).append("</td>")
                    .append("<td style='color:#ef4444;font-weight:600'>").append(day.get("failed")).append("</td>")
                    .append("</tr>");
            }
        }

        StringBuilder flakyRows = new StringBuilder();
        if (flaky.isEmpty()) {
            flakyRows.append("<tr><td colspan='5' style='color:#22c55e;font-weight:600;padding:16px'>✅ No flaky tests detected — all tests are stable!</td></tr>");
        } else {
            for (Map<String, Object> t : flaky) {
                flakyRows.append("<tr>")
                    .append("<td style='font-weight:600'>").append(t.get("testName")).append("</td>")
                    .append("<td>").append(t.get("totalRuns")).append("</td>")
                    .append("<td style='color:#22c55e;font-weight:600'>").append(t.get("passed")).append("</td>")
                    .append("<td style='color:#ef4444;font-weight:600'>").append(t.get("failed")).append("</td>")
                    .append("<td style='color:#ef4444;font-weight:700'>").append(t.get("flakinessRate")).append("%</td>")
                    .append("</tr>");
            }
        }

        String css = "* { margin:0; padding:0; box-sizing:border-box; font-family:'Segoe UI',sans-serif; }" +
            "body { background:linear-gradient(135deg,#e0f2fe 0%,#f0fdf4 50%,#fef9c3 100%); min-height:100vh; padding:24px; }" +
            ".container { max-width:1200px; margin:0 auto; }" +
            ".topbar { display:flex; align-items:center; gap:16px; background:white; border-radius:16px; padding:20px 28px; margin-bottom:24px; box-shadow:0 4px 20px rgba(0,0,0,0.08); border-left:6px solid #8b5cf6; }" +
            ".topbar h1 { color:#1e293b; font-size:1.8em; font-weight:700; }" +
            ".topbar p { color:#64748b; font-size:0.92em; margin-top:3px; }" +
            ".stats-grid { display:grid; grid-template-columns:repeat(auto-fit,minmax(180px,1fr)); gap:16px; margin-bottom:24px; }" +
            ".stat-card { background:white; border-radius:14px; padding:20px; text-align:center; box-shadow:0 4px 16px rgba(0,0,0,0.07); transition:transform 0.2s; position:relative; overflow:hidden; }" +
            ".stat-card:hover { transform:translateY(-4px); box-shadow:0 8px 24px rgba(0,0,0,0.12); }" +
            ".stat-card::before { content:''; position:absolute; top:0; left:0; right:0; height:4px; }" +
            ".sc1::before{background:#3b82f6} .sc2::before{background:#22c55e} .sc3::before{background:#ef4444} .sc4::before{background:#f59e0b} .sc5::before{background:#8b5cf6} .sc6::before{background:#06b6d4}" +
            ".stat-icon { font-size:1.8em; margin-bottom:6px; }" +
            ".stat-value { font-size:2em; font-weight:800; margin:4px 0; }" +
            ".sc1 .stat-value{color:#3b82f6} .sc2 .stat-value{color:#22c55e} .sc3 .stat-value{color:#ef4444} .sc4 .stat-value{color:#f59e0b} .sc5 .stat-value{color:#8b5cf6} .sc6 .stat-value{color:#06b6d4}" +
            ".stat-label { color:#64748b; font-size:0.82em; font-weight:600; text-transform:uppercase; letter-spacing:0.5px; }" +
            ".card { background:white; border-radius:14px; padding:24px; margin-bottom:20px; box-shadow:0 4px 16px rgba(0,0,0,0.07); }" +
            ".card-title { display:flex; align-items:center; gap:10px; color:#1e293b; font-size:1.1em; font-weight:700; margin-bottom:16px; padding-bottom:12px; border-bottom:2px solid #f1f5f9; }" +
            "table { width:100%; border-collapse:collapse; }" +
            "th { background:#f8fafc; color:#475569; padding:11px 14px; text-align:left; font-size:0.82em; font-weight:700; text-transform:uppercase; letter-spacing:0.5px; border-bottom:2px solid #e2e8f0; }" +
            "td { padding:11px 14px; border-bottom:1px solid #f1f5f9; color:#334155; font-size:0.92em; }" +
            "tr:hover td { background:#f8fafc; }" +
            ".footer { text-align:center; color:#64748b; font-size:0.85em; margin-top:20px; }";

        String html = "<!DOCTYPE html><html lang='en'><head><meta charset='UTF-8'>" +
            "<meta name='viewport' content='width=device-width,initial-scale=1'>" +
            "<title>Analytics Stats Overview</title><style>" + css + "</style></head><body>" +
            "<div class='container'>" +
            "<div class='topbar'>" +
            "<svg width='48' height='48' viewBox='0 0 48 48' fill='none'><rect width='48' height='48' rx='12' fill='#f5f3ff'/>" +
            "<circle cx='24' cy='24' r='12' stroke='#8b5cf6' stroke-width='3' fill='none'/>" +
            "<path d='M24 12 L24 24 L32 24' stroke='#8b5cf6' stroke-width='3' stroke-linecap='round'/>" +
            "<circle cx='24' cy='24' r='2' fill='#8b5cf6'/></svg>" +
            "<div><h1>Analytics Stats Overview</h1><p>Generated: " + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + " &nbsp;|&nbsp; Regression Test Suite Framework</p></div></div>" +
            "<div class='stats-grid'>" +
            "<div class='stat-card sc1'><div class='stat-icon'>🧪</div><div class='stat-value'>" + total + "</div><div class='stat-label'>Total Tests Run</div></div>" +
            "<div class='stat-card sc2'><div class='stat-icon'>✅</div><div class='stat-value'>" + passed + "</div><div class='stat-label'>Passed</div></div>" +
            "<div class='stat-card sc3'><div class='stat-icon'>❌</div><div class='stat-value'>" + failed + "</div><div class='stat-label'>Failed</div></div>" +
            "<div class='stat-card sc4'><div class='stat-icon'>📈</div><div class='stat-value'>" + passRate + "%</div><div class='stat-label'>Pass Rate</div></div>" +
            "<div class='stat-card sc5'><div class='stat-icon'>⚡</div><div class='stat-value'>" + avgDuration + "ms</div><div class='stat-label'>Avg Duration</div></div>" +
            "<div class='stat-card sc6'><div class='stat-icon'>📋</div><div class='stat-value'>" + totalCases + "</div><div class='stat-label'>Test Cases</div></div>" +
            "</div>" +
            "<div class='card'><div class='card-title'><span>📅</span> Trend Analysis — Last 30 Days</div>" +
            "<div style='overflow-x:auto'><table><thead><tr><th>Date</th><th>Pass Rate</th><th>Total</th><th>Passed</th><th>Failed</th></tr></thead>" +
            "<tbody>" + trendRows + "</tbody></table></div></div>" +
            "<div class='card'><div class='card-title'><span>🔄</span> Flaky Tests Detection</div>" +
            "<div style='overflow-x:auto'><table><thead><tr><th>Test Name</th><th>Total Runs</th><th>Passed</th><th>Failed</th><th>Flakiness Rate</th></tr></thead>" +
            "<tbody>" + flakyRows + "</tbody></table></div></div>" +
            "<div class='footer'>🚀 Automated Regression Test Suite Framework &nbsp;|&nbsp; Spring Boot + Selenium + REST-Assured &nbsp;|&nbsp; © 2026</div>" +
            "</div></body></html>";

        return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(html);
    }
}
