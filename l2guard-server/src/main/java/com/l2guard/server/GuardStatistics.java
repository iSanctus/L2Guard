package com.l2guard.server;

/**
 * Statistics for L2Guard server
 */
public class GuardStatistics {
    private int activeSessions;
    private int totalPlayersProtected;
    private int totalBotsDetected;
    private int totalDebuggersDetected;
    private int totalHooksDetected;
    private long uptime;

    public GuardStatistics() {
        this.activeSessions = 0;
        this.totalPlayersProtected = 0;
        this.totalBotsDetected = 0;
        this.totalDebuggersDetected = 0;
        this.totalHooksDetected = 0;
        this.uptime = 0;
    }

    public int getActiveSessions() {
        return activeSessions;
    }

    public void setActiveSessions(int activeSessions) {
        this.activeSessions = activeSessions;
    }

    public int getTotalPlayersProtected() {
        return totalPlayersProtected;
    }

    public void setTotalPlayersProtected(int totalPlayersProtected) {
        this.totalPlayersProtected = totalPlayersProtected;
    }

    public int getTotalBotsDetected() {
        return totalBotsDetected;
    }

    public void setTotalBotsDetected(int totalBotsDetected) {
        this.totalBotsDetected = totalBotsDetected;
    }

    public int getTotalDebuggersDetected() {
        return totalDebuggersDetected;
    }

    public void setTotalDebuggersDetected(int totalDebuggersDetected) {
        this.totalDebuggersDetected = totalDebuggersDetected;
    }

    public int getTotalHooksDetected() {
        return totalHooksDetected;
    }

    public void setTotalHooksDetected(int totalHooksDetected) {
        this.totalHooksDetected = totalHooksDetected;
    }

    public long getUptime() {
        return uptime;
    }

    public void setUptime(long uptime) {
        this.uptime = uptime;
    }

    @Override
    public String toString() {
        return String.format(
            "L2Guard Statistics:\n" +
            "  Active Sessions: %d\n" +
            "  Total Players Protected: %d\n" +
            "  Total Bots Detected: %d\n" +
            "  Total Debuggers Detected: %d\n" +
            "  Total Hooks Detected: %d\n" +
            "  Uptime: %d seconds",
            activeSessions, totalPlayersProtected, totalBotsDetected,
            totalDebuggersDetected, totalHooksDetected, uptime
        );
    }
}
