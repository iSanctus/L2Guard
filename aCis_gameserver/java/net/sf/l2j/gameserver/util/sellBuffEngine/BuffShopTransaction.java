package net.sf.l2j.gameserver.util.sellBuffEngine;

import java.util.Objects;

import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.enums.skills.SkillTargetType;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.Summon;
import net.sf.l2j.gameserver.model.olympiad.OlympiadManager;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUse;
import net.sf.l2j.gameserver.network.serverpackets.SkillList;
import net.sf.l2j.gameserver.skills.L2Skill;
import net.sf.l2j.gameserver.util.sellBuffEngine.ShopObject.PrivateBuff;

/**
 * Representa uma �nica transa��o de compra de buff.
 */
public final class BuffShopTransaction
{
	private final Player buyer;
	private final Player sellerNpc;
	private final PrivateBuff buffToSell;
	private final L2Skill buffSkill;
	private final String targetTypeName;
	private Creature actualTarget;
	
	public BuffShopTransaction(Player buyer, Player sellerNpc, ShopObject shop, int buffId, int buffLevel, String targetTypeName)
	{
		this.buyer = Objects.requireNonNull(buyer, "Buyer n�o pode ser nulo.");
		this.sellerNpc = Objects.requireNonNull(sellerNpc, "SellerNpc n�o pode ser nulo.");
		this.targetTypeName = targetTypeName;
		this.buffToSell = shop.getBuff(buffId);
		this.buffSkill = (this.buffToSell != null) ? SkillTable.getInstance().getInfo(buffId, buffLevel) : null;
	}
	
	public boolean execute()
	{
		if (!validateInitialState())
			return false;
		if (!applyEffectAndConsumeResources())
			return false;
		processShopPayment();
		return true;
	}
	
	/**
	 * Valida��es preliminares que n�o envolvem consumo.
	 */
	private boolean validateInitialState()
	{
		if (buffToSell == null || buffSkill == null)
		{
			buyer.sendMessage("Este buff n�o est� mais dispon�vel.");
			return false;
		}
		
		if (OlympiadManager.getInstance().isRegistered(buyer) || buyer.isDead() || buyer.isInCombat())
		{
			buyer.sendMessage("Voc� n�o pode comprar buffs neste estado.");
			return false;
		}
		
		if (buyer.getAdena() < buffToSell.price())
		{
			buyer.sendMessage("Voc� n�o tem adena suficiente.");
			return false;
		}
		
		return validateAndSetTarget() && validatePurchaseRestrictions();
	}
	
	private boolean validateAndSetTarget()
	{
		if ("pet".equalsIgnoreCase(targetTypeName))
		{
			final Summon pet = buyer.getSummon();
			if (pet == null || pet.isDead())
			{
				buyer.sendMessage("Voc� n�o possui um pet/summon ativo para receber este buff.");
				return false;
			}
			this.actualTarget = pet;
		}
		else
		{
			this.actualTarget = buyer;
		}
		
		// Valida��es de alvo...
		if (BuffShopConfigs.BUFFSHOP_ALLOWED_SELF_SKILLS.contains(buffSkill.getId()) && actualTarget != buyer)
		{
			buyer.sendMessage("Esta skill s� pode ser usada em voc�.");
			return false;
		}
		if (buffSkill.getTargetType() == SkillTargetType.SUMMON && actualTarget == buyer)
		{
			buyer.sendMessage("Este buff s� pode ser usado em um pet/summon.");
			return false;
		}
		return true;
	}
	
	private boolean validatePurchaseRestrictions()
	{
		if (BuffShopConfigs.BUFFSHOP_RESTRICTED_SUMMONS.contains(buffSkill.getId()) || BuffShopConfigs.BUFFSHOP_ALLOWED_SELF_SKILLS.contains(buffSkill.getId()))
		{
			if (buyer.getSkillLevel(buffSkill.getId()) > 0)
			{
				buyer.sendMessage("Voc� j� possui esta skill em sua classe original.");
				return false;
			}
		}
		if (BuffShopConfigs.BUFFSHOP_RESTRICTED_SUMMONS.contains(buffSkill.getId()))
		{
			if (!BuffShopConfigs.BUFFSHOP_SUMMON_BUYER_CLASSES.contains(buyer.getClassId()))
			{
				buyer.sendMessage("Sua classe n�o tem permiss�o para comprar esta invoca��o.");
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Cobra o pre�o da loja (Adena) AP�S o sucesso da conjura��o.
	 */
	private void processShopPayment()
	{
		if (buffToSell != null)
		{
			buyer.reduceAdena(buffToSell.price(), true);
		}
	}
	
	/**
	 * Valida custos, consome recursos e aplica o efeito. Retorna true em sucesso.
	 */
	private boolean applyEffectAndConsumeResources()
	{
		boolean isSpecialSkill = BuffShopConfigs.BUFFSHOP_RESTRICTED_SUMMONS.contains(buffSkill.getId()) || BuffShopConfigs.BUFFSHOP_ALLOWED_SELF_SKILLS.contains(buffSkill.getId());
		
		// CAMINHO 1: Summons e Self-Buffs. O comprador paga os custos da skill.
		if (isSpecialSkill)
		{
			boolean success = false;
			buyer.addSkill(buffSkill, false);
			try
			{
				// useSkill() valida e consome MP/itens do buyer, e falha se j� tiver pet.
				
				final int requiredItemId = buffSkill.getItemConsumeId();
				if (requiredItemId > 0)
				{
					if (!buyer.destroyItemByItemId(requiredItemId, buffSkill.getItemConsume(), true))
					{
						buyer.sendMessage("Voc� n�o possui os itens necess�rios.");
						return false;
					}
					
					buffSkill.useSkill(buyer, new Creature[]
					{
						buyer
					});
				}
			}
			finally
			{
				buyer.removeSkill(buffSkill.getId(), false);
				buyer.sendPacket(new SkillList(buyer));
			}
			return true;
		}
		// CAMINHO 2: Buffs normais. O vendedor paga o MP, comprador paga itens.
		else
		{
			if (sellerNpc.getStatus().getMp() < buffSkill.getMpConsume())
			{
				buyer.sendMessage("O vendedor est� sem mana no momento.");
				return false;
			}
			
			final int requiredItemId = buffSkill.getItemConsumeId();
			if (requiredItemId > 0)
			{
				if (!buyer.destroyItemByItemId(requiredItemId, buffSkill.getItemConsume(), true))
				{
					buyer.sendMessage("Voc� n�o possui os itens necess�rios.");
					return false;
				}
			}
			
			sellerNpc.getStatus().reduceMp(buffSkill.getMpConsume());
			buyer.sendPacket(new MagicSkillUse(sellerNpc, actualTarget, buffSkill.getId(), buffSkill.getLevel(), 1500, 1500));
			buffSkill.getEffects(sellerNpc, actualTarget);
			return true;
		}
	}
}