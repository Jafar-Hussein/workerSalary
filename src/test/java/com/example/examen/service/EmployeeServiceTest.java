package com.example.examen.service;

import com.example.examen.dto.EmployeeDTO;
import com.example.examen.model.Employee;
import com.example.examen.model.User;
import com.example.examen.repo.EmployeeRepo;
import com.example.examen.repo.SalaryRepo;
import com.example.examen.repo.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmployeeServiceTest {

    @Mock
    private EmployeeRepo employeeRepo;
    @Mock
    private UserRepo userRepo;
    @Mock
    private SalaryRepo salaryRepo;
    @Mock
    private UserService userService;
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private TypeMap<EmployeeDTO, Employee> typeMap;

    @InjectMocks
    private EmployeeService employeeService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(modelMapper.typeMap(EmployeeDTO.class, Employee.class)).thenReturn(typeMap);
    }
    @Test
    void saveEmployeeInfoByUsername_Success() {
        String username = "user1";
        User user = new User();
        EmployeeDTO employeeDTO = new EmployeeDTO();
        Employee employee = new Employee();

        when(userRepo.findByUsername(username)).thenReturn(Optional.of(user));
        when(modelMapper.map(employeeDTO, Employee.class)).thenReturn(employee);
        when(employeeRepo.save(any(Employee.class))).thenReturn(employee);

        ResponseEntity<?> response = employeeService.saveEmployeeInfoByUsername(username, employeeDTO);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Employee info saved successfully", response.getBody());
    }
    @Test
    void saveEmployeeInfoByUsername_UserNotFound() {
        String username = "user1";
        EmployeeDTO employeeDTO = new EmployeeDTO();

        when(userRepo.findByUsername(username)).thenReturn(Optional.empty());

        ResponseEntity<?> response = employeeService.saveEmployeeInfoByUsername(username, employeeDTO);

        assertEquals(400, response.getStatusCodeValue());
        assertEquals("User not found", response.getBody());
    }
    @Test
    void getCurrentUserEmployeeInfo_Success() {
        User user = mock(User.class);
        Employee employee = mock(Employee.class);
        EmployeeDTO employeeDTO = new EmployeeDTO();

        when(userService.getCurrentUser()).thenReturn(user);
        when(user.getEmployee()).thenReturn(employee);
        when(modelMapper.map(employee, EmployeeDTO.class)).thenReturn(employeeDTO);

        ResponseEntity<?> response = employeeService.getCurrentUserEmployeeInfo();

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody() instanceof EmployeeDTO);
    }
    @Test
    void getCurrentUserEmployeeInfo_NotLoggedIn() {
        when(userService.getCurrentUser()).thenReturn(null);

        ResponseEntity<?> response = employeeService.getCurrentUserEmployeeInfo();

        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatusCodeValue());
    }
    @Test
    void updateEmployeeInfo_Success() {
        User user = mock(User.class);  // User should be a mock
        Employee existingEmployee = new Employee();
        EmployeeDTO employeeDTO = new EmployeeDTO();

        when(userService.getCurrentUser()).thenReturn(user);
        when(user.getEmployee()).thenReturn(existingEmployee);  // Now this should work as user is mocked
        when(employeeRepo.save(any(Employee.class))).thenReturn(existingEmployee);  // Use any() for safety

        ResponseEntity<?> response = employeeService.updateEmployeeInfo(employeeDTO);

        assertEquals(200, response.getStatusCodeValue());
    }


    @Test
    void updateEmployeeInfo_NoEmployeeFound() {
        // Mock the User object instead of using a new instance
        User user = mock(User.class);

        when(userService.getCurrentUser()).thenReturn(user);
        when(user.getEmployee()).thenReturn(null); // Now this is a valid mock method call

        assertThrows(RuntimeException.class, () -> employeeService.updateEmployeeInfo(new EmployeeDTO()));
    }

    @Test
    void deleteEmployeeAndUser_Success() {
        Long employeeId = 1L;
        Employee employee = new Employee();
        User user = new User();
        employee.setUser(user);

        when(employeeRepo.findById(employeeId)).thenReturn(Optional.of(employee));

        ResponseEntity<?> response = employeeService.deleteEmployeeAndUser(employeeId);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Employee and associated user deleted successfully.", response.getBody());
        verify(employeeRepo).delete(employee);
        verify(userRepo).delete(user);
    }
    @Test
    void deleteEmployeeAndUser_NotFound() {
        Long employeeId = 1L;

        when(employeeRepo.findById(employeeId)).thenReturn(Optional.empty());

        ResponseEntity<?> response = employeeService.deleteEmployeeAndUser(employeeId);

        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCodeValue());
    }


}