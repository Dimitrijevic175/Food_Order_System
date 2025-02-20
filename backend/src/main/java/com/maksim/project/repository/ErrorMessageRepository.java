package com.maksim.project.repository;

import com.maksim.project.model.ErrorMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ErrorMessageRepository extends JpaRepository<ErrorMessage,Long> {
    // Metoda za filtriranje ErrorMessage po userId
    Page<ErrorMessage> findByUserId(Long userId, Pageable pageable);

}
