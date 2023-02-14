package com.progr3.client;

import com.progr3.entities.Email;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;

public class ClientController {
    @FXML
    protected TitledPane titledPane;

    @FXML
    protected TableView<Email> tableView;

    @FXML
    protected TextArea textArea;

    public static ClientModel clientModel;

    @FXML
    public void initialize() {
        clientModel = new ClientModel(titledPane);
        clientModel.viewMessagesStartup(tableView);
        clientModel.displayContent(tableView, textArea);
    }


}