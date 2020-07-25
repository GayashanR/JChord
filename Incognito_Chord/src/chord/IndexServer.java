
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.RowFilter.Entry;

/**
 *
 * @author lahiru_b
 */
public class IndexServer {
    public static void main(String[] args) {
        DatagramSocket sock = null;
       // String filesnames ="ADD:Lord of the rings:train dragon";
       // String filesnames ="REM:Lord of the rings";
        String filesnames ="SER:Lord";
        String s;
    //    List<Neighbour> nodes = new ArrayList<Neighbour>();
        HashMap<String,Integer> map=new HashMap<String,Integer>();//Creating HashMap
//        map.put("Lord of the rings", 2);
//        map.put("Lord of the Jungle", 3);
//        map.put("Lord ", 1);
//        map.put("Nelson ", 2);
        
        int peerCount =1;
        String result ="";
        List<String> resultset= new ArrayList();
         
        try {
            sock = new DatagramSocket(4444);
            System.out.println("Index Server is created at 4444. Waiting for incoming data...");

            while (true) {

                // Create readers and writers from socket
                byte[] receive = new byte[65535];
                DatagramPacket DpReceive = new DatagramPacket(receive, receive.length);
                try {
                    sock.receive(DpReceive);
                } catch (IOException ex) {
                    Logger.getLogger(IndexServer.class.getName()).log(Level.SEVERE, null, ex);
                }
                // Read input from client
                String query = data(receive).toString().trim();

                if (query != null) {
                    filesnames = query;
                    List<String> items = Arrays.asList(filesnames.split("\\s*:\\s*"));
                    System.out.println(Arrays.asList(map));

                    //adding to the hasmap
                    if (items.get(0).equals("ADD")) {

                        //loop the list and add to hashmap
                        for (int i = 0; i < items.size() - 1; i++) {
                            if (map.containsKey(items.get(i + 1))) {
                                map.put(items.get(i + 1), peerCount + 1);

                            } else {
                                map.put(items.get(i + 1), peerCount);
                            }

                        }
                    } else if (items.get(0).equals("SER")) { //searching

                        for (int i = 0; i < items.size() - 1; i++) {

                            for (Map.Entry<String, Integer> e : map.entrySet()) {
                                if (e.getKey().startsWith(items.get(i + 1))) {
                                    resultset.add(e.getKey() + ":" + e.getValue());
                                }

                            }

                            result = "SEARCH_RES:" + resultset.size() + ":";
                            result += String.join(":", resultset);

                        }

                    } else if (items.get(0).equals("REM")) { //removing the file name

                        for (int i = 0; i < items.size() - 1; i++) {
                            if (map.containsKey(items.get(i + 1))) {

                                //result="filename:"+items.get(i+1)+":"+"peercount:"+ map.get(items.get(i+1));
                                int peercount = map.get(items.get(i + 1));

                                if (peercount > 1) {// if the peer count is more than 1 then remove 1 from the peer count.

                                    map.replace(items.get(i + 1), peercount, peercount - 1);
                                    result = "removed";

                                } else if (peercount == 1) { //if the peer count is 1 then remove from the map.
                                    map.remove(items.get(i + 1));
                                    result = "removed";
                                }

                            }

                        }

                    }
                }

                System.out.println(Arrays.asList(map));
                System.out.println(Arrays.asList(result));
                
                byte[] toSend  = result.getBytes();
                DatagramPacket packet =new DatagramPacket(toSend, toSend.length, DpReceive.getAddress(), DpReceive.getPort());
                sock.send(packet);
            }

        } catch (Exception ex) {
            Logger.getLogger(IndexServer.class.getName()).log(Level.SEVERE, null, ex);
        }
       
    }
    
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
        
    
}
