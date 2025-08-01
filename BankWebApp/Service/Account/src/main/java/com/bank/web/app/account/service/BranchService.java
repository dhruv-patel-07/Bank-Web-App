package com.bank.web.app.account.service;

import com.bank.web.app.account.dto.BranchDTO;
import com.bank.web.app.account.dto.ResponseDTO;
import com.bank.web.app.account.model.Branch;
import com.bank.web.app.account.model.Employee;
import com.bank.web.app.account.repo.BranchRepo;
import com.bank.web.app.account.repo.EmployeeRepo;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BranchService {

    @Autowired
    private BranchRepo branchRepo;

    @Autowired
    private EmployeeRepo employeeRepo;

    public ResponseDTO addNewBranch(BranchDTO branchDTO) {
        Branch branch = new Branch();
        branch.setBranchCode(branchDTO.getBranchCode());
        branch.setBranchName(branchDTO.getBranchName());
        branch.setAddress(branchDTO.getAddress());
        branch.setPhoneNumber(branchDTO.getPhoneNumber());
        branchRepo.save(branch);

        return new ResponseDTO("200","Ok",null);

    }

    public String findBranchNameById(String uid) {

        Employee employee = employeeRepo.findByEmployeeId(uid);
        return employee.getBranch().getBranchName();
    }
}
