package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.datatables.HennaTable;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.item.Henna;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;

public final class RequestHennaEquip extends L2GameClientPacket
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
		
		final Henna henna = HennaTable.getInstance().getTemplate(_symbolId);
		if (henna == null)
			return;
		
		if (!henna.isForThisClass(activeChar))
		{
			activeChar.sendPacket(SystemMessageId.CANT_DRAW_SYMBOL);
			return;
		}
		
		if (activeChar.getHennaEmptySlots() == 0)
		{
			activeChar.sendPacket(SystemMessageId.SYMBOLS_FULL);
			return;
		}
		
		final ItemInstance ownedDyes = activeChar.getInventory().getItemByItemId(henna.getDyeId());
		final int count = (ownedDyes == null) ? 0 : ownedDyes.getCount();
		
		if (count < Henna.getAmountDyeRequire())
		{
			activeChar.sendPacket(SystemMessageId.CANT_DRAW_SYMBOL);
			return;
		}
		
		// reduceAdena sends a message.
		if (!activeChar.reduceAdena("Henna", henna.getPrice(), activeChar.getCurrentFolkNPC(), true))
			return;
		
		// destroyItemByItemId sends a message.
		if (!activeChar.destroyItemByItemId("Henna", henna.getDyeId(), Henna.getAmountDyeRequire(), activeChar, true))
			return;
		
		activeChar.addHenna(henna);
	}
}