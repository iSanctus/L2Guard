package com.l2guard.server;

/**
 * Data received in heartbeat from client
 */
public class HeartbeatData {
    private boolean threatDetected;
    private String threatDescription;
    private String evidence;
    private long timestamp;

    public HeartbeatData() {
        this.threatDetected = false;
        this.threatDescription = "";
        this.evidence = "";
        this.timestamp = System.currentTimeMillis();
    }

    public HeartbeatData(boolean threatDetected, String threatDescription, String evidence) {
        this.threatDetected = threatDetected;
        this.threatDescription = threatDescription;
        this.evidence = evidence;
        this.timestamp = System.currentTimeMillis();
    }

    public boolean isThreatDetected() {
        return threatDetected;
    }

    public void setThreatDetected(boolean threatDetected) {
        this.threatDetected = threatDetected;
    }

    public String getThreatDescription() {
        return threatDescription;
    }

    public void setThreatDescription(String threatDescription) {
        this.threatDescription = threatDescription;
    }

    public String getEvidence() {
        return evidence;
    }

    public void setEvidence(String evidence) {
        this.evidence = evidence;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
