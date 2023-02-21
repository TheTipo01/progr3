package com.progr3.client;

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

    public void setContent(String content) {
        this.content.setText(content);
    }

    public void setImage(ImageType image) {
        switch (image) {
            case Error -> {
                this.image.setImage(new Image(Objects.requireNonNull(ClientMain.class.getResourceAsStream("/icons/x-octagon-fill.png"))));
            }
            case Success -> {
                this.image.setImage(new Image(Objects.requireNonNull(ClientMain.class.getResourceAsStream("/icons/check-circle-fill.png"))));
            }
            case Warning -> {
                this.image.setImage(new Image(Objects.requireNonNull(ClientMain.class.getResourceAsStream("/icons/exclamation-triangle-fill.png"))));
            }
        }
    }

    public void setYes(EventHandler<ActionEvent> event) {
        yes.setVisible(true);
        yes.setOnAction((event1) -> {
            event.handle(event1);
            // Called to close the popup
            onCloseButton(event1);
        });
    }

    public void onCloseButton(ActionEvent event) {
        ((Stage) content.getScene().getWindow()).close();
    }

    // TODO: maybe reworkare in MVC? idk se è interamente corretto? bruh
    public static void showPopup(String title, String content, ImageType image, EventHandler<ActionEvent> event) throws IOException {
        URL clientUrl = ClientMain.class.getResource("/client/popup.fxml");
        FXMLLoader loader = new FXMLLoader(clientUrl);
        Scene scene = new Scene(loader.load());
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
