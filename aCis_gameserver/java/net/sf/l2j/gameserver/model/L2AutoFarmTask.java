package net.sf.l2j.gameserver.model;

import java.util.concurrent.ScheduledFuture;

import net.sf.l2j.commons.pool.ThreadPool;

import net.sf.l2j.gameserver.enums.ShortcutType;
import net.sf.l2j.gameserver.geoengine.GeoEngine;
import net.sf.l2j.gameserver.handler.usercommandhandlers.Escape;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.Chest;
import net.sf.l2j.gameserver.model.actor.instance.GrandBoss;
import net.sf.l2j.gameserver.model.actor.instance.Monster;
import net.sf.l2j.gameserver.model.actor.instance.RaidBoss;

public class L2AutoFarmTask
{
    private final Player _actor;
    private ScheduledFuture<?> _task;

    public L2AutoFarmTask(Player actor)
    {
        _actor = actor;
    }

    public void start()
    {
        if (_task == null)
            _task = ThreadPool.scheduleAtFixedRate(() -> thinking(), 500, 500);
    }

    private void thinking()
    {
        if (_actor == null || !_actor.isOnline() || _actor.isDead())
        {
            stop();
            return;
        }

        if (_actor.isMoving() || _actor.isAllSkillsDisabled() || _actor.isAttackingNow() || _actor.isOutOfControl())
            return;

        if (forceStopBuffer())
            return;

        Monster monster = findCreature();

        if (monster != null)
        {
            _actor.setTarget(monster);

            // Verifica se o F1 da barra 10 tem o action de attack
            if (hasAttackShortcut())
            {
                // Inicia ataque físico no alvo
                if (!_actor.isAttackingNow())
                    _actor.doAutoAttack(monster);
            }
        }
    }

    public boolean hasAttackShortcut()
    {
        if (_actor == null || _actor.getShortcutList() == null)
            return false;

        for (Shortcut sc : _actor.getShortcutList().getShortcuts())
        {
            // Page 9 = barra 10 (0-based) | Slot 0 = F1
            if (sc.getPage() == 9 && sc.getSlot() == 0 && sc.getType() == ShortcutType.ACTION && sc.getId() == 2)
                return true;
        }
        return false;
    }

    public Monster findCreature()
    {
        Monster closest = null;
        double minDist = Double.MAX_VALUE;

        for (Monster mob : _actor.getKnownTypeInRadius(Monster.class, 3500))
        {
            if (mob == null || mob.isDead() || !GeoEngine.getInstance().canSeeTarget(_actor, mob))
                continue;

            if (mob instanceof RaidBoss || mob instanceof GrandBoss || mob instanceof Chest)
                continue;

            double dist = calculateDistance(mob, false);
            if (dist < minDist)
            {
                minDist = dist;
                closest = mob;
            }
        }
        return closest;
    }

    private double calculateDistance(WorldObject target, boolean includeZ)
    {
        if (target == null)
            return Double.MAX_VALUE;

        final int dx = target.getX() - _actor.getX();
        final int dy = target.getY() - _actor.getY();
        final int dz = target.getZ() - _actor.getZ();

        double dist = dx * dx + dy * dy;
        if (includeZ)
            dist += dz * dz;

        return Math.sqrt(dist);
    }

    private boolean forceStopBuffer()
    {
        final boolean force = running() && _actor.getAllEffects().length <= 8;

        if (force)
        {
            new Escape().useUserCommand(52, _actor);
            stop();
        }
        return force;
    }

    public void stop()
    {
        if (_task != null)
        {
            _task.cancel(false);
            _task = null;
        }

        if (_actor != null)
            _actor.setTarget(null);
    }

    public boolean running()
    {
        return _task != null;
    }
}
