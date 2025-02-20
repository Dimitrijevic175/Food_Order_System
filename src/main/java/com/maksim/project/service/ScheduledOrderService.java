package com.maksim.project.service;

import com.maksim.project.model.*;
import com.maksim.project.repository.ErrorMessageRepository;
import com.maksim.project.repository.OrderRepository;
import com.maksim.project.repository.ScheduledOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ScheduledOrderService {

    @Autowired
    private ScheduledOrderRepository scheduledOrderRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ErrorMessageRepository errorMessageRepository;



}
