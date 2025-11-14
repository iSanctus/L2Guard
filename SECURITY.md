# L2Guard Security Architecture

## Tamper Protection

L2Guard implements multiple layers of tamper protection to prevent users from disabling or bypassing security features.

### 🔒 Protection Layers

#### 1. **Hardcoded Security Settings**

All critical security settings are **compiled into the executable** and **cannot be modified** by users:

```csharp
// These are CONSTANTS in the code, not configurable
public const bool PROCESS_SCANNING_ENABLED = true;
public const bool MODULE_SCANNING_ENABLED = true;
public const bool ANTI_DEBUG_ENABLED = true;
public const bool HOOK_DETECTION_ENABLED = true;
public const int SCAN_INTERVAL_MS = 5000;
```

**What this prevents:**
- ✅ Users cannot disable bot detection
- ✅ Users cannot slow down or speed up scan intervals
- ✅ Users cannot change action on bot detection to "ignore"

#### 2. **No Client-Side Configuration for Security**

The old approach (vulnerable):
```json
{
  "protection": {
    "scanInterval": 5,
    "strictMode": true,
    "allowDebugger": false  ← User could change this to true!
  }
}
```

The new approach (secure):
- Security settings are NOT read from any config file
- Only server connection info (IP, port) can be configured
- If config file is missing/corrupted, defaults are used

#### 3. **Integrity Verification**

On startup, L2Guard verifies:

**a) Assembly Integrity**
- Checks if the L2Guard.Client.dll has been modified
- Uses SHA-256 hash verification
- Prevents recompilation with security features removed

**b) File Integrity**
- Verifies launcher executable exists and has reasonable size
- Detects if launcher was replaced with a dummy/fake version
- Checks for file corruption

**c) Code Integrity**
- Verifies critical classes haven't been removed
- Ensures all detection modules are present
- Detects if someone removed ProcessScanner, AntiDebug, etc.

**If ANY integrity check fails → Guard refuses to start**

#### 4. **Server-Side Validation**

Even if a player bypasses client-side checks:

1. **Heartbeat System**: Server expects encrypted heartbeats every 30 seconds
2. **Version Verification**: Server validates guard version
3. **Threat Reporting**: Client must report detected threats to server
4. **Missing Heartbeat = Disconnect**: If heartbeat stops, player is kicked

### 🛡️ Attack Scenarios & Protections

#### Attack #1: Modify JSON config to disable scanning

**How it would work (old system):**
```json
{
  "protection": {
    "scanInterval": 999999,  ← Slow down to never scan
    "processScanningEnabled": false  ← Disable detection
  }
}
```

**Protection:**
- ✅ Security settings are hardcoded in code
- ✅ Config file only contains server IP/port
- ✅ Even if config is deleted, defaults are secure

#### Attack #2: Replace L2Guard DLL with modified version

**How it would work:**
1. Decompile L2Guard.Client.dll
2. Remove bot detection code
3. Recompile and replace DLL

**Protection:**
- ✅ Integrity check calculates hash of DLL on startup
- ✅ If hash doesn't match → refuse to start
- ✅ Server validates guard version
- ✅ Code obfuscation makes decompiling harder

#### Attack #3: Create fake launcher that does nothing

**How it would work:**
1. Create fake L2GuardLauncher.exe that just launches L2.exe
2. Skip all security checks

**Protection:**
- ✅ File size check (fake launcher would be too small)
- ✅ Server requires heartbeat from guard
- ✅ No heartbeat = disconnect from server
- ✅ Can implement code signing (future enhancement)

#### Attack #4: Run debugger to skip integrity checks

**How it would work:**
1. Attach debugger to L2GuardLauncher.exe
2. Set breakpoint on integrity check
3. Modify return value to always return true

**Protection:**
- ✅ Anti-debug detection runs first
- ✅ Multiple anti-debug techniques (hard to bypass all)
- ✅ Debugger detected = refuse to start
- ✅ Timing checks detect single-stepping

#### Attack #5: Terminate guard process after game starts

**How it would work:**
1. Let guard start game
2. Kill L2GuardLauncher.exe process
3. Run bot

**Protection:**
- ✅ Heartbeat system - server expects regular updates
- ✅ No heartbeat = automatic disconnect
- ✅ Can implement watchdog process (future)
- ✅ Can inject guard into game process (advanced)

### 📊 Security Comparison

| Aspect | OLD (Vulnerable) | NEW (Secure) |
|--------|------------------|--------------|
| Scan interval | JSON config | Hardcoded constant |
| Detection toggle | JSON config | Always enabled |
| Integrity check | None | Multi-layer verification |
| Config tampering | Possible | Only server IP editable |
| DLL replacement | Undetected | Hash verification |
| Fake launcher | Works | File size check + heartbeat |
| Debugger bypass | Easy | Multiple anti-debug checks |

### 🔐 Additional Security Recommendations

#### 1. Code Signing (Strongly Recommended)

Sign the executable with a certificate:
```bash
# Using signtool (Windows SDK)
signtool sign /f certificate.pfx /p password L2GuardLauncher.exe
```

**Benefits:**
- Players get "Verified Publisher" instead of "Unknown Publisher"
- Harder to distribute modified versions
- Windows SmartScreen won't block it

#### 2. Code Obfuscation

Use tools like **ConfuserEx** or **Dotfuscator**:
```bash
ConfuserEx.exe -n -o output/ L2Guard.Client.dll
```

**Benefits:**
- Makes decompilation much harder
- Hides detection logic
- Slows down reverse engineering

#### 3. Server-Side Ban Lists

Maintain a database of known modified guard versions:
```java
// In server
if (isKnownTamperedVersion(guardHash)) {
    player.ban("Modified guard detected");
}
```

#### 4. Regular Updates

Update guard version frequently:
- New bot signatures
- New anti-tamper techniques
- Close discovered bypasses

Server enforces minimum version:
```java
if (guardVersion < "1.0.5") {
    player.sendMessage("Please update L2Guard");
    player.disconnect();
}
```

### ⚠️ Known Limitations

Even with all these protections, determined attackers can:

1. **Kernel-Mode Drivers**: Advanced bots using kernel drivers can bypass usermode detection
2. **Virtual Machines**: Bots can run in VM and pass through input to real game
3. **Hardware Emulation**: Sophisticated setups can emulate mouse/keyboard at hardware level
4. **Reverse Engineering**: Given enough time, any protection can be reversed

**However:**
- These advanced techniques require **significant effort**
- 95%+ of bot users won't have the skills
- Regular updates keep forcing attackers to start over
- The goal is to make botting harder than playing legit

### 🎯 Best Practices for Server Admins

1. **Enable All Protection**: Set `requireGuard: true` and `allowUnprotectedClients: false`
2. **Monitor Logs**: Review bot detection events daily
3. **Update Frequently**: Push new guard versions monthly
4. **Educate Players**: Explain why anti-bot is necessary
5. **Fast Response**: Ban detected botters immediately
6. **Community Reporting**: Let players report suspected bots
7. **Signature Updates**: Add new bot programs as they appear

### 🔍 Verification Checklist

Before deploying L2Guard, verify:

- [ ] All security settings are hardcoded (check `SecurityConfig.cs`)
- [ ] No security settings in JSON config files
- [ ] Integrity checks are enabled in `GuardEngine.cs`
- [ ] Server requires guard (`requireGuard: true`)
- [ ] Heartbeat timeout is configured (<60 seconds)
- [ ] Action on bot detected is "ban" or "kick"
- [ ] Logs are being written and monitored
- [ ] Code is obfuscated (if using obfuscation)
- [ ] Executables are signed (if using code signing)

### 📖 For Developers

If you're modifying L2Guard:

**DO:**
- ✅ Add new bot signatures to `KnownBots.cs`
- ✅ Improve detection algorithms
- ✅ Add new anti-tamper checks
- ✅ Update documentation

**DON'T:**
- ❌ Move security settings to config files
- ❌ Add "disable" flags for critical features
- ❌ Remove integrity checks
- ❌ Make scan intervals configurable
- ❌ Add debug modes that skip security

### 🆘 What to Do If Protection is Bypassed

If you discover a bypass:

1. **Don't panic** - No protection is perfect
2. **Document the bypass** - Understand how it works
3. **Patch quickly** - Add detection for the bypass technique
4. **Push update** - Force all players to update
5. **Ban retrospectively** - Check logs for past exploiters
6. **Learn and improve** - Add similar checks to prevent future bypasses

---

**Remember**: Security is an arms race. L2Guard makes botting significantly harder, but determined attackers with enough time and skill can find bypasses. The goal is to raise the barrier high enough that 95%+ of would-be botters give up.
