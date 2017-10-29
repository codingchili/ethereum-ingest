package com.codingchili.ethereumingest.views;

import javafx.fxml.Initializable;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Makes the initialize method optional.
 */
public interface ApplicationScene extends Initializable {

    default void initialize(URL location, ResourceBundle resources) {
        // do nothing.
    }
}
