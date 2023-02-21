package com.progr3.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;


public class ClientMain extends Application {
    public static final int port = 42069;
    public static final String host = "localhost";
    public static final int waitTime = 1000;

    public void start(Stage stage) throws IOException {
        URL clientUrl = ClientMain.class.getResource("/client/login.fxml");
        FXMLLoader loader = new FXMLLoader(clientUrl);
        Scene scene = new Scene(loader.load());
        stage.setTitle("Login");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
