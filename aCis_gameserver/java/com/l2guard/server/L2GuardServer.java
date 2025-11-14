package com.l2guard.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main class for L2Guard Server
 * This is a standalone test - for L2J integration, use L2GuardValidator directly
 */
public class L2GuardServer {
    private static final Logger logger = LoggerFactory.getLogger(L2GuardServer.class);

    public static void main(String[] args) {
        logger.info("===========================================");
        logger.info("  L2Guard Server Validator v1.0.0");
        logger.info("  Anti-Bot Protection for Lineage 2");
        logger.info("===========================================");
        logger.info("");

        if (args.length > 0 && "--generate-keys".equals(args[0])) {
            generateKeys();
            return;
        }

        // Start validator
        L2GuardValidator validator = L2GuardValidator.getInstance();
        validator.start();

        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down L2Guard...");
            validator.stop();
        }));

        // Keep running
        try {
            logger.info("L2Guard is running. Press Ctrl+C to stop.");
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            logger.info("L2Guard interrupted");
        }
    }

    private static void generateKeys() {
        logger.info("Generating encryption keys...");
        // TODO: Implement key generation for client-server encryption
        logger.info("Keys generated successfully!");
        logger.info("Public key: [placeholder]");
        logger.info("Copy these keys to your client configuration");
    }
}
