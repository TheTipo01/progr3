package com.progr3.client;

import com.progr3.client.enumerations.ImageType;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.kordamp.bootstrapfx.BootstrapFX;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

public class PopupController {
    @FXML
    private Label content;

    @FXML
    private ImageView image;

    @FXML
    private Button yes;

    private void setContent(String content) {
        this.content.setText(content);
    }

    private void setImage(ImageType image) {
        switch (image) {
            case Error ->
                    this.image.setImage(new Image(Objects.requireNonNull(LoginMain.class.getResourceAsStream("/icons/x-octagon-fill.png"))));
            case Success ->
                    this.image.setImage(new Image(Objects.requireNonNull(LoginMain.class.getResourceAsStream("/icons/check-circle-fill.png"))));
            case Warning ->
                    this.image.setImage(new Image(Objects.requireNonNull(LoginMain.class.getResourceAsStream("/icons/exclamation-triangle-fill.png"))));
        }
    }

    private void setYes(EventHandler<ActionEvent> event) {
        yes.setVisible(true);
        yes.setOnAction((event1) -> {
            event.handle(event1);
            // Called to close the popup
            onCloseButton();
        });
    }

    public void onCloseButton() {
        ((Stage) content.getScene().getWindow()).close();
    }

    public static void showPopup(String title, String content, ImageType image, EventHandler<ActionEvent> event) throws IOException {
        URL clientUrl = LoginMain.class.getResource("/client/popup.fxml");
        FXMLLoader loader = new FXMLLoader(clientUrl);
        Scene scene = new Scene(loader.load());
        scene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.setTitle(title);
        stage.setResizable(false);

        PopupController controller = loader.getController();

        controller.setContent(content);
        controller.setImage(image);
        if (event != null) {
            controller.setYes(event);
        }

        stage.show();
    }
}
