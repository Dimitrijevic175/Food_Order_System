package com.maksim.project.model;

import java.io.Serializable;

public class OrderStatusMessage implements Serializable {

    private Long orderId;
    private Status nextStatus;
    private int delay;

    // Konstruktor, getter-i i setter-i
    public OrderStatusMessage(Long orderId, Status nextStatus, int delay) {
        this.orderId = orderId;
        this.nextStatus = nextStatus;
        this.delay = delay;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Status getNextStatus() {
        return nextStatus;
    }

    public void setNextStatus(Status nextStatus) {
        this.nextStatus = nextStatus;
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }
}
