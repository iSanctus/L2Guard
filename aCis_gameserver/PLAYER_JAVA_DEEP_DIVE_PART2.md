 PLAYER.JAVA DEEP DIVE - PART 2: WORKFLOWS & PRACTICAL GUIDE

**Continuation of Player.java Analysis**

---

## ðŸ“‹ TABLE OF CONTENTS (PART 2)

1. [Common Workflows](#common-workflows)
2. [Threading & Synchronization](#threading--synchronization)
3. [Network Integration](#network-integration)
4. [Item Management](#item-management)
5. [Combat System](#combat-system)
6. [Trading & Private Stores](#trading--private-stores)
7. [Teleportation System](#teleportation-system)
8. [Observer & Olympiad Modes](#observer--olympiad-modes)
9. [Performance Considerations](#performance-considerations)
10. [Common Pitfalls & Best Practices](#common-pitfalls--best-practices)
11. [Extension Examples](#extension-examples)

---

## ðŸ”„ COMMON WORKFLOWS

### Workflow 1: PLAYER LOGIN

```
Step-by-step flow from login to entering world:

1. CLIENT CONNECTS
   â””â”€> GameClient created
   â””â”€> Blowfish key exchange

2. AUTHENTICATION
   â””â”€> RequestAuthLogin packet
   â””â”€> Check with LoginServer
   â””â”€> Account validated

3. CHARACTER SELECTION
   â””â”€> CharSelectionInfo sent
   â””â”€> Player clicks character
   â””â”€> Player.restore(objectId) called
       â”œâ”€> Load from database
       â”œâ”€> Restore skills
       â”œâ”€> Restore inventory
       â”œâ”€> Restore quests
       â””â”€> Restore effects

4. ENTER WORLD
   â””â”€> EnterWorld packet
   â””â”€> player.onPlayerEnter() called
       â”œâ”€> Add to World
       â”œâ”€> Add to region
       â”œâ”€> Validate Seven Signs
       â”œâ”€> Handle jail punishment
       â”œâ”€> Revalidate zones
       â”œâ”€> Notify friends
       â””â”€> Send initial packets:
           â”œâ”€> UserInfo
           â”œâ”€> CharInfo (to others)
           â”œâ”€> SkillList
           â”œâ”€> ShortCutInit
           â”œâ”€> ItemList
           â”œâ”€> QuestList
           â””â”€> SSQInfo

5. PLAYER IS NOW IN GAME
```

**Key Code:**

```java
public void onPlayerEnter() {
    // Handle Cursed Weapons
    if (isCursedWeaponEquipped())
        CursedWeaponManager.getInstance().getCursedWeapon(getCursedWeaponEquippedId()).cursedOnLogin();
    
    // Add to game time task
    GameTimeTaskManager.getInstance().add(this);
    
    // Seven Signs validation
    if (isIn7sDungeon() && !isGM()) {
        if (SevenSignsManager.getInstance().isSealValidationPeriod() || 
            SevenSignsManager.getInstance().isCompResultsPeriod()) {
            if (SevenSignsManager.getInstance().getPlayerCabal(getObjectId()) != 
                SevenSignsManager.getInstance().getWinningCabal()) {
                teleportTo(RestartType.TOWN);
                setIsIn7sDungeon(false);
            }
        }
    }
    
    // Handle jail
    _punishment.handle();
    
    // GM login messages
    if (isGM()) {
        if (isInvul())
            sendMessage("Entering world in Invulnerable mode.");
        if (!getAppearance().isVisible())
            sendMessage("Entering world in Invisible mode.");
        if (isBlockingAll())
            sendMessage("Entering world in Refusal mode.");
    }
    
    // Revalidate zones
    revalidateZone(true);
    
    // Notify friends
    RelationManager.getInstance().notifyFriends(this, true);
}
```

### Workflow 2: PLAYER LOGOUT

```
Step-by-step flow from logout to disconnect:

1. PLAYER INITIATES LOGOUT
   â””â”€> Logout packet received
   â””â”€> logout(closeClient) called

2. CLEANUP
   â””â”€> cleanup() method
       â”œâ”€> Set online status to false
       â”œâ”€> Abort all actions
       â”œâ”€> Remove from party match
       â”œâ”€> Dismount if mounted
       â”œâ”€> Unsummon pet/summon
       â”œâ”€> Stop all tasks
       â”œâ”€> Remove from boss zones
       â”œâ”€> Remove from water task
       â”œâ”€> Remove from PvP flag task
       â”œâ”€> Cancel quest timers
       â”œâ”€> Stop signets & toggles
       â”œâ”€> Decay from world
       â”œâ”€> Leave party
       â”œâ”€> Leave olympiad
       â”œâ”€> Update clan member list
       â”œâ”€> Cancel active trade
       â”œâ”€> Remove from GM list if GM
       â”œâ”€> Set position to saved location
       â””â”€> Delete inventory from world

3. SAVE TO DATABASE
   â””â”€> store(true) method
       â”œâ”€> storeCharBase()
       â”œâ”€> storeCharSub()
       â””â”€> storeEffect(true)

4. REMOVE FROM WORLD
   â””â”€> World.getInstance().removePlayer(this)
   â””â”€> Notify friends (offline)

5. CLOSE CONNECTION
   â””â”€> GameClient.close()
   â””â”€> Send LeaveWorld packet
   â””â”€> Close socket
```

**Key Code:**

```java
public void logout(boolean closeClient) {
    final GameClient client = _client;
    if (client == null)
        return;
    
    if (client.isDetached())
        client.cleanMe(true);
    else if (!client.getConnection().isClosed())
        client.close((closeClient) ? LeaveWorld.STATIC_PACKET : ServerClose.STATIC_PACKET);
}

@Override
public void deleteMe() {
    super.deleteMe();
    
    cleanup();
    store();
}

private synchronized void cleanup() {
    try {
        // Put online status to false
        setOnlineStatus(false, true);
        
        // Abort all actions
        abortAll(true);
        
        // Remove from party match
        removeMeFromPartyMatch();
        
        // Dismount
        if (isFlying())
            removeSkill(FrequentSkill.WYVERN_BREATH.getSkill().getId(), false);
        
        if (isMounted())
            dismount();
        else if (_summon != null)
            _summon.unSummon(this);
        
        // Stop all tasks
        stopChargeTask();
        _punishment.stopTask(true);
        
        // Remove from task managers
        WaterTaskManager.getInstance().remove(this);
        AttackStanceTaskManager.getInstance().remove(this);
        PvpFlagTaskManager.getInstance().remove(this, false);
        GameTimeTaskManager.getInstance().remove(this);
        ShadowItemTaskManager.getInstance().remove(this);
        
        // Cancel quest timers
        for (Quest quest : ScriptData.getInstance().getQuests())
            quest.cancelQuestTimers(this);
        
        // Stop effects
        for (AbstractEffect effect : getAllEffects()) {
            if (effect.getSkill().isToggle()) {
                effect.exit();
                continue;
            }
            
            switch (effect.getEffectType()) {
                case SIGNET_GROUND:
                case SIGNET_EFFECT:
                    effect.exit();
                    break;
            }
        }
        
        // Decay from world
        decayMe();
        
        // Leave party
        if (_party != null)
            _party.removePartyMember(this, MessageType.DISCONNECTED);
        
        // Handle olympiad
        if (OlympiadManager.getInstance().isRegistered(this) || getOlympiadGameId() != -1)
            OlympiadManager.getInstance().removeDisconnectedCompetitor(this);
        
        // Update clan
        if (getClan() != null) {
            final ClanMember clanMember = getClan().getClanMember(getObjectId());
            if (clanMember != null)
                clanMember.setPlayerInstance(null);
        }
        
        // Cancel trade
        if (getActiveRequester() != null) {
            setActiveRequester(null);
            cancelActiveTrade();
        }
        
        // Remove from GM list
        if (isGM())
            AdminData.getInstance().deleteGm(this);
        
        // Clear observer mode
        if (isInObserverMode())
            setXYZInvisible(_savedLocation);
        
        // Delete inventory from world
        getInventory().deleteMe();
        
        // Clear warehouses
        clearWarehouse();
        clearFreight();
        clearDepositedFreight();
        
        // Handle cursed weapon
        if (isCursedWeaponEquipped())
            CursedWeaponManager.getInstance().getCursedWeapon(_cursedWeaponEquippedId).setPlayer(null);
        
        // Broadcast clan update
        if (_clan != null)
            _clan.broadcastToMembersExcept(this, new PledgeShowMemberListUpdate(this));
        
        // Free throne
        if (isSeated()) {
            final WorldObject object = World.getInstance().getObject(_throneId);
            if (object instanceof StaticObject staticObject)
                staticObject.setBusy(false);
        }
        
        // Notify friends
        RelationManager.getInstance().notifyFriends(this, false);
        
        // Force remove from world
        World.getInstance().removePlayer(this);
        
    } catch (Exception e) {
        LOGGER.error("Couldn't disconnect correctly the player.", e);
    }
}
```

### Workflow 3: TELEPORTATION

```
Teleport Flow:

1. TELEPORT REQUEST
   â””â”€> teleportTo(x, y, z, randomOffset) called
   
2. VALIDATE CONDITIONS
   â”œâ”€> Check if can teleport
   â”œâ”€> Check zone restrictions
   â””â”€> Check combat state

3. CANCEL ACTIVE STATES
   â”œâ”€> cancelActiveEnchant()
   â”œâ”€> cancelActiveTrade()
   â””â”€> Stop casting/attacking

4. HANDLE BOAT
   â””â”€> If in boat, remove from passengers

5. TELEPORT
   â”œâ”€> super.teleportTo() (Creature logic)
   â”œâ”€> Update position
   â”œâ”€> Decay from old region
   â”œâ”€> Spawn in new region
   â””â”€> onTeleported() called
       â”œâ”€> Apply spawn protection
       â”œâ”€> Teleport tamed beast
       â”œâ”€> Teleport summon
       â””â”€> Cancel store mode if active

6. BROADCAST
   â”œâ”€> Send TeleportToLocation packet
   â”œâ”€> Send ValidateLocation packet
   â””â”€> Broadcast to nearby players
```

**Key Code:**

```java
@Override
public boolean teleportTo(int x, int y, int z, int randomOffset) {
    if (!super.teleportTo(x, y, z, randomOffset))
        return false;
    
    // Cancel active enchant and trade
    cancelActiveEnchant();
    cancelActiveTrade();
    
    // Remove from boat
    final Boat boat = _boatInfo.getBoat();
    if (boat != null)
        boat.removePassenger(this);
    
    return true;
}

@Override
public final void onTeleported() {
    super.onTeleported();
    
    // Apply spawn protection
    if (Config.PLAYER_SPAWN_PROTECTION > 0)
        setSpawnProtection(true);
    
    // Teleport tamed beast
    if (_tamedBeast != null)
        _tamedBeast.teleportTo(getPosition(), 0);
    
    // Teleport summon
    if (_summon != null)
        _summon.teleportTo(getPosition(), 0);
    
    // Cancel store mode
    if (isInStoreMode())
        setOperateType(OperateType.NONE);
}
```

### Workflow 4: LEVEL UP

```
Level Up Flow:

1. GAIN EXPERIENCE
   â””â”€> addExpAndSp(exp, sp) called
   â””â”€> PlayerStatus.addExpAndSp() handles calculation

2. CHECK FOR LEVEL UP
   â””â”€> if (exp >= expForNextLevel)
       â””â”€> levelUp triggered

3. LEVEL UP PROCESS (in PlayerStatus)
   â”œâ”€> Increment level
   â”œâ”€> Recalculate stats
   â”œâ”€> Restore HP/MP/CP to full
   â”œâ”€> Update max HP/MP/CP
   â””â”€> Broadcast level up effect

4. GIVE SKILLS (in Player)
   â””â”€> giveSkills() called
       â”œâ”€> If AUTO_LEARN_SKILLS
       â”‚   â””â”€> rewardSkills() - give ALL skills
       â””â”€> Else
           â””â”€> Give only auto-get skills

5. NOTIFICATIONS
   â”œâ”€> Send StatusUpdate packet
   â”œâ”€> Send SystemMessage (level up)
   â”œâ”€> Send UserInfo packet
   â”œâ”€> Broadcast SocialAction (level up animation)
   â””â”€> Update party members

6. UPDATE DATABASE
   â””â”€> Auto-save triggers on level up
```

### Workflow 5: ITEM PICKUP

```
Item Pickup Flow:

1. PLAYER CLICKS ITEM
   â””â”€> Action packet received
   â””â”€> Player.getAI().tryToPickUp(item)

2. VALIDATE PICKUP
   â”œâ”€> Check distance (< 150)
   â”œâ”€> Check if item is pickable
   â”œâ”€> Check weight limit
   â””â”€> Check inventory space

3. PICKUP ITEM
   â””â”€> item.pickupMe(player)
       â”œâ”€> Remove from ground
       â”œâ”€> player.addItem(item, true)
       â””â”€> Destroy item from world

4. ADD TO INVENTORY
   â””â”€> addItem(ItemInstance item, boolean sendMessage)
       â”œâ”€> Check item count
       â”œâ”€> Send system message
       â”œâ”€> _inventory.addItem(item)
       â”œâ”€> Check cursed weapon
       â”œâ”€> Auto-equip arrows if bow equipped
       â””â”€> Send InventoryUpdate packet

5. SPECIAL CASES
   â”œâ”€> If herbs â†’ use immediately, don't add to inventory
   â”œâ”€> If adena â†’ updateAdena()
   â””â”€> If quest item â†’ trigger quest event
```

**Key Code:**

```java
@Override
public void addItem(ItemInstance item, boolean sendMessage) {
    // Don't bother with 0 or inferior amount
    if (item.getCount() < 1)
        return;
    
    // Send message to client, if requested
    if (sendMessage) {
        if (item.getCount() > 1)
            sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_PICKED_UP_S2_S1)
                .addItemName(item).addNumber(item.getCount()));
        else if (item.getEnchantLevel() > 0)
            sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_PICKED_UP_A_S1_S2)
                .addNumber(item.getEnchantLevel()).addItemName(item));
        else
            sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_PICKED_UP_S1)
                .addItemName(item));
    }
    
    // Add the item to inventory
    final ItemInstance newItem = _inventory.addItem(item);
    if (newItem == null)
        return;
    
    // If the item is a Cursed Weapon, activate it
    if (CursedWeaponManager.getInstance().isCursed(newItem.getItemId()))
        CursedWeaponManager.getInstance().activate(this, newItem);
    // If you pickup arrows and a bow is equipped, try to equip them
    else if (item.getItem().getItemType() == EtcItemType.ARROW && 
             getAttackType() == WeaponType.BOW && 
             !getInventory().hasItemIn(Paperdoll.LHAND))
        checkAndEquipArrows();
}
```

---

## ðŸ”’ THREADING & SYNCHRONIZATION

### Thread Safety in Player.java

Player.java deals with **MULTIPLE THREADS**:
- **Main game thread** - Processing packets
- **AI thread** - Running AI tasks
- **Timer threads** - Various scheduled tasks
- **Network thread** - Receiving packets

### Critical Synchronized Methods

```java
// 1. Store method - prevents concurrent saves
public synchronized void store(boolean storeActiveEffects) {
    storeCharBase();
    storeCharSub();
    storeEffect(storeActiveEffects);
}

// 2. Cleanup method - prevents concurrent cleanup
private synchronized void cleanup() {
    // ... cleanup logic
}

// 3. Bypass management - thread-safe bypass validation
public synchronized void addBypass(String bypass) {
    if (bypass == null)
        return;
    _validBypass.add(bypass);
}

public synchronized boolean validateBypass(String cmd) {
    for (String bp : _validBypass) {
        if (bp == null)
            continue;
        if (bp.equals(cmd))
            return true;
    }
    return true;
}

public synchronized void clearBypass() {
    _validBypass.clear();
    _validBypass2.clear();
}
```

### Locks Used

```java
// 1. Subclass Lock - prevents race conditions during class change
private final ReentrantLock _subclassLock = new ReentrantLock();

public boolean setActiveClass(int classIndex) {
    if (!_subclassLock.tryLock())
        return false;  // Another thread is changing class
    
    try {
        // ... class change logic
        return true;
    } finally {
        _subclassLock.unlock();
    }
}

// 2. Mount feed lock - implicit synchronized in methods
protected synchronized void startFeed(int npcId) {
    _canFeed = npcId > 0;
    // ... feed logic
}

protected synchronized void stopFeed() {
    if (_mountFeedTask != null) {
        _mountFeedTask.cancel(false);
        _mountFeedTask = null;
    }
}
```

### Thread-Safe Collections

```java
// Concurrent maps - allow multi-threaded access
private final Map<Integer, L2Skill> _skills = new ConcurrentSkipListMap<>();
private final Map<Integer, Timestamp> _reuseTimeStamps = new ConcurrentHashMap<>();
private final Map<Integer, SubClass> _subClasses = new ConcurrentSkipListMap<>();

// Concurrent sets - allow multi-threaded access
private final Set<Integer> _activeSoulShots = ConcurrentHashMap.newKeySet(1);
private final Set<Integer> _selectedBlocksList = ConcurrentHashMap.newKeySet();
private final Set<Integer> _selectedFriendList = ConcurrentHashMap.newKeySet();
```

### Atomic Operations

```java
// AtomicInteger for charges - thread-safe increment/decrement
private final AtomicInteger _charges = new AtomicInteger();

public void increaseCharges(int count, int max) {
    // ... validation
    
    if (_charges.addAndGet(count) >= max) {
        _charges.set(max);
        sendPacket(SystemMessageId.FORCE_MAXLEVEL_REACHED);
    }
    // ...
}

public boolean decreaseCharges(int count) {
    if (_charges.get() < count)
        return false;
    
    if (_charges.addAndGet(-count) == 0)
        stopChargeTask();
    else
        restartChargeTask();
    
    return true;
}
```

### Volatile Fields

```java
// Volatile for falling timestamp - ensures visibility across threads
private volatile long _fallingTimestamp;

public final boolean isFalling(int z) {
    if (isDead() || getMove().getMoveType() != MoveType.GROUND)
        return false;
    
    if (System.currentTimeMillis() < _fallingTimestamp)
        return true;
    
    // ... calculate fall damage
    
    setFalling();  // Updates volatile field
    return false;
}

public final void setFalling() {
    _fallingTimestamp = System.currentTimeMillis() + FALLING_VALIDATION_DELAY;
}
```

### Thread Pool Usage

```java
// Schedule tasks on ThreadPool
public void tempInventoryDisable() {
    _inventoryDisable = true;
    
    ThreadPool.schedule(() -> _inventoryDisable = false, 1500);
}

// Spawn protection task
public void setSpawnProtection(boolean isActive) {
    if (isActive) {
        if (_protectTask == null)
            _protectTask = ThreadPool.schedule(() -> {
                setSpawnProtection(false);
                sendMessage("The spawn protection has ended.");
            }, Config.PLAYER_SPAWN_PROTECTION * 1000L);
    } else {
        _protectTask.cancel(true);
        _protectTask = null;
    }
    broadcastUserInfo();
}
```

---

## ðŸ“¡ NETWORK INTEGRATION

### Sending Packets to Player

```java
@Override
public void sendPacket(L2GameServerPacket packet) {
    if (_client != null)
        _client.sendPacket(packet);
}

@Override
public void sendPacket(SystemMessageId id) {
    sendPacket(SystemMessage.getSystemMessage(id));
}
```

**Usage Examples:**

```java
// Simple message
player.sendPacket(SystemMessageId.YOU_PICKED_UP_ADENA);

// Message with parameters
player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_PICKED_UP_S2_S1)
    .addItemName(item).addNumber(count));

// Custom packet
player.sendPacket(new UserInfo(player));
player.sendPacket(new SkillList(player));
player.sendPacket(new InventoryUpdate(player));
```

### Broadcasting Packets

```java
// Broadcast to nearby players (excluding self)
@Override
public void broadcastPacket(L2GameServerPacket packet, boolean selfToo) {
    if (selfToo)
        sendPacket(packet);
    
    super.broadcastPacket(packet, selfToo);
}

// Broadcast in radius (including self)
@Override
public void broadcastPacketInRadius(L2GameServerPacket packet, int radius) {
    sendPacket(packet);
    
    super.broadcastPacketInRadius(packet, radius);
}

// Broadcast user info (to self and others)
public final void broadcastUserInfo() {
    sendPacket(new UserInfo(this));
    
    if (getPolymorphTemplate() != null)
        broadcastPacket(new AbstractNpcInfo.PcMorphInfo(this, getPolymorphTemplate()), false);
    else
        broadcastCharInfo();
}

// Broadcast character info (to others only)
public final void broadcastCharInfo() {
    forEachKnownType(Player.class, player -> {
        player.sendPacket(new CharInfo(this));
        
        final int relation = getRelation(player);
        final boolean isAutoAttackable = isAttackableWithoutForceBy(player);
        
        player.sendPacket(new RelationChanged(this, relation, isAutoAttackable));
        if (_summon != null)
            player.sendPacket(new RelationChanged(_summon, relation, isAutoAttackable));
    });
}
```

### Important Update Packets

```java
// Full user info update
player.broadcastUserInfo();  // Sends UserInfo + CharInfo

// Status update (HP/MP/CP)
StatusUpdate su = new StatusUpdate(player);
su.addAttribute(StatusType.CUR_HP, (int) player.getStatus().getHp());
su.addAttribute(StatusType.CUR_MP, (int) player.getStatus().getMp());
su.addAttribute(StatusType.CUR_CP, (int) player.getStatus().getCp());
player.sendPacket(su);

// Inventory update
InventoryUpdate iu = new InventoryUpdate(player);
iu.addItem(item);
player.sendPacket(iu);

// Skill list update
player.sendPacket(new SkillList(player));

// Effect icons update
player.updateEffectIcons();

// Shortcut update
player.sendPacket(new ShortCutInit(player));
```

---

## ðŸŽ’ ITEM MANAGEMENT

### Adding Items

```java
// Method 1: Add ItemInstance
player.addItem(itemInstance, true);  // sendMessage = true

// Method 2: Add by itemId and count
ItemInstance newItem = player.addItem(itemId, count, true);

// Method 3: Add earned item (harvest, sweep, quest)
ItemInstance newItem = player.addEarnedItem(itemId, count, true);
```

### Destroying Items

```java
// Method 1: Destroy entire ItemInstance
boolean success = player.destroyItem(itemInstance, true);

// Method 2: Destroy part of ItemInstance
boolean success = player.destroyItem(itemInstance, count, true);

// Method 3: Destroy by objectId
boolean success = player.destroyItem(objectId, count, true);

// Method 4: Destroy by itemId
boolean success = player.destroyItemByItemId(itemId, count, true);
```

### Special Cases

```java
// Adena management
player.addAdena(count, true);
boolean success = player.reduceAdena(count, true);
int adenaCount = player.getAdena();

// Ancient Adena management
player.addAncientAdena(count, true);
boolean success = player.reduceAncientAdena(count, true);
int ancientAdenaCount = player.getAncientAdena();
```

### Item Validation

```java
public ItemInstance checkItemManipulation(int objectId, int count) {
    // The item doesn't exist in World
    if (World.getInstance().getObject(objectId) == null)
        return null;
    
    // The item doesn't exist in inventory, or owner isn't this player
    final ItemInstance item = getInventory().getItemByObjectId(objectId);
    if (item == null || item.getOwnerId() != getObjectId())
        return null;
    
    // Count integrity
    if (count < 1 || (count > 1 && !item.isStackable()) || count > item.getCount())
        return null;
    
    // Summon item linked to active pet
    if (_summon != null && _summon.getControlItemId() == objectId || 
        _mountObjectId == objectId)
        return null;
    
    // Item being enchanted
    if (getActiveEnchantItem() != null && 
        getActiveEnchantItem().getObjectId() == objectId)
        return null;
    
    // Augmented weapon while casting
    if (item.isAugmented() && getCast().isCastingNow())
        return null;
    
    return item;
}
```

---

## âš”ï¸ COMBAT SYSTEM

### Attacking

```java
// Player attacks target
player.getAI().tryToAttack(target, ctrlPressed, shiftPressed);

// Attack is processed by PlayerAttack class
// Results in:
1. Attack animation (Attack packet)
2. Damage calculation (Formulas.calcPhysicalAttackDamage)
3. HP reduction on target
4. Status update packets
5. PvP flag update if attacking player
```

### Skill Casting

```java
// Player casts skill
player.getCast().doCast(skill);

// Cast is processed by PlayerCast class
// Flow:
1. Validate can cast
2. Consume MP/HP
3. Consume items (if required)
4. Start cast animation (MagicSkillUse packet)
5. Wait cast time
6. Hit target (call skill handler)
7. Apply effects
8. Add reuse delay
9. Send status updates
```

### PvP System

```java
// When player hits another player
public void updatePvPStatus(Creature target) {
    final Player player = target.getActingPlayer();
    if (player == null)
        return;
    
    if (isInDuel() && player.getDuelId() == getDuelId())
        return;
    
    if ((!isInsideZone(ZoneId.PVP) || !target.isInsideZone(ZoneId.PVP)) && 
        player.getKarma() == 0) {
        
        PvpFlagTaskManager.getInstance().add(
            this, 
            checkIfPvP(player) ? Config.PVP_PVP_TIME : Config.PVP_NORMAL_TIME
        );
        
        if (getPvpFlag() == 0)
            updatePvPFlag(1);
    }
}

// When player kills another player
public void onKillUpdatePvPKarma(Playable target) {
    // ... validation
    
    // Check if PvP
    if (checkIfPvP(target) || 
        (targetPlayer.getClan() != null && getClan() != null && 
         getClan().isAtWarWith(targetPlayer.getClanId()) && 
         targetPlayer.getClan().isAtWarWith(getClanId())) || 
        (targetPlayer.getKarma() > 0 && Config.KARMA_AWARD_PK_KILL)) {
        
        // PvP kill
        if (target instanceof Player) {
            setPvpKills(getPvpKills() + 1);
            sendPacket(new UserInfo(this));
        }
    }
    // PK
    else if (targetPlayer.getKarma() == 0 && targetPlayer.getPvpFlag() == 0) {
        if (target instanceof Player)
            setPkKills(getPkKills() + 1);
        
        setKarma(getKarma() + Formulas.calculateKarmaGain(getPkKills(), target instanceof Summon));
        
        checkItemRestriction();
        PvpFlagTaskManager.getInstance().remove(this, true);
    }
}
```

---

## ðŸª TRADING & PRIVATE STORES

### Private Store Opening

```java
// Open Private Buy Store
public void tryOpenPrivateBuyStore() {
    if (!canOpenPrivateStore(true))
        return;
    
    if (getOperateType() == OperateType.NONE || getOperateType() == OperateType.BUY) {
        if (isSittingNow())
            return;
        
        if (getOperateType() == OperateType.BUY)
            standUp();
        
        setOperateType(OperateType.BUY_MANAGE);
        sendPacket(new PrivateStoreManageListBuy(this));
    }
}

// Open Private Sell Store
public void tryOpenPrivateSellStore(boolean isPackageSale) {
    if (!canOpenPrivateStore(true))
        return;
    
    if (getOperateType() == OperateType.NONE || 
        getOperateType() == OperateType.SELL || 
        getOperateType() == OperateType.PACKAGE_SELL) {
        
        if (isSittingNow())
            return;
        
        if (getOperateType() == OperateType.SELL || 
            getOperateType() == OperateType.PACKAGE_SELL)
            standUp();
        
        setOperateType(OperateType.SELL_MANAGE);
        sendPacket(new PrivateStoreManageListSell(this, isPackageSale));
    }
}
```

### Trading

```java
// Start trade
public void startTrade(Player partner) {
    onTradeStart(partner);
    partner.onTradeStart(this);
}

public void onTradeStart(Player partner) {
    _activeTradeList = new TradeList(this);
    _activeTradeList.setPartner(partner);
    
    sendPacket(SystemMessage.getSystemMessage(SystemMessageId.BEGIN_TRADE_WITH_S1)
        .addString(partner.getName()));
    sendPacket(new TradeStart(this));
}

// Confirm trade
public void onTradeConfirm(Player partner) {
    sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CONFIRMED_TRADE)
        .addString(partner.getName()));
    
    partner.sendPacket(TradePressOwnOk.STATIC_PACKET);
    sendPacket(TradePressOtherOk.STATIC_PACKET);
}

// Cancel trade
public void cancelActiveTrade() {
    if (_activeTradeList == null)
        return;
    
    final Player partner = _activeTradeList.getPartner();
    if (partner != null)
        partner.onTradeCancel(this);
    
    onTradeCancel(this);
}

public void onTradeCancel(Player partner) {
    if (_activeTradeList == null)
        return;
    
    _activeTradeList.lock();
    _activeTradeList = null;
    
    sendPacket(SendTradeDone.FAIL_STATIC_PACKET);
    sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CANCELED_TRADE)
        .addString(partner.getName()));
}
```

---

## ðŸŒ TELEPORTATION SYSTEM

### Teleport Methods

```java
// Method 1: Teleport to coordinates
player.teleportTo(x, y, z, randomOffset);

// Method 2: Teleport to Location object
player.teleportTo(location, randomOffset);

// Method 3: Teleport to RestartType (town, castle, etc.)
player.teleportTo(RestartType.TOWN);

// Method 4: Instant teleport (no validation)
player.setXYZ(x, y, z);
player.revalidateZone(true);
```

### Teleport Validation

```java
@Override
public boolean teleportTo(int x, int y, int z, int randomOffset) {
    // Call parent teleport
    if (!super.teleportTo(x, y, z, randomOffset))
        return false;
    
    // Cancel active states
    cancelActiveEnchant();
    cancelActiveTrade();
    
    // Remove from boat
    final Boat boat = _boatInfo.getBoat();
    if (boat != null)
        boat.removePassenger(this);
    
    return true;
}
```

### Post-Teleport Processing

```java
@Override
public final void onTeleported() {
    super.onTeleported();
    
    // Apply spawn protection
    if (Config.PLAYER_SPAWN_PROTECTION > 0)
        setSpawnProtection(true);
    
    // Teleport tamed beast
    if (_tamedBeast != null)
        _tamedBeast.teleportTo(getPosition(), 0);
    
    // Teleport summon
    if (_summon != null)
        _summon.teleportTo(getPosition(), 0);
    
    // Cancel store mode
    if (isInStoreMode())
        setOperateType(OperateType.NONE);
}
```

---

## ðŸ‘ï¸ OBSERVER & OLYMPIAD MODES

### Observer Mode

```java
// Enter observer mode
public void enterObserverMode(ObserverLocation loc) {
    // Check adena cost
    if (loc.getCost() > 0 && !reduceAdena(loc.getCost(), true))
        return;
    
    // Unsummon everything
    dropAllSummons();
    
    // Leave party
    if (getParty() != null)
        getParty().removePartyMember(this, MessageType.EXPELLED);
    
    standUp();
    
    // Save current location
    _savedLocation.set(getPosition());
    
    // Make invulnerable and invisible
    setInvul(true);
    getAppearance().setVisible(false);
    setIsParalyzed(true);
    
    // Abort all actions
    abortAll(true);
    
    // Teleport to observer location
    teleportTo(loc, 0);
    sendPacket(new ObserverStart(loc));
}

// Leave observer mode
public void leaveObserverMode() {
    getAI().tryToIdle();
    
    setTarget(null);
    getAppearance().setVisible(true);
    setInvul(false);
    setIsParalyzed(false);
    
    sendPacket(new ObserverEnd(_savedLocation));
    teleportTo(_savedLocation, 0);
    
    _savedLocation.clean();
}
```

### Olympiad Mode

```java
// Enter olympiad observer mode
public void enterOlympiadObserverMode(int id) {
    final OlympiadGameTask task = OlympiadGameManager.getInstance().getOlympiadTask(id);
    if (task == null)
        return;
    
    dropAllSummons();
    
    if (getParty() != null)
        getParty().removePartyMember(this, MessageType.EXPELLED);
    
    _olympiadGameId = id;
    
    standUp();
    
    if (!isInObserverMode())
        _savedLocation.set(getPosition());
    
    setTarget(null);
    setInvul(true);
    getAppearance().setVisible(false);
    
    teleportTo(task.getZone().getSpawns(SpawnType.NORMAL).get(2), 0);
    sendPacket(new ExOlympiadMode(3));
}

// Leave olympiad observer mode
public void leaveOlympiadObserverMode() {
    if (_olympiadGameId == -1)
        return;
    
    _olympiadGameId = -1;
    
    getAI().tryToIdle();
    
    setTarget(null);
    getAppearance().setVisible(true);
    setInvul(false);
    
    sendPacket(new ExOlympiadMode(0));
    teleportTo(_savedLocation, 0);
    
    _savedLocation.clean();
}
```

---

## âš¡ PERFORMANCE CONSIDERATIONS

### Memory Management

```java
// Clear collections on logout
public void clearWarehouse() {
    if (_warehouse != null)
        _warehouse.deleteMe();
    _warehouse = null;
}

public void clearFreight() {
    if (_freight != null)
        _freight.deleteMe();
    _freight = null;
}

public void clearDepositedFreight() {
    _depositedFreight.forEach(PcFreight::deleteMe);
    _depositedFreight.clear();
}
```

### Lazy Loading

```java
// Warehouse is only loaded when needed
public PcWarehouse getWarehouse() {
    if (_warehouse == null) {
        _warehouse = new PcWarehouse(this);
        _warehouse.restore();
    }
    return _warehouse;
}

// Freight is only loaded when needed
public PcFreight getFreight() {
    if (_freight == null) {
        _freight = new PcFreight(this);
        _freight.restore();
    }
    return _freight;
}
```

### Efficient Updates

```java
// Use InventoryUpdate instead of full ItemList
InventoryUpdate iu = new InventoryUpdate(player);
iu.addItem(item);
player.sendPacket(iu);

// Batch status updates
StatusUpdate su = new StatusUpdate(player);
su.addAttribute(StatusType.CUR_HP, (int) hp);
su.addAttribute(StatusType.CUR_MP, (int) mp);
su.addAttribute(StatusType.CUR_CP, (int) cp);
player.sendPacket(su);
```

---

## âš ï¸ COMMON PITFALLS & BEST PRACTICES

### Pitfall 1: Modifying Player State Without Validation

**âŒ BAD:**
```java
player.getStatus().setHp(10000);  // No validation!
```

**âœ… GOOD:**
```java
int maxHp = player.getStatus().getMaxHp();
player.getStatus().setHp(Math.min(10000, maxHp));
```

### Pitfall 2: Not Broadcasting Updates

**âŒ BAD:**
```java
player.setKarma(100);  // Others won't see karma change
```

**âœ… GOOD:**
```java
player.setKarma(100);  // setKarma() already broadcasts
// OR if you modified state directly:
player.broadcastUserInfo();
```

### Pitfall 3: Forgetting to Save

**âŒ BAD:**
```java
player.setPvpKills(player.getPvpKills() + 1);
// Not saved to database!
```

**âœ… GOOD:**
```java
player.setPvpKills(player.getPvpKills() + 1);
player.store();  // Save to database
// OR rely on auto-save on logout
```

### Pitfall 4: Not Checking Online State

**âŒ BAD:**
```java
player.sendPacket(packet);  // Might crash if client is null
```

**âœ… GOOD:**
```java
if (player != null && player.isOnline())
    player.sendPacket(packet);
```

### Pitfall 5: Concurrent Modification

**âŒ BAD:**
```java
for (L2Skill skill : player.getSkills().values()) {
    player.removeSkill(skill.getId(), true);  // Modifies map while iterating!
}
```

**âœ… GOOD:**
```java
List<Integer> skillIds = new ArrayList<>(player.getSkills().keySet());
for (int skillId : skillIds) {
    player.removeSkill(skillId, true);
}
```

---

## ðŸš€ EXTENSION EXAMPLES

### Example 1: Custom Buff System

```java
// Add custom method to Player.java
public void giveCustomBuffs() {
    // Give custom buffs
    addSkill(SkillTable.getInstance().getInfo(1204, 2), false);  // Wind Walk
    addSkill(SkillTable.getInstance().getInfo(1040, 3), false);  // Shield
    addSkill(SkillTable.getInstance().getInfo(1068, 3), false);  // Might
    
    // Update effects
    updateEffectIcons();
    sendMessage("You have received custom buffs!");
}

// Call from admin command or NPC
player.giveCustomBuffs();
```

### Example 2: Custom Title System

```java
// Add to Player.java
private String _customTitle = "";

public void setCustomTitle(String title) {
    _customTitle = title;
    setTitle(title);
    broadcastTitleInfo();
}

public String getCustomTitle() {
    return _customTitle;
}

// Usage
player.setCustomTitle("Â§cVIP Player");
```

### Example 3: Playtime Tracker

```java
// Add to Player.java
public long getTotalOnlineTime() {
    long totalOnlineTime = _onlineTime;
    if (_onlineBeginTime > 0)
        totalOnlineTime += (System.currentTimeMillis() - _onlineBeginTime) / 1000;
    return totalOnlineTime;
}

public String getPlaytimeFormatted() {
    long seconds = getTotalOnlineTime();
    long hours = seconds / 3600;
    long minutes = (seconds % 3600) / 60;
    long secs = seconds % 60;
    
    return String.format("%02d:%02d:%02d", hours, minutes, secs);
}

// Usage
player.sendMessage("Your playtime: " + player.getPlaytimeFormatted());
```

---

## ðŸŽ“ SUMMARY

Player.java is the **heart** of L2JACIS. Understanding it means understanding:

1. **Identity** - How players are created, restored, and identified
2. **State Management** - How player data is stored and updated
3. **Skills & Progression** - How leveling and skills work
4. **Combat** - How PvP/PvE mechanics function
5. **Social Systems** - Clans, parties, friends
6. **Economy** - Items, trading, stores
7. **Threading** - How to safely modify player state
8. **Network** - How to communicate with clients

**Key Takeaways:**
- Always validate before modifying state
- Always broadcast changes to nearby players
- Always use thread-safe operations
- Always save important state to database
- Always check for null values
- Always handle edge cases

**Next Steps:**
1. Study specific systems in detail (combat, skills, etc.)
2. Practice with small modifications
3. Test thoroughly in development environment
4. Use logging to debug issues
5. Follow existing patterns and conventions

---

**END OF PLAYER.JAVA DEEP DIVE - PART 2**

*Document Created: November 12, 2025*  
*For: DEUS ACIS L2JACIS Project*