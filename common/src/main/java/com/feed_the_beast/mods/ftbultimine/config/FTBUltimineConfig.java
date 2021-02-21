package com.feed_the_beast.mods.ftbultimine.config;

import com.feed_the_beast.mods.ftbultimine.FTBUltimine;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;

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

	@Comment("Max amount of blocks that can be ultimined at once" + "\n"
			+ "Range: 1 - 32768")
	public int maxBlocks = 64;

	@Comment("Hunger multiplied for each block mined with ultimine" + "\n"
			+ "Range: 0 - 10000")
	public double exhaustionPerBlock = 20;

	@Comment("Groups stone types together so you can mine all of them at once")
	public boolean mergeStone = true;

	@Comment("Disable warnings for potentially laggy config settings")
	public boolean noLagWarnings = false;

	@Comment("Manual x offset of FTB Ultimine overlay, required for some modpacks")
	@ConfigEntry.Gui.Excluded
	public int renderTextManually = -1;

	@Comment("Render the white outline around blocks to be mined" + "\n"
			+ "Should be disabled for more than 512 blocks!")
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
