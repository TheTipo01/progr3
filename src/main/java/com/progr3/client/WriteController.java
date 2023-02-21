package com.progr3.client;

import com.progr3.entities.Email;
import com.progr3.entities.Packet;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WriteController {
    private WriteModel model;

    @FXML
    private TextField from;

    @FXML
    private TextField to;

    @FXML
    private TextField object;

    @FXML
    private TextArea content;

    @FXML
    private Button send;

    @FXML
    private Button close;

    @FXML
    public void initialize() {
        model = new WriteModel();
        from.setText(ClientModel.account.getAddress());
        from.setDisable(true);
    }

    @FXML
    public void onSendBtnClick(ActionEvent event) throws IOException {
        to.setDisable(false);
        object.setDisable(false);
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

    private void setParams(Email email) {
        to.setDisable(true);
        object.setText("Re: " + email.getObject());
        object.setDisable(true);
        content.setText("\n\n\n\n\n[" + email.getTimestamp() + "] " + email.getSender() + " ha inviato:\n" + email.getText());
    }

    public void setParamsReply(Email email) {
        setParams(email);
        to.setText(email.getSender());
    }

    public void setParamsReplyAll(Email email) {
        setParams(email);
        List<String> receivers = new ArrayList<>(email.getReceivers());
        receivers.remove(ClientModel.account.getAddress());

        to.setText(email.getSender() + ", " + String.join(", ", receivers));
    }

    public void setParamsForward(Email email) {
        to.setDisable(false);
        object.setText("Fwd: " + email.getObject());
        object.setDisable(true);
        content.setText("\n\n---- Messaggio Inoltrato ----\nOggetto: " + email.getObject() +
                "\nData: " + email.getTimestamp() +
                "\nMittente: " + email.getSender() +
                "\n\n\n" + email.getText());
    }
}
