/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package src.clientsync;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Scanner;

/**
 *
 * @author Spellkaze
 */
public class ClientSync {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args){
        // TODO code application logic here
        Scanner scn = new Scanner(System.in);
        try{
        Socket s = new Socket("localhost", 8080); 
      
            // obtaining input and out streams 
            DataInputStream dis = new DataInputStream(s.getInputStream()); 
            DataOutputStream dos = new DataOutputStream(s.getOutputStream()); 
      
            // the following loop performs the exchange of 
            // information between client and client handler
            int count = 1;
            String tosend;

            while (true)
            {
                if( count == 1 ){ // to react differently first time it receives, "connection stablished"
                    System.out.println(dis.readUTF());

                    //msgs to test
                    System.out.println( "Enter your ID:");
                    System.out.println( "Run number" + " " + count);


                    tosend = scn.nextLine();
                    dos.writeUTF(tosend);

                    // msg to test
                    System.out.println( "What I sent " + tosend);
                    System.out.println( "What I received:");
                    System.out.println(dis.readUTF());
                }
                else{
                    // msg test
                    System.out.println("\nSecond run, and ahead:");

                    tosend = scn.nextLine();
                    dos.writeUTF(tosend);
                    System.out.println(dis.readUTF());
                }

                count++;
            }
            
        }catch(IOException ex)
        {
            
            System.err.println("Error: " + ex);
            System.err.println("Closing client");
        }
    }

}
