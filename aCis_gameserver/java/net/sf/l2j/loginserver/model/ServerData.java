package net.sf.l2j.loginserver.model;

import net.sf.l2j.commons.network.ServerType;

public record ServerData(ServerType type, String hostName, int serverId, int port, int currentPlayers, int maxPlayers, int ageLimit, boolean isPvp, boolean isTestServer, boolean isShowingBrackets, boolean isShowingClock)
{
	public ServerData(ServerType type, String hostName, GameServerInfo gsi)
	{
		this(type, hostName, gsi.getId(), gsi.getPort(), gsi.getCurrentPlayerCount(), gsi.getMaxPlayers(), gsi.getAgeLimit(), gsi.isPvp(), gsi.isTestServer(), gsi.isShowingBrackets(), gsi.isShowingClock());
	}
}