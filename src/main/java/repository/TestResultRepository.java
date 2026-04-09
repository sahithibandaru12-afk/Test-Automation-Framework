package com.mycompany.mytestframework.repository;

import com.mycompany.mytestframework.entity.TestResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TestResultRepository extends JpaRepository<TestResult, Long> {
    List<TestResult> findByStatus(String status);
    List<TestResult> findByTestCaseIdOrderByExecutionTimeDesc(Long testCaseId);
}