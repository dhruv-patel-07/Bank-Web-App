package com.bank.web.app.account.repo;

import com.bank.web.app.account.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepo extends JpaRepository<Employee,String> {

    Employee findByEmployeeId(String id);
}
