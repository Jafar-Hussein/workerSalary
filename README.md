# WorkerSalary

## Projektbeskrivning

WorkerSalary är en applikation som hanterar tidrapportering och lön för anställda. Systemet tillåter användare att checka in och checka ut, registrera ledighetsansökningar, och hantera löneberäkningar baserat på arbetade timmar och timlön. Applikationen är utformad för att användas både av administratörer och vanliga användare.

## Funktioner

- **Check-in och check-out**: Anställda kan checka in och checka ut för att registrera arbetade timmar.
- **Löneberäkning**: Lön beräknas automatiskt baserat på timlön och arbetade timmar för varje anställd.
- **Ledighetsansökningar**: Anställda kan registrera, uppdatera och hantera sina ledighetsansökningar.
- **Administratörshantering**: Administratörer kan hantera anställdas information, löner, och godkänna ledighetsansökningar.
- **JWT-baserad autentisering**: Säkerhet implementeras med JWT (JSON Web Tokens) för autentisering och auktorisering.

## Teknisk Stack

- **Backend**: Spring Boot
- **Säkerhet**: Spring Security, JWT
- **Databas**: MySQL
- **Byggverktyg**: Maven
- **Övriga teknologier**:
    - ModelMapper för DTO-konvertering
    - BCrypt för lösenordskryptering
    - JPA för databasinteraktion

## Installation

### Förutsättningar
- Java 17
- MySQL
- Maven

### Steg för att köra applikationen

1. Klona detta repository:

   ```bash
   git clone https://github.com/ditt-anvandar-namn/WorkerSalary.git
   cd WorkerSalary
   
2. Skapa en MySQL-databas och uppdatera `application.properties` med dina databasuppgifter:

   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/worker_salary
   spring.datasource.username=root
   spring.datasource.password=root
   ```
   
4. Bygg och kör applikationen med Maven:
```properties
   mvn clean install
   mvn spring-boot:run

  ```
5. API Endpoints
## Autentisering

### POST `/auth/register`
- **Beskrivning**: Registrera en ny användare (Endast för admin).
- **Säkerhet**: `hasRole('ADMIN')`
- **Kropp**: `AuthRequest`

### POST `/auth/login`
- **Beskrivning**: Logga in och generera JWT-token.
- **Säkerhet**: Ingen
- **Kropp**: `AuthRequest`

---

## Check-In

### POST `/check-in/`
- **Beskrivning**: Checka in användaren för arbetspass.
- **Säkerhet**: `hasRole('USER') eller 'ADMIN'`

### GET `/check-in/all`
- **Beskrivning**: Hämta alla anställdas incheckningsinformation (Endast för admin).
- **Säkerhet**: `hasRole('ADMIN')`

### GET `/check-in/emp-check-ins`
- **Beskrivning**: Hämta inloggad användares incheckningshistorik.
- **Säkerhet**: `hasRole('USER')`

### PUT `/check-in/{checkInId}`
- **Beskrivning**: Justera incheckningstid för en specifik incheckning.
- **Säkerhet**: `hasAnyRole('ADMIN', 'USER')`
- **Kropp**: `CheckInAdjustmentDTO`

---

## Check-Out

### POST `/check-out/`
- **Beskrivning**: Checka ut användaren från arbetspass.
- **Säkerhet**: `hasRole('USER') eller 'ADMIN'`

### GET `/check-out/all`
- **Beskrivning**: Hämta alla anställdas utcheckningsinformation (Endast för admin).
- **Säkerhet**: `hasRole('ADMIN')`

### GET `/check-out/emp-check-outs`
- **Beskrivning**: Hämta inloggad användares utcheckningshistorik.
- **Säkerhet**: `hasRole('USER')`

### PUT `/check-out/{checkOutId}`
- **Beskrivning**: Justera utcheckningstid för en specifik utcheckning.
- **Säkerhet**: `hasAnyRole('ADMIN', 'USER')`
- **Kropp**: `CheckOutAdjustmentDTO`

---

## Anställd

### POST `/employee/add`
- **Beskrivning**: Lägg till ny anställd (Endast för admin).
- **Säkerhet**: `hasRole('ADMIN')`
- **Kropp**: `EmployeeCreationDTO`

### GET `/employee/current`
- **Beskrivning**: Hämta information om inloggad användare.
- **Säkerhet**: `hasAnyRole('ADMIN', 'USER')`

### GET `/employee/admin-all`
- **Beskrivning**: Hämta alla anställdas information (Endast för admin).
- **Säkerhet**: `hasRole('ADMIN')`

### GET `/employee/all`
- **Beskrivning**: Hämta alla anställdas information (Tillgänglig för både användare och admin).
- **Säkerhet**: `hasAnyRole('ADMIN', 'USER')`

### PUT `/employee/user-update`
- **Beskrivning**: Uppdatera inloggad användares information.
- **Säkerhet**: `hasAnyRole('ADMIN', 'USER')`
- **Kropp**: `EmployeeDTO`

### PUT `/employee/admin-update/{id}`
- **Beskrivning**: Uppdatera information för en specifik anställd (Endast för admin).
- **Säkerhet**: `hasRole('ADMIN')`
- **Kropp**: `AdminEmployeeDTO`

### DELETE `/employee/delete/{id}`
- **Beskrivning**: Ta bort en anställd och den associerade användaren (Endast för admin).
- **Säkerhet**: `hasRole('ADMIN')`

---

## Lön

### GET `/salary/user-salary`
- **Beskrivning**: Hämta aktuell månadslön för inloggad användare.
- **Säkerhet**: `hasAnyRole('ADMIN', 'USER')`

### GET `/salary/user-salaries`
- **Beskrivning**: Hämta tidigare löner för inloggad användare.
- **Säkerhet**: `hasAnyRole('ADMIN', 'USER')`

### POST `/salary/set-salary/{employeeId}`
- **Beskrivning**: Sätt löneinformation för en specifik anställd (Endast för admin).
- **Säkerhet**: `hasRole('ADMIN')`
- **Kropp**: `SalaryDTO`

### PUT `/salary/{hourlyRate}`
- **Beskrivning**: Uppdatera timlön för en specifik anställd (Endast för admin).
- **Säkerhet**: `hasRole('ADMIN')`

---

## Ledighetsansökningar

### POST `/leave-request/create`
- **Beskrivning**: Skapa en ny ledighetsansökan för användare.
- **Säkerhet**: `hasRole('USER')`
- **Kropp**: `LeaveRequestDTO`

### PUT `/leave-request/{leaveRequestId}/dates`
- **Beskrivning**: Uppdatera datum för en ledighetsansökan.
- **Säkerhet**: `hasRole('USER')`
- **Kropp**: `LeaveRequestDTO`

### PUT `/leave-request/{leaveRequestId}/status`
- **Beskrivning**: Uppdatera status för en ledighetsansökan (Endast för admin).
- **Säkerhet**: `hasRole('ADMIN')`
- **Query Param**: `status`

### GET `/leave-request/all`
- **Beskrivning**: Hämta alla ledighetsansökningar (Endast för admin).
- **Säkerhet**: `hasRole('ADMIN')`

### GET `/leave-request/employee-request`
- **Beskrivning**: Hämta alla ledighetsansökningar för inloggad användare.
- **Säkerhet**: `hasAnyRole('ADMIN', 'USER')`

---
## Säkerhet
- JWT (JSON Web Tokens) används för autentisering.
- Användare får sina roller som administratör eller användare via JWT-token.
