package com.example.examen;

import com.example.examen.dto.EmployeeDTO;
import com.example.examen.model.*;
import com.example.examen.repo.EmployeeRepo;
import com.example.examen.repo.SalaryRepo;
import com.example.examen.repo.UserRepo;
import com.example.examen.service.CheckInService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.YearMonth;

@SpringBootApplication
public class ExamenApplication {

	public static void main(String[] args) {
		SpringApplication.run(ExamenApplication.class, args);
	}

	@Bean
	CommandLineRunner run(PasswordEncoder encoder, UserRepo userRepo, EmployeeRepo employeeRepo, SalaryRepo salaryRepo){
		return args -> {
			if (userRepo.existsByUsername("admin")) {
				return;
			}
			// Create a new user
			User user = new User();
			user.setUsername("admin");
			user.setPassword(encoder.encode("admin"));
			user.setRole(Roles.ADMIN);
// Create a new employee and link it to the user
			Employee adminEmployee = new Employee();
			adminEmployee.setFirstName("John");
			adminEmployee.setLastName("Doe");
			adminEmployee.setAddress("123 Main St");
			adminEmployee.setCity("Springfield");
			adminEmployee.setJobTitle("Manager");
			adminEmployee.setEmail("admin@example.com");
			adminEmployee.setPhone("1234567890");

			// Linking the user and employee
			adminEmployee.setUser(user); // Assuming the Employee entity has a setUser method
			user.setEmployee(adminEmployee); // You'll need to add a field + setter in the User class for Employee

			// Save the employee and user
			employeeRepo.save(adminEmployee); // This should cascade and save adminUser too, if cascading is properly set up
			// set employye salary
			Salary salary = new Salary();
			salary.setEmployee(adminEmployee);
			salary.setMonth(YearMonth.now());
			salary.setHourlyRate(BigDecimal.valueOf(2000.00));
			salary.setWorkedHours(2);
			salary.setTotalSalary(BigDecimal.valueOf(salary.getWorkedHours()).multiply(salary.getHourlyRate()));
			salaryRepo.save(salary);
		};

		};
	}

