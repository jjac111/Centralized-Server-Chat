/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package src.serversync;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Spellkaze
 */
public class ConnectionRunnable implements Runnable {

    private enum ConnectionStatus {
        TO_SERVER, TO_CLIENT
    };
    ConnectionStatus status = ConnectionStatus.TO_SERVER;
    private String message = "";
    private String id = null;
    private int port = 0;
    private InetAddress ip = null;
    private String userToSend = null;
    boolean isConnected = true;
    
    protected Socket clientSocket = null;
    final DataInputStream input;
    final DataOutputStream output;

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

            System.out.println("\nNew Thread Assigning id:  " + id);
            DataHolder.AddConnection(id, this);
            System.out.println(DataHolder.ListConnections());
            output.writeUTF(DataHolder.ListConnections());

        } catch (IOException ex) {
            Logger.getLogger(ConnectionRunnable.class.getName()).log(Level.SEVERE, null, ex);
        }

        // to know the number of runs
        int count = 1;

        while (isConnected) {
            // msgs to test
            System.out.println( "\nThread " + id + " run: " + count);
            System.out.println("ESTADO: " + this.status);

            toReturn = "EMPTY_MESSAGE";

            try {
                recieved = input.readUTF();
                String[] command = recieved.split(" "); // cambie el puesto de command
                System.out.println("\nCOMANDOS thread " + id + " run: " + count);
                for( String c: command){
                    System.out.println(c);
                }
                if (status == ConnectionStatus.TO_SERVER) {
                    //System.out.println(recieved);

                    // msg test
                    System.out.println("TO_SERVER");

                    switch (command[0]) {
                        case "list":
                            toReturn = DataHolder.ListConnections();
                            break;
                        case "connect":
                            System.out.println("Attempting to connect client with: " + command[1]);
                            if (DataHolder.IsUserConnected(command[1])) {
                                userToSend = command[1];
                                status = ConnectionStatus.TO_CLIENT;
                                DataHolder.AddMessagesToQueue(command[1], recieved, id);

                                // msg to test
                                System.out.println("Mensaje enviado a cliente " + command[1] + " " + recieved );

                                /// aqui iria el wait, pero cuando llama a data holder
                                
                                
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

                    // msg test
                    System.out.println("TO_CLIENT");

                    if (recieved.equals("disconnect")) {
                        toReturn = "Disconnecting from client: " + userToSend + ". Connecting back to Server";
                        status = ConnectionStatus.TO_SERVER;
                        userToSend = null;
                    } else if (DataHolder.AddMessagesToQueue(userToSend, id + ": " + recieved, id)) {
                        //toReturn = "Message sent correctly";
                        toReturn = message;

                        // msg to test
                        System.out.println("Mensaje enviado a cliente " + userToSend + " " + recieved );

                    } else {
                        //toReturn = "Message could not be sent because user was not connected";
                    }
                }
                System.out.println("To return: " + toReturn);
                output.writeUTF(toReturn);

                /* NO ES NECESARIO, ESTO ESTA DE MAS
                if (status == ConnectionStatus.TO_CLIENT) {
                    toReturn = message;
                    output.writeUTF(toReturn);
                    
                } else {
                    output.writeUTF(message);
                    message = "";
                }
                */

            } catch (IOException ex) {
                //Logger.getLogger(ConnectionRunnable.class.getName()).log(Level.SEVERE, null, ex);
                System.err.println("Lost connection with client: " + this.toString() + ", terminating thread and removing from connected list");
                if (id != null) {
                    DataHolder.RemoveConnection(id);
                }
                isConnected = false;
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

}
