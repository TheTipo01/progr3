package com.progr3.client;

import com.progr3.client.enumerations.ImageType;
import com.progr3.entities.Email;
import com.progr3.entities.Packet;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

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
    public void initialize() {
        model = new WriteModel();
        from.setText(ClientModel.account.getAddress());
        from.setDisable(true);
    }

    /**
     * Action performed when clicking the "Invia" button inside a Write view
     */
    @FXML
    public void onSendBtnClick() {
        to.setDisable(false);
        object.setDisable(false);

        // Creates the email from the form data and checks
        // its validity through the model
        Email email = model.formatEmail(to.getText(), object.getText(), content.getText());
        if (email == null) {
            // email is null when it's invalid: if so, show error popup
            PopupController.showPopup("Errore", "Una delle email non è valida!", ImageType.Error, null);
            return;
        }

        // Create a pop-up with the result of a send operation, base on the type of error
        Packet result = model.sendMail(email);
        switch (result.getType()) {
            case Error -> {
                // The error packet to check if an email was correctly sent
                boolean error = (boolean) result.getData();

                if (error) {
                    PopupController.showPopup("Errore", "Email non esistente!", ImageType.Error, null);
                } else {
                    ((Stage) from.getScene().getWindow()).close();
                    PopupController.showPopup("Successo", "Email inviata con successo.", ImageType.Success, null);
                }
            }
            case ErrorPartialSend -> {
                // The ErrorPartialSend packet returns a list of all the addresses
                // that are invalid when sending an email
                ArrayList<String> notSent = (ArrayList) result.getData();

                ((Stage) from.getScene().getWindow()).close();

                // We create a String with the incorrect addresses
                StringBuilder addresses = new StringBuilder();
                for (String s : notSent) {
                    addresses.append(s).append(", ");
                }
                addresses = new StringBuilder(addresses.substring(0, addresses.length() - 2));

                // And show the user these addresses inside a popup after
                // having sent the email. The email will still be sent, but not to
                // the shown addresses.
                PopupController.showPopup("Attenzione", "Email non inviata ai seguenti indirizzi:\n" + addresses, ImageType.Warning, null);
            }
            case ConnectionError ->
                    PopupController.showPopup("Errore", "Connessione al server assente.", ImageType.Error, null);
        }
    }

    @FXML
    public void onCloseBtnClick() {
        ((Stage) content.getScene().getWindow()).close();
    }

    public void setNotify(Notify notify) {
        model.setNotify(notify);
    }

    /**
     * Pre-compiles the content and the "to" field. Used by the other methods
     * that handle the Reply, Reply All and Forward functions.
     *
     * @param email email selected by the user
     */
    private void setParams(Email email) {
        to.setDisable(true);
        object.setText("Re: " + email.getObject());
        object.setDisable(true);
        content.setText("\n\n\n\n\n[" + email.getTimestamp() + "] " + email.getSender() + " ha inviato:\n" + email.getText());
    }

    /**
     * Pre-compiles the content and the "to" field for a reply
     *
     * @param email The email from where to get the "to" field
     */
    public void setParamsReply(Email email) {
        setParams(email);
        to.setText(email.getSender());
    }

    /**
     * Pre-compiles the content and the "to" field for a reply to everyone
     *
     * @param email The email from where to get the "to" field
     */
    public void setParamsReplyAll(Email email) {
        setParams(email);
        List<String> receivers = new ArrayList<>(email.getReceivers());
        receivers.remove(ClientModel.account.getAddress());

        if (receivers.size() > 0) {
            to.setText(email.getSender() + ", " + String.join(", ", receivers));
        } else {
            to.setText(email.getSender());
        }
    }

    /**
     * Pre-compiles the object and content field for a forward
     *
     * @param email The email from where to get the "to" field
     */
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
