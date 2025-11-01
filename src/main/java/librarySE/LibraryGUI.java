package librarySE;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.math.BigDecimal;
import java.time.LocalDate;

public class LibraryGUI extends Application {

    private User currentUser;

    private TableView<LibraryItem> itemTable;
    private Label finesLabel;

    @Override
    public void start(Stage primaryStage) {

        // Main layout
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        // Top: Login controls
        HBox loginBox = new HBox(10);
        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        Button loginBtn = new Button("Login");
        loginBox.getChildren().addAll(usernameField, passwordField, loginBtn);
        root.setTop(loginBox);

        // Center: TableView of LibraryItems
        itemTable = new TableView<>();
        TableColumn<LibraryItem, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));

        TableColumn<LibraryItem, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(cell -> 
            javafx.beans.property.SimpleStringProperty.stringExpression(
                javafx.beans.binding.Bindings.createStringBinding(
                    () -> cell.getValue().getMaterialType().toString()
                )
            )
        );

        TableColumn<LibraryItem, Boolean> availCol = new TableColumn<>("Available");
        availCol.setCellValueFactory(cell -> 
            javafx.beans.property.SimpleBooleanProperty.booleanExpression(
                javafx.beans.binding.Bindings.createBooleanBinding(
                    () -> cell.getValue().isAvailable()
                )
            )
        );

        itemTable.getColumns().addAll(titleCol, typeCol, availCol);
        root.setCenter(itemTable);

        // Right: Buttons and actions
        VBox actionBox = new VBox(10);
        actionBox.setPadding(new Insets(0,0,0,10));
        Button borrowBtn = new Button("Borrow");
        Button returnBtn = new Button("Return");
        Button payFinesBtn = new Button("Pay Fines");
        Button addBookBtn = new Button("Add Book (Admin)");
        Button deleteItemBtn = new Button("Delete Item (Admin)");

        finesLabel = new Label("Fines: 0");

        actionBox.getChildren().addAll(borrowBtn, returnBtn, payFinesBtn, addBookBtn, deleteItemBtn, finesLabel);
        root.setRight(actionBox);

        // Scene
        Scene scene = new Scene(root, 900, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Library System");
        primaryStage.show();

        // Login action
        loginBtn.setOnAction(e -> {
            String user = usernameField.getText().trim();
            String pass = passwordField.getText().trim();
            // For demo: check Admin singleton
            try {
                Admin admin = Admin.getInstance();
                if (admin.login(user, pass)) {
                    currentUser = admin;
                    showAlert("Success", "Admin logged in!");
                }
            } catch (Exception ex) {
                // fallback: normal user
                currentUser = new User(user, Role.USER, pass, user+"@mail.com");
                showAlert("Success", "User logged in!");
            }
            refreshItems();
            updateFines();
        });

        // Borrow action
        borrowBtn.setOnAction(e -> {
            if (currentUser == null) { showAlert("Error", "Login first!"); return; }
            LibraryItem selected = itemTable.getSelectionModel().getSelectedItem();
            if (selected == null) return;
            try {
                BorrowManager.getInstance().borrowItem(currentUser, selected);
                showAlert("Success", "Item borrowed!");
                refreshItems();
            } catch (Exception ex) {
                showAlert("Error", ex.getMessage());
            }
            updateFines();
        });

        // Return action
        returnBtn.setOnAction(e -> {
            if (currentUser == null) { showAlert("Error", "Login first!"); return; }
            LibraryItem selected = itemTable.getSelectionModel().getSelectedItem();
            if (selected == null) return;
            try {
                BorrowManager.getInstance().returnItem(currentUser, selected);
                showAlert("Success", "Item returned!");
                refreshItems();
            } catch (Exception ex) {
                showAlert("Error", ex.getMessage());
            }
            updateFines();
        });

        // Pay fines action (all fines)
        payFinesBtn.setOnAction(e -> {
            if (currentUser == null) { showAlert("Error", "Login first!"); return; }
            BigDecimal total = BorrowManager.getInstance().calculateTotalFines(currentUser, LocalDate.now());
            currentUser.payFine(total);
            showAlert("Success", "Paid all fines!");
            updateFines();
        });

        // Add Book (Admin)
        addBookBtn.setOnAction(e -> {
            if (!(currentUser instanceof Admin)) { showAlert("Error", "Admin only!"); return; }
            Book book = new Book("ISBN-"+System.currentTimeMillis(),"New Book","Author");
            ItemManager.getInstance().addItem(book, (Admin) currentUser);
            refreshItems();
        });

        // Delete Item (Admin)
        deleteItemBtn.setOnAction(e -> {
            if (!(currentUser instanceof Admin)) { showAlert("Error", "Admin only!"); return; }
            LibraryItem selected = itemTable.getSelectionModel().getSelectedItem();
            if (selected == null) return;
            ItemManager.getInstance().deleteItem(selected, (Admin) currentUser);
            refreshItems();
        });
    }

    private void refreshItems() {
        itemTable.setItems(FXCollections.observableArrayList(ItemManager.getInstance().getAllItems()));
    }

    private void updateFines() {
        if (currentUser != null)
            finesLabel.setText("Fines: " + currentUser.getFineBalance());
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
