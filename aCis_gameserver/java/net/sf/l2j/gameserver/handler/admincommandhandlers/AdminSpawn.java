package net.sf.l2j.gameserver.handler.admincommandhandlers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.List;
import java.util.StringTokenizer;

import net.sf.l2j.commons.data.Pagination;
import net.sf.l2j.commons.lang.StringUtil;

import net.sf.l2j.gameserver.data.manager.FenceManager;
import net.sf.l2j.gameserver.data.manager.SpawnManager;
import net.sf.l2j.gameserver.data.xml.AdminData;
import net.sf.l2j.gameserver.data.xml.NpcData;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.Fence;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.spawn.ASpawn;
import net.sf.l2j.gameserver.model.spawn.Spawn;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public class AdminSpawn implements IAdminCommandHandler
{
	private static final String OTHER_XML_FOLDER = "./data/xml/spawnlist/custom";
		
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_list_spawns",
		"admin_spawn",
		"admin_delete",
		"admin_unspawnall",
		"admin_respawnall",
		"admin_spawnfence",
		"admin_deletefence",
		"admin_listfence"
	};
	
	@Override
	public void useAdminCommand(String command, Player player)
	{
		if (command.startsWith("admin_list_spawns"))
		{
			final StringTokenizer st = new StringTokenizer(command, " ");
			st.nextToken();
			
			int npcId = 0;
			
			final String entry = (st.hasMoreTokens()) ? st.nextToken() : null;
			final int page = (st.hasMoreTokens()) ? Integer.parseInt(st.nextToken()) : 1;
			
			if (entry == null)
			{
				final Npc npc = getTarget(Npc.class, player, false);
				if (npc == null)
				{
					player.sendPacket(SystemMessageId.INVALID_TARGET);
					return;
				}
				
				npcId = npc.getNpcId();
			}
			else if (StringUtil.isDigit(entry))
				npcId = Integer.parseInt(entry);
			else
			{
				final NpcTemplate template = NpcData.getInstance().getTemplateByName(entry);
				if (template != null)
					npcId = template.getNpcId();
			}
			
			if (npcId == 0)
			{
				player.sendPacket(SystemMessageId.INVALID_TARGET);
				return;
			}
			
			int row = 0 + (8 * (page - 1));
			
			// Generate data.
			final Pagination<Npc> list = new Pagination<>(World.getInstance().getNpcs(npcId).stream(), page, PAGE_LIMIT_8);
			list.append("<html><body>");
			
			for (Npc npc : list)
			{
				list.append((row % 2) == 0 ? "<table width=280 height=41 bgcolor=000000><tr>" : "<table width=280 height=41><tr>");
				list.append("<td><a action=\"bypass -h admin_teleport ", npc.getX(), " ", npc.getY(), " ", npc.getZ(), "\">", row);
				
				final ASpawn spawn = npc.getSpawn();
				if (spawn == null)
					list.append(" - (", npc.getPosition(), ")", "</a>");
				else
					list.append(" - ", spawn, "</a><br1>", spawn.getDescription());
				
				list.append("</td></tr></table><img src=\"L2UI.SquareGray\" width=280 height=1>");
				
				row++;
			}
			
			list.generateSpace(42);
			list.generatePages("bypass admin_list_spawns " + npcId + " %page%");
			list.append("</body></html>");
			
			final NpcHtmlMessage html = new NpcHtmlMessage(0);
			html.setHtml(list.getContent());
			player.sendPacket(html);
		}
		else if (command.startsWith("admin_unspawnall"))
		{
			World.toAllOnlinePlayers(SystemMessage.getSystemMessage(SystemMessageId.NPC_SERVER_NOT_OPERATING));
			SpawnManager.getInstance().despawn();
			World.getInstance().deleteVisibleNpcSpawns();
			AdminData.getInstance().broadcastMessageToGMs("NPCs' unspawn is now complete.");
		}
		else if (command.startsWith("admin_respawnall"))
		{
			// make sure all spawns are deleted
			SpawnManager.getInstance().despawn();
			World.getInstance().deleteVisibleNpcSpawns();
			
			// now respawn all
			NpcData.getInstance().reload();
			SpawnManager.getInstance().reload();
			AdminData.getInstance().broadcastMessageToGMs("NPCs' respawn is now complete.");
		}
		else if (command.startsWith("admin_spawnfence"))
		{
			StringTokenizer st = new StringTokenizer(command, " ");
			try
			{
				st.nextToken();
				int type = Integer.parseInt(st.nextToken());
				int sizeX = (Integer.parseInt(st.nextToken()) / 100) * 100;
				int sizeY = (Integer.parseInt(st.nextToken()) / 100) * 100;
				int height = 1;
				if (st.hasMoreTokens())
					height = Math.min(Integer.parseInt(st.nextToken()), 3);
				
				FenceManager.getInstance().addFence(player.getX(), player.getY(), player.getZ(), type, sizeX, sizeY, height);
				
				listFences(player);
			}
			catch (Exception e)
			{
				player.sendMessage("Usage: //spawnfence <type> <width> <length> [height]");
			}
		}
		else if (command.startsWith("admin_deletefence"))
		{
			StringTokenizer st = new StringTokenizer(command, " ");
			st.nextToken();
			try
			{
				final WorldObject worldObject = World.getInstance().getObject(Integer.parseInt(st.nextToken()));
				if (worldObject instanceof Fence fence)
				{
					FenceManager.getInstance().removeFence(fence);
					
					if (st.hasMoreTokens())
						listFences(player);
				}
				else
					player.sendPacket(SystemMessageId.INVALID_TARGET);
			}
			catch (Exception e)
			{
				player.sendMessage("Usage: //deletefence <objectId>");
			}
		}
		else if (command.startsWith("admin_listfence"))
			listFences(player);
		else if (command.startsWith("admin_spawn"))
		{
			StringTokenizer st = new StringTokenizer(command, " ");
			try
			{
				final String cmd = st.nextToken();
				final String idOrName = st.nextToken();
				final int respawnTime = (st.hasMoreTokens()) ? Integer.parseInt(st.nextToken()) : 60;
				
				final WorldObject targetWorldObject = getTarget(WorldObject.class, player, true);
				
				NpcTemplate template;
				
				// First parameter was an ID number
				if (idOrName.matches("[0-9]*"))
					template = NpcData.getInstance().getTemplate(Integer.parseInt(idOrName));
				// First parameter wasn't just numbers, so go by name not ID
				else
					template = NpcData.getInstance().getTemplateByName(idOrName.replace('_', ' '));
				
				try
				{
					final Spawn spawn = new Spawn(template);
					spawn.setLoc(targetWorldObject.getPosition());
					spawn.setRespawnDelay(respawnTime);
					spawn.doSpawn(false);
					addSpawn(spawn);
					
					player.sendMessage("You spawned " + template.getName() + ". - Cmd: " + cmd);
					
				}
				catch (Exception e)
				{
					player.sendPacket(SystemMessageId.APPLICANT_INFORMATION_INCORRECT);
				}
			}
			catch (Exception e)
			{
				sendFile(player, "spawns.htm");
			}
		}
		else if (command.startsWith("admin_delete"))
		{
			// Target must be a Npc.
			final WorldObject targetWorldObject = player.getTarget();
			if (!(targetWorldObject instanceof Npc targetNpc))
			{
				player.sendPacket(SystemMessageId.INVALID_TARGET);
				return;
			}
			
			// Npc ASpawn must be Spawn type.
			final ASpawn spawn = targetNpc.getSpawn();
			if (!(spawn instanceof Spawn))
			{
				player.sendPacket(SystemMessageId.INVALID_TARGET);
				return;
			}
			
			// Delete the Npc.
			targetNpc.deleteMe();
			
			// Delete the Spawn entry.
			SpawnManager.getInstance().deleteSpawn((Spawn) spawn);
			
			// Send Player log.
			player.sendMessage("You deleted " + targetNpc.getName() + ".");
		}
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
	private static void listFences(Player player)
	{
		final List<Fence> fences = FenceManager.getInstance().getFences();
		final StringBuilder sb = new StringBuilder();
		
		sb.append("<html><body>Total Fences: " + fences.size() + "<br><br>");
		for (Fence fence : fences)
			sb.append("<a action=\"bypass -h admin_deletefence " + fence.getObjectId() + " 1\">Fence: " + fence.getObjectId() + " [" + fence.getX() + " " + fence.getY() + " " + fence.getZ() + "]</a><br>");
		sb.append("</body></html>");
		
		final NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setHtml(sb.toString());
		player.sendPacket(html);
	}
		
		private static void addSpawn(Spawn spawn)
		{
			SpawnManager.getInstance().addSpawn(spawn);
			
			// Create output directory if it doesn't exist
			final File outputDirectory = new File(OTHER_XML_FOLDER);
			if (!outputDirectory.exists())
			{
				try
				{
					outputDirectory.mkdir();
				}
				catch (SecurityException se)
				{
					// empty
				}
			}
			
			// XML file for spawn
			final String name = spawn.getNpc().getName().replaceAll("(\\s|')+","").toLowerCase() + "_" + System.currentTimeMillis();
			final String npcMakerName = spawn.getNpc().getName().replaceAll("(\\s|')+","").toLowerCase() + "_" + System.nanoTime();
			final String fileName = spawn.getNpc().getName().replaceAll("(\\s|')+","").toLowerCase();
			
			final int x = ((spawn.getLocX() - World.WORLD_X_MIN) >> 15) + World.TILE_X_MIN;
			final int y = ((spawn.getLocY() - World.WORLD_Y_MIN) >> 15) + World.TILE_Y_MIN;
			final File spawnFile = new File(OTHER_XML_FOLDER + "/" + fileName + "_" + x + "_" + y + ".xml");
			
			// Write info to XML
			final String spawnId = String.valueOf(spawn.getNpcId());
			final String spawnLoc = String.valueOf(spawn.getLocX() + ";" + spawn.getLocY() + ";" + spawn.getLocZ() + ";" + spawn.getHeading());
			
			final String respawnDelay = spawn.calculateRespawnDelay() + "sec";
			
			if (spawnFile.exists()) // update
			{
				final File tempFile = new File(OTHER_XML_FOLDER + "/" + name + "_" + x + "_" + y + ".tmp");
				try (BufferedReader reader = new BufferedReader(new FileReader(spawnFile));
					BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile)))
				{
					String currentLine;
					while ((currentLine = reader.readLine()) != null)
					{
						if (currentLine.contains("</list>"))
						{
							writer.write("	<territory name=\"" + name + "\" minZ=\"" + (spawn.getLocZ()) + "\" maxZ=\"" + (spawn.getLocZ() + 16) + "\">\n");
							writer.write("		<node x=\"" + (spawn.getLocX() + 50) + "\" y=\"" + (spawn.getLocY() + 50) + "\" />\n");
							writer.write("		<node x=\"" + (spawn.getLocX() - 50) + "\" y=\"" + (spawn.getLocY() + 50) + "\" />\n");
							writer.write("		<node x=\"" + (spawn.getLocX() - 50) + "\" y=\"" + (spawn.getLocY() - 50) + "\" />\n");
							writer.write("		<node x=\"" + (spawn.getLocX() + 50) + "\" y=\"" + (spawn.getLocY() - 50) + "\" />\n");
							writer.write("	</territory>\n");
							writer.write("	<npcmaker name=\"" + npcMakerName + "\" territory=\"" + name + "\" maximumNpcs=\"" + 1 + "\">\n");
							writer.write("		<npc id=\"" + spawnId + "\" pos=\"" + spawnLoc + "\" total=\"" + 1 + "\" respawn=\"" + respawnDelay + "\" /> <!-- " + spawn.getNpc().getTemplate().getName() + " -->\n");
							writer.write("	</npcmaker>\n");
							writer.write(currentLine + "\n");
							continue;
						}
						writer.write(currentLine + "\n");
					}
					writer.close();
					reader.close();
					spawnFile.delete();
					tempFile.renameTo(spawnFile);
				}
				catch (Exception e)
				{
					LOGGER.warn("Could not store spawn in the spawn XML files: " + e);
				}
			}
			else // new file
			{
				try (BufferedWriter writer = new BufferedWriter(new FileWriter(spawnFile)))
				{
					writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
					writer.write("<list>\n");
					writer.write("	<territory name=\"" + name + "\" minZ=\"" + (spawn.getLocZ()) + "\" maxZ=\"" + (spawn.getLocZ() + 16) + "\">\n");
					writer.write("		<node x=\"" + (spawn.getLocX() + 50) + "\" y=\"" + (spawn.getLocY() + 50) + "\" />\n");
					writer.write("		<node x=\"" + (spawn.getLocX() - 50) + "\" y=\"" + (spawn.getLocY() + 50) + "\" />\n");
					writer.write("		<node x=\"" + (spawn.getLocX() - 50) + "\" y=\"" + (spawn.getLocY() - 50) + "\" />\n");
					writer.write("		<node x=\"" + (spawn.getLocX() + 50) + "\" y=\"" + (spawn.getLocY() - 50) + "\" />\n");
					writer.write("	</territory>\n");
					writer.write("	<npcmaker name=\"" + npcMakerName + "\" territory=\"" + name + "\" maximumNpcs=\"" + 1 + "\">\n");
					writer.write("		<npc id=\"" + spawnId + "\" pos=\"" + spawnLoc + "\" total=\"" + 1 + "\" respawn=\"" + respawnDelay + "\" /> <!-- " + spawn.getNpc().getTemplate().getName() + " -->\n");
					writer.write("	</npcmaker>\n");
					writer.write("</list>\n");
					writer.close();
				}
				catch (Exception e)
				{
					LOGGER.warn("Spawn " + spawn + " could not be added to the spawn XML files: " + e);
				}
			}
		}
}