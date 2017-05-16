package net.sf.l2j.gameserver.model.entity.events;

import java.util.Calendar;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

import net.sf.l2j.commons.concurrent.ThreadPool;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.util.Broadcast;

/**
 * @author L0ngh0rn
 */
public class LMManager
{
	protected static final Logger _log = Logger.getLogger(LMManager.class.getName());
	
	private LMStartTask _task;
	
	private LMManager()
	{
		if (Config.LM_EVENT_ENABLED)
		{
			this.scheduleEventStart();
			_log.info("LMEventEngine[LMManager.LMManager()]: Started.");
		}
		else
			_log.info("LMEventEngine[LMManager.LMManager()]: Engine is disabled.");
	}
	
	public static LMManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	@SuppressWarnings("null")
	public void scheduleEventStart()
	{
		try
		{
			Calendar currentTime = Calendar.getInstance();
			Calendar nextStartTime = null;
			Calendar testStartTime = null;
			for (String timeOfDay : Config.LM_EVENT_INTERVAL)
			{
				// Creating a Calendar object from the specified interval value
				testStartTime = Calendar.getInstance();
				testStartTime.setLenient(true);
				String[] splitTimeOfDay = timeOfDay.split(":");
				testStartTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(splitTimeOfDay[0]));
				testStartTime.set(Calendar.MINUTE, Integer.parseInt(splitTimeOfDay[1]));
				testStartTime.set(Calendar.SECOND, 00);
				// If the date is in the past, make it the next day (Example: Checking for "1:00", when the time is 23:57.)
				if (testStartTime.getTimeInMillis() < currentTime.getTimeInMillis())
					testStartTime.add(Calendar.DAY_OF_MONTH, 1);
				// Check for the test date to be the minimum (smallest in the specified list)
				if (nextStartTime == null || testStartTime.getTimeInMillis() < nextStartTime.getTimeInMillis())
					nextStartTime = testStartTime;
			}
			_task = new LMStartTask(nextStartTime.getTimeInMillis());
			ThreadPool.execute(_task);
		}
		catch (Exception e)
		{
			_log.warning("LMEventEngine[LMManager.scheduleEventStart()]: Error figuring out a start time. Check LMEventInterval in config file.");
		}
	}
	
	public void startReg()
	{
		if (!LMEvent.startParticipation())
		{
			Broadcast.announceToOnlinePlayers("LM Event: Event was cancelled.");
			_log.warning("LMEventEngine[LMManager.run()]: Error spawning event npc for participation.");
			
			this.scheduleEventStart();
		}
		else
		{
			Broadcast.announceToOnlinePlayers("LM Event: Registration opened for " + Config.LM_EVENT_PARTICIPATION_TIME + " minute(s).");
			
			// schedule registration end
			_task.setStartTime(System.currentTimeMillis() + 60000L * Config.LM_EVENT_PARTICIPATION_TIME);
			ThreadPool.execute(_task);
		}
	}
	
	public void startEvent()
	{
		if (!LMEvent.startFight())
		{
			Broadcast.announceToOnlinePlayers("LM Event: Event cancelled due to lack of Participation.");
			_log.info("LMEventEngine[LMManager.run()]: Lack of registration, abort event.");
			
			this.scheduleEventStart();
		}
		else
		{
			LMEvent.sysMsgToAllParticipants("LM Event: Teleporting participants to an arena in " + Config.LM_EVENT_START_LEAVE_TELEPORT_DELAY + " second(s).");
			_task.setStartTime(System.currentTimeMillis() + 60000L * Config.LM_EVENT_RUNNING_TIME);
			ThreadPool.execute(_task);
		}
	}
	
	public void endEvent()
	{
		Broadcast.announceToOnlinePlayers(LMEvent.calculateRewards());
		LMEvent.sysMsgToAllParticipants("LM Event: Teleporting back to the registration npc in " + Config.LM_EVENT_START_LEAVE_TELEPORT_DELAY + " second(s).");
		LMEvent.stopFight();
		this.scheduleEventStart();
	}
	
	public void skipDelay()
	{
		if (_task.nextRun.cancel(false))
		{
			_task.setStartTime(System.currentTimeMillis());
			ThreadPool.execute(_task);
		}
	}
	
	class LMStartTask implements Runnable
	{
		private long _startTime;
		public ScheduledFuture<?> nextRun;
		
		public LMStartTask(long startTime)
		{
			_startTime = startTime;
		}
		
		public void setStartTime(long startTime)
		{
			_startTime = startTime;
		}
		
		@Override
		public void run()
		{
			int delay = (int) Math.round((_startTime - System.currentTimeMillis()) / 1000.0);
			
			if (delay > 0)
			{
				this.announce(delay);
			}
			
			int nextMsg = 0;
			if (delay > 3600)
			{
				nextMsg = delay - 3600;
			}
			else if (delay > 1800)
			{
				nextMsg = delay - 1800;
			}
			else if (delay > 900)
			{
				nextMsg = delay - 900;
			}
			else if (delay > 600)
			{
				nextMsg = delay - 600;
			}
			else if (delay > 300)
			{
				nextMsg = delay - 300;
			}
			else if (delay > 60)
			{
				nextMsg = delay - 60;
			}
			else if (delay > 5)
			{
				nextMsg = delay - 5;
			}
			else if (delay > 0)
			{
				nextMsg = delay;
			}
			else
			{
				// start
				if (LMEvent.isInactive())
				{
					LMManager.this.startReg();
				}
				else if (LMEvent.isParticipating())
				{
					LMManager.this.startEvent();
				}
				else
				{
					LMManager.this.endEvent();
				}
			}
			
			if (delay > 0)
			{
				nextRun = ThreadPool.schedule(this, nextMsg * 1000);
			}
		}
		
		private void announce(long time)
		{
			if (time >= 3600 && time % 3600 == 0)
			{
				if (LMEvent.isParticipating())
				{
					Broadcast.announceToOnlinePlayers("LM Event: " + (time / 60 / 60) + " hour(s) until registration is closed!");
				}
				else if (LMEvent.isStarted())
				{
					LMEvent.sysMsgToAllParticipants("LM Event: " + (time / 60 / 60) + " hour(s) until event is finished!");
				}
			}
			else if (time >= 60)
			{
				if (LMEvent.isParticipating())
				{
					Broadcast.announceToOnlinePlayers("LM Event: " + (time / 60) + " minute(s) until registration is closed!");
				}
				else if (LMEvent.isStarted())
				{
					LMEvent.sysMsgToAllParticipants("LM Event: " + (time / 60) + " minute(s) until the event is finished!");
				}
			}
			else
			{
				if (LMEvent.isParticipating())
				{
					Broadcast.announceToOnlinePlayers("LM Event: " + time + " second(s) until registration is closed!");
				}
				else if (LMEvent.isStarted())
				{
					LMEvent.sysMsgToAllParticipants("LM Event: " + time + " second(s) until the event is finished!");
				}
			}
		}
	}
	
	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final LMManager _instance = new LMManager();
	}
}