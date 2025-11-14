# L2Guard Integration Guide

This guide explains how to integrate L2Guard with your L2J-based Lineage 2 private server.

## Overview

L2Guard consists of two components:
1. **Client Guard** (C#) - Runs on player's machine, detects bot programs
2. **Server Validator** (Java) - Runs on your L2J server, validates clients

## Prerequisites

### For Server Administrators

- L2J-based Lineage 2 server (aCis, L2JFrozen, L2JServer, etc.)
- Java 11 or higher
- Maven 3.6+ (for building)

### For Players

- Windows operating system
- .NET 6.0 Runtime
- Lineage 2 game client

## Server-Side Integration

### Step 1: Build the Server Component

```bash
cd l2guard-server
mvn clean package
```

This creates `target/l2guard-server-1.0.0.jar`

### Step 2: Install on Your L2J Server

1. Copy the JAR to your server's lib directory:
```bash
cp target/l2guard-server-1.0.0.jar /path/to/your/l2jserver/libs/
```

2. The configuration file will be auto-generated on first run

### Step 3: Integrate with L2J Code

Add L2Guard to your L2J server code. The integration points depend on your L2J version, but typically:

#### In Your Server Startup Class

```java
import com.l2guard.server.L2GuardValidator;

public class GameServer {
    public void init() {
        // ... existing initialization code ...

        // Initialize L2Guard
        L2GuardValidator.getInstance().start();

        // ... rest of initialization ...
    }

    public void shutdown() {
        // Stop L2Guard
        L2GuardValidator.getInstance().stop();

        // ... existing shutdown code ...
    }
}
```

#### In Your Player Login Handler

```java
import com.l2guard.server.L2GuardValidator;

public class EnterWorldPacket {
    @Override
    public void run() {
        String accountName = player.getAccountName();
        String ipAddress = player.getIP();

        // Check if player has L2Guard
        if (!L2GuardValidator.getInstance().registerPlayer(accountName, ipAddress)) {
            player.sendMessage("L2Guard protection is required to play on this server.");
            player.sendMessage("Please download L2Guard from: https://yourserver.com/l2guard");
            player.closeConnection();
            return;
        }

        // ... rest of login code ...
    }
}
```

#### In Your Player Disconnect Handler

```java
import com.l2guard.server.L2GuardValidator;

public class LeaveWorldPacket {
    @Override
    public void run() {
        String accountName = player.getAccountName();

        // Remove guard session
        L2GuardValidator.getInstance().removePlayer(accountName);

        // ... rest of disconnect code ...
    }
}
```

#### Custom Packet for Guard Handshake

Create a custom packet to receive guard handshakes from clients:

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
        L2PcInstance player = getClient().getActiveChar();
        if (player == null) {
            return;
        }

        String accountName = player.getAccountName();
        boolean accepted = L2GuardValidator.getInstance()
            .handleGuardHandshake(accountName, _guardVersion, _hwid);

        if (!accepted) {
            player.sendMessage("L2Guard validation failed. Please update your client.");
            player.closeConnection();
        }
    }
}
```

#### Custom Packet for Heartbeat

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
        L2PcInstance player = getClient().getActiveChar();
        if (player == null) {
            return;
        }

        HeartbeatData data = new HeartbeatData(_threatDetected, _threatDescription, _evidence);
        L2GuardValidator.getInstance().handleHeartbeat(player.getAccountName(), data);
    }
}
```

### Step 4: Configure L2Guard

Edit `l2guard-server-config.json`:

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

**Configuration Options:**

- `enabled`: Enable/disable L2Guard (true/false)
- `requireGuard`: Require all players to use L2Guard (true/false)
- `heartbeatTimeout`: Seconds before missing heartbeat triggers action
- `allowUnprotectedClients`: Allow players without L2Guard (true/false)
- `actionOnMissingHeartbeat`: Action when heartbeat is missing ("disconnect", "log", "none")
- `actionOnBotDetected`: Action when bot is detected ("ban", "kick", "log")
- `banDuration`: Ban duration in seconds (86400 = 24 hours)
- `logAllDetections`: Log all detection events
- `detailedLogs`: Include detailed information in logs

## Client-Side Distribution

### Step 1: Build the Client Guard

On a Windows machine with .NET 6.0 SDK:

```bash
cd L2Guard.Client
dotnet build -c Release

cd ../L2Guard.Launcher
dotnet build -c Release
```

### Step 2: Package for Distribution

Create a distribution package with:

```
L2Guard/
├── L2GuardLauncher.exe           # Main launcher
├── L2Guard.Client.dll             # Guard library
├── L2Guard.Client.runtimeconfig.json
├── L2Guard.Client.deps.json
└── README.txt                     # Player instructions
```

### Step 3: Player Instructions

Create a `README.txt` for players:

```
L2Guard - Anti-Bot Protection
==============================

INSTALLATION:

1. Extract all files to your Lineage 2 game directory
   (The same folder that contains L2.exe)

2. Run L2GuardLauncher.exe instead of L2.exe

3. If any bot programs are detected, close them and restart the launcher

IMPORTANT:
- Always use L2GuardLauncher.exe to start the game
- Do not run any bot programs (Adrenaline, L2Walker, etc.)
- Do not close the L2Guard window while playing

TROUBLESHOOTING:

If L2Guard fails to start:
1. Install .NET 6.0 Runtime: https://dotnet.microsoft.com/download/dotnet/6.0
2. Run as Administrator
3. Disable antivirus temporarily (some AVs flag anti-cheat software)

If you have legitimate tools running (OBS, Discord, etc.) and get false positives:
Contact server admins with details.

For support, visit: https://yourserver.com/support
```

## Testing

### Test Server-Side

1. Start your L2J server with L2Guard integrated
2. Check logs for: `L2Guard validator started successfully`
3. Try connecting without guard - should be rejected if `requireGuard: true`

### Test Client-Side

1. Run L2GuardLauncher.exe
2. Should see scan progress
3. If no bots detected: "Ready to Launch" button appears
4. Launch game and verify connection to server

### Test Bot Detection

1. Run a known bot program (in test environment only!)
2. Run L2GuardLauncher.exe
3. Should detect bot and prevent game launch
4. Check server logs for detection events

## Advanced Configuration

### Whitelisting Processes

If legitimate software is being flagged, you can whitelist it by modifying `ModuleScanner.cs`:

```csharp
private readonly HashSet<string> _whitelistedDLLs = new(StringComparer.OrdinalIgnoreCase)
{
    "l2guard.client.dll",
    "obs.dll",              // OBS Studio
    "discord-rpc.dll",      // Discord overlay
    "your-legit-tool.dll"
};
```

### Adjusting Detection Sensitivity

In `GuardEngine.cs`, adjust scan intervals:

```csharp
// Scan every 10 seconds instead of 5
validator.Start(10000);
```

### Custom Ban Logic

In your L2J server integration, implement custom ban logic:

```java
private void handleThreatDetection(String accountName, HeartbeatData data) {
    // Custom ban implementation
    Player player = World.getInstance().getPlayer(accountName);
    if (player != null) {
        // Log to database
        logBotDetection(accountName, data);

        // Ban account
        BanManager.getInstance().addBan(
            accountName,
            "Bot/Cheat detected: " + data.getThreatDescription(),
            config.getBanDuration()
        );

        // Disconnect
        player.sendMessage("You have been banned for using bot software.");
        player.closeConnection();
    }
}
```

## Performance Optimization

### Server-Side

- L2Guard adds minimal overhead (<1% CPU)
- Heartbeats are small (~1KB every 30 seconds)
- Use connection pooling if scaling to 1000+ players

### Client-Side

- Process scanning: ~2-5% CPU
- Memory: ~30-50 MB
- Network: <5 KB/minute

## Troubleshooting

### Server Issues

**Problem**: "Configuration file not found"
**Solution**: Config auto-generates on first run, check write permissions

**Problem**: "Failed to start L2Guard validator"
**Solution**: Check Java version (requires 11+) and classpath

**Problem**: Players can connect without guard
**Solution**: Ensure `requireGuard: true` and `allowUnprotectedClients: false`

### Client Issues

**Problem**: "Cannot find L2.exe"
**Solution**: Place L2GuardLauncher.exe in game directory

**Problem**: "Failed to initialize guard"
**Solution**: Run as Administrator, install .NET 6.0 Runtime

**Problem**: False positive detections
**Solution**: Add legitimate tools to whitelist, adjust sensitivity

### Network Issues

**Problem**: Heartbeat timeout
**Solution**: Increase `heartbeatTimeout` in config, check firewall

**Problem**: Cannot connect to guard port
**Solution**: Open port 2107 (or configured port) in firewall

## Security Best Practices

1. **Update Regularly**: Keep bot signatures up to date
2. **Monitor Logs**: Review detection logs for patterns
3. **Educate Players**: Make sure players understand why L2Guard is required
4. **Backup Detection Data**: Log all detections for future analysis
5. **Use HTTPS**: If distributing L2Guard via web, use HTTPS
6. **Code Signing**: Sign L2GuardLauncher.exe to prevent tampering warnings

## Known Limitations

- **Windows Only**: Client guard only works on Windows
- **Signature-Based**: New/unknown bots may evade detection initially
- **Kernel Bots**: Advanced kernel-mode bots may bypass usermode detection
- **Performance**: Very old PCs may experience slight performance impact

## Support

For issues or questions:
- GitHub Issues: https://github.com/yourserver/L2Guard/issues
- Discord: https://discord.gg/yourserver
- Email: support@yourserver.com

## License

L2Guard is provided under MIT License for legitimate server protection use.
