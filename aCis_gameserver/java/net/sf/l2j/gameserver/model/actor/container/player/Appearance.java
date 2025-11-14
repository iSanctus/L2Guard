package net.sf.l2j.gameserver.model.actor.container.player;

import net.sf.l2j.gameserver.enums.actors.Sex;

public final class Appearance
{
	// Campos existentes
	private byte _face;
	private byte _hairColor;
	private byte _hairStyle;
	private Sex _sex;
	private boolean _isVisible = true;
	private int _nameColor = 0xF1FFFF;
	private int _titleColor = 0xFFFF77;
	
	// Novos campos para os itens visuais, todos como 'int'
	private int _under; // Corrigido de byte para int
	private int _helmet;
	private int _chest;
	private int _legs;
	private int _gloves;
	private int _feet;
	private int _rhand;
	private int _lhand;
	
	public Appearance(byte face, byte hColor, byte hStyle, Sex sex)
	{
		_face = face;
		_hairColor = hColor;
		_hairStyle = hStyle;
		_sex = sex;
	}
	
	// --- Getters e Setters existentes (Face, Hair, Sex, Colors, etc.) ---
	public byte getFace()
	{
		return _face;
	}
	
	public void setFace(int value)
	{
		_face = (byte) value;
	}
	
	public byte getHairColor()
	{
		return _hairColor;
	}
	
	public void setHairColor(int value)
	{
		_hairColor = (byte) value;
	}
	
	public byte getHairStyle()
	{
		return _hairStyle;
	}
	
	public void setHairStyle(int value)
	{
		_hairStyle = (byte) value;
	}
	
	public Sex getSex()
	{
		return _sex;
	}
	
	public void setSex(Sex sex)
	{
		_sex = sex;
	}
	
	public boolean isVisible()
	{
		return _isVisible;
	}
	
	public void setVisible(boolean val)
	{
		_isVisible = val;
	}
	
	public int getNameColor()
	{
		return _nameColor;
	}
	
	public void setNameColor(int nameColor)
	{
		_nameColor = nameColor;
	}
	
	public int getTitleColor()
	{
		return _titleColor;
	}
	
	public void setTitleColor(int titleColor)
	{
		_titleColor = titleColor;
	}
	
	// ... (outros métodos existentes como setTitleColor(r,g,b) podem permanecer aqui) ...
	public void setNameColor(int red, int green, int blue)
	{
		_nameColor = (red & 0xFF) + ((green & 0xFF) << 8) + ((blue & 0xFF) << 16);
	}
	
	public void setTitleColor(int red, int green, int blue)
	{
		_titleColor = (red & 0xFF) + ((green & 0xFF) << 8) + ((blue & 0xFF) << 16);
	}
	
	// --- INICIO GETTERS E SETTERS PARA EQUIPAMENTOS ---
	
	// Getters (METODOS NOVOS E NECESSARIOS)
	public int getUnder()
	{
		return _under;
	}
	
	public int getHelmet()
	{
		return _helmet;
	}
	
	public int getChest()
	{
		return _chest;
	}
	
	public int getLegs()
	{
		return _legs;
	}
	
	public int getGloves()
	{
		return _gloves;
	}
	
	public int getFeet()
	{
		return _feet;
	}
	
	public int getWeapon()
	{
		return _rhand;
	}
	
	public int getShield()
	{
		return _lhand;
	}
	
	public void setUnder(int id)
	{
		this._under = id;
	} // Corrigido, sem cast para byte
	
	public void setHelmet(int id)
	{
		this._helmet = id;
	}
	
	public void setChest(int id)
	{
		this._chest = id;
	}
	
	public void setLegs(int id)
	{
		this._legs = id;
	}
	
	public void setGloves(int id)
	{
		this._gloves = id;
	}
	
	public void setFeet(int id)
	{
		this._feet = id;
	}
	
	public void setWeapon(int id)
	{
		this._rhand = id;
	}
	
	public void setShield(int id)
	{
		this._lhand = id;
	}
	
	// --- FIM DA SEÇÃO DE GETTERS E SETTERS PARA EQUIPAMENTOS ---
}