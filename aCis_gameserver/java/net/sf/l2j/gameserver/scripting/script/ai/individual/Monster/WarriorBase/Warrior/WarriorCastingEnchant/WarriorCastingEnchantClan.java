package net.sf.l2j.gameserver.scripting.script.ai.individual.Monster.WarriorBase.Warrior.WarriorCastingEnchant;

import net.sf.l2j.commons.random.Rnd;

import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.enums.actors.NpcSkillType;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.skills.L2Skill;

public class WarriorCastingEnchantClan extends WarriorCastingEnchant
{
	public WarriorCastingEnchantClan()
	{
		super("ai/individual/Monster/WarriorBase/Warrior/WarriorCastingEnchant");
	}
	
	public WarriorCastingEnchantClan(String descr)
	{
		super(descr);
	}
	
	protected final int[] _npcIds =
	{
		21616,
		21634,
		20081,
		20794,
		20840,
		21614,
		21615,
		20846,
		21632,
		21633,
		21010,
		20643,
		20593,
		20682
	};
	
	@Override
	public void onClanAttacked(Npc caller, Npc called, Creature attacker, int damage, L2Skill skill)
	{
		if (attacker instanceof Playable && called.getAI().getLifeTime() > 7 && called.getAI().getCurrentIntention().getType() != IntentionType.ATTACK && called._i_ai1 == 0 && Rnd.get(100) < 50 && called.getStatus().getHpRatio() > 0.5)
		{
			final L2Skill buff = getNpcSkillByType(called, NpcSkillType.BUFF);
			if (getAbnormalLevel(caller, buff) <= 0)
				called.getAI().addCastDesire(caller, buff, 1000000);
		}
		called._i_ai1 = 1;
		
		super.onClanAttacked(caller, called, attacker, damage, skill);
	}
}