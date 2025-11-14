package net.sf.l2j.gameserver.handler.admincommandhandlers;

import java.util.StringTokenizer;

import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.data.xml.ArmorSetData;
import net.sf.l2j.gameserver.enums.Paperdoll;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.ArmorSet;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.item.kind.Armor;
import net.sf.l2j.gameserver.model.item.kind.Item;
import net.sf.l2j.gameserver.model.item.kind.Weapon;
import net.sf.l2j.gameserver.network.serverpackets.ItemList;
import net.sf.l2j.gameserver.network.serverpackets.SkillList;
import net.sf.l2j.gameserver.skills.L2Skill;

public class AdminEnchant implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_seteh",
		"admin_setec",
		"admin_seteg",
		"admin_setel",
		"admin_seteb",
		"admin_setew",
		"admin_setes",
		"admin_setle",
		"admin_setre",
		"admin_setlf",
		"admin_setrf",
		"admin_seten",
		"admin_setun",
		"admin_setba",
		"admin_enchant"
	};
	
	@Override
	public void useAdminCommand(String command, Player player)
	{
		final StringTokenizer st = new StringTokenizer(command, " ");
		st.nextToken(); // skip command
		
		int armorType = -1;
		
		if (command.startsWith("admin_seteh"))
			armorType = Item.SLOT_HEAD;
		else if (command.startsWith("admin_setec"))
			armorType = Item.SLOT_CHEST;
		else if (command.startsWith("admin_seteg"))
			armorType = Item.SLOT_GLOVES;
		else if (command.startsWith("admin_seteb"))
			armorType = Item.SLOT_FEET;
		else if (command.startsWith("admin_setel"))
			armorType = Item.SLOT_LEGS;
		else if (command.startsWith("admin_setew"))
			armorType = Item.SLOT_R_HAND;
		else if (command.startsWith("admin_setes"))
			armorType = Item.SLOT_L_HAND;
		else if (command.startsWith("admin_setle"))
			armorType = Item.SLOT_L_EAR;
		else if (command.startsWith("admin_setre"))
			armorType = Item.SLOT_R_EAR;
		else if (command.startsWith("admin_setlf"))
			armorType = Item.SLOT_L_FINGER;
		else if (command.startsWith("admin_setrf"))
			armorType = Item.SLOT_R_FINGER;
		else if (command.startsWith("admin_seten"))
			armorType = Item.SLOT_NECK;
		else if (command.startsWith("admin_setun"))
			armorType = Item.SLOT_UNDERWEAR;
		else if (command.startsWith("admin_setba"))
			armorType = Item.SLOT_BACK;
		
		if (armorType != -1)
		{
			try
			{
				int ench = Integer.parseInt(command.substring(12));
				
				// check value
				if (ench < 0 || ench > 65535)
					player.sendMessage("You must set the enchant level to be between 0-65535.");
				else
					setEnchant(player, ench, armorType);
			}
			catch (Exception e)
			{
				player.sendMessage("Please specify a new enchant value.");
			}
		}
		
		if (st.countTokens() == 2)
		{
			try
			{
				final Paperdoll paperdoll = Paperdoll.getEnumByName(st.nextToken());
				if (paperdoll == Paperdoll.NULL)
				{
					player.sendMessage("Unknown paperdoll slot.");
					return;
				}
				
				final int enchant = Integer.parseInt(st.nextToken());
				if (enchant < 0 || enchant > 65535)
				{
					player.sendMessage("You must set the enchant level between 0 - 65535.");
					return;
				}
				
				final Player targetPlayer = getTargetPlayer(player, true);
				
				final ItemInstance item = targetPlayer.getInventory().getItemFrom(paperdoll);
				if (item == null)
				{
					player.sendMessage(targetPlayer.getName() + " doesn't wear any item in " + paperdoll + " slot.");
					return;
				}
				
				final Item toTestItem = item.getItem();
				final int oldEnchant = item.getEnchantLevel();
				
				// Do nothing if both values are the same.
				if (oldEnchant == enchant)
				{
					player.sendMessage(targetPlayer.getName() + "'s " + toTestItem.getName() + " enchant is already set to " + enchant + ".");
					return;
				}
				
				item.setEnchantLevel(enchant, player);
				
				// If item is equipped, verify the skill obtention/drop (+4 duals, +6 armorset).
				if (item.isEquipped())
				{
					final int currentEnchant = item.getEnchantLevel();
					
					// Skill bestowed by +4 duals.
					if (toTestItem instanceof Weapon weapon)
					{
						// Old enchant was >= 4 and new is lower : we drop the skill.
						if (oldEnchant >= 4 && currentEnchant < 4)
						{
							final L2Skill enchant4Skill = weapon.getEnchant4Skill();
							if (enchant4Skill != null)
							{
								targetPlayer.removeSkill(enchant4Skill.getId(), false);
								targetPlayer.sendPacket(new SkillList(targetPlayer));
							}
						}
						// Old enchant was < 4 and new is 4 or more : we add the skill.
						else if (oldEnchant < 4 && currentEnchant >= 4)
						{
							final L2Skill enchant4Skill = weapon.getEnchant4Skill();
							if (enchant4Skill != null)
							{
								targetPlayer.addSkill(enchant4Skill, false);
								targetPlayer.sendPacket(new SkillList(targetPlayer));
							}
						}
					}
					// Add skill bestowed by +6 armorset.
					else if (toTestItem instanceof Armor)
					{
						// Old enchant was >= 6 and new is lower : we drop the skill.
						if (oldEnchant >= 6 && currentEnchant < 6)
						{
							// Check if player is wearing a chest item.
							final int itemId = targetPlayer.getInventory().getItemIdFrom(Paperdoll.CHEST);
							if (itemId > 0)
							{
								final ArmorSet armorSet = ArmorSetData.getInstance().getSet(itemId);
								if (armorSet != null)
								{
									final int skillId = armorSet.getEnchant6skillId();
									if (skillId > 0)
									{
										targetPlayer.removeSkill(skillId, false);
										targetPlayer.sendPacket(new SkillList(targetPlayer));
									}
								}
							}
						}
						// Old enchant was < 6 and new is 6 or more : we add the skill.
						else if (oldEnchant < 6 && currentEnchant >= 6)
						{
							// Check if player is wearing a chest item.
							final int itemId = targetPlayer.getInventory().getItemIdFrom(Paperdoll.CHEST);
							if (itemId > 0)
							{
								final ArmorSet armorSet = ArmorSetData.getInstance().getSet(itemId);
								if (armorSet != null && armorSet.isEnchanted6(targetPlayer)) // has all parts of set enchanted to 6 or more
								{
									final int skillId = armorSet.getEnchant6skillId();
									if (skillId > 0)
									{
										final L2Skill skill = SkillTable.getInstance().getInfo(skillId, 1);
										if (skill != null)
										{
											targetPlayer.addSkill(skill, false);
											targetPlayer.sendPacket(new SkillList(targetPlayer));
										}
									}
								}
							}
						}
					}
				}
				
				item.setEnchantLevel(enchant, player);
				
				targetPlayer.broadcastUserInfo();
				
				player.sendMessage(targetPlayer.getName() + "'s " + toTestItem.getName() + " enchant was modified from " + oldEnchant + " to " + enchant + ".");
			}
			catch (Exception e)
			{
				player.sendMessage("Please specify a new enchant value.");
			}
		}
		else
		{
			player.sendMessage("Usage: //enchant slot enchant");
			player.sendMessage("Slots: under|lear|rear|neck|lfinger|rfinger|head|rhand|lhand");
			player.sendMessage("Slots: gloves|chest|legs|feet|cloak|face|hair|hairall");
			sendFile(player, "enchant.htm");
		}
	}
	
	private static void setEnchant(Player activeChar, int enchant, int armorType)
	{
		WorldObject target = activeChar.getTarget();
		if (!(target instanceof Player))
			target = activeChar;
		
		final Player player = (Player) target;
		
		final ItemInstance item = player.getInventory().getItemFrom(armorType);
		if (item == null)
		{
			activeChar.sendMessage(player.getName() + " doesn't wear any item in " + armorType + " slot.");
			return;
		}
		
		final Item it = item.getItem();
		final int oldEnchant = item.getEnchantLevel();
		
		// Do nothing if both values are the same.
		if (oldEnchant == enchant)
		{
			activeChar.sendMessage(player.getName() + "'s " + it.getName() + " enchant is already set to " + enchant + ".");
			return;
		}
		
		item.setEnchantLevel(enchant, player);
		
		// If item is equipped, verify the skill obtention/drop (+4 duals, +6 armorset).
		if (item.isEquipped())
		{
			final int currentEnchant = item.getEnchantLevel();
			
			// Skill bestowed by +4 duals.
			if (it instanceof Weapon)
			{
				// Old enchant was >= 4 and new is lower : we drop the skill.
				if (oldEnchant >= 4 && currentEnchant < 4)
				{
					final L2Skill enchant4Skill = ((Weapon) it).getEnchant4Skill();
					if (enchant4Skill != null)
					{
						player.removeSkill(enchant4Skill.getId(), false);
						player.sendPacket(new SkillList(player));
					}
				}
				// Old enchant was < 4 and new is 4 or more : we add the skill.
				else if (oldEnchant < 4 && currentEnchant >= 4)
				{
					final L2Skill enchant4Skill = ((Weapon) it).getEnchant4Skill();
					if (enchant4Skill != null)
					{
						player.addSkill(enchant4Skill, false);
						player.sendPacket(new SkillList(player));
					}
				}
			}
			// Add skill bestowed by +6 armorset.
			else if (it instanceof Armor)
			{
				// Old enchant was >= 6 and new is lower : we drop the skill.
				if (oldEnchant >= 6 && currentEnchant < 6)
				{
					// Check if player is wearing a chest item.
					final int itemId = player.getInventory().getItemIdFrom(Paperdoll.CHEST);
					if (itemId > 0)
					{
						final ArmorSet armorSet = ArmorSetData.getInstance().getSet(itemId);
						if (armorSet != null)
						{
							final int skillId = armorSet.getEnchant6skillId();
							if (skillId > 0)
							{
								player.removeSkill(skillId, false);
								player.sendPacket(new SkillList(player));
							}
						}
					}
				}
				// Old enchant was < 6 and new is 6 or more : we add the skill.
				else if (oldEnchant < 6 && currentEnchant >= 6)
				{
					// Check if player is wearing a chest item.
					final int itemId = player.getInventory().getItemIdFrom(Paperdoll.CHEST);
					if (itemId > 0)
					{
						final ArmorSet armorSet = ArmorSetData.getInstance().getSet(itemId);
						if (armorSet != null && armorSet.isEnchanted6(player)) // has all parts of set enchanted to 6 or more
						{
							final int skillId = armorSet.getEnchant6skillId();
							if (skillId > 0)
							{
								final L2Skill skill = SkillTable.getInstance().getInfo(skillId, 1);
								if (skill != null)
								{
									player.addSkill(skill, false);
									player.sendPacket(new SkillList(player));
								}
							}
						}
					}
				}
			}
		}
		
		player.sendPacket(new ItemList(player, false));
		player.broadcastUserInfo();
		
		activeChar.sendMessage(player.getName() + "'s " + it.getName() + " enchant was modified from " + oldEnchant + " to " + enchant + ".");
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}