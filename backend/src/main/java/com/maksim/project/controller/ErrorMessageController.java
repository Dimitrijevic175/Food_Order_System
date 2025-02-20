package com.maksim.project.controller;

import com.maksim.project.model.ErrorMessage;
import com.maksim.project.service.ErrorMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/errors")
public class ErrorMessageController {

    @Autowired
    private ErrorMessageService errorMessageService;

    // Endpoint za dohvatanje svih ErrorMessage
    @GetMapping
    public Page<ErrorMessage> getAllErrorMessages(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return errorMessageService.getAllErrorMessages(page, size);
    }

    // Endpoint za dohvatanje ErrorMessage po userId
    @GetMapping("/user")
    public Page<ErrorMessage> getErrorMessagesByUserId(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return errorMessageService.getErrorMessagesByUserId(userId, page, size);
    }
}
