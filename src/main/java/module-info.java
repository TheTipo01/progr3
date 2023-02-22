module com.progr3.client {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;

    opens com.progr3.client to javafx.fxml;
    exports com.progr3.client;

    opens com.progr3.server to javafx.fxml;
    exports com.progr3.server;
    exports com.progr3.server.logger;
    opens com.progr3.server.logger to javafx.fxml;
    exports com.progr3.entities;
    exports com.progr3.client.enumerations;
    opens com.progr3.client.enumerations to javafx.fxml;
}
