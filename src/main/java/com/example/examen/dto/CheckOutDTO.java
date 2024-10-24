package com.example.examen.dto;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CheckOutDTO {
    private Long id;
    @Column(nullable = false)
    private LocalDateTime checkOutDateTime;
}
