module hr.javafx.business.businessproposalsystem {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;
    requires jdk.jdi;
    requires bcrypt;
    requires org.slf4j;


    opens hr.javafx.business.businessproposalsystem to javafx.fxml;
    exports hr.javafx.business.businessproposalsystem;
    exports controller;
    opens controller to javafx.fxml;
}