/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package src.serverclientsync;

import javax.xml.crypto.Data;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Spellkaze
 */
public class ConnectionRunnable implements Runnable {

    public enum ConnectionStatus {
        TO_SERVER, TO_CLIENT, WAITING
    };
    ConnectionStatus status = ConnectionStatus.TO_SERVER;
    public String message = "";
    public String id = null;
    private int port = 0;
    private InetAddress ip = null;
    public String userToSend = null;
    private boolean isConnected = true;
    
    protected Socket clientSocket = null;
    public final DataInputStream input;
    public final DataOutputStream output;
    public int countWhenDisconnected = 0;
    public int count = 1;
    public ConnectionRunnable(Socket clientSocket, InputStream input, OutputStream output) {
        this.clientSocket = clientSocket;
        this.input = new DataInputStream(input);
        this.output = new DataOutputStream(output);
        ip = clientSocket.getInetAddress();
        port = clientSocket.getPort();
        System.out.println("Opening Thread Connection with: " + ip + ":" + port);
    }

    @Override
    public void run() {
        String recieved;
        String toReturn;
        try {
            output.writeUTF("CONNECTION STABLISHED" );
            id = input.readUTF();
            DataHolder.AddConnection(id, this);
            System.out.println(DataHolder.ListConnections());
            output.writeUTF(DataHolder.ListConnections());

        } catch (IOException ex) {
            Logger.getLogger(ConnectionRunnable.class.getName()).log(Level.SEVERE, null, ex);
        }


        while (isConnected) {

            toReturn = "EMPTY_MESSAGE";

            if( count == countWhenDisconnected){
                try {
                countWhenDisconnected = 0;
                toReturn = DataHolder.ReadMessage(id);
                setStatus(ConnectionStatus.TO_SERVER);
                System.out.println("To return: " + toReturn);
                DataHolder.setDisconnected(true);
                output.writeUTF(toReturn);
                }
                catch(IOException ex){
                    System.err.println("Unable to read.");
                }
            }
            else{
                try {
                    recieved = input.readUTF();
                    String[] command = recieved.split(" "); // cambie el puesto de command
                    for( String c: command){
                        System.out.println(c);
                    }
                    if (status == ConnectionStatus.TO_SERVER) {

                        switch (command[0]) {
                            case "list":
                                DataHolder.SendMessage(id, DataHolder.ListConnections(), null);
                                toReturn = DataHolder.ReadMessage(id);
                                break;
                            case "connect":
                                System.out.println("Attempting to connect client with: " + command[1]);
                                if (DataHolder.IsUserConnected(command[1])) {
                                    userToSend = command[1];

                                    DataHolder.SendMessage( id, recieved, command[1] );
                                    toReturn = DataHolder.ReadMessage(id);
                                    count++;

                                    // msg to test
                                    //System.out.println("Mensaje enviado a cliente " + command[1] + " " + recieved );

                                } else {
                                    toReturn = "No user called " + command[1] + " is currently connected";
                                }
                                break;
                            case "disconnect":
                                isConnected = false;
                                toReturn = "Order of Disconnect!";
                                DataHolder.RemoveConnection(id);
                                break;
                        }
                    } else {

                        if (DataHolder.SendMessage(id, id + ": " + recieved, userToSend)) {
                            toReturn = DataHolder.ReadMessage(id);
                        }
                    }

                    output.writeUTF(toReturn);

                } catch (IOException ex) {
                    //Logger.getLogger(ConnectionRunnable.class.getName()).log(Level.SEVERE, null, ex);
                    System.err.println("Lost connection with client: " + this.toString() + ", terminating thread and removing from connected list");
                    if (id != null) {
                        DataHolder.RemoveConnection(id);
                    }
                    isConnected = false;
                }
            }

            count++;
        }
    }

    @Override
    public String toString() {
        return "Client{" + "id=" + id + ", port=" + port + ", ip=" + ip + '}';
    }

    public void AddMessage(String message, String user) {
        this.status = ConnectionStatus.TO_CLIENT;
        this.userToSend = user;
        this.message = message;
    }

    public ConnectionStatus getStatus(){ return status; }

    public void setStatus(ConnectionStatus status) {
        this.status = status;
    }
}
