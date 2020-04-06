/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientsync;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Spellkaze
 */
public class ClientSync {

    
    /**
     * @param args the command line arguments
     * @throws java.lang.Exception
     */
    public static void main(String[] args) throws Exception{
        
        Scanner scn = new Scanner(System.in);
        
        boolean inChat = false;
        String response;
        
        
        try{
        Socket s = new Socket("localhost", 8080); 
      
            // obtaining input and out streams 
            DataInputStream dis = new DataInputStream(s.getInputStream()); 
            DataOutputStream dos = new DataOutputStream(s.getOutputStream()); 
      
            
            System.out.println(dis.readUTF());
            
            String id = scn.nextLine(); 
            dos.writeUTF(id); 
            
            // the following loop performs the exchange of 
            // information between client and client handler
            ExecutorService executor = Executors.newFixedThreadPool(1);
            while (true)  
            {
                response = dis.readUTF();
                System.out.println(response); 
                
                String tosend = "";
                
                // Future task para leer en línea de comando
                FutureTask<String> readNextLine = new FutureTask<>(() -> {
                        return scn.nextLine();
                    });
                try {
                    // Entre estas dos líneas puede estar el error.
                    executor.execute(readNextLine);
                    tosend = readNextLine.get(10000, TimeUnit.MILLISECONDS);

                } catch (TimeoutException e) {
                    if(tosend.isEmpty()) tosend = "list";
                    readNextLine.cancel(true);
                    System.out.println(tosend);
                }
                
                
                dos.writeUTF(tosend); 
                  
            }
            
        }catch(IOException ex)
        {
            
            System.err.println("Error: " + ex);
            System.err.println("Closing client");
        }
    }
    
    

   

}
