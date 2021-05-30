package com.ironhack.crm;

import javax.persistence.*;
import java.util.Objects;

@Entity
public class Opportunity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(columnDefinition = "ENUM('HYBRID', 'FLATBED','BOX')")
    @Enumerated(EnumType.STRING)
    private Product product;
    private int quantity;


    @Column(columnDefinition = "ENUM('OPEN','CLOSED_WON','CLOSED_LOST')")
    @Enumerated(EnumType.STRING)
    private Status status;

    @ManyToOne
    @JoinColumn(name="sales_rep")
    private SalesRep salesRep;

    @OneToOne
    @JoinColumn(name = "decision_maker")
    private Contact decisionMaker;

    @ManyToOne
    @JoinColumn(name="account_id")
    private Account account;


    public Opportunity() {
    }

    public Opportunity(Product product, int quantity, Contact decisionMaker, Status status) {
        this.product = product;
        this.quantity = quantity;
        this.decisionMaker = decisionMaker;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Contact getDecisionMaker() {
        return decisionMaker;
    }

    public void setDecisionMaker(Contact decisionMaker) {
        this.decisionMaker = decisionMaker;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Opportunity that = (Opportunity) o;
        return getQuantity() == that.getQuantity() &&
                getProduct() == that.getProduct() &&
                Objects.equals(getDecisionMaker(), that.getDecisionMaker()) &&
                getStatus() == that.getStatus();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getProduct(), getQuantity(), getDecisionMaker(), getStatus());
    }

    @Override
    public String toString() {
        return "Opportunity{" +
                "product=" + product +
                ", quantity=" + quantity +
                ", decisionMaker=" + decisionMaker +
                ", status=" + status +
                '}';
    }
}