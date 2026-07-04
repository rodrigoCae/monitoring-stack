package com.bank.payment.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_transactions")
public class PaymentTransaction extends PanacheEntity {

    public Double amount;
    public String status;
    public LocalDateTime createdAt;

    public PaymentTransaction() {}

    public PaymentTransaction(Double amount, String status) {
        this.amount = amount;
        this.status = status;
        this.createdAt = LocalDateTime.now();
    }
}