/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chord;

import static chord.Sender.data;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author GayashanRathnavibush
 */
public class Heart implements Runnable {
    private Node chordNode = null;
    private final static int initialDelaySeconds = 30;
    private final static int delaySeconds = 5;

    public Heart(Node chordNode) {
        this.chordNode = chordNode;
    }

    /**
     * Sends heartbeats out to neighbors and updates pointers as necessary
     */
    public void run() {
        try {
            Thread.sleep(Heart.initialDelaySeconds * 1000);

            while (true) {
                this.testSuccessor();
                this.testPredecessor();

                Thread.sleep(Heart.delaySeconds * 1000);
            }
        } catch (InterruptedException e) {
            System.err.println("checkNeighbors() thread interrupted");
            e.printStackTrace();
        }
    }

    private void testSuccessor() {
        // Only send heartbeats if we are not the destination
        if (!this.chordNode.getAddress().equals(this.chordNode.getFirstSuccessor().getAddress()) || (this.chordNode.getPort() != this.chordNode.getFirstSuccessor().getPort())) {
            try {
                // Open socket to successor
//                Socket socket = new Socket(this.chordNode.getFirstSuccessor().getAddress(), this.chordNode.getFirstSuccessor().getPort());

                DatagramSocket socket = new DatagramSocket();//(Config.MY_PORT + 1);
                // Send query to chord
                String message = Chord.PING_QUERY + ":" + this.chordNode.getId();
                        
                byte[] toSend  = message.getBytes();
                InetAddress IPAddress; 
                try {
                    IPAddress = InetAddress.getByName(this.chordNode.getFirstSuccessor().getAddress());
                    DatagramPacket packet =new DatagramPacket(toSend, toSend.length, IPAddress, this.chordNode.getFirstSuccessor().getPort());
                    try {
                        socket.send(packet);
                    } catch (IOException ex) {
                        Logger.getLogger(Heart.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } catch (UnknownHostException ex) {
                    Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                System.out.println("Sent: " + Chord.PING_QUERY + ":" + this.chordNode.getId());

                byte[] receive = new byte[65535]; 
                DatagramPacket DpReceive = new DatagramPacket(receive, receive.length); 
                try {
                    socket.receive(DpReceive);
                } catch (IOException ex) {
                    Logger.getLogger(Heart.class.getName()).log(Level.SEVERE, null, ex);
                }

                // Read response from chord
                String serverResponse = data(receive).toString();
                
                System.out.println("Received: " + serverResponse);

                // If we do not receive the proper response then something has gone wrong and we need to set our new immediate successor to the backup
                if (!serverResponse.equals(Chord.PING_RESPONSE)) {
                    this.chordNode.acquire();
                    this.chordNode.setFirstSuccessor(this.chordNode.getSecondSuccessor());
                    this.chordNode.getFingers().put(0, this.chordNode.getSecondSuccessor());
                    this.chordNode.release();
                }

                // Close connections
//                socketWriter.close();
//                socketReader.close();
                socket.close();
            } catch (IOException e) {
                this.chordNode.acquire();
                this.chordNode.setFirstSuccessor(this.chordNode.getSecondSuccessor());
                this.chordNode.getFingers().put(0, this.chordNode.getSecondSuccessor());
                this.chordNode.release();
            }
        }
    }

    private void testPredecessor() {
        // Only send heartbeats if we are not the destination
        if (!this.chordNode.getAddress().equals(this.chordNode.getFirstPredecessor().getAddress()) || (this.chordNode.getPort() != this.chordNode.getFirstPredecessor().getPort())) {
            try {
                // Open socket to predecessor
//                Socket socket = new Socket(this.chordNode.getFirstPredecessor().getAddress(), this.chordNode.getFirstPredecessor().getPort());

                DatagramSocket socket = new DatagramSocket();//(Config.MY_PORT + 1);
                // Send query to chord
                String message = Chord.PING_QUERY + ":" + this.chordNode.getId();
                        
                byte[] toSend  = message.getBytes();
                InetAddress IPAddress; 
                try {
                    IPAddress = InetAddress.getByName(this.chordNode.getFirstPredecessor().getAddress());
                    DatagramPacket packet =new DatagramPacket(toSend, toSend.length, IPAddress, this.chordNode.getFirstPredecessor().getPort());
                    try {
                        socket.send(packet);
                    } catch (IOException ex) {
                        Logger.getLogger(Heart.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } catch (UnknownHostException ex) {
                    Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                System.out.println("Sent: " + Chord.PING_QUERY + ":" + this.chordNode.getId());

                byte[] receive = new byte[65535]; 
                DatagramPacket DpReceive = new DatagramPacket(receive, receive.length); 
                try {
                    socket.receive(DpReceive);
                } catch (IOException ex) {
                    Logger.getLogger(Heart.class.getName()).log(Level.SEVERE, null, ex);
                }

                // Read response from chord
                String serverResponse = data(receive).toString();
                
                System.out.println("Received: " + serverResponse);

                // If we do not receive the proper response then something has gone wrong and we need to set our new immediate predecessor to the backup
                if (!serverResponse.equals(Chord.PING_RESPONSE)) {
                    this.chordNode.acquire();
                    this.chordNode.setFirstPredecessor(this.chordNode.getSecondPredecessor());
                    this.chordNode.release();
                }

                // Close connections
                socket.close();
            } catch (IOException e) {
                this.chordNode.acquire();
                this.chordNode.setFirstPredecessor(this.chordNode.getSecondPredecessor());
                this.chordNode.release();
            }
        }
    }
}
