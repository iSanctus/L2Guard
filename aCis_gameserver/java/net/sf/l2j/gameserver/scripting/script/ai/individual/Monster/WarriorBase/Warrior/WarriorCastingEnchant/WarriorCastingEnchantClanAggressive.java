package net.sf.l2j.gameserver.scripting.script.ai.individual.Monster.WarriorBase.Warrior.WarriorCastingEnchant;

import net.sf.l2j.commons.random.Rnd;

import net.sf.l2j.gameserver.enums.actors.NpcSkillType;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.skills.L2Skill;

public class WarriorCastingEnchantClanAggressive extends WarriorCastingEnchantClan
{
	public WarriorCastingEnchantClanAggressive()
	{
		super("ai/individual/Monster/WarriorBase/Warrior/WarriorCastingEnchant");
	}
	
	public WarriorCastingEnchantClanAggressive(String descr)
	{
		super(descr);
	}
	
	protected final int[] _npcIds =
	{
		21237,
		21241,
		20789,
		20804,
		20806,
		20791,
		21245,
		21253,
		21249,
		21004,
		20803,
		21002,
		21005,
		20834,
		21213,
		21217,
		21209,
		21221,
		21229,
		21225,
		20793,
		20808,
		20066,
		20571,
		20076,
		21233
	};
	
	@Override
	public void onSeeCreature(Npc npc, Creature creature)
	{
		if (npc.getAI().getLifeTime() > 7 && npc.isInMyTerritory())
		{
			if (npc._i_ai1 == 0 && Rnd.get(100) < 33)
			{
				final L2Skill buff = getNpcSkillByType(npc, NpcSkillType.BUFF);
				npc.getAI().addCastDesire(npc, buff, 1000000);
			}
			npc._i_ai1 = 1;
		}
		
		if (!(creature instanceof Playable))
			return;
		
		tryToAttack(npc, creature);
		
		super.onSeeCreature(npc, creature);
	}
}