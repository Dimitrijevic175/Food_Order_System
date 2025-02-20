package com.maksim.project.controller;

import com.maksim.project.model.*;
import com.maksim.project.repository.ErrorMessageRepository;
import com.maksim.project.repository.OrderRepository;
import com.maksim.project.security.Admin;
import com.maksim.project.service.OrderService;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ErrorMessageRepository errorMessageRepository;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @GetMapping("/user/{userId}")
    public List<Order> getUserOrders(@PathVariable Long userId) {
        List<Order> allOrders = orderService.getOrdersForUser(userId);

        // Filtriraj samo aktivne porudžbine
        return allOrders.stream()
                .filter(Order::isActive) // Pretpostavljamo da postoji metoda isActive() koja vraća true za aktivne porudžbine
                .collect(Collectors.toList());
    }

    // Endpoint za pretragu svih porudžbina, dostupan samo adminima
    @Admin
    @GetMapping("/all")
    public List<Order> getAllOrders() {
        List<Order> allOrders = orderService.getAllOrders();

        // Filtriraj samo aktivne porudžbine
        return allOrders.stream()
                .filter(Order::isActive) // Pretpostavljamo da postoji metoda isActive() koja vraća true za aktivne porudžbine
                .collect(Collectors.toList());
    }

    // Endpoint za pretragu porudžbina po statusu
    @GetMapping("/status")
    public List<Order> getOrdersByStatus(@RequestParam List<Status> status) {
        return orderService.getOrdersByStatus(status);
    }

    // Endpoint za pretragu porudžbina prema datumu sa body-jem
    @PostMapping("/date-range")
    public List<Order> getOrdersByDateRange(@RequestBody DataRange dateRange) {
        return orderService.getOrdersByDateRange(dateRange.getDateFrom(), dateRange.getDateTo());
    }


    @GetMapping({"/admin", "/admin/{userId}"})
    @Admin
    public List<Order> getOrders(@PathVariable(required = false) Long userId) {
        List<Order> allOrders = orderService.getOrdersForAdmin(userId);

        // Filtriraj samo aktivne porudžbine
        return allOrders.stream()
                .filter(Order::isActive) // Pretpostavljamo da postoji metoda isActive() koja vraća true za aktivne porudžbine
                .collect(Collectors.toList());
    }


    // Endpoint za kreiranje nove porudžbine
    @PostMapping("/place-order")
    public ResponseEntity<Order> placeOrder(@RequestBody OrderRequest orderRequest, @RequestHeader Long userId) {
        try {
            // Kreiranje porudžbine pomoću servisa
            Order order = orderService.placeOrder(orderRequest, userId);

            // Vraćanje odgovora sa statusom 201 (Created) i porudžbinom
            return ResponseEntity.status(201).body(order);
        } catch (Exception e) {

            String errorMessage = "Maksimalan broj istovremenih porudžbina je dostignut.";
            // Beležimo grešku u bazi
            ErrorMessage error = new ErrorMessage();
            error.setErrorMessage(errorMessage);
            error.setTimestamp(LocalDateTime.now());
            error.setOrderId(null); // Ako nemamo konkretnu porudžbinu koja je izazvala grešku, možete staviti null ili neku default vrednost
            error.setOperation("placeOrder");
            error.setUserId(userId);
            // Pozivamo repository da sačuvamo grešku u bazi
            errorMessageRepository.save(error); // Ovo je ključna linija


            // U slučaju greške, vraćanje odgovora sa statusom 400 (Bad Request)
            return ResponseEntity.status(400).body(null);
        }
    }

    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<Order> cancelOrder(@PathVariable Long orderId) {
        try {
            Order canceledOrder = orderService.cancelOrder(orderId);
            return ResponseEntity.ok(canceledOrder);
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException ex) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/{orderId}/track")
    public ResponseEntity<String> trackOrder(@PathVariable Long orderId) {
        try {
            String status = orderService.trackOrder(orderId);
            return ResponseEntity.ok(status);
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/statuses")
    public ResponseEntity<Map<Long, String>> getAllOrdersStatuses() {
        try {
            Map<Long, String> statuses = orderService.getAllOrdersStatuses(); // Metoda koja vraća mapu orderId -> status
            return ResponseEntity.ok(statuses);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @PostMapping("/{orderId}/prepare")
    public ResponseEntity<Void> startPreparing(@PathVariable Long orderId) {
        amqpTemplate.convertAndSend("orderStatusQueue", orderId);
        return ResponseEntity.ok().build(); // Odmah vraća odgovor
    }

    @PostMapping("/{orderId}/deliver")
    public ResponseEntity<Void> startDelivery(@PathVariable Long orderId) {
        amqpTemplate.convertAndSend("orderDeliverQueue", orderId);
        return ResponseEntity.ok().build(); // Odmah vraća odgovor
    }

    @PostMapping("/{orderId}/delivered")
    public ResponseEntity<Void> delivered(@PathVariable Long orderId) {
        amqpTemplate.convertAndSend("orderDoneQueue", orderId);
        return ResponseEntity.ok().build(); // Odmah vraća odgovor
    }

    @PostMapping("/schedule-order")
    public ResponseEntity<ScheduledOrder> scheduleOrder(
            @RequestBody ScheduleOrderRequest scheduleOrderRequest) {
        try {
            ScheduledOrder scheduledOrder = orderService.scheduleOrder(
                    scheduleOrderRequest.getOrderRequest(),
                    scheduleOrderRequest.getUserId(),
                    scheduleOrderRequest.getScheduledTime()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(scheduledOrder);
        } catch (Exception e) {
            String errorMessage = "Maksimalan broj istovremenih porudžbina je dostignut.";
            // Beležimo grešku u bazi
            ErrorMessage error = new ErrorMessage();
            error.setErrorMessage(errorMessage);
            error.setTimestamp(LocalDateTime.now());
            error.setOrderId(null); // Ako nemamo konkretnu porudžbinu koja je izazvala grešku, možete staviti null ili neku default vrednost
            error.setOperation("scheduleOrder");
            error.setUserId(scheduleOrderRequest.getUserId());
            // Pozivamo repository da sačuvamo grešku u bazi
            errorMessageRepository.save(error); // Ovo je ključna linija
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }





    // Endpoint za pretragu po statusu za korisnika
    @GetMapping("/status-for-user")
    public List<Order> getOrdersByStatusForUser(
            @RequestParam String status,
            @RequestParam Long userId) {
        return orderService.getOrdersByStatusForUser(status, userId);
    }

    // Endpoint za pretragu po datumu za korisnika
    @GetMapping("/date-range-for-user")
    public List<Order> getOrdersByDateRangeForUser(
            @RequestParam String dateFrom,
            @RequestParam String dateTo,
            @RequestParam Long userId) {
        LocalDate from = LocalDate.parse(dateFrom);
        LocalDate to = LocalDate.parse(dateTo);
        return orderService.getOrdersByDateRangeForUser(from, to, userId);
    }

    @GetMapping("/createdBy/{userId}")
    public ResponseEntity<List<Order>> getOrdersByCreatedBy(@PathVariable Long userId) {
        List<Order> orders = orderService.getOrdersByCreatedBy(userId);
        return ResponseEntity.ok(orders);
    }

//    @PutMapping("/{orderId}/start")
//    public ResponseEntity<?> startOrderProcessing(@PathVariable Long orderId) {
//        try {
//            orderService.startOrderProcessing(orderId);
//            return ResponseEntity.ok("Proces promene statusa za porudžbinu " + orderId + " je započet.");
//        } catch (IllegalArgumentException e) {
//            return ResponseEntity.badRequest().body(e.getMessage());
//        }
//    }

    @PutMapping("/{orderId}/start")
    public ResponseEntity<?> startOrderProcessing(@PathVariable Long orderId) {
        try {
            orderService.startOrderProcessing(orderId);
            // Vraćanje JSON objekta sa porukom
            return ResponseEntity.ok(new ResponseMessage("Proces promene statusa za porudžbinu " + orderId + " je započet."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ResponseMessage(e.getMessage()));
        }
    }

    // Klasa za ResponseMessage
    public static class ResponseMessage {
        private String message;

        public ResponseMessage(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }



}
