package net.sf.l2j.gameserver.scripting.script.ai.individual.Monster;

import net.sf.l2j.commons.random.Rnd;

import net.sf.l2j.gameserver.enums.actors.NpcSkillType;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.NpcStringId;
import net.sf.l2j.gameserver.skills.L2Skill;

public class WarriorHero extends MonsterAI
{
	public WarriorHero()
	{
		super("ai/individual/Monster");
	}
	
	public WarriorHero(String descr)
	{
		super(descr);
	}
	
	protected final int[] _npcIds =
	{
		21260
	};
	
	@Override
	public String onTimer(String name, Npc npc, Player player)
	{
		if (name.equalsIgnoreCase("3001"))
		{
			npc.deleteMe();
		}
		else if (name.equalsIgnoreCase("3002"))
		{
			npc.broadcastNpcSay(NpcStringId.get(1000434 + Rnd.get(7)));
		}
		return super.onTimer(name, npc, player);
	}
	
	@Override
	public void onCreated(Npc npc)
	{
		final Creature c0 = (Creature) World.getInstance().getObject(npc._param3);
		if (c0 != null)
		{
			final L2Skill heroSkill = getNpcSkillByType(npc, NpcSkillType.HERO_SKILL);
			npc.getAI().addCastDesire(c0, heroSkill, 1000000);
			
			npc.getAI().addAttackDesire(c0, 1000);
		}
		
		startQuestTimer("3001", npc, null, 15000);
		startQuestTimer("3002", npc, null, 8000);
		
		super.onCreated(npc);
	}
}