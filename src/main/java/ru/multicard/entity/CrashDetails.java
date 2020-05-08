package ru.multicard.entity;

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
public class CrashDetails {

    @Id
    @NonNull
    private Long Id;

    private String atmId;
    private String atmSerialNumber;
    private String bankName;
    private String reason;
    @Column(name = "begin_date")
    private LocalDateTime begin;
    @Column(name = "end_date")
    private LocalDateTime end;
    private String channel;
}
