package com.progr3.client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

public class PopupController {
    @FXML
    public Label title;

    @FXML
    public Label content;

    @FXML
    public ImageView image;

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

    public void onOkButtonPress(ActionEvent event) {
        ((Stage) content.getScene().getWindow()).close();
    }

    //TODO: maybe reworkare in MVC? idk se è interamente corretto? bruh
    public static void showPopup(String title, String content, ImageType image) throws IOException {
        URL clientUrl = ClientMain.class.getResource("/client/popup.fxml");
        FXMLLoader loader = new FXMLLoader(clientUrl);
        Scene scene = new Scene(loader.load());
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.setTitle(title);

        PopupController controller = loader.getController();

        controller.setContent(content);
        controller.setImage(image);

        stage.show();
    }
}
