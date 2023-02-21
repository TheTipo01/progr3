package com.progr3.client;

import com.progr3.entities.Email;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class ClientController {
    @FXML
    protected TitledPane titledPane;

    @FXML
    protected TableView<Email> tableView;

    @FXML
    protected TextArea textArea;

    private ClientModel clientModel;

    private NotifyController notifyController;

    @FXML
    public void initialize() {
        notifyController = new NotifyController();

        clientModel = new ClientModel(titledPane, tableView, textArea, notifyController);

        // Start the thread to watch for new emails
        ServerListener listener = new ServerListener(clientModel);
        listener.start();
    }

    public void onBtnWrite(ActionEvent event) throws IOException {
        URL clientUrl = ClientMain.class.getResource("/client/write.fxml");
        FXMLLoader loader = new FXMLLoader(clientUrl);
        Scene scene = new Scene(loader.load());
        WriteController writeController = loader.getController();
        writeController.setNotify(notifyController);
        Stage stage = new Stage();
        stage.setScene(scene);
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
        stage.show();
    }
}