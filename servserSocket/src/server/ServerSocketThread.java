package server;

import dataBase.AuthService;
import gui.Controller;
import javafx.application.Platform;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.sql.SQLException;
import java.util.Vector;

public class ServerSocketThread extends Thread{

    private static final int TIMEOUT = 3000;
    private static final int LIMIT = 3;
    private final int port;
    private final Controller controller;
    private final Vector<ClientHandler> clients  = new Vector<>();
    private final AuthService authService = new AuthService();
    private volatile int counter = 0;

    public ServerSocketThread(int port, Controller controller) {
        this.port = port;
        this.controller = controller;
        start();
    }

    public AuthService getAuthService() {
        return authService;
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)){
            controller.finish.setFill(Color.WHITE);
            authService.connect();
            serverSocket.setSoTimeout(TIMEOUT);
            controller.start.setFill(Color.web("#2fcf23"));
            while (!isInterrupted()){
                Socket socket;
                try {
                    socket = serverSocket.accept();
                }catch (SocketTimeoutException e){
                    continue;
                }
                new ClientHandler(this, socket);
                Platform.runLater(() ->{
                    controller.count.setText(String.valueOf(addConnect()));
                });
            }
            controller.start.setFill(Color.WHITE);
            stopClientHandler();
        }catch (IOException e) {
            controller.start.setFill(Color.BLUE);
            e.printStackTrace();
        }catch (ClassNotFoundException | SQLException e){
            controller.start.setFill(Color.YELLOW);
        }finally {
            authService.disconnect();
            controller.finish.setFill(Color.web("#ff1f36"));
        }
    }

    private void stopClientHandler(){
        broadcastMsg(SMC.DISCONNECTION);
    }

    synchronized void sendPrivateMessages(String message, ClientHandler clientHandler){
        String[] strings = message.split("\\s+", LIMIT);
        for (ClientHandler client : clients) {
            if (client.getNick().equals(strings[1])){
                client.sendMessage(String.format("%s: %s;", clientHandler.getNick(), strings[2]));
                clientHandler.sendMessage(String.format("%s: @%s (%s);",clientHandler.getNick(), strings[1], strings[2]));
                return;
            }
        }
        clientHandler.sendMessage(String.format("%s %s", SMC.NO, strings[1]));
    }

    synchronized boolean isBusyNick(String nick){
        for (ClientHandler clientHandler : clients) {
            if (clientHandler.getNick().equals(nick)) return true;
        }
        return false;
    }

    synchronized void broadcastMsg(String message){
        for (ClientHandler clientHandler : clients) {
            clientHandler.sendMessage(message);
        }
    }

    synchronized void subscribe(ClientHandler clientHandler){
        clients.add(clientHandler);
    }

    synchronized void unsubscribe(ClientHandler clientHandler){
        clients.remove(clientHandler);
        Platform.runLater(() ->{
            controller.count.setText(String.valueOf(delConnect()));
        });
    }

    private synchronized int addConnect(){
        return ++counter;
    }

    private synchronized int delConnect(){
        return  --counter;
    }
}
