package com.codingchili.ethereumingest.views;

import com.codingchili.ethereumingest.model.ApplicationConfig;
import javafx.collections.FXCollections;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import static com.codingchili.core.files.Configurations.launcher;
import static com.codingchili.ethereumingest.views.Importing.IMPORTING_FXML;

public class Settings implements ApplicationScene {
    public static final String SETTINGS_FXML = "/settings.fxml";
    private ApplicationConfig config = ApplicationConfig.get();
    @FXML
    Label version;
    @FXML
    TextField blockStart;
    @FXML
    TextField blockEnd;
    @FXML
    TextField endpoint;
    @FXML
    TextField blockIndex;
    @FXML
    TextField txIndex;
    @FXML
    CheckBox importTx;
    @FXML
    CheckBox importBlocks;
    @FXML
    ComboBox<String> storageType;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // set values from configuration.
        version.setText(launcher().getVersion());
        blockEnd.setText(config.getBlockEnd());
        blockStart.setText(config.getStartBlock());
        blockIndex.setText(config.getBlockIndex());
        txIndex.setText(config.getTxIndex());
        importTx.setSelected(config.isTxImport());
        importBlocks.setSelected(config.isBlockImport());
        endpoint.setText(config.getIpc());
        storageType.setItems(FXCollections.observableList(getStorageList()));
        storageType.getSelectionModel().select(config.getStorage().name());

        txIndex.setDisable(!config.isTxImport());
        blockIndex.setDisable(!config.isBlockImport());

        importTx.selectedProperty().addListener((ov, previous, current) -> {
            txIndex.setDisable(!current);
        });
        importBlocks.selectedProperty().addListener((ov, previous, current) -> {
            blockIndex.setDisable(!current);
        });
    }

    private List<String> getStorageList() {
        return Arrays.stream(ApplicationConfig.StorageType.values())
                .map(Enum::name)
                .collect(Collectors.toList());
    }

    @FXML
    public void startImport(Event event) {
        config.setBlockEnd(blockEnd.getText());
        config.setStartBlock(blockStart.getText());
        config.setBlockIndex(blockIndex.getText());
        config.setTxIndex(txIndex.getText());
        config.setBlockImport(importBlocks.isSelected());
        config.setTxImport(importTx.isSelected());
        config.setIpc(endpoint.getText());
        config.setStorage(
                ApplicationConfig.StorageType.valueOf(
                        storageType.getSelectionModel().getSelectedItem()));
        config.save();

        if (config.isBlockImport() || config.isTxImport()) {
            Async.setScene(IMPORTING_FXML);
        } else {
            Form.showInfoAlert(
                    "Configuration",
                    "Nothing selected for import, please select to import blocks or transactions.");
        }
    }
}
