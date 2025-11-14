package net.sf.l2j.gameserver.instancemanager.custom;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import net.sf.l2j.commons.pool.ThreadPool;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Player;

/**
 * HwidManager minimal — compatível com aCis 409.
 * - conta clientes por HWID (multibox)
 * - kica clientes que ultrapassam o limite (kick only)
 * - monitora farming por HWID e kica jogadores próximos do mesmo HWID
 *
 * Observações:
 * - Usa Config.HWID_MULTIBOX_PROTECTION_CLIENTS_PER_PC,
 *   Config.FARM_PROTECT_RADIUS e Config.FARM_PROTECT_RADIUS_VALUE
 * - Não faz bans, apenas fecha a conexão do GameClient
 */
public final class HwidManager
{
    private static final Logger _log = Logger.getLogger(HwidManager.class.getName());

    // Mapa HWID -> último Player (para proteção de farm)
    private final Map<String, Player> _checkHwidFarm = new ConcurrentHashMap<>();

    private HwidManager()
    {
        _log.info("HwidManager loaded.");
    }

    public static HwidManager getInstance()
    {
        return SingletonHolder._instance;
    }

    private static class SingletonHolder
    {
        private static final HwidManager _instance = new HwidManager();
    }

    /**
     * Conta quantos players ativos há com esse hwid.
     * @param hwid 
     * @return 
     */
    private int countByHwid(String hwid)
    {
        if (hwid == null) return 0;

        int count = 0;
        Collection<Player> all = World.getInstance().getPlayers();
        for (Player p : all)
        {
            if (p == null) continue;
            if (p.getClient() == null) continue;
            String ph = p.getClient().getHWID();
            if (hwid.equals(ph) && !p.getClient().isDetached())
                count++;
        }
        return count;
    }

    /**
     * Valida multibox para um jogador ativo.
     * Se exceder e forcedKick = true => kica (fecha client) o activeChar.
     *
     * @param activeChar jogador a validar
     * @param forcedKick se true, fecha a conexão do jogador quando exceder o limite
     * @return true se excede o limite, false caso contrário
     */
    public boolean validateBox(Player activeChar, boolean forcedKick)
    {
        if (activeChar == null) return false;
        if (activeChar.getClient() == null) return false;

        String hwid = activeChar.getClient().getHWID();
        if (hwid == null) return false;

        int limit = Config.HWID_MULTIBOX_PROTECTION_CLIENTS_PER_PC;
        int count = countByHwid(hwid);

        if (count > limit)
        {
            _log.warning("Multibox protection: HWID=" + hwid + " has " + count + " clients (limit " + limit + ").");
            if (forcedKick)
            {
                try
                {
                    activeChar.sendMessage("You exceeded the allowed number of clients per PC (" + limit + "). You will be disconnected.");
                }
                catch (Exception ignore) { }

                // kick (close client) — apenas kick conforme pedido
                if (activeChar.getClient() != null)
                {
                    try
                    {
                        activeChar.getClient().closeNow();
                    }
                    catch (Exception e)
                    {
                        _log.warning("Couldn't close client for player " + activeChar.getName() + ": " + e.getMessage());
                    }
                }
            }
            return true;
        }

        return false;
    }

    /**
     * Proteção de farm por HWID.
     * - mantém mapa hwid -> player
     * - se outro player com mesmo HWID estiver muito próximo (radius), ele será kicado
     *
     * @param player jogador que pediu ação
     * @return true sempre que o player é aceito (a lógica de kicagem é interna). Retorna false só se player for nulo.
     */
    public boolean checkFarmPlayer(final Player player)
    {
        if (player == null) return false;
        if (player.getClient() == null) return true;

        final String hwid = player.getClient().getHWID();
        if (hwid == null) return true;

        // se já temos um player registrado com esse HWID
        if (_checkHwidFarm.containsKey(hwid))
        {
            final Player registered = _checkHwidFarm.get(hwid);

            // opção: kicar jogadores próximos com mesmo HWID (se habilitado no Config)
            if (Config.FARM_PROTECT_RADIUS)
            {
                final int radius = Math.max(0, Config.FARM_PROTECT_RADIUS_VALUE);
                final int radiusSq = radius * radius;

                // percorre todos os players do mundo (precaução — a implementação 409 nem sempre tem método "getKnownPlayersInRadius")
                for (Player nearby : World.getInstance().getPlayers())
                {
                    if (nearby == null) continue;
                    if (nearby.getClient() == null) continue;
                    if (nearby == player) continue;

                    String nh = nearby.getClient().getHWID();
                    if (nh == null) continue;
                    if (!nh.equals(hwid)) continue;

                    // calcula distância quadrada (x,y)
                    int dx = player.getX() - nearby.getX();
                    int dy = player.getY() - nearby.getY();
                    int dz = player.getZ() - nearby.getZ();
                    int distSq = dx * dx + dy * dy + dz * dz;

                    if (distSq <= radiusSq)
                    {
                        try
                        {
                            nearby.sendMessage("You cannot use multibox to farm. You will be disconnected.");
                        }
                        catch (Exception ignore) { }

                        // schedule kick em 1s para evitar problemas de thread
                        ThreadPool.schedule(() ->
                        {
                            try
                            {
                                if (nearby.getClient() != null)
                                    nearby.getClient().closeNow();
                            }
                            catch (Exception e)
                            {
                                _log.warning("Failed to close client (farm protection) for " + nearby.getName() + ": " + e.getMessage());
                            }
                        }, 1000);
                    }
                }
            }

            // se jogador registrado está online, permite somente se for o mesmo objeto
            if (registered.isOnline())
            {
                return registered == player;
            }
            else
            {
                // substitui registro
                _checkHwidFarm.put(hwid, player);
                return true;
            }
        }
        else
        {
            _checkHwidFarm.put(hwid, player);
            return true;
        }
    }

    /**
     * Remove um HWID do mapa de farm (chamar ao desconectar/limpar se quiser).
     */
    public void removePlayer(String hwid)
    {
        if (hwid == null) return;
        _checkHwidFarm.remove(hwid);
    }
}
