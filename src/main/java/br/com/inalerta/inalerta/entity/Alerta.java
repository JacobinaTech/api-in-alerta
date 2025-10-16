package br.com.inalerta.inalerta.entity;

import com.google.cloud.firestore.annotation.Exclude;
import com.opencsv.bean.CsvBindByPosition;
import com.opencsv.bean.CsvCustomBindByPosition;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
public class Alerta {

    @CsvBindByPosition(position = 0)
    private Double latitude;

    @CsvBindByPosition(position = 1)
    private Double longitude;

    @CsvBindByPosition(position = 2)
    private String satelite;


    @CsvCustomBindByPosition(position = 3, converter = LocalDateTimeConverter.class)
    private transient LocalDateTime dataHora; // Trocamos @Exclude por transient


    public String getData() {
        if (this.dataHora == null) {
            return null;
        }
        // Formata a data para um padrão universal (ISO 8601), ideal para bancos de dados.
        return this.dataHora.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    @Override
    public String toString() {
        return "Alerta{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                ", satelite='" + satelite + '\'' +
                ", data=" + getData() +
                '}';
    }
}

