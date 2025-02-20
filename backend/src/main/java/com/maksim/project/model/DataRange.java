package com.maksim.project.model;

import java.time.LocalDate;

public class DataRange {

    private LocalDate dateFrom;
    private LocalDate dateTo;

    // Getters and Setters
    public LocalDate getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(LocalDate dateFrom) {
        this.dateFrom = dateFrom;
    }

    public LocalDate getDateTo() {
        return dateTo;
    }

    public void setDateTo(LocalDate dateTo) {
        this.dateTo = dateTo;
    }

}
