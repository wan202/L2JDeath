package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.ai.CtrlEvent;
import net.sf.l2j.gameserver.model.actor.ai.CtrlIntention;
import net.sf.l2j.gameserver.model.actor.ai.NextAction;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.zone.ZoneId;
import net.sf.l2j.gameserver.model.zone.type.L2MultiZone;
import net.sf.l2j.gameserver.templates.skills.L2SkillType;

public final class RequestMagicSkillUse extends L2GameClientPacket
{
	private int _magicId;
	protected boolean _ctrlPressed;
	protected boolean _shiftPressed;
	
	@Override
	protected void readImpl()
	{
		_magicId = readD(); // Identifier of the used skill
		_ctrlPressed = readD() != 0; // True if it's a ForceAttack : Ctrl pressed
		_shiftPressed = readC() != 0; // True if Shift pressed
	}
	
	@Override
	protected void runImpl()
	{
		// Get the current player
		final Player activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		
		// Get the level of the used skill
		final int level = activeChar.getSkillLevel(_magicId);
		if (level <= 0)
		{
			ActionF();
			return;
		}
		
		// Get the L2Skill template corresponding to the skillID received from the client
		final L2Skill skill = SkillTable.getInstance().getInfo(_magicId, level);
		if (skill == null)
		{
			ActionF();
			_log.warning("No skill found with id " + _magicId + " and level " + level + ".");
			return;
		}
		
		// If Alternate rule Karma punishment is set to true, forbid skill Return to player with Karma
		if (skill.getSkillType() == L2SkillType.RECALL && !Config.KARMA_PLAYER_CAN_TELEPORT && activeChar.getKarma() > 0)
		{
			ActionF();
			return;
		}
		
		// players mounted on pets cannot use any toggle skills
		if (skill.isToggle() && activeChar.isMounted())
		{
			ActionF();
			return;
		}
		
		if (activeChar.isOutOfControl())
		{
			ActionF();
			return;
		}
		
		if (activeChar.isInsideZone(ZoneId.MULTI) && L2MultiZone.isRestrictedSkill(skill.getId()))
		{
			activeChar.sendMessage(skill.getName() + " cannot be used inside multi zone.");
			ActionF();
			return;
		}
		
		if (activeChar.isAttackingNow())
		{
			if (skill.isToggle())
			{
				ActionF();
				return;
			}
			
			activeChar.getAI().setNextAction(new NextAction(CtrlEvent.EVT_READY_TO_ACT, CtrlIntention.CAST, new Runnable()
			{
				@Override
				public void run()
				{
					activeChar.useMagic(skill, _ctrlPressed, _shiftPressed);
				}
			}));
		}
		else
			activeChar.useMagic(skill, _ctrlPressed, _shiftPressed);
	}
}