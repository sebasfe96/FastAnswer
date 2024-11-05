package com.answer.fastanswer2;

import com.answer.fastanswer2.models.Message;
import com.answer.fastanswer2.models.MessageProperties;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

public class MessageController {

    @FXML
    private HBox vboxMessages = new HBox();

    @FXML
    private TableView<MessageProperties> messageTable = new TableView<>();

    @FXML
    private TableColumn<MessageProperties, Integer> idColumn = new TableColumn<>();

    @FXML
    private TableColumn<MessageProperties, String> nameColumn = new TableColumn<>();

    @FXML
    private TableColumn<MessageProperties, String> messageColumn =  new TableColumn<>();

    @FXML
    private TableColumn<MessageProperties, String> dateColumn = new TableColumn<>();

    @FXML
    private TableColumn<MessageProperties, Void> deleteColumn = new TableColumn<>();

    @FXML
    private TableColumn<MessageProperties, Void> editColumn = new TableColumn<>();

    @FXML
    private TextField messageInput;

    @FXML
    private TextField nameInput;

    private static final String DIRECTORY_PATH = "C:/Messages/"; // Cambia esto por la ruta deseada
    private static final String FILE_NAME = "messages.txt";
    private static final Path FILE_PATH = Paths.get(DIRECTORY_PATH, FILE_NAME);

    private ObservableList<Message> messageList = FXCollections.observableArrayList();

    private Stage messageStage;

    private int currentId = 0;


    @FXML
    public void initialize() {
        createFileIfNotExists();
        loadMessages();
        idColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject());
        messageColumn.setCellValueFactory(cellData -> cellData.getValue().messageProperty());
        dateColumn.setCellValueFactory(cellData -> cellData.getValue().dateProperty());
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        if(!messageList.isEmpty()){
            currentId = messageList.get(messageList.size() - 1).getId() + 1;
            messageTable.setItems(convertToPropertiesList(messageList));
            deleteButtomConfirm();
            editButtonSetup();
            messageLoad(); // Cargar los mensajes y añadirlos a la vista
        }else currentId = 1;
    }

    @FXML
    public void messageLoadView() {
        Platform.runLater(() -> {
            if (messageStage != null) {
                if (messageStage.isIconified()) {
                    messageStage.setIconified(false);
                }
                messageStage.toFront();
                messageStage.requestFocus();
                return;
            }
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("MessageView.fxml"));
                Parent root = fxmlLoader.load();

                messageStage = new Stage();
                messageStage.setTitle("Mensajes");
                messageStage.setScene(new Scene(root, 600, 100));
                messageStage.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void messageLoad() {
        vboxMessages.getChildren().clear();
        for (Message message : messageList) {
            Button messageButton = new Button(message.getName());
            messageButton.setOnAction(event -> sendMessageToWhatsApp(message.getMessage(), messageButton));
            vboxMessages.getChildren().add(messageButton);
        }
    }

    private void editButtonSetup() {
        editColumn.setCellFactory(new Callback<>() {
            @Override
            public TableCell<MessageProperties, Void> call(final TableColumn<MessageProperties, Void> param) {
                return new TableCell<>() {
                    private final Button btn = new Button("Modificar");

                    {
                        btn.setOnAction(event -> {
                            MessageProperties message = getTableView().getItems().get(getIndex());
                            openEditDialog(message); // Abrir diálogo de edición
                        });
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);  // No mostrar nada si la fila está vacía
                        } else {
                            setGraphic(btn);  // Mostrar el botón si la fila no está vacía
                        }
                    }
                };
            }
        });
    }

    private void openEditDialog(MessageProperties message) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Editar Mensaje");

        VBox dialogVBox = new VBox(10);

        TextField nameField = new TextField(message.nameProperty().getValue());
        nameField.setPromptText("Editar nombre del botón");

        TextField messageField = new TextField(message.messageProperty().getValue());
        messageField.setPromptText("Editar mensaje");

        Button saveButton = new Button("Guardar Cambios");
        saveButton.setOnAction(event -> {

            message.nameProperty().setValue(nameField.getText());
            message.messageProperty().setValue(messageField.getText());

            updateMessageInFile(message);

            dialog.close();

            messageTable.refresh();
        });

        dialogVBox.getChildren().addAll(new Label("Nombre del Botón:"), nameField, new Label("Mensaje:"), messageField, saveButton);
        Scene dialogScene = new Scene(dialogVBox, 300, 200);
        dialog.setScene(dialogScene);
        dialog.show();
    }

    private void updateMessageInFile(MessageProperties updatedMessage) {
        File originalFile = new File(FILE_PATH.toString());
        File tempFile = new File(FILE_PATH + ".tmp");

        try (BufferedReader reader = new BufferedReader(new FileReader(originalFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {

            String line;
            boolean isUpdated = false;

            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");
                if (fields.length >= 4) { // Asegurarse de que la línea tenga todos los campos
                    String messageId = fields[0];

                    if (messageId.equals(String.valueOf(updatedMessage.idProperty().getValue()))) {
                        writer.write(updatedMessage.idProperty().getValue() + "," +
                                updatedMessage.messageProperty().getValue() + "," +
                                updatedMessage.dateProperty().getValue() + "," +
                                updatedMessage.nameProperty().getValue());
                        isUpdated = true;
                    } else {
                        writer.write(line);
                    }
                    writer.newLine();
                }
            }

            if (!isUpdated) {
                System.out.println("No se encontró el mensaje con el ID: " + updatedMessage.idProperty().getValue());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        if (originalFile.delete()) {
            if (!tempFile.renameTo(originalFile)) {
                System.out.println("No se pudo renombrar el archivo temporal.");
            }
        } else {
            System.out.println("No se pudo eliminar el archivo original.");
        }
    }

    private void sendMessageToWhatsApp(String message, Button sendButton) {
        sendButton.setDisable(true);

        Stage stage = (Stage) vboxMessages.getScene().getWindow();

        Platform.runLater(() -> stage.setIconified(true));

        new Thread(() -> {
            try {
                StringSelection stringSelection = new StringSelection(message);
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(stringSelection, null);

                Thread.sleep(100);

                Robot robot = new Robot();
                robot.setAutoDelay(100);
                robot.keyPress(KeyEvent.VK_CONTROL);
                robot.keyPress(KeyEvent.VK_V);
                robot.keyRelease(KeyEvent.VK_V);
                robot.keyRelease(KeyEvent.VK_CONTROL);

                robot.keyPress(KeyEvent.VK_ENTER);
                robot.keyRelease(KeyEvent.VK_ENTER);

            } catch (AWTException | InterruptedException e) {
                e.printStackTrace();
            } finally {
                Platform.runLater(() -> {
                    sendButton.setDisable(false);

                    if (stage.isIconified()) {
                        stage.setIconified(false);
                    }
                    stage.toFront();
                    stage.requestFocus();
                });
            }
        }).start();
    }


    private void deleteButtomConfirm() {
        deleteColumn.setCellFactory(new Callback<>() {
            @Override
            public TableCell<MessageProperties, Void> call(final TableColumn<MessageProperties, Void> param) {
                return new TableCell<>() {

                    private final Button btn = new Button("Eliminar");

                    {
                        btn.setOnAction(event -> {
                            MessageProperties message = getTableView().getItems().get(getIndex());
                            messageTable.getItems().remove(message);
                            deleteMessageFromFile(message);
                            messageTable.setItems(convertToPropertiesList(messageList));
                        });
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);  // No mostrar nada si la fila está vacía
                        } else {
                            setGraphic(btn);  // Mostrar el botón si la fila no está vacía
                        }
                    }
                };
            }
        });
    }

    private void deleteMessageFromFile(MessageProperties message) {
        // Elimina de messageList
        messageList.removeIf(msg -> msg.getId() == message.idProperty().getValue());

        File originalFile = new File(FILE_PATH.toString());
        File tempFile = new File(FILE_PATH + ".tmp");

        boolean isDeleted = false; // Para verificar si se eliminó alguna línea

        try (BufferedReader reader = new BufferedReader(new FileReader(originalFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {

            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");
                if (fields.length > 0) {
                    String messageId = fields[0];

                    if (!messageId.equals(message.idProperty().getValue().toString())) {
                        writer.write(line);
                        writer.newLine();
                    } else {
                        isDeleted = true;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (isDeleted) {
            if (originalFile.delete()) {
                if (!tempFile.renameTo(originalFile)) {
                    System.out.println("No se pudo renombrar el archivo temporal.");
                } else {
                    System.out.println("Archivo actualizado correctamente.");
                    messageTable.setItems(convertToPropertiesList(messageList));
                }
            } else {
                System.out.println("No se pudo eliminar el archivo original.");
            }
        } else {
            tempFile.delete();
        }
    }

    private void createFileIfNotExists() {
        try {
            if (!Files.exists(Paths.get(DIRECTORY_PATH))) {
                Files.createDirectories(Paths.get(DIRECTORY_PATH));
            }
            if (!Files.exists(FILE_PATH)) {
                Files.createFile(FILE_PATH);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadMessages() {
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH.toString()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(","); // Separa los campos por coma
                if (parts.length == 4) {
                    int id = Integer.parseInt(parts[0]);
                    String messageContent = parts[1];
                    String createdAt = parts[2];
                    String nameMessage = parts[3];
                    Message message = new Message(id, messageContent, LocalDateTime.parse(createdAt),nameMessage);
                    messageList.add(message);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onAddMessage() {
        String newMessage = messageInput.getText(); // Obtiene el mensaje del área de texto
        String newName = nameInput.getText(); // Obtiene el nombre del botón del campo de texto

        if (!newMessage.isEmpty() && !newName.isEmpty()) {
            Message message = new Message(currentId++, newMessage, LocalDateTime.now(), newName);
            messageList.add(message);
            messageInput.clear();
            nameInput.clear();

            messageTable.setItems(convertToPropertiesList(messageList)); // Actualizar la tabla
            saveMessageToFile(message);
            deleteButtomConfirm(); // Configurar botón eliminar
        } else {
            // Mostrar una alerta si alguno de los campos está vacío
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Campos Vacíos");
            alert.setHeaderText(null);
            alert.setContentText("Por favor, rellena ambos campos antes de agregar un mensaje.");
            alert.showAndWait();
        }
    }

    private void saveMessageToFile(Message message) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("C:\\Messages\\messages.txt", true))) {
            writer.write(message.toString());
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onNewFile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("MessageForm.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Gestión de Mensajes");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ObservableList<MessageProperties> convertToPropertiesList(ObservableList<Message> messageList) {
        ObservableList<MessageProperties> propertiesList = FXCollections.observableArrayList();

        for (Message message : messageList) {
            MessageProperties properties = new MessageProperties(
                    message.getId(),
                    message.getMessage(),
                    message.getCreatedAt(),
                    message.getName()
            );
            propertiesList.add(properties);
        }

        return propertiesList;
    }

    @FXML
    public void onAbout() {
        Alert aboutDialog = new Alert(Alert.AlertType.INFORMATION);
        aboutDialog.setTitle("Acerca de");
        aboutDialog.setHeaderText("Quick Responses");
        aboutDialog.setContentText("Esta aplicación te permite crear y gestionar respuestas rápidas para enviar directamente a diferentes redes sociales desde la versión web. "
                + "Características principales:\n"
                + "- Crear, modificar y eliminar respuestas rápidas.\n"
                + "- Guardar respuestas en un archivo para uso futuro.\n"
                + "- Enviar respuestas seleccionadas con un solo clic en WhatsApp Web.\n\n"
                + "Desarrollada por Sebastian Saavedra, esta herramienta está diseñada para facilitar el envío de mensajes predefinidos de forma eficiente.");
        aboutDialog.showAndWait();
    }
}
