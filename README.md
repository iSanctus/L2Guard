# aCis Server with L2Guard Integration

This branch contains the complete **aCis gameserver** with **L2Guard Anti-Bot Protection** fully integrated.

## What's Here

This is a ready-to-use aCis server that includes:
- ✅ Complete aCis gameserver source code
- ✅ L2Guard server library integrated (`java/com/l2guard/server/`)
- ✅ All necessary code modifications for L2Guard
- ✅ Complete documentation for setup

## Integration Details

### Modified Files:
1. **GameServer.java** - L2Guard initialization on server startup
2. **Shutdown.java** - L2Guard cleanup on server shutdown
3. **EnterWorld.java** - Player validation when entering world
4. **Logout.java** - Session cleanup on player logout

### Added Files:
- `java/com/l2guard/server/` - L2Guard server library
- `L2GUARD_INTEGRATION.md` - Complete setup guide

## Quick Start

### 1. Download Dependencies

Place in `libs/` folder:
```bash
cd libs/
wget https://repo1.maven.org/maven2/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar
wget https://repo1.maven.org/maven2/org/slf4j/slf4j-api/2.0.9/slf4j-api-2.0.9.jar
wget https://repo1.maven.org/maven2/ch/qos/logback/logback-classic/1.4.11/logback-classic-1.4.11.jar
wget https://repo1.maven.org/maven2/ch/qos/logback/logback-core/1.4.11/logback-core-1.4.11.jar
```

### 2. Compile

```bash
ant
```

### 3. Configure (Optional)

On first start, L2Guard creates: `game/config/l2guard-server-config.json`

**Testing mode** (default - allows unprotected clients):
```json
{
  "requireGuard": false,
  "allowUnprotectedClients": true
}
```

**Production mode** (enforces L2Guard):
```json
{
  "requireGuard": true,
  "allowUnprotectedClients": false
}
```

### 4. Start Server

```bash
./startGameServer.sh
```

Look for:
```
====================================
        L2Guard Anti-Bot Protection
====================================
[L2Guard] L2Guard validator started successfully
```

## What L2Guard Does

Detects and blocks external bot programs:
- **Adrenaline** - Popular L2 bot
- **L2Walker** - Classic bot program
- **L2Tower** - Advanced bot with scripting
- **L2.Net** - Bot framework
- **L2PHX** - Packet editor
- **Cheat Engine** - Memory editor
- And more...

## Client Distribution

Players need the **L2Guard Client** from the main L2Guard repository to connect when protection is enforced.

See the main repository README for client build instructions.

## Documentation

For complete setup instructions, see:
- **L2GUARD_INTEGRATION.md** - Detailed integration guide
- Main L2Guard repo - Client information

## License

- **aCis**: GPL-3.0 (original aCis license)
- **L2Guard**: MIT License

## Support

For integration issues, see `L2GUARD_INTEGRATION.md` in this directory.

For L2Guard issues, see the main repository documentation.

---

**This branch provides a complete, ready-to-compile aCis server with built-in bot protection.** 🛡️
