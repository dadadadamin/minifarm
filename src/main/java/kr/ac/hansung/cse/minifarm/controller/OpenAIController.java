package kr.ac.hansung.cse.minifarm.controller;

import kr.ac.hansung.cse.minifarm.service.OpenAIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/openai")
public class OpenAIController {

    private final OpenAIService openAIService;

    @Autowired
    public OpenAIController(OpenAIService openAIService) {
        this.openAIService = openAIService;
    }

    @PostMapping("/diagnose")
    public String diagnose(@RequestBody String description) {
        return openAIService.analyzePlantDisease(description);
    }
}
