package net.sf.l2j.loginserver.network.serverpackets;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public abstract class ServerBasePacket
{
	ByteArrayOutputStream _bao;
	
	protected ServerBasePacket()
	{
		_bao = new ByteArrayOutputStream();
	}
	
	protected void writeD(int value)
	{
		_bao.write(value & 0xFF);
		_bao.write(value >> 8 & 0xFF);
		_bao.write(value >> 16 & 0xFF);
		_bao.write(value >> 24 & 0xFF);
	}
	
	protected void writeH(int value)
	{
		_bao.write(value & 0xFF);
		_bao.write(value >> 8 & 0xFF);
	}
	
	protected void writeC(int value)
	{
		_bao.write(value & 0xFF);
	}
	
	protected void writeF(double org)
	{
		long value = Double.doubleToRawLongBits(org);
		_bao.write((int) (value & 0xFF));
		_bao.write((int) (value >> 8 & 0xFF));
		_bao.write((int) (value >> 16 & 0xFF));
		_bao.write((int) (value >> 24 & 0xFF));
		_bao.write((int) (value >> 32 & 0xFF));
		_bao.write((int) (value >> 40 & 0xFF));
		_bao.write((int) (value >> 48 & 0xFF));
		_bao.write((int) (value >> 56 & 0xFF));
	}
	
	protected void writeS(String text)
	{
		if (text != null && !text.isEmpty())
		{
			try
			{
				_bao.write(text.getBytes(StandardCharsets.UTF_16LE));
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		
		_bao.write(0);
		_bao.write(0);
	}
	
	protected void writeB(byte[] array)
	{
		if (array != null && array.length > 0)
		{
			try
			{
				_bao.write(array);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	public int getLength()
	{
		return _bao.size() + 2;
	}
	
	public byte[] getBytes()
	{
		writeD(0x00); // reserve for checksum
		
		int padding = _bao.size() % 8;
		if (padding != 0)
		{
			for (int i = padding; i < 8; i++)
				writeC(0x00);
		}
		
		return _bao.toByteArray();
	}
	
	public abstract byte[] getContent() throws IOException;
}