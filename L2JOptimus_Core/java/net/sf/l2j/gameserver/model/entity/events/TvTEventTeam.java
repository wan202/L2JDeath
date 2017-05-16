package net.sf.l2j.gameserver.model.entity.events;

import java.util.HashMap;
import java.util.Map;

import net.sf.l2j.gameserver.model.actor.instance.Player;

/**
 * @author FBIagent
 */
public class TvTEventTeam
{
	private String _name;
	private int[] _coordinates = new int[3];
	private short _points;
	private Map<Integer, Player> _participatedPlayers = new HashMap<>();
	
	/**
	 * C'tor initialize the team<br>
	 * <br>
	 * @param name as String<br>
	 * @param coordinates as int[]<br>
	 */
	public TvTEventTeam(String name, int[] coordinates)
	{
		_name = name;
		_coordinates = coordinates;
		_points = 0;
	}
	
	/**
	 * Adds a player to the team<br>
	 * <br>
	 * @param playerInstance as Player<br>
	 * @return boolean: true if success, otherwise false<br>
	 */
	public boolean addPlayer(Player playerInstance)
	{
		if (playerInstance == null)
		{
			return false;
		}
		
		synchronized (_participatedPlayers)
		{
			_participatedPlayers.put(playerInstance.getObjectId(), playerInstance);
		}
		
		return true;
	}
	
	/**
	 * Removes a player from the team<br>
	 * <br>
	 * @param playerObjectId as String<br>
	 */
	public void removePlayer(int playerObjectId)
	{
		synchronized (_participatedPlayers)
		{
			_participatedPlayers.remove(playerObjectId);
		}
	}
	
	/**
	 * Increases the points of the team<br>
	 */
	public void increasePoints()
	{
		++_points;
	}
	
	/**
	 * Cleanup the team and make it ready for adding players again<br>
	 */
	public void cleanMe()
	{
		_participatedPlayers.clear();
		_participatedPlayers = new HashMap<>();
		_points = 0;
	}
	
	/**
	 * Is given player in this team?<br>
	 * <br>
	 * @param playerObjectId as String<br>
	 * @return boolean: true if player is in this team, otherwise false<br>
	 */
	public boolean containsPlayer(int playerObjectId)
	{
		boolean containsPlayer;
		
		synchronized (_participatedPlayers)
		{
			containsPlayer = _participatedPlayers.containsKey(playerObjectId);
		}
		
		return containsPlayer;
	}
	
	/**
	 * Returns the name of the team<br>
	 * <br>
	 * @return String: name of the team<br>
	 */
	public String getName()
	{
		return _name;
	}
	
	/**
	 * Returns the coordinates of the team spot<br>
	 * <br>
	 * @return int[]: team coordinates<br>
	 */
	public int[] getCoordinates()
	{
		return _coordinates;
	}
	
	/**
	 * Returns the points of the team<br>
	 * <br>
	 * @return short: team points<br>
	 */
	public short getPoints()
	{
		return _points;
	}
	
	/**
	 * Returns name and instance of all participated players in FastMap<br>
	 * <br>
	 * @return Map<String, Player>: map of players in this team<br>
	 */
	public Map<Integer, Player> getParticipatedPlayers()
	{
		Map<Integer, Player> participatedPlayers = null;
		
		synchronized (_participatedPlayers)
		{
			participatedPlayers = _participatedPlayers;
		}
		
		return participatedPlayers;
	}
	
	/**
	 * Returns player count of this team<br>
	 * <br>
	 * @return int: number of players in team<br>
	 */
	public int getParticipatedPlayerCount()
	{
		int participatedPlayerCount;
		
		synchronized (_participatedPlayers)
		{
			participatedPlayerCount = _participatedPlayers.size();
		}
		
		return participatedPlayerCount;
	}
}
