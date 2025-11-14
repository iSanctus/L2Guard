# L2JACIS SOURCE CODE - COMPREHENSIVE ANALYSIS & REFERENCE GUIDE

**Project:** Lineage 2 Java ACis Private Server  
**Base Path:** `C:\Users\AAAAAAAAAAAAAAAAAAAA\Desktop\deus\DEUS ACIS`  
**Version:** aCis (Custom Build)  
**Last Updated:** November 12, 2025

---

## 📋 TABLE OF CONTENTS

1. [Project Architecture Overview](#project-architecture-overview)
2. [Directory Structure](#directory-structure)
3. [Core Java Packages](#core-java-packages)
4. [Configuration System](#configuration-system)
5. [Database Layer](#database-layer)
6. [Network & Protocol](#network--protocol)
7. [Game Entities & Models](#game-entities--models)
8. [Handler System](#handler-system)
9. [Data Management](#data-management)
10. [Scripting & Quests](#scripting--quests)
11. [AI & NPC System](#ai--npc-system)
12. [Skills & Combat](#skills--combat)
13. [Extension Points](#extension-points)
14. [Key Files Quick Reference](#key-files-quick-reference)
15. [Development Guidelines](#development-guidelines)

---

## 🏗️ PROJECT ARCHITECTURE OVERVIEW

### High-Level Architecture

```
L2JACIS Structure
│
├── Java Source Code (net.sf.l2j)
│   ├── Commons (shared utilities)
│   ├── Config (configuration management)
│   ├── GameServer (main game logic)
│   ├── LoginServer (authentication)
│   └── Account Manager & GS Registration
│
├── Game Data
│   ├── Config (properties files)
│   ├── Data (XML/HTML/Geodata)
│   └── Scripts (Python/Java)
│
└── Build & Deployment
    ├── Libraries (libs/)
    ├── Launcher
    └── Build System (build.xml)
```

### Core Design Patterns

- **Singleton Pattern**: Managers, Data loaders (e.g., `SkillTable`, `ItemData`)
- **Factory Pattern**: `IdFactory` for object ID generation
- **Handler Pattern**: Extensible command/action handling
- **Observer Pattern**: Event-driven systems (zones, AI)
- **Template Pattern**: Quest system, NPC AI
- **Strategy Pattern**: Skill effects, target selection
- **Pool Pattern**: Thread pools, connection pools

---

## 📁 DIRECTORY STRUCTURE

### Root Structure

```
DEUS ACIS/
├── .classpath              # Eclipse classpath configuration
├── .gitattributes         # Git attributes
├── .gitignore            # Git ignore rules
├── .project              # Eclipse project file
├── .settings/            # IDE settings
├── build.xml             # Ant build script
├── game/                 # Game server runtime files
├── java/                 # Java source code
├── laucher/             # Launcher application
├── libs/                # External libraries
├── LICENSE              # License file
├── login/               # Login server runtime files
├── Mount.xml            # Mount configuration
└── tools/               # Development tools
```

### Java Package Structure

```
java/net/sf/l2j/
├── accountmanager/          # Account management CLI
├── commons/                 # Shared utilities & libraries
│   ├── config/             # Configuration utilities
│   ├── crypt/              # Encryption (Blowfish, BCrypt)
│   ├── data/               # Data structures (StatSet, MemoSet, Pagination)
│   ├── geometry/           # Geometric calculations (Circle, Rectangle, Polygon, etc.)
│   ├── logging/            # Logging framework
│   ├── math/               # Math utilities
│   ├── mmocore/            # MMO networking core
│   ├── network/            # Network utilities
│   ├── pool/               # Connection & thread pools
│   ├── random/             # Random number generation (Rnd)
│   └── util/               # General utilities
│
├── config/                  # Configuration system
│   └── HwidProtectionConfig.java
│
├── Config.java             # Main configuration class
│
├── gameserver/             # Game server core (MAIN PACKAGE)
└── loginserver/            # Login server
```

---

## 🎮 CORE JAVA PACKAGES

### 1. COMMONS PACKAGE (`net.sf.l2j.commons`)

**Purpose**: Shared utilities used across both game and login servers.

#### Key Components:

**`commons/pool/`**
- `ConnectionPool.java` - Database connection pooling
- `ThreadPool.java` - Thread management and scheduling

**`commons/crypt/`**
- `BCrypt.java` - Password hashing
- Blowfish encryption (in mmocore)

**`commons/geometry/`**
- `Circle.java`, `Rectangle.java`, `Polygon.java`, `Triangle.java`
- `Cube.java`, `Cuboid.java`, `Cylinder.java`, `Sphere.java`
- Used for zone shapes, range calculations, collision detection

**`commons/data/`**
- `StatSet.java` - Key-value data storage
- `MemoSet.java` - Memory-based data storage
- `Pagination.java` - List pagination helper

**`commons/random/`**
- `Rnd.java` - Cryptographically secure random number generation

---

### 2. GAMESERVER PACKAGE (`net.sf.l2j.gameserver`)

**Purpose**: Core game logic, world management, and gameplay systems.

#### 🌍 World Management

**`model/World.java`** - Central world container
- Manages all game objects
- Region-based spatial partitioning
- Object tracking and lookups

**`model/WorldObject.java`** - Base class for all world entities
- Position management
- Visibility system
- Knowledge list (nearby objects)

**`model/WorldRegion.java`** - Spatial region (2048x2048 units)
- Object registration
- Active/inactive region management
- Neighbor region references

#### 👤 Actor System (`model/actor/`)

**Hierarchy:**
```
WorldObject (abstract)
│
├── Creature (abstract) - Living entities with stats
│   ├── Playable (abstract) - Player-controlled
│   │   ├── Player - Human players
│   │   └── Summon - Player summons
│   │       ├── Pet - Pets
│   │       └── Servitor - Summoned servants
│   │
│   ├── Npc - Non-player characters
│   │   ├── Attackable - Aggressive NPCs
│   │   │   ├── Monster
│   │   │   ├── RaidBoss
│   │   │   ├── GrandBoss
│   │   │   └── Guard
│   │   │
│   │   └── Folk - Merchants, trainers, etc.
│   │
│   └── StaticObject
│
├── Item - Dropped items
├── Boat - Ships
└── Door - Doors
```

**Key Actor Files:**
- `model/actor/Player.java` - **CRITICAL** ~15,000+ lines, player character implementation
- `model/actor/Creature.java` - Base creature with HP/MP/stats
- `model/actor/Npc.java` - NPC base class
- `model/actor/Attackable.java` - Attackable monster base

#### 🎯 AI System (`model/actor/ai/`)

**Structure:**
```
ai/
├── Desire.java             # AI desire/intention
├── DesireQueue.java        # Desire management queue
├── Intention.java          # AI intentions (IDLE, ATTACK, FOLLOW, etc.)
│
└── type/                   # AI implementations
    ├── AbstractAI.java     # Base AI class
    ├── CreatureAI.java     # Generic creature AI
    ├── AttackableAI.java   # Monster AI with aggro
    ├── NpcAI.java          # Basic NPC AI
    ├── PlayerAI.java       # Player AI
    ├── PlayableAI.java     # Playable entity AI
    ├── SummonAI.java       # Summon AI
    ├── BoatAI.java         # Boat AI
    └── DoorAI.java         # Door AI
```

**AI Intentions:**
- `IDLE` - Doing nothing
- `ACTIVE` - Active but not in combat
- `ATTACK` - Attacking target
- `CAST` - Casting skill
- `MOVE_TO` - Moving to location
- `FOLLOW` - Following target
- `PICK_UP` - Picking up item
- `INTERACT` - Interacting with object

#### ⚔️ Combat System

**`model/actor/attack/`**
- `CreatureAttack.java` - Base attack logic
- `PlayerAttack.java` - Player-specific attack
- `PlayableAttack.java` - Playable attack
- `AttackableAttack.java` - Monster attack

**`model/actor/cast/`**
- `CreatureCast.java` - Base casting logic
- `PlayerCast.java` - Player spell casting
- `PlayableCast.java` - Playable casting
- `NpcCast.java` - NPC spell casting

**`model/actor/move/`**
- `CreatureMove.java` - Base movement
- `PlayerMove.java` - Player movement
- `BoatMove.java` - Boat movement
- `SummonMove.java` - Summon movement

#### 🎒 Container System (`model/actor/container/`)

**Player Containers:**
- `player/Appearance.java` - Visual appearance
- `player/HennaList.java` - Henna/tattoos
- `player/MacroList.java` - Macros
- `player/QuestList.java` - Quest tracking
- `player/RadarList.java` - Radar markers
- `player/RecipeBook.java` - Crafting recipes
- `player/ShortcutList.java` - Shortcuts
- `player/SubClass.java` - Subclass data
- `player/CubicList.java` - Cubic management
- `player/Request.java` - Request handling (trade, party, etc.)

**Creature Containers:**
- `creature/EffectList.java` - Active effects/buffs
- `creature/ChanceSkillList.java` - Triggered skills
- `creature/FusionSkill.java` - Fusion skills

**Attackable Containers:**
- `attackable/AggroList.java` - Aggro management
- `attackable/HateList.java` - Hate tracking

**Monster Containers:**
- `monster/OverhitState.java` - Overhit damage tracking
- `monster/SeedState.java` - Seed state for harvesting
- `monster/SpoilState.java` - Spoil state

---

### 3. NPC INSTANCES (`model/actor/instance/`)

**180+ NPC Types!** Here are the key categories:

**Boss NPCs:**
- `GrandBoss.java` - Grand boss base
- `RaidBoss.java` - Raid boss base

**Service NPCs:**
- `Folk.java` - Generic service NPC
- `Merchant.java` - Shop keeper
- `Warehouse Keeper.java` - Storage NPC
- `Gatekeeper.java` - Teleporter
- `Fisherman.java` - Fishing merchant
- `Trainer.java` - Skill trainer
- `ClassMaster.java` - Class change NPC

**Village Masters (Class Change):**
- `VillageMaster.java` - Base class changer
- `VillageMasterFighter.java`
- `VillageMasterMystic.java`
- `VillageMasterPriest.java`
- `VillageMasterDwarf.java`
- `VillageMasterOrc.java`
- `VillageMasterDElf.java`

**Combat NPCs:**
- `Monster.java` - Basic monster
- `Guard.java` - Town guard
- `SiegeGuard.java` - Castle guard
- `SiegeNpc.java` - Siege NPC base

**Special NPCs:**
- `Pet.java` - Player pet
- `Servitor.java` - Summoned creature
- `BabyPet.java` - Baby pet
- `TamedBeast.java` - Tamed beast
- `Cubic.java` - Cubic summon
- `Door.java` - Interactive door
- `Fence.java` - Fence object
- `StaticObject.java` - Static world object
- `Chest.java` - Treasure chest
- `FeedableBeast.java` - Beast that can be fed

**Siege NPCs:**
- `SiegeFlag.java` - Siege flag
- `FlameTower.java` - Flame tower
- `LifeTower.java` - Life control tower
- `HolyThing.java` - Castle artifact

**Event NPCs:**
- `ChristmasTree.java` - Holiday NPC
- `FestivalMonster.java` - Festival event monster
- `FestivalGuide.java` - Festival NPC

**Seven Signs NPCs:**
- `DawnPriest.java` - Dawn priest
- `DuskPriest.java` - Dusk priest
- `SignsPriest.java` - Seven Signs priest

**Castle NPCs:**
- `CastleChamberlain.java` - Castle manager
- `CastleBlacksmith.java` - Castle blacksmith
- `CastleMagician.java` - Castle buffer
- `CastleWarehouseKeeper.java` - Castle warehouse
- `CastleGatekeeper.java` - Castle teleporter
- `CastleDoorman.java` - Castle door manager

**Clan Hall NPCs:**
- `ClanHallManagerNpc.java` - Clan hall manager
- `ClanHallDoorman.java` - Clan hall door

**Special Instances:**
- `Adventurer.java` - Adventurer's Guild
- `SchemeBuffer.java` - Buff scheme NPC
- `PlayerBuffer.java` - Custom buffer
- `SymbolMaker.java` - Symbol creator
- `OlympiadManagerNpc.java` - Olympiad manager
- `DerbyTrackManagerNpc.java` - Monster race manager
- `ManorManagerNpc.java` - Manor system NPC
- `MercenaryManagerNpc.java` - Mercenary manager
- `WyvernManagerNpc.java` - Wyvern manager
- `WeddingManagerNpc.java` - Wedding NPC

---

## 🗄️ DATA MANAGEMENT (`gameserver/data/`)

### Data Loaders

**`data/xml/`** - XML-based data loaders (Singletons)

**Core Data:**
- `ItemData.java` - All item templates
- `NpcData.java` - All NPC templates
- `PlayerData.java` - Player templates per race
- `PlayerLevelData.java` - Level-up data
- `SkillTreeData.java` - Skill learning trees
- `AdminData.java` - Admin permissions
- `AnnouncementData.java` - Server announcements

**Equipment:**
- `ArmorSetData.java` - Armor set bonuses
- `HennaData.java` - Henna/tattoo data
- `AugmentationData.java` - Augmentation system
- `SoulCrystalData.java` - Soul crystal leveling

**World Data:**
- `DoorData.java` - All doors
- `BoatData.java` - Boat routes
- `StaticObjectData.java` - Static objects
- `FishData.java` - Fishing data
- `RecipeData.java` - Crafting recipes
- `TeleportData.java` - Teleport locations
- `RestartPointData.java` - Restart locations
- `InstantTeleportData.java` - Instant teleports

**NPCs & Spawns:**
- `SpellbookData.java` - Spellbooks
- `SummonItemData.java` - Summoning items
- `NewbieBuffData.java` - Newbie buffs
- `ObserverGroupData.java` - Observer groups
- `ManorAreaData.java` - Manor system
- `MultisellData.java` - Multisell shops
- `WalkerRouteData.java` - NPC walker routes
- `HealSpsData.java` - Heal/SP data
- `IconTable.java` - Skill icons
- `ClanHallDecoData.java` - Clan hall decorations

### Data Managers

**`data/manager/`** - Runtime managers (Singletons)

**Core Managers:**
- `BuyListManager.java` - Shop management
- `BufferManager.java` - Buffer scheme management
- `CastleManager.java` - Castle management
- `CastleManorManager.java` - Manor system
- `ClanHallManager.java` - Clan hall management
- `SpawnManager.java` - Spawn management
- `ZoneManager.java` - Zone management

**Event Managers:**
- `CursedWeaponManager.java` - Cursed weapons
- `DuelManager.java` - Duel system
- `FestivalOfDarknessManager.java` - Seven Signs festival
- `SevenSignsManager.java` - Seven Signs system
- `HeroManager.java` - Hero system

**Special Managers:**
- `CoupleManager.java` - Wedding system
- `DerbyTrackManager.java` - Monster races
- `FishingChampionshipManager.java` - Fishing contests
- `LotteryManager.java` - Lottery system
- `PartyMatchRoomManager.java` - Party matching
- `PetitionManager.java` - GM petitions
- `RaidPointManager.java` - Raid boss points
- `RelationManager.java` - Player relations
- `FenceManager.java` - Fence management

### Data Cache

**`data/cache/`**
- `HtmCache.java` - HTML template caching
- `CrestCache.java` - Clan/ally crest caching

---

## 🌐 NETWORK & PROTOCOL (`gameserver/network/`)

### Network Architecture

```
network/
├── GameClient.java              # Client connection state
├── GameCrypt.java               # Blowfish encryption
├── GamePacketHandler.java       # Packet routing
├── BlowFishKeygen.java         # Key generation
├── SessionKey.java             # Session keys
├── SystemMessageId.java        # System message IDs (5000+)
├── NpcStringId.java            # NPC string IDs
│
├── clientpackets/              # Client → Server (250+ packets)
│   ├── L2GameClientPacket.java # Base client packet
│   ├── AuthLogin.java          # Login authentication
│   ├── EnterWorld.java         # Enter game world
│   ├── MoveBackwardToLocation.java
│   ├── AttackRequest.java
│   ├── RequestMagicSkillUse.java
│   ├── Say2.java               # Chat messages
│   ├── UseItem.java
│   ├── RequestBypassToServer.java
│   └── [250+ more packets...]
│
├── serverpackets/              # Server → Client (400+ packets)
│   ├── L2GameServerPacket.java # Base server packet
│   ├── UserInfo.java           # Player info
│   ├── CharInfo.java           # Character info
│   ├── NpcInfo.java            # NPC info
│   ├── StatusUpdate.java       # HP/MP/CP updates
│   ├── Attack.java             # Attack animation
│   ├── MagicSkillUse.java     # Skill cast animation
│   ├── SystemMessage.java      # System messages
│   ├── CreatureSay.java        # Chat messages
│   ├── InventoryUpdate.java    # Inventory changes
│   ├── DeleteObject.java       # Remove object
│   ├── SpawnItem.java          # Spawn ground item
│   ├── Die.java                # Death
│   ├── Revive.java             # Revival
│   └── [400+ more packets...]
│
├── gameserverpackets/          # GameServer → LoginServer
│   ├── AuthRequest.java
│   ├── PlayerAuthRequest.java
│   ├── PlayerInGame.java
│   ├── PlayerLogout.java
│   └── ServerStatus.java
│
└── loginserverpackets/         # LoginServer → GameServer
    ├── AuthResponse.java
    ├── InitLS.java
    ├── KickPlayer.java
    ├── LoginServerFail.java
    └── PlayerAuthResponse.java
```

### Key Network Classes

**GameClient.java** - Represents connected client
- Login state management
- Account info
- Active character
- Session key
- Flood protection
- Packet queue

**Packet Structure:**
```java
// Client packet example
public final class RequestMagicSkillUse extends L2GameClientPacket {
    private int _skillId;
    private boolean _ctrlPressed;
    private boolean _shiftPressed;
    
    @Override
    protected void readImpl() {
        _skillId = readD();          // Read 4-byte integer
        _ctrlPressed = readD() != 0; // Read boolean as int
        _shiftPressed = readC() != 0;// Read boolean as byte
    }
    
    @Override
    protected void runImpl() {
        final Player player = getClient().getPlayer();
        if (player == null)
            return;
        // Process skill use...
    }
}

// Server packet example
public class MagicSkillUse extends L2GameServerPacket {
    private final int _objectId;
    private final int _targetId;
    private final int _skillId;
    private final int _skillLevel;
    
    @Override
    protected void writeImpl() {
        writeC(0x48); // Packet opcode
        writeD(_objectId);
        writeD(_targetId);
        writeD(_skillId);
        writeD(_skillLevel);
        writeD(0); // Hit time
        writeD(0); // Cooldown
    }
}
```

---

## ⚙️ HANDLER SYSTEM (`gameserver/handler/`)

### Handler Architecture

The handler system provides extensibility for commands, skills, items, and more.

```
handler/
├── AdminCommandHandler.java         # Admin command registry
├── ChatHandler.java                 # Chat command registry
├── ItemHandler.java                 # Item use registry
├── SkillHandler.java                # Skill type registry
├── TargetHandler.java               # Target type registry
├── UserCommandHandler.java          # User command registry (/loc, /time, etc.)
├── VoicedCommandHandler.java        # Voiced command registry (.)
│
├── admincommandhandlers/            # 30+ admin commands
│   ├── AdminAdmin.java              # //admin
│   ├── AdminTeleport.java           # //teleport, //recall
│   ├── AdminSpawn.java              # //spawn
│   ├── AdminItem.java               # //give
│   ├── AdminEditChar.java           # //setclass, //setlevel
│   ├── AdminSkill.java              # //skill_list, //add_skill
│   ├── AdminEnchant.java            # //seteh, //setew
│   ├── AdminPunish.java             # //ban, //jail
│   ├── AdminManage.java             # //server_shutdown
│   └── [30+ more commands...]
│
├── chathandlers/                    # Chat types
│   ├── ChatAll.java                 # Normal chat
│   ├── ChatShout.java               # Shout (!)
│   ├── ChatTell.java                # Private message (")
│   ├── ChatParty.java               # Party chat (#)
│   ├── ChatClan.java                # Clan chat (@)
│   ├── ChatAlliance.java            # Alliance chat ($)
│   ├── ChatTrade.java               # Trade chat (+)
│   └── ChatHeroVoice.java           # Hero chat (%)
│
├── itemhandlers/                    # Item use handlers
│   ├── SoulShots.java               # Soulshots
│   ├── SpiritShots.java             # Spiritshots
│   ├── BlessedSpiritShots.java      # BSS
│   ├── ScrollsOfResurrection.java   # Scrolls of Resurrection
│   ├── EnchantScrolls.java          # Enchant scrolls
│   ├── Books.java                   # Spellbooks
│   ├── Harvesters.java              # Harvester
│   ├── Seeds.java                   # Seed planting
│   ├── SoulCrystals.java            # Soul crystal leveling
│   ├── Keys.java                    # Key usage
│   ├── Maps.java                    # Treasure maps
│   ├── ItemSkills.java              # Items with skills
│   └── SummonItems.java             # Summon items
│
├── skillhandlers/                   # Skill type handlers
│   ├── Pdam.java                    # Physical damage
│   ├── Mdam.java                    # Magical damage
│   ├── Blow.java                    # Dagger skills
│   ├── Heal.java                    # Healing
│   ├── ManaHeal.java                # MP heal
│   ├── CombatPointHeal.java         # CP heal
│   ├── Resurrect.java               # Resurrection
│   ├── Continuous.java              # Buffs/debuffs
│   ├── Cancel.java                  # Cancel buffs
│   ├── Disablers.java               # Stun/root/paralyze/sleep
│   ├── Spoil.java                   # Spoil
│   ├── Sweep.java                   # Sweeper
│   ├── Harvest.java                 # Harvest
│   ├── Sow.java                     # Sow
│   └── [25+ more handlers...]
│
├── targethandlers/                  # Target type handlers
│   ├── TargetOne.java               # Single target
│   ├── TargetSelf.java              # Self target
│   ├── TargetArea.java              # Area around target
│   ├── TargetAura.java              # Area around self
│   ├── TargetParty.java             # Party members
│   ├── TargetClan.java              # Clan members
│   ├── TargetAlly.java              # Ally members
│   ├── TargetCorpsePlayer.java      # Dead player
│   └── [20+ more handlers...]
│
├── usercommandhandlers/             # User commands (/)
│   ├── Loc.java                     # /loc - Show location
│   ├── Time.java                    # /time - Show time
│   ├── OlympiadStat.java            # /olympiadstat
│   ├── PartyInfo.java               # /partyinfo
│   ├── ClanWarsList.java            # /clanwarslist
│   ├── SiegeStatus.java             # /siege
│   └── Mount.java / Dismount.java   # Mount controls
│
└── voicedcommandhandlers/           # Voiced commands (.)
    ├── VoicedMenu.java              # .menu
    ├── VoicedInfo.java              # .info
    ├── VoicedServer.java            # .server
    └── BuffManagerVCmd.java         # .buffshop
```

### Creating Custom Handlers

**Example: Custom Admin Command**
```java
public class AdminCustom implements IAdminCommandHandler {
    private static final String[] ADMIN_COMMANDS = {
        "admin_custom",
        "admin_customaction"
    };
    
    @Override
    public boolean useAdminCommand(String command, Player player) {
        if (command.equals("admin_custom")) {
            player.sendMessage("Custom command executed!");
            return true;
        }
        return false;
    }
    
    @Override
    public String[] getAdminCommandList() {
        return ADMIN_COMMANDS;
    }
}
```

**Register in `AdminCommandHandler.java`:**
```java
public AdminCommandHandler() {
    registerHandler(new AdminCustom());
    // ... other handlers
}
```

---

## 🎯 SKILLS & COMBAT SYSTEM

### Skill System (`gameserver/skills/`)

**Core Classes:**
```
skills/
├── L2Skill.java                    # Base skill class
├── Formulas.java                   # Combat calculations
├── Calculator.java                 # Stat calculations
├── AbstractEffect.java             # Base effect class
├── ChanceCondition.java            # Chance conditions
│
├── l2skills/                       # Skill types
│   ├── L2SkillDefault.java         # Default skill
│   ├── L2SkillChargeDmg.java       # Charge skills
│   ├── L2SkillDrain.java           # Drain skills
│   ├── L2SkillElemental.java       # Elemental skills
│   ├── L2SkillSeed.java            # Seed skills
│   ├── L2SkillSignet.java          # Signet skills
│   ├── L2SkillSummon.java          # Summon skills
│   ├── L2SkillTeleport.java        # Teleport skills
│   └── L2SkillCreateItem.java      # Item creation
│
├── effects/                        # 70+ effect types
│   ├── EffectBuff.java             # Buff effect
│   ├── EffectDebuff.java           # Debuff effect
│   ├── EffectStun.java             # Stun
│   ├── EffectRoot.java             # Root
│   ├── EffectSleep.java            # Sleep
│   ├── EffectParalyze.java         # Paralyze
│   ├── EffectFear.java             # Fear
│   ├── EffectMute.java             # Silence magic
│   ├── EffectPhysicalMute.java     # Silence physical
│   ├── EffectDamOverTime.java      # DOT damage
│   ├── EffectHeal.java             # Heal effect
│   ├── EffectHealOverTime.java     # HOT heal
│   ├── EffectInvincible.java       # Invincibility
│   └── [70+ more effects...]
│
├── conditions/                     # Skill conditions
│   ├── Condition.java              # Base condition
│   ├── ConditionPlayerLevel.java   # Level requirement
│   ├── ConditionPlayerHp.java      # HP requirement
│   ├── ConditionPlayerMp.java      # MP requirement
│   ├── ConditionPlayerState.java   # State requirement
│   └── [30+ more conditions...]
│
├── basefuncs/                      # Stat functions
│   ├── Func.java                   # Base function
│   ├── FuncAdd.java                # Addition
│   ├── FuncMul.java                # Multiplication
│   ├── FuncSet.java                # Set value
│   └── [10+ more functions...]
│
└── funcs/                          # Specific stat funcs
    ├── FuncPAtkMod.java            # P.Atk modifier
    ├── FuncMAtkMod.java            # M.Atk modifier
    ├── FuncPDefMod.java            # P.Def modifier
    ├── FuncMDefMod.java            # M.Def modifier
    ├── FuncMaxHpMul.java           # Max HP multiplier
    └── [15+ more funcs...]
```

### Combat Formulas

**Key Calculations in `Formulas.java`:**
- Physical attack damage
- Magical attack damage
- Critical hit chance
- Critical damage
- Skill power calculation
- Attack/cast speed modifiers
- Accuracy/evasion
- Shield defense
- Reflect damage
- Backstab damage multiplier
- PvP damage modifiers

---

## 📜 SCRIPTING & QUESTS

### Quest System (`gameserver/scripting/`)

```
scripting/
├── Quest.java                      # Base quest class
├── QuestState.java                 # Player quest state
├── QuestTimer.java                 # Quest timers
├── ScheduledQuest.java             # Scheduled quests
│
└── quest/                          # 600+ quests!
    ├── Q001_LettersOfLove.java     # Quest ID 1
    ├── Q002_WhatWomenWant.java     # Quest ID 2
    │...
    ├── Q070-Q100_SagaOf*.java      # 3rd class quests
    ├── Q101-Q127_*.java            # Low level quests
    ├── Q151-Q171_*.java            # Mid level quests
    ├── Q211-Q235_*.java            # 2nd class quests
    ├── Q241-Q247_PossessorOf*.java # Soul crystal quests
    ├── Q257-Q277_*.java            # Repeatable quests
    ├── Q291-Q300_*.java            # Dwarven quests
    ├── Q303-Q386_*.java            # General quests
    ├── Q401-Q418_PathTo*.java      # 1st class quests
    ├── Q419-Q421_*Pet*.java        # Pet quests
    ├── Q422_RepentYourSins.java    # PK quest
    ├── Q426_QuestForFishingShot.java
    ├── Q431_WeddingMarch.java      # Wedding quest
    ├── Q432_BirthdayPartySong.java # Birthday quest
    ├── Q501-Q510_Clan*.java        # Clan quests
    ├── Q601-Q663_*.java            # High level quests
    ├── Q688_DefeatTheElrokianRaiders.java
    ├── SecondClassQuest.java       # 2nd class base
    └── ThirdClassQuest.java        # 3rd class base
```

### Quest Structure Example

```java
public class Q001_LettersOfLove extends Quest {
    // NPC IDs
    private static final int DARIN = 30048;
    private static final int ROXXY = 30006;
    
    // Item IDs
    private static final int LETTER = 1000;
    private static final int RING = 1001;
    
    public Q001_LettersOfLove() {
        super(1, "Letters of Love");
        
        setItemsIds(LETTER);
        
        addStartNpc(DARIN);
        addTalkId(DARIN, ROXXY);
    }
    
    @Override
    public String onAdvEvent(String event, Npc npc, Player player) {
        // Handle quest events
    }
    
    @Override
    public String onTalk(Npc npc, Player player) {
        // Handle NPC talk
    }
}
```

### Script AI System (`scripting/script/ai/`)

**Massive AI Library - 500+ AI Scripts!**

**Boss AI:**
- `boss/antharas/` - Antharas AI
- `boss/baium/` - Baium AI
- `boss/benom/` - Benom AI
- `boss/core/` - Core AI
- `boss/frintezza/` - Frintezza AI (13 AI files!)
- `boss/orfen/` - Orfen AI
- `boss/queenant/` - Queen Ant AI
- `boss/sailren/` - Sailren AI
- `boss/valakas/` - Valakas AI
- `boss/zaken/` - Zaken AI

**Monster AI Hierarchy:**
```
individual/Monster/
├── MonsterAI.java                  # Base monster AI
├── MonsterBehavior/                # Behavior patterns
│   ├── WarriorBehavior/            # Melee behavior
│   └── WizardBehavior/             # Caster behavior
│
├── WarriorBase/                    # Warrior monsters
│   ├── Warrior/                    # 100+ warrior AIs
│   │   ├── Warrior.java
│   │   ├── WarriorAggressive/      # Aggressive variants
│   │   ├── WarriorCasting*/        # Casting warriors
│   │   ├── WarriorPhysicalSpecial/ # Special attack warriors
│   │   └── [100+ more...]
│   │
│   └── Chests/                     # Treasure chests
│       ├── TreasureChest.java
│       └── TreasureChestMimic.java
│
├── WizardBase/                     # Wizard monsters
│   └── Wizard/                     # 50+ wizard AIs
│       ├── Wizard.java
│       ├── WizardDDMagic2/         # DD casters
│       ├── WizardCorpse*/          # Undead casters
│       ├── WizardHealer*/          # Healer mobs
│       └── [50+ more...]
│
├── RaidBoss/                       # Raid boss AI
│   ├── RaidBossStandard.java
│   ├── RaidBossAlone/              # Solo raid bosses
│   └── RaidBossParty/              # Group raid bosses
│
├── RaidPrivate/                    # Raid boss minions
│   ├── RaidPrivateFighter.java
│   ├── RaidPrivateArcher.java
│   ├── RaidPrivateWizard.java
│   └── RaidPrivateHealer.java
│
└── LV3Monster/                     # Level 3 monsters
    ├── LV3Knight.java              # Tank mobs
    ├── LV3Wizard.java              # Caster mobs
    ├── LV3Healer.java              # Healer mobs
    ├── LV3Ranger.java              # Archer mobs
    └── [12+ more...]
```

**Group AI:**
- `group/FollowerMovingAroundMaster.java` - Follower behavior
- `group/RandomTeleport.java` - Random teleportation
- `group/Walkers.java` - NPC walkers

**Siege AI:**
- `siegeguards/GludioHold/` - Gludio guards (10+ types)
- `siegeguards/GludioStand/` - Standing guards
- `siegeguards/GludioWizard/` - Wizard guards
- `siegeguards/GludioCleric/` - Healer guards

**Siegeable Hall AI:**
- `siegablehall/BanditStronghold.java`
- `siegablehall/DevastatedCastle.java`
- `siegablehall/FortressOfResistance.java`
- `siegablehall/RainbowSpringsChateau.java`
- `siegablehall/WildBeastReserve.java`
- Plus 30+ special AI scripts for clan hall sieges

**Special Individual AI:**
- `individual/DefaultNpc.java` - Basic NPC
- `individual/Guard/` - Town guards (5 types)
- `individual/AgitWarrior/` - Clan hall guards
- `individual/Corpse.java` - Corpse behavior
- `individual/ImperialGravekeeper.java`
- `individual/SacrificialVictim.java`
- `individual/RoyalRush*/` - Royal Rush event (20+ AIs)

### Spawn Makers (`scripting/script/maker/`)

**70+ Spawn Makers** - Control how/when NPCs spawn:
- `DefaultMaker.java` - Standard spawn
- `DefaultUseDBMaker.java` - DB-driven spawn
- `RandomSpawnMaker.java` - Random spawn
- `InstantSpawnMaker.java` - Instant spawn
- `OnDayNightSpawnMaker.java` - Day/night spawn
- `EventMaker.java` - Event spawn
- `FarmMaker.java` - Farm spawn
- `ParentSpawnAllMaker.java` - Parent-child spawn
- `UniqueNpcKillEventMaker.java` - Spawn on kill
- Plus 60+ specialized makers

### Feature Scripts (`scripting/script/feature/`)

**Core Features:**
- `Alliance.java` - Alliance system
- `Clan.java` - Clan system
- `FirstClassChange.java` - 1st class change
- `SecondClassChange.java` - 2nd class change
- `Tutorial.java` - Tutorial system
- `NewbieHelper.java` - Newbie assistance
- `HeroWeapon.java` - Hero weapons
- `HeroCirclet.java` - Hero circlets
- `ShadowWeapon.java` - Shadow weapons
- `EchoCrystal.java` - Echo crystals
- `KetraOrcSupport.java` - Ketra buffs
- `VarkaSilenosSupport.java` - Varka buffs
- `CabalBuffer.java` - Seven Signs buffs
- `BlackJudge.java` - Karma cleanse
- `MissQueen.java` - Beauty contest
- `RaidbossInfo.java` - RB info

### Teleport Scripts (`scripting/script/teleport/`)
- `GrandBossTeleporter.java` - Grand boss entry
- `NoblesseTeleporter.java` - Noblesse teleports
- `HuntingGroundsTeleporter.java` - Hunting area ports
- `OracleTeleporter.java` - Oracle teleports
- `PaganTeleporter.java` - Pagan Temple
- `PrimevalSummoner.java` - Primeval Isle
- `DimensionalVortex.java` - Dimensional rift
- Plus 5+ more teleporters

---

## 🗺️ ZONE SYSTEM (`gameserver/model/zone/`)

### Zone Types

```
zone/
├── ZoneForm.java                   # Zone shape base
│
├── form/                           # Zone shapes
│   ├── ZoneCuboid.java             # Box shape
│   ├── ZoneCylinder.java           # Cylinder shape
│   └── ZoneNPoly.java              # Polygon shape
│
└── type/                           # 25+ zone types
    ├── ArenaZone.java              # PvP arena
    ├── BossZone.java               # Boss zone
    ├── CastleZone.java             # Castle area
    ├── CastleTeleportZone.java     # Castle teleport
    ├── ClanHallZone.java           # Clan hall
    ├── DamageZone.java             # Damage zone
    ├── DerbyTrackZone.java         # Monster race
    ├── EffectZone.java             # Buff/debuff zone
    ├── FishingZone.java            # Fishing area
    ├── HqZone.java                 # HQ zone
    ├── JailZone.java               # Jail
    ├── MotherTreeZone.java         # Mother tree
    ├── NoLandingZone.java          # No wyvern landing
    ├── NoRestartZone.java          # Can't restart here
    ├── NoStoreZone.java            # Can't open store
    ├── NoSummonFriendZone.java     # Can't summon
    ├── OlympiadStadiumZone.java    # Olympiad arena
    ├── PeaceZone.java              # Safe zone
    ├── PrayerZone.java             # Prayer area
    ├── ScriptZone.java             # Scripted zone
    ├── SiegeZone.java              # Siege area
    ├── SwampZone.java              # Swamp (slow)
    ├── TownZone.java               # Town
    ├── WaterZone.java              # Water
    │
    └── subtype/                    # Zone subtypes
        ├── CastleZoneType.java
        ├── ResidenceZoneType.java
        ├── SpawnZoneType.java
        └── ZoneType.java
```

### Zone Events

Zones trigger events:
- `onEnter(Creature)` - Enter zone
- `onExit(Creature)` - Exit zone
- `onDieInside(Creature)` - Die in zone
- `onReviveInside(Creature)` - Revive in zone

---

## 📊 ENUMERATIONS (`gameserver/enums/`)

### Core Enums

**`actors/ClassId.java`** - All 139 classes
- Fighter classes
- Mystic classes
- All 3rd class transformations

**`actors/ClassRace.java`**
- HUMAN, ELF, DARK_ELF, ORC, DWARF

**`actors/ClassType.java`**
- FIGHTER, MYSTIC

**`actors/Sex.java`**
- MALE, FEMALE

**`IntentionType.java`** - AI intentions
- IDLE, ACTIVE, REST, ATTACK, CAST, MOVE_TO, FOLLOW, PICK_UP, INTERACT

**`actors/OperateType.java`** - Private store types
- NONE, BUY, SELL, PACKAGE_SELL, MANUFACTURE

**`MessageType.java`** - Chat types
- ALL, SHOUT, TELL, PARTY, CLAN, GM, PETITION_PLAYER, PETITION_GM, TRADE, ALLIANCE, ANNOUNCEMENT, BOAT, FRIEND, MSNCHAT, PARTY_ROOM, COMMANDER_CHANNEL, HERO_VOICE

**`skills/SkillType.java`** - 100+ skill types
- PDAM, MDAM, BUFF, DEBUFF, HEAL, MANAHEAL, RESURRECT, STUN, ROOT, SLEEP, etc.

**`skills/Stats.java`** - Character stats
- MAX_HP, MAX_MP, MAX_CP, POWER_ATTACK, MAGIC_ATTACK, POWER_DEFENCE, MAGIC_DEFENCE, ACCURACY, EVASION, CRITICAL_RATE, etc.

**`skills/AbnormalEffect.java`** - Visual effects
- NULL, BIG_HEAD, FLAME, BLEEDING1, BLEEDING2, POISON, FEAR, STUNNED, etc.

**`items/ArmorType.java`**
- NONE, LIGHT, HEAVY, MAGIC, SIGIL

**`items/WeaponType.java`**
- NONE, SWORD, BLUNT, DAGGER, BOW, POLE, FIST, DUAL, DUALFIST, FISHINGROD, RAPIER, ANCIENTSWORD, CROSSBOW, FLAG, OWNTHING

**`items/CrystalType.java`**
- NONE, D, C, B, A, S

**`items/EtcItemType.java`**
- ARROW, MATERIAL, PET_COLLAR, POTION, RECIPE, SCROLL, QUEST, MONEY, OTHER, SEED, SHOT, SPELLBOOK

**`ZoneId.java`** - Zone flags
- PVP, PEACE, SIEGE, MOTHER_TREE, CLAN_HALL, NO_LANDING, WATER, JAIL, MONSTER_TRACK, CASTLE, SWAMP, NO_SUMMON_FRIEND, SCRIPT, HQ, DANGER_AREA, NO_STORE, NO_RESTART

**`EventHandler.java`** - Event types for scripts

**`OlympiadType.java`**
- CLASSED, NON_CLASSED, TEAMS

**`OlympiadState.java`**
- BEGIN, GAME, VALIDATE, IDLE, WAITING

**`BossStatus.java`**
- ALIVE, DEAD, LOCKED

**`SiegeStatus.java`**
- NOT_ATTACKER, ATTACKER, DEFENDER, OWNER

**`CabalType.java`** - Seven Signs
- NORMAL, DUSK, DAWN

**`SealType.java`** - Seven Signs seals
- AVARICE, GNOSIS, STRIFE

**Plus 50+ more enums!**

---

## 🎮 OLYMPIAD SYSTEM (`gameserver/model/olympiad/`)

```
olympiad/
├── Olympiad.java                   # Main olympiad manager
├── OlympiadManager.java            # Match manager
├── OlympiadGameManager.java        # Game scheduler
├── OlympiadGameTask.java           # Game task
├── AbstractOlympiadGame.java       # Base game
├── OlympiadGameNormal.java         # Normal game
├── OlympiadGameClassed.java        # Classed match
├── OlympiadGameNonClassed.java     # Non-classed match
├── OlympiadNoble.java              # Noble data
└── Participant.java                # Match participant
```

**Features:**
- Weekly competition
- Classed and non-classed battles
- Team battles
- Ranking system
- Hero selection
- Noble points

---

## 🏰 SIEGE SYSTEM (`gameserver/model/residence/`)

```
residence/
├── Residence.java                  # Base residence
├── Siegable.java                   # Siegeable interface
│
├── castle/                         # Castle system
│   ├── Castle.java                 # Castle data
│   └── Siege.java                  # Siege logic
│
└── clanhall/                       # Clan hall system
    ├── ClanHall.java               # Clan hall data
    ├── SiegableHall.java           # Siegeable hall
    ├── ClanHallSiege.java          # Clan hall siege
    ├── ClanHallFunction.java       # Hall functions
    ├── Auction.java                # Auction system
    ├── Bidder.java                 # Bidder
    └── Seller.java                 # Seller
```

**Castle Features:**
- Tax system
- Door/artifact management
- Siege registration
- Mercenary system
- Siege guards
- Teleport system

**Clan Hall Features:**
- Auction system
- Decorations
- Buffers
- Teleporters
- Item creation

---

## 👥 SOCIAL SYSTEM

### Clans (`gameserver/model/pledge/`)

```
pledge/
├── Clan.java                       # Main clan class
├── ClanMember.java                 # Clan member
├── SubPledge.java                  # Sub-unit (Royal Guard, Order of Knights, etc.)
└── ClanInfo.java                   # Clan info packet data
```

**Clan Features:**
- Level 1-8 progression
- Sub-pledges (Royal Guard, Order of Knights)
- Clan skills
- Clan wars
- Clan penalties
- Clan reputation
- Clan warehouse

### Parties & Command Channels (`gameserver/model/group/`)

```
group/
├── AbstractGroup.java              # Base group
├── Party.java                      # Party (max 9)
├── CommandChannel.java             # CC (multiple parties)
└── PartyMatchRoom.java             # Party matching
```

**Party Features:**
- Up to 9 members
- Experience/drop distribution
- Party chat
- Party commands
- Party matching system

**Command Channel:**
- Multiple parties
- Max members configurable
- Clan leaders only
- Special commands

---

## 📦 ITEM SYSTEM

### Item Classes (`gameserver/model/item/`)

```
item/
├── ArmorSet.java                   # Armor set bonuses
├── DropCategory.java               # Drop category
├── DropData.java                   # Drop configuration
├── LifeStone.java                  # Life stone
├── MercenaryTicket.java            # Mercenary ticket
│
├── instance/                       # Item instances
│   ├── ItemInstance.java           # Actual item
│   └── ItemInfo.java               # Item info packet
│
└── kind/                           # Item templates
    ├── Item.java                   # Base item template
    ├── Weapon.java                 # Weapon template
    ├── Armor.java                  # Armor template
    └── EtcItem.java                # Etc item template
```

### Item Containers (`gameserver/model/itemcontainer/`)

```
itemcontainer/
├── ItemContainer.java              # Base container
├── Inventory.java                  # Base inventory
├── PcInventory.java                # Player inventory
├── PetInventory.java               # Pet inventory
├── PcWarehouse.java                # Personal warehouse
├── ClanWarehouse.java              # Clan warehouse
├── PcFreight.java                  # Freight
│
└── listeners/                      # Container listeners
    ├── ArmorSetListener.java       # Armor set bonuses
    ├── BowRodListener.java         # Bow/rod equip
    ├── ItemPassiveSkillsListener.java # Passive skills
    ├── StatsListener.java          # Stat changes
    └── OnEquipListener.java        # Equip callback
```

**Inventory System:**
- 80 slots (dwarf: 100)
- Paperdoll (equipment slots)
- Item listeners
- Weight management
- Enchant tracking
- Augmentation support

---

## ⚙️ CONFIGURATION SYSTEM

### Main Config File: `Config.java`

**Configuration Loading Order:**
1. `server.properties` - Server settings
2. `players.properties` - Player settings
3. `npcs.properties` - NPC settings
4. `clans.properties` - Clan settings
5. `events.properties` - Event settings
6. `geoengine.properties` - Geodata settings
7. `siege.properties` - Siege settings
8. `loginserver.properties` - Login server
9. `CustomMods/*.properties` - Custom mods
10. `hwid.properties` - HWID protection
11. `autofarm.properties` - Auto-farm (if present)

### Key Config Categories

**Server:**
- Hostname/IP
- Ports (gameserver: 7777, loginserver: 2106/9014)
- Database connection
- Thread pool settings
- Max players
- Auto-save intervals

**Rates:**
- XP/SP rates
- Drop rates (adena, items, spoil)
- Quest reward rates
- Craft rates

**Player:**
- Starting level/stats
- Inventory/warehouse slots
- Weight limits
- Enchant chances
- PvP settings
- Death penalty

**NPC:**
- Spawn multipliers
- Raid boss settings
- Grand boss timers
- AI configuration
- Champion mobs

**Clan:**
- Creation cost
- Level requirements
- Clan penalties
- Wars

**Olympiad:**
- Competition period
- Battle settings
- Reward configuration

**Siege:**
- Siege times
- Registration periods
- Siege rewards

---

## 🛠️ DEVELOPMENT QUICK REFERENCE

### Creating Custom Content

**1. Custom NPC**
```java
// File: java/net/sf/l2j/gameserver/model/actor/instance/CustomNpc.java
public class CustomNpc extends Folk {
    public CustomNpc(int objectId, NpcTemplate template) {
        super(objectId, template);
    }
    
    @Override
    public void onAction(Player player) {
        player.sendMessage("Hello from custom NPC!");
        showChatWindow(player);
    }
    
    @Override
    public void onBypassFeedback(Player player, String command) {
        if (command.equals("custom_action")) {
            // Custom action
        }
    }
}
```

**2. Custom Item Handler**
```java
// File: handler/itemhandlers/CustomItem.java
public class CustomItem implements IItemHandler {
    @Override
    public void useItem(Playable playable, ItemInstance item) {
        if (!(playable instanceof Player))
            return;
            
        Player player = (Player) playable;
        
        // Custom item logic here
        player.sendMessage("Custom item used!");
        
        // Consume item
        player.destroyItem("Consume", item, 1, null, false);
    }
    
    @Override
    public int[] getItemIds() {
        return new int[] { 12345 }; // Your custom item ID
    }
}
```

**3. Custom Admin Command**
```java
// File: handler/admincommandhandlers/AdminCustom.java
public class AdminCustom implements IAdminCommandHandler {
    private static final String[] ADMIN_COMMANDS = {
        "admin_mycustom"
    };
    
    @Override
    public boolean useAdminCommand(String command, Player player) {
        if (command.equals("admin_mycustom")) {
            player.sendMessage("Custom admin command!");
            return true;
        }
        return false;
    }
    
    @Override
    public String[] getAdminCommandList() {
        return ADMIN_COMMANDS;
    }
}
```

**4. Custom Quest**
```java
// File: scripting/quest/Q999_CustomQuest.java
public class Q999_CustomQuest extends Quest {
    private static final int NPC_ID = 30000;
    private static final int ITEM_ID = 57; // Adena
    private static final int REWARD_AMOUNT = 10000;
    
    public Q999_CustomQuest() {
        super(999, "Custom Quest Title");
        
        addStartNpc(NPC_ID);
        addTalkId(NPC_ID);
    }
    
    @Override
    public String onAdvEvent(String event, Npc npc, Player player) {
        QuestState st = player.getQuestState(getName());
        if (st == null)
            return null;
            
        if (event.equals("accept")) {
            st.setState(QuestStatus.STARTED);
            st.setCond(1);
            return "quest_accepted.htm";
        }
        return null;
    }
    
    @Override
    public String onTalk(Npc npc, Player player) {
        QuestState st = player.getQuestState(getName());
        if (st == null)
            return null;
            
        switch (st.getState()) {
            case CREATED:
                return "quest_start.htm";
            case STARTED:
                if (st.getCond() == 1) {
                    st.giveItems(ITEM_ID, REWARD_AMOUNT);
                    st.exitQuest(false);
                    return "quest_completed.htm";
                }
                break;
        }
        return null;
    }
}
```

---

## 🔍 KEY FILES QUICK REFERENCE

### Most Important Core Files

**ESSENTIAL (Top 10):**
1. `gameserver/GameServer.java` - Server initialization (~3000 lines)
2. `gameserver/model/actor/Player.java` - Player class (~15000 lines!)
3. `gameserver/model/actor/Creature.java` - Base creature (~5000 lines)
4. `gameserver/data/SkillTable.java` - All skills
5. `gameserver/data/xml/ItemData.java` - All items
6. `gameserver/data/xml/NpcData.java` - All NPCs
7. `gameserver/network/GamePacketHandler.java` - Packet routing
8. `gameserver/skills/Formulas.java` - Combat calculations
9. `Config.java` - Configuration system
10. `commons/pool/ThreadPool.java` - Thread management

**NETWORK (Top 5):**
1. `network/GameClient.java` - Client state
2. `network/GameCrypt.java` - Encryption
3. `network/clientpackets/EnterWorld.java` - Enter game
4. `network/serverpackets/UserInfo.java` - Player info
5. `network/serverpackets/CharInfo.java` - Character info

**HANDLERS (Top 5):**
1. `handler/AdminCommandHandler.java` - Admin commands
2. `handler/ItemHandler.java` - Item usage
3. `handler/SkillHandler.java` - Skill types
4. `handler/VoicedCommandHandler.java` - Voiced commands
5. `handler/UserCommandHandler.java` - User commands

**AI (Top 5):**
1. `model/actor/ai/type/CreatureAI.java` - Base AI
2. `model/actor/ai/type/AttackableAI.java` - Monster AI
3. `model/actor/ai/type/PlayerAI.java` - Player AI
4. `scripting/Quest.java` - Quest base
5. `scripting/script/ai/individual/Monster/MonsterAI.java` - Monster base

---

## 🎯 COMMON TASKS GUIDE

### Finding Things

**Find Item ID:**
- Check `game/data/xml/items/` XML files
- Search in database table `items`

**Find NPC ID:**
- Check `game/data/xml/npcs/` XML files
- Search in database table `npc`

**Find Skill ID:**
- Check `game/data/xml/skills/` XML files
- Search in database table `skills`

**Find Quest:**
- Check `java/net/sf/l2j/gameserver/scripting/quest/`
- Quest ID = filename number (Q001 = quest ID 1)

### Modifying Things

**Change Drop Rates:**
1. Edit `game/config/server.properties`
2. Find `RateDropItems` or similar
3. Change value, restart server

**Add Admin Command:**
1. Create class in `handler/admincommandhandlers/`
2. Implement `IAdminCommandHandler`
3. Register in `AdminCommandHandler.java` constructor
4. Add command to `game/data/xml/adminCommands.xml`

**Add Custom Item:**
1. Add item to `game/data/xml/items/` XML
2. Create handler in `handler/itemhandlers/` (if special use)
3. Register handler in `ItemHandler.java`
4. Insert into database `items` table

**Create Custom NPC:**
1. Add to `game/data/xml/npcs/` XML
2. Create AI in `scripting/script/ai/individual/`
3. Add spawn in `game/data/xml/spawnlist/`
4. Insert into database `npc` table

---

## 📚 ADDITIONAL SYSTEMS

### GeoEngine (`gameserver/geoengine/`)

**Pathfinding & Geodata:**
- `GeoEngine.java` - Main geodata engine
- `geodata/` - Block structures
- `pathfinding/PathFinder.java` - A* pathfinding
- `pathfinding/Node.java` - Pathfinding node

**Features:**
- Line of sight checks
- Movement validation
- Z-coordinate calculation
- Pathfinding

### Task Managers (`gameserver/taskmanager/`)

**Background Tasks:**
- `AiTaskManager.java` - AI tick (1000ms)
- `AttackStanceTaskManager.java` - Combat stance (15s)
- `DecayTaskManager.java` - Corpse decay (8.5s)
- `GameTimeTaskManager.java` - In-game time
- `ItemsOnGroundTaskManager.java` - Item cleanup
- `PvpFlagTaskManager.java` - PvP flag (90s)
- `ShadowItemTaskManager.java` - Shadow items
- `WaterTaskManager.java` - Water zone checks (250ms)
- `WalkerTaskManager.java` - NPC walkers (1000ms)
- `BoatTaskManager.java` - Boat movement (1000ms)

### Seven Signs (`gameserver/data/manager/SevenSignsManager.java`)

**Features:**
- Competition period (1 week)
- Seal validation
- Cabal (Dawn/Dusk) system
- Ancient Adena rewards
- Mammon merchants
- Festival of Darkness

### Manor System (`gameserver/data/manager/CastleManorManager.java`)

**Features:**
- Seed production
- Crop procurement
- Castle economy
- Alternative castle income

### Community Board (`gameserver/communitybbs/`)

**Managers:**
- `TopBBSManager.java` - Main page
- `RegionBBSManager.java` - Region board
- `ClanBBSManager.java` - Clan board
- `MailBBSManager.java` - Mail system
- `PostBBSManager.java` - Posts
- `FriendsBBSManager.java` - Friends
- `FavoriteBBSManager.java` - Favorites
- `RankingBBSManager.java` - Rankings

---

## 🎓 DEVELOPMENT BEST PRACTICES

### Code Style

1. **Naming Conventions:**
   - Classes: `PascalCase`
   - Methods: `camelCase`
   - Variables: `_camelCaseWithUnderscore` (private fields)
   - Constants: `UPPER_SNAKE_CASE`

2. **Package Structure:**
   - Keep related classes together
   - Use sub-packages for organization
   - Follow existing patterns

3. **Design Patterns:**
   - Use Singleton for managers
   - Use Factory for object creation
   - Use Template for quests/AI
   - Use Strategy for skill effects

### Performance Tips

1. **Object Pooling:**
   - Reuse ThreadPool for tasks
   - Use ConnectionPool for DB

2. **Caching:**
   - HtmCache for HTML
   - CrestCache for images
   - Data loaders cache templates

3. **Region System:**
   - Use for spatial queries
   - Efficient neighbor lookups
   - Active/inactive optimization

4. **Lazy Loading:**
   - Load player data on demand
   - Cache frequently used data

### Testing

1. **Test Character:**
   - Create test character
   - Use //setlevel, //setclass
   - Grant items with //give

2. **Debug Commands:**
   - //debug - Toggle debug
   - //gmspeed - GM speed
   - //invul - Invulnerability
   - //gmshop - GM shop

3. **Logging:**
   - Use LOGGER for debugging
   - Check `game/log/` directory
   - Enable verbose logging in config

---

## 🚀 QUICK START DEVELOPMENT

### Setup Development Environment

1. **Prerequisites:**
   - JDK 8+ (aCis uses Java 8)
   - Eclipse/IntelliJ IDE
   - MySQL/MariaDB
   - Git

2. **Import Project:**
   - Import as existing project
   - Configure build path
   - Add libs/*.jar to classpath

3. **Database:**
   - Run SQL scripts from `tools/sql/`
   - Configure `game/config/server.properties`
   - Update database credentials

4. **Build:**
   - Use Ant with `build.xml`
   - Or build in IDE
   - Output goes to compiled jars

5. **Run:**
   - Start `LoginServer.java`
   - Start `GameServer.java`
   - Connect with client

### Common Development Workflow

1. **Adding Feature:**
   - Identify required systems (handler, data, etc.)
   - Create necessary classes
   - Register with appropriate managers
   - Test thoroughly

2. **Modifying Existing:**
   - Find relevant files (use search)
   - Understand existing code
   - Make minimal changes
   - Test edge cases

3. **Debugging:**
   - Add logging statements
   - Use debug commands
   - Check error logs
   - Use breakpoints in IDE

---

## 🎯 SYSTEM INTERACTION MAP

```
Player
  ↓
GameClient (Network)
  ↓
GamePacketHandler
  ↓
L2GameClientPacket (e.g., RequestMagicSkillUse)
  ↓
Player.doCast(skill)
  ↓
PlayerCast.doAction()
  ↓
L2Skill.getEffects()
  ↓
EffectTemplate.getEffect()
  ↓
Effect.onStart()
  ↓
Formulas.calcSkillSuccess()
  ↓
Creature.reduceCurrentHp()
  ↓
StatusUpdate (Server Packet)
  ↓
GameClient.sendPacket()
  ↓
Player sees result
```

---

## 📊 STATISTICS

**Code Statistics:**
- **Total Java Files:** ~2,500+
- **Lines of Code:** ~500,000+
- **Quests:** 600+
- **AI Scripts:** 500+
- **Client Packets:** 250+
- **Server Packets:** 400+
- **Admin Commands:** 30+
- **Item Handlers:** 25+
- **Skill Handlers:** 35+
- **Effect Types:** 70+
- **NPC Types:** 180+
- **Zone Types:** 25+

**Data Files:**
- **Items:** 10,000+
- **NPCs:** 5,000+
- **Skills:** 2,000+
- **Spawns:** 50,000+

---

## 🎉 CONCLUSION

This L2JACIS source is a **massive, comprehensive Lineage 2 server emulation**. It implements virtually every game system from the original Lineage 2 C4/Interlude chronicles.

**Key Strengths:**
- Well-organized package structure
- Extensive handler system for easy extension
- Huge quest/AI script library
- Professional code quality
- Active community

**Development Focus Areas:**
- Network protocol (for client communication)
- Quest scripting (for custom content)
- AI scripting (for NPC behavior)
- Handler system (for commands/items/skills)
- Data management (for game content)

**Most Modified Files in Custom Servers:**
1. Quest scripts (new quests)
2. AI scripts (custom bosses)
3. Handler classes (custom commands)
4. Config.java (rates/settings)
5. Player.java (custom features)

---

**Happy Coding! 🎮**

*Document Version: 1.0*  
*Last Updated: November 12, 2025*  
*For: DEUS ACIS L2JACIS Source*
