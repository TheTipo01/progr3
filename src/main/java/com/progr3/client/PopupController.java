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

import java.net.URL;
import java.util.Objects;

public class PopupController {
    @FXML
    private Label content;

    @FXML
    private ImageView image;

    @FXML
    private Button yes;

    @FXML
    private Button close;

    private void setContent(String content) {
        this.content.setText(content);
    }

    /**
     * Sets the correct image for the popup
     *
     * @param image The type of image to set
     */
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

    /**
     * Function used to show the "Si" button for confirmation inside the popup
     *
     * @param event The function called before closing the popup
     */
    private void setYes(EventHandler<ActionEvent> event) {
        yes.setVisible(true);
        yes.setOnAction((event1) -> {
            event.handle(event1);
            // Called to close the popup
            onCloseButton();
        });
    }

    private void centerCloseButton() {
        close.setLayoutX(close.getLayoutX() - 65);
    }

    public void onCloseButton() {
        ((Stage) content.getScene().getWindow()).close();
    }

    /**
     * Main method responsible for showing the popup on screen
     *
     * @param title   String value of popup title
     * @param content String value of popup text
     * @param image   ImageType enumeration for icon selection
     * @param event   Function that handles popup confirmation
     */
    public static void showPopup(String title, String content, ImageType image, EventHandler<ActionEvent> event) {
        try {
            URL clientUrl = LoginMain.class.getResource("/client/popup.fxml");
            FXMLLoader loader = new FXMLLoader(clientUrl);
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet());
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle(title);
            stage.setResizable(false);

            // Move the popup over the ClientMain window
            try {
                stage.setX(ClientController.getX() + 250);
                stage.setY(ClientController.getY() + 100);
            } catch (Exception ignored) {
                // If the popup is called from the login window, the ClientController class is not initialized
            }

            PopupController controller = loader.getController();

            controller.setContent(content);
            controller.setImage(image);

            // Shows the confirmation button only if needed
            // (only if an action is binded to the popup)
            if (event != null) {
                controller.setYes(event);
            } else {
                controller.centerCloseButton();
            }

            stage.show();
        } catch (Exception ignored) {
            // The only method that can generate an exception is the loading of the resource, but it can't because the
            // .fxml file exists
        }
    }
}
