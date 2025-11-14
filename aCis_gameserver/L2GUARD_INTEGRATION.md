# L2Guard Integration for aCis

This branch integrates **L2Guard Anti-Bot Protection** directly into your aCis server.

## What Was Changed

### 1. L2Guard Server Library Added
**Location:** `java/com/l2guard/server/`

Added L2Guard server validation code:
- `L2GuardValidator.java` - Main validator
- `L2GuardConfig.java` - Configuration manager
- `PlayerGuardSession.java` - Player session tracking
- `HeartbeatData.java` - Client heartbeat data
- `GuardStatistics.java` - Statistics tracker
- `L2GuardServer.java` - Standalone server (optional)

### 2. Server Startup Modified
**File:** `java/net/sf/l2j/gameserver/GameServer.java` (line ~254)

Added L2Guard initialization after scripts load:
```java
StringUtil.printSection("L2Guard Anti-Bot Protection");
try
{
    com.l2guard.server.L2GuardValidator.getInstance().start();
    LOGGER.info("L2Guard protection enabled successfully.");
}
catch (Exception e)
{
    LOGGER.warn("Failed to start L2Guard: {}", e.getMessage());
}
```

### 3. Server Shutdown Modified
**File:** `java/net/sf/l2j/gameserver/Shutdown.java` (line ~122)

Added L2Guard cleanup on shutdown:
```java
// Stop L2Guard
try
{
    com.l2guard.server.L2GuardValidator.getInstance().stop();
    LOGGER.info("L2Guard has been stopped.");
}
catch (Exception e)
{
    // Silent catch.
}
```

### 4. Player Login Modified
**File:** `java/net/sf/l2j/gameserver/network/clientpackets/EnterWorld.java` (line ~75)

Added L2Guard validation when players enter world:
```java
// L2Guard Anti-Bot Protection validation
final String accountName = getClient().getAccountName();
final String ipAddress = getClient().getConnection().getInetAddress().getHostAddress();

if (!com.l2guard.server.L2GuardValidator.getInstance().registerPlayer(accountName, ipAddress))
{
    player.sendMessage("L2Guard Anti-Bot Protection Required!");
    player.sendMessage("Please download from: https://yourserver.com/l2guard");
    player.logout();
    return;
}
```

### 5. Player Logout Modified
**File:** `java/net/sf/l2j/gameserver/network/clientpackets/Logout.java` (line ~52)

Added L2Guard session cleanup:
```java
// L2Guard cleanup
try
{
    com.l2guard.server.L2GuardValidator.getInstance().removePlayer(player.getAccountName());
}
catch (Exception e)
{
    // Silent catch
}
```

## Dependencies Required

L2Guard requires additional libraries. Download and place in `libs/` folder:

### 1. Gson (JSON parsing)
**Download:** https://repo1.maven.org/maven2/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar
**File:** `libs/gson-2.10.1.jar`

### 2. SLF4J API (Logging)
**Download:** https://repo1.maven.org/maven2/org/slf4j/slf4j-api/2.0.9/slf4j-api-2.0.9.jar
**File:** `libs/slf4j-api-2.0.9.jar`

### 3. Logback Classic (Logging implementation)
**Download:** https://repo1.maven.org/maven2/ch/qos/logback/logback-classic/1.4.11/logback-classic-1.4.11.jar
**File:** `libs/logback-classic-1.4.11.jar`

### 4. Logback Core (Logging core)
**Download:** https://repo1.maven.org/maven2/ch/qos/logback/logback-core/1.4.11/logback-core-1.4.11.jar
**File:** `libs/logback-core-1.4.11.jar`

### Quick Download Script:

```bash
cd libs/

# Download Gson
wget https://repo1.maven.org/maven2/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar

# Download SLF4J
wget https://repo1.maven.org/maven2/org/slf4j/slf4j-api/2.0.9/slf4j-api-2.0.9.jar

# Download Logback
wget https://repo1.maven.org/maven2/ch/qos/logback/logback-classic/1.4.11/logback-classic-1.4.11.jar
wget https://repo1.maven.org/maven2/ch/qos/logback/logback-core/1.4.11/logback-core-1.4.11.jar
```

## Building the Server

```bash
# Compile
ant

# Or if using build script
./build.sh
```

## Configuration

On first server start, L2Guard creates: `game/config/l2guard-server-config.json`

### Development/Testing Config (Recommended First):

```json
{
  "enabled": true,
  "requireGuard": false,
  "heartbeatTimeout": 120,
  "allowUnprotectedClients": true,
  "actionOnMissingHeartbeat": "log",
  "actionOnBotDetected": "log",
  "banDuration": 3600,
  "logAllDetections": true,
  "detailedLogs": true
}
```

This allows testing without blocking real players.

### Production Config (After Testing):

```json
{
  "enabled": true,
  "requireGuard": true,
  "heartbeatTimeout": 60,
  "allowUnprotectedClients": false,
  "actionOnMissingHeartbeat": "disconnect",
  "actionOnBotDetected": "ban",
  "banDuration": 86400,
  "logAllDetections": true,
  "detailedLogs": true
}
```

This enforces L2Guard requirement for all players.

## Testing

### 1. Start Server

```bash
./startGameServer.sh
```

### 2. Check Logs

Look for:
```
====================================
        L2Guard Anti-Bot Protection
====================================
[L2Guard] Starting L2Guard Server Validator v1.0.0
[L2Guard] Configuration:
[L2Guard]   - Guard Required: false
[L2Guard]   - Heartbeat Timeout: 120s
[L2Guard]   - Allow Unprotected: true
[L2Guard] L2Guard validator started successfully
```

### 3. Test Connection

- With `requireGuard: false` - Players can connect without guard
- With `requireGuard: true` - Players without guard are rejected

## Client Distribution

Players need the L2Guard client from the main L2Guard repository:
- `L2GuardLauncher.exe`
- `L2Guard.Client.dll`
- Supporting DLLs

See main L2Guard repository for client build instructions.

## Troubleshooting

### "ClassNotFoundException: com.l2guard.server.L2GuardValidator"

**Problem:** L2Guard source not compiled
**Solution:** Run `ant` to recompile

### "NoClassDefFoundError: com/google/gson/Gson"

**Problem:** Missing Gson dependency
**Solution:** Download gson-2.10.1.jar to libs/

### "NoClassDefFoundError: org/slf4j/Logger"

**Problem:** Missing SLF4J dependency
**Solution:** Download slf4j-api-2.0.9.jar to libs/

### Players Can Still Connect Without Guard

**Problem:** Config allows unprotected clients
**Solution:** Set `requireGuard: true` and `allowUnprotectedClients: false`

### No L2Guard Section in Logs

**Problem:** L2Guard initialization failed
**Solution:** Check console for errors, verify dependencies

## Reverting the Integration

If you need to remove L2Guard:

```bash
# Switch back to main branch
git checkout main

# Or manually:
# 1. Delete: java/com/l2guard/
# 2. Revert changes to GameServer.java, Shutdown.java, EnterWorld.java, Logout.java
# 3. Remove libs/gson*.jar and libs/*slf4j*.jar
# 4. Rebuild with ant
```

## Support

For L2Guard issues, see:
- Main repository: https://github.com/iSanctus/L2Guard
- Documentation: QUICKSTART.md, INTEGRATION_GUIDE.md, SECURITY.md

For aCis integration issues:
- Check this README first
- Verify all dependencies are installed
- Check server logs for errors

## Next Steps

1. ✅ Download and install dependencies
2. ✅ Compile server with `ant`
3. ✅ Start server and verify L2Guard logs
4. ✅ Test with `requireGuard: false` first
5. ✅ Build and distribute client to players
6. ✅ Enable `requireGuard: true` when ready
7. ✅ Monitor logs for bot detections

---

**Integration completed successfully!** 🛡️
