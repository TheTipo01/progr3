package com.progr3.client;

import com.progr3.client.enumerations.ImageType;
import com.progr3.client.enumerations.WriteMode;
import com.progr3.entities.Email;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.kordamp.bootstrapfx.BootstrapFX;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ClientController {
    private static Stage stage;

    @FXML
    private TextField receivers;

    @FXML
    private TitledPane titledPane;

    @FXML
    private TableView<Email> tableView;

    @FXML
    private TextArea textArea;

    @FXML
    private Label status;

    private ClientModel clientModel;

    private Notify notify;

    private ServerPoller listener;

    @FXML
    public void initialize() {
        // Initializing class responsible for notifying the user for new mail
        notify = new Notify();

        try {
            // Initializing the model
            clientModel = new ClientModel(notify);
        } catch (Exception e) {
            try {
                PopupController.showPopup("Errore", "Impossibile caricare le email. Riprovare?", ImageType.Error, (event) -> {
                    initialize();
                });
            } catch (Exception ignored) {
            }
            return;
        }


        // Updating the title to include the number of messages in the inbox
        // As well as adding a listener to it inside the model, so that it can dynamically change
        updateTitle();
        clientModel.addListenerMessages(c -> updateTitle());

        // Listener that checks for server availability
        clientModel.addListenerOnline(((observable, oldValue, newValue) -> {
            Platform.runLater(() -> {
                if (newValue) {
                    status.setText("Online");
                    status.getStyleClass().remove("lbl-danger");
                    status.getStyleClass().add("lbl-success");
                } else {
                    status.setText("Offline");
                    status.getStyleClass().remove("lbl-success");
                    status.getStyleClass().add("lbl-danger");
                }
            });
        }));

        // Initializing main client content
        initializeTableView();
        displayContent();

        // Initializing and starting the listener for new mail
        listener = new ServerPoller(clientModel);
        listener.start();
    }

    /**
     * Function that sets the actions to do when closing the window
     *
     * @param stage
     */
    public void setOnCloseRequest(Stage stage) {
        stage.setOnCloseRequest(t -> {
            Platform.exit();
            listener.interrupt();
            System.exit(0);
        });

        ClientController.stage = stage;
    }

    /**
     * Function used to update title bar; shows correct number of total
     * and unread number of emails in inbox
     */
    public void updateTitle() {
        Platform.runLater(() -> {
            int unread = clientModel.getNotReadMessages();
            if (unread > 0) {
                titledPane.textProperty().setValue("Ciao, " + clientModel.getCurrentEmail() +
                        " (" + clientModel.getMessagesSize() + " messaggi, di cui " + unread + " non letti)");
            } else {
                titledPane.textProperty().setValue("Ciao, " + clientModel.getCurrentEmail() +
                        " (" + clientModel.getMessagesSize() + " messaggi)");
            }
        });
    }

    /**
     * This method is used after initializing the TableView, and it is responsible
     * of adding main functionality of the TableView, such as changing the
     * content's value when clicking on a row.
     */
    private void displayContent() {
        tableView.getSelectionModel().setCellSelectionEnabled(true);
        ObservableList<TablePosition> selectedCells = tableView.getSelectionModel().getSelectedCells();

        // Listener needed to change content and table details when
        // selecting a table entry
        selectedCells.addListener((ListChangeListener<TablePosition>) c -> {
            if (selectedCells.size() > 0) {
                TablePosition tablePosition = selectedCells.get(0);
                Email email = clientModel.getMessage(tablePosition.getRow());
                textArea.setText(email.getText());
                receivers.setText(String.join(", ", email.getReceivers()));

                clientModel.setEmailAsRead(email);

                tableView.refresh();
                updateTitle();
            }
        });
    }

    /**
     * This method initialize the TableView
     */
    public void initializeTableView() {
        TableColumn<Email, String> objectCol = new TableColumn<>("Oggetto");
        objectCol.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getObject()));

        TableColumn<Email, String> senderCol = new TableColumn<>("Mittente");
        senderCol.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getSender()));

        TableColumn<Email, Date> dateCol = new TableColumn<>("Data");
        dateCol.setCellValueFactory(p -> new SimpleObjectProperty<>(p.getValue().getTimestamp()));

        // Setting proper data formatting
        dateCol.setCellFactory(column -> new TableCell<>() {
            private final SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

            @Override
            protected void updateItem(Date item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(format.format(item));
                }
            }
        });

        // Adding created columns to table
        tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        tableView.getColumns().addAll(objectCol, senderCol, dateCol);

        // Binding our messages to the TableView through the model
        clientModel.bindTableView(tableView);

        // Maximizes column size
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Setting sort policy for date
        dateCol.setSortType(TableColumn.SortType.DESCENDING);
        tableView.getSortOrder().add(dateCol);

        // Set unread emails bold
        tableView.setRowFactory(new Callback<>() {
            @Override
            public TableRow<Email> call(TableView<Email> email) {
                return new TableRow<>() {
                    @Override
                    protected void updateItem(Email item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null) {
                            if (item.isRead()) {
                                setStyle("");
                            } else {
                                setStyle("-fx-font-weight: bold");
                            }
                        }
                    }
                };
            }
        });
    }

    /**
     * Action performed when the "Cancella" button is pressed.
     */
    public void onBtnDelete() {
        // Getting email from the TableView
        Email email = tableView.getSelectionModel().getSelectedItem();

        // If no item is selected in the TableView, email is null, so nothing happens
        if (email != null) {
            // Confirmation popup
            PopupController.showPopup("Elimina email", "Vuoi eliminare la mail con oggetto: \"" + email.getObject() + "\"?", ImageType.Warning, (ActionEvent event2) -> {
                // If "Si" is selected, proceed with email (file) deletion
                boolean status = clientModel.deleteEmail(email, ClientModel.account);
                if (!status) {
                    // status = deletion result; if False, server is unavailable: show error popup
                    PopupController.showPopup("Errore", "Connessione al server assente.", ImageType.Error, null);
                }
            });
        }
    }

    /**
     * Creates scene for writing an email, according to the write mode.
     *
     * @param mode  The write mode
     * @param email The email to reply to
     */
    public void openWrite(WriteMode mode, Email email) {
        try {
            URL clientUrl = LoginMain.class.getResource("/client/write.fxml");
            FXMLLoader loader = new FXMLLoader(clientUrl);
            Scene scene = new Scene(loader.load());

            scene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());

            WriteController writeController = loader.getController();
            writeController.setNotify(notify);

            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setResizable(false);

            // Move the popup over the ClientMain window
            stage.setX(getX() + 100);
            stage.setY(getY());

            // Mode selector: uses different methods for each mode to properly fill
            // details inside the Write view, and sets the title accordingly
            switch (mode) {
                case Reply -> {
                    writeController.setParamsReply(email);
                    stage.setTitle("Rispondi ad una email");
                }
                case ReplyAll -> {
                    writeController.setParamsReplyAll(email);
                    stage.setTitle("Rispondi ad una email");
                }
                case Forward -> {
                    writeController.setParamsForward(email);
                    stage.setTitle("Inoltra una email");
                }
                case Normal -> stage.setTitle("Scrivi una email");
            }

            stage.show();
        } catch (IOException ignored) {
            // This exception is never thrown, since the FXML file is always found
        }
    }

    /**
     * Action performed when the "Scrivi" button is pressed.
     */
    public void onBtnWrite() throws IOException {
        openWrite(WriteMode.Normal, null);
    }

    /**
     * Action performed when the "Rispondi" button is pressed.
     * Doesn't work if no email is selected in the TableView.
     */
    public void onBtnReply() {
        Email email = tableView.getSelectionModel().getSelectedItem();
        if (email != null) {
            openWrite(WriteMode.Reply, email);
        }
    }

    /**
     * Action performed when the "Rispondi a tutti" button is pressed.
     * Doesn't work if no email is selected in the TableView.
     */
    public void onBtnReplyAll() {
        Email email = tableView.getSelectionModel().getSelectedItem();
        if (email != null) {
            openWrite(WriteMode.ReplyAll, email);
        }
    }

    /**
     * Action performed when the "Inoltra" button is pressed.
     * Doesn't work if no email is selected in the TableView.
     */
    public void onBtnForward() {
        Email email = tableView.getSelectionModel().getSelectedItem();
        if (email != null) {
            openWrite(WriteMode.Forward, email);
        }
    }

    /**
     * Obtains the X coordinate of the Client window.
     *
     * @return X coordinate
     */
    public static double getX() {
        return stage.getX();
    }

    /**
     * Obtains the Y coordinate of the Client window.
     *
     * @return Y coordinate
     */
    public static double getY() {
        return stage.getY();
    }
}