package com.mycompany.mytestframework.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mycompany.mytestframework.service.ReportService;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping("/html")
    public ResponseEntity<String> generateHtmlReport() {
        String htmlContent = reportService.generateHtmlReport();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_HTML);

        return new ResponseEntity<>(htmlContent, headers, HttpStatus.OK);
    }

    @GetMapping("/csv")
    public ResponseEntity<String> generateCsvReport() {
        String csvContent = reportService.generateCsvReport();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=test-report.csv");

        return new ResponseEntity<>(csvContent, headers, HttpStatus.OK);
    }

    // GET /api/reports/junit  — JUnit XML format (CI/CD compatible)
    @GetMapping("/junit")
    public ResponseEntity<String> generateJUnitReport() {
        String xmlContent = reportService.generateJUnitXmlReport();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_XML);
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=junit-report.xml");

        return new ResponseEntity<>(xmlContent, headers, HttpStatus.OK);
    }

    // POST /api/logs/collect  — collect and return failure logs
    @org.springframework.web.bind.annotation.PostMapping("/logs/collect")
    public ResponseEntity<String> collectLogs() {
        String logs = reportService.collectFailureLogs();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);

        return new ResponseEntity<>(logs, headers, HttpStatus.OK);
    }

    // GET /api/reports/generate  — alias that returns HTML report
    @GetMapping("/generate")
    public ResponseEntity<String> generateReport() {
        return generateHtmlReport();
    }
    @GetMapping("/dashboard")
    public ResponseEntity<String> analyticsDashboard() {
        String css = "* { margin:0; padding:0; box-sizing:border-box; font-family:'Segoe UI',sans-serif; }" +
            "body { background:linear-gradient(135deg,#e0f2fe 0%,#f0fdf4 50%,#fef9c3 100%); min-height:100vh; padding:24px; }" +
            ".container { max-width:1200px; margin:0 auto; }" +
            ".topbar { display:flex; align-items:center; gap:16px; background:white; border-radius:16px; padding:20px 28px; margin-bottom:24px; box-shadow:0 4px 20px rgba(0,0,0,0.08); border-left:6px solid #3b82f6; }" +
            ".topbar h1 { color:#1e293b; font-size:1.8em; font-weight:700; }" +
            ".topbar p { color:#64748b; font-size:0.92em; margin-top:3px; }" +
            ".stats-grid { display:grid; grid-template-columns:repeat(auto-fit,minmax(200px,1fr)); gap:16px; margin-bottom:24px; }" +
            ".stat-card { background:white; border-radius:14px; padding:22px 20px; text-align:center; box-shadow:0 4px 16px rgba(0,0,0,0.07); transition:transform 0.2s; position:relative; overflow:hidden; cursor:default; }" +
            ".stat-card:hover { transform:translateY(-4px); box-shadow:0 8px 24px rgba(0,0,0,0.12); }" +
            ".stat-card::before { content:''; position:absolute; top:0; left:0; right:0; height:4px; }" +
            ".sc-blue::before{background:#3b82f6} .sc-green::before{background:#22c55e} .sc-red::before{background:#ef4444} .sc-amber::before{background:#f59e0b}" +
            ".stat-icon { font-size:2em; margin-bottom:8px; }" +
            ".stat-value { font-size:2.4em; font-weight:800; margin:4px 0; }" +
            ".sc-blue .stat-value{color:#3b82f6} .sc-green .stat-value{color:#22c55e} .sc-red .stat-value{color:#ef4444} .sc-amber .stat-value{color:#f59e0b}" +
            ".stat-label { color:#64748b; font-size:0.85em; font-weight:600; text-transform:uppercase; letter-spacing:0.5px; }" +
            ".card { background:white; border-radius:14px; padding:24px; margin-bottom:20px; box-shadow:0 4px 16px rgba(0,0,0,0.07); }" +
            ".card-title { display:flex; align-items:center; gap:10px; color:#1e293b; font-size:1.1em; font-weight:700; margin-bottom:16px; padding-bottom:12px; border-bottom:2px solid #f1f5f9; }" +
            "table { width:100%; border-collapse:collapse; }" +
            "th { background:#f8fafc; color:#475569; padding:11px 14px; text-align:left; font-size:0.82em; font-weight:700; text-transform:uppercase; letter-spacing:0.5px; border-bottom:2px solid #e2e8f0; }" +
            "td { padding:11px 14px; border-bottom:1px solid #f1f5f9; color:#334155; font-size:0.92em; }" +
            "tr:hover td { background:#f8fafc; }" +
            ".pass-rate { font-weight:700; } .good{color:#22c55e} .bad{color:#ef4444}" +
            ".actions { display:flex; flex-wrap:wrap; gap:10px; }" +
            "button { padding:10px 20px; border:none; border-radius:10px; cursor:pointer; font-size:0.9em; font-weight:600; transition:all 0.2s; }" +
            ".btn-primary { background:#3b82f6; color:white; } .btn-primary:hover { background:#2563eb; transform:translateY(-1px); }" +
            ".btn-success { background:#22c55e; color:white; } .btn-success:hover { background:#16a34a; transform:translateY(-1px); }" +
            ".btn-purple { background:#8b5cf6; color:white; } .btn-purple:hover { background:#7c3aed; transform:translateY(-1px); }" +
            ".loading { text-align:center; padding:40px; color:#94a3b8; font-size:1em; }" +
            ".footer { text-align:center; color:#64748b; font-size:0.85em; margin-top:20px; }";

        String html = "<!DOCTYPE html><html lang='en'><head><meta charset='UTF-8'>" +
            "<meta name='viewport' content='width=device-width,initial-scale=1'>" +
            "<title>Analytics Dashboard</title><style>" + css + "</style></head><body>" +
            "<div class='container'>" +
            "<div class='topbar'>" +
            "<svg width='48' height='48' viewBox='0 0 48 48' fill='none'><rect width='48' height='48' rx='12' fill='#eff6ff'/>" +
            "<rect x='10' y='28' width='6' height='10' rx='2' fill='#3b82f6'/>" +
            "<rect x='20' y='20' width='6' height='18' rx='2' fill='#22c55e'/>" +
            "<rect x='30' y='14' width='6' height='24' rx='2' fill='#f59e0b'/>" +
            "<path d='M10 26 L20 18 L30 22 L38 12' stroke='#ef4444' stroke-width='2.5' fill='none' stroke-linecap='round'/></svg>" +
            "<div><h1>Test Analytics Dashboard</h1><p>Live metrics &nbsp;|&nbsp; Regression Test Suite Framework &nbsp;|&nbsp; Spring Boot + Selenium + REST-Assured</p></div></div>" +
            "<div class='stats-grid'>" +
            "<div class='stat-card sc-blue'><div class='stat-icon'>🧪</div><div class='stat-value' id='total'>-</div><div class='stat-label'>Total Tests</div></div>" +
            "<div class='stat-card sc-green'><div class='stat-icon'>✅</div><div class='stat-value' id='passed'>-</div><div class='stat-label'>Passed</div></div>" +
            "<div class='stat-card sc-red'><div class='stat-icon'>❌</div><div class='stat-value' id='failed'>-</div><div class='stat-label'>Failed</div></div>" +
            "<div class='stat-card sc-amber'><div class='stat-icon'>📈</div><div class='stat-value' id='passRate'>-</div><div class='stat-label'>Pass Rate</div></div>" +
            "</div>" +
            "<div class='card'><div class='card-title'><span>📅</span> Trend Analysis — Last 30 Days</div><div id='trends'><div class='loading'>⏳ Loading trends...</div></div></div>" +
            "<div class='card'><div class='card-title'><span>🔄</span> Flaky Tests Detection</div><div id='flaky'><div class='loading'>⏳ Loading...</div></div></div>" +
            "<div class='card'><div class='card-title'><span>⚡</span> Quick Actions</div><div class='actions'>" +
            "<button class='btn-primary' onclick='refreshData()'>🔄 Refresh Data</button>" +
            "<button class='btn-success' onclick=\"window.location.href='/api/reports/html'\">📊 Full Report</button>" +
            "<button class='btn-purple' onclick=\"window.location.href='/api/analytics/overview'\">📋 Stats Overview</button>" +
            "<button class='btn-primary' onclick=\"window.location.href='/api/tests/suites/view'\">🗂️ Test Suites</button>" +
            "<button class='btn-success' onclick=\"window.location.href='/api/schedule/view'\">⏰ Scheduling</button>" +
            "</div></div>" +
            "<div class='footer'>🚀 Automated Regression Test Suite Framework &nbsp;|&nbsp; © 2026</div>" +
            "</div>" +
            "<script>" +
            "async function fetchStats(){const r=await fetch('/api/analytics/stats');const d=await r.json();" +
            "document.getElementById('total').innerText=d.totalTests;" +
            "document.getElementById('passed').innerText=d.passed;" +
            "document.getElementById('failed').innerText=d.failed;" +
            "document.getElementById('passRate').innerText=d.passRate+'%';}" +
            "async function fetchTrends(){const r=await fetch('/api/analytics/trends?days=30');const d=await r.json();" +
            "if(!d.trends||d.trends.length===0){document.getElementById('trends').innerHTML='<p style=\"color:#94a3b8;padding:20px\">No trend data available yet.</p>';return;}" +
            "let h='<div style=\"overflow-x:auto\"><table><thead><tr><th>Date</th><th>Pass Rate</th><th>Total</th><th>Passed</th><th>Failed</th></tr></thead><tbody>';" +
            "d.trends.forEach(t=>{h+=`<tr><td>${t.date}</td><td class='pass-rate ${t.passRate>=80?'good':'bad'}'>${t.passRate}%</td><td>${t.total}</td><td style='color:#22c55e;font-weight:600'>${t.passed}</td><td style='color:#ef4444;font-weight:600'>${t.failed}</td></tr>`;});" +
            "h+='</tbody></table></div>';document.getElementById('trends').innerHTML=h;}" +
            "async function fetchFlaky(){const r=await fetch('/api/analytics/flaky');const f=await r.json();" +
            "if(f.length===0){document.getElementById('flaky').innerHTML='<p style=\"color:#22c55e;padding:10px;font-weight:600\">✅ No flaky tests detected — all tests are stable!</p>';return;}" +
            "let h='<div style=\"overflow-x:auto\"><table><thead><tr><th>Test Name</th><th>Total Runs</th><th>Passed</th><th>Failed</th><th>Flakiness Rate</th></tr></thead><tbody>';" +
            "f.forEach(t=>{h+=`<tr><td style='font-weight:600'>${t.testName}</td><td>${t.totalRuns}</td><td style='color:#22c55e;font-weight:600'>${t.passed}</td><td style='color:#ef4444;font-weight:600'>${t.failed}</td><td style='color:#ef4444;font-weight:700'>${t.flakinessRate}%</td></tr>`;});" +
            "h+='</tbody></table></div>';document.getElementById('flaky').innerHTML=h;}" +
            "async function refreshData(){" +
            "document.getElementById('trends').innerHTML='<div class=\"loading\">⏳ Loading...</div>';" +
            "document.getElementById('flaky').innerHTML='<div class=\"loading\">⏳ Loading...</div>';" +
            "await fetchStats();await fetchTrends();await fetchFlaky();}" +
            "refreshData();" +
            "</script></body></html>";

        return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(html);
    }
}