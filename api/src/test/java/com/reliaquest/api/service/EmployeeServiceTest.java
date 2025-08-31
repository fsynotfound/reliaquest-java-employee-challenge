package com.reliaquest.api.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.reliaquest.api.exception.EmployeeNotFoundException;
import com.reliaquest.api.exception.InvalidUUIDException;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.model.EmployeeInput;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

class EmployeeServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private EmployeeService employeeService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAllEmployees_shouldReturnList() {
        Map<String, Object> data = new HashMap<>();
        data.put("id", UUID.randomUUID().toString());
        data.put("employee_name", "Alice");
        data.put("employee_salary", 8000);
        data.put("employee_age", 30);
        data.put("employee_title", "Engineer");
        data.put("employee_email", "alice@example.com");

        Map<String, Object> mockResponseBody = new HashMap<>();
        mockResponseBody.put("data", List.of(data));
        ResponseEntity<Map> response = new ResponseEntity<>(mockResponseBody, HttpStatus.OK);

        when(restTemplate.getForEntity(anyString(), eq(Map.class))).thenReturn(response);

        List<Employee> employees = employeeService.getAllEmployees();
        assertEquals(1, employees.size());
        assertEquals("Alice", employees.get(0).getEmployee_name());
    }

    @Test
    void getEmployeeById_shouldReturnEmployee() {
        String uuid = UUID.randomUUID().toString();
        Map<String, Object> data = new HashMap<>();
        data.put("id", uuid);
        data.put("employee_name", "Bob");
        data.put("employee_salary", 9000);
        data.put("employee_age", 28);
        data.put("employee_title", "Manager");
        data.put("employee_email", "bob@example.com");

        Map<String, Object> mockResponseBody = new HashMap<>();
        mockResponseBody.put("data", data);
        ResponseEntity<Map> response = new ResponseEntity<>(mockResponseBody, HttpStatus.OK);

        when(restTemplate.getForEntity(contains(uuid), eq(Map.class))).thenReturn(response);

        Employee emp = employeeService.getEmployeeById(uuid);
        assertEquals("Bob", emp.getEmployee_name());
    }

    @Test
    void getEmployeeById_shouldThrowNotFound() {
        String uuid = UUID.randomUUID().toString();

        HttpClientErrorException notFoundException = new HttpClientErrorException(
                HttpStatus.NOT_FOUND,
                "404 Not Found",
                HttpHeaders.EMPTY,
                "".getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8);

        when(restTemplate.getForEntity(contains(uuid), eq(Map.class))).thenThrow(notFoundException);

        assertThrows(EmployeeNotFoundException.class, () -> employeeService.getEmployeeById(uuid));
    }

    @Test
    void getEmployeeById_shouldThrowInvalidUUID() {
        String invalidId = "not-a-uuid";
        assertThrows(InvalidUUIDException.class, () -> employeeService.getEmployeeById(invalidId));
    }

    @Test
    void createEmployee_shouldReturnCreatedEmployee() {
        EmployeeInput input = new EmployeeInput();
        input.setName("Carol");
        input.setAge(25);
        input.setSalary(8500);
        input.setTitle("Dev");

        Map<String, Object> data = new HashMap<>();
        data.put("id", UUID.randomUUID().toString());
        data.put("employee_name", "Carol");
        data.put("employee_salary", 8500);
        data.put("employee_age", 25);
        data.put("employee_title", "Dev");
        data.put("employee_email", "carol@example.com");

        Map<String, Object> mockResponseBody = new HashMap<>();
        mockResponseBody.put("data", data);
        ResponseEntity<Map> response = new ResponseEntity<>(mockResponseBody, HttpStatus.OK);

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(response);

        Employee emp = employeeService.createEmployee(input);
        assertEquals("Carol", emp.getEmployee_name());
        assertEquals(8500, emp.getEmployee_salary());
    }

    @Test
    void deleteEmployee_shouldReturnName() {
        String uuid = UUID.randomUUID().toString();

        Map<String, Object> getData = new HashMap<>();
        getData.put("id", uuid);
        getData.put("employee_name", "Dave");
        getData.put("employee_salary", 7500);
        getData.put("employee_age", 32);
        getData.put("employee_title", "QA");
        getData.put("employee_email", "dave@example.com");

        Map<String, Object> getResponseBody = new HashMap<>();
        getResponseBody.put("data", getData);
        ResponseEntity<Map> getResponse = new ResponseEntity<>(getResponseBody, HttpStatus.OK);

        when(restTemplate.getForEntity(contains(uuid), eq(Map.class))).thenReturn(getResponse);

        Map<String, Object> deleteResponseBody = new HashMap<>();
        deleteResponseBody.put("data", true);
        ResponseEntity<Map> deleteResponse = new ResponseEntity<>(deleteResponseBody, HttpStatus.OK);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(deleteResponse);

        String deletedName = employeeService.deleteEmployee(uuid);
        assertEquals("Dave", deletedName);
    }

    @Test
    void getTop10HighestEarningNames_shouldReturnCorrectList() {
        List<Map<String, Object>> employeesData = new ArrayList<>();
        for (int i = 1; i <= 15; i++) {
            Map<String, Object> emp = new HashMap<>();
            emp.put("id", UUID.randomUUID().toString());
            emp.put("employee_name", "Emp" + i);
            emp.put("employee_salary", 1000 * i);
            emp.put("employee_age", 25 + i);
            emp.put("employee_title", "Title" + i);
            emp.put("employee_email", "emp" + i + "@example.com");
            employeesData.add(emp);
        }

        Map<String, Object> mockResponseBody = new HashMap<>();
        mockResponseBody.put("data", employeesData);
        ResponseEntity<Map> response = new ResponseEntity<>(mockResponseBody, HttpStatus.OK);

        when(restTemplate.getForEntity(anyString(), eq(Map.class))).thenReturn(response);

        List<String> top10 = employeeService.getTop10HighestEarningNames();

        List<String> expected = new ArrayList<>();
        for (int i = 15; i >= 6; i--) {
            expected.add("Emp" + i);
        }

        assertEquals(expected, top10);
    }

    @Test
    void getHighestSalary_shouldReturnMaxSalary() {
        List<Map<String, Object>> employeesData = new ArrayList<>();
        employeesData.add(Map.of(
                "id",
                UUID.randomUUID().toString(),
                "employee_name",
                "John",
                "employee_salary",
                7000,
                "employee_age",
                30,
                "employee_title",
                "Dev",
                "employee_email",
                "john@example.com"));
        employeesData.add(Map.of(
                "id",
                UUID.randomUUID().toString(),
                "employee_name",
                "Jane",
                "employee_salary",
                9000,
                "employee_age",
                35,
                "employee_title",
                "Manager",
                "employee_email",
                "jane@example.com"));

        Map<String, Object> mockResponseBody = new HashMap<>();
        mockResponseBody.put("data", employeesData);
        ResponseEntity<Map> response = new ResponseEntity<>(mockResponseBody, HttpStatus.OK);

        when(restTemplate.getForEntity(anyString(), eq(Map.class))).thenReturn(response);

        int highest = employeeService.getHighestSalary();
        assertEquals(9000, highest);
    }

    @Test
    void searchEmployeesByName_shouldReturnMatching() {
        List<Map<String, Object>> employeesData = new ArrayList<>();
        employeesData.add(Map.of(
                "id",
                UUID.randomUUID().toString(),
                "employee_name",
                "Alice Smith",
                "employee_salary",
                7000,
                "employee_age",
                29,
                "employee_title",
                "Engineer",
                "employee_email",
                "alice@example.com"));
        employeesData.add(Map.of(
                "id",
                UUID.randomUUID().toString(),
                "employee_name",
                "Bob Johnson",
                "employee_salary",
                6500,
                "employee_age",
                35,
                "employee_title",
                "QA",
                "employee_email",
                "bob@example.com"));

        Map<String, Object> mockResponseBody = new HashMap<>();
        mockResponseBody.put("data", employeesData);
        ResponseEntity<Map> response = new ResponseEntity<>(mockResponseBody, HttpStatus.OK);

        when(restTemplate.getForEntity(anyString(), eq(Map.class))).thenReturn(response);

        List<Employee> result = employeeService.searchEmployeesByName("smith");
        assertEquals(1, result.size());
        assertEquals("Alice Smith", result.get(0).getEmployee_name());
    }
}
