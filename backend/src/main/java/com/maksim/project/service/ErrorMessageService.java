package com.maksim.project.service;

import com.maksim.project.model.ErrorMessage;
import com.maksim.project.repository.ErrorMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class ErrorMessageService {

    @Autowired
    private ErrorMessageRepository errorMessageRepository;

    // Metoda za dohvatanje svih ErrorMessage
    public Page<ErrorMessage> getAllErrorMessages(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        return errorMessageRepository.findAll(pageRequest);
    }

    // Metoda za dohvatanje ErrorMessage po userId
    public Page<ErrorMessage> getErrorMessagesByUserId(Long userId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        return errorMessageRepository.findByUserId(userId, pageRequest);
    }
}