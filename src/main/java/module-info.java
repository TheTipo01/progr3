module com.progr3.client {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;

    opens com.progr3.client to javafx.fxml;
    exports com.progr3.client;
}
