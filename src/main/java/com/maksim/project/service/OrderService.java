package com.maksim.project.service;


import com.maksim.project.model.*;
import com.maksim.project.repository.DishRepository;
import com.maksim.project.repository.ErrorMessageRepository;
import com.maksim.project.repository.OrderRepository;

import com.maksim.project.repository.ScheduledOrderRepository;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.*;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


@Service
public class OrderService {


    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private DishRepository dishRepository;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Autowired
    private ScheduledOrderRepository scheduledOrderRepository;

    @Autowired
    private ErrorMessageRepository errorMessageRepository;

    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(5);

    private static final int MAX_CONCURRENT_ORDERS = 3;

    // iz order u preparing
    @Async
    @Transactional
    @RabbitListener(queues = "orderStatusQueue")
    public void changeStatusToPreparing(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));

        if (order.getStatus() != Status.ORDERED) {
            throw new IllegalStateException("Order must be in ORDERED state to transition to PREPARING");
        }
        order.setActive(false);

        order.setStatus(Status.PREPARING);
        System.out.println("ORDER SET TO PREPARING");
        orderRepository.save(order);
    }

    // iz preparing u in delivery
    @Async
    @Transactional
    @RabbitListener(queues = "orderDeliverQueue")
    public void changeStatusToInDelivery(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));

        if (order.getStatus() != Status.PREPARING) {
            throw new IllegalStateException("Order must be in PREPARING state to transition to IN_DELIVERY");
        }

        order.setStatus(Status.IN_DELIVERY);
        System.out.println("ORDER SET TO IN_DELIVERY");
        orderRepository.save(order);
    }

    // iz in delivery u delivered
    @Async
    @Transactional
    @RabbitListener(queues = "orderDoneQueue")
    public void changeStatusToDelivered(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));

        if (order.getStatus() != Status.IN_DELIVERY) {
            throw new IllegalStateException("Order must be in IN_DELIVERY state to transition to DELIVERED");
        }

        order.setStatus(Status.DELIVERED);
        System.out.println("ORDER SET TO DELIVERED");
        order.setActive(true); // Porudžbina se završava
        orderRepository.save(order);
    }









    private List<Order> filterActiveOrders(List<Order> orders) {
        List<Order> activeOrders = new ArrayList<>();
        for (Order order : orders) {
            if (order.isActive()) {
                activeOrders.add(order);
            }
        }
        return activeOrders;
    }
    public List<Order> getOrdersForUser(Long userId) {
        return filterActiveOrders(orderRepository.findByCreatedBy(userId));
    }

    public List<Order> getOrdersForAdmin(Long userId) {
        List<Order> orders = (userId == null) ? orderRepository.findAll() : orderRepository.findByCreatedBy(userId);
        return filterActiveOrders(orders);
    }

    public List<Order> getAllOrders() {
        return filterActiveOrders(orderRepository.findAll());
    }

    public List<Order> getOrdersByStatus(List<Status> statuses) {
        return filterActiveOrders(orderRepository.findByStatusIn(statuses));
    }

    public List<Order> getOrdersByDateRange(LocalDate dateFrom, LocalDate dateTo) {
        if (dateTo == null) {
            throw new IllegalArgumentException("The 'dateTo' parameter must be defined.");
        }
        List<Order> orders = orderRepository.findOrdersByCreatedDateBetween(dateFrom.atStartOfDay(), dateTo.atTime(23, 59, 59));
        return filterActiveOrders(orders);
    }

    public Order placeOrder(OrderRequest orderRequest, Long userId) {

        // Prvo proveravamo broj istovremenih porudžbina
        long activeOrders = orderRepository.countByStatusInAndActiveTrue(
                List.of(Status.PREPARING, Status.IN_DELIVERY)
        );

//        long orderNumber =  orderRepository.countByStatusInAndActiveTrue(List.of(Status.ORDERED));
        // Proveravamo broj zakazanih i postojećih porudžbina
        long scheduledOrders = scheduledOrderRepository.countByScheduledTimeAndExecutedFalse(LocalDateTime.now());
        long orderNumber = orderRepository.countByStatusInAndActiveTrue(List.of(Status.ORDERED));
        long finalNumber = scheduledOrders + orderNumber;


        if (activeOrders >= MAX_CONCURRENT_ORDERS || finalNumber+1 > 3) {
            String errorMessage = "Maksimalan broj istovremenih porudžbina je dostignut.";

            throw new IllegalStateException(errorMessage);  // Odbijamo kreiranje porudžbine
        }

        Order order = new Order();
        order.setStatus(Status.ORDERED);
        order.setCreatedBy(userId);
        order.setActive(true);
        order.setCreatedDate(LocalDateTime.now());
        order.setAddress(orderRequest.getAddress());

        // Učitavanje jela sa dishIds
        List<Dish> dishes = dishRepository.findAllById(orderRequest.getDishIds());
        order.setItems(dishes);

        Order savedOrder = orderRepository.save(order);

//        // Pokretanje asinhronog procesa za promenu stanja
//        processOrderStatus(savedOrder.getId());

        return savedOrder;
    }

    public Order cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + orderId));

        if (!order.isActive()) {
            throw new IllegalStateException("Only active orders can be canceled.");
        }

        if (!order.getStatus().equals(Status.ORDERED)) {
            throw new IllegalStateException("Only orders in the ORDERED status can be canceled.");
        }

        order.setStatus(Status.CANCELED);
        return orderRepository.save(order);
    }

    public String trackOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Porudžbina sa ID-jem " + orderId + " nije pronađena."));

        return order.getStatus().name(); // Vraća status kao string
    }



    // ZAKAZIVANJE

//    @Transactional
//    public ScheduledOrder scheduleOrder(OrderRequest orderRequest, Long userId, LocalDateTime scheduledTime) {
//
//        // Prvo proveravamo broj istovremenih porudžbina
//        long activeOrders = orderRepository.countByStatusInAndActiveTrue(
//                List.of(Status.PREPARING, Status.IN_DELIVERY)
//        );
//
//        if (activeOrders >= MAX_CONCURRENT_ORDERS) {
//            String errorMessage = "Maksimalan broj istovremenih porudžbina je dostignut.";
//            ErrorMessage error = new ErrorMessage();
//            error.setErrorMessage(errorMessage);
//            error.setTimestamp(LocalDateTime.now());
//            errorMessageRepository.save(error);
//            throw new IllegalStateException(errorMessage);  // Odbijamo zakazivanje porudžbine
//        }
//
//
//        ScheduledOrder scheduledOrder = new ScheduledOrder();
//        scheduledOrder.setUserId(userId);
//        scheduledOrder.setScheduledTime(scheduledTime);
//        scheduledOrder.setExecuted(false);
//
//        Order order = new Order();
//        order.setStatus(Status.ORDERED);
//        order.setCreatedBy(userId);
//        order.setActive(true);
//        order.setAddress(orderRequest.getAddress());
//        order.setItems(dishRepository.findAllById(orderRequest.getDishIds()));
//
//        orderRepository.save(order);
//        scheduledOrder.setOrder(order);
//
//        return scheduledOrderRepository.save(scheduledOrder);
//    }
@Transactional
public ScheduledOrder scheduleOrder(OrderRequest orderRequest, Long userId, LocalDateTime scheduledTime) {

    // Prvo proveravamo broj istovremenih porudžbina
    long activeOrders = orderRepository.countByStatusInAndActiveTrue(
            List.of(Status.PREPARING, Status.IN_DELIVERY)
    );

    // Proveravamo broj zakazanih i postojećih porudžbina
    long scheduledOrders = scheduledOrderRepository.countByScheduledTimeAndExecutedFalse(scheduledTime);
    long orderNumber = orderRepository.countByStatusInAndActiveTrue(List.of(Status.ORDERED));
    long finalNumber = scheduledOrders + orderNumber;

    if (activeOrders >= MAX_CONCURRENT_ORDERS || finalNumber > 3) {
        String errorMessage = "Maksimalan broj istovremenih porudžbina je dostignut.";


        // Vraćamo IllegalStateException kako bi kontroler obradio grešku i vratio 400
        throw new IllegalStateException(errorMessage);
    }

    // Ako ima mesta, nastavljamo sa kreiranjem porudžbine
    ScheduledOrder scheduledOrder = new ScheduledOrder();
    scheduledOrder.setUserId(userId);
    scheduledOrder.setScheduledTime(scheduledTime);
    scheduledOrder.setExecuted(false);

    Order order = new Order();
    order.setStatus(Status.ORDERED);
    order.setCreatedBy(userId);
    order.setActive(true);
    order.setAddress(orderRequest.getAddress());
    order.setItems(dishRepository.findAllById(orderRequest.getDishIds()));

    orderRepository.save(order);
    scheduledOrder.setOrder(order);

    return scheduledOrderRepository.save(scheduledOrder);
}


    private void logError(ScheduledOrder scheduledOrder, String errorMessage) {
        ErrorMessage error = new ErrorMessage();
        error.setOrderId(scheduledOrder.getOrder().getId());
        error.setTimestamp(LocalDateTime.now());

        error.setErrorMessage(errorMessage);
        errorMessageRepository.save(error);
    }

    @Scheduled(fixedRate = 60000) // Proverava na svakih minut
    @Transactional
    public void executeScheduledOrders() {
        // Dohvatite zakazane porudžbine koje nisu izvršene i čije je vreme prošlo
        List<ScheduledOrder> pendingOrders = scheduledOrderRepository.findByExecutedFalseAndScheduledTimeBefore(LocalDateTime.now());

        for (ScheduledOrder scheduledOrder : pendingOrders) {
            try {
                Order order = scheduledOrder.getOrder();

                // Proverite broj porudžbina u stanjima PREPARING i IN_DELIVERY
                long activeOrders = orderRepository.countByStatusInAndActiveTrue(List.of(Status.PREPARING, Status.IN_DELIVERY));

                // Ako ima mesta za novu porudžbinu
                if (activeOrders <= MAX_CONCURRENT_ORDERS && order.getStatus() != Status.CANCELED) {
                    order.setStatus(Status.ORDERED); // Postavite status na ORDERED
                    order.setCreatedDate(LocalDateTime.now()); // Postavite trenutni datum
                    orderRepository.save(order); // Sačuvajte porudžbinu u bazi

                    scheduledOrder.setExecuted(true); // Obeležite da je porudžbina izvršena
                    scheduledOrderRepository.save(scheduledOrder); // Sačuvajte izmenu zakazane porudžbine
                    startOrderProcessing(order.getId());
                } else {
                    // Beleženje greške ukoliko je prekoračen limit
                    logError(scheduledOrder, "Prekoračen maksimalni broj porudžbina u pripremi ili dostavi.");
                }
            } catch (Exception e) {
                // Beleženje greške za neočekivane situacije
                logError(scheduledOrder, "Došlo je do greške prilikom izvršavanja porudžbine: " + e.getMessage());
            }
        }
    }



    public List<Order> getOrdersByStatusForUser(String status, Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID must be provided.");
        }

        Status orderStatus;
        try {
            orderStatus = Status.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status provided: " + status);
        }

        List<Order> orders = orderRepository.findByStatusAndCreatedBy(orderStatus, userId);
        return filterActiveOrders(orders);
    }

    public List<Order> getOrdersByDateRangeForUser(LocalDate dateFrom, LocalDate dateTo, Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID must be provided.");
        }

        if (dateFrom == null || dateTo == null) {
            throw new IllegalArgumentException("Both dateFrom and dateTo must be defined.");
        }

        List<Order> orders = orderRepository.findOrdersByCreatedDateBetweenAndCreatedBy(
                dateFrom.atStartOfDay(),
                dateTo.atTime(23, 59, 59),
                userId
        );

        return filterActiveOrders(orders);
    }


    // Dodajemo metod za pretragu porudžbina prema 'createdBy'
    public List<Order> getOrdersByCreatedBy(Long userId) {
        List<Order> orders = orderRepository.findByCreatedBy(userId);
        return filterActiveOrders(orders);
    }


    public void startOrderProcessing(Long orderId) {
        executorService.schedule(() -> changeStatus(orderId, Status.PREPARING), getDelay(10), TimeUnit.SECONDS);
        executorService.schedule(() -> changeStatus(orderId, Status.IN_DELIVERY), getDelay(25), TimeUnit.SECONDS);
        executorService.schedule(() -> changeStatus(orderId, Status.DELIVERED), getDelay(45), TimeUnit.SECONDS);
    }

    private synchronized void changeStatus(Long orderId, Status newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Porudžbina nije pronađena."));

        if (!order.isProcessing()) {
            order.setProcessing(true);
            order.setStatus(newStatus);
            order.setProcessing(false);
            orderRepository.save(order);
        }
    }

    private int getDelay(int baseSeconds) {
        Random random = new Random();
        int deviation = random.nextInt(6); // Dodavanje vremenske devijacije (0-5 sekundi uključivo)
        return baseSeconds + deviation;
    }

    public Map<Long, String> getAllOrdersStatuses() {
        List<Order> orders = orderRepository.findAll(); // Dohvati sve porudžbine
        Map<Long, String> statuses = new HashMap<>();
        for (Order order : orders) {
            statuses.put(order.getId(), order.getStatus().toString()); // Dodaj ID porudžbine i njen status u mapu
        }
        return statuses;
    }

}
