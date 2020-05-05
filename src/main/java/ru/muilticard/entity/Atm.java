package ru.muilticard.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Data
@NoArgsConstructor
public class Atm {

    @Id
    @NonNull private String id;
    @NonNull private String serialNumber;
    @NonNull private String bankName;
}
