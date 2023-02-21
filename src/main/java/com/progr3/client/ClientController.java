package com.progr3.client;

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

        clientModel.addListenerMessages(c -> Platform.runLater(() -> titledPane.textProperty().setValue("Ciao, " + clientModel.getCurrentEmail() + " (" + clientModel.getMessagesSize() + " messaggi)")));

        initializeTableView();
        displayContent();

        ServerListener listener = new ServerListener(clientModel);
        listener.start();
    }

    private void displayContent() {
        tableView.getSelectionModel().setCellSelectionEnabled(true);
        ObservableList selectedCells = tableView.getSelectionModel().getSelectedCells();

        selectedCells.addListener((ListChangeListener) c -> {
            if (selectedCells.size() > 0) {
                TablePosition tablePosition = (TablePosition) selectedCells.get(0);
                Email email = clientModel.getMessage(tablePosition.getRow());
                textArea.setText(email.getText());
                receivers.setText(String.join(", ", email.getReceivers()));
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

        dateCol.setSortType(TableColumn.SortType.DESCENDING);
        tableView.getSortOrder().add(dateCol);
    }

    public void onBtnWrite(ActionEvent event) throws IOException {
        URL clientUrl = ClientMain.class.getResource("/client/write.fxml");
        FXMLLoader loader = new FXMLLoader(clientUrl);
        Scene scene = new Scene(loader.load());
        WriteController writeController = loader.getController();
        writeController.setNotify(notifyController);
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public void onBtnDelete(ActionEvent event) throws IOException {
        Email email = tableView.getSelectionModel().getSelectedItem();

        PopupController.showPopup("Elimina email", "Vuoi eliminare la mail con oggetto: \"" + email.getObject() + "\"?", ImageType.Warning, (ActionEvent event2) -> {
            clientModel.deleteEmail(email, ClientModel.account);
        });
    }

    public void onBtnReply(ActionEvent event) throws IOException {
        Email email = tableView.getSelectionModel().getSelectedItem();

        URL clientUrl = ClientMain.class.getResource("/client/write.fxml");
        FXMLLoader loader = new FXMLLoader(clientUrl);
        Scene scene = new Scene(loader.load());

        WriteController writeController = loader.getController();
        writeController.setNotify(notifyController);
        writeController.setParamsReply(email);

        Stage stage = new Stage();
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public void onBtnReplyAll(ActionEvent event) throws IOException {
        Email email = tableView.getSelectionModel().getSelectedItem();

        URL clientUrl = ClientMain.class.getResource("/client/write.fxml");
        FXMLLoader loader = new FXMLLoader(clientUrl);
        Scene scene = new Scene(loader.load());

        WriteController writeController = loader.getController();
        writeController.setNotify(notifyController);
        writeController.setParamsReplyAll(email);

        Stage stage = new Stage();
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public void onBtnForward(ActionEvent event) throws IOException {
        Email email = tableView.getSelectionModel().getSelectedItem();

        URL clientUrl = ClientMain.class.getResource("/client/write.fxml");
        FXMLLoader loader = new FXMLLoader(clientUrl);
        Scene scene = new Scene(loader.load());

        WriteController writeController = loader.getController();
        writeController.setNotify(notifyController);
        writeController.setParamsForward(email);

        Stage stage = new Stage();
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }
}