package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
public class SocketThread extends Thread {

    private Client client;
    private Socket socket;
    private DataOutputStream out;

    SocketThread(Socket socket, Client client) throws IOException {
        this.client = client;
        this.socket = socket;
        out = new DataOutputStream(socket.getOutputStream());
        start();
    }

    @Override
    public void run() {
        try (DataInputStream in = new DataInputStream(socket.getInputStream())){
            while (!isInterrupted()) {
                client.getMessage(in.readUTF());
            }
        }catch (IOException e) {
            client.outputToLabel(SMC.NO_COMMUNICATION);
        }finally {
            close();
        }
    }

    void sendMessage(String message){
        try {
            out.writeUTF(message);
            out.flush();
        } catch (IOException e) {
            close();
            e.printStackTrace();
        }
    }

    private void close(){
        client.closeSocket();
    }


}
