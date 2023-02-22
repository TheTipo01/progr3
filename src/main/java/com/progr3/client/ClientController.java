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

    private ClientModel clientModel;

    private NotifyController notifyController;

    @FXML
    public void initialize() {
        notifyController = new NotifyController();

        clientModel = new ClientModel(notifyController);

        updateTitle();
        clientModel.addListenerMessages(c -> updateTitle());

        initializeTableView();
        displayContent();

        ServerListener listener = new ServerListener(clientModel);
        listener.start();
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
                clientModel.setEmailAsRead(email);
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
    }

    public void onBtnDelete() throws IOException {
        Email email = tableView.getSelectionModel().getSelectedItem();

        PopupController.showPopup("Elimina email", "Vuoi eliminare la mail con oggetto: \"" + email.getObject() + "\"?", ImageType.Warning, (ActionEvent event2) -> {
            clientModel.deleteEmail(email, ClientModel.account);
            //TODO: fare qualcosa col risultato della call: controllare se è stata eliminata o meno
        });
    }

    public void openWrite(WriteMode mode, Email email) throws IOException {
        URL clientUrl = LoginMain.class.getResource("/client/write.fxml");
        FXMLLoader loader = new FXMLLoader(clientUrl);
        Scene scene = new Scene(loader.load());
        scene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());
        WriteController writeController = loader.getController();
        writeController.setNotify(notifyController);

        if (email != null) {
            switch (mode) {
                case Reply -> writeController.setParamsReply(email);
                case ReplyAll -> writeController.setParamsReplyAll(email);
                case Forward -> writeController.setParamsForward(email);
            }
        }

        Stage stage = new Stage();
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public void onBtnWrite() throws IOException {
        openWrite(WriteMode.Normal, null);
    }

    public void onBtnReply() throws IOException {
        openWrite(WriteMode.Reply, tableView.getSelectionModel().getSelectedItem());
    }

    public void onBtnReplyAll() throws IOException {
        openWrite(WriteMode.ReplyAll, tableView.getSelectionModel().getSelectedItem());
    }

    public void onBtnForward() throws IOException {
        openWrite(WriteMode.Forward, tableView.getSelectionModel().getSelectedItem());
    }
}