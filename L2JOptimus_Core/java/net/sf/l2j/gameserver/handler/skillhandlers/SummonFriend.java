package net.sf.l2j.gameserver.handler.skillhandlers;

import net.sf.l2j.commons.math.MathUtil;

import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.entity.events.DMEvent;
import net.sf.l2j.gameserver.model.entity.events.LMEvent;
import net.sf.l2j.gameserver.model.entity.events.TvTEvent;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ConfirmDlg;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.skills.L2SkillType;

/**
 * @authors BiTi, Sami
 */
public class SummonFriend implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.SUMMON_FRIEND
	};
	
	@Override
	public void useSkill(Creature activeChar, L2Skill skill, WorldObject[] targets)
	{
		if (!(activeChar instanceof Player))
			return;
		
		final Player player = (Player) activeChar;
		
		if (!TvTEvent.isInactive() && TvTEvent.isPlayerParticipant(player.getObjectId()) || !DMEvent.isInactive() && DMEvent.isPlayerParticipant(player.getObjectId()) || !LMEvent.isInactive() && LMEvent.isPlayerParticipant(player.getObjectId()))
		{
			player.sendMessage("You can not use this action when it is participating in this event.");
			return;
		}
		// Check player status.
		if (!Player.checkSummonerStatus(player))
			return;
		
		for (WorldObject obj : targets)
		{
			// The target must be a player.
			if (!(obj instanceof Player))
				continue;
			
			// Can't summon yourself.
			final Player target = ((Player) obj);
			if (activeChar == target)
				continue;
			
			// Check target status.
			if (!Player.checkSummonTargetStatus(target, player))
				continue;
			
			// Check target distance.
			if (MathUtil.checkIfInRange(50, activeChar, target, false))
				continue;
			
			// Check target teleport request status.
			if (!target.teleportRequest(player, skill))
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_ALREADY_SUMMONED).addCharName(target));
				continue;
			}
			
			// Send a request for Summon Friend skill.
			if (skill.getId() == 1403)
			{
				final ConfirmDlg confirm = new ConfirmDlg(SystemMessageId.S1_WISHES_TO_SUMMON_YOU_FROM_S2_DO_YOU_ACCEPT.getId());
				confirm.addCharName(player);
				confirm.addZoneName(activeChar.getPosition());
				confirm.addTime(30000);
				confirm.addRequesterId(player.getObjectId());
				target.sendPacket(confirm);
			}
			else
			{
				Player.teleToTarget(target, player, skill);
				target.teleportRequest(null, null);
			}
		}
	}
	
	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}