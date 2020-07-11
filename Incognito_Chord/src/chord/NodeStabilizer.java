/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chord;

import static chord.Sender.data;
import java.awt.List;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author GayashanRathnavibush
 */
public class NodeStabilizer extends Thread {
    private Node chordNode;
    private int delaySeconds = 10;

    public NodeStabilizer(Node chordNode) {
        this.chordNode = chordNode;
    }

    /**
     * Method that periodically runs to determine if node needs a new successor by contacting the listed successor and asking for its predecessor. If the current successor has a predecessor that is different than itself it sets its successor to the predecessor.
     */
    public void run() {
        try {
            // Initially sleep
            Thread.sleep(this.delaySeconds * 1000);

            DatagramSocket socket= new DatagramSocket();
            
            while (true) {
                // Only open a connection to the successor if it is not ourselves
                if (!this.chordNode.getAddress().equals(this.chordNode.getFirstSuccessor().getAddress()) || (this.chordNode.getPort() != this.chordNode.getFirstSuccessor().getPort())) {
                    
                    

                    // Submit a request for the predecessor
                    String message = Chord.REQUEST_PREDECESSOR + " " + this.chordNode.getId() + " asking " + this.chordNode.getFirstSuccessor().getId();
                    message = Message.customFormat("0000", message.length()) + " " + message;
                    byte[] toSend  = message.getBytes(); 
                    InetAddress IPAddress; 
                    try {
                        IPAddress = InetAddress.getByName(this.chordNode.getFirstSuccessor().getAddress());
                        DatagramPacket packet =new DatagramPacket(toSend, toSend.length, IPAddress, this.chordNode.getFirstSuccessor().getPort()); 
                        socket.send(packet);
                    } catch (UnknownHostException ex) {
                        Logger.getLogger(NodeStabilizer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    
                    System.out.println("Sent: " + message);

                    byte[] receive = new byte[65535]; 
                    DatagramPacket DpReceive = new DatagramPacket(receive, receive.length); 
                    try {
                        socket.receive(DpReceive);
                    } catch (IOException ex) {
                        Logger.getLogger(NodeStabilizer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    
                    // Read response from chord
                    String serverResponse = data(receive).toString();

                    System.out.println("Received: " + serverResponse);

                    // Parse server response for address and port
                    String[] predecessorFragments = serverResponse.split(" ");
                    String predecessorAddress = predecessorFragments[0];
                    int predecessorPort = Integer.valueOf(predecessorFragments[1]);

                    // If the address:port that was returned from the server is not ourselves then we need to adopt it as our new successor
                    if (!this.chordNode.getAddress().equals(predecessorAddress) || (this.chordNode.getPort() != predecessorPort)) {
                        this.chordNode.acquire();

                        Finger newSuccessor = new Finger(predecessorAddress, predecessorPort);

                        // Update finger table entries to reflect new successor
                        this.chordNode.getFingers().put(1, this.chordNode.getFingers().get(0));
                        this.chordNode.getFingers().put(0, newSuccessor);

                        // Update successor entries to reflect new successor
                        this.chordNode.setSecondSuccessor(this.chordNode.getFirstSuccessor());
                        this.chordNode.setFirstSuccessor(newSuccessor);

                        this.chordNode.release();

                        
                        
                        

                        // Tell successor that this node is its new predecessor
                        String message1 = Chord.JOIN + " " + this.chordNode.getAddress() + " " + this.chordNode.getPort();
                        message1 = Message.customFormat("0000", message1.length()) + " " + message1;
                        byte[] toSend1  = message1.getBytes(); 
                        InetAddress IPAddress1; 
                        try {
                            IPAddress1 = InetAddress.getByName(newSuccessor.getAddress());
                            DatagramPacket packet =new DatagramPacket(toSend1, toSend1.length, IPAddress1, newSuccessor.getPort()); 
                            socket.send(packet);
                        } catch (UnknownHostException ex) {
                            Logger.getLogger(NodeStabilizer.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        
                        System.out.println("Sent: " + message1);
                    }

                    BigInteger bigQuery = BigInteger.valueOf(2L);
                    BigInteger bigSelfId = BigInteger.valueOf(this.chordNode.getId());

                    this.chordNode.acquire();

                    // Refresh all fingers by asking successor for nodes
                    for (int i = 0; i < 32; i++) {
                        BigInteger bigResult = bigQuery.pow(i);
                        bigResult = bigResult.add(bigSelfId);

                        // Send query to chord
                        String message1 = Chord.FIND_NODE + " " + bigResult.longValue();
                        message1 = Message.customFormat("0000", message1.length()) + " " + message1;
                        byte[] toSend1  = message1.getBytes(); 
                        InetAddress IPAddress1; 
                        try {
                            IPAddress1 = InetAddress.getByName(this.chordNode.getFirstSuccessor().getAddress());
                            DatagramPacket packet =new DatagramPacket(toSend1, toSend1.length, IPAddress1, this.chordNode.getFirstSuccessor().getPort()); 
                            socket.send(packet);
                        } catch (UnknownHostException ex) {
                            Logger.getLogger(NodeStabilizer.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        
                        System.out.println("Sent: " + message1);

                        receive = new byte[65535]; 
                        DpReceive = new DatagramPacket(receive, receive.length); 
                        try {
                            socket.receive(DpReceive);
                        } catch (IOException ex) {
                            Logger.getLogger(NodeStabilizer.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    
                        // Read response from chord
                        serverResponse = data(receive).toString();

                        // Parse out address and port
                        String[] serverResponseFragments = serverResponse.split(" ", 3);
                        String[] addressFragments = serverResponseFragments[2].split(" ");

                        // Add response finger to table
                        this.chordNode.getFingers().put(i, new Finger(addressFragments[0], Integer.valueOf(addressFragments[1])));
                        this.chordNode.setFirstSuccessor(this.chordNode.getFingers().get(0));
                        this.chordNode.setSecondSuccessor(this.chordNode.getFingers().get(1));

                        System.out.println("Received: " + serverResponse);
                    }

                    this.chordNode.release();

                } else if (!this.chordNode.getAddress().equals(this.chordNode.getFirstPredecessor().getAddress()) || (this.chordNode.getPort() != this.chordNode.getFirstPredecessor().getPort())) {
                    
                    BigInteger bigQuery = BigInteger.valueOf(2L);
                    BigInteger bigSelfId = BigInteger.valueOf(this.chordNode.getId());

                    this.chordNode.acquire();

                    // Refresh all fingers by asking successor for nodes
                    for (int i = 0; i < 32; i++) {
                        BigInteger bigResult = bigQuery.pow(i);
                        bigResult = bigResult.add(bigSelfId);

                        // Send query to chord
                        String message = Chord.FIND_NODE + " " + bigResult.longValue();
                        message = Message.customFormat("0000", message.length()) + " " + message;
                        byte[] toSend  = message.getBytes(); 
                        InetAddress IPAddress; 
                        try {
                            IPAddress = InetAddress.getByName(this.chordNode.getFirstPredecessor().getAddress());
                            DatagramPacket packet =new DatagramPacket(toSend, toSend.length, IPAddress, this.chordNode.getFirstPredecessor().getPort()); 
                            socket.send(packet);
                        } catch (UnknownHostException ex) {
                            Logger.getLogger(NodeStabilizer.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        
                        System.out.println("Sent: " + message);

                        byte[] receive = new byte[65535]; 
                        DatagramPacket DpReceive = new DatagramPacket(receive, receive.length); 
                        try {
                            socket.receive(DpReceive);
                        } catch (IOException ex) {
                            Logger.getLogger(NodeStabilizer.class.getName()).log(Level.SEVERE, null, ex);
                        }

                        // Read response from chord
                        String serverResponse = data(receive).toString();


                        // Parse out address and port
                        String[] serverResponseFragments = serverResponse.split(" ", 3);
                        String[] addressFragments = serverResponseFragments[2].split(" ");

                        // Add response finger to table
                        this.chordNode.getFingers().put(i, new Finger(addressFragments[0], Integer.valueOf(addressFragments[1])));
                        this.chordNode.setFirstSuccessor(this.chordNode.getFingers().get(0));
                        this.chordNode.setSecondSuccessor(this.chordNode.getFingers().get(1));

                        System.out.println("Received: " + serverResponse);
                    }

                    this.chordNode.release();

                }
                this.chordNode.acquire();
                ArrayList<String> keysToRemove = new ArrayList<>();
                for(int i = 0; i < chordNode.getKeys().size(); i++)
                {
                    // Send query to chord
                    long keyId = Long.parseLong((String) chordNode.getKeys().keySet().toArray()[i]);
                    String message = Chord.FIND_NODE + " " + Long.parseLong((String) chordNode.getKeys().keySet().toArray()[i]);
                    message = Message.customFormat("0000", message.length()) + " " + message;
                    byte[] toSend  = message.getBytes(); 
                    InetAddress IPAddress; 
                    try {
                        IPAddress = InetAddress.getByName(this.chordNode.getFirstPredecessor().getAddress());
                        DatagramPacket packet =new DatagramPacket(toSend, toSend.length, IPAddress, this.chordNode.getFirstPredecessor().getPort()); 
                        socket.send(packet);
                    } catch (UnknownHostException ex) {
                        Logger.getLogger(NodeStabilizer.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    System.out.println("Sent: " + message);

                    byte[] receive = new byte[65535]; 
                    DatagramPacket DpReceive = new DatagramPacket(receive, receive.length); 
                    try {
                        socket.receive(DpReceive);
                    } catch (IOException ex) {
                        Logger.getLogger(NodeStabilizer.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    // Read response from chord
                    String serverResponse = data(receive).toString();


                    // Parse out address and port
                    String[] serverResponseFragments = serverResponse.split(" ", 3);
                    String[] addressFragments = serverResponseFragments[2].split(" ");
                    
                    if(!this.chordNode.getAddress().equals(addressFragments[0]) || (this.chordNode.getPort() != Integer.valueOf(addressFragments[1])))
                    {
                        //Store in the found node
                        String id = (String) chordNode.getKeys().keySet().toArray()[i];
                        chordNode.getKey(id);
                        message = Chord.STORE + " " + Long.parseLong(id) + " " +  chordNode.getKey(id).size();
                        for(int j = 0; j < chordNode.getKey(id).size(); j++)
                        {
                            message += " " + chordNode.getKey(id).get(j).getAddress() + " " + chordNode.getKey(id).get(j).getPort();
                        }
                        
                        message = Message.customFormat("0000", message.length()) + " " + message;
                        toSend  = message.getBytes(); 

                        try {
                            IPAddress = InetAddress.getByName(addressFragments[0]);
                            DatagramPacket packet =new DatagramPacket(toSend, toSend.length, IPAddress, Integer.valueOf(addressFragments[1])); 
                            socket.send(packet);
                        } catch (UnknownHostException ex) {
                            Logger.getLogger(NodeStabilizer.class.getName()).log(Level.SEVERE, null, ex);
                        }

                        System.out.println("Sent: " + message);

                        receive = new byte[65535]; 
                        DpReceive = new DatagramPacket(receive, receive.length); 
                        try {
                            socket.receive(DpReceive);
                        } catch (IOException ex) {
                            Logger.getLogger(NodeStabilizer.class.getName()).log(Level.SEVERE, null, ex);
                        }

                        // Read response from chord
                        serverResponse = data(receive).toString().trim(); // LEN STOREOK 0//0 for succss 1 for do not 


                        // Parse out address and port
                        serverResponseFragments = serverResponse.split(" ");
                        if("0".equals(serverResponseFragments[2]))
                        {
                            keysToRemove.add((String) chordNode.getKeys().keySet().toArray()[i]);
                        }
                    }
                }
                
                
                for(int j=0; j < keysToRemove.size(); j++)
                {
                    chordNode.removeKey(keysToRemove.get(j));
                }
                this.chordNode.release();
                // Stabilize again after delay
                Thread.sleep(this.delaySeconds * 1000);
            }
        } catch (InterruptedException e) {
            System.err.println("stabilize() thread interrupted");
            e.printStackTrace();
        } catch (UnknownHostException e) {
            System.err.println("stabilize() could not find host of first successor");
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("stabilize() could not connect to first successor");
            e.printStackTrace();
        }
    }
}
