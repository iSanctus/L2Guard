package net.sf.l2j.gameserver.communitybbs.manager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.stream.IntStream;

import net.sf.l2j.commons.lang.StringUtil;
import net.sf.l2j.commons.pool.ConnectionPool;

import net.sf.l2j.gameserver.data.cache.HtmCache;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Player;

public class RankingBBSManager extends BaseBBSManager
{
	private static final StringBuilder PVP = new StringBuilder();
	private static final StringBuilder PKS = new StringBuilder();
	
	private static final int PAGE_LIMIT_15 = 15;
	
	private long _nextUpdate;
	
	protected RankingBBSManager()
	{
	}
	
	@Override
	public void parseCmd(String command, Player player)
	{
		if (command.equals("_bbsranking"))
			showRakingList(player);
		else
			super.parseCmd(command, player);
	}
	
	public void showRakingList(Player player)
	{
		if (_nextUpdate < System.currentTimeMillis())
		{
			PVP.setLength(0);
			PKS.setLength(0);
			
			try (Connection con = ConnectionPool.getConnection())
			{
				try (PreparedStatement ps = con.prepareStatement("SELECT char_name, pvpkills FROM characters WHERE pvpkills > 0 ORDER BY pvpkills DESC LIMIT " + PAGE_LIMIT_15);
					ResultSet rs = ps.executeQuery())
				{
					int index = 1;
					while (rs.next())
					{
						final String name = rs.getString("char_name");
						final Player databasePlayer = World.getInstance().getPlayer(name);
						final String status = "L2UI_CH3.msnicon" + (databasePlayer != null && databasePlayer.isOnline() ? "1" : "4");
						
						StringUtil.append(PVP, "<table width=300 bgcolor=000000><tr><td width=20 align=right>", getColor(index), String.format("%02d", index), "</td>");
						StringUtil.append(PVP, "<td width=20 height=18><img src=", status, " width=16 height=16></td><td width=160 align=left>", name, "</td>");
						StringUtil.append(PVP, "<td width=100 align=right>", StringUtil.formatNumber(rs.getInt("pvpkills")), "</font></td></tr></table><img src=L2UI.SquareGray width=296 height=1>");
						index++;
					}
					IntStream.range(index - 1, PAGE_LIMIT_15).forEach(x -> applyEmpty(PVP));
				}
				
				try (PreparedStatement ps = con.prepareStatement("SELECT char_name, pkkills FROM characters WHERE pkkills > 0 ORDER BY pkkills DESC LIMIT " + PAGE_LIMIT_15);
					ResultSet rs = ps.executeQuery())
				{
					int index = 1;
					while (rs.next())
					{
						final String name = rs.getString("char_name");
						final Player databasePlayer = World.getInstance().getPlayer(name);
						final String status = "L2UI_CH3.msnicon" + (databasePlayer != null && databasePlayer.isOnline() ? "1" : "4");
						
						StringUtil.append(PKS, "<table width=300 bgcolor=000000><tr><td width=20 align=right>", getColor(index), String.format("%02d", index), "</td>");
						StringUtil.append(PKS, "<td width=20 height=18><img src=", status, " width=16 height=16></td><td width=160 align=left>", name, "</td>");
						StringUtil.append(PKS, "<td width=100 align=right>", StringUtil.formatNumber(rs.getInt("pkkills")), "</font></td></tr></table><img src=L2UI.SquareGray width=296 height=1>");
						index++;
					}
					IntStream.range(index - 1, PAGE_LIMIT_15).forEach(x -> applyEmpty(PKS));
				}
			}
			catch (Exception e)
			{
				LOGGER.warn("There was problem while updating ranking system.", e);
			}
			
			_nextUpdate = System.currentTimeMillis() + 60000L;
		}
		
		String content = HtmCache.getInstance().getHtm(CB_PATH + getFolder() + "ranklist.htm");
		content = content.replaceAll("%name%", player.getName());
		content = content.replaceAll("%pvp%", PVP.toString());
		content = content.replaceAll("%pks%", PKS.toString());
		
		content = content.replaceAll("%time%", String.valueOf((_nextUpdate - System.currentTimeMillis()) / 1000));
		separateAndSend(content, player);
	}
	
	protected void applyEmpty(StringBuilder sb)
	{
		sb.append("<table width=300 bgcolor=000000><tr>");
		sb.append("<td width=20 align=right><font color=B09878>--</font></td><td width=20 height=18></td>");
		sb.append("<td width=160 align=left><font color=B09878>----------------</font></td>");
		sb.append("<td width=100 align=right><font color=FF0000>0</font></td>");
		sb.append("</tr></table><img src=L2UI.SquareGray width=296 height=1>");
	}
	
	protected String getColor(int index)
	{
		switch (index)
		{
			case 1:
				return "<font color=FFFF00>";
			case 2:
				return "<font color=FFA500>";
			case 3:
				return "<font color=E9967A>";
		}
		return "";
	}
	
	@Override
	protected String getFolder()
	{
		return "ranking/";
	}

		public static RankingBBSManager getInstance()
		{
			return SingletonHolder.INSTANCE;
		}
		
		private static class SingletonHolder
		{
			protected static final RankingBBSManager INSTANCE = new RankingBBSManager();
		}
	}
