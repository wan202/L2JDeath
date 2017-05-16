package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.commons.random.Rnd;

import net.sf.l2j.gameserver.datatables.SoulCrystalsTable;
import net.sf.l2j.gameserver.model.AbsorbInfo;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Attackable;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.instance.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.soulcrystal.LevelingInfo;
import net.sf.l2j.gameserver.model.soulcrystal.SoulCrystalData;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;
import net.sf.l2j.gameserver.scripting.quests.audio.Sound;

public class Q350_EnhanceYourWeapon extends Quest
{
	private static final String qn = "Q350_EnhanceYourWeapon";
	
	public Q350_EnhanceYourWeapon()
	{
		super(350, "Enhance Your Weapon");
		
		addStartNpc(30115, 30194, 30856);
		addTalkId(30115, 30194, 30856);
		
		for (int npcId : SoulCrystalsTable.getNpcInfos().keySet())
			addKillId(npcId);
		
		for (int crystalId : SoulCrystalsTable.getSoulCrystalInfos().keySet())
			addItemUse(crystalId);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		// Start the quest.
		if (event.endsWith("-04.htm"))
		{
			st.setState(Quest.STATE_STARTED);
			st.set("cond", "1");
			st.playSound(Sound.SOUND_ACCEPT);
		}
		// Give Red Soul Crystal.
		else if (event.endsWith("-09.htm"))
		{
			st.playSound(Sound.SOUND_MIDDLE);
			st.giveItems(4629, 1);
		}
		// Give Green Soul Crystal.
		else if (event.endsWith("-10.htm"))
		{
			st.playSound(Sound.SOUND_MIDDLE);
			st.giveItems(4640, 1);
		}
		// Give Blue Soul Crystal.
		else if (event.endsWith("-11.htm"))
		{
			st.playSound(Sound.SOUND_MIDDLE);
			st.giveItems(4651, 1);
		}
		// Terminate the quest.
		else if (event.endsWith("-exit.htm"))
			st.exitQuest(true);
		
		return htmltext;
	}
	
	@Override
	public String onTalk(Npc npc, Player player)
	{
		String htmltext = getNoQuestMsg();
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		switch (st.getState())
		{
			case STATE_CREATED:
				if (player.getLevel() < 40)
					htmltext = npc.getNpcId() + "-lvl.htm";
				else
					htmltext = npc.getNpcId() + "-01.htm";
				break;
			
			case STATE_STARTED:
				// Check inventory for soul crystals.
				for (ItemInstance item : player.getInventory().getItems())
				{
					// Crystal found, show "how to" html.
					if (SoulCrystalsTable.getSoulCrystalInfos().get(item.getItemId()) != null)
						return npc.getNpcId() + "-03.htm";
				}
				// No crystal found, offer a new crystal.
				htmltext = npc.getNpcId() + "-21.htm";
				break;
		}
		
		return htmltext;
	}
	
	@Override
	public String onItemUse(ItemInstance item, Player user, WorldObject target)
	{
		// Caster is dead.
		if (user.isDead())
			return null;
		
		// No target, or target isn't an Attackable.
		if (target == null || !(target instanceof Attackable))
			return null;
		
		final Attackable mob = ((Attackable) target);
		
		// Mob is dead or not registered in _npcInfos.
		if (mob.isDead() || !SoulCrystalsTable.getNpcInfos().containsKey(mob.getNpcId()))
			return null;
		
		// Add user to mob's absorber list.
		mob.addAbsorber(user, item);
		
		return null;
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isPet)
	{
		// Retrieve individual mob informations.
		final LevelingInfo npcInfo = SoulCrystalsTable.getNpcInfos().get(npc.getNpcId());
		if (npcInfo == null)
			return null;
		
		// Handle npc leveling info type.
		switch (npcInfo.getAbsorbCrystalType())
		{
			case FULL_PARTY:
				final Attackable mob = (Attackable) npc;
				final int chance = Rnd.get(100);
				
				for (Player player : getPartyMembersState(killer, npc, Quest.STATE_STARTED))
					tryToStageCrystal(player, mob, npcInfo, chance);
				break;
			
			case PARTY_ONE_RANDOM:
				final Player player = getRandomPartyMemberState(killer, npc, Quest.STATE_STARTED);
				if (player != null)
					tryToStageCrystal(player, (Attackable) npc, npcInfo, Rnd.get(100));
				break;
			
			case LAST_HIT:
				if (checkPlayerState(killer, npc, Quest.STATE_STARTED) != null)
					tryToStageCrystal(killer, (Attackable) npc, npcInfo, Rnd.get(100));
				break;
		}
		
		return null;
	}
	
	/**
	 * Define the Soul Crystal and try to stage it. Checks for quest enabled, crystal(s) in inventory, required usage of crystal, mob's ability to level crystal and mob vs player level gap.
	 * @param player : The player to make checks on.
	 * @param mob : The mob to make checks on.
	 * @param npcInfo : The mob's leveling informations.
	 * @param chance : Input variable used to determine keep/stage/break of the crystal.
	 */
	private static void tryToStageCrystal(Player player, Attackable mob, LevelingInfo npcInfo, int chance)
	{
		SoulCrystalData crystalData = null;
		ItemInstance crystalItem = null;
		
		// Iterate through player's inventory to find crystal(s).
		for (ItemInstance item : player.getInventory().getItems())
		{
			SoulCrystalData data = SoulCrystalsTable.getSoulCrystalInfos().get(item.getItemId());
			if (data == null)
				continue;
			
			// More crystals found.
			if (crystalData != null)
			{
				// Leveling requires soul crystal being used?
				if (npcInfo.skillRequired())
				{
					// Absorb list contains killer and his AbsorbInfo is registered.
					final AbsorbInfo ai = mob.getAbsorbInfo(player.getObjectId());
					if (ai != null && ai.isRegistered())
						player.sendPacket(SystemMessageId.SOUL_CRYSTAL_ABSORBING_FAILED_RESONATION);
				}
				else
					player.sendPacket(SystemMessageId.SOUL_CRYSTAL_ABSORBING_FAILED_RESONATION);
				
				return;
			}
			
			crystalData = data;
			crystalItem = item;
		}
		
		// No crystal found, return without any notification.
		if (crystalData == null || crystalItem == null)
			return;
		
		// Leveling requires soul crystal being used?
		if (npcInfo.skillRequired())
		{
			// Absorb list doesn't contain killer or his AbsorbInfo is not registered.
			final AbsorbInfo ai = mob.getAbsorbInfo(player.getObjectId());
			if (ai == null || !ai.isRegistered())
				return;
			
			// Check if Absorb list contains valid crystal and whether it was used properly.
			if (!ai.isValid(crystalItem.getObjectId()))
			{
				player.sendPacket(SystemMessageId.SOUL_CRYSTAL_ABSORBING_REFUSED);
				return;
			}
		}
		
		// Check, if npc stages this type of crystal.
		if (!npcInfo.isInLevelList(crystalData.getLevel()))
		{
			player.sendPacket(SystemMessageId.SOUL_CRYSTAL_ABSORBING_REFUSED);
			return;
		}
		
		// Check level difference limitation, dark blue monsters does not stage.
		if (player.getLevel() - mob.getLevel() > 8)
		{
			player.sendPacket(SystemMessageId.SOUL_CRYSTAL_ABSORBING_REFUSED);
			return;
		}
		
		// Lucky, crystal successfully stages.
		if (chance < npcInfo.getChanceStage())
			exchangeCrystal(player, crystalData, true);
		// Bad luck, crystal accidentally breaks.
		else if (chance < (npcInfo.getChanceStage() + npcInfo.getChanceBreak()))
			exchangeCrystal(player, crystalData, false);
		// Bad luck, crystal doesn't stage.
		else
			player.sendPacket(SystemMessageId.SOUL_CRYSTAL_ABSORBING_FAILED);
	}
	
	/**
	 * Remove the old crystal and add new one if stage, broken crystal if break. Send messages in both cases.
	 * @param player : The player to check on (inventory and send messages).
	 * @param scd : SoulCrystalData of to take information form.
	 * @param stage : Switch to determine success or fail.
	 */
	private static void exchangeCrystal(Player player, SoulCrystalData scd, boolean stage)
	{
		QuestState st = player.getQuestState(qn);
		
		st.takeItems(scd.getCrystalItemId(), 1);
		if (stage)
		{
			player.sendPacket(SystemMessageId.SOUL_CRYSTAL_ABSORBING_SUCCEEDED);
			st.giveItems(scd.getStagedItemId(), 1);
			st.playSound(Sound.SOUND_ITEMGET);
		}
		else
		{
			int broken = scd.getBrokenItemId();
			if (broken != 0)
			{
				player.sendPacket(SystemMessageId.SOUL_CRYSTAL_BROKE);
				st.giveItems(broken, 1);
			}
		}
	}
}