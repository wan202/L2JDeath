package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.datatables.ClanTable;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.network.SystemMessageId;

public final class RequestStopPledgeWar extends L2GameClientPacket
{
	private String _pledgeName;
	
	@Override
	protected void readImpl()
	{
		_pledgeName = readS();
	}
	
	@Override
	protected void runImpl()
	{
		final Player player = getClient().getActiveChar();
		if (player == null)
			return;
		
		final L2Clan playerClan = player.getClan();
		if (playerClan == null)
			return;
		
		final L2Clan clan = ClanTable.getInstance().getClanByName(_pledgeName);
		if (clan == null)
			return;
		
		if ((player.getClanPrivileges() & L2Clan.CP_CL_PLEDGE_WAR) != L2Clan.CP_CL_PLEDGE_WAR)
		{
			player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return;
		}
		
		if (!playerClan.isAtWarWith(clan.getClanId()))
		{
			player.sendPacket(SystemMessageId.NOT_INVOLVED_IN_WAR);
			return;
		}
		
		for (Player member : playerClan.getOnlineMembers())
		{
			if (member.isInCombat())
			{
				player.sendPacket(SystemMessageId.CANT_STOP_CLAN_WAR_WHILE_IN_COMBAT);
				return;
			}
		}
		
		ClanTable.getInstance().deleteClansWars(playerClan.getClanId(), clan.getClanId());
		
		for (Player member : clan.getOnlineMembers())
			member.broadcastUserInfo();
		
		for (Player member : playerClan.getOnlineMembers())
			member.broadcastUserInfo();
	}
}