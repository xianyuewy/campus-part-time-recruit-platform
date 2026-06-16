package org.example.col_stu_ptj_sys.ai.service;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.service.AiServices;
import org.example.col_stu_ptj_sys.ai.util.RecruitTool;
import org.springframework.stereotype.Service;

@Service
public class AiAgentService {

    private final ChatAgent chatAgent;

    // 构造注入：模型、记忆、检索器、工具类，全部和 AiConfig 里的 Bean 对应
    public AiAgentService(OpenAiChatModel chatModel,
                          ChatMemory chatMemory,
                          ContentRetriever contentRetriever,
                          RecruitTool recruitTool) {

        // 用自定义的 ChatAgent 接口构建代理
        this.chatAgent = AiServices.builder(ChatAgent.class)
                .chatLanguageModel(chatModel)
                .chatMemory(chatMemory)
                .contentRetriever(contentRetriever)
                .tools(recruitTool)
                .build();
    }

    // 对外对话方法
    public String chat(Long userId, String question) {
        // 拼接用户ID到问题中，让工具调用时可以识别用户
        String fullQuestion = "当前登录用户ID：" + userId + "\n用户问题：" + question;
        return chatAgent.chat(fullQuestion);
    }
}