package com.bank.web.app.account.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class Employee {
    @Id
    private String employeeId;
    private String position;
    @ManyToOne
    @JoinColumn(name = "branch_id")
    private Branch branch;
}
