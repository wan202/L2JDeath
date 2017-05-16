package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.commons.concurrent.ThreadPool;

import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.ai.CtrlIntention;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.MoveToPawn;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public class SiegeFlag extends Npc
{
	private final L2Clan _clan;
	
	public SiegeFlag(Player player, int objectId, NpcTemplate template)
	{
		super(objectId, template);
		
		_clan = player.getClan();
		
		// Player clan became null during flag initialization ; don't bother setting clan flag.
		if (_clan != null)
			_clan.setFlag(this);
		
		setIsInvul(false);
	}
	
	@Override
	public boolean isAttackable()
	{
		return !isInvul();
	}
	
	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		return !isInvul();
	}
	
	@Override
	public boolean doDie(Creature killer)
	{
		if (!super.doDie(killer))
			return false;
		
		// Reset clan flag to null.
		if (_clan != null)
			_clan.setFlag(null);
		
		return true;
	}
	
	@Override
	public void onForcedAttack(Player player)
	{
		onAction(player);
	}
	
	@Override
	public void onAction(Player player)
	{
		// Set the target of the Player player
		if (player.getTarget() != this)
			player.setTarget(this);
		else
		{
			if (isAutoAttackable(player) && Math.abs(player.getZ() - getZ()) < 100)
				player.getAI().setIntention(CtrlIntention.ATTACK, this);
			else
			{
				// Rotate the player to face the instance
				player.sendPacket(new MoveToPawn(player, this, Npc.INTERACTION_DISTANCE));
				
				// Send ActionFailed to the player in order to avoid he stucks
				player.ActionF();
			}
		}
	}
	
	@Override
	public void reduceCurrentHp(double damage, Creature attacker, L2Skill skill)
	{
		// Send warning to owners of headquarters that theirs base is under attack.
		if (_clan != null && isScriptValue(0))
		{
			_clan.broadcastToOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.BASE_UNDER_ATTACK));
			
			setScriptValue(1);
			ThreadPool.schedule(() -> setScriptValue(0), 20000);
		}
		super.reduceCurrentHp(damage, attacker, skill);
	}
	
	@Override
	public void addFuncsToNewCharacter()
	{
	}
}