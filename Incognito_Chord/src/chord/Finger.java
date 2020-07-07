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
public class Finger {
    private String address;
    private int port;
    private long id;
    
    public Finger(String address, int port) {
        this.address = address;
        this.port = port;

        // Hash address:port
        SHA1Hasher sha1Hasher = new SHA1Hasher(this.address + ":" + this.port);
        this.id = sha1Hasher.getLong();
    }

    public String getAddress() {
        return this.address;
    }

    public int getPort() {
        return this.port;
    }

    public long getId() {
        return this.id;
    }
}
