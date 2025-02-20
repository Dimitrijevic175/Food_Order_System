package com.maksim.project.repository;

import com.maksim.project.model.Order;
import com.maksim.project.model.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
@Repository
public interface OrderRepository extends JpaRepository<Order,Long> {

    List<Order> findByCreatedBy(Long userId);

    List<Order> findByStatusIn(List<Status> statuses);

    List<Order> findOrdersByCreatedDateBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT o FROM Order o WHERE o.active = true")
    List<Order> findOrdersByActiveTrue();

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = 'ORDERED' AND o.active = true")
    long countActiveOrders();

    long countByStatusInAndActiveTrue(List<Status> statuses);

    List<Order> findByStatusAndCreatedBy(Status status, Long createdBy);

    List<Order> findOrdersByCreatedDateBetweenAndCreatedBy(LocalDateTime dateFrom, LocalDateTime dateTo, Long createdBy);


}
