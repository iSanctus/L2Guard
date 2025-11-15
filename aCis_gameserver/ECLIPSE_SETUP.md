# Eclipse Setup Guide for L2Guard Integration

## Good News: Dependencies Included!

All required JAR dependencies are now included in the `libs/` folder when you clone the repository. You should NOT see any compilation errors if you're using the latest version of the branch.

### Verify JARs Are Present

Check that your `libs/` folder contains these 5 files:
- ✅ mariadb-java-client-3.1.4.jar (already there)
- ✅ gson-2.10.1.jar (included)
- ✅ slf4j-api-2.0.9.jar (included)
- ✅ logback-classic-1.4.11.jar (included)
- ✅ logback-core-1.4.11.jar (included)

If these files are missing, pull the latest changes:
```bash
git pull origin claude/acis-server-integration-01ChHJtYTSc4CiezfytgedqB
```

Or use the automated download scripts:
```bash
./download-dependencies.sh    # Linux/Mac
download-dependencies.bat     # Windows
```

### Refresh Eclipse

After importing the project into Eclipse:

1. Right-click on your project in Eclipse
2. Select **"Refresh"** (or press F5)
3. Select **"Project"** → **"Clean..."**
4. Select **"Clean all projects"**
5. Click **OK**

Eclipse will automatically rebuild and you should see **0 errors**!

### Verify Setup

All JARs should show up in **"Referenced Libraries"** in Eclipse's Project Explorer:
- ✅ mariadb-java-client-3.1.4.jar
- ✅ gson-2.10.1.jar
- ✅ slf4j-api-2.0.9.jar
- ✅ logback-classic-1.4.11.jar
- ✅ logback-core-1.4.11.jar

## What These Libraries Do

- **Gson**: Parses JSON configuration files for L2Guard
- **SLF4J**: Logging framework interface
- **Logback**: Logging implementation (writes logs to files)

## Troubleshooting

### If you see compilation errors:

1. **Verify JARs are in the right location**
   - They must be in: `aCis_gameserver/libs/` (or your project name)
   - Not in subdirectories
   - If missing, pull the latest changes or run the download script

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
