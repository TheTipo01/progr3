package com.progr3.server;

import com.progr3.client.LoginMain;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ServerMain extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(LoginMain.class.getResource("/server/view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);
        stage.setTitle("Server Log");
        stage.setScene(scene);

        stage.setOnCloseRequest(t -> {
            Platform.exit();
            System.exit(0);
        });

        List<ServerObserver> observers = new ArrayList<>();
        observers.add(fxmlLoader.getController());

        Server server = new Server(42069, observers);
        Thread serverThread = new Thread(server);
        serverThread.start();

        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
