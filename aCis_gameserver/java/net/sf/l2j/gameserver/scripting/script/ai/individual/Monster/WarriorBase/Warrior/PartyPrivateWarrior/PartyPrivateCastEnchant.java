package net.sf.l2j.gameserver.scripting.script.ai.individual.Monster.WarriorBase.Warrior.PartyPrivateWarrior;

import net.sf.l2j.commons.random.Rnd;

import net.sf.l2j.gameserver.enums.actors.NpcSkillType;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.skills.L2Skill;

public class PartyPrivateCastEnchant extends PartyPrivateWarrior
{
	public PartyPrivateCastEnchant()
	{
		super("ai/individual/Monster/WarriorBase/Warrior/PartyPrivateWarrior");
	}
	
	public PartyPrivateCastEnchant(String descr)
	{
		super(descr);
	}
	
	protected final int[] _npcIds =
	{
		20976,
		20769,
		20765,
		22099
	};
	
	@Override
	public void onCreated(Npc npc)
	{
		npc._i_ai0 = 0;
		npc._i_ai1 = 0;
		
		super.onCreated(npc);
	}
	
	@Override
	public void onAttacked(Npc npc, Creature attacker, int damage, L2Skill skill)
	{
		if (attacker instanceof Playable && npc._i_ai1 == 0 && Rnd.get(100) < 33 && npc.getStatus().getHpRatio() > 0.5)
		{
			final L2Skill buff = getNpcSkillByType(npc, NpcSkillType.BUFF);
			npc.getAI().addCastDesire(npc, buff, 1000000);
			
			npc._i_ai1 = 1;
		}
		super.onAttacked(npc, attacker, damage, skill);
	}
}