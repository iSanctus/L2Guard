package net.sf.l2j.gameserver.util.sellBuffEngine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import net.sf.l2j.gameserver.model.actor.container.player.Appearance;
import net.sf.l2j.gameserver.model.location.Location;


/**
 * Um objeto de dados que representa o estado de uma �nica loja de buffs.
 */
public final class ShopObject
{
	private static final Logger _log = Logger.getLogger(ShopObject.class.getName());
	
	private final int ownerId;
	private final Map<Integer, PrivateBuff> buffs = new LinkedHashMap<>();
	private final List<Integer> equippedItems = new ArrayList<>();
	private final Location location = new Location(0, 0, 0);
	
	private int sellerNpcObjectId;
	private String title = "No Title";
	private String storeMessage = "Buffs for Sale!";
	private int heading;
	private int classId;
	private Appearance appearance;
	private String tempPrice = "0";
	
	public ShopObject(final int ownerId)
	{
		this.ownerId = ownerId;
	}
	
	/**
	 * Adiciona um buff � lista de venda.
	 * @param buffId O ID da skill.
	 * @param lvl O n�vel da skill.
	 * @param price O pre�o da skill.
	 */
	public void addBuff(final int buffId, final int lvl, final int price)
	{
		buffs.put(buffId, new PrivateBuff(price, buffId, lvl));
	}
	
	/**
	 * Adiciona um buff a partir de uma string (usado pelo DAO). Garante que os par�metros sejam passados na ordem correta.
	 * @param line A string no formato "id,level,price".
	 */
	public void addBuff(final String line)
	{
		if (line == null || line.isEmpty())
			return;
		try
		{
			final String[] parts = line.split(",");
			if (parts.length == 3)
			{
				final int skillId = Integer.parseInt(parts[0].trim());
				final int skillLevel = Integer.parseInt(parts[1].trim());
				final int price = Integer.parseInt(parts[2].trim());
				
				// Chamada na ordem correta: (ID, N�vel, Pre�o)
				addBuff(skillId, skillLevel, price);
			}
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "ShopObject: Erro ao analisar a linha de buff '" + line + "' para ownerId " + this.ownerId, e);
		}
	}
	
	public void removeBuff(final Integer buffId)
	{
		buffs.remove(buffId);
	}
	
	public PrivateBuff getBuff(int skillId)
	{
		return buffs.get(skillId);
	}
	
	public Map<Integer, PrivateBuff> getBuffList()
	{
		return Collections.unmodifiableMap(buffs);
	}
	
	public String getBuffLine()
	{
		return buffs.values().stream().map(b -> String.join(",", String.valueOf(b.skillId()), String.valueOf(b.skillLevel()), String.valueOf(b.price()))).collect(Collectors.joining(";"));
	}
	
	// --- Getters e Setters ---
	public int getOwnerId()
	{
		return ownerId;
	}
	
	public int getSellerNpcObjectId()
	{
		return sellerNpcObjectId;
	}
	
	public void setSellerNpcObjectId(final int npcObjectId)
	{
		this.sellerNpcObjectId = npcObjectId;
	}
	
	public String getTitle()
	{
		return title;
	}
	
	public void setTitle(final String title)
	{
		this.title = (title != null && !title.isEmpty()) ? title : "No Title";
	}
	
	public String getStoreMessage()
	{
		return storeMessage;
	}
	
	public void setStoreMessage(String message)
	{
		this.storeMessage = (message == null || message.isEmpty()) ? "Buffs for Sale!" : message;
	}
	
	public int getX()
	{
		return location.getX();
	}
	
	public int getY()
	{
		return location.getY();
	}
	
	public int getZ()
	{
		return location.getZ();
	}
	
	public int getHeading()
	{
		return heading;
	}
	
	public void setXYZ(final int x, final int y, final int z, final int heading)
	{
		this.location.set(x, y, z);
		this.heading = heading;
	}
	
	public int getClassId()
	{
		return classId;
	}
	
	public void setClassId(int classId)
	{
		this.classId = classId;
	}
	
	public Appearance getAppearance()
	{
		return appearance;
	}
	
	public void setAppearance(Appearance appearance)
	{
		this.appearance = appearance;
	}
	
	public List<Integer> getEquippedItems()
	{
		return equippedItems;
	}
	
	public void setEquippedItems(List<Integer> items)
	{
		this.equippedItems.clear();
		if (items != null)
			this.equippedItems.addAll(items);
	}
	
	public String getTempPrice()
	{
		return tempPrice;
	}
	
	public void setTempPrice(String price)
	{
		this.tempPrice = price;
	}
	
	/**
	 * Record para armazenar os dados de um buff na loja de forma imut�vel.
	 */
	public record PrivateBuff(int price, int skillId, int skillLevel)
	{
	}
}