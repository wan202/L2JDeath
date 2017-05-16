package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.datatables.HennaTable;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.item.Henna;
import net.sf.l2j.gameserver.network.serverpackets.HennaItemRemoveInfo;

public final class RequestHennaItemRemoveInfo extends L2GameClientPacket
{
	private int _symbolId;
	
	@Override
	protected void readImpl()
	{
		_symbolId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		
		final Henna template = HennaTable.getInstance().getTemplate(_symbolId);
		if (template == null)
			return;
		
		activeChar.sendPacket(new HennaItemRemoveInfo(template, activeChar));
	}
}