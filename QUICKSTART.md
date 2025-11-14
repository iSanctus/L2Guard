# L2Guard Quick Start Guide

## Step 1: Build the Server Component

### Requirements
- Java 11 or higher
- Maven 3.6+

### Build Steps

```bash
cd l2guard-server
mvn clean package
```

This creates: `target/l2guard-server-1.0.0.jar`

### Install to Your L2J Server

1. **Copy the JAR file:**
```bash
# For Linux/Mac
cp target/l2guard-server-1.0.0.jar /path/to/your/l2jserver/libs/

# For Windows
copy target\l2guard-server-1.0.0.jar C:\path\to\your\l2jserver\libs\
```

2. **Add to classpath** (if needed):

Edit your server startup script:

**Linux (startGameServer.sh):**
```bash
java -cp ".:libs/*:l2guard-server-1.0.0.jar" com.yourserver.GameServer
```

**Windows (startGameServer.bat):**
```bat
java -cp ".;libs/*;l2guard-server-1.0.0.jar" com.yourserver.GameServer
```

---

## Step 2: Integrate with Your L2J Server Code

The integration depends on your L2J version. Here are examples for popular versions:

### Option A: aCis (Most Popular)

**1. Initialize L2Guard on Server Startup**

Edit: `gameserver/java/net/sf/l2j/gameserver/GameServer.java`

```java
import com.l2guard.server.L2GuardValidator;

public class GameServer {

    public void init() throws Exception {
        // ... existing code ...

        _log.info("Loading scripts...");
        ScriptData.getInstance();

        // ADD THIS: Initialize L2Guard
        _log.info("Initializing L2Guard Anti-Bot Protection...");
        L2GuardValidator.getInstance().start();

        // ... rest of existing code ...
    }

    public void shutdown() {
        // ADD THIS at the start of shutdown
        _log.info("Stopping L2Guard...");
        L2GuardValidator.getInstance().stop();

        // ... existing shutdown code ...
    }
}
```

**2. Check Guard on Player Login**

Edit: `gameserver/java/net/sf/l2j/gameserver/network/clientpackets/EnterWorld.java`

```java
import com.l2guard.server.L2GuardValidator;

@Override
protected void runImpl() {
    Player player = getClient().getPlayer();
    if (player == null) {
        return;
    }

    // ADD THIS: Check if player has L2Guard
    String accountName = player.getAccountName();
    String ipAddress = getClient().getConnection().getInetAddress().getHostAddress();

    if (!L2GuardValidator.getInstance().registerPlayer(accountName, ipAddress)) {
        player.sendMessage("L2Guard protection is required to play on this server.");
        player.sendMessage("Download L2Guard from: https://yourserver.com/l2guard");
        player.logout();
        return;
    }

    // ... existing code continues ...
}
```

**3. Clean up on Player Disconnect**

Edit: `gameserver/java/net/sf/l2j/gameserver/network/clientpackets/Logout.java`

```java
import com.l2guard.server.L2GuardValidator;

@Override
protected void runImpl() {
    Player player = getClient().getPlayer();
    if (player == null) {
        return;
    }

    // ADD THIS: Remove guard session
    L2GuardValidator.getInstance().removePlayer(player.getAccountName());

    // ... existing logout code ...
}
```

---

### Option B: L2JFrozen

**1. Server Startup**

Edit: `head-src/com/l2jfrozen/gameserver/GameServer.java`

```java
import com.l2guard.server.L2GuardValidator;

public class GameServer {

    public void load() throws Exception {
        // ... existing code ...

        LOGGER.info("Loading scripts");
        ScriptCache.getInstance();

        // ADD THIS
        LOGGER.info("Starting L2Guard Anti-Bot Protection");
        L2GuardValidator.getInstance().start();

        // ... rest of code ...
    }
}
```

**2. Player Login Check**

Edit: `head-src/com/l2jfrozen/gameserver/network/clientpackets/EnterWorld.java`

```java
import com.l2guard.server.L2GuardValidator;

public void runImpl() {
    L2PcInstance activeChar = getClient().getActiveChar();

    // ADD THIS
    String account = getClient().getAccountName();
    String ip = getClient().getConnection().getInetAddress().getHostAddress();

    if (!L2GuardValidator.getInstance().registerPlayer(account, ip)) {
        activeChar.sendMessage("L2Guard is required. Download from: yourserver.com/l2guard");
        activeChar.logout();
        return;
    }

    // ... existing code ...
}
```

---

### Option C: L2JServer (Official)

**1. Server Startup**

Edit: `java/com/l2jserver/gameserver/GameServer.java`

```java
import com.l2guard.server.L2GuardValidator;

public class GameServer {

    public void init() throws Exception {
        // ... after loading all managers ...

        _log.info("Loading Scripts");
        ScriptEngineManager.getInstance();

        // ADD THIS
        _log.info("Starting L2Guard");
        L2GuardValidator.getInstance().start();

        // ... rest of code ...
    }
}
```

**2. Player Login**

Edit: `java/com/l2jserver/gameserver/network/clientpackets/EnterWorld.java`

```java
import com.l2guard.server.L2GuardValidator;

@Override
protected void runImpl() {
    L2PcInstance player = getActiveChar();

    // ADD THIS
    if (!L2GuardValidator.getInstance().registerPlayer(
            getClient().getAccountName(),
            getClient().getConnectionAddress().getHostAddress())) {
        player.sendMessage("L2Guard is required to play.");
        player.logout(false);
        return;
    }

    // ... existing code ...
}
```

---

## Step 3: Create Custom Packets (For Full Integration)

For the heartbeat system to work, you need to add custom packets.

### Create Guard Handshake Packet

**Location:** `gameserver/network/clientpackets/RequestGuardHandshake.java`

```java
package com.yourserver.gameserver.network.clientpackets;

import com.l2guard.server.L2GuardValidator;

public class RequestGuardHandshake extends L2GameClientPacket {
    private String _guardVersion;
    private String _hwid;

    @Override
    protected void readImpl() {
        _guardVersion = readS();
        _hwid = readS();
    }

    @Override
    protected void runImpl() {
        var player = getClient().getActiveChar();
        if (player == null) return;

        String account = getClient().getAccountName();
        boolean accepted = L2GuardValidator.getInstance()
            .handleGuardHandshake(account, _guardVersion, _hwid);

        if (!accepted) {
            player.sendMessage("L2Guard validation failed. Please update.");
            player.logout();
        }
    }

    @Override
    public String getType() {
        return "[C] D0:F0 RequestGuardHandshake";
    }
}
```

### Create Heartbeat Packet

**Location:** `gameserver/network/clientpackets/RequestGuardHeartbeat.java`

```java
package com.yourserver.gameserver.network.clientpackets;

import com.l2guard.server.L2GuardValidator;
import com.l2guard.server.HeartbeatData;

public class RequestGuardHeartbeat extends L2GameClientPacket {
    private boolean _threatDetected;
    private String _threatDescription;
    private String _evidence;

    @Override
    protected void readImpl() {
        _threatDetected = readC() == 1;
        _threatDescription = readS();
        _evidence = readS();
    }

    @Override
    protected void runImpl() {
        var player = getClient().getActiveChar();
        if (player == null) return;

        HeartbeatData data = new HeartbeatData(
            _threatDetected,
            _threatDescription,
            _evidence
        );

        L2GuardValidator.getInstance().handleHeartbeat(
            player.getAccount(),
            data
        );
    }

    @Override
    public String getType() {
        return "[C] D0:F1 RequestGuardHeartbeat";
    }
}
```

### Register the Packets

Edit your packet handler (location varies by version):

```java
// In your packet factory/handler
case 0xF0:
    packet = new RequestGuardHandshake();
    break;
case 0xF1:
    packet = new RequestGuardHeartbeat();
    break;
```

---

## Step 4: Configure L2Guard

On first run, L2Guard creates: `l2guard-server-config.json`

**Recommended Production Settings:**

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

**For Testing (allow unprotected clients):**

```json
{
  "enabled": true,
  "requireGuard": false,
  "heartbeatTimeout": 60,
  "allowUnprotectedClients": true,
  "actionOnMissingHeartbeat": "log",
  "actionOnBotDetected": "log",
  "banDuration": 3600,
  "logAllDetections": true,
  "detailedLogs": true
}
```

---

## Step 5: Test the Server Integration

1. **Start your L2J server**
2. **Check the logs** for:
```
[L2Guard] Starting L2Guard Server Validator v1.0.0
[L2Guard] Configuration:
[L2Guard]   - Guard Required: true
[L2Guard]   - Heartbeat Timeout: 60s
[L2Guard] L2Guard validator started successfully
```

3. **Try to connect** with regular client (should be rejected if `requireGuard: true`)

---

## Step 6: Build Client Guard (For Players)

### Requirements
- Windows with .NET 6.0 SDK
- Visual Studio 2022 or `dotnet` CLI

### Build Steps

```bash
# Clone or navigate to L2Guard directory
cd L2Guard

# Build the solution
dotnet build L2Guard.sln -c Release
```

### Output Files
```
L2Guard/L2Guard.Launcher/bin/Release/net6.0-windows/
├── L2GuardLauncher.exe          <- Main launcher
├── L2Guard.Client.dll           <- Guard library
├── Newtonsoft.Json.dll
├── System.Management.dll
└── (other dependencies)
```

---

## Step 7: Distribute to Players

### Create Player Package

1. **Create a folder:** `L2Guard-Client`

2. **Copy files:**
```
L2Guard-Client/
├── L2GuardLauncher.exe
├── L2Guard.Client.dll
├── Newtonsoft.Json.dll
├── System.Management.dll
└── README.txt
```

3. **Create README.txt:**
```
L2Guard - Anti-Bot Protection
==============================

INSTALLATION:
1. Extract all files to your Lineage 2 game directory
   (Same folder as L2.exe)

2. Run L2GuardLauncher.exe to start the game

3. Click "Launch Lineage 2" when ready

REQUIREMENTS:
- .NET 6.0 Runtime (will be installed automatically if needed)
- Windows 10 or later

TROUBLESHOOTING:
- Run as Administrator if it fails to start
- Make sure no bot programs are running
- Download .NET 6.0: https://dotnet.microsoft.com/download/dotnet/6.0

For support: discord.gg/yourserver
```

4. **Upload to your website:**
```
https://yourserver.com/downloads/L2Guard-Client.zip
```

5. **Announce to players:**
```
⚠️ IMPORTANT UPDATE ⚠️

L2Guard Anti-Bot Protection is now required!

📥 Download: https://yourserver.com/downloads/L2Guard-Client.zip

📖 Installation:
1. Extract to your Lineage 2 folder
2. Use L2GuardLauncher.exe to start the game

❓ Questions? Ask in #support
```

---

## Step 8: Testing the Full System

### Test 1: Normal Player (Should Work)

1. Player downloads L2Guard
2. Runs L2GuardLauncher.exe
3. Sees "All checks passed!"
4. Launches game
5. Connects to server successfully

### Test 2: Bot Detection (Should Block)

1. Run a bot program (Adrenaline, L2Walker, etc.)
2. Try to run L2GuardLauncher.exe
3. Should see: "Bot Detected: Adrenaline"
4. Game launch is blocked

### Test 3: No Guard (Should Be Rejected)

1. Player tries to connect with normal L2.exe (without guard)
2. Server should disconnect them
3. Message: "L2Guard protection is required"

---

## Troubleshooting

### Server Issues

**Problem:** "Class not found: com.l2guard.server.L2GuardValidator"

**Solution:**
```bash
# Make sure JAR is in classpath
ls -la libs/l2guard-server-1.0.0.jar

# Add to startup script
java -cp ".:libs/*:l2guard-server-1.0.0.jar" ...
```

---

**Problem:** "Config file not found"

**Solution:**
```bash
# Config auto-generates on first run
# Check server has write permissions
chmod 755 /path/to/server/
```

---

**Problem:** Players can still connect without guard

**Solution:**
```json
// Edit l2guard-server-config.json
{
  "requireGuard": true,        <- Must be true
  "allowUnprotectedClients": false  <- Must be false
}
```

---

### Client Issues

**Problem:** ".NET Runtime not installed"

**Solution:**
- Download .NET 6.0 Runtime: https://dotnet.microsoft.com/download/dotnet/6.0
- Install "Desktop Runtime" (not SDK)

---

**Problem:** "Cannot find L2.exe"

**Solution:**
- Place L2GuardLauncher.exe in the same folder as L2.exe
- Or place in /system/ folder

---

**Problem:** False positive (OBS, Discord flagged as threat)

**Solution:**
- Add to whitelist in `ModuleScanner.cs`:
```csharp
private readonly HashSet<string> _whitelistedDLLs = new(StringComparer.OrdinalIgnoreCase)
{
    "l2guard.client.dll",
    "obs.dll",           // Add OBS
    "discord-rpc.dll",   // Add Discord
};
```
- Rebuild and redistribute

---

## Next Steps

1. **Test thoroughly** in development environment
2. **Gradual rollout**: Start with `allowUnprotectedClients: true` for 1 week
3. **Monitor logs** for false positives
4. **Collect feedback** from players
5. **Enforce protection**: Set `requireGuard: true` after testing
6. **Regular updates**: Update bot signatures monthly

---

## Support

- **Integration Guide:** INTEGRATION_GUIDE.md (detailed)
- **Security Info:** SECURITY.md (architecture)
- **GitHub Issues:** https://github.com/yourserver/L2Guard/issues

Good luck! 🛡️
