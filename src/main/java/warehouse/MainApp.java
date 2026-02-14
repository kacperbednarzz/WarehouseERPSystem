package warehouse;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.Scene;
import java.sql.SQLException;

public class MainApp extends Application {
    private DatabaseManager db = new DatabaseManager();
    private TableView<Product> productTable = new TableView<>();
    private TableView<Supplier> supplierTable = new TableView<>();
    private TableView<Order> orderTable = new TableView<>();

    private int selectedSupplierId = -1;
    private Label statusLabel = new Label("SYSTEM GOTOWY");

    private final String GREEN_BUTTON_STYLE =
            "-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; " +
                    "-fx-background-radius: 5; -fx-padding: 10 25 10 25; -fx-cursor: hand;";

    private final String HOVER_STYLE = "-fx-background-color: #2ecc71;";
    private final double UNIFORM_BUTTON_WIDTH = 200.0;

    @Override
    public void start(Stage stage) {
        db.initializeDatabase();

        stage.setTitle("WAREHOUSE ERP");

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        tabPane.getTabs().addAll(
                new Tab("PRODUKTY", createProductPanel()),
                new Tab("DOSTAWCY", createSupplierPanel()),
                new Tab("ZAMÓWIENIA", createOrderPanel())
        );

        addContextMenu(productTable, "product");
        addContextMenu(supplierTable, "supplier");
        addContextMenu(orderTable, "order");

        setupEditEvents();

        statusLabel.setMaxWidth(Double.MAX_VALUE);
        statusLabel.setPadding(new Insets(8, 15, 8, 15));
        statusLabel.setStyle("-fx-background-color: #ecf0f1; -fx-text-fill: #2c3e50; -fx-font-weight: bold; -fx-border-color: #bdc3c7; -fx-border-width: 1 0 0 0;");

        VBox mainLayout = new VBox(tabPane, statusLabel);
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        Scene scene = new Scene(mainLayout, 1200, 800);

        scene.getStylesheets().add("data:text/css," +
                ".table-view .column-header-background { -fx-background-color: #f1f1f1; } " +
                ".table-view { -fx-background-insets: 0; -fx-padding: 0; } " +
                ".table-row-cell { -fx-background-color: white; -fx-text-background-color: black; } " +
                ".table-row-cell:selected { -fx-background-color: #3498db !important; } " +
                ".table-row-cell:selected .text { -fx-fill: white !important; } "
        );

        stage.setScene(scene);
        stage.show();
        refreshAllData();
    }

    // LOGIKA EDYCJI

    private void setupEditEvents() {
        productTable.setRowFactory(tv -> {
            TableRow<Product> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    showEditProductDialog(row.getItem());
                }
            });
            return row;
        });

        supplierTable.setRowFactory(tv -> {
            TableRow<Supplier> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    showEditSupplierDialog(row.getItem());
                }
            });
            return row;
        });
    }

    private void showEditProductDialog(Product p) {
        Stage editStage = new Stage();
        editStage.setTitle("EDYCJA PRODUKTU: " + p.getName());
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20)); grid.setHgap(10); grid.setVgap(10);

        TextField nameIn = new TextField(p.getName());
        TextField qtyIn = new TextField(String.valueOf(p.getQuantity()));
        TextField nettoIn = new TextField(String.valueOf(p.getPriceNetto()));

        grid.add(new Label("Nazwa:"), 0, 0); grid.add(nameIn, 1, 0);
        grid.add(new Label("Ilość:"), 0, 1); grid.add(qtyIn, 1, 1);
        grid.add(new Label("Cena Netto:"), 0, 2); grid.add(nettoIn, 1, 2);

        Button saveBtn = new Button("ZAPISZ ZMIANY");
        styleButton(saveBtn);
        saveBtn.setOnAction(e -> {
            try {
                double netto = Double.parseDouble(nettoIn.getText().replace(",", "."));
                p.setName(nameIn.getText());
                p.setQuantity(Integer.parseInt(qtyIn.getText()));
                p.setPriceNetto(netto);
                p.setPriceBrutto(netto * 1.23);

                db.updateProduct(p);
                refreshAllData();
                showStatus("ZAKTUALIZOWANO PRODUKT", false);
                editStage.close();
            } catch (Exception ex) { showStatus("BŁĄD EDYCJI!", true); }
        });

        VBox layout = new VBox(15, grid, saveBtn);
        layout.setAlignment(Pos.CENTER);
        editStage.setScene(new Scene(layout, 350, 250));
        editStage.show();
    }

    private void showEditSupplierDialog(Supplier s) {
        Stage editStage = new Stage();
        editStage.setTitle("EDYCJA DOSTAWCY: " + s.getName());
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20)); grid.setHgap(10); grid.setVgap(10);

        TextField nameIn = new TextField(s.getName());
        TextField phoneIn = new TextField(s.getPhone());
        TextField emailIn = new TextField(s.getEmail());

        grid.add(new Label("Firma:"), 0, 0); grid.add(nameIn, 1, 0);
        grid.add(new Label("Telefon:"), 0, 1); grid.add(phoneIn, 1, 1);
        grid.add(new Label("Email:"), 0, 2); grid.add(emailIn, 1, 2);

        Button saveBtn = new Button("ZAPISZ ZMIANY");
        styleButton(saveBtn);
        saveBtn.setOnAction(e -> {
            try {
                s.setName(nameIn.getText());
                s.setPhone(phoneIn.getText());
                s.setEmail(emailIn.getText());

                db.updateSupplier(s);
                refreshAllData();
                showStatus("ZAKTUALIZOWANO DOSTAWCĘ", false);
                editStage.close();
            } catch (Exception ex) { showStatus("BŁĄD EDYCJI!", true); }
        });

        VBox layout = new VBox(15, grid, saveBtn);
        layout.setAlignment(Pos.CENTER);
        editStage.setScene(new Scene(layout, 350, 250));
        editStage.show();
    }


    private String getNextOrderNumber() {
        try {
            var orders = db.getAllOrders();
            int lastId = 0;
            for (Order o : orders) {
                String num = o.getOrderNumber();
                if (num != null && num.startsWith("ZAM/")) {
                    try {
                        int id = Integer.parseInt(num.substring(4));
                        if (id > lastId) lastId = id;
                    } catch (Exception e) {}
                }
            }
            return String.format("ZAM/%05d", lastId + 1);
        } catch (Exception e) {
            return "ZAM/00001";
        }
    }

    private void showStatus(String message, boolean isError) {
        Platform.runLater(() -> {
            statusLabel.setText(message.toUpperCase());
            statusLabel.setStyle(isError ?
                    "-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold;" :
                    "-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold;");
        });

        Thread timer = new Thread(() -> {
            try {
                Thread.sleep(3500);
                Platform.runLater(() -> {
                    statusLabel.setText("SYSTEM GOTOWY");
                    statusLabel.setStyle("-fx-background-color: #ecf0f1; -fx-text-fill: #2c3e50; -fx-font-weight: bold; -fx-border-color: #bdc3c7; -fx-border-width: 1 0 0 0;");
                });
            } catch (InterruptedException e) {}
        });
        timer.setDaemon(true);
        timer.start();
    }

    private <T> void addContextMenu(TableView<T> table, String type) {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem deleteItem = new MenuItem("Usuń wybrany rekord");
        deleteItem.setStyle("-fx-text-fill: #c0392b; -fx-font-weight: bold;");

        deleteItem.setOnAction(e -> {
            T selectedItem = table.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                Alert confirm = new Alert(Alert.AlertType.NONE);
                confirm.setTitle("Potwierdzenie");
                confirm.setHeaderText(null);
                confirm.setContentText("Czy na pewno chcesz usunąć ten rekord?");
                confirm.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

                confirm.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.YES) {
                        try {
                            if (type.equals("product")) db.deleteProduct(((Product) selectedItem).getId());
                            else if (type.equals("supplier")) db.deleteSupplier(((Supplier) selectedItem).getId());
                            else if (type.equals("order")) db.deleteOrder(((Order) selectedItem).getId());
                            refreshAllData();
                            showStatus("USUNIĘTO POMYŚLNIE", false);
                        } catch (SQLException ex) {
                            showStatus("BŁĄD BAZY: " + ex.getMessage(), true);
                        }
                    }
                });
            }
        });
        contextMenu.getItems().add(deleteItem);
        table.setContextMenu(contextMenu);
    }

    // PANELE

    private VBox createProductPanel() {
        setupProductColumns();
        productTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        GridPane grid = new GridPane();
        grid.setHgap(15); grid.setVgap(10);
        TextField nameIn = new TextField(); TextField qtyIn = new TextField();
        TextField nettoIn = new TextField(); TextField supplierIn = new TextField();
        supplierIn.setEditable(false);
        supplierIn.setPromptText("Kliknij by wybrać...");
        supplierIn.setOnMouseClicked(e -> openSupplierPicker(supplierIn));

        grid.add(new Label("Nazwa:"), 0, 0); grid.add(nameIn, 1, 0);
        grid.add(new Label("Ilość:"), 0, 1); grid.add(qtyIn, 1, 1);
        grid.add(new Label("Netto:"), 2, 0); grid.add(nettoIn, 3, 0);
        grid.add(new Label("Dostawca:"), 2, 1); grid.add(supplierIn, 3, 1);

        Button btnAdd = new Button("DODAJ PRODUKT");
        styleButton(btnAdd);
        btnAdd.setOnAction(e -> {
            try {
                if(nameIn.getText().isEmpty()) { showStatus("WPISZ NAZWĘ!", true); return; }
                if(selectedSupplierId == -1) { showStatus("WYBIERZ DOSTAWCĘ!", true); return; }
                double netto = Double.parseDouble(nettoIn.getText().replace(",", "."));
                int qty = Integer.parseInt(qtyIn.getText());
                db.addProduct(new Product(0, nameIn.getText(), qty, netto, netto * 1.23, selectedSupplierId, supplierIn.getText(), ""));
                nameIn.clear(); qtyIn.clear(); nettoIn.clear(); supplierIn.clear();
                selectedSupplierId = -1;
                refreshAllData();
                showStatus("DODANO PRODUKT", false);
            } catch (Exception ex) { showStatus("BŁĄD DANYCH!", true); }
        });

        HBox actionPanel = createBottomPanel(grid, btnAdd);
        VBox layout = new VBox(0, productTable, actionPanel);
        VBox.setVgrow(productTable, Priority.ALWAYS);
        return layout;
    }

    private VBox createSupplierPanel() {
        setupSupplierColumns();
        supplierTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        GridPane grid = new GridPane();
        grid.setHgap(15); grid.setVgap(10);
        TextField sNameIn = new TextField(); TextField sPhoneIn = new TextField(); TextField sEmailIn = new TextField();
        grid.add(new Label("Firma:"), 0, 0); grid.add(sNameIn, 1, 0);
        grid.add(new Label("Telefon:"), 0, 1); grid.add(sPhoneIn, 1, 1);
        grid.add(new Label("E-mail:"), 2, 0); grid.add(sEmailIn, 3, 0);
        Button sBtnAdd = new Button("DODAJ DOSTAWCĘ");
        styleButton(sBtnAdd);
        sBtnAdd.setOnAction(e -> {
            try {
                if(sNameIn.getText().isEmpty()) { showStatus("WPISZ NAZWĘ FIRMY!", true); return; }
                db.addSupplier(sNameIn.getText(), sPhoneIn.getText(), sEmailIn.getText());
                sNameIn.clear(); sPhoneIn.clear(); sEmailIn.clear();
                refreshAllData();
                showStatus("DODANO DOSTAWCĘ", false);
            } catch (Exception ex) { showStatus("BŁĄD ZAPISU!", true); }
        });
        HBox actionPanel = createBottomPanel(grid, sBtnAdd);
        VBox layout = new VBox(0, supplierTable, actionPanel);
        VBox.setVgrow(supplierTable, Priority.ALWAYS);
        return layout;
    }

    private VBox createOrderPanel() {
        setupOrderColumns();
        orderTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        GridPane grid = new GridPane();
        grid.setHgap(15); grid.setVgap(10);
        TextField pNameIn = new TextField(); pNameIn.setEditable(false);
        pNameIn.setPromptText("Wybierz produkt...");
        TextField qtyIn = new TextField();
        TextField orderNumIn = new TextField(getNextOrderNumber());
        orderNumIn.setEditable(false);
        orderNumIn.setStyle("-fx-background-color: #f4f4f4; -fx-font-weight: bold;");

        TextField nettoIn = new TextField(); TextField bruttoIn = new TextField();

        pNameIn.setOnMouseClicked(e -> openProductPicker(pNameIn, nettoIn, bruttoIn));

        grid.add(new Label("Produkt:"), 0, 0); grid.add(pNameIn, 1, 0);
        grid.add(new Label("Ilość:"), 0, 1); grid.add(qtyIn, 1, 1);
        grid.add(new Label("Nr ZAM:"), 2, 0); grid.add(orderNumIn, 3, 0);
        grid.add(new Label("Netto:"), 2, 1); grid.add(nettoIn, 3, 1);
        grid.add(new Label("Brutto:"), 4, 0); grid.add(bruttoIn, 5, 0);

        Button btnOrder = new Button("ZŁÓŻ ZAMÓWIENIE");
        styleButton(btnOrder);
        btnOrder.setOnAction(e -> {
            try {
                String name = pNameIn.getText();
                if(name.isEmpty()) { showStatus("WYBIERZ PRODUKT!", true); return; }
                int reqQty = Integer.parseInt(qtyIn.getText());
                Product p = db.getAllProducts().stream().filter(i -> i.getName().equals(name)).findFirst().orElse(null);

                if (p != null && p.getQuantity() < reqQty) {
                    showStatus("BRAK TOWARU! DOSTĘPNE: " + p.getQuantity(), true);
                    return;
                }

                db.addOrder(new Order(0, name, reqQty, Double.parseDouble(nettoIn.getText()), Double.parseDouble(bruttoIn.getText()), orderNumIn.getText(), ""));
                db.updateProductQuantity(name, reqQty);
                pNameIn.clear(); qtyIn.clear(); nettoIn.clear(); bruttoIn.clear();
                refreshAllData();
                orderNumIn.setText(getNextOrderNumber());
                showStatus("ZAMÓWIENIE ZREALIZOWANE", false);
            } catch (Exception ex) { showStatus("BŁĄD DANYCH!", true); }
        });

        HBox actionPanel = createBottomPanel(grid, btnOrder);
        VBox layout = new VBox(0, orderTable, actionPanel);
        VBox.setVgrow(orderTable, Priority.ALWAYS);
        return layout;
    }


    private void openSupplierPicker(TextField targetField) {
        Stage pickerStage = new Stage();
        pickerStage.setTitle("WYBIERZ DOSTAWCĘ");
        TableView<Supplier> tempTable = new TableView<>();
        TableColumn<Supplier, String> sNameCol = new TableColumn<>("Nazwa Firmy");
        sNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        tempTable.getColumns().add(sNameCol);
        tempTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        try { tempTable.setItems(FXCollections.observableArrayList(db.getAllSuppliers())); } catch (Exception e) {}
        tempTable.setRowFactory(tv -> {
            TableRow<Supplier> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    Supplier s = row.getItem();
                    targetField.setText(s.getName());
                    selectedSupplierId = s.getId();
                    pickerStage.close();
                }
            });
            return row;
        });
        pickerStage.setScene(new Scene(new VBox(tempTable), 400, 300));
        pickerStage.show();
    }

    private void openProductPicker(TextField targetField, TextField nettoField, TextField bruttoField) {
        Stage pickerStage = new Stage();
        pickerStage.setTitle("WYBIERZ PRODUKT");
        TableView<Product> tempTable = new TableView<>();

        TableColumn<Product, String> nameCol = new TableColumn<>("Produkt");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        TableColumn<Product, Integer> qCol = new TableColumn<>("Dostępne");
        qCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        TableColumn<Product, Double> nCol = new TableColumn<>("Netto");
        nCol.setCellValueFactory(new PropertyValueFactory<>("priceNetto"));

        tempTable.getColumns().addAll(nameCol, qCol, nCol);
        tempTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        try { tempTable.setItems(FXCollections.observableArrayList(db.getAllProducts())); } catch (Exception e) {}
        tempTable.setRowFactory(tv -> {
            TableRow<Product> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    Product selected = row.getItem();
                    targetField.setText(selected.getName());
                    nettoField.setText(String.valueOf(selected.getPriceNetto()));
                    bruttoField.setText(String.valueOf(selected.getPriceBrutto()));
                    pickerStage.close();
                }
            });
            return row;
        });
        pickerStage.setScene(new Scene(new VBox(tempTable), 500, 400));
        pickerStage.show();
    }


    private HBox createBottomPanel(GridPane grid, Button btn) {
        HBox panel = new HBox(30, grid, btn);
        panel.setAlignment(Pos.CENTER_LEFT);
        panel.setPadding(new Insets(20, 30, 20, 30));
        panel.setStyle("-fx-background-color: #ffffff; -fx-border-color: #cccccc; -fx-border-width: 1 0 0 0;");
        return panel;
    }

    private void setupProductColumns() {
        TableColumn<Product, Void> lpCol = createLpColumn();
        TableColumn<Product, String> nCol = new TableColumn<>("Nazwa Produktu");
        nCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        TableColumn<Product, String> sCol = new TableColumn<>("Dostawca");
        sCol.setCellValueFactory(new PropertyValueFactory<>("supplierName"));
        TableColumn<Product, Integer> qCol = new TableColumn<>("Ilość");
        qCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        TableColumn<Product, Double> nettoCol = new TableColumn<>("Netto");
        nettoCol.setCellValueFactory(new PropertyValueFactory<>("priceNetto"));
        TableColumn<Product, Double> bruttoCol = new TableColumn<>("Brutto");
        bruttoCol.setCellValueFactory(new PropertyValueFactory<>("priceBrutto"));
        productTable.getColumns().setAll(lpCol, nCol, sCol, qCol, nettoCol, bruttoCol);
    }

    private void setupSupplierColumns() {
        TableColumn<Supplier, Void> lpCol = createLpColumn();
        TableColumn<Supplier, String> nCol = new TableColumn<>("Nazwa Firmy");
        nCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        TableColumn<Supplier, String> pCol = new TableColumn<>("Telefon");
        pCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
        TableColumn<Supplier, String> eCol = new TableColumn<>("Email");
        eCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        supplierTable.getColumns().setAll(lpCol, nCol, pCol, eCol);
    }

    private void setupOrderColumns() {
        TableColumn<Order, Void> lpCol = createLpColumn();
        TableColumn<Order, String> pCol = new TableColumn<>("Produkt");
        pCol.setCellValueFactory(new PropertyValueFactory<>("productName"));
        TableColumn<Order, Integer> qCol = new TableColumn<>("Ilość");
        qCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        TableColumn<Order, String> nCol = new TableColumn<>("Nr Zamówienia");
        nCol.setCellValueFactory(new PropertyValueFactory<>("orderNumber"));
        TableColumn<Order, String> dCol = new TableColumn<>("Data");
        dCol.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
        dCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else setText(item.contains(".") ? item.substring(0, item.lastIndexOf(".")) : item);
            }
        });
        orderTable.getColumns().setAll(lpCol, pCol, qCol, nCol, dCol);
    }

    private <T> TableColumn<T, Void> createLpColumn() {
        TableColumn<T, Void> lpCol = new TableColumn<>("Lp.");
        lpCol.setMaxWidth(50); lpCol.setMinWidth(50);
        lpCol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : String.valueOf(getIndex() + 1));
            }
        });
        return lpCol;
    }

    private void styleButton(Button btn) {
        btn.setStyle(GREEN_BUTTON_STYLE);
        btn.setMinWidth(UNIFORM_BUTTON_WIDTH);
        btn.setMaxWidth(UNIFORM_BUTTON_WIDTH);
        btn.setOnMouseEntered(e -> btn.setStyle(GREEN_BUTTON_STYLE + HOVER_STYLE));
        btn.setOnMouseExited(e -> btn.setStyle(GREEN_BUTTON_STYLE));
    }

    private void refreshAllData() {
        try {
            productTable.setItems(FXCollections.observableArrayList(db.getAllProducts()));
            supplierTable.setItems(FXCollections.observableArrayList(db.getAllSuppliers()));
            orderTable.setItems(FXCollections.observableArrayList(db.getAllOrders()));
        } catch (Exception e) {}
    }

    public static void main(String[] args) { launch(args); }
}