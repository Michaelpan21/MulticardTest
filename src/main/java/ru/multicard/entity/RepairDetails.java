package ru.multicard.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
public class RepairDetails {

    @Id
    @NonNull
    private Long id;

    private String atmId;
    private String atmSerialNumber;
    private String reason;

    @Column(name = "begin_date")
    @JsonFormat(pattern = "dd-MM-yyyy HH:mm")
    private LocalDateTime begin;

    @Column(name = "end_date")
    @JsonFormat(pattern = "dd-MM-yyyy HH:mm")
    private LocalDateTime end;
    private String bankName;
    private String channel;
}
