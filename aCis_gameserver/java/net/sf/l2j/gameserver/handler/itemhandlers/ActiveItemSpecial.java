package net.sf.l2j.gameserver.handler.itemhandlers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

import net.sf.l2j.commons.pool.ConnectionPool;

import net.sf.l2j.gameserver.enums.actors.ClassId;
import net.sf.l2j.gameserver.enums.actors.Sex;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUse;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.skills.L2Skill;

public class ActiveItemSpecial implements IItemHandler
{
    // ==================== Itens especiais ====================
    public static final int CHANGE_NAME   = 18025;
    public static final int CHANGE_NOBLES = 18026;
    public static final int CHANGE_SEXY   = 18027;
    public static final int CHANGE_CLASS  = 18028;
    public static final int PK_CLEAN      = 18029;

    private static final int[] ITEM_IDS =
    {
        CHANGE_NAME,
        CHANGE_NOBLES,
        CHANGE_SEXY,
        CHANGE_CLASS,
        PK_CLEAN
    };

    @Override
    public void useItem(Playable playable, ItemInstance item, boolean forceUse)
    {
        if (!(playable instanceof Player))
            return;

        Player player = (Player) playable;
        int itemId = item.getItemId();

        switch (itemId)
        {
            case CHANGE_NOBLES:
                if (!player.isNoble())
                {
                    player.setNoble(true, true);
                    player.broadcastPacket(new MagicSkillUse(player, 5103, 1, 1000, 0));
                    player.getInventory().addItem(7694, 1); // Tiara nobre
                    player.getInventory().destroyItem(item, 1);
                    player.sendMessage("You are now a noble!");
                    player.broadcastUserInfo();
                }
                else
                    player.sendMessage("You are already a noble!");
                break;

            case CHANGE_SEXY:
                if (player.isInOlympiadMode() || player.isAllSkillsDisabled())
                {
                    player.sendMessage("You cannot use this item right now.");
                    return;
                }

                player.getAppearance().setSex(player.getAppearance().getSex() == Sex.MALE ? Sex.FEMALE : Sex.MALE);
                player.getInventory().destroyItem(item, 1);
                player.broadcastUserInfo();
                player.sendMessage("Your gender has been changed. You will be disconnected for security reasons.");

                new Thread(() -> {
                    try
                    {
                        Thread.sleep(2000);
                        player.deleteMe();
                        if (player.getClient() != null)
                            player.getClient().closeNow();
                    }
                    catch (InterruptedException e) { e.printStackTrace(); }
                }).start();
                break;

            case CHANGE_CLASS:
                NpcHtmlMessage htmlClass = new NpcHtmlMessage(player.getObjectId());
                htmlClass.setFile("data/html/class_changer/class.htm");
                htmlClass.replace("%name%", player.getName());
                player.sendPacket(htmlClass);
                break;

            case CHANGE_NAME:
                NpcHtmlMessage htmlName = new NpcHtmlMessage(player.getObjectId());
                htmlName.setFile("data/html/mods/namechange.htm");
                htmlName.replace("%name%", player.getName());
                player.sendPacket(htmlName);
                break;

            case PK_CLEAN:
                if (player.isInOlympiadMode())
                {
                    player.sendMessage("You cannot use this item during Olympiad.");
                    return;
                }

                if (player.getPkKills() > 0)
                {
                    player.setPkKills(0);
                    player.getInventory().destroyItem(item, 1);
                    player.broadcastUserInfo();
                    player.sendMessage("Your PK count has been reset.");
                }
                else
                    player.sendMessage("You have no PKs to clean.");
                break;

            default: break;
        }
    }

    public int[] getItemIds()
    {
        return ITEM_IDS;
    }

    // ==================== Troca de Classe ====================
    public static void applyClassChange(Player player, int newClassId, ItemInstance item)
    {
        if (player == null)
            return;

        try
        {
            ClassId newClass = ClassId.values()[newClassId];
            if (player.getClassId() == newClass)
            {
                player.sendMessage("You are already in this class.");
                return;
            }

            // Remover skills antigas da memória e do banco
            List<L2Skill> oldSkills = new ArrayList<>(player.getSkills().values());
            for (L2Skill skill : oldSkills)
                player.removeSkill(skill.getId(), true, true); // true = remove do banco também

            // Aplicar nova classe
            player.setClassId(newClass.getId());
            player.setBaseClass(newClass.getId());
            player.store();

            // Atualizar info
            player.broadcastUserInfo();
            player.sendMessage("Your class has been changed to " + newClass.name() + "!");
            player.broadcastPacket(new MagicSkillUse(player, 5103, 1, 1000, 0));

            // Consumir item
            if (item != null)
                player.getInventory().destroyItem(item, 1);

            // Desconectar para recarregar completamente
            new Thread(() -> {
                try
                {
                    Thread.sleep(2000);
                    player.deleteMe();
                    if (player.getClient() != null)
                        player.getClient().closeNow();
                }
                catch (InterruptedException e) { e.printStackTrace(); }
            }).start();

        }
        catch (Exception e)
        {
            player.sendMessage("An error occurred while changing your class.");
            e.printStackTrace();
        }
    }

    // ==================== Troca de Nome ====================
    public static void processNameChange(Player player, String newName, ItemInstance item)
    {
        if (player == null || newName == null || newName.isEmpty())
            return;

        // Validação simples: letras e números, 2-16 caracteres
        if (!newName.matches("[A-Za-z0-9]{2,16}"))
        {
            player.sendMessage("Invalid name. Only letters and numbers, 2-16 characters.");
            return;
        }

        try (Connection con = ConnectionPool.getConnection();
             PreparedStatement st = con.prepareStatement(
                     "UPDATE characters SET char_name = ? WHERE obj_Id = ?"))
        {
            st.setString(1, newName);
            st.setInt(2, player.getObjectId());
            st.executeUpdate();

            // Atualizar o nome no objeto Player
            player.setName(newName);
            player.sendMessage("Your name has been changed to " + newName + "!");

            // Consumir o item
            if (item != null)
                player.getInventory().destroyItem(item, 1);

            // Desconectar para atualizar completamente
            new Thread(() -> {
                try
                {
                    Thread.sleep(2000);
                    player.deleteMe();
                    if (player.getClient() != null)
                        player.getClient().closeNow();
                }
                catch (InterruptedException e) { e.printStackTrace(); }
            }).start();
        }
        catch (Exception e)
        {
            player.sendMessage("An error occurred while changing your name.");
            e.printStackTrace();
        }
    }
}
