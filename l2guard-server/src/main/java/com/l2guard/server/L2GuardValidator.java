package com.l2guard.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Main validator for L2Guard server-side protection
 * Validates that clients are running the guard and receives integrity reports
 */
public class L2GuardValidator {
    private static final Logger logger = LoggerFactory.getLogger(L2GuardValidator.class);
    private static L2GuardValidator instance;

    private final Map<String, PlayerGuardSession> activeSessions;
    private final L2GuardConfig config;
    private final ScheduledExecutorService scheduler;
    private boolean isRunning;

    private L2GuardValidator() {
        this.activeSessions = new ConcurrentHashMap<>();
        this.config = L2GuardConfig.load();
        this.scheduler = Executors.newScheduledThreadPool(2);
        this.isRunning = false;
    }

    public static synchronized L2GuardValidator getInstance() {
        if (instance == null) {
            instance = new L2GuardValidator();
        }
        return instance;
    }

    /**
     * Start the L2Guard validator
     */
    public void start() {
        if (isRunning) {
            logger.warn("L2Guard validator is already running");
            return;
        }

        logger.info("Starting L2Guard Server Validator v1.0.0");
        logger.info("Configuration:");
        logger.info("  - Guard Required: {}", config.isRequireGuard());
        logger.info("  - Heartbeat Timeout: {}s", config.getHeartbeatTimeout());
        logger.info("  - Allow Unprotected: {}", config.isAllowUnprotectedClients());

        // Start heartbeat monitor
        scheduler.scheduleAtFixedRate(
                this::checkHeartbeats,
                10,
                10,
                TimeUnit.SECONDS
        );

        isRunning = true;
        logger.info("L2Guard validator started successfully");
    }

    /**
     * Stop the validator
     */
    public void stop() {
        if (!isRunning) {
            return;
        }

        logger.info("Stopping L2Guard validator...");
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
        }

        activeSessions.clear();
        isRunning = false;
        logger.info("L2Guard validator stopped");
    }

    /**
     * Register a new player connection
     * Returns true if player is allowed to connect, false otherwise
     */
    public boolean registerPlayer(String accountName, String ipAddress) {
        if (!config.isRequireGuard() || config.isAllowUnprotectedClients()) {
            // Guard not required, allow connection
            return true;
        }

        // In strict mode, deny connection until guard handshake is received
        logger.info("Player {} ({}) connecting - awaiting guard handshake", accountName, ipAddress);
        return false;
    }

    /**
     * Handle guard handshake from client
     */
    public boolean handleGuardHandshake(String accountName, String guardVersion, String hwid) {
        logger.info("Received guard handshake from {} - Version: {}", accountName, guardVersion);

        // Validate guard version
        if (!isValidGuardVersion(guardVersion)) {
            logger.warn("Player {} has invalid guard version: {}", accountName, guardVersion);
            return false;
        }

        // Create session
        PlayerGuardSession session = new PlayerGuardSession(accountName, guardVersion, hwid);
        activeSessions.put(accountName, session);

        logger.info("Player {} authenticated with L2Guard", accountName);
        return true;
    }

    /**
     * Handle heartbeat from client
     */
    public void handleHeartbeat(String accountName, HeartbeatData data) {
        PlayerGuardSession session = activeSessions.get(accountName);
        if (session == null) {
            logger.warn("Received heartbeat from unknown player: {}", accountName);
            return;
        }

        session.updateHeartbeat();

        // Check integrity data
        if (data.isThreatDetected()) {
            logger.warn("THREAT DETECTED for player {}: {}", accountName, data.getThreatDescription());
            handleThreatDetection(accountName, data);
        }
    }

    /**
     * Handle bot/cheat detection report from client
     */
    private void handleThreatDetection(String accountName, HeartbeatData data) {
        logger.error("=== BOT/CHEAT DETECTED ===");
        logger.error("Player: {}", accountName);
        logger.error("Threat: {}", data.getThreatDescription());
        logger.error("Evidence: {}", data.getEvidence());
        logger.error("==========================");

        // Take action based on configuration
        String action = config.getActionOnBotDetected();
        switch (action.toLowerCase()) {
            case "ban":
                logger.info("Banning player {} for bot/cheat detection", accountName);
                // Call your L2J server's ban method here
                // Example: GameServer.getInstance().banPlayer(accountName, config.getBanDuration());
                break;

            case "kick":
                logger.info("Kicking player {} for bot/cheat detection", accountName);
                // Call your L2J server's kick method here
                break;

            case "log":
                logger.info("Logging bot detection for player {} (no action taken)", accountName);
                break;

            default:
                logger.warn("Unknown action: {}", action);
        }
    }

    /**
     * Check for missing heartbeats
     */
    private void checkHeartbeats() {
        long currentTime = System.currentTimeMillis();
        long timeoutMs = config.getHeartbeatTimeout() * 1000L;

        activeSessions.forEach((accountName, session) -> {
            long timeSinceLastHeartbeat = currentTime - session.getLastHeartbeatTime();

            if (timeSinceLastHeartbeat > timeoutMs) {
                logger.warn("Player {} missed heartbeat ({}s ago)",
                    accountName, timeSinceLastHeartbeat / 1000);

                // Take action
                String action = config.getActionOnMissingHeartbeat();
                if ("disconnect".equalsIgnoreCase(action)) {
                    logger.info("Disconnecting player {} for missing heartbeat", accountName);
                    // Call your L2J server's disconnect method here
                    activeSessions.remove(accountName);
                }
            }
        });
    }

    /**
     * Validate guard version
     */
    private boolean isValidGuardVersion(String version) {
        // Accept version 1.x.x
        return version != null && version.startsWith("1.");
    }

    /**
     * Remove player session
     */
    public void removePlayer(String accountName) {
        activeSessions.remove(accountName);
        logger.info("Player {} disconnected - guard session removed", accountName);
    }

    /**
     * Get statistics
     */
    public GuardStatistics getStatistics() {
        GuardStatistics stats = new GuardStatistics();
        stats.setActiveSessions(activeSessions.size());
        stats.setTotalPlayersProtected(activeSessions.size());
        return stats;
    }

    /**
     * Check if player has valid guard session
     */
    public boolean hasValidSession(String accountName) {
        PlayerGuardSession session = activeSessions.get(accountName);
        if (session == null) {
            return false;
        }

        long timeSinceLastHeartbeat = System.currentTimeMillis() - session.getLastHeartbeatTime();
        return timeSinceLastHeartbeat < (config.getHeartbeatTimeout() * 1000L);
    }

    public L2GuardConfig getConfig() {
        return config;
    }

    public boolean isRunning() {
        return isRunning;
    }
}
