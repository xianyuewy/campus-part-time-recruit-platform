package org.example.col_stu_ptj_sys.dto.company;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ScheduleInterviewRequest {

    @NotNull(message = "请填写面试时间")
    @Future(message = "面试时间必须晚于当前时间")
    private LocalDateTime interviewTime;

    @NotBlank(message = "请填写面试地点")
    @Size(max = 200, message = "面试地点不能超过 200 字")
    private String interviewLocation;

    @Size(max = 500, message = "面试说明不能超过 500 字")
    private String interviewNote;
}
