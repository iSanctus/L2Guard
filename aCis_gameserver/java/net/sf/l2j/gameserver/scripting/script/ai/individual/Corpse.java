package net.sf.l2j.gameserver.scripting.script.ai.individual;

import net.sf.l2j.gameserver.model.actor.Npc;

public class Corpse extends DefaultNpc
{
	public Corpse()
	{
		super("ai/individual");
	}
	
	public Corpse(String descr)
	{
		super(descr);
	}
	
	protected final int[] _npcIds =
	{
		18119,
	};
	
	@Override
	public void onCreated(Npc npc)
	{
		npc.doDie(npc);
	}
}
