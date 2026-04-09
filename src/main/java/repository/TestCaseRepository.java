package com.mycompany.mytestframework.repository;

import com.mycompany.mytestframework.entity.TestCase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TestCaseRepository extends JpaRepository<TestCase, Long> {
    List<TestCase> findByType(String type);
    List<TestCase> findByTestSuiteId(Long suiteId);
}