package com.example;

import javafx.beans.property.*;
import java.time.LocalDate;

public class Expense {

    private final StringProperty description = new SimpleStringProperty();
    private final DoubleProperty amount = new SimpleDoubleProperty();
    private final ObjectProperty<LocalDate> date = new SimpleObjectProperty<>();
    private final StringProperty category = new SimpleStringProperty();

    public Expense(String description, double amount, LocalDate date, String category) {
        this.description.set(description);
        this.amount.set(amount);
        this.date.set(date);
        this.category.set(category);
    }

    public StringProperty descriptionProperty() { return description; }
    public String getDescription() { return description.get(); }

    public DoubleProperty amountProperty() { return amount; }
    public double getAmount() { return amount.get(); }

    public ObjectProperty<LocalDate> dateProperty() { return date; }
    public LocalDate getDate() { return date.get(); }

    public StringProperty categoryProperty() { return category; }
    public String getCategory() { return category.get(); }
}
