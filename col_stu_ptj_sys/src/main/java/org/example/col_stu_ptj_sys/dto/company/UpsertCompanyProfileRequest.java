package org.example.col_stu_ptj_sys.dto.company;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpsertCompanyProfileRequest {
    @NotBlank
    private String companyName;
    private String licenseNo;
    private String contactName;
    private String contactPhone;
    private String description;
}
