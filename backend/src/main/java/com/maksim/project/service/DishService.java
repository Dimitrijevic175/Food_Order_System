package com.maksim.project.service;

import com.maksim.project.model.Dish;
import com.maksim.project.repository.DishRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DishService {

    @Autowired
    private DishRepository dishRepository;

    public List<Dish> allDishes(){
        return dishRepository.findAll();
    }

}
