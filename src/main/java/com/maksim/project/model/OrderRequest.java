package com.maksim.project.model;

import java.util.List;

public class OrderRequest {

    private List<Long> dishIds;  // Lista ID-jeva jela
    private String address;  // Adresa korisnika


    public List<Long> getDishIds() {
        return dishIds;
    }

    public void setDishIds(List<Long> dishIds) {
        this.dishIds = dishIds;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
