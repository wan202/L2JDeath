package net.sf.l2j.gameserver.network.serverpackets;

import java.util.List;

import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.entity.Castle;

public class SiegeDefenderList extends L2GameServerPacket
{
	private final Castle _castle;
	
	public SiegeDefenderList(Castle castle)
	{
		_castle = castle;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xcb);
		writeD(_castle.getCastleId());
		writeD(0x00); // 0
		writeD(0x01); // 1
		writeD(0x00); // 0
		
		final List<L2Clan> defenders = _castle.getSiege().getDefenderClans();
		final List<L2Clan> pendingDefenders = _castle.getSiege().getPendingClans();
		final int size = defenders.size() + pendingDefenders.size();
		
		if (size > 0)
		{
			writeD(size);
			writeD(size);
			
			for (L2Clan clan : defenders)
			{
				writeD(clan.getClanId());
				writeS(clan.getName());
				writeS(clan.getLeaderName());
				writeD(clan.getCrestId());
				writeD(0x00); // signed time (seconds) (not storated by L2J)
				
				switch (_castle.getSiege().getSide(clan))
				{
					case OWNER:
						writeD(0x01);
						break;
					
					case PENDING:
						writeD(0x02);
						break;
					
					case DEFENDER:
						writeD(0x03);
						break;
					
					default:
						writeD(0x00);
						break;
				}
				
				writeD(clan.getAllyId());
				writeS(clan.getAllyName());
				writeS(""); // AllyLeaderName
				writeD(clan.getAllyCrestId());
			}
			
			for (L2Clan clan : pendingDefenders)
			{
				writeD(clan.getClanId());
				writeS(clan.getName());
				writeS(clan.getLeaderName());
				writeD(clan.getCrestId());
				writeD(0x00); // signed time (seconds) (not storated by L2J)
				writeD(0x02); // waiting approval
				writeD(clan.getAllyId());
				writeS(clan.getAllyName());
				writeS(""); // AllyLeaderName
				writeD(clan.getAllyCrestId());
			}
		}
		else
		{
			writeD(0x00);
			writeD(0x00);
		}
	}
}