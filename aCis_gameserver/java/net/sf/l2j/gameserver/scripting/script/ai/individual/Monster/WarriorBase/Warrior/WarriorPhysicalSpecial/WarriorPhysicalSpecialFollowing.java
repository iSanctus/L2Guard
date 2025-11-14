package net.sf.l2j.gameserver.scripting.script.ai.individual.Monster.WarriorBase.Warrior.WarriorPhysicalSpecial;

import net.sf.l2j.commons.random.Rnd;

import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.location.Location;

public class WarriorPhysicalSpecialFollowing extends WarriorPhysicalSpecial
{
	public WarriorPhysicalSpecialFollowing()
	{
		super("ai/individual/Monster/WarriorBase/Warrior/WarriorPhysicalSpecial");
	}
	
	public WarriorPhysicalSpecialFollowing(String descr)
	{
		super(descr);
	}
	
	protected final int[] _npcIds =
	{
		21674,
		21675,
		21697,
		21698,
		21720,
		21721,
		21743,
		21744,
		21766,
		21767,
		21789,
		21790
	};
	
	@Override
	public void onScriptEvent(Npc npc, int eventId, int arg1, int arg2)
	{
		if (eventId == 10019)
		{
			Creature c0 = (Creature) World.getInstance().getObject(arg1);
			if (c0 != null)
			{
				int x = (c0.getX() - 50) + Rnd.get(100);
				int y = (c0.getY() - 50) + Rnd.get(100);
				
				Location moveToLoc = new Location(x, y, c0.getZ());
				
				npc.getAI().addMoveToDesire(moveToLoc, 15);
			}
		}
		super.onScriptEvent(npc, eventId, arg1, arg2);
	}
}