package net.sf.l2j.gameserver.datatables;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import net.sf.l2j.commons.random.Rnd;

import net.sf.l2j.gameserver.model.FishData;
import net.sf.l2j.gameserver.xmlfactory.XMLDocumentFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * @author -Nemesiss-, Java-man
 */
public class FishTable
{
	private static final Logger _log = Logger.getLogger(FishTable.class.getName());
	
	private static final List<FishData> _fishes = new ArrayList<>();
	
	public static FishTable getInstance()
	{
		return SingletonHolder._instance;
	}
	
	protected FishTable()
	{
		try
		{
			final File f = new File("./data/xml/fishes.xml");
			final Document doc = XMLDocumentFactory.getInstance().loadDocument(f);
			
			final Node n = doc.getFirstChild();
			for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
			{
				if (d.getNodeName().equalsIgnoreCase("fish"))
				{
					NamedNodeMap attrs = d.getAttributes();
					
					int id = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
					int lvl = Integer.parseInt(attrs.getNamedItem("level").getNodeValue());
					String name = attrs.getNamedItem("name").getNodeValue();
					int hp = Integer.parseInt(attrs.getNamedItem("hp").getNodeValue());
					int hpreg = Integer.parseInt(attrs.getNamedItem("hpregen").getNodeValue());
					int type = Integer.parseInt(attrs.getNamedItem("fish_type").getNodeValue());
					int group = Integer.parseInt(attrs.getNamedItem("fish_group").getNodeValue());
					int fish_guts = Integer.parseInt(attrs.getNamedItem("fish_guts").getNodeValue());
					int guts_check_time = Integer.parseInt(attrs.getNamedItem("guts_check_time").getNodeValue());
					int wait_time = Integer.parseInt(attrs.getNamedItem("wait_time").getNodeValue());
					int combat_time = Integer.parseInt(attrs.getNamedItem("combat_time").getNodeValue());
					
					_fishes.add(new FishData(id, lvl, name, hp, hpreg, type, group, fish_guts, guts_check_time, wait_time, combat_time));
				}
			}
		}
		catch (Exception e)
		{
			_log.warning("FishTable: Error while creating table" + e);
		}
		
		_log.info("FishTable: Loaded " + _fishes.size() + " fishes.");
	}
	
	public static FishData getFish(int lvl, int type, int group)
	{
		final List<FishData> result = new ArrayList<>();
		
		for (FishData fish : _fishes)
		{
			if (fish.getLevel() != lvl || fish.getType() != type || fish.getGroup() != group)
				continue;
			
			result.add(fish);
		}
		
		if (result.isEmpty())
		{
			_log.warning("Couldn't find any fish with lvl: " + lvl + " and type: " + type);
			return null;
		}
		
		return Rnd.get(result);
	}
	
	private static class SingletonHolder
	{
		protected static final FishTable _instance = new FishTable();
	}
}