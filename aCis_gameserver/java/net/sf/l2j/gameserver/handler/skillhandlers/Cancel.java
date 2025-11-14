package net.sf.l2j.gameserver.handler.skillhandlers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.sf.l2j.commons.random.Rnd;

import net.sf.l2j.gameserver.enums.items.ShotType;
import net.sf.l2j.gameserver.enums.skills.EffectType;
import net.sf.l2j.gameserver.enums.skills.SkillType;
import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.skills.AbstractEffect;
import net.sf.l2j.gameserver.skills.Formulas;
import net.sf.l2j.gameserver.skills.L2Skill;
import net.sf.l2j.gameserver.util.SpecialModCancelRestore;

public class Cancel implements ISkillHandler
{
    private static final SkillType[] SKILL_IDS =
    {
        SkillType.CANCEL,
        SkillType.MAGE_BANE,
        SkillType.WARRIOR_BANE
    };

    @Override
    public void useSkill(Creature creature, L2Skill skill, WorldObject[] targets, ItemInstance item)
    {
        final int minRate = (skill.getSkillType() == SkillType.CANCEL) ? 25 : 40;
        final int maxRate = (skill.getSkillType() == SkillType.CANCEL) ? 75 : 95;
        final double skillPower = skill.getPower();

        for (WorldObject obj : targets)
        {
            if (!(obj instanceof Creature target))
                continue;

            if (target.isDead())
                continue;

            int count = skill.getMaxNegatedEffects();
            final int diffLevel = skill.getMagicLevel() - target.getStatus().getLevel();
            final double skillVuln = Formulas.calcSkillVulnerability(creature, target, skill, skill.getSkillType());

            final List<AbstractEffect> list = Arrays.asList(target.getAllEffects());
            Collections.shuffle(list);

            final List<L2Skill> cancelledBuffs = new ArrayList<>();

            for (AbstractEffect effect : list)
            {
                if (effect.getSkill().isToggle() || effect.getSkill().isDebuff())
                    continue;

                if (EffectType.isntCancellable(effect.getEffectType()))
                    continue;

                // Mage & Warrior Bane rules
                switch (skill.getSkillType())
                {
                    case MAGE_BANE:
                        if ("casting_time_down".equalsIgnoreCase(effect.getTemplate().getStackType()) ||
                            "ma_up".equalsIgnoreCase(effect.getTemplate().getStackType()))
                            break;
                        else
                            continue;

                    case WARRIOR_BANE:
                        if ("attack_time_down".equalsIgnoreCase(effect.getTemplate().getStackType()) ||
                            "speed_up".equalsIgnoreCase(effect.getTemplate().getStackType()))
                            break;
                        else
                            continue;
                }

                // Calcula chance de cancelamento
                if (calcCancelSuccess(effect.getPeriod(), diffLevel, skillPower, skillVuln, minRate, maxRate))
                {
                    cancelledBuffs.add(effect.getSkill());
                    effect.exit();

                    count--;
                    if (count == 0)
                        break;
                }
            }

            // Restaurar buffs cancelados (tempo inicial do skill)
            if (!cancelledBuffs.isEmpty() && SpecialModCancelRestore.CANCEL_RESTORE)
            {
                if (target instanceof Player player && !player.isInOlympiadMode())
                {
                    SpecialModCancelRestore.scheduleBuffRestore(player, cancelledBuffs);
                    player.sendMessage("Some of your buffs were cancelled, but will be restored in " +
                        (SpecialModCancelRestore.CUSTOM_CANCEL_TASK_DELAY / 1000) + " seconds!");
                }
            }

            // Mensagens de feedback
            if (target instanceof Player targetPlayer)
            {
                if (cancelledBuffs.isEmpty())
                    targetPlayer.sendMessage("Your buffs were not affected by " + skill.getName() + ".");
                else
                    targetPlayer.sendMessage("Your buffs were cancelled by " + creature.getName() + " using " + skill.getName() + ".");
            }
        }

        // Efeitos em self
        if (skill.hasSelfEffects())
        {
            final AbstractEffect effect = creature.getFirstEffect(skill.getId());
            if (effect != null && effect.isSelfEffect())
                effect.exit();

            skill.getEffectsSelf(creature);
        }

        creature.setChargedShot(
            creature.isChargedShot(ShotType.BLESSED_SPIRITSHOT) ? ShotType.BLESSED_SPIRITSHOT : ShotType.SPIRITSHOT,
            skill.isStaticReuse()
        );
    }

    private static boolean calcCancelSuccess(int effectPeriod, int diffLevel, double baseRate, double vuln, int minRate, int maxRate)
    {
        double rate = (2 * diffLevel + baseRate + effectPeriod / 120) * vuln;
        return Rnd.get(100) < Math.clamp(rate, minRate, maxRate);
    }

    @Override
    public SkillType[] getSkillIds()
    {
        return SKILL_IDS;
    }
}
