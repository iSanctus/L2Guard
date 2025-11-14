package net.sf.l2j.gameserver.handler.itemhandlers.custom.special;

import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.skills.L2Skill;

public class ItemClan implements IItemHandler {

    private final int reputation = 5000;
    private final byte level = 8;
    private final int[] clanSkills = new int[]{
        370, 371, 372, 373, 374, 375, 376, 377, 378, 379,
        380, 381, 382, 383, 384, 385, 386, 387, 388, 389,
        390, 391
    };

    @Override
    public void useItem(Playable playable, ItemInstance item, boolean forceUse) {
        if (!(playable instanceof Player))
            return;

        Player player = (Player) playable;

        if (!player.isClanLeader()) {
            player.sendMessage("You are not the clan leader.");
            return;
        }

        if (player.getClan().getLevel() >= level) {
            player.sendMessage("Your clan is already maximum level!");
            return;
        }

        // Atualiza nível e reputação do clã
        player.getClan().changeLevel(level);
        player.getClan().addReputationScore(reputation);

        // Adiciona skills do clã
        for (int skillId : clanSkills) {
            L2Skill skill = SkillTable.getInstance().getInfo(skillId, SkillTable.getInstance().getMaxLevel(skillId));
            if (skill != null) {
                player.getClan().addClanSkill(skill, true);
            }
        }

        // Atualiza clã no DB
        player.getClan().updateClanInDB();

        // Mensagem de sucesso
        player.sendMessage("Your clan Level, Skills, and Reputation have been updated!");

        // Remove o item corretamente na 409
        player.destroyItem(item, 1, true); // 1 = quantidade, true = envia mensagem
    }
}
