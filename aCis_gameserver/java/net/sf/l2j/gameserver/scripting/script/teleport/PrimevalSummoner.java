package net.sf.l2j.gameserver.scripting.script.teleport;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.group.Party;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.skills.L2Skill;

public class PrimevalSummoner extends Quest
{
	public PrimevalSummoner()
	{
		super(-1, "teleport");
		
		addTalkId(32104);
		addCreated(32104);
		addUseSkillFinished(32104);
	}
	
	@Override
	public void onCreated(Npc npc)
	{
		npc._c_ai0 = npc;
	}
	
	@Override
	public String onTalk(Npc npc, Player player)
	{
		String htmltext = "";
		
		int i2 = 0;
		
		final Party party0 = player.getParty();
		if (party0 != null)
		{
			for (Player partyMember : party0.getMembers())
				if (partyMember.isDead() && npc.getSpawn().isInMyTerritory(partyMember))
					i2++;
		}
		
		if (i2 == 0)
			htmltext = "vervato002.htm";
		else if (player.reduceAdena(200000, true))
		{
			if (npc._c_ai0 == npc)
			{
				npc._c_ai0 = player;
				
				npc.getAI().addCastDesire(npc, 5121, 1, 1000000);
			}
			else
				htmltext = "vervato005.htm";
		}
		else
			htmltext = "vervato003.htm";
		
		return htmltext;
	}
	
	@Override
	public void onUseSkillFinished(Npc npc, Creature creature, L2Skill skill, boolean success)
	{
		if (skill.getId() == 5121 && npc._c_ai0 != npc)
		{
			final Party party0 = npc._c_ai0.getParty();
			if (party0 != null)
			{
				for (Player partyMember : party0.getMembers())
				{
					if (partyMember.isDead() && npc.getSpawn().isInMyTerritory(partyMember))
						partyMember.teleportTo(11320, -23504, -3640, 0);
				}
			}
			startQuestTimer("2005", npc, null, 3000);
		}
	}
	
	@Override
	public String onTimer(String name, Npc npc, Player player)
	{
		if (name.equalsIgnoreCase("2005") && npc._c_ai0 != npc)
		{
			final NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
			html.setFile("./data/html/script/" + getDescr() + "/" + getName() + "/" + "vervato004.htm");
			
			npc._c_ai0.sendPacket(html);
			npc._c_ai0.sendPacket(ActionFailed.STATIC_PACKET);
			npc._c_ai0 = npc;
		}
		return super.onTimer(name, npc, player);
	}
}