package com.example.examen.service;

import com.example.examen.adminDTO.CheckOutInfoDTO;
import com.example.examen.dto.CheckOutAdjustmentDTO;
import com.example.examen.dto.CheckOutDTO;
import com.example.examen.model.CheckOut;
import com.example.examen.model.Employee;
import com.example.examen.model.User;
import com.example.examen.repo.CheckInRepo;
import com.example.examen.repo.CheckOutRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CheckoutServiceTest {

    @Mock
    private CheckOutRepo checkOutRepo;

    @Mock
    private UserService userService;

    @Mock
    private EmployeeService employeeService;

    @Mock
    private CheckInRepo checkInRepo;

    @Mock
    private SalaryService salaryService;

    @InjectMocks
    private CheckoutService checkoutService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }
    @Test
    void testCheckOut_Success() {
        User user = mock(User.class);
        Employee employee = mock(Employee.class);
        when(user.getEmployee()).thenReturn(employee);
        when(userService.getCurrentUser()).thenReturn(user);

        checkoutService.checkOut();

        verify(checkOutRepo).save(any());
        verify(salaryService).updateWorkedHoursAndRecalculateSalary(employee.getId());
    }
    @Test
    void testCheckOut_UserNotFound_ThrowsException() {
        when(userService.getCurrentUser()).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> checkoutService.checkOut());
    }
    @Test
    void testGetAllEmployeeCheckOuts() {
        Employee employee = new Employee();
        employee.setFirstName("John");
        employee.setLastName("Doe");

        CheckOut checkOut = new CheckOut();
        checkOut.setEmployee(employee);
        checkOut.setCheckOutDateTime(LocalDateTime.now());

        when(checkOutRepo.findAll()).thenReturn(List.of(checkOut));

        ResponseEntity<List<CheckOutInfoDTO>> response = checkoutService.getAllEmployeeCheckOuts();

        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("John Doe", response.getBody().get(0).getEmployeeName());
    }
    @Test
    void testGetCheckOutsByEmployeeId_Success() {
        User user = mock(User.class);
        Employee employee = mock(Employee.class);
        when(user.getEmployee()).thenReturn(employee);
        when(employee.getId()).thenReturn(1L);
        when(userService.getCurrentUser()).thenReturn(user);

        CheckOut checkOut = new CheckOut();
        checkOut.setId(1L);
        checkOut.setCheckOutDateTime(LocalDateTime.now());

        when(checkOutRepo.findAllByEmployeeId(1L)).thenReturn(List.of(checkOut));

        List<CheckOutDTO> result = checkoutService.getCheckOutsByEmployeeId();

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }
    @Test
    void testGetCheckOutsByEmployeeId_UserNotFound_ThrowsException() {
        when(userService.getCurrentUser()).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> checkoutService.getCheckOutsByEmployeeId());
    }
    @Test
    void testAdjustCheckOutTime_Success() {
        User user = mock(User.class);
        CheckOut checkOut = new CheckOut();
        Employee employee = new Employee();
        employee.setId(1L);
        checkOut.setEmployee(employee);
        checkOut.setId(1L);

        when(userService.getCurrentUser()).thenReturn(user);
        when(user.isCheckOutAdjustmentAllowed(checkOut)).thenReturn(true);
        when(checkOutRepo.findById(1L)).thenReturn(Optional.of(checkOut));

        CheckOutAdjustmentDTO dto = new CheckOutAdjustmentDTO();
        dto.setNewCheckOutDateTime(LocalDateTime.now());

        CheckOut result = checkoutService.adjustCheckOutTime(1L, dto);

        verify(checkOutRepo).save(checkOut);
        assertNotNull(result);
    }
    @Test
    void testAdjustCheckOutTime_Unauthorized_ThrowsException() {
        User user = mock(User.class);
        CheckOut checkOut = new CheckOut();
        checkOut.setId(1L);

        when(userService.getCurrentUser()).thenReturn(user);
        when(user.isCheckOutAdjustmentAllowed(checkOut)).thenReturn(false);
        when(checkOutRepo.findById(1L)).thenReturn(Optional.of(checkOut));

        CheckOutAdjustmentDTO dto = new CheckOutAdjustmentDTO();
        dto.setNewCheckOutDateTime(LocalDateTime.now());

        assertThrows(IllegalStateException.class, () -> checkoutService.adjustCheckOutTime(1L, dto));
    }

}