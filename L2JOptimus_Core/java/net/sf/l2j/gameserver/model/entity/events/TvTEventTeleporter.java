package net.sf.l2j.gameserver.model.entity.events;

import net.sf.l2j.commons.concurrent.ThreadPool;
import net.sf.l2j.commons.random.Rnd;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.actor.Summon;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.entity.Duel.DuelState;

public class TvTEventTeleporter implements Runnable
{
	private Player _playerInstance = null;
	private int[] _coordinates = new int[3];
	private boolean _adminRemove = false;
	
	/**
	 * Initialize the teleporter and start the delayed task<br>
	 * <br>
	 * @param playerInstance as Player<br>
	 * @param coordinates as int[]<br>
	 * @param fastSchedule as boolean<br>
	 * @param adminRemove as boolean<br>
	 */
	public TvTEventTeleporter(Player playerInstance, int[] coordinates, boolean fastSchedule, boolean adminRemove)
	{
		_playerInstance = playerInstance;
		_coordinates = coordinates;
		_adminRemove = adminRemove;
		
		long delay = (TvTEvent.isStarted() ? Config.TVT_EVENT_RESPAWN_TELEPORT_DELAY : Config.TVT_EVENT_START_LEAVE_TELEPORT_DELAY) * 1000;
		
		ThreadPool.schedule(this, fastSchedule ? 0 : delay);
	}
	
	/**
	 * The task method to teleport the player<br>
	 * 1. Unsummon pet if there is one<br>
	 * 2. Remove all effects<br>
	 * 3. Revive and full heal the player<br>
	 * 4. Teleport the player<br>
	 * 5. Broadcast status and user info<br>
	 * <br>
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run()
	{
		if (_playerInstance == null)
			return;
		
		Summon summon = _playerInstance.getPet();
		
		if (summon != null)
			summon.unSummon(_playerInstance);
		
		if (Config.TVT_EVENT_EFFECTS_REMOVAL == 0 || (Config.TVT_EVENT_EFFECTS_REMOVAL == 1 && (_playerInstance.getTeam() == 0 || (_playerInstance.isInDuel() && _playerInstance.getDuelState() != DuelState.INTERRUPTED))))
			_playerInstance.stopAllEffectsExceptThoseThatLastThroughDeath();
		
		if (_playerInstance.isInDuel())
			_playerInstance.setDuelState(DuelState.INTERRUPTED);
		
		_playerInstance.doRevive();
		
		_playerInstance.teleToLocation(_coordinates[0] + Rnd.get(101) - 50, _coordinates[1] + Rnd.get(101) - 50, _coordinates[2], 0);
		
		if (TvTEvent.isStarted() && !_adminRemove)
			_playerInstance.setTeam(TvTEvent.getParticipantTeamId(_playerInstance.getObjectId()) + 1);
		else
			_playerInstance.setTeam(0);
		
		_playerInstance.setCurrentCp(_playerInstance.getMaxCp());
		_playerInstance.setCurrentHp(_playerInstance.getMaxHp());
		_playerInstance.setCurrentMp(_playerInstance.getMaxMp());
		
		_playerInstance.broadcastStatusUpdate();
		_playerInstance.broadcastUserInfo();
	}
}
