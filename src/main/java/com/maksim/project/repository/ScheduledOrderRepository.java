package com.maksim.project.repository;

import com.maksim.project.model.ScheduledOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
@Repository
public interface ScheduledOrderRepository extends JpaRepository<ScheduledOrder, Long> {

    List<ScheduledOrder> findByExecutedFalseAndScheduledTimeBefore(LocalDateTime time);

    // Metoda koja broji zakazane porudžbine koje nisu izvršene (executed = false)
    long countByScheduledTimeAndExecutedFalse(LocalDateTime scheduledTime);

}
