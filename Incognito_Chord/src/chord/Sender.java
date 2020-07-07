/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chord;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 *
 * @author GayashanRathnavibush
 */
public class Sender {
    private final String bootstrapIP;
    private final int bootStrapPort;
    private static Sender instance=null;
    
    private Sender(){
        bootstrapIP=Config.BOOTSTRAP_IP;
        bootStrapPort=Config.BOOTSTRAP_PORT;
    }
    
    public static Sender getInstance(){
        if(instance==null){
            instance = new Sender();
            return instance;
        }
        else{
            return instance;
        }
    }
    
    public String sendUDPMessage(String message, String peerIp, int peerPort){
        String res = "";
        try{
            DatagramSocket clientSocket = new DatagramSocket(); 
            InetAddress IPAddress = InetAddress.getByName(peerIp); 
            
            byte[] toSend  = message.getBytes(); 
		  
            DatagramPacket packet =new DatagramPacket(toSend, toSend.length, IPAddress, peerPort); 
            System.out.println("sending message:"+message+"\nfrom-"+Config.MY_IP+":"+Config.MY_PORT+",to-"+peerIp+":"+peerPort);
            clientSocket.send(packet);
            
            byte[] receive = new byte[65535]; 
            DatagramPacket DpReceive = new DatagramPacket(receive, receive.length); 
            clientSocket.receive(DpReceive);
            
            res = data(receive).toString();
            }
        catch(IOException ioe){
            ioe.printStackTrace();
	}
        return res;
    }
    
    // A utility method to convert the byte array 
    // data into a string representation. 
    public static StringBuilder data(byte[] a) 
    { 
        if (a == null) 
            return null; 
        StringBuilder ret = new StringBuilder(); 
        int i = 0; 
        while (a[i] != 0) 
        { 
            ret.append((char) a[i]); 
            i++; 
        } 
        return ret; 
    } 
    
    public String sendTCPMessage(String sentence){
        //System.out.println("inside send message"+sentence);
        
        Socket clientSocket = null;
        PrintWriter outToServer=null;
        BufferedReader inFromServer=null;
        char[] buf=new char[10000];
        String returnVal = "0";
        try{
            clientSocket = new Socket(bootstrapIP, bootStrapPort);
            
             outToServer = new PrintWriter(clientSocket.getOutputStream(),true);
  
             inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
          
            outToServer.println(sentence);   

           buf=new char[10000];

            inFromServer.read(buf);
            System.out.println(buf); 
            returnVal = String.valueOf(buf).trim();
        }
        catch(UnknownHostException e){
            returnVal="-1";
            System.out.println("1:"+e.getMessage());
        } 
        catch (IOException ex) {  
            returnVal = "-2";
            System.out.println("2:"+ex.getMessage());
        }
        finally{
            try {
                if(outToServer!=null)
                    outToServer.close();
                
                if(inFromServer!=null)
                    inFromServer.close();
                
                if(clientSocket!=null)
                    clientSocket.close();
                
            } catch (IOException ex) {
                System.out.println("3:"+ex.getMessage());
            }
        }
        
        return returnVal;
    }
    
}
