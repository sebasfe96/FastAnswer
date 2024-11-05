module com.answer.fastanswer {
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;
    requires javafx.web;
    requires java.desktop;
    requires java.logging;
    requires jnativehook;

    opens com.answer.fastanswer2 to javafx.fxml;
    opens com.answer.fastanswer2.models to javafx.base;
    exports com.answer.fastanswer2;
}