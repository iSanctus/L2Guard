package net.sf.l2j.gameserver.taskmanager;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.l2j.commons.pool.ThreadPool;

import net.sf.l2j.gameserver.model.actor.Npc;

/**
 * Handle all {@link Npc} AI tasks.
 */
public final class AiTaskManager implements Runnable
{
	private final Set<Npc> _npcs = ConcurrentHashMap.newKeySet();
	
	protected AiTaskManager()
	{
		// Run task each second.
		ThreadPool.scheduleAtFixedRate(this, 1000, 1000);
	}
	
	@Override
	public final void run()
	{
		// Loop all Npcs.
		for (Npc npc : _npcs)
			npc.getAI().runAI();
	}
	
	/**
	 * Add the {@link Npc} set as parameter to the {@link AiTaskManager}.
	 * @param npc : The {@link Npc} to add.
	 */
	public final void add(Npc npc)
	{
		npc.setAISleeping(false);
		
		_npcs.add(npc);
	}
	
	/**
	 * Remove the {@link Npc} set as parameter from the {@link AiTaskManager}.
	 * @param npc : The {@link Npc} to remove.
	 */
	public final void remove(Npc npc)
	{
		npc.setAISleeping(true);
		
		_npcs.remove(npc);
	}
	
	public static final AiTaskManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static final class SingletonHolder
	{
		protected static final AiTaskManager INSTANCE = new AiTaskManager();
	}
}