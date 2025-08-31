package com.reliaquest.api.service;

import com.reliaquest.api.exception.EmployeeNotFoundException;
import com.reliaquest.api.exception.InvalidUUIDException;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.model.EmployeeInput;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class EmployeeService {

    private final RestTemplate restTemplate;
    private static final String BASE_URL = "http://localhost:8112/api/v1/employee";

    public EmployeeService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    private <T> T callWithRetry(java.util.concurrent.Callable<T> fn, String opDesc) {
        int attempts = 3;
        long backoff = 500;
        for (int i = 1; i <= attempts; i++) {
            try {
                return fn.call();
            } catch (HttpClientErrorException.TooManyRequests e) {
                log.warn("[{}] hit 429 (attempt {}/{})", opDesc, i, attempts);
                if (i == attempts) throw e;
                try { Thread.sleep(backoff); } catch (InterruptedException ignored) {}
                backoff *= 2;
            } catch (Exception e) {
                if (e instanceof RuntimeException re) throw re;
                throw new RuntimeException(e);
            }
        }
        throw new IllegalStateException("Retry loop unexpectedly exited for " + opDesc);
    }

    public List<Employee> getAllEmployees() {
        log.info("Fetching all employees...");
        try {
            ResponseEntity<Map> response = callWithRetry(
                    () -> restTemplate.getForEntity(BASE_URL, Map.class),
                    "GET all employees"
            );
            List<Map<String, Object>> rawList = (List<Map<String, Object>>) response.getBody().get("data");
            return rawList.stream().map(this::mapToEmployee).collect(Collectors.toList());
        } catch (ResourceAccessException e) {
            log.error("Mock API not reachable: {}", e.getMessage());
            throw new RuntimeException("Mock API not reachable. Did you start server:bootRun?");
        }
    }

    public List<Employee> searchEmployeesByName(String searchString) {
        log.info("Searching employees with fragment: {}", searchString);
        return getAllEmployees().stream()
                .filter(e -> e.getEmployee_name() != null &&
                        e.getEmployee_name().toLowerCase().contains(searchString.toLowerCase()))
                .collect(Collectors.toList());
    }

    public Employee getEmployeeById(String id) {
        log.info("Fetching employee by id: {}", id);

        if (!isValidUUID(id)) {
            log.error("Invalid UUID format: {}", id);
            throw new InvalidUUIDException("Invalid UUID format: " + id);
        }

        try {
            String url = BASE_URL + "/" + id;
            ResponseEntity<Map> response = callWithRetry(
                    () -> restTemplate.getForEntity(url, Map.class),
                    "GET employee by id"
            );
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
            return mapToEmployee(data);

        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                log.warn("Employee not found: {}", id);
                throw new EmployeeNotFoundException("Employee not found: " + id);
            }
            throw e;
        }
    }

    public int getHighestSalary() {
        int max = getAllEmployees().stream()
                .mapToInt(Employee::getEmployee_salary)
                .max().orElse(0);
        log.info("Highest salary = {}", max);
        return max;
    }

    public List<String> getTop10HighestEarningNames() {
        List<String> names = getAllEmployees().stream()
                .sorted((a, b) -> Integer.compare(b.getEmployee_salary(), a.getEmployee_salary()))
                .limit(10)
                .map(Employee::getEmployee_name)
                .collect(Collectors.toList());
        log.info("Top 10 earners: {}", names);
        return names;
    }

    public Employee createEmployee(EmployeeInput input) {
        log.info("Creating employee: name={}, title={}", input.getName(), input.getTitle());

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("name", input.getName());
        requestBody.put("salary", input.getSalary());
        requestBody.put("age", input.getAge());
        requestBody.put("title", input.getTitle());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> response = callWithRetry(
                () -> restTemplate.postForEntity(BASE_URL, request, Map.class),
                "POST create employee"
        );
        Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
        return mapToEmployee(data);
    }

    public String deleteEmployee(String id) {
        log.info("Deleting employee by id: {}", id);

        Employee employee = getEmployeeById(id);  // May throw InvalidUUIDException or EmployeeNotFoundException
        String name = employee.getEmployee_name();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> requestBody = Map.of("name", name);
        HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> response = callWithRetry(
                () -> restTemplate.exchange(BASE_URL, HttpMethod.DELETE, request, Map.class),
                "DELETE employee"
        );
        Map<String, Object> responseBody = response.getBody();
        if (Boolean.TRUE.equals(responseBody.get("data"))) {
            log.info("Deleted employee: {}", name);
            return name;
        } else {
            throw new RuntimeException("Delete failed for employee: " + name);
        }
    }

    private boolean isValidUUID(String id) {
        try {
            UUID.fromString(id);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private Employee mapToEmployee(Map<String, Object> map) {
        Employee e = new Employee();
        e.setId((String) map.get("id"));
        e.setEmployee_name((String) map.get("employee_name"));
        e.setEmployee_salary(((Number) map.get("employee_salary")).intValue());
        e.setEmployee_age(((Number) map.get("employee_age")).intValue());
        e.setEmployee_title((String) map.get("employee_title"));
        e.setEmployee_email((String) map.get("employee_email"));
        return e;
    }
}
