package com.example.examen.adminDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CheckInInfoDTO {
    private String employeeName;
    private LocalDateTime checkInTime;
}
