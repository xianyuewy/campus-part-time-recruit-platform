package org.example.col_stu_ptj_sys.dto.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SendApplicationMessageRequest {

    @NotBlank(message = "消息内容不能为空")
    @Size(max = 2000, message = "单条消息不超过2000字")
    private String content;
}
