package ru.muilticard.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
public class AtmRepair {

    @Id
    @NonNull
    private Long Id;

    @ManyToOne
    @JoinColumn(name = "atm_id")
    @NonNull
    private Atm atm;

    private String reason;
    @Column(name = "begin_date")
    private LocalDateTime begin;
    @Column(name = "end_date")
    private LocalDateTime end;
    private String pipe;
}
