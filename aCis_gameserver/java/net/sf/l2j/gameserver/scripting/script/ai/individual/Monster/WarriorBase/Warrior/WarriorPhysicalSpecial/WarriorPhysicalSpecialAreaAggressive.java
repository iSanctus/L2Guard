package net.sf.l2j.gameserver.scripting.script.ai.individual.Monster.WarriorBase.Warrior.WarriorPhysicalSpecial;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;

public class WarriorPhysicalSpecialAreaAggressive extends WarriorPhysicalSpecialAggressive
{
	public WarriorPhysicalSpecialAreaAggressive()
	{
		super("ai/individual/Monster/WarriorBase/Warrior/WarriorPhysicalSpecial");
	}
	
	public WarriorPhysicalSpecialAreaAggressive(String descr)
	{
		super(descr);
	}
	
	protected final int[] _npcIds =
	{
		22011,
		22012,
		22013,
		22014,
		22015,
		22016
	};
	
	@Override
	public void onCreated(Npc npc)
	{
		// TODO: Area on/off
		// gg::Area_SetOnOff(AreaName,1);
		super.onCreated(npc);
	}
	
	@Override
	public void onMyDying(Npc npc, Creature killer)
	{
		// TODO: Area on/off
		// gg::Area_SetOnOff(AreaName,0);
		super.onMyDying(npc, killer);
	}
}
