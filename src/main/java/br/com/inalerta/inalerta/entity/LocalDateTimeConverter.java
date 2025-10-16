package br.com.inalerta.inalerta.entity;

import com.opencsv.bean.AbstractBeanField;
import com.opencsv.exceptions.CsvConstraintViolationException;
import com.opencsv.exceptions.CsvDataTypeMismatchException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class LocalDateTimeConverter extends AbstractBeanField<LocalDateTime, String> {

    @Override
    protected Object convert(String value) throws CsvDataTypeMismatchException, CsvConstraintViolationException {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            // Define o formato esperado da data no CSV
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            return LocalDateTime.parse(value, formatter);
        } catch (DateTimeParseException e) {
            // Lança uma exceção que o OpenCSV entende
            throw new CsvDataTypeMismatchException(value, LocalDateTime.class, "Falha ao converter a data. Formato esperado: 'yyyy-MM-dd HH:mm:ss'");
        }
    }
}
