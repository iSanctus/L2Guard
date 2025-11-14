package net.sf.l2j.gameserver.util;

import java.io.FileInputStream;
import java.util.List;
import java.util.Properties;

import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.skills.L2Skill;

/**
 * Sistema de restauração de buffs cancelados pelo Cancel/MageBane/WarriorBane.
 * Lê a configuração do arquivo CustomMods/SpecialMods.ini.
 * Restaura os buffs com o tempo inicial do skill.
 */
public class SpecialModCancelRestore
{
    /** Ativa/desativa a restauração de buffs cancelados */
    public static boolean CANCEL_RESTORE = false;

    /** Delay (em milissegundos) para restaurar os buffs */
    public static int CUSTOM_CANCEL_TASK_DELAY = 10000;

    static
    {
        try (FileInputStream fis = new FileInputStream("config/CustomMods/SpecialMods.ini"))
        {
            Properties props = new Properties();
            props.load(fis);

            CANCEL_RESTORE = Boolean.parseBoolean(props.getProperty("CancelRestore", "false"));
            CUSTOM_CANCEL_TASK_DELAY = Integer.parseInt(props.getProperty("CustomCancelTaskDelay", "10000"));

            System.out.println("[SpecialModCancelRestore] CancelRestore: " + CANCEL_RESTORE +
                               ", Delay: " + CUSTOM_CANCEL_TASK_DELAY + "ms");
        }
        catch (Exception e)
        {
            System.err.println("[SpecialModCancelRestore] Erro ao ler SpecialMods.ini: " + e.getMessage());
        }
    }

    /**
     * Agenda a restauração de buffs para o player.
     * @param player Player alvo
     * @param buffsCanceled Lista de buffs cancelados
     */
    public static void scheduleBuffRestore(Player player, List<L2Skill> buffsCanceled)
    {
        if (!CANCEL_RESTORE || player == null || buffsCanceled == null || buffsCanceled.isEmpty())
            return;

        ThreadPool.schedule(() -> restoreBuffs(player, buffsCanceled), CUSTOM_CANCEL_TASK_DELAY);
    }

    /**
     * Restaura os buffs cancelados no player.
     * O tempo será o padrão do skill, já que a 409 não permite ajustar o tempo restante.
     * @param player Player alvo
     * @param buffsCanceled Lista de buffs cancelados
     */
    private static void restoreBuffs(Player player, List<L2Skill> buffsCanceled)
    {
        if (player == null || !player.isOnline() || buffsCanceled.isEmpty())
            return;

        for (L2Skill skill : buffsCanceled)
        {
            if (skill == null)
                continue;

            try
            {
                // Evita reaplicar se o buff já estiver ativo
                if (player.getFirstEffect(skill.getId()) != null)
                    continue;

                // Reaplica o buff normalmente
                skill.getEffects(player, player);

                // Toca som para o jogador ao restaurar
                player.sendPacket(new net.sf.l2j.gameserver.network.serverpackets.PlaySound("ItemSound.quest_middle"));

            }
            catch (Exception e)
            {
                System.err.println("[CancelRestore] Erro ao restaurar buff: " +
                                   (skill != null ? skill.getName() : "null") +
                                   " para " + player.getName());
                e.printStackTrace();
            }
        }
    }
}
