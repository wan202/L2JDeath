package net.sf.l2j.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;

import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.item.kind.Item;

public class SellList extends L2GameServerPacket
{
	private final Player _activeChar;
	private final int _money;
	private final List<ItemInstance> _selllist = new ArrayList<>();
	
	public SellList(Player player)
	{
		_activeChar = player;
		_money = _activeChar.getAdena();
	}
	
	@Override
	public final void runImpl()
	{
		for (ItemInstance item : _activeChar.getInventory().getItems())
		{
			if (!item.isEquipped() && item.isSellable() && (_activeChar.getPet() == null || item.getObjectId() != _activeChar.getPet().getControlItemId()))
				_selllist.add(item);
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x10);
		writeD(_money);
		writeD(0x00);
		writeH(_selllist.size());
		
		for (ItemInstance temp : _selllist)
		{
			if (temp == null || temp.getItem() == null)
				continue;
			
			Item item = temp.getItem();
			
			writeH(item.getType1());
			writeD(temp.getObjectId());
			writeD(temp.getItemId());
			writeD(temp.getCount());
			writeH(item.getType2());
			writeH(temp.getCustomType1());
			writeD(item.getBodyPart());
			writeH(temp.getEnchantLevel());
			writeH(temp.getCustomType2());
			writeH(0x00);
			writeD(item.getReferencePrice() / 2);
		}
	}
}