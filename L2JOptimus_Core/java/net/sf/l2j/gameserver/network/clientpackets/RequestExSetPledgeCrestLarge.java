package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.cache.CrestCache;
import net.sf.l2j.gameserver.cache.CrestCache.CrestType;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.network.SystemMessageId;

public final class RequestExSetPledgeCrestLarge extends L2GameClientPacket
{
	private int _length;
	private byte[] _data;
	
	@Override
	protected void readImpl()
	{
		_length = readD();
		if (_length > 2176)
			return;
		
		_data = new byte[_length];
		readB(_data);
	}
	
	@Override
	protected void runImpl()
	{
		if (_length < 0 || _length > 2176)
			return;
		
		final Player activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		
		final L2Clan clan = activeChar.getClan();
		if (clan == null)
			return;
		
		if (clan.getDissolvingExpiryTime() > System.currentTimeMillis())
		{
			activeChar.sendPacket(SystemMessageId.CANNOT_SET_CREST_WHILE_DISSOLUTION_IN_PROGRESS);
			return;
		}
		
		if ((activeChar.getClanPrivileges() & L2Clan.CP_CL_REGISTER_CREST) != L2Clan.CP_CL_REGISTER_CREST)
		{
			activeChar.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return;
		}
		
		if (_length == 0)
		{
			if (clan.getCrestLargeId() != 0)
			{
				clan.changeLargeCrest(0);
				activeChar.sendPacket(SystemMessageId.CLAN_CREST_HAS_BEEN_DELETED);
			}
		}
		else
		{
			if (clan.getLevel() < 3)
			{
				activeChar.sendPacket(SystemMessageId.CLAN_LVL_3_NEEDED_TO_SET_CREST);
				return;
			}
			
			final int crestId = IdFactory.getInstance().getNextId();
			if (CrestCache.getInstance().saveCrest(CrestType.PLEDGE_LARGE, crestId, _data))
			{
				clan.changeLargeCrest(crestId);
				activeChar.sendPacket(SystemMessageId.CLAN_EMBLEM_WAS_SUCCESSFULLY_REGISTERED);
			}
		}
	}
}