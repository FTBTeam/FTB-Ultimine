package com.feed_the_beast.mods.ftbultimine;

import blue.endless.jankson.Comment;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;

import java.util.Collections;
import java.util.List;

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

	@Comment("Max blocks you can mine at once")
	public int maxBlocks = 64;

	@Comment("Hunger multiplied for each block mined with ultimine")
	public double exhaustionPerBlock = 20;

	@Comment("Considers different types of stones the same")
	public boolean mergeStone = true;

	@Comment("Tools that won't let you active ultimine when held")
	// TODO: change this back to ResLoc (or even Item?) once dan fixes Jankson stuff
	public final List<String> toolBlacklist = Collections.singletonList("mininggadgets:mininggadget");

	@Comment("Disable lag warnings")
	public boolean noLagWarnings = false;

	@Comment("Required for some modpacks")
	@ConfigEntry.Gui.Excluded
	public int renderTextManually = -1;

	@Comment("Render the white outline around blocks to be mined")
	public boolean renderOutline = true;

	public static void init()
	{
		holder = AutoConfig.register(FTBUltimineConfig.class, JanksonConfigSerializer::new);
		holder.registerSaveListener((manager, data) -> {
			data.validatePostLoad();
			return InteractionResult.PASS;
		});
	}

	@Override
	public void validatePostLoad()
	{
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
}
