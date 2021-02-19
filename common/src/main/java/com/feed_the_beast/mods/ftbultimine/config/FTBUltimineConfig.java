package com.feed_the_beast.mods.ftbultimine.config;

import com.feed_the_beast.mods.ftbultimine.FTBUltimine;
import com.feed_the_beast.mods.ftbultimine.config.client.FTBUltimineConfigScreen;
import com.feed_the_beast.mods.ftbultimine.config.client.ToolList;
import me.shedaniel.architectury.utils.Env;
import me.shedaniel.architectury.utils.EnvExecutor;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;
import net.minecraft.ChatFormatting;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.Set;

/**
 * @author LatvianModder
 */
@Config(name = FTBUltimine.MOD_ID)
@Config.Gui.Background("minecraft:textures/block/stone.png")
public class FTBUltimineConfig implements ConfigData
{
	@ConfigEntry.Gui.Excluded
	private static ConfigHolder<FTBUltimineConfig> holder = null;

	public static FTBUltimineConfig get()
	{
		return holder.get();
	}

	@Comment("\r\n" +
			"Max amount of blocks that can be ultimined at once"
			+ "\r\n" + "Range: 1 - 32768")
	public int maxBlocks = 64;

	@Comment("\r\n" +
			"Hunger multiplied for each block mined with ultimine"
			+ "\r\n" + "Range: 0 - 10000")
	public double exhaustionPerBlock = 20;

	@Comment("Groups stone types together so you can mine all of them at once")
	public boolean mergeStone = true;

	@ToolList
	@Comment("Tools that won't let you activate ultimine when held")
	public List<String> toolBlacklist = null;

	@Comment("Disable warnings for potentially laggy config settingsJ")
	public boolean noLagWarnings = false;

	@Comment("Manual x offset of FTB Ultimine overlay, required for some modpacks")
	@ConfigEntry.Gui.Excluded
	public int renderTextManually = -1;

	@Comment("\r\n" +
			"Render the white outline around blocks to be mined"
			+ "\r\n" + "Should be disabled for more than 512 blocks!")
	public boolean renderOutline = true;

	public static void init()
	{
		holder = AutoConfig.register(FTBUltimineConfig.class, JanksonConfigSerializer::new);

		EnvExecutor.runInEnv(Env.CLIENT, () -> FTBUltimineConfigScreen::init);

		holder.registerSaveListener((manager, data) -> {
			data.validatePostLoad();
			return InteractionResult.PASS;
		});
	}

	@Override
	public void validatePostLoad()
	{
		if (toolBlacklist == null)
		{
			toolBlacklist = new ArrayList<>();
			toolBlacklist.add("mininggadgets:mininggadget");
		}

		// Validate all resource locations and remove duplicates
		Set<String> known = new HashSet<>();
		for (ListIterator<String> iterator = toolBlacklist.listIterator(); iterator.hasNext(); )
		{
			String id = iterator.next();
			try
			{
				id = new ResourceLocation(id.toLowerCase()).toString();
			}
			catch (ResourceLocationException rle)
			{
				id = null;
			}

			if (id == null || known.contains(id))
			{
				iterator.remove();
			}
			else
			{
				iterator.set(id);
				known.add(id);
			}
		}

		maxBlocks = Mth.clamp(maxBlocks, 1, 32768);
		if (!noLagWarnings && maxBlocks > 8192)
		{
			FTBUltimine.LOGGER.warn("maxBlocks is set to more than 8192 blocks!");
			FTBUltimine.LOGGER.warn("This may cause a lot of tick and FPS lag!");
		}

		exhaustionPerBlock = Mth.clamp(exhaustionPerBlock, 0, 10000);

		if (!noLagWarnings && renderOutline && maxBlocks > 512)
		{
			FTBUltimine.LOGGER.warn("Outline rendering is enabled for more than 512 blocks per excavation!");
			FTBUltimine.LOGGER.warn("This will almost definitely cause a lot of FPS lag!");
		}
	}

	public static Optional<Component> validateItems(List<String> strings)
	{
		for (String id : strings)
		{
			if (!Registry.ITEM.containsKey(ResourceLocation.tryParse(id)))
			{
				return Optional.of(new TextComponent("Invalid item: " + id).withStyle(ChatFormatting.RED));
			}
		}

		return Optional.empty();
	}
}
