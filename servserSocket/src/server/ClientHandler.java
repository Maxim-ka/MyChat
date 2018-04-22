package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

class ClientHandler extends Thread{
    private final ServerSocketThread serverThread;
    private final Socket socket;
    private DataOutputStream out;
    private String nick;
    private boolean connection = true;

    public String getNick() {
        return nick;
    }

    @Override
    public void run() {
        String msg;
        try (DataInputStream in = new DataInputStream(socket.getInputStream())){
            do{
                msg = in.readUTF();
                if(msg.startsWith(SMC.SERVICE)){
                    if (msg.equalsIgnoreCase(SMC.DISCONNECTION)){
                        connection = false;
                        sendMessage(msg);
                    }
                    if (msg.startsWith(SMC.REG)) register(msg);
                    if (msg.startsWith(SMC.AUTH)) confirmAuthorization(msg);
                    if (msg.startsWith(SMC.CHANGE)) changeNick(msg);
                    if (msg.startsWith(SMC.W)) serverThread.sendPrivateMessages(msg, this);
                }else serverThread.broadcastMsg(String.format("%s: %s;",nick, msg));
            }while (connection);
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            serverThread.unsubscribe(this);
            try {
                out.flush();
                out.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void changeNick(String string){
        String[] strings = string.split("\\s+");
        if (strings.length == 3){
            String answer = serverThread.getAuthService().change(strings[1], strings[2]);
            if (answer == null){
                nick = strings[2];
                sendMessage(String.format("%s %s", SMC.YES_CHANGE, nick));
                serverThread.broadcastMsg(String.format("%s %s", SMC.ADD, serverThread.getListNick()));
                return;
            }
            if (answer.equals(strings[2])) {
                sendMessage(String.format("%s %s",SMC.NO_CHANGE, strings[2]));
                return;
            }
        }
        sendMessage(SMC.NO_CHANGE);
    }

    private void register(String string){
        String[] strings = string.split("\\s+");
        if (strings.length == 4){
            String answer = serverThread.getAuthService().registrationOfUsers(strings[1], strings[2], strings[3]);
            if (answer == null){
                nick = strings[3];
                authorize();
                return;
            }
            if (answer.equals(strings[1])) {
                connection = false;
                sendMessage(String.format("%s %s",SMC.REFUSAL, strings[1]));
                return;
            }
            if (answer.equals(strings[2])) {
                connection = false;
                sendMessage(String.format("%s %s",SMC.REFUSAL, strings[2]));
                return;
            }
            if (answer.equals(strings[3])) {
                connection = false;
                sendMessage(String.format("%s %s",SMC.REFUSAL, strings[3]));
                return;
            }
        } else sendMessage(SMC.REFUSAL);
        connection = false;
    }

    ClientHandler(ServerSocketThread serverThread, Socket socket) {
        this.serverThread = serverThread;
        this.socket = socket;
        try{
            out = new DataOutputStream(socket.getOutputStream());
        }catch (IOException e) {
            try {
                socket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        start();
    }

    void sendMessage(String message){
        try {
            out.writeUTF(message);
            out.flush();
        } catch (IOException e) {
            connection = false;
        }
    }

    private void confirmAuthorization(String string){
        String[] strings = string.split("\\s+");
        if (strings.length == 3){
            nick = serverThread.getAuthService().getNick(strings[1], strings[2]);
            if (nick != null){
                if (serverThread.isBusyNick(nick)){
                    connection = false;
                    sendMessage(SMC.REPETITION);
                    return;
                }
                authorize();
                return;
            }
        }
        connection = false;
        sendMessage(SMC.INVALID);
    }

    private void authorize(){
        serverThread.subscribe(this);
        connection = true;
        sendMessage(String.format("%s %s %s", SMC.OK, nick, serverThread.getListNick(this)));
    }
}
