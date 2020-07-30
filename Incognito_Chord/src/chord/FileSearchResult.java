/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chord;

import java.util.List;

/**
 *
 * @author lakindu
 */
public class FileSearchResult {
    private List<Finger> peers;
    private int hopCount;
    private long latency;

    public List<Finger> getPeers() {
        return peers;
    }

    public void setPeers(List<Finger> peers) {
        this.peers = peers;
    }

    public int getHopCount() {
        return hopCount;
    }

    public void setHopCount(int hopCount) {
        this.hopCount = hopCount;
    }

    public long getLatency() {
        return latency;
    }

    public void setLatency(long latency) {
        this.latency = latency;
    }
}
