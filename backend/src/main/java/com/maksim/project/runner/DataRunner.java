package com.maksim.project.runner;

import com.maksim.project.model.*;
import com.maksim.project.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Profile({"default"})
@Component
public class DataRunner implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataRunner.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final PermissionRepository permissionRepository;

    private RoleRepository roleRepository;
    private final OrderRepository orderRepository;
    private final DishRepository dishRepository;

    private ErrorMessageRepository errorMessageRepository;

    @Autowired
    public DataRunner(UserRepository userRepository,ErrorMessageRepository errorMessageRepository,DishRepository dishRepository,OrderRepository orderRepository ,RoleRepository roleRepository ,PasswordEncoder passwordEncoder,PermissionRepository permissionRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.permissionRepository = permissionRepository;
        this.roleRepository = roleRepository;
        this.orderRepository = orderRepository;
        this.dishRepository = dishRepository;
        this.errorMessageRepository = errorMessageRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("DataRunner: Starting execution.");

        if (userRepository.findByEmail("admin@gmail.com") == null) {
            logger.info("DataRunner: Creating 'admin' user.");
            User user = new User();
            user.setEmail("admin@gmail.com");

            user.setFirstName("admin");
            user.setLastName("adminovic");

            String hashedPassword = passwordEncoder.encode("admin");
            logger.info("DataRunner: Hashed password: {}", hashedPassword);
            user.setPassword(hashedPassword);

            // can_create_users, can_read_users, can_update_users, can_delete_users.
            Permission one = new Permission("can_create_users");
            Permission two = new Permission("can_read_users");
            Permission three = new Permission("can_update_users");
            Permission four = new Permission("can_delete_users");
            Permission five = new Permission("can_search_order");
            Permission six = new Permission("can_place_order");
            Permission seven = new Permission("can_cancel_order");
            Permission eight = new Permission("can_track_order");
            Permission nine = new Permission("can_schedule_order");


            permissionRepository.save(one);
            permissionRepository.save(two);
            permissionRepository.save(three);
            permissionRepository.save(four);
            permissionRepository.save(five);
            permissionRepository.save(six);
            permissionRepository.save(seven);
            permissionRepository.save(eight);
            permissionRepository.save(nine);

            Set<Permission> permissions = new HashSet<>();
            permissions.add(one);
            permissions.add(two);
            permissions.add(three);
            permissions.add(four);
            permissions.add(five);
            permissions.add(six);
            permissions.add(seven);
            permissions.add(eight);
            permissions.add(nine);

            Role adminRole = new Role();
            adminRole.setName("ROLE_ADMIN");

            Role userRole = new Role();
            userRole.setName("ROLE_USER");

            roleRepository.save(adminRole);
            roleRepository.save(userRole);

            user.setPermissions(permissions);
            user.setRole(adminRole);

            userRepository.save(user);


            logger.info("DataRunner: Creating 'pera' user.");
            User pera = new User();
            pera.setEmail("pera@gmail.com");

            pera.setFirstName("pera");
            pera.setLastName("peric");

            String hashedPassword2 = passwordEncoder.encode("perapera");
            logger.info("DataRunner: Hashed password: {}", hashedPassword2);
            pera.setPassword(hashedPassword2);


            Set<Permission> permissions2 = new HashSet<>();
            permissions2.add(two);
            permissions2.add(five);
            permissions2.add(six);

            pera.setPermissions(permissions2);
            pera.setRole(userRole);

            Order order4 = new Order();
            order4.setStatus(Status.DELIVERED);
            order4.setCreatedBy(2L); // Povezivanje sa drugim korisnikom
            order4.setActive(true);
            order4.setCreatedDate(LocalDateTime.now());  // Postavljanje trenutnog vremena kao datuma kreiranja
            order4.setAddress("Beogradska 246");
            orderRepository.save(order4);

            Order order5 = new Order();
            order5.setStatus(Status.CANCELED);
            order5.setCreatedBy(2L); // Povezivanje sa drugim korisnikom
            order5.setActive(false);
            order5.setCreatedDate(LocalDateTime.now());  // Postavljanje trenutnog vremena kao datuma kreiranja
            order5.setAddress("Stare Ranke 1");
            orderRepository.save(order5);

            Set<Order> orders = new HashSet<>();
            orders.add(order4);
            orders.add(order5);

            pera.setOrders(orders);

            userRepository.save(pera);

            logger.info("DataRunner: 'admin' user saved successfully.");
        } else {
            logger.info("DataRunner: 'admin' user already exists.");
        }
        // Dodavanje jela
        logger.info("DataRunner: Creating sample dishes.");

        Dish dish1 = new Dish();
        dish1.setName("Pizza");
        dishRepository.save(dish1);

        Dish dish2 = new Dish();
        dish2.setName("Spaghetti");
        dishRepository.save(dish2);

        Dish dish3 = new Dish();
        dish3.setName("Salad");
        dishRepository.save(dish3);

        logger.info("DataRunner: Sample dishes created successfully.");



        // Dodavanje porudžbina za testiranje
        logger.info("DataRunner: Creating sample orders.");

        Order order1 = new Order();
        order1.setStatus(Status.ORDERED);
        order1.setCreatedBy(1L); // Povezivanje sa korisnikom
        order1.setActive(true);
        order1.setCreatedDate(LocalDateTime.now());
        order1.setItems(List.of(dish1, dish2)); // Dodavanje jela u porudžbinu
        orderRepository.save(order1);

        Order order2 = new Order();
        order2.setStatus(Status.PREPARING);
        order2.setCreatedBy(1L); // Povezivanje sa korisnikom
        order2.setActive(true);
        order2.setCreatedDate(LocalDateTime.now());
        order2.setItems(List.of(dish2, dish3)); // Dodavanje jela u porudžbinu
        orderRepository.save(order2);

        Order order3 = new Order();
        order3.setStatus(Status.IN_DELIVERY);
        order3.setCreatedBy(1L); // Povezivanje sa korisnikom
        order3.setActive(true);
        order3.setCreatedDate(LocalDateTime.now());
        order3.setItems(List.of(dish1, dish3)); // Dodavanje jela u porudžbinu
        orderRepository.save(order3);

//        Order order4 = new Order();
//        order4.setStatus(Status.DELIVERED);
//        order4.setCreatedBy(2L); // Povezivanje sa drugim korisnikom
//        order4.setActive(true);
//        order4.setCreatedDate(LocalDateTime.now());  // Postavljanje trenutnog vremena kao datuma kreiranja
//        orderRepository.save(order4);
//
//        Order order5 = new Order();
//        order5.setStatus(Status.CANCELED);
//        order5.setCreatedBy(2L); // Povezivanje sa drugim korisnikom
//        order5.setActive(false);
//        order5.setCreatedDate(LocalDateTime.now());  // Postavljanje trenutnog vremena kao datuma kreiranja
//        orderRepository.save(order5);

//        Order order21 = new Order();
//        order21.setStatus(Status.IN_DELIVERY);
//        order21.setCreatedBy(1L); // Povezivanje sa korisnikom
//        order21.setActive(true);
//        order21.setCreatedDate(LocalDateTime.now());
//        order21.setItems(List.of(dish1, dish3)); // Dodavanje jela u porudžbinu
//        orderRepository.save(order21);
//
//        Order order22 = new Order();
//        order22.setStatus(Status.IN_DELIVERY);
//        order22.setCreatedBy(1L); // Povezivanje sa korisnikom
//        order22.setActive(true);
//        order22.setCreatedDate(LocalDateTime.now());
//        order22.setItems(List.of(dish1, dish3)); // Dodavanje jela u porudžbinu
//        orderRepository.save(order22);

        // Greška za porudžbinu 1
        ErrorMessage error1 = new ErrorMessage();
        error1.setOrderId(order1.getId());
        error1.setErrorMessage("Greška u pripremi porudžbine. Nedostaju sastojci.");
        error1.setTimestamp(LocalDateTime.now());
        error1.setOperation("data runner");
        error1.setUserId(1L);
        errorMessageRepository.save(error1);

        // Greška za porudžbinu 2
        ErrorMessage error2 = new ErrorMessage();
        error2.setOrderId(order2.getId());
        error2.setErrorMessage("Greška u dostavi. Poremećen status isporuke.");
        error2.setTimestamp(LocalDateTime.now());
        error2.setOperation("data runner");
        error2.setUserId(1L);
        errorMessageRepository.save(error2);

        // Greška za porudžbinu 3
        ErrorMessage error3 = new ErrorMessage();
        error3.setOrderId(order3.getId());
        error3.setErrorMessage("Greška u statusu porudžbine. Isporuka kasni.");
        error3.setTimestamp(LocalDateTime.now());
        error3.setOperation("data runner");
        error3.setUserId(2L);
        errorMessageRepository.save(error3);


        logger.info("DataRunner: Sample orders created successfully.");
    }
}

