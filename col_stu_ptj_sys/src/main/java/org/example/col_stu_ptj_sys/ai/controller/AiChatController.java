package org.example.col_stu_ptj_sys.ai.controller;

import org.example.col_stu_ptj_sys.ai.service.AiAgentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class AiChatController {

    private final AiAgentService aiAgentService;

    public AiChatController(AiAgentService aiAgentService) {
        this.aiAgentService = aiAgentService;
    }

    @GetMapping("/chat")
    public String chat(@RequestParam Long userId,
                       @RequestParam String question) {
        return aiAgentService.chat(userId, question);
    }
}