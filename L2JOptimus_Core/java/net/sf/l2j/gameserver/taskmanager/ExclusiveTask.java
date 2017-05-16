/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package net.sf.l2j.gameserver.taskmanager;

import java.util.concurrent.Future;

import net.sf.l2j.commons.concurrent.ThreadPool;

public abstract class ExclusiveTask
{
	private final boolean _returnIfAlreadyRunning;
	
	private Future<?> _future;
	private boolean _isRunning;
	private Thread _currentThread;
	
	protected abstract void onElapsed();
	
	protected ExclusiveTask(boolean returnIfAlreadyRunning)
	{
		_returnIfAlreadyRunning = returnIfAlreadyRunning;
	}
	
	protected ExclusiveTask()
	{
		this(false);
	}
	
	public synchronized boolean isScheduled()
	{
		return _future != null;
	}
	
	public synchronized final void cancel()
	{
		if (_future != null)
		{
			_future.cancel(false);
			_future = null;
		}
	}
	
	public synchronized final void schedule(long delay)
	{
		cancel();
		
		_future = ThreadPool.schedule(_runnable, delay);
	}
	
	public synchronized final void execute()
	{
		ThreadPool.execute(_runnable);
	}
	
	public synchronized final void scheduleAtFixedRate(long delay, long period)
	{
		cancel();
		
		_future = ThreadPool.scheduleAtFixedRate(_runnable, delay, period);
	}
	
	private final Runnable _runnable = new Runnable()
	{
		@Override
		public void run()
		{
			if (tryLock())
			{
				try
				{
					onElapsed();
				}
				finally
				{
					unlock();
				}
			}
		}
	};
	
	synchronized boolean tryLock()
	{
		if (_returnIfAlreadyRunning)
		{
			return !_isRunning;
		}
		
		_currentThread = Thread.currentThread();
		
		for (;;)
		{
			try
			{
				notifyAll();
				
				if (_currentThread != Thread.currentThread())
				{
					return false;
				}
				
				if (!_isRunning)
				{
					return true;
				}
				
				wait();
			}
			catch (InterruptedException e)
			{
			}
		}
	}
	
	synchronized void unlock()
	{
		_isRunning = false;
	}
	
}