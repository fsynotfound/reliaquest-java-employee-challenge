package com.reliaquest.api.controller;

import com.reliaquest.api.model.Employee;
import com.reliaquest.api.model.EmployeeInput;
import com.reliaquest.api.service.EmployeeService;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class EmployeeController implements IEmployeeController<Employee, EmployeeInput> {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @Override
    public ResponseEntity<List<Employee>> getAllEmployees() {
        log.info("[API] GET /");
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }

    @Override
    public ResponseEntity<List<Employee>> getEmployeesByNameSearch(String searchString) {
        log.info("[API] GET /search/{}", searchString);
        return ResponseEntity.ok(employeeService.searchEmployeesByName(searchString));
    }

    @Override
    public ResponseEntity<Employee> getEmployeeById(String id) {
        log.info("[API] GET /{}", id);
        return ResponseEntity.ok(employeeService.getEmployeeById(id));
    }

    @Override
    public ResponseEntity<Integer> getHighestSalaryOfEmployees() {
        log.info("[API] GET /highestSalary");
        return ResponseEntity.ok(employeeService.getHighestSalary());
    }

    @Override
    public ResponseEntity<List<String>> getTopTenHighestEarningEmployeeNames() {
        log.info("[API] GET /topTenHighestEarningEmployeeNames");
        return ResponseEntity.ok(employeeService.getTop10HighestEarningNames());
    }

    @Override
    public ResponseEntity<Employee> createEmployee(EmployeeInput employeeInput) {
        log.info("[API] POST / (name={})", employeeInput.getName());
        return ResponseEntity.ok(employeeService.createEmployee(employeeInput));
    }

    @Override
    public ResponseEntity<String> deleteEmployeeById(String id) {
        log.info("[API] DELETE /{} (lookup name via GET /{})", id, id);
        return ResponseEntity.ok(employeeService.deleteEmployee(id));
    }
}
