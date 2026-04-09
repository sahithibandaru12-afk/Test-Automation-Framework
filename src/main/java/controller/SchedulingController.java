package com.mycompany.mytestframework.controller;

import com.mycompany.mytestframework.entity.TestCase;
import com.mycompany.mytestframework.entity.TestResult;
import com.mycompany.mytestframework.repository.TestCaseRepository;
import com.mycompany.mytestframework.service.ParallelExecutionService;
import com.mycompany.mytestframework.service.SchedulingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/schedule")
public class SchedulingController {

    @Autowired
    private SchedulingService schedulingService;

    @Autowired
    private TestCaseRepository testCaseRepository;

    @Autowired
    private ParallelExecutionService parallelExecutionService;

    // GET /api/schedule/status
    @GetMapping("/status")
    public Map<String, String> getSchedulingStatus() {
        Map<String, String> status = new HashMap<>();
        status.put("scheduling", "enabled");
        status.put("daily", "Every day at 9:00 AM");
        status.put("hourly", "Every hour at minute 0");
        status.put("weekly", "Every Monday at 8:00 AM");
        status.put("message", "Scheduling system is active");
        return status;
    }

    // GET /api/execution/status — current test case count and readiness
    @GetMapping("/execution/status")
    public Map<String, Object> getExecutionStatus() {
        long total = testCaseRepository.count();
        Map<String, Object> status = new HashMap<>();
        status.put("status", "ready");
        status.put("totalTestCases", total);
        status.put("timestamp", LocalDateTime.now().toString());
        status.put("message", total > 0 ? "Framework ready with " + total + " test cases" : "No test cases configured yet");
        return status;
    }

    // POST /api/schedule/run — trigger a full parallel run immediately
    @PostMapping("/run")
    public Map<String, Object> triggerRun(@RequestParam(defaultValue = "3") int threads) {
        List<TestCase> all = testCaseRepository.findAll();
        List<Long> ids = all.stream().map(TestCase::getId).toList();

        Map<String, Object> response = new HashMap<>();
        if (ids.isEmpty()) {
            response.put("status", "skipped");
            response.put("message", "No test cases found");
            return response;
        }

        List<TestResult> results = parallelExecutionService.executeTestsInParallel(ids, threads);
        long passed = results.stream().filter(r -> "PASS".equals(r.getStatus())).count();
        long failed = results.size() - passed;

        response.put("status", "completed");
        response.put("total", results.size());
        response.put("passed", passed);
        response.put("failed", failed);
        response.put("threads", threads);
        return response;
    }

    // POST /api/schedule/run/daily
    @PostMapping("/run/daily")
    public String triggerDailyTests() {
        schedulingService.runDailyTests();
        return "Daily tests triggered manually";
    }

    // POST /api/schedule/run/hourly
    @PostMapping("/run/hourly")
    public String triggerHourlyTests() {
        schedulingService.runHourlyHealthCheck();
        return "Hourly health check triggered manually";
    }

    // POST /api/schedule/run/weekly
    @PostMapping("/run/weekly")
    public String triggerWeeklyTests() {
        schedulingService.runWeeklyRegressionTests();
        return "Weekly regression tests triggered manually";
    }

    // GET /api/schedule/view — styled HTML scheduling status page
    @GetMapping(value = "/view", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> getScheduleHtml() {
        long totalCases = testCaseRepository.count();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String now = LocalDateTime.now().format(fmt);

        String css = "* { margin:0; padding:0; box-sizing:border-box; font-family:'Segoe UI',sans-serif; }" +
            "body { background:linear-gradient(135deg,#e0f2fe 0%,#f0fdf4 50%,#fef9c3 100%); min-height:100vh; padding:24px; }" +
            ".container { max-width:900px; margin:0 auto; }" +
            ".topbar { display:flex; align-items:center; gap:16px; background:white; border-radius:16px; padding:20px 28px; margin-bottom:24px; box-shadow:0 4px 20px rgba(0,0,0,0.08); border-left:6px solid #f59e0b; }" +
            ".topbar h1 { color:#1e293b; font-size:1.8em; font-weight:700; }" +
            ".topbar p { color:#64748b; font-size:0.92em; margin-top:3px; }" +
            ".status-pill { display:inline-flex; align-items:center; gap:8px; background:#dcfce7; color:#166534; padding:8px 18px; border-radius:24px; font-weight:700; font-size:0.95em; margin-bottom:20px; }" +
            ".dot { width:10px; height:10px; background:#22c55e; border-radius:50%; animation:pulse 1.5s infinite; }" +
            "@keyframes pulse { 0%,100%{opacity:1} 50%{opacity:0.4} }" +
            ".schedule-grid { display:grid; grid-template-columns:1fr 1fr; gap:16px; margin-bottom:20px; }" +
            ".schedule-card { background:white; border-radius:14px; padding:20px; box-shadow:0 4px 16px rgba(0,0,0,0.07); border-top:4px solid; transition:transform 0.2s; }" +
            ".schedule-card:hover { transform:translateY(-3px); }" +
            ".sc-daily { border-color:#3b82f6; } .sc-hourly { border-color:#22c55e; } .sc-weekly { border-color:#8b5cf6; } .sc-manual { border-color:#f59e0b; }" +
            ".schedule-card h3 { font-size:1em; font-weight:700; margin-bottom:8px; }" +
            ".sc-daily h3{color:#3b82f6} .sc-hourly h3{color:#22c55e} .sc-weekly h3{color:#8b5cf6} .sc-manual h3{color:#f59e0b}" +
            ".schedule-card p { color:#64748b; font-size:0.88em; margin-bottom:8px; }" +
            ".schedule-card .time { font-weight:700; color:#1e293b; font-size:1em; }" +
            ".card { background:white; border-radius:14px; padding:24px; margin-bottom:16px; box-shadow:0 4px 16px rgba(0,0,0,0.07); }" +
            ".card-title { display:flex; align-items:center; gap:10px; color:#1e293b; font-size:1.1em; font-weight:700; margin-bottom:16px; padding-bottom:12px; border-bottom:2px solid #f1f5f9; }" +
            ".info-row { display:flex; justify-content:space-between; align-items:center; padding:12px 0; border-bottom:1px solid #f1f5f9; }" +
            ".info-row:last-child { border-bottom:none; }" +
            ".info-label { color:#64748b; font-size:0.92em; }" +
            ".info-value { font-weight:700; color:#1e293b; font-size:0.92em; }" +
            ".footer { text-align:center; color:#64748b; font-size:0.85em; margin-top:20px; }";

        String html = "<!DOCTYPE html><html lang='en'><head><meta charset='UTF-8'>" +
            "<meta name='viewport' content='width=device-width,initial-scale=1'>" +
            "<title>Scheduling Status</title><style>" + css + "</style></head><body>" +
            "<div class='container'>" +
            "<div class='topbar'>" +
            "<svg width='48' height='48' viewBox='0 0 48 48' fill='none'><rect width='48' height='48' rx='12' fill='#fffbeb'/>" +
            "<circle cx='24' cy='26' r='14' stroke='#f59e0b' stroke-width='3' fill='none'/>" +
            "<path d='M24 14 L24 26 L32 26' stroke='#f59e0b' stroke-width='3' stroke-linecap='round' stroke-linejoin='round'/>" +
            "<path d='M20 8 L28 8' stroke='#f59e0b' stroke-width='2.5' stroke-linecap='round'/></svg>" +
            "<div><h1>Scheduling & Execution Status</h1><p>Generated: " + now + " &nbsp;|&nbsp; Regression Test Suite Framework</p></div></div>" +
            "<div class='status-pill'><div class='dot'></div> Scheduling System ACTIVE</div>" +
            "<div class='schedule-grid'>" +
            "<div class='schedule-card sc-daily'><h3>📅 Daily Run</h3><p>Executes all test cases sequentially</p><div class='time'>Every day at 9:00 AM</div></div>" +
            "<div class='schedule-card sc-hourly'><h3>🕐 Hourly Health Check</h3><p>Parallel health check — 3 threads</p><div class='time'>Every hour at :00</div></div>" +
            "<div class='schedule-card sc-weekly'><h3>📆 Weekly Regression</h3><p>Full parallel regression — 5 threads</p><div class='time'>Every Monday at 8:00 AM</div></div>" +
            "<div class='schedule-card sc-manual'><h3>⚡ Manual Trigger</h3><p>POST /api/schedule/run to run immediately</p><div class='time'>On demand</div></div>" +
            "</div>" +
            "<div class='card'><div class='card-title'><span>📊</span> System Information</div>" +
            "<div class='info-row'><span class='info-label'>Scheduler Status</span><span class='info-value' style='color:#22c55e'>✅ Running</span></div>" +
            "<div class='info-row'><span class='info-label'>Total Test Cases Configured</span><span class='info-value'>" + totalCases + " test cases</span></div>" +
            "<div class='info-row'><span class='info-label'>Server Time</span><span class='info-value'>" + now + "</span></div>" +
            "<div class='info-row'><span class='info-label'>Execution Mode</span><span class='info-value'>Parallel + Sequential (Multithreading)</span></div>" +
            "<div class='info-row'><span class='info-label'>Framework</span><span class='info-value'>Spring Boot 4 + Quartz Scheduler</span></div>" +
            "<div class='info-row'><span class='info-label'>Test Integrations</span><span class='info-value'>Selenium WebDriver + REST-Assured</span></div>" +
            "</div>" +
            "<div class='footer'>🚀 Automated Regression Test Suite Framework &nbsp;|&nbsp; Spring Boot + Selenium + REST-Assured &nbsp;|&nbsp; © 2026</div>" +
            "</div></body></html>";

        return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(html);
    }
}
