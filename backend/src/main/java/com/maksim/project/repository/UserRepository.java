package com.maksim.project.repository;

import com.maksim.project.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface UserRepository extends JpaRepository<User, Long> {
//    public User findByUsername(String username);

     User findByEmail(String email);
     List<User> findAll();

}
