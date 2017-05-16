package net.sf.l2j.gameserver.model.actor.instance;

import java.util.StringTokenizer;

import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.entity.achievementengine.Achievement;
import net.sf.l2j.gameserver.model.entity.achievementengine.AchievementsManager;
import net.sf.l2j.gameserver.model.entity.achievementengine.Condition;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * @author Matim
 * @version 1.0
 */
public class Achievements extends Folk
{
	public Achievements(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if (player == null || player.getCurrentFolkNPC() == null || player.getCurrentFolkNPC().getObjectId() != this.getObjectId())
			return;
		
		if (command.startsWith("showMyAchievements"))
		{
			player.getAchievemntData();
			showMyAchievements(player);
		}
		else if (command.startsWith("achievementInfo"))
		{
			StringTokenizer st = new StringTokenizer(command, " ");
			st.nextToken();
			int id = Integer.parseInt(st.nextToken());
			
			showAchievementInfo(id, player);
		}
		else if (command.startsWith("topList"))
		{
			showTopListWindow(player);
		}
		else if (command.startsWith("showMainWindow"))
		{
			showChatWindow(player, 0);
		}
		else if (command.startsWith("getReward"))
		{
			StringTokenizer st = new StringTokenizer(command, " ");
			st.nextToken();
			int id = Integer.parseInt(st.nextToken());
			AchievementsManager.getInstance().rewardForAchievement(id, player);
			player.saveAchievementData(id);
			showMyAchievements(player);
			
		}
		else if (command.startsWith("showMyStats"))
		{
			showMyStatsWindow(player);
		}
		else if (command.startsWith("showHelpWindow"))
		{
			showHelpWindow(player);
		}
	}
	
	@Override
	public void showChatWindow(Player player, int val)
	{
		StringBuilder tb = new StringBuilder();
		tb.append("<html><title>Achievements Manager</title><body><center><br>");
		tb.append("<img src=\"l2font-e.replay_logo-e\" width=250 height=80><br1><center><img src=\"L2UI.SquareGray\" width=300 height=1></center><table bgcolor=000000 width=319><tr><td><center><font color=\"LEVEL\">Hello <font color=\"LEVEL\">" + player.getName() + "</font></center></td></font></tr></table><center><img src=\"L2UI.SquareGray\" width=300 height=1></center>");
		tb.append("<br><font color=\"LEVEL\">Are you looking for challenge?</font>");
		tb.append("<br><img src=\"l2ui.squaregray\" width=\"270\" height=\"1\"><br>");
		tb.append("<button value=\"My Achievements\" action=\"bypass -h npc_%objectId%_showMyAchievements\" width=134 height=21 back=\"L2UI_CH3.br_bar2_hp1\" fore=\"L2UI_CH3.br_bar2_hp1\">");
		tb.append("<button value=\"Statistics\" action=\"bypass -h npc_%objectId%_showMyStats\" width=134 height=21 back=\"L2UI_CH3.br_bar2_hp1\" fore=\"L2UI_CH3.br_bar2_hp1\">");
		tb.append("<button value=\"Help\" action=\"bypass -h npc_%objectId%_showHelpWindow\" width=134 height=21 back=\"L2UI_CH3.br_bar2_hp1\" fore=\"L2UI_CH3.br_bar2_hp1\">");
		tb.append("<br><img src=\"l2ui.squaregray\" width=\"270\" height=\"1\"><br>");
		tb.append("<center><br><img src=l2ui.bbs_lineage2 height=16 width=80></center>");
		
		NpcHtmlMessage msg = new NpcHtmlMessage(this.getObjectId());
		msg.setHtml(tb.toString());
		msg.replace("%objectId%", String.valueOf(this.getObjectId()));
		
		player.sendPacket(msg);
	}
	
	private void showMyAchievements(Player player)
	{
		StringBuilder tb = new StringBuilder();
		tb.append("<html><title>Achievements Manager</title><body><br>");
		
		tb.append("<center><font color=\"LEVEL\">My achievements</font>:</center><br>");
		
		if (AchievementsManager.getInstance().getAchievementList().isEmpty())
		{
			tb.append("There are no Achievements created yet!");
		}
		else
		{
			int i = 0;
			
			tb.append("<table width=270 border=0 bgcolor=\"33FF33\">");
			tb.append("<tr><td width=270 align=\"left\">Name:</td><td width=60 align=\"right\">Info:</td><td width=200 align=\"center\">Status:</td></tr></table>");
			tb.append("<br><img src=\"l2ui.squaregray\" width=\"270\" height=\"1\"><br>");
			
			for (Achievement a : AchievementsManager.getInstance().getAchievementList().values())
			{
				tb.append(getTableColor(i));
				tb.append("<tr><td width=270 align=\"left\">" + a.getName() + "</td><td width=50 align=\"right\"><a action=\"bypass -h npc_%objectId%_achievementInfo " + a.getID() + "\">info</a></td><td width=200 align=\"center\">" + getStatusString(a.getID(), player) + "</td></tr></table>");
				i++;
			}
			
			tb.append("<br><img src=\"l2ui.squaregray\" width=\"270\" height=\"1s\"><br>");
			tb.append("<center><button value=\"Back\" action=\"bypass -h npc_%objectId%_showMainWindow\" width=134 height=21 back=\"L2UI_CH3.br_bar2_hp1\" fore=\"L2UI_CH3.br_bar2_hp1\"><center>");
		}
		
		NpcHtmlMessage msg = new NpcHtmlMessage(this.getObjectId());
		msg.setHtml(tb.toString());
		msg.replace("%objectId%", String.valueOf(this.getObjectId()));
		
		player.sendPacket(msg);
	}
	
	private void showAchievementInfo(int achievementID, Player player)
	{
		Achievement a = AchievementsManager.getInstance().getAchievementList().get(achievementID);
		
		StringBuilder tb = new StringBuilder();
		tb.append("<html><title>Achievements Manager</title><body><br>");
		
		tb.append("<table width=270 border=0 bgcolor=\"33FF33\">");
		tb.append("<tr><td width=270 align=\"center\">" + a.getName() + "</td></tr></table><br>");
		tb.append("<center>Status: " + getStatusString(achievementID, player));
		
		if (a.meetAchievementRequirements(player) && !player.getCompletedAchievements().contains(achievementID))
		{
			tb.append("<button value=\"Receive Reward!\" action=\"bypass -h npc_%objectId%_getReward " + a.getID() + "\" width=134 height=21 back=\"L2UI_CH3.br_bar2_hp1\" fore=\"L2UI_CH3.br_bar2_hp1\">");
		}
		
		tb.append("<br><img src=\"l2ui.squaregray\" width=\"270\" height=\"1s\"><br>");
		
		tb.append("<table width=270 border=0 bgcolor=\"33FF33\">");
		tb.append("<tr><td width=270 align=\"center\">Description</td></tr></table><br>");
		tb.append(a.getDescription());
		tb.append("<br><img src=\"l2ui.squaregray\" width=\"270\" height=\"1s\"><br>");
		
		tb.append("<table width=270 border=0 bgcolor=\"33FF33\">");
		tb.append("<tr><td width=270 align=\"left\">Condition:</td><td width=100 align=\"right\">Value:</td><td width=200 align=\"center\">Status:</td></tr></table>");
		tb.append(getConditionsStatus(achievementID, player));
		tb.append("<br><img src=\"l2ui.squaregray\" width=\"270\" height=\"1s\"><br>");
		tb.append("<center><button value=\"Back\" action=\"bypass -h npc_%objectId%_showMyAchievements\" width=134 height=21 back=\"L2UI_CH3.br_bar2_hp1\" fore=\"L2UI_CH3.br_bar2_hp1\"><center>");
		
		NpcHtmlMessage msg = new NpcHtmlMessage(this.getObjectId());
		msg.setHtml(tb.toString());
		msg.replace("%objectId%", String.valueOf(this.getObjectId()));
		
		player.sendPacket(msg);
	}
	
	private void showMyStatsWindow(Player player)
	{
		StringBuilder tb = new StringBuilder();
		tb.append("<html><title>Achievements Manager</title><body><center><br>");
		tb.append("Check your <font color=\"LEVEL\">Achievements </font>statistics:");
		tb.append("<br><img src=\"l2ui.squaregray\" width=\"270\" height=\"1\"><br>");
		
		player.getAchievemntData();
		int completedCount = player.getCompletedAchievements().size();
		
		tb.append("You have completed: " + completedCount + "/<font color=\"LEVEL\">" + AchievementsManager.getInstance().getAchievementList().size() + "</font>");
		
		tb.append("<br><img src=\"l2ui.squaregray\" width=\"270\" height=\"1s\"><br>");
		tb.append("<center><button value=\"Back\" action=\"bypass -h npc_%objectId%_showMainWindow\" width=134 height=21 back=\"L2UI_CH3.br_bar2_hp1\" fore=\"L2UI_CH3.br_bar2_hp1\"><center>");
		
		NpcHtmlMessage msg = new NpcHtmlMessage(this.getObjectId());
		msg.setHtml(tb.toString());
		msg.replace("%objectId%", String.valueOf(this.getObjectId()));
		
		player.sendPacket(msg);
	}
	
	private void showTopListWindow(Player player)
	{
		StringBuilder tb = new StringBuilder();
		tb.append("<html><title>Achievements Manager</title><body><center><br>");
		tb.append("Check your <font color=\"LEVEL\">Achievements </font>Top List:");
		tb.append("<br><img src=\"l2ui.squaregray\" width=\"270\" height=\"1\"><br>");
		
		tb.append("List Player " + player.getCompletedAchievements() + " ");
		
		tb.append("<br><img src=\"l2ui.squaregray\" width=\"270\" height=\"1s\"><br>");
		tb.append("<center><button value=\"Back\" action=\"bypass -h npc_%objectId%_showMainWindow\" width=134 height=21 back=\"L2UI_CH3.br_bar2_hp1\" fore=\"L2UI_CH3.br_bar2_hp1\"><center>");
		
		NpcHtmlMessage msg = new NpcHtmlMessage(this.getObjectId());
		msg.setHtml(tb.toString());
		msg.replace("%objectId%", String.valueOf(this.getObjectId()));
		
		player.sendPacket(msg);
	}
	
	private void showHelpWindow(Player player)
	{
		StringBuilder tb = new StringBuilder();
		tb.append("<html><title>Achievements Manager</title><body><center><br>");
		tb.append("Achievements  <font color=\"LEVEL\">Help </font>page:");
		tb.append("<br><img src=\"l2ui.squaregray\" width=\"270\" height=\"1\"><br>");
		
		tb.append("<center>You can check status of your achievements, receive reward if every condition of achievement is meet, if not you can check which condition is still not meet, by using info button");
		tb.append("<br><img src=\"l2ui.squaregray\" width=\"270\" height=\"1s\"><br>");
		tb.append("<font color=\"FF0000\">Not Completed</font> - you did not meet the achivement requirements.<br>");
		tb.append("<font color=\"LEVEL\">Get Reward</font> - you may receive reward, click info.<br>");
		tb.append("<font color=\"5EA82E\">Completed</font> - achievement completed, reward received.<br></center>");
		
		tb.append("Achievements Engine by <font color=\"LEVEL\">Interlude Project</font>");
		tb.append("<br><img src=\"l2ui.squaregray\" width=\"270\" height=\"1s\"><br>");
		tb.append("<center><button value=\"Back\" action=\"bypass -h npc_%objectId%_showMainWindow\" width=134 height=21 back=\"L2UI_CH3.br_bar2_hp1\" fore=\"L2UI_CH3.br_bar2_hp1\"><center>");
		
		NpcHtmlMessage msg = new NpcHtmlMessage(this.getObjectId());
		msg.setHtml(tb.toString());
		msg.replace("%objectId%", String.valueOf(this.getObjectId()));
		
		player.sendPacket(msg);
	}
	
	private static String getStatusString(int achievementID, Player player)
	{
		if (player.getCompletedAchievements().contains(achievementID))
		{
			return "<font color=\"5EA82E\">Completed</font>";
		}
		if (AchievementsManager.getInstance().getAchievementList().get(achievementID).meetAchievementRequirements(player))
		{
			return "<font color=\"LEVEL\">Get Reward</font>";
		}
		return "<font color=\"FF0000\">Not Completed</font>";
	}
	
	private static String getTableColor(int i)
	{
		if (i % 2 == 0)
			return "<table width=270 border=0 bgcolor=\"444444\">";
		return "<table width=270 border=0>";
	}
	
	private static String getConditionsStatus(int achievementID, Player player)
	{
		int i = 0;
		String s = "</center>";
		Achievement a = AchievementsManager.getInstance().getAchievementList().get(achievementID);
		String completed = "<font color=\"5EA82E\">Completed</font></td></tr></table>";
		String notcompleted = "<font color=\"FF0000\">Not Completed</font></td></tr></table>";
		
		for (Condition c : a.getConditions())
		{
			s += getTableColor(i);
			s += "<tr><td width=270 align=\"left\">" + c.getName() + "</td><td width=50 align=\"right\">" + c.getValue() + "</td><td width=200 align=\"center\">";
			i++;
			
			if (c.meetConditionRequirements(player))
				s += completed;
			else
				s += notcompleted;
		}
		return s;
	}
}