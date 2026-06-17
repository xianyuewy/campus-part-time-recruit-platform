package org.example.col_stu_ptj_sys.ai.service;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.col_stu_ptj_sys.dto.student.ResumeParseResult;
import org.example.col_stu_ptj_sys.entity.StudentResume;
import org.example.col_stu_ptj_sys.mapper.StudentResumeMapper;
import org.example.col_stu_ptj_sys.util.ResumeTextExtractor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ResumeParseServiceImpl {

    private final AiAgentService aiService;
    private final StudentResumeMapper studentResumeMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();
    // 任务结果缓存
    private final Map<String, ResumeParseResult> taskCache = new ConcurrentHashMap<>();

    public ResumeParseServiceImpl(AiAgentService aiService, StudentResumeMapper studentResumeMapper) {
        this.aiService = aiService;
        this.studentResumeMapper = studentResumeMapper;
    }

    /**
     * 原有同步解析方法，完全保留不动
     */
    public ResumeParseResult parseResume(Long userId, MultipartFile file) throws IOException {
        String resumeText = ResumeTextExtractor.extractText(file);
        return parseResumeFromText(userId, resumeText);
    }

    /**
     * 方法2：解析简历并自动保存到数据库（一键生成在线简历）
     * 自动保存附件、映射字段、更新/新增用户简历
     */
    public StudentResume parseAndSaveResume(Long userId, MultipartFile file) throws IOException {
        // 1. 解析简历结构化数据
        ResumeParseResult parseResult = parseResume(userId, file);

        // 2. 保存简历附件到本地uploads目录（对齐你现有命名规则）
        String originalFileName = file.getOriginalFilename();
        String suffix = originalFileName.substring(originalFileName.lastIndexOf("."));
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String fileName = userId + "_" + uuid + suffix;
        // 保存路径：项目根目录/uploads/resumes/
        String savePath = System.getProperty("user.dir") + "/uploads/resumes/";
        File saveDir = new File(savePath);
        if (!saveDir.exists()) {
            saveDir.mkdirs();
        }
        file.transferTo(new File(savePath + fileName));
        String attachmentPath = "/uploads/resumes/" + fileName;

        // 3. 字段映射：AI解析结果 → 你的StudentResume数据库实体
        StudentResume resume = new StudentResume();
        resume.setUserId(userId);
        resume.setEducation(parseResult.getSchool()); // 院校对应education字段
        resume.setSkills(String.join("、", parseResult.getSkills())); // 技能数组转顿号分隔字符串
        resume.setWorkExperience(parseResult.getExperience()); // 经历对应workExperience
        resume.setSelfIntro(parseResult.getSelfEvaluation()); // 自我评价对应selfIntro
        resume.setAttachmentPath(attachmentPath);
        resume.setAttachmentOriginalName(originalFileName);

        // 4. 判断用户是否已有简历，有则更新，无则新增
        LambdaQueryWrapper<StudentResume> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudentResume::getUserId, userId);
        StudentResume existResume = studentResumeMapper.selectOne(wrapper);
        if (existResume != null) {
            resume.setId(existResume.getId());
            studentResumeMapper.updateById(resume);
        } else {
            studentResumeMapper.insert(resume);
        }

        return resume;
    }

    // ========== 抽离：纯文本解析核心，同步异步复用 ==========
    private ResumeParseResult parseResumeFromText(Long userId, String resumeText) throws IOException {
        String prompt = "你是专业的简历解析助手，现在给你一份大学生兼职求职简历的纯文本内容，请你从中提取以下信息，严格输出JSON格式，不要多余文字，字段缺失填空字符串。\n" +
                "必须提取的字段：\n" +
                "{\n" +
                "  \"name\": \"姓名\",\n" +
                "  \"gender\": \"性别\",\n" +
                "  \"phone\": \"手机号\",\n" +
                "  \"email\": \"邮箱\",\n" +
                "  \"school\": \"毕业院校\",\n" +
                "  \"major\": \"专业\",\n" +
                "  \"grade\": \"年级/学历\",\n" +
                "  \"availableTime\": \"可上岗时间/每周可工作时长\",\n" +
                "  \"expectSalary\": \"期望薪资/日薪\",\n" +
                "  \"skills\": [\"掌握的技能列表，数组格式\"],\n" +
                "  \"experience\": \"实习/兼职/项目经历摘要，100字以内\",\n" +
                "  \"selfEvaluation\": \"自我评价摘要，50字以内\"\n" +
                "}\n" +
                "注意：\n" +
                "1. 只输出纯JSON，不要markdown代码块、不要解释文字\n" +
                "2. 找不到的字段填空字符串或空数组\n" +
                "3. 技能字段拆分成数组，每个技能一个元素\n" +
                "4. 针对兼职场景，重点提取可工作时间、期望薪资、专业、技能\n" +
                "\n\n简历内容：\n" + resumeText;

        String aiResponse = aiService.chat(userId, prompt);
        aiResponse = aiResponse.replace("```json", "").replace("```", "").trim();
        return objectMapper.readValue(aiResponse, ResumeParseResult.class);
    }

    // ========== 修复：异步方法接收字节数组 ==========
    @Async
    public void asyncParse(String taskId, Long userId, byte[] fileBytes, String fileName) {
        try {
            String resumeText = ResumeTextExtractor.extractFromBytes(fileBytes, fileName);
            ResumeParseResult result = parseResumeFromText(userId, resumeText);
            taskCache.put(taskId, result);
        } catch (Exception e) {
            e.printStackTrace(); // 控制台打印异常，方便排查
            taskCache.put(taskId, null); // 标记解析失败
        }
    }

    public ResumeParseResult getResult(String taskId) {
        return taskCache.get(taskId);
    }
}