package net.sf.l2j.gameserver.model.vehicles;

import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2j.commons.concurrent.ThreadPool;

import net.sf.l2j.gameserver.instancemanager.BoatManager;
import net.sf.l2j.gameserver.model.Location;
import net.sf.l2j.gameserver.model.VehiclePathPoint;
import net.sf.l2j.gameserver.model.actor.Vehicle;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.clientpackets.Say2;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.scripting.quests.audio.Sound;

public class BoatGiranTalking implements Runnable
{
	private static final Logger _log = Logger.getLogger(BoatGiranTalking.class.getName());
	
	private static final Location OUST_LOC_1 = new Location(46763, 187041, -3451);
	private static final Location OUST_LOC_2 = new Location(-96777, 258970, -3623);
	
	// Time: 868s
	private static final VehiclePathPoint[] GIRAN_TO_TALKING =
	{
		new VehiclePathPoint(51914, 189023, -3610, 150, 800),
		new VehiclePathPoint(60567, 189789, -3610, 150, 800),
		new VehiclePathPoint(63732, 197457, -3610, 200, 800),
		new VehiclePathPoint(63732, 219946, -3610, 250, 800),
		new VehiclePathPoint(62008, 222240, -3610, 250, 1200),
		new VehiclePathPoint(56115, 226791, -3610, 250, 1200),
		new VehiclePathPoint(40384, 226432, -3610, 300, 800),
		new VehiclePathPoint(37760, 226432, -3610, 300, 800),
		new VehiclePathPoint(27153, 226791, -3610, 300, 800),
		new VehiclePathPoint(12672, 227535, -3610, 300, 800),
		new VehiclePathPoint(-1808, 228280, -3610, 300, 800),
		new VehiclePathPoint(-22165, 230542, -3610, 300, 800),
		new VehiclePathPoint(-42523, 235205, -3610, 300, 800),
		new VehiclePathPoint(-68451, 259560, -3610, 250, 800),
		new VehiclePathPoint(-70848, 261696, -3610, 200, 800),
		new VehiclePathPoint(-83344, 261610, -3610, 200, 800),
		new VehiclePathPoint(-88344, 261660, -3610, 180, 800),
		new VehiclePathPoint(-92344, 261660, -3610, 180, 800),
		new VehiclePathPoint(-94242, 261659, -3610, 150, 800)
	};
	
	private static final VehiclePathPoint[] TALKING_DOCK =
	{
		new VehiclePathPoint(-96622, 261660, -3610, 150, 800)
	};
	
	// Time: 1398s
	private static final VehiclePathPoint[] TALKING_TO_GIRAN =
	{
		new VehiclePathPoint(-113925, 261660, -3610, 150, 800),
		new VehiclePathPoint(-126107, 249116, -3610, 180, 800),
		new VehiclePathPoint(-126107, 234499, -3610, 180, 800),
		new VehiclePathPoint(-126107, 219882, -3610, 180, 800),
		new VehiclePathPoint(-109414, 204914, -3610, 180, 800),
		new VehiclePathPoint(-92807, 204914, -3610, 180, 800),
		new VehiclePathPoint(-80425, 216450, -3610, 250, 800),
		new VehiclePathPoint(-68043, 227987, -3610, 250, 800),
		new VehiclePathPoint(-63744, 231168, -3610, 250, 800),
		new VehiclePathPoint(-60844, 231369, -3610, 250, 1800),
		new VehiclePathPoint(-44915, 231369, -3610, 200, 800),
		new VehiclePathPoint(-28986, 231369, -3610, 200, 800),
		new VehiclePathPoint(8233, 207624, -3610, 200, 800),
		new VehiclePathPoint(21470, 201503, -3610, 180, 800),
		new VehiclePathPoint(40058, 195383, -3610, 180, 800),
		new VehiclePathPoint(43022, 193793, -3610, 150, 800),
		new VehiclePathPoint(45986, 192203, -3610, 150, 800),
		new VehiclePathPoint(48950, 190613, -3610, 150, 800)
	};
	
	private static final VehiclePathPoint GIRAN_DOCK = TALKING_TO_GIRAN[TALKING_TO_GIRAN.length - 1];
	
	private final Vehicle _boat;
	private int _cycle = 0;
	private int _shoutCount = 0;
	
	private final CreatureSay ARRIVED_AT_GIRAN;
	private final CreatureSay ARRIVED_AT_GIRAN_2;
	private final CreatureSay LEAVE_GIRAN5;
	private final CreatureSay LEAVE_GIRAN1;
	private final CreatureSay LEAVE_GIRAN0;
	private final CreatureSay LEAVING_GIRAN;
	private final CreatureSay ARRIVED_AT_TALKING;
	private final CreatureSay ARRIVED_AT_TALKING_2;
	private final CreatureSay LEAVE_TALKING5;
	private final CreatureSay LEAVE_TALKING1;
	private final CreatureSay LEAVE_TALKING0;
	private final CreatureSay LEAVING_TALKING;
	private final CreatureSay BUSY_TALKING;
	
	private final CreatureSay ARRIVAL_TALKING15;
	private final CreatureSay ARRIVAL_TALKING10;
	private final CreatureSay ARRIVAL_TALKING5;
	private final CreatureSay ARRIVAL_TALKING1;
	private final CreatureSay ARRIVAL_GIRAN20;
	private final CreatureSay ARRIVAL_GIRAN15;
	private final CreatureSay ARRIVAL_GIRAN10;
	private final CreatureSay ARRIVAL_GIRAN5;
	private final CreatureSay ARRIVAL_GIRAN1;
	
	public BoatGiranTalking(Vehicle boat)
	{
		_boat = boat;
		
		ARRIVED_AT_GIRAN = new CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_ARRIVED_AT_GIRAN);
		ARRIVED_AT_GIRAN_2 = new CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_LEAVE_FOR_TALKING_AFTER_10_MINUTES);
		LEAVE_GIRAN5 = new CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_LEAVE_FOR_TALKING_IN_5_MINUTES);
		LEAVE_GIRAN1 = new CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_LEAVE_FOR_TALKING_IN_1_MINUTE);
		LEAVE_GIRAN0 = new CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_LEAVE_SOON_FOR_TALKING);
		LEAVING_GIRAN = new CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_LEAVING_FOR_TALKING);
		ARRIVED_AT_TALKING = new CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_ARRIVED_AT_TALKING);
		ARRIVED_AT_TALKING_2 = new CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_LEAVE_FOR_GIRAN_AFTER_10_MINUTES);
		LEAVE_TALKING5 = new CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_LEAVE_FOR_GIRAN_IN_5_MINUTES);
		LEAVE_TALKING1 = new CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_LEAVE_FOR_GIRAN_IN_1_MINUTE);
		LEAVE_TALKING0 = new CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_LEAVE_SOON_FOR_GIRAN);
		LEAVING_TALKING = new CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_LEAVING_FOR_GIRAN);
		BUSY_TALKING = new CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_GIRAN_TALKING_DELAYED);
		
		ARRIVAL_TALKING15 = new CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_FROM_GIRAN_ARRIVE_AT_TALKING_15_MINUTES);
		ARRIVAL_TALKING10 = new CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_FROM_GIRAN_ARRIVE_AT_TALKING_10_MINUTES);
		ARRIVAL_TALKING5 = new CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_FROM_GIRAN_ARRIVE_AT_TALKING_5_MINUTES);
		ARRIVAL_TALKING1 = new CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_FROM_GIRAN_ARRIVE_AT_TALKING_1_MINUTE);
		ARRIVAL_GIRAN20 = new CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_FROM_TALKING_ARRIVE_AT_GIRAN_20_MINUTES);
		ARRIVAL_GIRAN15 = new CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_FROM_TALKING_ARRIVE_AT_GIRAN_15_MINUTES);
		ARRIVAL_GIRAN10 = new CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_FROM_TALKING_ARRIVE_AT_GIRAN_10_MINUTES);
		ARRIVAL_GIRAN5 = new CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_FROM_TALKING_ARRIVE_AT_GIRAN_5_MINUTES);
		ARRIVAL_GIRAN1 = new CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_FROM_TALKING_ARRIVE_AT_GIRAN_1_MINUTE);
	}
	
	@Override
	public void run()
	{
		try
		{
			switch (_cycle)
			{
				case 0:
					BoatManager.getInstance().broadcastPacket(GIRAN_DOCK, TALKING_DOCK[0], LEAVE_GIRAN5);
					ThreadPool.schedule(this, 240000);
					break;
				case 1:
					BoatManager.getInstance().broadcastPacket(GIRAN_DOCK, TALKING_DOCK[0], LEAVE_GIRAN1);
					ThreadPool.schedule(this, 40000);
					break;
				case 2:
					BoatManager.getInstance().broadcastPacket(GIRAN_DOCK, TALKING_DOCK[0], LEAVE_GIRAN0);
					ThreadPool.schedule(this, 20000);
					break;
				case 3:
					BoatManager.getInstance().broadcastPackets(GIRAN_DOCK, TALKING_DOCK[0], LEAVING_GIRAN, ARRIVAL_TALKING15);
					_boat.broadcastPacket(Sound.ITEMSOUND_SHIP_ARRIVAL_DEPARTURE.withObject(_boat));
					_boat.payForRide(3946, 1, OUST_LOC_1);
					_boat.executePath(GIRAN_TO_TALKING);
					ThreadPool.schedule(this, 250000);
					break;
				case 4:
					BoatManager.getInstance().broadcastPacket(TALKING_DOCK[0], GIRAN_DOCK, ARRIVAL_TALKING10);
					ThreadPool.schedule(this, 300000);
					break;
				case 5:
					BoatManager.getInstance().broadcastPacket(TALKING_DOCK[0], GIRAN_DOCK, ARRIVAL_TALKING5);
					ThreadPool.schedule(this, 240000);
					break;
				case 6:
					BoatManager.getInstance().broadcastPacket(TALKING_DOCK[0], GIRAN_DOCK, ARRIVAL_TALKING1);
					break;
				case 7:
					if (BoatManager.getInstance().dockBusy(BoatManager.TALKING_ISLAND))
					{
						if (_shoutCount == 0)
							BoatManager.getInstance().broadcastPacket(TALKING_DOCK[0], GIRAN_DOCK, BUSY_TALKING);
						
						_shoutCount++;
						if (_shoutCount > 35)
							_shoutCount = 0;
						
						ThreadPool.schedule(this, 5000);
						return;
					}
					BoatManager.getInstance().dockShip(BoatManager.TALKING_ISLAND, true);
					_boat.executePath(TALKING_DOCK);
					break;
				case 8:
					BoatManager.getInstance().broadcastPackets(TALKING_DOCK[0], GIRAN_DOCK, ARRIVED_AT_TALKING, ARRIVED_AT_TALKING_2);
					_boat.broadcastPacket(Sound.ITEMSOUND_SHIP_ARRIVAL_DEPARTURE.withObject(_boat));
					ThreadPool.schedule(this, 300000);
					break;
				case 9:
					BoatManager.getInstance().broadcastPacket(TALKING_DOCK[0], GIRAN_DOCK, LEAVE_TALKING5);
					ThreadPool.schedule(this, 240000);
					break;
				case 10:
					BoatManager.getInstance().broadcastPacket(TALKING_DOCK[0], GIRAN_DOCK, LEAVE_TALKING1);
					ThreadPool.schedule(this, 40000);
					break;
				case 11:
					BoatManager.getInstance().broadcastPacket(TALKING_DOCK[0], GIRAN_DOCK, LEAVE_TALKING0);
					ThreadPool.schedule(this, 20000);
					break;
				case 12:
					BoatManager.getInstance().dockShip(BoatManager.TALKING_ISLAND, false);
					BoatManager.getInstance().broadcastPackets(TALKING_DOCK[0], GIRAN_DOCK, LEAVING_TALKING);
					_boat.broadcastPacket(Sound.ITEMSOUND_SHIP_ARRIVAL_DEPARTURE.withObject(_boat));
					_boat.payForRide(3945, 1, OUST_LOC_2);
					_boat.executePath(TALKING_TO_GIRAN);
					ThreadPool.schedule(this, 200000);
					break;
				case 13:
					BoatManager.getInstance().broadcastPacket(GIRAN_DOCK, TALKING_DOCK[0], ARRIVAL_GIRAN20);
					ThreadPool.schedule(this, 300000);
					break;
				case 14:
					BoatManager.getInstance().broadcastPacket(GIRAN_DOCK, TALKING_DOCK[0], ARRIVAL_GIRAN15);
					ThreadPool.schedule(this, 300000);
					break;
				case 15:
					BoatManager.getInstance().broadcastPacket(GIRAN_DOCK, TALKING_DOCK[0], ARRIVAL_GIRAN10);
					ThreadPool.schedule(this, 300000);
					break;
				case 16:
					BoatManager.getInstance().broadcastPacket(GIRAN_DOCK, TALKING_DOCK[0], ARRIVAL_GIRAN5);
					ThreadPool.schedule(this, 240000);
					break;
				case 17:
					BoatManager.getInstance().broadcastPacket(GIRAN_DOCK, TALKING_DOCK[0], ARRIVAL_GIRAN1);
					break;
				case 18:
					BoatManager.getInstance().broadcastPackets(GIRAN_DOCK, TALKING_DOCK[0], ARRIVED_AT_GIRAN, ARRIVED_AT_GIRAN_2);
					_boat.broadcastPacket(Sound.ITEMSOUND_SHIP_ARRIVAL_DEPARTURE.withObject(_boat));
					ThreadPool.schedule(this, 300000);
					break;
			}
			_shoutCount = 0;
			_cycle++;
			if (_cycle > 18)
				_cycle = 0;
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, e.getMessage());
		}
	}
	
	public static void load()
	{
		final Vehicle boat = BoatManager.getInstance().getNewBoat(2, 48950, 190613, -3610, 60800);
		if (boat != null)
		{
			boat.registerEngine(new BoatGiranTalking(boat));
			boat.runEngine(180000);
		}
	}
}
