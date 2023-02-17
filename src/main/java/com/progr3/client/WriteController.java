package com.progr3.client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class WriteController {
    public WriteModel model;

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
        if (model.sendMail(to.getText(), object.getText(), content.getText())) {
            // handle success
            ((Stage) from.getScene().getWindow()).close();
        } else {
            // handle failure
            ((Stage) from.getScene().getWindow()).close();
        }
    }

    @FXML
    public void onCloseBtnClick(ActionEvent event) {

    }
}
