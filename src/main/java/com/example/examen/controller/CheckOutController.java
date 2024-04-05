package com.example.examen.controller;

import com.example.examen.dto.CheckOutAdjustmentDTO;
import com.example.examen.service.CheckoutService;
import com.example.examen.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/check-out")
@RequiredArgsConstructor
@CrossOrigin(origins= "*")
public class CheckOutController {
    private final CheckoutService checkOutService;
    private final UserService userService;

    // check out
    @PostMapping("/")
    public ResponseEntity<?> checkOut() {
        checkOutService.checkOut();
        return ResponseEntity.ok("Checked out successfully");
    }
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllEmployeeCheckOuts() {
        return ResponseEntity.ok(checkOutService.getAllEmployeeCheckOuts());
    }

    @GetMapping("/emp-check-outs")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getCheckOutsByEmployeeId() {
        return ResponseEntity.ok(checkOutService.getCheckOutsByEmployeeId());
    }

    @PutMapping("/{checkOutId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<?> adjustCheckOutTime(@PathVariable Long checkOutId, @RequestBody CheckOutAdjustmentDTO adjustmentDTO) {
        if (userService.getCurrentUser() == null) {
            return ResponseEntity.badRequest().body("User not found");
        }
        checkOutService.adjustCheckOutTime(checkOutId, adjustmentDTO);
        return ResponseEntity.ok("Check-out time adjusted successfully");
    }


}
