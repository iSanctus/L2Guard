package com.l2guard.server;

/**
 * Represents a player's guard session
 */
public class PlayerGuardSession {
    private final String accountName;
    private final String guardVersion;
    private final String hwid;
    private final long connectionTime;
    private long lastHeartbeatTime;
    private int heartbeatCount;

    public PlayerGuardSession(String accountName, String guardVersion, String hwid) {
        this.accountName = accountName;
        this.guardVersion = guardVersion;
        this.hwid = hwid;
        this.connectionTime = System.currentTimeMillis();
        this.lastHeartbeatTime = System.currentTimeMillis();
        this.heartbeatCount = 0;
    }

    public void updateHeartbeat() {
        this.lastHeartbeatTime = System.currentTimeMillis();
        this.heartbeatCount++;
    }

    public String getAccountName() {
        return accountName;
    }

    public String getGuardVersion() {
        return guardVersion;
    }

    public String getHwid() {
        return hwid;
    }

    public long getConnectionTime() {
        return connectionTime;
    }

    public long getLastHeartbeatTime() {
        return lastHeartbeatTime;
    }

    public int getHeartbeatCount() {
        return heartbeatCount;
    }

    public long getSessionDuration() {
        return System.currentTimeMillis() - connectionTime;
    }
}
