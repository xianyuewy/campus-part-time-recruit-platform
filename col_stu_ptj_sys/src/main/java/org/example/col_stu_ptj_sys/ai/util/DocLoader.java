package org.example.col_stu_ptj_sys.ai.util;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;

@Component
public class DocLoader {

    private final EmbeddingStore<TextSegment> embeddingStore;
    private final EmbeddingModel embeddingModel;

    public DocLoader(EmbeddingStore<TextSegment> embeddingStore, EmbeddingModel embeddingModel) {
        this.embeddingStore = embeddingStore;
        this.embeddingModel = embeddingModel;
    }

    // 项目启动自动加载知识库
    @PostConstruct
    public void loadDoc() {
        // 兼职平台规则、常见问题（可自行扩充）
        String rule = "1. 学生可免费浏览岗位、投递简历；2. 企业发布岗位需完成资质审核；" +
                "3. 简历投递后不可重复投递同一岗位；4. 可在个人中心查看投递记录；" +
                "5. 违规内容会被系统自动拦截。";

        // 修复1：用 Document.from() 替代不存在的 StringDocumentLoader
        Document document = Document.from(rule);

        // 文档切块
        var splitter = DocumentSplitters.recursive(300, 50);
        var segments = splitter.split(document);

        // 修复2：embedAll 返回 Response 包装对象，取 .content() 拿到向量列表
        var embeddingResponse = embeddingModel.embedAll(segments);
        embeddingStore.addAll(embeddingResponse.content(), segments);
    }
}