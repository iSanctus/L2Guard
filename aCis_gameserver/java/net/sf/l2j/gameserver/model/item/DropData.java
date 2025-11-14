package net.sf.l2j.gameserver.model.item;

import net.sf.l2j.commons.random.Rnd;

public record DropData(int itemId, int minDrop, int maxDrop, double chance)
{
	
	public static final int MAX_CHANCE = 1000000;
	public static final int PERCENT_CHANCE = 1000000 / 100;
	
	@Override
	public String toString()
	{
		return "DropData =[ItemID: " + itemId + " Min: " + minDrop + " Max: " + maxDrop + " Chance: " + chance + "%]";
	}
	
	public int getRandomDrop()
	{
		return Rnd.get(minDrop, maxDrop);
	}
}