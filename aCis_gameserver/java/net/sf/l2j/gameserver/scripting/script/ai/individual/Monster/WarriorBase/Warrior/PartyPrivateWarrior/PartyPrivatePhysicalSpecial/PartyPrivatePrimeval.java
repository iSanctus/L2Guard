package net.sf.l2j.gameserver.scripting.script.ai.individual.Monster.WarriorBase.Warrior.PartyPrivateWarrior.PartyPrivatePhysicalSpecial;

import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.actor.Player;

public class PartyPrivatePrimeval extends PartyPrivatePhysicalSpecial
{
	public PartyPrivatePrimeval()
	{
		super("ai/individual/Monster/WarriorBase/Warrior/PartyPrivateWarrior/PartyPrivatePhysicalSpecial");
	}
	
	public PartyPrivatePrimeval(String descr)
	{
		super(descr);
	}
	
	protected final int[] _npcIds =
	{
		22204,
		22201,
		22209,
		22197,
		22212
	};
	
	@Override
	public void onScriptEvent(Npc npc, int eventId, int arg1, int arg2)
	{
		if (eventId == 10016)
		{
			final Creature c0 = (Creature) World.getInstance().getObject(arg1);
			if (c0 instanceof Player && getNpcIntAIParam(npc, "BroadCastReception") == 1)
			{
				npc.removeAllAttackDesire();
				npc.getAI().addAttackDesire(c0, 100);
			}
		}
		super.onScriptEvent(npc, eventId, arg1, arg2);
	}
	
	@Override
	public void onNoDesire(Npc npc)
	{
		if (npc.hasMaster() && !npc.getMaster().isDead())
			npc.getAI().addFollowDesire(npc.getMaster(), 5);
		else
			npc.getAI().addMoveToDesire(npc.getSpawnLocation(), 30);
	}
	
	@Override
	public void onSeeCreature(Npc npc, Creature creature)
	{
		if (getNpcIntAIParam(npc, "ag_type") == 1)
		{
			if (!(creature instanceof Playable))
				return;
			
			tryToAttack(npc, creature);
		}
	}
}