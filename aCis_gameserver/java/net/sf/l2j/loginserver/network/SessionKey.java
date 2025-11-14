package net.sf.l2j.loginserver.network;

/**
 * <p>
 * This class is used to represent session keys used by the client to authenticate in the gameserver
 * </p>
 * <p>
 * A SessionKey is made up of two 8 bytes keys. One is send in the {@link net.sf.l2j.loginserver.network.serverpackets.LoginOk LoginOk} packet and the other is sent in {@link net.sf.l2j.loginserver.network.serverpackets.PlayOk PlayOk}
 * </p>
 * @param playOkId1
 * @param playOkId2
 * @param loginOkId1
 * @param loginOkId2
 */
public record SessionKey(int playOkId1, int playOkId2, int loginOkId1, int loginOkId2)
{
	@Override
	public String toString()
	{
		return "PlayOk: " + playOkId1 + " " + playOkId2 + " LoginOk:" + loginOkId1 + " " + loginOkId2;
	}
	
	public boolean checkLoginPair(int loginOk1, int loginOk2)
	{
		return loginOkId1 == loginOk1 && loginOkId2 == loginOk2;
	}
}