package com.example.examen.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "employees")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String address;
    private String city;
    private String jobTitle;
    @OneToOne(mappedBy = "employee", cascade = CascadeType.ALL)
    private User user;
    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL)
    private List<LeaveRequest> leaveRequests;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL)
    private List<CheckIn> checkIns;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL)
    private List<CheckOut> checkOuts;
    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL)
    private List<Salary> salaries;
}
