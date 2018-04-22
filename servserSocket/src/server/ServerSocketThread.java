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
                Platform.runLater(() -> controller.count.setText(String.valueOf(addConnect())));
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
                clientHandler.sendMessage(String.format("%s: @%s (%s);",
                        clientHandler.getNick(), strings[1], strings[2]));
                return;
            }
        }
        clientHandler.sendMessage(String.format("%s %s", SMC.NO, strings[1]));
        broadcastMsg(String.format("%s %s", SMC.ADD, getListNick()));
    }

    synchronized boolean isBusyNick(String nick){
        for (ClientHandler clientHandler : clients) {
            if (clientHandler.getNick().equals(nick)) return true;
        }
        return false;
    }

    private synchronized void broadcastMsg(String message, ClientHandler clientHandler){
        for (ClientHandler client : clients) {
            if (clientHandler.getNick().equals(client.getNick())) continue;
            client.sendMessage(message);
        }
    }

    synchronized void broadcastMsg(String message){
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    synchronized void subscribe(ClientHandler clientHandler){
        if (clients.add(clientHandler))
            broadcastMsg(String.format("%s %s", SMC.ADD, clientHandler.getNick()), clientHandler);
    }

    synchronized void unsubscribe(ClientHandler clientHandler){
        if (clients.remove(clientHandler)){
            broadcastMsg(String.format("%s %s", SMC.DEL, clientHandler.getNick()));
        }
        Platform.runLater(() -> controller.count.setText(String.valueOf(delConnect())));
    }

    String getListNick(){
        StringBuilder stringBuilder = new StringBuilder();
        for (ClientHandler client : clients) {
            stringBuilder.append(client.getNick()).append(" ");
        }
        return stringBuilder.toString();
    }

    String getListNick(ClientHandler clientHandler){
        StringBuilder stringBuilder = new StringBuilder();
        for (ClientHandler client : clients) {
            if (clientHandler.getNick().equals(client.getNick())) continue;
            stringBuilder.append(client.getNick()).append(" ");
        }
        return stringBuilder.toString();
    }

    private synchronized int addConnect(){
        return ++counter;
    }

    private synchronized int delConnect(){
        return  --counter;
    }
}
