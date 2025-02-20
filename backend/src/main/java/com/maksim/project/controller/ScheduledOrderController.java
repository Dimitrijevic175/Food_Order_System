package com.maksim.project.controller;

import com.maksim.project.service.ScheduledOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/scheduled-orders")
public class ScheduledOrderController {

    private final ScheduledOrderService scheduledOrderService;

    @Autowired
    public ScheduledOrderController(ScheduledOrderService scheduledOrderService) {
        this.scheduledOrderService = scheduledOrderService;
    }


}

