package com.progr3.server;

import com.progr3.client.Client;
import com.progr3.server.Logger.ServerLogger;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ServerMain extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Client.class.getResource("/server/view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);
        stage.setTitle("Server Log");
        stage.setScene(scene);

        List<ServerObserver> observers = new ArrayList<>();
        observers.add(new ServerLogger());


        Server server = new Server(42069, observers);
        Thread serverThread = new Thread(server);
        serverThread.start();

        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
