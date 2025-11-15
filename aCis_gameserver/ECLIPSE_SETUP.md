# Eclipse Setup Guide for L2Guard Integration

## Quick Fix for Compilation Errors

You're getting 63 errors because the required JAR dependencies are not yet downloaded. Follow these steps:

### Step 1: Download Required JARs

**Option A: Using Terminal (Recommended)**

Open terminal in the `libs/` folder and run:

```bash
cd libs/

# Download Gson (JSON library)
curl -O https://repo1.maven.org/maven2/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar

# Download SLF4J API (Logging interface)
curl -O https://repo1.maven.org/maven2/org/slf4j/slf4j-api/2.0.9/slf4j-api-2.0.9.jar

# Download Logback Classic (Logging implementation)
curl -O https://repo1.maven.org/maven2/ch/qos/logback/logback-classic/1.4.11/logback-classic-1.4.11.jar

# Download Logback Core (Logging core)
curl -O https://repo1.maven.org/maven2/ch/qos/logback/logback-core/1.4.11/logback-core-1.4.11.jar
```

**Option B: Manual Download**

If terminal doesn't work, download these files manually and place them in `libs/`:

1. **gson-2.10.1.jar**
   - URL: https://repo1.maven.org/maven2/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar

2. **slf4j-api-2.0.9.jar**
   - URL: https://repo1.maven.org/maven2/org/slf4j/slf4j-api/2.0.9/slf4j-api-2.0.9.jar

3. **logback-classic-1.4.11.jar**
   - URL: https://repo1.maven.org/maven2/ch/qos/logback/logback-classic/1.4.11/logback-classic-1.4.11.jar

4. **logback-core-1.4.11.jar**
   - URL: https://repo1.maven.org/maven2/ch/qos/logback/logback-core/1.4.11/logback-core-1.4.11.jar

### Step 2: Refresh Eclipse

After downloading the JARs:

1. Right-click on your project in Eclipse
2. Select **"Refresh"** (or press F5)
3. Select **"Project"** → **"Clean..."**
4. Select **"Clean all projects"**
5. Click **OK**

Eclipse will automatically rebuild and all 63 errors should disappear!

### Step 3: Verify Setup

After refresh, you should see in `libs/` folder:
- ✅ mariadb-java-client-3.1.4.jar (already there)
- ✅ gson-2.10.1.jar (newly added)
- ✅ slf4j-api-2.0.9.jar (newly added)
- ✅ logback-classic-1.4.11.jar (newly added)
- ✅ logback-core-1.4.11.jar (newly added)

All JARs should show up in **"Referenced Libraries"** in Eclipse's Project Explorer.

## What These Libraries Do

- **Gson**: Parses JSON configuration files for L2Guard
- **SLF4J**: Logging framework interface
- **Logback**: Logging implementation (writes logs to files)

## Troubleshooting

### If errors persist after download:

1. **Verify JARs are in the right location**
   - They must be in: `PROJECT_TARGON_409/libs/` (or your project name)
   - Not in subdirectories

2. **Check Eclipse classpath**
   - Right-click project → **"Build Path"** → **"Configure Build Path"**
   - Go to **"Libraries"** tab
   - You should see all 5 JAR files listed
   - If not, click **"Add JARs"** and add them manually

3. **Force Eclipse rebuild**
   - **Project** → **"Clean..."**
   - Select your project
   - Check **"Start a build immediately"**
   - Click **OK**

4. **Restart Eclipse**
   - Sometimes Eclipse needs a restart to pick up new JARs
   - Close and reopen Eclipse

### If you see "player.logout() requires arguments" error:

This is now fixed in the latest commit. Pull the latest changes:

```bash
git pull origin claude/acis-server-integration-01ChHJtYTSc4CiezfytgedqB
```

Then refresh Eclipse (F5).

## After All Errors Are Fixed

You should have **0 errors** and the project will compile successfully!

Then you can:
1. Run the server with `GameServer.java`
2. Check logs for L2Guard initialization
3. Configure L2Guard in `game/config/l2guard-server-config.json`

---

**Need help?** See `L2GUARD_INTEGRATION.md` for complete documentation.
