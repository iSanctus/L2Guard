# L2Guard - Lineage2 External Bot Protection System

## Overview

L2Guard is a comprehensive anti-cheat system designed to detect and block external bot programs (like Adrenaline, L2Walker, L2Tower) used on Lineage2 private servers. It uses a hybrid client-server architecture to prevent bot programs from hooking into the game client.

## The Problem: External Bot Programs

Popular L2 bots like **Adrenaline**, **L2Walker**, **L2Tower**, and **L2.Net** work by:
- 🎯 Launching before the game client
- 💉 Injecting DLLs into the game process
- 🧠 Reading game memory (player positions, NPC data, inventory)
- ✏️ Writing to game memory (speedhacks, teleport hacks)
- 🪝 Hooking Windows APIs (DirectX, Winsock, User32)
- 📦 Intercepting and modifying network packets


## L2Guard Solution

### 🛡️ Two-Layer Protection

```
┌─────────────────────────────────────┐
│  Client Guard (C#/.NET)             │
│  • Runs with game client            │
│  • Detects bot processes            │
│  • Detects DLL injection            │
│  • Detects memory tampering         │
│  • Sends integrity proofs to server │
└─────────────────────────────────────┘
              ↓ Encrypted heartbeat
┌─────────────────────────────────────┐
│  Server Validator (Java)            │
│  • Validates client is protected    │
│  • Verifies integrity proofs        │
│  • Disconnects unprotected clients  │
│  • Logs bot detection events        │
└─────────────────────────────────────┘
```

## 🔐 Tamper Protection

L2Guard includes **built-in tamper resistance** to prevent users from disabling security features:

- **Hardcoded Security Settings**: All critical settings are compiled into the code, not configurable via JSON
- **Integrity Verification**: Checks guard files haven't been modified before starting
- **No Client-Side Config**: Security settings cannot be changed by users
- **Server Validation**: Server enforces guard requirements and validates versions

**See [SECURITY.md](SECURITY.md) for complete security architecture details.**

## Features

### 🔍 Client-Side Detection (C# Guard)

1. **Process Scanner**
   - Scans for known bot processes (adrenaline.exe, l2walker.exe, l2tower.exe, etc.)
   - Detects suspicious process names and patterns
   - Monitors new processes during gameplay

2. **DLL Injection Detection**
   - Enumerates all loaded modules in game process
   - Detects unknown/suspicious DLLs
   - Blocks new DLL injection attempts
   - Validates digital signatures of loaded modules

3. **Memory Integrity Checks**
   - Monitors critical game memory regions
   - Detects memory modifications
   - Checks for code patches and hooks

4. **Anti-Debugging**
   - Detects attached debuggers (OllyDbg, x64dbg, Cheat Engine)
   - Prevents debugging of the guard itself
   - Detects debugger-related processes

5. **Hook Detection**
   - Scans for hooked Windows APIs (DirectX, Winsock, User32)
   - Detects inline hooks and IAT hooks
   - Validates function integrity

6. **Window Scanner**
   - Detects suspicious window titles
   - Identifies bot control panels
   - Monitors overlay windows

### 🔐 Server-Side Validation (Java)

1. **Client Heartbeat System**
   - Requires regular integrity proofs from client
   - Encrypted communication
   - Timeout disconnection for missing heartbeats

2. **Packet Signature Validation**
   - Validates packets come from protected client
   - Detects packet tampering
   - Cryptographic signatures

3. **Connection Fingerprinting**
   - Ensures client is running L2Guard
   - Validates guard version
   - Prevents unprotected connections

4. **Behavioral Analysis (Fallback)**
   - Monitors suspicious patterns if client protection is bypassed
   - Machine learning-based detection
   - Rate limiting and anomaly detection

## Architecture

### Client Guard (C#/.NET)

```
L2Guard.Client/
├── Core/
│   ├── GuardEngine.cs          # Main engine
│   ├── ProcessScanner.cs       # Bot process detection
│   ├── ModuleScanner.cs        # DLL injection detection
│   ├── MemoryProtector.cs      # Memory integrity checks
│   ├── HookDetector.cs         # API hook detection
│   └── AntiDebug.cs            # Anti-debugging
├── Communication/
│   ├── ServerConnection.cs     # Server heartbeat
│   ├── PacketSigner.cs         # Packet signing
│   └── Crypto.cs               # Encryption
├── Signatures/
│   ├── KnownBots.cs            # Bot signatures database
│   ├── SuspiciousDLLs.cs       # DLL blacklist
│   └── ProcessPatterns.cs      # Suspicious patterns
└── Launcher/
    └── L2GuardLauncher.exe     # Launches guard + game
```

### Server Validator (Java)

```
l2guard-server/
├── core/
│   ├── L2GuardValidator.java   # Main validator
│   ├── HeartbeatHandler.java   # Client heartbeat verification
│   └── PacketValidator.java    # Packet signature validation
├── crypto/
│   ├── SignatureVerifier.java  # Cryptographic verification
│   └── KeyManager.java         # Key management
├── detection/
│   ├── BehavioralAnalyzer.java # Fallback detection
│   └── AnomalyDetector.java    # Pattern detection
└── config/
    └── L2GuardConfig.java      # Configuration
```

## Technology Stack

### Client Guard
- **Language**: C# (.NET Framework 4.8 / .NET 6+)
- **Windows API**: P/Invoke for low-level access
- **Encryption**: AES-256 + RSA
- **Anti-Tampering**: Code obfuscation + integrity checks

### Server Validator
- **Language**: Java 11+
- **Build Tool**: Maven
- **Integration**: L2J Server plugin
- **Logging**: SLF4J + Logback

## Installation

### Server Setup (L2J)

1. Build the server component:
```bash
cd l2guard-server
mvn clean package
```

2. Copy JAR to your L2J server:
```bash
cp target/l2guard-server-1.0.0.jar /path/to/l2server/libs/
```

3. Configure L2Guard:
```bash
cp config/l2guard-server-config.json /path/to/l2server/config/
```

4. Generate encryption keys:
```bash
java -jar l2guard-server-1.0.0.jar --generate-keys
```

### Client Setup

1. Build the client guard:
```bash
cd L2Guard.Client
dotnet build -c Release
```

2. Distribute to players:
```
L2GuardLauncher.exe    # Players use this to launch the game
L2Guard.Client.dll     # Guard library
l2guard-config.json    # Configuration with server details
```

3. Players launch the game through L2GuardLauncher.exe instead of the normal L2.exe

## How It Works

### Protection Flow

1. **Player Launches Game**
   ```
   Player runs L2GuardLauncher.exe
   ↓
   Guard loads and initializes
   ↓
   Guard scans for existing bot processes
   ↓
   If clean: Launch Lineage2.exe
   If bots detected: Block launch + show warning
   ```

2. **During Gameplay**
   ```
   Guard continuously monitors:
   - New processes
   - Loaded DLLs
   - Memory integrity
   - Debuggers
   - API hooks

   Every 30 seconds:
   - Guard sends encrypted heartbeat to server
   - Includes integrity proof
   - Server validates and responds
   ```

3. **Bot Detection**
   ```
   If bot detected:
   Client Side: Log evidence + notify server
   ↓
   Server Side: Disconnect player + log event + ban
   ```

## Detection Methods

### Known Bot Signatures

The system detects popular bots:

| Bot Name | Detection Method |
|----------|------------------|
| Adrenaline | Process name, window title, DLL signatures |
| L2Walker | Process name, memory patterns |
| L2Tower | Process name, DLL injection |
| L2.Net | Process name, hook patterns |
| L2PHX | Memory signatures |
| Cheat Engine | Process name, window title |

### Advanced Detection

- **Pattern-based**: Detects unknown bots by suspicious behavior
  - Multiple DLLs with obfuscated names
  - Hooks on game-related APIs
  - Memory read/write patterns
  - Debugger presence

- **Heuristic Analysis**:
  - Unusual module load order
  - Suspicious thread creation
  - Hidden windows
  - Timing attacks

## Configuration

### Client Config (`l2guard-config.json`)

```json
{
  "server": {
    "host": "your.l2server.com",
    "guardPort": 2107,
    "heartbeatInterval": 30
  },
  "protection": {
    "scanInterval": 5,
    "strictMode": true,
    "allowDebugger": false,
    "checkMemoryIntegrity": true,
    "detectHooks": true
  },
  "action": {
    "onBotDetected": "exit",
    "showWarnings": true
  }
}
```

### Server Config (`l2guard-server-config.json`)

```json
{
  "guard": {
    "enabled": true,
    "requireGuard": true,
    "heartbeatTimeout": 60,
    "allowUnprotectedClients": false
  },
  "action": {
    "onMissingHeartbeat": "disconnect",
    "onBotDetected": "ban",
    "banDuration": 86400
  },
  "logging": {
    "logAllDetections": true,
    "detailedLogs": true
  }
}
```

## Bypassing Countermeasures

L2Guard is designed to resist bypass attempts:

### Anti-Tampering
- **Code Obfuscation**: Guard code is obfuscated
- **Integrity Checks**: Guard validates its own integrity
- **Anti-Debugging**: Prevents debugging of the guard
- **Process Protection**: Makes it hard to terminate the guard

### Self-Protection
- **Kernel-Mode Components** (optional): Driver for enhanced protection
- **Watchdog Process**: Monitors guard process
- **Encrypted Communication**: Prevents packet replay attacks

### Update System
- **Signature Updates**: New bot signatures can be pushed
- **Remote Configuration**: Update detection rules remotely
- **Version Enforcement**: Server can require minimum guard version

## Performance Impact

- **Memory**: ~30-50 MB
- **CPU**: ~2-5% (during scans)
- **Network**: ~1 KB per heartbeat (every 30s)
- **Game Performance**: Minimal impact (<1% FPS)

## False Positives

To minimize false positives:

1. **Whitelist System**: Allow legitimate tools (OBS, Discord overlay, etc.)
2. **Multiple Checks**: Requires multiple indicators before flagging
3. **Grace Period**: Warnings before automatic bans
4. **Admin Review**: Detailed logs for manual review

## Known Limitations

1. **Windows Only**: Client guard only works on Windows
2. **Kernel-Level Bots**: Some advanced bots using kernel drivers may evade detection
3. **Updated Bots**: New bot versions may bypass signatures (requires signature updates)
4. **Linux Users**: Cannot run client guard (could use Wine detection as fallback)

## Roadmap

- [x] Basic process scanning
- [x] DLL injection detection
- [x] Server heartbeat validation
- [ ] Kernel-mode driver for enhanced protection
- [ ] Machine learning-based unknown bot detection
- [ ] Automated signature updates
- [ ] Web dashboard for admins
- [ ] Hardware ban system (HWID)
- [ ] Screenshot verification system
- [ ] Trusted execution environment (TEE) support

## Legal & Ethical Considerations

⚠️ **Important**:
- L2Guard is designed for **legitimate server protection**
- Server owners must **notify players** about anti-cheat software
- Respect player privacy - only collect necessary data
- Comply with local laws regarding anti-cheat software
- Players should be informed about data collected

## Support & Updates

Bot developers constantly update their software. L2Guard requires:
- **Regular signature updates** for new bot versions
- **Community reporting** of new bots
- **Active maintenance** to stay effective

## Contributing

We welcome contributions! Especially:
- New bot signatures
- Detection method improvements
- Bypass resistance techniques
- Performance optimizations

## License

MIT License - See LICENSE file

## Disclaimer

This software is provided for legitimate server protection purposes. Users are responsible for compliance with applicable laws and game policies. The developers are not responsible for misuse.

---

**Remember**: The fight against bots is ongoing. No system is 100% effective, but L2Guard makes it significantly harder for bot users and raises the technical barrier.
