package net.sf.l2j.gameserver.handler.admincommandhandlers;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.entity.events.DMEvent;
import net.sf.l2j.gameserver.model.entity.events.DMEventTeleporter;
import net.sf.l2j.gameserver.model.entity.events.DMManager;

/**
 * @author L0ngh0rn
 */
public class AdminDMEvent implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_dm_add",
		"admin_dm_remove",
		"admin_dm_advance"
	};
	
	@Override
	public boolean useAdminCommand(String command, Player activeChar)
	{
		if (command.equals("admin_dm_add"))
		{
			WorldObject target = activeChar.getTarget();
			
			if (!(target instanceof Player))
			{
				activeChar.sendMessage("You should select a player!");
				return true;
			}
			
			add(activeChar, (Player) target);
		}
		else if (command.equals("admin_dm_remove"))
		{
			WorldObject target = activeChar.getTarget();
			
			if (!(target instanceof Player))
			{
				activeChar.sendMessage("You should select a player!");
				return true;
			}
			
			remove(activeChar, (Player) target);
		}
		else if (command.equals("admin_dm_advance"))
		{
			DMManager.getInstance().skipDelay();
		}
		
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
	private static void add(Player activeChar, Player playerInstance)
	{
		if (DMEvent.isPlayerParticipant(playerInstance))
		{
			activeChar.sendMessage("Player already participated in the event!");
			return;
		}
		
		if (!DMEvent.addParticipant(playerInstance))
		{
			activeChar.sendMessage("Player instance could not be added, it seems to be null!");
			return;
		}
		
		if (DMEvent.isStarted())
		{
			new DMEventTeleporter(playerInstance, true, false);
		}
	}
	
	private static void remove(Player activeChar, Player playerInstance)
	{
		if (!DMEvent.removeParticipant(playerInstance))
		{
			activeChar.sendMessage("Player is not part of the event!");
			return;
		}
		
		new DMEventTeleporter(playerInstance, Config.DM_EVENT_PARTICIPATION_NPC_COORDINATES, true, true);
	}
}
