package com.codingchili.ethereumingest.views;

import javafx.scene.Parent;
import javafx.scene.Scene;

import static com.codingchili.ethereumingest.views.Form.*;

/**
 * Simple base class that adds the css style to all childs and sets
 * the same width for all views.
 */
public class StyledScene extends Scene {

    public StyledScene(Parent parent) {
        super(parent, WIDTH, HEIGHT);
        getStylesheets().add(Form.css(CSS_FILE));
    }
}
