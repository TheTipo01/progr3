package com.progr3.client;

import com.progr3.entities.Account;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;

public class LoginController {

    @FXML
    public TextField email;

    @FXML
    public TextField password;

    @FXML
    public void onBtnLogin() throws Exception {
        ClientModel.account = new Account(email.getText(), password.getText());

        URL clientUrl = ClientMain.class.getResource("/client/client.fxml");
        FXMLLoader loader = new FXMLLoader(clientUrl);
        Scene scene = new Scene(loader.load(), 900, 600);
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.show();

        ((Stage) email.getScene().getWindow()).close();
    }
}
