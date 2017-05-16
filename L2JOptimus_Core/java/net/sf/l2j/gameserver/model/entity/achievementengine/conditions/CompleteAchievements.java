package net.sf.l2j.gameserver.model.entity.achievementengine.conditions;

import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.entity.achievementengine.Condition;

public class CompleteAchievements extends Condition
{
	public CompleteAchievements(Object value)
	{
		super(value);
		setName("Complete Achievements");
	}
	
	@Override
	public boolean meetConditionRequirements(Player player)
	{
		if (getValue() == null)
			return false;
		
		int val = Integer.parseInt(getValue().toString());
		
		return player.getCompletedAchievements().size() >= val;
	}
}