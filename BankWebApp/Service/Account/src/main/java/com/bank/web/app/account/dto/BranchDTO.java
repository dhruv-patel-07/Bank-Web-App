package com.bank.web.app.account.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class BranchDTO{
        @NotBlank(message = "Branch name is required")
        private String branchName;
        @NotBlank(message = "Branch Code is required")
        private String branchCode;
        @NotBlank(message = "Address is required")
        private String address;
        @Pattern(regexp = "^\\+91\\d{10}$",message = "Contact Number Not Valid")
        private String phoneNumber;

}
