package com.example;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ExpenseTrackerPage extends VBox {

    private final ObservableList<Expense> expenses = FXCollections.observableArrayList();
    private final Label totalLabel = new Label("Total: $0.00");

    public ExpenseTrackerPage() {
        setSpacing(16);
        setPadding(new Insets(24));
        setAlignment(Pos.TOP_CENTER);
        setMaxWidth(700);

        Label title = new Label("Expense Tracker");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold;");

        HBox form = createForm();
        TableView<Expense> table = createTable();

        totalLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Update total when expenses change
        expenses.addListener((javafx.collections.ListChangeListener<Expense>) c -> updateTotal());

        // Add some sample data
        expenses.addAll(
            new Expense("Groceries", 52.30, LocalDate.of(2026, 3, 1), "Food"),
            new Expense("Bus pass", 75.00, LocalDate.of(2026, 3, 1), "Transport"),
            new Expense("Coffee", 4.50, LocalDate.of(2026, 3, 2), "Food")
        );

        getChildren().addAll(title, form, table, totalLabel);
    }

    private HBox createForm() {
        TextField descField = new TextField();
        descField.setPromptText("Description");
        descField.setPrefWidth(160);

        TextField amountField = new TextField();
        amountField.setPromptText("Amount");
        amountField.setPrefWidth(100);

        DatePicker datePicker = new DatePicker(LocalDate.now());
        datePicker.setPrefWidth(130);

        ComboBox<String> categoryBox = new ComboBox<>();
        categoryBox.getItems().addAll("Food", "Transport", "Housing", "Entertainment", "Other");
        categoryBox.setValue("Food");
        categoryBox.setPrefWidth(120);

        Button addBtn = new Button("Add");
        addBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");

        addBtn.setOnAction(e -> {
            String desc = descField.getText().trim();
            String amtText = amountField.getText().trim();
            if (desc.isEmpty() || amtText.isEmpty()) return;
            try {
                double amount = Double.parseDouble(amtText);
                expenses.add(new Expense(desc, amount, datePicker.getValue(), categoryBox.getValue()));
                descField.clear();
                amountField.clear();
            } catch (NumberFormatException ex) {
                // ignore invalid input
            }
        });

        HBox form = new HBox(8, descField, amountField, datePicker, categoryBox, addBtn);
        form.setAlignment(Pos.CENTER_LEFT);
        return form;
    }

    @SuppressWarnings("unchecked")
    private TableView<Expense> createTable() {
        TableView<Expense> table = new TableView<>(expenses);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        TableColumn<Expense, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));

        TableColumn<Expense, Number> amountCol = new TableColumn<>("Amount ($)");
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        amountCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("%.2f", item.doubleValue()));
            }
        });

        TableColumn<Expense, LocalDate> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        dateCol.setCellFactory(col -> new TableCell<>() {
            private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : fmt.format(item));
            }
        });

        TableColumn<Expense, String> catCol = new TableColumn<>("Category");
        catCol.setCellValueFactory(new PropertyValueFactory<>("category"));

        TableColumn<Expense, Void> deleteCol = new TableColumn<>("");
        deleteCol.setPrefWidth(60);
        deleteCol.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("X");
            {
                btn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-size: 10px;");
                btn.setOnAction(e -> expenses.remove(getIndex()));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        table.getColumns().addAll(descCol, amountCol, dateCol, catCol, deleteCol);
        table.setPrefHeight(300);
        return table;
    }

    private void updateTotal() {
        double total = expenses.stream().mapToDouble(Expense::getAmount).sum();
        totalLabel.setText(String.format("Total: $%.2f", total));
    }
}
