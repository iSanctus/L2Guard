package net.sf.l2j.gameserver.util.sellBuffEngine;

import java.util.List;
import java.util.stream.Collectors;

import net.sf.l2j.gameserver.data.sql.PlayerInfoTable;
import net.sf.l2j.gameserver.data.xml.AdminData;
import net.sf.l2j.gameserver.data.xml.PlayerData;
import net.sf.l2j.gameserver.data.xml.PlayerLevelData;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.AccessLevel;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.template.PlayerTemplate;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.records.PlayerLevel;
import net.sf.l2j.gameserver.taskmanager.ItemInstanceTaskManager;

public final class BuffShopFactory
{
	private static class SingletonHolder
	{
		private static final BuffShopFactory _instance = new BuffShopFactory();
	}
	
	public static BuffShopFactory getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private BuffShopFactory()
	{
	}
	
	public Player createShopNpc(final Player creator, final ShopObject shopConfig)
	{
		populateShopConfigFromCreator(shopConfig, creator);
		final PlayerTemplate template = creator.getTemplate();
		final Player dummyNpc = new Player(IdFactory.getInstance().getNextId(), template, "BuffShop_" + creator.getObjectId(), creator.getAppearance());
		configureDummyBase(dummyNpc, creator.getName(), shopConfig.getTitle());
		configureNpcStats(dummyNpc, creator.getStatus().getMaxHp(), creator.getStatus().getMaxCp());
		equipGhostItems(dummyNpc, shopConfig.getEquippedItems());
		return dummyNpc;
	}
	
	public Player createShopNpc(final ShopObject shopConfig)
	{
		final PlayerTemplate template = PlayerData.getInstance().getTemplate(shopConfig.getClassId());
		if (template == null)
			return null;
		final Player dummyNpc = new Player(IdFactory.getInstance().getNextId(), template, "BuffShopOffline_" + shopConfig.getOwnerId(), shopConfig.getAppearance());
		String ownerName = PlayerInfoTable.getInstance().getPlayerName(shopConfig.getOwnerId());
		if (ownerName == null || ownerName.isEmpty())
			ownerName = "Buff Seller";
		configureDummyBase(dummyNpc, ownerName, shopConfig.getTitle());
		configureNpcStats(dummyNpc, template.getBaseHpMax(80), template.getBaseCpMax(80));
		equipGhostItems(dummyNpc, shopConfig.getEquippedItems());
		return dummyNpc;
	}
	
	private void configureDummyBase(Player npc, String name, String title)
	{
		npc.setDummy(true);
		npc.setName(name);
		npc.setTitle(title);
		AccessLevel defaultAccessLevel = AdminData.getInstance().getAccessLevel(0);
		if (defaultAccessLevel != null)
		{
			npc.setAccessLevel(defaultAccessLevel.getLevel());
		}
	}
	
	private void populateShopConfigFromCreator(ShopObject config, Player creator)
	{
		config.setClassId(creator.getClassId().getId());
		config.setAppearance(creator.getAppearance());
		config.setEquippedItems(creator.getInventory().getPaperdollItems().stream().map(ItemInstance::getItemId).collect(Collectors.toList()));
		config.setStoreMessage("Buffs " + creator.getClassId().toString() + " Lv" + creator.getStatus().getLevel());
	}
	
	private void configureNpcStats(Player npc, double maxHp, double maxCp)
	{
		final int baseLevel = 80;
		PlayerLevel pl = PlayerLevelData.getInstance().getPlayerLevel(baseLevel);
		if (pl != null)
			npc.getStatus().addExpAndSp(pl.requiredExpToLevelUp() - npc.getStatus().getExp(), 0);
		npc.getStatus().setHp(maxHp);
		npc.getStatus().setCp(maxCp);
		npc.getStatus().setMp(4000);
	}
	
	private void equipGhostItems(Player npc, List<Integer> itemIds)
	{
		if (itemIds == null)
			return;
		for (final int itemId : itemIds)
		{
			final ItemInstance dummyItem = ItemInstance.create(itemId, 1);
			if (dummyItem == null)
				continue;
			ItemInstanceTaskManager.getInstance().add(dummyItem);
			npc.getInventory().addItem(dummyItem);
			npc.getInventory().equipItem(dummyItem);
		}
	}
}