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
        notify = new Notify();

        try {
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


        updateTitle();
        clientModel.addListenerMessages(c -> updateTitle());
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

        initializeTableView();
        displayContent();

        listener = new ServerPoller(clientModel);
        listener.start();
    }

    public void setOnCloseRequest(Stage stage) {
        stage.setOnCloseRequest(t -> {
            Platform.exit();
            listener.interrupt();
            System.exit(0);
        });
    }

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

    private void displayContent() {
        tableView.getSelectionModel().setCellSelectionEnabled(true);
        ObservableList<TablePosition> selectedCells = tableView.getSelectionModel().getSelectedCells();

        selectedCells.addListener((ListChangeListener<TablePosition>) c -> {
            if (selectedCells.size() > 0) {
                TablePosition tablePosition = selectedCells.get(0);
                Email email = clientModel.getMessage(tablePosition.getRow());
                textArea.setText(email.getText());
                receivers.setText(String.join(", ", email.getReceivers()));

                try {
                    clientModel.setEmailAsRead(email);
                } catch (Exception ignored) {
                }

                tableView.refresh();
                updateTitle();
            }
        });
    }

    public void initializeTableView() {
        TableColumn<Email, String> objectCol = new TableColumn<>("Oggetto");
        objectCol.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getObject()));

        TableColumn<Email, String> senderCol = new TableColumn<>("Mittente");
        senderCol.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getSender()));

        TableColumn<Email, Date> dateCol = new TableColumn<>("Data");
        dateCol.setCellValueFactory(p -> new SimpleObjectProperty<>(p.getValue().getTimestamp()));
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

        tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        tableView.getColumns().addAll(objectCol, senderCol, dateCol);
        clientModel.bindTableView(tableView);

        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        dateCol.setSortType(TableColumn.SortType.DESCENDING);
        tableView.getSortOrder().add(dateCol);


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

    public void onBtnDelete() throws IOException {
        Email email = tableView.getSelectionModel().getSelectedItem();

        if (email != null) {
            PopupController.showPopup("Elimina email", "Vuoi eliminare la mail con oggetto: \"" + email.getObject() + "\"?", ImageType.Warning, (ActionEvent event2) -> {
                boolean status = clientModel.deleteEmail(email, ClientModel.account);
                if (!status) {
                    try {
                        PopupController.showPopup("Errore", "Connessione al server assente.", ImageType.Error, null);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    public void openWrite(WriteMode mode, Email email) throws IOException {
        URL clientUrl = LoginMain.class.getResource("/client/write.fxml");
        FXMLLoader loader = new FXMLLoader(clientUrl);
        Scene scene = new Scene(loader.load());
        scene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());

        WriteController writeController = loader.getController();
        writeController.setNotify(notify);

        Stage stage = new Stage();
        stage.setScene(scene);
        stage.setResizable(false);

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
    }

    public void onBtnWrite() throws IOException {
        openWrite(WriteMode.Normal, null);
    }

    public void onBtnReply() throws IOException {
        Email email = tableView.getSelectionModel().getSelectedItem();
        if (email != null) {
            openWrite(WriteMode.Reply, email);
        }
    }

    public void onBtnReplyAll() throws IOException {
        Email email = tableView.getSelectionModel().getSelectedItem();
        if (email != null) {
            openWrite(WriteMode.ReplyAll, email);
        }
    }

    public void onBtnForward() throws IOException {
        Email email = tableView.getSelectionModel().getSelectedItem();
        if (email != null) {
            openWrite(WriteMode.Forward, email);
        }
    }
}