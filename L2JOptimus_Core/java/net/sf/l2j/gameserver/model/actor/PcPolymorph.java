package net.sf.l2j.gameserver.model.actor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;

import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.model.CharSelectInfoPackage;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.itemcontainer.Inventory;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.NpcInfoPolymorph;

/**
 * @author paytaly
 */
public class PcPolymorph extends Npc
{
	private CharSelectInfoPackage _polymorphInfo;
	private int _nameColor = 0xFFFFFF;
	private int _titleColor = 0xFFFF77;
	private String _visibleTitle = "";
	
	public PcPolymorph(int objectId, NpcTemplate template)
	{
		super(objectId, template);
		setIsInvul(true);
	}
	
	@Override
	public boolean hasRandomAnimation()
	{
		return false;
	}
	
	public CharSelectInfoPackage getPolymorphInfo()
	{
		return _polymorphInfo;
	}
	
	public void setPolymorphInfo(CharSelectInfoPackage polymorphInfo)
	{
		_polymorphInfo = polymorphInfo;
		
		for (WorldObject object : getKnownType(WorldObject.class))
		{
			if (object instanceof Player)
			{
				sendInfo(object.getActingPlayer());
			}
		}
	}
	
	public int getNameColor()
	{
		return _nameColor;
	}
	
	public void setNameColor(int nameColor)
	{
		_nameColor = nameColor;
	}
	
	public int getTitleColor()
	{
		return _titleColor;
	}
	
	public void setTitleColor(int titleColor)
	{
		_titleColor = titleColor;
	}
	
	public String getVisibleTitle()
	{
		return _visibleTitle;
	}
	
	public void setVisibleTitle(String title)
	{
		_visibleTitle = title == null ? "" : title;
	}
	
	@Override
	public void sendInfo(Player activeChar)
	{
		if (getPolymorphInfo() == null)
		{
			super.sendInfo(activeChar);
			return;
		}
		
		activeChar.sendPacket(new NpcInfoPolymorph(this));
	}
	
	@Override
	public String getHtmlPath(int npcId, int val)
	{
		String pom = "" + npcId;
		if (val != 0)
		{
			pom += "-" + val;
		}
		return "data/html/polymorph/" + pom + ".htm";
	}
	
	@Override
	public void showChatWindow(Player player, int val)
	{
		String filename = getHtmlPath(getNpcId(), val);
		
		// Send a Server->Client NpcHtmlMessage containing the text of the Npc to the Player
		final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(filename);
		html.replace("%objectId%", getObjectId());
		html.replace("%ownername%", getPolymorphInfo() != null ? getPolymorphInfo().getName() : "");
		player.sendPacket(html);
		
		// Send a Server->Client ActionFailed to the Player in order to avoid that the client wait another packet
		player.ActionF();
	}
	
	public static CharSelectInfoPackage loadCharInfo(int objectId)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection(); PreparedStatement statement = con.prepareStatement("SELECT char_name, race, base_class, classid, sex, face, hairStyle, hairColor, clanid FROM characters WHERE obj_Id = ?"))
		{
			statement.setInt(1, objectId);
			
			try (ResultSet rs = statement.executeQuery())
			{
				if (rs.next())
				{
					final CharSelectInfoPackage charInfo = new CharSelectInfoPackage(objectId, rs.getString("char_name"));
					charInfo.setRace(rs.getInt("race"));
					charInfo.setBaseClassId(rs.getInt("base_class"));
					charInfo.setClassId(rs.getInt("classid"));
					charInfo.setSex(rs.getInt("sex"));
					charInfo.setFace(rs.getInt("face"));
					charInfo.setHairStyle(rs.getInt("hairStyle"));
					charInfo.setHairColor(rs.getInt("hairColor"));
					charInfo.setClanId(rs.getInt("clanid"));
					
					// Get the augmentation id for equipped weapon
					int weaponObjId = charInfo.getPaperdollObjectId(Inventory.PAPERDOLL_RHAND);
					if (weaponObjId > 0)
					{
						try (PreparedStatement statementAugment = con.prepareStatement("SELECT attributes FROM augmentations WHERE item_id = ?"))
						{
							statementAugment.setInt(1, weaponObjId);
							try (ResultSet rsAugment = statementAugment.executeQuery())
							{
								if (rsAugment.next())
								{
									int augment = rsAugment.getInt("attributes");
									charInfo.setAugmentationId(augment == -1 ? 0 : augment);
								}
							}
						}
					}
					
					return charInfo;
				}
			}
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Could not restore char info: " + e.getMessage(), e);
		}
		
		return null;
	}
}
