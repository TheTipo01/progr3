package com.progr3.client;

import com.progr3.entities.Account;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;

public class LoginController {
    private LoginModel model;

    @FXML
    private TextField email;

    @FXML
    private TextField password;

    @FXML
    private Label errorLabel;

    @FXML
    public void initialize() {
        model = new LoginModel();
        errorLabel.setVisible(false);
    }

    @FXML
    public void onBtnLogin() throws Exception {
        Account account = new Account(email.getText(), password.getText());
        if (!model.verifyAccount(account)) {
            errorLabel.setVisible(false);

            ClientModel.account = account;

            URL clientUrl = ClientMain.class.getResource("/client/client.fxml");
            FXMLLoader loader = new FXMLLoader(clientUrl);
            Scene scene = new Scene(loader.load());
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setResizable(false);
            stage.show();

            ((Stage) email.getScene().getWindow()).close();
        } else {
            errorLabel.setVisible(true);
        }
    }


}
