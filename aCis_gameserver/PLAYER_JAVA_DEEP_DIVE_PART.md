# PLAYER.JAVA - COMPLETE DEEP DIVE ANALYSIS

**File Location:** `java/net/sf/l2j/gameserver/model/actor/Player.java`  
**Size:** ~15,000+ lines  
**Status:** ðŸ”¥ **CRITICAL CORE CLASS**  
**Purpose:** Represents every human player character in the game world

---

## ðŸ“‹ TABLE OF CONTENTS

1. [Class Overview](#class-overview)
2. [Class Hierarchy](#class-hierarchy)
3. [Critical Member Variables](#critical-member-variables)
4. [Database Constants](#database-constants)
5. [Core Systems Breakdown](#core-systems-breakdown)
6. [Method Categories](#method-categories)
7. [State Management](#state-management)
8. [Container Systems](#container-systems)
9. [Common Workflows](#common-workflows)
10. [Extension Points](#extension-points)
11. [Performance Considerations](#performance-considerations)
12. [Safety & Threading](#safety--threading)
13. [Integration Points](#integration-points)

---

## ðŸŽ¯ CLASS OVERVIEW

### What is Player.java?

**Player.java** is the **MOST IMPORTANT** class in the entire L2JACIS server. It represents:
- Every logged-in player character
- Player state (HP/MP/CP, inventory, skills, position)
- Player actions (movement, combat, trading, crafting)
- Player social systems (clan, party, friends)
- Player progression (exp, level, subclass)

### Key Characteristics

```java
public final class Player extends Playable
```

- **final** - Cannot be extended
- **extends Playable** - Inherits from playable entities (players + summons)
- **Thread-safe operations** - Uses locks and atomic operations
- **Database-persistent** - Saves state to MySQL database
- **Network-aware** - Sends packets to connected client

---

## ðŸ—ï¸ CLASS HIERARCHY

```
WorldObject (abstract)
    â†“
Creature (abstract)
    â†“
Playable (abstract)
    â†“
Player (final) â† WE ARE HERE
```

### Inheritance Chain

**From WorldObject:**
- `_objectId` - Unique ID in the world
- `_position` - X, Y, Z, heading
- `_isVisible` - Visibility state
- `getKnownList()` - Nearby objects

**From Creature:**
- `_status` (PlayerStatus) - HP/MP/CP, stats
- `_ai` (PlayerAI) - AI behavior
- `_attack` (PlayerAttack) - Attack logic
- `_cast` (PlayerCast) - Spell casting
- `_move` (PlayerMove) - Movement logic
- `_effects` - Active buffs/debuffs
- `_skills` - Known skills

**From Playable:**
- Shared player/summon functionality
- Karma system
- PvP mechanics

---

## ðŸ’¾ CRITICAL MEMBER VARIABLES

### 1. IDENTITY & CONNECTION

```java
// Account & Character
private final String _accountName;           // Account name (e.g., "john_doe")
private GameClient _client;                   // Connected game client
private final Map<Integer, String> _chars = new HashMap<>();  // All chars on account

// Online State
private boolean _isOnline;                    // Is currently logged in?
private long _onlineTime;                     // Total online time
private long _onlineBeginTime;                // Login timestamp
private long _lastAccess;                     // Last access time
private long _uptime;                         // Current session uptime
private long _deleteTimer;                    // Character deletion timer
```

**Usage:**
- `_accountName` - NEVER changes, used for database queries
- `_client` - Can be null if player logged out but still in world
- `_isOnline` - Used to check if player is active

### 2. CLASS SYSTEM

```java
// Class Management
protected int _baseClass;                     // Original class ID
protected int _activeClass;                   // Current active class ID
protected int _classIndex;                    // 0 = base, 1-3 = subclass index

// Subclass System
private final Map<Integer, SubClass> _subClasses = new ConcurrentSkipListMap<>();
private final ReentrantLock _subclassLock = new ReentrantLock();
```

**Key Points:**
- Base class = the first class you choose (can't change)
- Active class = currently playing class
- Subclasses = up to 3 additional classes you can switch to
- `_subclassLock` prevents race conditions during class change

**Example Flow:**
```
Player starts as Fighter (baseClass = 0)
  â†“
Becomes Warrior at level 20 (baseClass = 2, activeClass = 2)
  â†“
Adds subclass Mage (subClasses.put(1, new SubClass(wizard)))
  â†“
Switches to Mage (activeClass = 10, classIndex = 1)
```

### 3. STATISTICS & PROGRESSION

```java
// Experience & Level
private long _expBeforeDeath;                 // Exp before last death
private int _karma;                           // Karma (negative = PK)
private int _pvpKills;                        // PvP kill count
private int _pkKills;                         // PK count
private byte _pvpFlag;                        // PvP flag (0/1)
private int _siegeState;                      // Siege participation state
```

**Karma System:**
- `_karma > 0` = Player killer (PK)
- `_karma == 0` = Normal player
- Karma increases when killing non-flagged players
- Karma decreases over time or by dying

**PvP Flag:**
- `0` = White name (not in PvP)
- `1` = Purple name (in PvP mode)
- Automatically flagged when attacking another player

### 4. APPEARANCE & CUSTOMIZATION

```java
private final Appearance _appearance;         // Face, hair, sex
```

**Appearance class contains:**
```java
public class Appearance {
    private byte _face;                       // Face ID (0-2)
    private byte _hairColor;                  // Hair color (0-3)
    private byte _hairStyle;                  // Hair style (0-5)
    private Sex _sex;                         // MALE or FEMALE
    private int _nameColor = 0xFFFFFF;        // Name color (RGB hex)
    private int _titleColor = 0xFFFF77;       // Title color (RGB hex)
    private boolean _isVisible = true;        // Visibility (for GMs)
}
```

### 5. INVENTORY & CONTAINERS

```java
// Inventory System
private final PcInventory _inventory = new PcInventory(this);
private final InventoryUpdate _iu = new InventoryUpdate(this);
private final List<PcFreight> _depositedFreight = new ArrayList<>();

// Warehouses
private PcWarehouse _warehouse;               // Personal warehouse
private PcFreight _freight;                   // Freight (at NPCs)

// Trading & Stores
private TradeList _activeTradeList;           // Active trade
private ItemContainer _activeWarehouse;       // Open warehouse
private final TradeList _buyList = new TradeList(this);
private final TradeList _sellList = new TradeList(this);
private final ManufactureList _manufactureList = new ManufactureList();
private OperateType _operateType = OperateType.NONE;  // Store mode
```

**Container Types:**
- `PcInventory` - Items on player (max 80 slots, dwarf 100)
- `PcWarehouse` - Personal storage (100-120 slots)
- `PcFreight` - Freight storage at NPCs
- `TradeList` - Items being traded
- `ManufactureList` - Crafting list (dwarves)

### 6. SOCIAL SYSTEMS

```java
// Clan System
private int _clanId;                          // Clan ID
private Clan _clan;                           // Clan object
private int _apprentice;                      // Apprentice player ID
private int _sponsor;                         // Sponsor player ID
private long _clanJoinExpiryTime;             // Clan join cooldown
private long _clanCreateExpiryTime;           // Clan create cooldown
private int _powerGrade;                      // Clan rank
private int _pledgeClass;                     // Pledge class
private int _pledgeType;                      // Pledge type (0=main, 100=academy, etc.)
private int _lvlJoinedAcademy;                // Academy join level

// Party System
private Party _party;                         // Current party
private LootRule _lootRule;                   // Loot distribution rule

// Recommendations
private int _recomHave;                       // Recommendations received
private int _recomLeft;                       // Recommendations left to give
private final List<Integer> _recomChars = new ArrayList<>();  // Given recommendations

// Marriage
private int _coupleId;                        // Couple ID if married
private boolean _isUnderMarryRequest;         // Pending marriage proposal
private int _requesterId;                     // Marriage requester ID
```

### 7. PLAYER STATE FLAGS

```java
// Combat State
private boolean _isFakeDeath;                 // Fake death effect active?
private long _recentFakeDeathEndTime;         // Fake death protection timer
private boolean _weaponGradePenalty;          // Weapon grade penalty?
private int _armorGradePenalty;               // Armor grade penalty level
private WeightPenalty _weightPenalty = WeightPenalty.NONE;

// Position States
private boolean _isSitting;                   // Is sitting?
private boolean _isStanding;                  // Is standing?
private boolean _isSittingNow;                // Transition: sitting
private boolean _isStandingNow;               // Transition: standing

// Activity States
private boolean _isCrafting;                  // Is crafting?
private boolean _isCrystallizing;             // Is crystallizing?
private boolean _inventoryDisable;            // Inventory disabled?

// GM States
private AccessLevel _accessLevel;             // Access level (GM)
private Location _enterWorld;                 // Debug: enter world location
private final Map<String, ExServerPrimitive> _debug = new HashMap<>();  // Debug packets

// Seven Signs
private boolean _isIn7sDungeon;               // In Seven Signs dungeon?
private int _alliedVarkaKetra;                // Alliance: [-5,-1] Varka, [1,5] Ketra
```

### 8. COMBAT & SKILLS

```java
// Skill Management
private final Map<Integer, L2Skill> _skills = new ConcurrentSkipListMap<>();
private final Map<Integer, Timestamp> _reuseTimeStamps = new ConcurrentHashMap<>();

// Effects & Charges
private final AtomicInteger _charges = new AtomicInteger();  // Force charges
private ScheduledFuture<?> _chargeTask;       // Charge decay task

// Auto-shots
private final Set<Integer> _activeSoulShots = ConcurrentHashMap.newKeySet(1);

// Active Items
private ItemInstance _activeEnchantItem;      // Item being enchanted
```

### 9. SUMMONING & MOUNTS

```java
// Pet/Summon System
private Summon _summon;                       // Active summon
private TamedBeast _tamedBeast;               // Tamed beast
private PetTemplate _petTemplate;             // Pet template
private PetDataEntry _petData;                // Pet data entry
private int _controlItemId;                   // Control item ID
private int _curFeed;                         // Current feed level
protected Future<?> _mountFeedTask;           // Feeding task
private boolean _canFeed;                     // Can feed mount?

// Mount System
private int _mountType;                       // 0=none, 1=strider, 2=wyvern
private int _mountNpcId;                      // Mount NPC ID
private int _mountLevel;                      // Mount level
private int _mountObjectId;                   // Mount item object ID
private ScheduledFuture<?> _dismountTask;     // Dismount task

// Throne/Seat
protected int _throneId;                      // Throne object ID
```

### 10. OLYMPIAD & EVENTS

```java
// Olympiad System
private boolean _isInOlympiadMode;            // In olympiad match?
private boolean _isInOlympiadStart;           // Match starting?
private int _olympiadGameId = -1;             // Game ID
private int _olympiadSide = -1;               // Side (1 or 2)

// Duel System
private DuelState _duelState = DuelState.NO_DUEL;
private int _duelId;                          // Duel ID
private SystemMessageId _noDuelReason;        // Reason can't duel

// Hero System
private boolean _isNoble;                     // Noble status
private boolean _isHero;                      // Hero status

// Cursed Weapons
private int _cursedWeaponEquippedId;          // Cursed weapon ID
```

### 11. PLAYER HELPERS & CONTAINERS

```java
// Helper Objects
private final Appearance _appearance;
private final Punishment _punishment = new Punishment(this);
private final RecipeBook _recipeBook = new RecipeBook(this);
private final PlayerMemo _memos = new PlayerMemo(getObjectId());
private final FishingStance _fishingStance = new FishingStance(this);
private final ShortcutList _shortcutList = new ShortcutList(this);
private final MacroList _macroList = new MacroList(this);
private final HennaList _hennaList = new HennaList(this);
private final RadarList _radarList = new RadarList(this);
private final CubicList _cubicList = new CubicList(this);
private final QuestList _questList = new QuestList(this);
private final Request _request = new Request(this);
```

### 12. TELEPORT & LOCATION

```java
private TeleportMode _teleportMode = TeleportMode.NONE;
private final Location _savedLocation = new Location(0, 0, 0);
private BoatInfo _boatInfo = new BoatInfo(this);
private int _partyRoom;                       // Party match room ID
```

### 13. MISCELLANEOUS STATE

```java
// PC Bang Points
private int _pcBangPoints = 0;

// Death Penalty
private int _deathPenaltyBuffLevel;

// Protection
private ScheduledFuture<?> _protectTask;      // Spawn protection task

// Folk Interaction
private Folk _currentFolk;                    // Current NPC

// Multisell
private PreparedListContainer _currentMultiSell;

// Compass
private int _lastCompassZone;                 // Last compass zone update

// Request System
private Player _activeRequester;              // Active requester
private long _requestExpireTime;              // Request expiry time

// Revival
private int _reviveRequested;                 // Revival requested?
private double _revivePower = .0;             // Revival power
private boolean _revivePet;                   // Reviving pet?

// Mail
private int _mailPosition;                    // Current mail position

// Validation
private final List<String> _validBypass = new ArrayList<>();
private final List<String> _validBypass2 = new ArrayList<>();

// Falling
private volatile long _fallingTimestamp;      // Falling validation timer

// Team
private TeamType _team = TeamType.NONE;       // Event team

// Buffer
private ScheduledFuture<?> _shortBuffTask;    // Short buff task
private int _shortBuffTaskSkillId;            // Short buff skill ID

// Summon Request
private Player _summonTargetRequest;          // Summon friend requester
private L2Skill _summonSkillRequest;          // Summon skill

// Gate Request
private Door _requestedGate;                  // Requested gate/door

// Peace Mode
private boolean _wantsPeace;                  // Wants peace mode?

// Block System
private boolean _isBlockingAll;               // Blocking all messages?
private final Set<Integer> _selectedBlocksList = ConcurrentHashMap.newKeySet();
private final Set<Integer> _selectedFriendList = ConcurrentHashMap.newKeySet();

// Forum
private Forum _forumMemo;                     // Community forum memo

// Lotto & Race
private final int[] _loto = new int[5];       // Lotto numbers
private final int[] _race = new int[2];       // Race numbers

// Auto Farm (Custom Addition)
private final L2AutoFarmTask _farm = new L2AutoFarmTask(this);

// Private Store (Custom Fields)
private PrivateStoreType _privateStoreType = PrivateStoreType.NONE;
private String tempBuffShopPrice;             // Buff shop price
private boolean _isDummy;                     // Is dummy player?
```

---

## ðŸ—„ï¸ DATABASE CONSTANTS

### SQL Prepared Statements

```java
// SKILLS
private static final String RESTORE_SKILLS_FOR_CHAR = 
    "SELECT skill_id,skill_level FROM character_skills WHERE char_obj_id=? AND class_index=?";
    
private static final String ADD_OR_UPDATE_SKILL = 
    "INSERT INTO character_skills (char_obj_id,skill_id,skill_level,class_index) VALUES (?,?,?,?) ON DUPLICATE KEY UPDATE skill_level=VALUES(skill_level)";
    
private static final String DELETE_SKILL_FROM_CHAR = 
    "DELETE FROM character_skills WHERE skill_id=? AND char_obj_id=? AND class_index=?";
    
private static final String DELETE_CHAR_SKILLS = 
    "DELETE FROM character_skills WHERE char_obj_id=? AND class_index=?";

// SKILL SAVE (Buffs/Cooldowns)
private static final String ADD_SKILL_SAVE = 
    "INSERT INTO character_skills_save (char_obj_id,skill_id,skill_level,effect_count,effect_cur_time,reuse_delay,systime,restore_type,class_index,buff_index) VALUES (?,?,?,?,?,?,?,?,?,?)";
    
private static final String RESTORE_SKILL_SAVE = 
    "SELECT skill_id,skill_level,effect_count,effect_cur_time, reuse_delay, systime, restore_type FROM character_skills_save WHERE char_obj_id=? AND class_index=? ORDER BY buff_index ASC";
    
private static final String DELETE_SKILL_SAVE = 
    "DELETE FROM character_skills_save WHERE char_obj_id=? AND class_index=?";

// CHARACTER DATA
private static final String INSERT_CHARACTER = 
    "INSERT INTO characters (account_name,obj_Id,char_name,level,maxHp,curHp,maxCp,curCp,maxMp,curMp,face,hairStyle,hairColor,sex,exp,sp,race,classid,base_class,title,accesslevel) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    
private static final String UPDATE_CHARACTER = 
    "UPDATE characters SET level=?,maxHp=?,curHp=?,maxCp=?,curCp=?,maxMp=?,curMp=?,face=?,hairStyle=?,hairColor=?,sex=?,heading=?,x=?,y=?,z=?,exp=?,expBeforeDeath=?,sp=?,karma=?,pvpkills=?,pkkills=?,clanid=?,race=?,classid=?,deletetime=?,title=?,accesslevel=?,online=?,isin7sdungeon=?,wantspeace=?,base_class=?,onlinetime=?,punish_level=?,punish_timer=?,nobless=?,power_grade=?,subpledge=?,lvl_joined_academy=?,apprentice=?,sponsor=?,varka_ketra_ally=?,clan_join_expiry_time=?,clan_create_expiry_time=?,char_name=?,death_penalty_level=? WHERE obj_id=?";
    
private static final String RESTORE_CHARACTER = 
    "SELECT * FROM characters WHERE obj_id=?";

// SUBCLASS
private static final String RESTORE_CHAR_SUBCLASSES = 
    "SELECT class_id,exp,sp,level,class_index FROM character_subclasses WHERE char_obj_id=? ORDER BY class_index ASC";
    
private static final String ADD_CHAR_SUBCLASS = 
    "INSERT INTO character_subclasses (char_obj_id,class_id,exp,sp,level,class_index) VALUES (?,?,?,?,?,?)";
    
private static final String UPDATE_CHAR_SUBCLASS = 
    "UPDATE character_subclasses SET exp=?,sp=?,level=?,class_id=? WHERE char_obj_id=? AND class_index =?";
    
private static final String DELETE_CHAR_SUBCLASS = 
    "DELETE FROM character_subclasses WHERE char_obj_id=? AND class_index=?";

// OTHER DELETIONS
private static final String DELETE_CHAR_HENNAS = 
    "DELETE FROM character_hennas WHERE char_obj_id=? AND class_index=?";
    
private static final String DELETE_CHAR_SHORTCUTS = 
    "DELETE FROM character_shortcuts WHERE char_obj_id=? AND class_index=?";

// RECOMMENDATIONS
private static final String RESTORE_CHAR_RECOMS = 
    "SELECT char_id,target_id FROM character_recommends WHERE char_id=?";
    
private static final String ADD_CHAR_RECOM = 
    "INSERT INTO character_recommends (char_id,target_id) VALUES (?,?)";
    
private static final String UPDATE_TARGET_RECOM_HAVE = 
    "UPDATE characters SET rec_have=? WHERE obj_Id=?";
    
private static final String UPDATE_CHAR_RECOM_LEFT = 
    "UPDATE characters SET rec_left=? WHERE obj_Id=?";

// NOBILITY
private static final String UPDATE_NOBLESS = 
    "UPDATE characters SET nobless=? WHERE obj_Id=?";
```

**Key Points:**
- Every player action that changes state is persisted to database
- Skills, effects, and cooldowns are saved on logout
- Subclass system has its own tables
- ON DUPLICATE KEY UPDATE = update if exists, insert if not

---

## ðŸŽ® CORE SYSTEMS BREAKDOWN

### System 1: CONSTRUCTORS & CREATION

#### Constructor (Restore Existing Player)

```java
public Player(int objectId, PlayerTemplate template, String accountName, Appearance app) {
    super(objectId, template);  // Call Creature constructor
    
    getStatus().initializeValues();  // Initialize HP/MP/CP
    
    _accountName = accountName;      // Set account
    _appearance = app;               // Set appearance
    
    _ai = new PlayerAI(this);        // Create AI
    
    // Restore inventory, warehouse, freight from database
    getInventory().restore();
    getWarehouse();
    getFreight();
}
```

#### Static Factory: Create New Player

```java
public static Player create(int objectId, PlayerTemplate template, 
                            String accountName, String name, 
                            byte hairStyle, byte hairColor, byte face, Sex sex)
{
    // 1. Create appearance
    final Appearance app = new Appearance(face, hairColor, hairStyle, sex);
    
    // 2. Create player object
    final Player player = new Player(objectId, template, accountName, app);
    
    // 3. Set name
    player.setName(name);
    
    // 4. Set default access level
    player.setAccessLevel(Config.DEFAULT_ACCESS_LEVEL);
    
    // 5. Cache player info
    PlayerInfoTable.getInstance().addPlayer(objectId, accountName, name, player.getAccessLevel().getLevel());
    
    // 6. Set base class
    player.setBaseClass(player.getClassId());
    
    // 7. INSERT into database
    try (Connection con = ConnectionPool.getConnection();
         PreparedStatement ps = con.prepareStatement(INSERT_CHARACTER)) {
        
        ps.setString(1, accountName);
        ps.setInt(2, player.getObjectId());
        ps.setString(3, player.getName());
        // ... (21 total parameters)
        ps.executeUpdate();
    }
    
    return player;
}
```

**Creation Flow:**
```
Client requests character creation
    â†“
RequestCharacterCreate packet
    â†“
Player.create() factory method
    â†“
INSERT INTO characters table
    â†“
Return Player object
    â†“
Client enters world
```

#### Static Factory: Restore Existing Player

```java
public static Player restore(int objectId) {
    Player player = null;
    
    try (Connection con = ConnectionPool.getConnection();
         PreparedStatement ps = con.prepareStatement(RESTORE_CHARACTER)) {
        
        ps.setInt(1, objectId);
        
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                // 1. Get class info
                final int activeClassId = rs.getInt("classid");
                final PlayerTemplate template = PlayerData.getInstance().getTemplate(activeClassId);
                
                // 2. Get appearance
                final Appearance app = new Appearance(
                    rs.getByte("face"), 
                    rs.getByte("hairColor"), 
                    rs.getByte("hairStyle"), 
                    Sex.VALUES[rs.getInt("sex")]
                );
                
                // 3. Create player
                player = new Player(objectId, template, rs.getString("account_name"), app);
                
                // 4. Restore all fields from ResultSet
                player.setName(rs.getString("char_name"));
                player._lastAccess = rs.getLong("lastAccess");
                player.getStatus().setExp(rs.getLong("exp"));
                player.getStatus().setLevel(rs.getByte("level"));
                player.getStatus().setSp(rs.getInt("sp"));
                // ... (50+ more fields)
                
                // 5. Restore clan
                final Clan clan = ClanTable.getInstance().getClan(rs.getInt("clanid"));
                if (clan != null) {
                    player.setClan(clan);
                    player.setPowerGrade(rs.getInt("power_grade"));
                    player.setPledgeType(rs.getInt("subpledge"));
                }
                
                // 6. Restore subclasses
                if (restoreSubClassData(player) && activeClassId != player.getBaseClass()) {
                    for (SubClass subClass : player.getSubClasses().values()) {
                        if (subClass.getClassId() == activeClassId) {
                            player._classIndex = subClass.getClassIndex();
                        }
                    }
                }
                
                // 7. Restore secondary data (skills, hennas, quests, etc.)
                player.restoreCharData();
                player.giveSkills();
                
                // 8. Restore buffs/cooldowns if enabled
                if (Config.STORE_SKILL_COOLTIME)
                    player.restoreEffects();
                
                // 9. Restore pet if exists
                final Pet pet = World.getInstance().getPet(player.getObjectId());
                if (pet != null) {
                    player.setSummon(pet);
                    pet.setOwner(player);
                }
                
                // 10. Apply penalties
                player.refreshWeightPenalty();
                player.refreshExpertisePenalty();
                player.refreshHennaList();
                
                // 11. Set online
                player.setOnlineStatus(true, false);
                player.setRunning(true);
                player.setStanding(true);
                
                // 12. Restore HP/MP/CP
                final double currentHp = rs.getDouble("curHp");
                player.getStatus().setCpHpMp(rs.getDouble("curCp"), currentHp, rs.getDouble("curMp"));
                
                // If HP < 0.5, player is dead
                if (currentHp < 0.5) {
                    player.setIsDead(true);
                    player.getStatus().stopHpMpRegeneration();
                }
                
                // 13. Add to world
                World.getInstance().addPlayer(player);
                
                break;
            }
        }
    } catch (Exception e) {
        LOGGER.error("Couldn't restore player data.", e);
    }
    
    return player;
}
```

**Restore Flow:**
```
Client sends login packet
    â†“
SELECT * FROM characters WHERE obj_id=?
    â†“
Player.restore() factory method
    â†“
Load all data from database
    â†“
Create Player object with all state
    â†“
Add to World
    â†“
Client sees character selection screen
```

---

### System 2: SAVE & PERSISTENCE

#### Main Save Method

```java
public synchronized void store(boolean storeActiveEffects) {
    storeCharBase();          // Save base character data
    storeCharSub();           // Save subclass data
    storeEffect(storeActiveEffects);  // Save buffs/cooldowns
}
```

#### Store Base Character Data

```java
public void storeCharBase() {
    // Get exp/level/sp from BASE class to store (not subclass)
    final int currentClassIndex = getClassIndex();
    
    _classIndex = 0;  // Switch to base class temporarily
    
    final long exp = getStatus().getExp();
    final int level = getStatus().getLevel();
    final int sp = getStatus().getSp();
    
    _classIndex = currentClassIndex;  // Restore current class
    
    try (Connection con = ConnectionPool.getConnection();
         PreparedStatement ps = con.prepareStatement(UPDATE_CHARACTER)) {
        
        // Set 46 parameters
        ps.setInt(1, level);
        ps.setInt(2, getStatus().getMaxHp());
        ps.setDouble(3, getStatus().getHp());
        // ... (43 more parameters)
        ps.setInt(46, getObjectId());  // WHERE obj_id=?
        
        ps.execute();
    }
}
```

**What Gets Saved:**
- Current level, exp, SP
- Current HP/MP/CP and MAX values
- Appearance (face, hair, etc.)
- Position (X, Y, Z, heading)
- Karma, PvP kills, PK kills
- Clan info
- Base class and active class
- Online status
- Delete timer
- Title
- Access level
- Seven Signs status
- Noble status
- Power grade
- Subpledge type
- Academy join level
- Apprentice/sponsor
- Varka/Ketra alliance
- Clan join/create expiry times
- Character name
- Death penalty level

#### Store Subclass Data

```java
private void storeCharSub() {
    if (_subClasses.isEmpty())
        return;
    
    try (Connection con = ConnectionPool.getConnection();
         PreparedStatement ps = con.prepareStatement(UPDATE_CHAR_SUBCLASS)) {
        
        for (SubClass subClass : _subClasses.values()) {
            ps.setLong(1, subClass.getExp());
            ps.setInt(2, subClass.getSp());
            ps.setInt(3, subClass.getLevel());
            ps.setInt(4, subClass.getClassId());
            ps.setInt(5, getObjectId());
            ps.setInt(6, subClass.getClassIndex());
            ps.addBatch();
        }
        
        ps.executeBatch();
    }
}
```

#### Store Effects & Cooldowns

```java
public void storeEffect(boolean storeEffects) {
    if (!Config.STORE_SKILL_COOLTIME || isInDuel())
        return;
    
    try (Connection con = ConnectionPool.getConnection()) {
        // 1. Delete all stored effects
        try (PreparedStatement ps = con.prepareStatement(DELETE_SKILL_SAVE)) {
            ps.setInt(1, getObjectId());
            ps.setInt(2, getClassIndex());
            ps.executeUpdate();
        }
        
        int index = 0;
        final List<Integer> storedSkills = new ArrayList<>();
        
        try (PreparedStatement ps = con.prepareStatement(ADD_SKILL_SAVE)) {
            // 2. Store all active effects (buffs)
            if (storeEffects) {
                for (AbstractEffect effect : getAllEffects()) {
                    // Skip HoT effects
                    if (effect.getEffectType() == EffectType.HEAL_OVER_TIME)
                        continue;
                    
                    final L2Skill skill = effect.getSkill();
                    
                    // Don't store the same skill twice
                    if (storedSkills.contains(skill.getReuseHashCode()))
                        continue;
                    
                    storedSkills.add(skill.getReuseHashCode());
                    
                    // Don't store herbs or toggles
                    if (effect.isHerbEffect() || skill.isToggle() || skill.getSkillType() == SkillType.CONT)
                        continue;
                    
                    ps.setInt(1, getObjectId());
                    ps.setInt(2, skill.getId());
                    ps.setInt(3, skill.getLevel());
                    ps.setInt(4, effect.getCount());
                    ps.setInt(5, effect.getTime());
                    
                    // Store reuse delay if exists
                    final Timestamp timestamp = _reuseTimeStamps.get(skill.getReuseHashCode());
                    if (timestamp != null && timestamp.hasNotPassed()) {
                        ps.setLong(6, timestamp.reuse());
                        ps.setDouble(7, timestamp.stamp());
                    } else {
                        ps.setLong(6, 0);
                        ps.setDouble(7, 0);
                    }
                    
                    ps.setInt(8, 0);  // restore_type = 0 (effect)
                    ps.setInt(9, getClassIndex());
                    ps.setInt(10, ++index);
                    ps.addBatch();
                }
            }
            
            // 3. Store leftover skill cooldowns (without effects)
            for (Map.Entry<Integer, Timestamp> entry : _reuseTimeStamps.entrySet()) {
                final int hash = entry.getKey();
                
                if (storedSkills.contains(hash))
                    continue;
                
                final Timestamp timestamp = entry.getValue();
                if (timestamp != null && timestamp.hasNotPassed()) {
                    storedSkills.add(hash);
                    
                    ps.setInt(1, getObjectId());
                    ps.setInt(2, timestamp.skillId());
                    ps.setInt(3, timestamp.skillLevel());
                    ps.setInt(4, -1);  // No effect count
                    ps.setInt(5, -1);  // No effect time
                    ps.setLong(6, timestamp.reuse());
                    ps.setDouble(7, timestamp.stamp());
                    ps.setInt(8, 1);  // restore_type = 1 (cooldown only)
                    ps.setInt(9, getClassIndex());
                    ps.setInt(10, ++index);
                    ps.addBatch();
                }
            }
            
            ps.executeBatch();
        }
    }
}
```

**Effect Storage:**
- `restore_type = 0` - Active effect with buff icon
- `restore_type = 1` - Cooldown only (no buff icon)
- Stores remaining time, effect count, and reuse delay
- Allows players to keep buffs on relog

---

### System 3: SKILL MANAGEMENT

#### Add Skill

```java
public boolean addSkill(L2Skill newSkill, boolean store, boolean updateShortcuts) {
    // 1. Validate
    if (newSkill == null)
        return false;
    
    // 2. Check if skill already exists at same level
    final L2Skill oldSkill = getSkills().get(newSkill.getId());
    if (oldSkill != null && oldSkill.equals(newSkill))
        return false;  // Already have this skill at this level
    
    // 3. Add skill to map
    getSkills().put(newSkill.getId(), newSkill);
    
    // 4. Remove old skill functions
    if (oldSkill != null) {
        // If skill came with another one, delete the other one too
        if (oldSkill.triggerAnotherSkill())
            removeSkill(oldSkill.getTriggeredId(), false);
        
        removeStatsByOwner(oldSkill);
    }
    
    // 5. Add new skill functions (stat modifiers)
    addStatFuncs(newSkill.getStatFuncs(this));
    
    // 6. Test and delete chance skill if found
    if (oldSkill != null && getChanceSkills() != null)
        removeChanceSkill(oldSkill.getId());
    
    // 7. If new skill is a chance skill, trigger it
    if (newSkill.isChance())
        addChanceTrigger(newSkill);
    
    // 8. Store to database if requested
    if (store)
        storeSkill(newSkill, -1);
    
    // 9. Update shortcuts if requested
    if (updateShortcuts)
        getShortcutList().refreshShortcuts(
            s -> s.getId() == newSkill.getId() && s.getType() == ShortcutType.SKILL, 
            newSkill.getLevel()
        );
    
    return true;
}
```

#### Remove Skill

```java
public L2Skill removeSkill(int skillId, boolean store, boolean removeEffect) {
    // 1. Remove from skills map
    final L2Skill oldSkill = getSkills().remove(skillId);
    if (oldSkill == null)
        return null;
    
    // 2. Remove triggered skills if any
    if (oldSkill.triggerAnotherSkill() && oldSkill.getTriggeredId() > 0)
        removeSkill(oldSkill.getTriggeredId(), false);
    
    // 3. Stop casting if using this skill
    if (getCast().getCurrentSkill() != null && 
        skillId == getCast().getCurrentSkill().getId())
        getCast().stop();
    
    // 4. Remove stat functions and effects
    if (removeEffect) {
        removeStatsByOwner(oldSkill);
        stopSkillEffects(skillId);
    }
    
    // 5. Remove chance skill if it was one
    if (oldSkill.isChance() && getChanceSkills() != null)
        removeChanceSkill(skillId);
    
    // 6. Delete from database if requested
    if (store) {
        try (Connection con = ConnectionPool.getConnection();
             PreparedStatement ps = con.prepareStatement(DELETE_SKILL_FROM_CHAR)) {
            ps.setInt(1, skillId);
            ps.setInt(2, getObjectId());
            ps.setInt(3, getClassIndex());
            ps.execute();
        }
        
        // Remove shortcuts
        if (!oldSkill.isPassive())
            getShortcutList().deleteShortcuts(skillId, ShortcutType.SKILL);
    }
    
    return oldSkill;
}
```

#### Give Skills (Auto-Learn or Manual Learn)

```java
public void giveSkills() {
    if (Config.AUTO_LEARN_SKILLS) {
        rewardSkills();  // Give ALL skills
    } else {
        // Give only AUTO-GET skills (passive skills and free skills)
        for (GeneralSkillNode skill : getAvailableAutoGetSkills())
            addSkill(skill.getSkill(), false);
        
        // Remove Lucky skill if level >= 10
        if (getStatus().getLevel() >= 10 && hasSkill(L2Skill.SKILL_LUCKY))
            removeSkill(L2Skill.SKILL_LUCKY, false);
        
        // Remove invalid skills
        removeInvalidSkills();
        
        sendPacket(new SkillList(this));
    }
}
```

#### Reward Skills (All Available Skills)

```java
public void rewardSkills() {
    // Give all available skills (auto-get + general)
    for (GeneralSkillNode skill : getAllAvailableSkills())
        addSkill(skill.getSkill(), skill.getCost() != 0, true);
    
    // Remove Lucky skill if level >= 10
    if (getStatus().getLevel() >= 10 && hasSkill(L2Skill.SKILL_LUCKY))
        removeSkill(L2Skill.SKILL_LUCKY, false);
    
    // Remove invalid skills
    removeInvalidSkills();
    
    sendPacket(new SkillList(this));
}
```

#### Get Available Skills

```java
// Get available AUTO-GET skills (max level only)
public List<GeneralSkillNode> getAvailableAutoGetSkills() {
    final List<GeneralSkillNode> result = new ArrayList<>();
    
    getTemplate().getSkills().stream()
        .filter(s -> s.getMinLvl() <= getStatus().getLevel() && s.getCost() == 0)
        .collect(Collectors.groupingBy(s -> s.getId(), Collectors.maxBy(COMPARE_SKILLS_BY_LVL)))
        .forEach((i, s) -> {
            if (getSkillLevel(i) < s.get().getValue())
                result.add(s.get());
        });
    
    return result;
}

// Get available GENERAL skills (need to buy from NPC)
public List<GeneralSkillNode> getAvailableSkills() {
    final List<GeneralSkillNode> result = new ArrayList<>();
    
    getTemplate().getSkills().stream()
        .filter(s -> s.getMinLvl() <= getStatus().getLevel() && s.getCost() != 0)
        .forEach(s -> {
            if (getSkillLevel(s.getId()) == s.getValue() - 1)
                result.add(s);
        });
    
    return result;
}

// Get ALL available skills (auto-get + general, max level only)
public List<GeneralSkillNode> getAllAvailableSkills() {
    final List<GeneralSkillNode> result = new ArrayList<>();
    
    getTemplate().getSkills().stream()
        .filter(s -> s.getMinLvl() <= getStatus().getLevel())
        .collect(Collectors.groupingBy(s -> s.getId(), Collectors.maxBy(COMPARE_SKILLS_BY_LVL)))
        .forEach((i, s) -> {
            if (getSkillLevel(i) < s.get().getValue())
                result.add(s.get());
        });
    
    return result;
}
```

---

### System 4: SUBCLASS SYSTEM

#### Add Subclass

```java
public boolean addSubClass(int classId, int classIndex) {
    if (!_subclassLock.tryLock())
        return false;  // Already being modified
    
    try {
        // 1. Validate
        if (_subClasses.size() == 3 || classIndex == 0 || _subClasses.containsKey(classIndex))
            return false;
        
        // 2. Create subclass
        final SubClass subclass = new SubClass(classId, classIndex);
        
        // 3. Insert into database
        try (Connection con = ConnectionPool.getConnection();
             PreparedStatement ps = con.prepareStatement(ADD_CHAR_SUBCLASS)) {
            ps.setInt(1, getObjectId());
            ps.setInt(2, subclass.getClassId());
            ps.setLong(3, subclass.getExp());
            ps.setInt(4, subclass.getSp());
            ps.setInt(5, subclass.getLevel());
            ps.setInt(6, subclass.getClassIndex());
            ps.execute();
        }
        
        // 4. Add to map
        _subClasses.put(subclass.getClassIndex(), subclass);
        
        // 5. Store skills for new subclass (up to level 40)
        PlayerData.getInstance().getTemplate(classId).getSkills().stream()
            .filter(s -> s.getMinLvl() <= 40)
            .collect(Collectors.groupingBy(s -> s.getId(), Collectors.maxBy(COMPARE_SKILLS_BY_LVL)))
            .forEach((i, s) -> storeSkill(s.get().getSkill(), classIndex));
        
        return true;
    } finally {
        _subclassLock.unlock();
    }
}
```

#### Set Active Class (Switch Classes)

```java
public boolean setActiveClass(int classIndex) {
    if (!_subclassLock.tryLock())
        return false;  // Already being modified
    
    SubClass subclass = null;
    if (classIndex != 0) {
        subclass = _subClasses.get(classIndex);
        if (subclass == null)
            return false;
    }
    
    try {
        // 1. Remove active augmented item skills
        for (ItemInstance item : getInventory().getAugmentedItems()) {
            if (item != null && item.isEquipped())
                item.getAugmentation().removeBonus(this);
        }
        
        // 2. Stop casting
        getCast().stop();
        
        // 3. Stop fusion skills from others
        forEachKnownType(Creature.class, 
            creature -> creature.getFusionSkill() != null && 
                       creature.getFusionSkill().getTarget() == this, 
            creature -> creature.getCast().stop());
        
        // 4. Save current class data
        store();
        _reuseTimeStamps.clear();
        
        // 5. Clear charges
        _charges.set(0);
        stopChargeTask();
        
        // 6. Set new class template
        setClassTemplate((subclass == null) ? getBaseClass() : subclass.getClassId());
        
        // 7. Update class index
        _classIndex = classIndex;
        
        // 8. Recalculate party level
        if (_party != null)
            _party.recalculateLevel();
        
        // 9. Unsummon servitor
        if (_summon instanceof Servitor)
            _summon.unSummon(this);
        
        // 10. Remove all skills
        for (L2Skill skill : getSkills().values())
            removeSkill(skill.getId(), false);
        
        // 11. Remove all effects except death-lasting ones
        stopAllEffectsExceptThoseThatLastThroughDeath();
        _cubicList.stopCubics(true);
        
        // 12. Clear/restore recipe book
        if (isSubClassActive())
            _recipeBook.clear();
        else
            _recipeBook.restore();
        
        // 13. Restore hennas
        _hennaList.restore();
        
        // 14. Restore skills
        restoreSkills();
        giveSkills();
        regiveTemporarySkills();  // Noble, Hero, Clan skills
        
        // 15. Clear disabled skills
        getDisabledSkills().clear();
        
        // 16. Restore effects
        restoreEffects();
        updateEffectIcons();
        sendPacket(new EtcStatusUpdate(this));
        
        // 17. Remove "Repent Your Sins" quest if exists
        final QuestState st = _questList.getQuestState("Q422_RepentYourSins");
        if (st != null)
            st.exitQuest(true);
        
        // 18. Cap HP/MP/CP to new max values
        int max = getStatus().getMaxHp();
        if (getStatus().getHp() > max)
            getStatus().setHp(max);
        
        max = getStatus().getMaxMp();
        if (getStatus().getMp() > max)
            getStatus().setMp(max);
        
        max = getStatus().getMaxCp();
        if (getStatus().getCp() > max)
            getStatus().setCp(max);
        
        // 19. Refresh penalties
        refreshWeightPenalty();
        refreshExpertisePenalty();
        refreshHennaList();
        broadcastUserInfo();
        
        // 20. Clear resurrect xp
        setExpBeforeDeath(0);
        
        // 21. Remove shot automation
        disableAutoShotsAll();
        
        // 22. Discharge weapon shots
        final ItemInstance item = getActiveWeaponInstance();
        if (item != null)
            item.unChargeAllShots();
        
        // 23. Restore shortcuts
        _shortcutList.restore();
        sendPacket(new ShortCutInit(this));
        
        // 24. Broadcast social action (class change animation)
        broadcastPacket(new SocialAction(this, 15));
        
        // 25. Send skill cooldown
        sendPacket(new SkillCoolTime(this));
        
        return true;
    } finally {
        _subclassLock.unlock();
    }
}
```

**Class Switch Flow:**
```
1. Player talks to Subclass NPC
2. Clicks "Change Class" button
3. Server calls setActiveClass(classIndex)
4. Save current class data to database
5. Remove all skills and effects
6. Change class template
7. Restore skills for new class
8. Restore effects for new class
9. Update shortcuts
10. Send class change animation
11. Player is now in new class
```

---

### System 5: DEATH & RESURRECTION

#### Death (doDie)

```java
@Override
public boolean doDie(Creature killer) {
    // 1. Call parent death logic
    if (!super.doDie(killer))
        return false;
    
    // 2. Stop mount feeding
    if (isMounted())
        stopFeed();
    
    // 3. Clear charges
    clearCharges();
    
    // 4. Stop fake death
    synchronized (this) {
        if (isFakeDeath())
            stopFakeDeath(true);
        if (getFarm().running())
            getFarm().stop();
    }
    
    if (killer != null) {
        final Player pk = killer.getActingPlayer();
        
        // 5. Clear resurrect xp
        setExpBeforeDeath(0);
        
        // 6. Handle cursed weapon
        if (isCursedWeaponEquipped())
            CursedWeaponManager.getInstance().drop(_cursedWeaponEquippedId, killer);
        else {
            if (pk == null || !pk.isCursedWeaponEquipped()) {
                // 7. Drop items on death
                onDieDropItem(killer);
                
                // 8. Clan wars reputation
                if (!isInArena()) {
                    if (pk != null && pk.getClan() != null && getClan() != null && 
                        !isAcademyMember() && !pk.isAcademyMember()) {
                        
                        if (_clan.isAtWarWith(pk.getClanId()) && 
                            pk.getClan().isAtWarWith(_clan.getClanId())) {
                            
                            if (getClan().getReputationScore() > 0)
                                pk.getClan().addReputationScore(1);
                            
                            if (pk.getClan().getReputationScore() > 0)
                                _clan.takeReputationScore(1);
                        }
                    }
                }
                
                // 9. Apply death penalty (XP loss)
                if (Config.ALLOW_DELEVEL && 
                    (!hasSkill(L2Skill.SKILL_LUCKY) || getStatus().getLevel() > 9))
                    applyDeathPenalty(
                        pk != null && getClan() != null && pk.getClan() != null && 
                        (getClan().isAtWarWith(pk.getClanId()) || pk.getClan().isAtWarWith(getClanId())), 
                        pk != null
                    );
            }
        }
    }
    
    // 10. Stop cubics
    _cubicList.stopCubics(false);
    
    // 11. Stop fusion skills
    if (getFusionSkill() != null)
        getCast().stop();
    
    forEachKnownType(Creature.class, 
        creature -> creature.getFusionSkill() != null && 
                   creature.getFusionSkill().getTarget() == this, 
        creature -> creature.getCast().stop());
    
    // 12. Calculate death penalty buff
    calculateDeathPenaltyBuffLevel(killer);
    
    // 13. Remove from water task
    WaterTaskManager.getInstance().remove(this);
    
    // 14. Phoenix blessing auto-revive
    if (isPhoenixBlessed())
        reviveRequest(this, null, false);
    
    // 15. Update effect icons
    updateEffectIcons();
    
    return true;
}
```

#### Apply Death Penalty (XP Loss)

```java
public void applyDeathPenalty(boolean atWar, boolean killedByPlayable) {
    // 1. Check if in PvP zone
    if (isInsideZone(ZoneId.PVP)) {
        // No XP loss in siege if Charm of Courage is active
        if (isInsideZone(ZoneId.SIEGE)) {
            if (isAffected(EffectFlag.CHARM_OF_COURAGE)) {
                stopEffects(EffectType.CHARM_OF_COURAGE);
                return;
            }
        }
        // No XP loss in arenas if killed by playable
        else if (killedByPlayable)
            return;
    }
    
    // 2. Get level
    final int lvl = getStatus().getLevel();
    
    // 3. Calculate percentage lost
    double percentLost = PlayerLevelData.getInstance().getPlayerLevel(lvl).expLossAtDeath();
    
    if (getKarma() > 0)
        percentLost *= Config.RATE_KARMA_EXP_LOST;
    
    if (isFestivalParticipant() || atWar || isInsideZone(ZoneId.SIEGE))
        percentLost /= 4.0;
    
    // 4. Calculate XP loss
    long lostExp = 0;
    
    final int maxLevel = PlayerLevelData.getInstance().getMaxLevel();
    if (lvl < maxLevel)
        lostExp = Math.round(
            (getStatus().getExpForLevel(lvl + 1) - getStatus().getExpForLevel(lvl)) * 
            percentLost / 100
        );
    else
        lostExp = Math.round(
            (getStatus().getExpForLevel(maxLevel) - getStatus().getExpForLevel(maxLevel - 1)) * 
            percentLost / 100
        );
    
    // 5. Save exp before death
    setExpBeforeDeath(getStatus().getExp());
    
    // 6. Update karma loss
    updateKarmaLoss(lostExp);
    
    // 7. Apply XP loss
    getStatus().addExp(-lostExp);
}
```

#### Revival (doRevive)

```java
@Override
public void doRevive() {
    super.doRevive();  // Parent revive logic
    
    // Remove Charm of Courage effect
    stopEffects(EffectType.CHARM_OF_COURAGE);
    sendPacket(new EtcStatusUpdate(this));
    
    // Clear revive request state
    _reviveRequested = 0;
    _revivePower = 0;
    
    // Restart mount feeding if mounted
    if (isMounted())
        startFeed(_mountNpcId);
}

@Override
public void doRevive(double revivePower) {
    // Restore exp based on revive power percentage
    restoreExp(revivePower);
    doRevive();
}
```

#### Revive Request

```java
public void reviveRequest(Player reviver, L2Skill skill, boolean isPet) {
    // 1. Check if already requested
    if (_reviveRequested == 1) {
        if (_revivePet == isPet)
            reviver.sendPacket(SystemMessageId.RES_HAS_ALREADY_BEEN_PROPOSED);
        else {
            if (isPet)
                reviver.sendPacket(SystemMessageId.CANNOT_RES_PET2);
            else
                reviver.sendPacket(SystemMessageId.MASTER_CANNOT_RES);
        }
        return;
    }
    
    // 2. Handle pet revival
    if (isPet) {
        if (_summon != null && _summon.isDead()) {
            _reviveRequested = 1;
            _revivePower = (_summon.isPhoenixBlessed()) ? 100. : 
                           Formulas.calcRevivePower(reviver, skill.getPower());
            _revivePet = isPet;
            
            sendPacket(new ConfirmDlg(SystemMessageId.RESSURECTION_REQUEST_BY_S1)
                .addCharName(reviver));
        }
    }
    // 3. Handle player revival
    else {
        if (isDead()) {
            _reviveRequested = 1;
            _revivePower = (isPhoenixBlessed()) ? 100. : 
                           Formulas.calcRevivePower(reviver, skill.getPower());
            _revivePet = isPet;
            
            sendPacket(new ConfirmDlg(SystemMessageId.RESSURECTION_REQUEST_BY_S1)
                .addCharName(reviver));
        }
    }
}
```

#### Revive Answer

```java
public void reviveAnswer(int answer) {
    // 1. Validate request state
    if (_reviveRequested != 1 || 
        (!isDead() && !_revivePet) || 
        (_revivePet && _summon != null && !_summon.isDead()))
        return;
    
    // 2. Decline
    if (answer == 0 && isPhoenixBlessed())
        stopPhoenixBlessing(null);
    
    // 3. Accept
    else if (answer == 1) {
        // Revive player
        if (!_revivePet) {
            if (_revivePower != 0)
                doRevive(_revivePower);
            else
                doRevive();
        }
        // Revive pet
        else if (_summon != null) {
            if (_revivePower != 0)
                _summon.doRevive(_revivePower);
            else
                _summon.doRevive();
        }
    }
    
    // 4. Clear state
    _reviveRequested = 0;
    _revivePower = 0;
}
```

---

## ðŸ“¦ COMMON WORKFLOWS

I'll create a separate section for common workflows in the next response due to length. Would you like me to continue with:

1. **Common Workflows** (Login, Teleport, Item Usage, Trading, etc.)
2. **Threading & Safety**
3. **Performance Tips**
4. **Extension Points**

Let me know and I'll continue the deep dive! ðŸš€