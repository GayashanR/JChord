/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chord;

/**
 *
 * @author GayashanRathnavibush
 */
public class RegisterResponse {
    private final MessageType message;
    private final String[] peerIps;
    private final int[] peerPorts;
    
    public RegisterResponse(MessageType msg,String[] peerIps,int[] peerPorts){
        this.message=msg;
        this.peerIps=peerIps;
        this.peerPorts=peerPorts;
    }
    
    public Boolean isSucess(){
        if(message==MessageType.REG_SUCCESS){
            return true;
        }
        else{
            return false;
        }
    }
    
    public String[] getPeerIps(){
        return peerIps;
    }
    
    public int[] getpeerPorts(){
        return peerPorts;
    }
    
    public Boolean isInitialNode(){
        if(message==MessageType.REG_SUCCESS && getPeerIps()==null&& getpeerPorts()==null){
            return true;
        }
        else return false;
    }
}
