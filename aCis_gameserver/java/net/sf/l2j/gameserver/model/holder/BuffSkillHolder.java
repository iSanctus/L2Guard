package net.sf.l2j.gameserver.model.holder;

/**
 * A container used for schemes buffer.
 */
public final class BuffSkillHolder extends IntIntHolder
{
	private final String _type;
	
	public BuffSkillHolder(int id, int price, Integer integer, String type, String skillInfo)
	{
		super(id, price);
		
		_type = type;
	}
	
	public final String getType()
	{
		return _type;
	}
}