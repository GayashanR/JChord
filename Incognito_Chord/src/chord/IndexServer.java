package chord;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author lahiru_b
 */
public class IndexServer {
    public static void main(String[] args) {
        DatagramSocket sock = null;
       // String filesnames ="ADD:Lord of the rings:train dragon";
        String filesnames ="REM:Lord of the rings";
        String s;
        List<Neighbour> nodes = new ArrayList<Neighbour>();
        HashMap<String,Integer> map=new HashMap<String,Integer>();//Creating HashMap
        map.put("Lord of the rings", 2);
        int peerCount =1;
        String result ="";
        try {
          //  sock = new DatagramSocket(555);
            System.out.println("Index Server is created at 555. Waiting for incoming data...");
            
             while (true) {   
              
              List<String> items = Arrays.asList(filesnames.split("\\s*:\\s*"));
                 System.out.println(Arrays.asList(map));
              
              
              //adding to the hasmap
              if(items.get(0).equals("ADD")){
               
                  //loop the list and add to hashmap
               for(int i=0; i<items.size()-1;i++){
                if(map.containsKey(items.get(i+1))){
                map.put(items.get(i+1), peerCount+1);
               
                }else{
                 map.put(items.get(i+1), peerCount);
                }   
               
             
              }
              }
              else if(items.get(0).equals("SER")){ //searching
                  
                  
                for(int i=0; i<items.size()-1;i++){
                if(map.containsKey(items.get(i+1))){
                
               result="filename:"+items.get(i+1)+":"+"peercount:"+ map.get(items.get(i+1));  
               
                }  
               
             
              }
               
              }
              else if(items.get(0).equals("REM")){ //removing the file name
                 
                   for(int i=0; i<items.size()-1;i++){
                if(map.containsKey(items.get(i+1))){
                
              //result="filename:"+items.get(i+1)+":"+"peercount:"+ map.get(items.get(i+1));
               int peercount= map.get(items.get(i+1));
               
               if(peercount>1){// if the peer count is more than 1 then remove 1 from the peer count.
              
                map.replace(items.get(i+1), peercount, peercount-1);
               result="removed";
               
               }else if(peercount==1){ //if the peer count is 1 then remove from the map.
               map.remove(items.get(i+1));
               result="removed";   
               }
               
                }  
               
             
              }
                        
               }
            
             System.out.println(Arrays.asList(map));
             // System.out.println(Arrays.asList(result));
        }
            
            
            
            
        } catch (Exception ex) {
            Logger.getLogger(IndexServer.class.getName()).log(Level.SEVERE, null, ex);
        }
       
    }
    

        
    
}
