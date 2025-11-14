package com.l2guard.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Configuration for L2Guard server validator
 */
public class L2GuardConfig {
    private static final Logger logger = LoggerFactory.getLogger(L2GuardConfig.class);
    private static final String CONFIG_FILE = "l2guard-server-config.json";

    private boolean enabled = true;
    private boolean requireGuard = true;
    private int heartbeatTimeout = 60;
    private boolean allowUnprotectedClients = false;
    private String actionOnMissingHeartbeat = "disconnect";
    private String actionOnBotDetected = "ban";
    private int banDuration = 86400;
    private boolean logAllDetections = true;
    private boolean detailedLogs = true;

    /**
     * Load configuration from file
     */
    public static L2GuardConfig load() {
        File configFile = new File(CONFIG_FILE);

        if (!configFile.exists()) {
            logger.info("Configuration file not found, creating default: {}", CONFIG_FILE);
            L2GuardConfig defaultConfig = new L2GuardConfig();
            defaultConfig.save();
            return defaultConfig;
        }

        try (FileReader reader = new FileReader(configFile)) {
            Gson gson = new Gson();
            L2GuardConfig config = gson.fromJson(reader, L2GuardConfig.class);
            logger.info("Configuration loaded from {}", CONFIG_FILE);
            return config;
        } catch (IOException e) {
            logger.error("Failed to load configuration: {}", e.getMessage());
            return new L2GuardConfig();
        }
    }

    /**
     * Save configuration to file
     */
    public void save() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(this, writer);
            logger.info("Configuration saved to {}", CONFIG_FILE);
        } catch (IOException e) {
            logger.error("Failed to save configuration: {}", e.getMessage());
        }
    }

    // Getters and setters

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isRequireGuard() {
        return requireGuard;
    }

    public void setRequireGuard(boolean requireGuard) {
        this.requireGuard = requireGuard;
    }

    public int getHeartbeatTimeout() {
        return heartbeatTimeout;
    }

    public void setHeartbeatTimeout(int heartbeatTimeout) {
        this.heartbeatTimeout = heartbeatTimeout;
    }

    public boolean isAllowUnprotectedClients() {
        return allowUnprotectedClients;
    }

    public void setAllowUnprotectedClients(boolean allowUnprotectedClients) {
        this.allowUnprotectedClients = allowUnprotectedClients;
    }

    public String getActionOnMissingHeartbeat() {
        return actionOnMissingHeartbeat;
    }

    public void setActionOnMissingHeartbeat(String actionOnMissingHeartbeat) {
        this.actionOnMissingHeartbeat = actionOnMissingHeartbeat;
    }

    public String getActionOnBotDetected() {
        return actionOnBotDetected;
    }

    public void setActionOnBotDetected(String actionOnBotDetected) {
        this.actionOnBotDetected = actionOnBotDetected;
    }

    public int getBanDuration() {
        return banDuration;
    }

    public void setBanDuration(int banDuration) {
        this.banDuration = banDuration;
    }

    public boolean isLogAllDetections() {
        return logAllDetections;
    }

    public void setLogAllDetections(boolean logAllDetections) {
        this.logAllDetections = logAllDetections;
    }

    public boolean isDetailedLogs() {
        return detailedLogs;
    }

    public void setDetailedLogs(boolean detailedLogs) {
        this.detailedLogs = detailedLogs;
    }
}
