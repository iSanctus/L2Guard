package net.sf.l2j.gameserver.model.actor.ai.type;

import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.ClanHallManagerNpc;
import net.sf.l2j.gameserver.model.residence.clanhall.ClanHall;
import net.sf.l2j.gameserver.model.residence.clanhall.ClanHallFunction;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.skills.L2Skill;

public class ClanHallManagerNpcAI extends NpcAI<ClanHallManagerNpc>
{
	private long _lastBuffCheckTime = 0;
	
	public ClanHallManagerNpcAI(ClanHallManagerNpc clanHallManager)
	{
		super(clanHallManager);
	}
	
	@Override
	public void thinkIdle()
	{
		// Handle auto buff for support magic function (MP, MP Reg)
		if (System.currentTimeMillis() - _lastBuffCheckTime > 300000)
		{
			_lastBuffCheckTime = System.currentTimeMillis();
			L2Skill supportMagicSkill = SkillTable.getInstance().getInfo(4367, 1);
			final ClanHallFunction chfSM = _actor.getClanHall().getFunction(ClanHall.FUNC_SUPPORT_MAGIC);
			if (chfSM != null)
				supportMagicSkill = SkillTable.getInstance().getInfo(4366 + chfSM.getLvl(), 1);
			
			supportMagicSkill.getEffects(_actor, _actor);
		}
	}
	
	@Override
	protected void thinkCast()
	{
		if (_currentIntention.getFinalTarget().getActingPlayer() == null)
		{
			super.thinkCast();
			return;
		}
		
		final L2Skill skill = _currentIntention.getSkill();
		
		if (_actor.isSkillDisabled(skill))
			return;
		
		final Player player = (Player) _currentIntention.getFinalTarget();
		
		final NpcHtmlMessage html = new NpcHtmlMessage(_actor.getObjectId());
		if (_actor.getStatus().getMp() < skill.getMpConsume() + skill.getMpInitialConsume())
			html.setFile("data/html/clanHallManager/support-no_mana.htm");
		else
		{
			super.thinkCast();
			
			html.setFile("data/html/clanHallManager/support-done.htm");
		}
		
		html.replace("%mp%", (int) _actor.getStatus().getMp());
		html.replace("%objectId%", _actor.getObjectId());
		player.sendPacket(html);
	}
	
	public void resetBuffCheckTime()
	{
		_lastBuffCheckTime = 0;
	}
}