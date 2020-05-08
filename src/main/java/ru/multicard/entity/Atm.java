package ru.multicard.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Data
@NoArgsConstructor
public class Atm {

    @Id
    private String id;
    private String serialNumber;
    private String bankName;

}
