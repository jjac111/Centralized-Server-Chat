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
        
        
        
        try{
        Socket s = new Socket("localhost", 8080); 
      
            // obtaining input and out streams 
            DataInputStream dis = new DataInputStream(s.getInputStream()); 
            DataOutputStream dos = new DataOutputStream(s.getOutputStream()); 
      
            
            System.out.println(dis.readUTF());
            
            String tosend = scn.nextLine(); 
            dos.writeUTF(tosend); 
            
            // the following loop performs the exchange of 
            // information between client and client handler
            ExecutorService executor = Executors.newFixedThreadPool(1);
            while (true)  
            { 
                System.out.println(dis.readUTF()); 
                
                
                
                FutureTask<String> readNextLine = new FutureTask<>(() -> {
                        return scn.nextLine();
                    });
                try {
                    
                    executor.execute(readNextLine);
                    tosend = readNextLine.get(10000, TimeUnit.MILLISECONDS);

                } catch (TimeoutException e) {
                    tosend = "list";
                    readNextLine.cancel(true);
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
