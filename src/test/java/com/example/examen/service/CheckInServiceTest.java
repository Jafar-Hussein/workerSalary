package com.example.examen.service;

import com.example.examen.adminDTO.CheckInInfoDTO;
import com.example.examen.dto.CheckInAdjustmentDTO;
import com.example.examen.dto.CheckInDTO;
import com.example.examen.model.CheckIn;
import com.example.examen.model.Employee;
import com.example.examen.model.User;
import com.example.examen.repo.CheckInRepo;
import com.example.examen.repo.CheckOutRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CheckInServiceTest {
    @Mock
    private CheckInRepo checkInRepo;

    @Mock
    private UserService userService;

    @Mock
    private EmployeeService employeeService;

    @Mock
    private CheckOutRepo checkOutRepo;

    @Mock
    private SalaryService salaryService;

    @InjectMocks
    private CheckInService checkInService;
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }
    @Test
    void testCheckIn_Success() {
        User user = mock(User.class);
        Employee employee = mock(Employee.class);
        when(user.getEmployee()).thenReturn(employee);
        when(userService.getCurrentUser()).thenReturn(user);

        checkInService.checkIn();

        verify(checkInRepo).save(any(CheckIn.class));
    }

    @Test
    void testCheckIn_UserNotFound_ThrowsException() {
        when(userService.getCurrentUser()).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> {
            checkInService.checkIn();
        });
    }


    @Test
    void testGetAllEmployeeCheckIns() {
        Employee employee1 = new Employee();
        employee1.setFirstName("John");
        employee1.setLastName("Doe");
        Employee employee2 = new Employee();
        employee2.setFirstName("Jane");
        employee2.setLastName("Doe");

        CheckIn checkIn1 = new CheckIn();
        checkIn1.setEmployee(employee1);
        CheckIn checkIn2 = new CheckIn();
        checkIn2.setEmployee(employee2);

        List<CheckIn> checkIns = List.of(checkIn1, checkIn2);
        when(checkInRepo.findAll()).thenReturn(checkIns);

        List<CheckInInfoDTO> result = checkInService.getAllEmployeeCheckIns();

        assertEquals(2, result.size());
        assertEquals("John Doe", result.get(0).getEmployeeName());
        assertEquals("Jane Doe", result.get(1).getEmployeeName());
    }



    @Test
    void testGetCheckInsByEmployeeId_Success() {
        User user = mock(User.class);
        Employee employee = mock(Employee.class);
        when(employee.getId()).thenReturn(1L);
        when(user.getEmployee()).thenReturn(employee);
        when(userService.getCurrentUser()).thenReturn(user);
        List<CheckIn> checkIns = List.of(new CheckIn(), new CheckIn());
        when(checkInRepo.findAllByEmployeeId(1L)).thenReturn(checkIns);

        List<CheckInDTO> result = checkInService.getCheckInsByEmployeeId();

        assertEquals(2, result.size());
    }
    @Test
    void testGetCheckInsByEmployeeId_UserNotFound_ThrowsException() {
        when(userService.getCurrentUser()).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> {
            checkInService.getCheckInsByEmployeeId();
        });
    }
    @Test
    void testAdjustCheckInTime_Success() {
        Employee employee = new Employee();
        employee.setId(1L);

        CheckIn checkIn = new CheckIn();
        checkIn.setId(1L);
        checkIn.setEmployee(employee);

        User user = mock(User.class);
        when(user.isCheckInAdjustmentAllowed(checkIn)).thenReturn(true);
        when(checkInRepo.findById(1L)).thenReturn(Optional.of(checkIn));
        when(userService.getCurrentUser()).thenReturn(user);

        CheckInAdjustmentDTO dto = new CheckInAdjustmentDTO();
        dto.setNewCheckInDateTime(LocalDateTime.now());

        CheckIn result = checkInService.adjustCheckInTime(1L, dto);

        verify(checkInRepo).save(checkIn);
        assertNotNull(result);
        assertEquals(1L, result.getEmployee().getId());
    }

    @Test
    void testAdjustCheckInTime_Unauthorized_ThrowsException() {
        CheckIn checkIn = new CheckIn();
        checkIn.setId(1L);
        User user = mock(User.class);
        when(user.isCheckInAdjustmentAllowed(checkIn)).thenReturn(false);
        when(checkInRepo.findById(1L)).thenReturn(Optional.of(checkIn));
        when(userService.getCurrentUser()).thenReturn(user);

        CheckInAdjustmentDTO dto = new CheckInAdjustmentDTO();
        dto.setNewCheckInDateTime(LocalDateTime.now());

        assertThrows(IllegalStateException.class, () -> {
            checkInService.adjustCheckInTime(1L, dto);
        });
    }


}