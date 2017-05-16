package net.sf.l2j.commons.mmocore;

import java.nio.ByteBuffer;

/**
 * @author KenM
 * @param <T>
 */
public abstract class AbstractPacket<T extends MMOClient<?>>
{
	protected ByteBuffer _buf;
	
	protected T _client;
	
	public final T getClient()
	{
		return _client;
	}
}