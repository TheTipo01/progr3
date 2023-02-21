package com.progr3.client;

import com.progr3.entities.Packet;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;

public class WriteController {
    private WriteModel model;

    @FXML
    public TextField from;

    @FXML
    public TextField to;

    @FXML
    public TextField object;

    @FXML
    public TextArea content;

    @FXML
    public Button send;

    @FXML
    public Button close;

    @FXML
    public void initialize() {
        model = new WriteModel();
        from.setText(ClientModel.account.getAddress());
        from.setDisable(true);
    }

    @FXML
    public void onSendBtnClick(ActionEvent event) throws IOException {
        Packet result = model.sendMail(to.getText(), object.getText(), content.getText());
        switch (result.getType()) {
            case Error -> {
                boolean error = (boolean) result.getData();

                if (error) {
                    PopupController.showPopup("Errore", "Errore durante l'invio dell'email.", ImageType.Error, null);
                } else {
                    ((Stage) from.getScene().getWindow()).close();
                    PopupController.showPopup("Successo", "Email inviata con successo.", ImageType.Success, null);
                }
            }
            case ErrorPartialSend -> {
                ArrayList<String> notSent = (ArrayList) result.getData();

                ((Stage) from.getScene().getWindow()).close();

                StringBuilder addresses = new StringBuilder();
                for (String s : notSent) {
                    addresses.append(s).append(", ");
                }
                addresses = new StringBuilder(addresses.substring(0, addresses.length() - 2));

                PopupController.showPopup("Attenzione", "Email non inviata ai seguenti indirizzi:\n" + addresses, ImageType.Warning, null);
            }
        }

    }

    @FXML
    public void onCloseBtnClick(ActionEvent event) {
        ((Stage) content.getScene().getWindow()).close();
    }

    public void setNotify(NotifyController notifyController) {
        model.setNotifyController(notifyController);
    }
}
