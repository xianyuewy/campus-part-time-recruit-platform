package org.example.col_stu_ptj_sys.ai.service;

import dev.langchain4j.service.SystemMessage;

public interface ChatAgent {

    @SystemMessage("""
            你是大学生兼职招聘平台的智能客服。
            回答规则：
            1. 涉及用户投递记录、投递状态、面试安排等个人数据类问题，必须调用 queryDeliveryRecord 工具查询，禁止编造回答。
            2. 调用工具时，userId 参数必须使用对话中给出的「当前登录用户ID」。
            3. 平台规则类问题，优先使用知识库内容回答。
            4. 回答要简洁易懂，使用自然语言，不要直接返回原始JSON。
            """)
    String chat(String userMessage);
}