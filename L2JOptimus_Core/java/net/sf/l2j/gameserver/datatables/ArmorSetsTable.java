package net.sf.l2j.gameserver.datatables;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2j.gameserver.model.item.ArmorSet;
import net.sf.l2j.gameserver.templates.StatsSet;
import net.sf.l2j.gameserver.xmlfactory.XMLDocumentFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class ArmorSetsTable
{
	private static Logger _log = Logger.getLogger(ArmorSetsTable.class.getName());
	
	private final Map<Integer, ArmorSet> _armorSets = new HashMap<>();
	
	public static ArmorSetsTable getInstance()
	{
		return SingletonHolder._instance;
	}
	
	protected ArmorSetsTable()
	{
		try
		{
			File f = new File("./data/xml/armorsets.xml");
			Document doc = XMLDocumentFactory.getInstance().loadDocument(f);
			
			final StatsSet set = new StatsSet();
			
			Node n = doc.getFirstChild();
			for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
			{
				if (!d.getNodeName().equalsIgnoreCase("armorset"))
					continue;
				
				final NamedNodeMap attrs = d.getAttributes();
				for (int i = 0; i < attrs.getLength(); i++)
				{
					final Node att = attrs.item(i);
					set.set(att.getNodeName(), att.getNodeValue());
				}
				
				_armorSets.put(set.getInteger("chest"), new ArmorSet(set));
				set.clear();
			}
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "ArmorSetsTable: Error loading armorsets.xml", e);
		}
		_log.info("ArmorSetsTable: Loaded " + _armorSets.size() + " armor sets.");
	}
	
	public ArmorSet getSet(int chestId)
	{
		return _armorSets.get(chestId);
	}
	
	public Collection<ArmorSet> getSets()
	{
		return _armorSets.values();
	}
	
	private static class SingletonHolder
	{
		protected static final ArmorSetsTable _instance = new ArmorSetsTable();
	}
}