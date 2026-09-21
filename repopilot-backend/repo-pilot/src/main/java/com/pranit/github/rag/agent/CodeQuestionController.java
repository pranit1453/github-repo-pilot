package com.pranit.github.rag.agent;

import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/repositories")
public class CodeQuestionController {

    private final CodeQuestionService service;

    public CodeQuestionController(
            CodeQuestionService service) {

        this.service = service;
    }

    @PostMapping("/{repositoryId}/ask")
    public AnswerResponse ask(
            @PathVariable UUID repositoryId,
            @RequestBody QuestionRequest request) {

        return new AnswerResponse(
                service.ask(
                        repositoryId,
                        request.question()));
    }

    public record QuestionRequest(
            @NotBlank String question) {
    }

    public record AnswerResponse(
            String answer) {
}