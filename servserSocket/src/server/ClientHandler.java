package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

class ClientHandler extends Thread{
    private final ServerSocketThread serverThread;
    private final Socket socket;
    private DataInputStream in ;
    private DataOutputStream out;
    private String nick;
    private boolean connection = true;

    public String getNick() {
        return nick;
    }

    @Override
    public void run() {
        String msg;
        try {
            do{
                msg = in.readUTF();
                if(msg.startsWith(SMC.SERVICE)){
                    if (msg.equalsIgnoreCase(SMC.DISCONNECTION)){
                        connection = false;
                        sendMessage(msg);
                    }
                    if (msg.startsWith(SMC.AUTH)) confirmAuthorization(msg);
                    if (msg.startsWith(SMC.W)) serverThread.sendPrivateMessages(msg, this);
                }else serverThread.broadcastMsg(String.format("%s: %s;",nick, msg));
            }while (connection);
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            serverThread.unsubscribe(this);
            try {
                in.close();
                out.flush();
                out.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    ClientHandler(ServerSocketThread serverThread, Socket socket) {
        this.serverThread = serverThread;
        this.socket = socket;
        try{
            in = new DataInputStream(socket.getInputStream());
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
                serverThread.subscribe(this);
                connection = true;
                sendMessage(String.format("%s %s %s", SMC.OK, nick, serverThread.getListNick(this)));
                return;
            }
        }
        connection = false;
        sendMessage(SMC.INVALID);
    }
}
