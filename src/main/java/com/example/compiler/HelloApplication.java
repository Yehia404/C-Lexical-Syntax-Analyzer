package com.example.compiler;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.util.Random;

public class HelloApplication extends Application {

    private TextArea textArea;
    private File currentFile;
    Random random;
    @Override
    public void start(Stage primaryStage) {
        long seed = System.currentTimeMillis();
        random = new Random(seed);
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #333;");

        MenuBar menuBar = new MenuBar();
        menuBar.setStyle("-fx-background-color: #666;");
        // Run Button
        Button runButton = new Button("Run");
        runButton.setStyle("-fx-background-color: #666;");
        runButton.setOnMouseEntered(e -> runButton.setStyle("-fx-background-color: #005e9e; -fx-text-fill: white;"));
        runButton.setOnMouseExited(e -> runButton.setStyle("-fx-background-color: #007ACC; -fx-text-fill: white;"));
        runButton.setOnAction(event -> {
            run();
        });

        Menu fileMenu = new Menu("File");
        fileMenu.setStyle("-fx-text-fill: white;");
        MenuItem openItem = new MenuItem("Open");
        openItem.setOnAction(e -> openFile(primaryStage));
        MenuItem saveItem = new MenuItem("Save");
        saveItem.setOnAction(e -> saveFile(primaryStage));
        MenuItem saveAsItem = new MenuItem("Save As");
        saveAsItem.setOnAction(e -> saveFileAs(primaryStage));
        fileMenu.getItems().addAll(openItem, saveItem, saveAsItem);
        menuBar.getMenus().add(fileMenu);

        textArea = new TextArea();
        textArea.setStyle("-fx-text-fill: white; -fx-control-inner-background: #333; -fx-font-size: 16px;");

        // Add components to the top of the root pane
        BorderPane topPane = new BorderPane();
        topPane.setStyle("-fx-background-color: #666;");
        topPane.setLeft(menuBar);
        topPane.setRight(runButton);
        root.setTop(topPane);
        root.setCenter(textArea);

        primaryStage.setScene(new Scene(root, 1000, 700));
        primaryStage.setTitle("Text Editor - Dark Mode");
        primaryStage.show();
    }

    private void saveFile(Stage primaryStage) {
        if(currentFile != null){
            saveContentToFile(currentFile);
        }
        else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Warning");
            alert.setHeaderText(null);
            alert.setContentText("You have to specify a file!");
            alert.showAndWait();
        }
    }

    private void run(){
        Compiler compiler = new Compiler();
        if(currentFile != null) {
            System.out.println(currentFile.getAbsolutePath());
            try {
            compiler.compile(currentFile.getAbsolutePath());
            }catch (Exception e) {
                showErrorAlert("An error occurred while compiling the file:\n" + e.getMessage());
            }
        }
        else{
            int randomNum = random.nextInt(1000);
            // Create a File object with the directory path and file name
            File file = new File("src/main/java/com/example/compiler", "test"+randomNum+".c");
            try {
                // Create the file
                if (file.createNewFile()) {
                    saveContentToFile(file);
                    try{
                    compiler.compile(file.getAbsolutePath());
                    } catch (Exception e) {
                        showErrorAlert("An error occurred while compiling the file:\n" + e.getMessage());
                    }
                } else {
                    System.out.println("File already exists.");
                }
            } catch (IOException e) {
                System.out.println("An error occurred.");
                e.printStackTrace();
            }
        }
    }
    private void openFile(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open File");
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            currentFile = file;
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                StringBuilder content = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
                textArea.setText(content.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void saveFileAs(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save As");

        // Add file extension filters
        FileChooser.ExtensionFilter txtFilter = new FileChooser.ExtensionFilter("Text Files (*.txt)", "*.txt");
        FileChooser.ExtensionFilter cFilter = new FileChooser.ExtensionFilter("C Files (*.c)", "*.c");
        fileChooser.getExtensionFilters().addAll(txtFilter, cFilter);

        // Show save dialog
        File initialDirectory = new File(System.getProperty("user.home"));
        fileChooser.setInitialDirectory(initialDirectory);

        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            if (!file.getName().contains(".")) {
                // If file extension is not specified, append the selected extension
                String selectedExtension = fileChooser.getSelectedExtensionFilter().getExtensions().get(0);
                file = new File(file.getAbsolutePath() + selectedExtension);
            }
            currentFile = file; // Update the current file
            saveContentToFile(file);
        }
    }
    private void saveContentToFile(File file) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(textArea.getText());
        } catch (IOException e) {
            e.printStackTrace();
        }
        }
    private void showErrorAlert(String errorMessage) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(errorMessage);
        // Get the dialog pane
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setMinHeight(Region.USE_PREF_SIZE);
        dialogPane.setMinWidth(600);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}