package org.example.col_stu_ptj_sys.ai.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.example.col_stu_ptj_sys.dto.PageResponse;
import org.example.col_stu_ptj_sys.dto.student.StudentApplicationVO;
import org.example.col_stu_ptj_sys.service.student.StudentJobService;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RecruitTool {

    private static final int MAX_RECORDS = 20;

    private final StudentJobService studentJobService;
    private final ObjectMapper objectMapper;

    public RecruitTool(StudentJobService studentJobService, ObjectMapper objectMapper) {
        this.studentJobService = studentJobService;
        this.objectMapper = objectMapper;
    }

    /**
     * 根据学生用户ID查询简历投递记录
     */
    @Tool("查询指定学生用户的简历投递记录，包含岗位名称、投递状态、面试信息等")
    public String queryDeliveryRecord(
            @P(value = "userId") String userId
    ) {
        log.info("[工具调用] 触发投递记录查询，原始userId字符串={}", userId);

        // 字符串转Long，手动处理格式和精度
        Long userIdLong;
        try {
            userIdLong = Long.parseLong(userId.trim());
        } catch (NumberFormatException e) {
            return "参数错误：用户ID格式无效，请传入正确的数字ID";
        }

        if (userIdLong <= 0) {
            return "参数错误：用户ID不能为空且必须为正整数";
        }

        try {
            PageResponse<StudentApplicationVO> page = studentJobService.listApplicationsByStudentUserId(
                    userIdLong, 1, MAX_RECORDS);

            if (page.getRecords() == null || page.getRecords().isEmpty()) {
                return String.format("用户ID：%d 暂无简历投递记录", userIdLong);
            }

            return objectMapper.writeValueAsString(page.getRecords());
        } catch (JsonProcessingException e) {
            log.error("序列化用户{}投递记录失败", userIdLong, e);
            return "查询成功，但结果序列化失败，请稍后重试";
        } catch (Exception e) {
            log.error("查询用户{}投递记录失败", userIdLong, e);
            return String.format("查询失败：%s", e.getMessage());
        }
    }
}