package com.answer.fastanswer2;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MessageApplication extends Application implements NativeKeyListener {

    private MessageController messageController;

    @Override
    public void start(Stage stage){
        try {
            InetAddress ip = InetAddress.getLocalHost();

            System.out.println("Dirección IP de la máquina: " + ip.getHostAddress());
            String ipUser = "192.168.56.1";
            if(ip.getHostAddress().equals(ipUser)){
                FXMLLoader fxmlLoader = new FXMLLoader(MessageApplication.class.getResource("/com/answer/fastanswer2/Message.fxml"));
                Scene scene = new Scene(fxmlLoader.load(), 500, 50);
                stage.setTitle("Quick Responses");
                stage.setScene(scene);
                stage.show();
                messageController = fxmlLoader.getController();
                registerGlobalKeyListener();
            }else showAlert("Error de autenticación", "La dirección IP no coincide. Por favor, comuníquese con el administrador.");

        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void registerGlobalKeyListener() {
        try {
            // Desactivar logs de JNativeHook
            Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
            logger.setLevel(Level.OFF);
            logger.setUseParentHandlers(false);

            // Registrar el listener de teclas globales
            GlobalScreen.registerNativeHook();
            GlobalScreen.addNativeKeyListener((NativeKeyListener) this);
        } catch (NativeHookException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        if (e.getKeyCode() == NativeKeyEvent.VC_F2) {
            Platform.runLater(() -> {
                if (messageController != null) {
                    messageController.messageLoadView();
                }
            });
        }
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent e) {
        // No es necesario implementar este método
    }

    @Override
    public void nativeKeyTyped(NativeKeyEvent e) {
        // No es necesario implementar este método
    }

    @Override
    public void stop() throws Exception {
        // Quitar el hook global cuando la aplicación se cierra
        GlobalScreen.unregisterNativeHook();
        super.stop();
    }


    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        Application.launch();
    }
}