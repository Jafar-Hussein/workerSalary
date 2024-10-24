package com.example.examen.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.YearMonth;

@Converter(autoApply = true) // Gör konverteraren tillgänglig automatiskt för alla YearMonth-attribut
public class YearMonthStringConverter implements AttributeConverter<YearMonth, String> {

    // Konvertera YearMonth till en sträng för lagring i databasen
    @Override
    public String convertToDatabaseColumn(YearMonth attribute) {
        return (attribute == null ? null : attribute.toString()); // Om YearMonth är null returnera null, annars YearMonth som en sträng
    }

    // Konvertera sträng från databasen tillbaka till YearMonth i entiteten
    @Override
    public YearMonth convertToEntityAttribute(String dbData) {
        return (dbData == null ? null : YearMonth.parse(dbData)); // Om strängen är null returnera null, annars konvertera sträng till YearMonth
    }
}
