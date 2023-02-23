package com.progr3.client;

import com.progr3.client.enumerations.ImageType;
import com.progr3.entities.Account;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.kordamp.bootstrapfx.BootstrapFX;

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
    public void onBtnLogin() {
        // Creates new account object from data inserted inside login window
        Account account = new Account(email.getText(), password.getText());
        try {
            // If the account is valid, proceeds with login process
            if (!model.verifyAccount(account)) {
                errorLabel.setVisible(false);

                // Saves the account inside the model
                ClientModel.account = account;

                // Initiates client window
                URL clientUrl = LoginMain.class.getResource("/client/client.fxml");
                FXMLLoader loader = new FXMLLoader(clientUrl);
                Scene scene = new Scene(loader.load());
                scene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());
                Stage stage = new Stage();

                // Gets controller before setting stage, to be able to set up
                // a close request needed for shutting down the window
                ClientController controller = loader.getController();
                controller.setOnCloseRequest(stage);

                stage.setScene(scene);
                stage.setResizable(false);
                stage.setTitle("Email client");
                stage.show();

                ((Stage) email.getScene().getWindow()).close();
            } else {
                errorLabel.setVisible(true);
            }
        } catch (Exception e) {
            // The exception is thrown when the server is unavailable.
            // If so, show an error popup
            PopupController.showPopup("Errore", "Connessione al server assente.", ImageType.Error, null);
        }
    }
}
