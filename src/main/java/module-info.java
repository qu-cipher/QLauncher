module ir.qcipher.qlauncher {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.antdesignicons;
    requires java.logging;
    requires com.fasterxml.jackson.databind;

    opens ir.qcipher.qlauncher.controllers to javafx.fxml;
    opens ir.qcipher.qlauncher.extra.jsonOBJ to com.fasterxml.jackson.databind;

    exports ir.qcipher.qlauncher;
    exports ir.qcipher.qlauncher.controllers;
    exports ir.qcipher.qlauncher.windows;
    exports ir.qcipher.qlauncher.utils;
    exports ir.qcipher.qlauncher.extra;
    opens ir.qcipher.qlauncher.extra.jsonOBJ.libraries to com.fasterxml.jackson.databind;
}