package net.sf.l2j.gameserver.handler.admincommandhandlers;

import net.sf.l2j.gameserver.datatables.AnnouncementTable;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.instance.Player;

/**
 * This class handles following admin commands:
 * <ul>
 * <li>announce list|all|all_auto|add|add_auto|del : announcement management.</li>
 * <li>ann : announces to all players (basic usage).</li>
 * <li>say : critical announces to all players.</li>
 * </ul>
 */
public class AdminAnnouncements implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_announce",
		"admin_ann",
		"admin_say"
	};
	
	@Override
	public boolean useAdminCommand(String command, Player activeChar)
	{
		if (command.startsWith("admin_announce"))
		{
			try
			{
				final String[] tokens = command.split(" ", 3);
				switch (tokens[1])
				{
					case "list":
						AnnouncementTable.getInstance().listAnnouncements(activeChar);
						break;
					
					case "all":
					case "all_auto":
						final boolean isAuto = tokens[1].equalsIgnoreCase("all_auto");
						for (Player player : World.getInstance().getPlayers())
							AnnouncementTable.getInstance().showAnnouncements(player, isAuto);
						
						AnnouncementTable.getInstance().listAnnouncements(activeChar);
						break;
					
					case "add":
						String[] split = tokens[2].split(" ", 2); // boolean string
						boolean crit = Boolean.parseBoolean(split[0]);
						
						if (!AnnouncementTable.getInstance().addAnnouncement(split[1], crit, false, -1, -1, -1))
							activeChar.sendMessage("Invalid //announce message content ; can't be null or empty.");
						
						AnnouncementTable.getInstance().listAnnouncements(activeChar);
						break;
					
					case "add_auto":
						split = tokens[2].split(" ", 6); // boolean boolean int int int string
						crit = Boolean.parseBoolean(split[0]);
						final boolean auto = Boolean.parseBoolean(split[1]);
						final int idelay = Integer.parseInt(split[2]);
						final int delay = Integer.parseInt(split[3]);
						final int limit = Integer.parseInt(split[4]);
						final String msg = split[5];
						
						if (!AnnouncementTable.getInstance().addAnnouncement(msg, crit, auto, idelay, delay, limit))
							activeChar.sendMessage("Invalid //announce message content ; can't be null or empty.");
						
						AnnouncementTable.getInstance().listAnnouncements(activeChar);
						break;
					
					case "del":
						AnnouncementTable.getInstance().delAnnouncement(Integer.parseInt(tokens[2]));
						AnnouncementTable.getInstance().listAnnouncements(activeChar);
						break;
					
					default:
						activeChar.sendMessage("Possible //announce parameters : <list|all|add|add_auto|del>");
						break;
				}
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Possible //announce parameters : <list|all|add|add_auto|del>");
			}
		}
		else if (command.startsWith("admin_ann") || command.startsWith("admin_say"))
			AnnouncementTable.getInstance().handleAnnounce(command, 10, command.startsWith("admin_say"));
		
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}