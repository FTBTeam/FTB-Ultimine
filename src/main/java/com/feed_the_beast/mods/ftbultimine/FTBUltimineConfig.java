package com.feed_the_beast.mods.ftbultimine;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * @author LatvianModder
 */
public class FTBUltimineConfig
{
	public static int maxBlocks;
	public static double exhaustionPerBlock;
	public static boolean mergeStone;
	public static final HashSet<ResourceLocation> toolBlacklist = new HashSet<>();

	private static Pair<CommonConfig, ForgeConfigSpec> server;

	public static void init()
	{
		FMLJavaModLoadingContext.get().getModEventBus().register(FTBUltimineConfig.class);

		server = new ForgeConfigSpec.Builder().configure(CommonConfig::new);

		ModLoadingContext modLoadingContext = ModLoadingContext.get();
		modLoadingContext.registerConfig(ModConfig.Type.COMMON, server.getRight());
	}

	@SubscribeEvent
	public static void reload(ModConfig.ModConfigEvent event)
	{
		ModConfig config = event.getConfig();

		if (config.getSpec() == server.getRight())
		{
			CommonConfig c = server.getLeft();
			maxBlocks = c.maxBlocks.get();
			exhaustionPerBlock = c.exhaustionPerBlock.get();
			mergeStone = c.mergeStone.get();
			toolBlacklist.clear();

			for (String s : c.toolBlacklist.get())
			{
				toolBlacklist.add(new ResourceLocation(s));
			}
		}
	}

	private static class CommonConfig
	{
		private final ForgeConfigSpec.IntValue maxBlocks;
		private final ForgeConfigSpec.DoubleValue exhaustionPerBlock;
		private final ForgeConfigSpec.BooleanValue mergeStone;
		private final ForgeConfigSpec.ConfigValue<List<? extends String>> toolBlacklist;

		private CommonConfig(ForgeConfigSpec.Builder builder)
		{
			maxBlocks = builder
					.comment("Max blocks you can mine at once")
					.translation("ftbultimine.max_blocks")
					.defineInRange("max_blocks", 64, 1, 32768);

			exhaustionPerBlock = builder
					.comment("Hunger multiplied for each block mined with ultimine")
					.translation("ftbultimine.exhaustion_per_block")
					.defineInRange("exhaustion_per_block", 20D, 0D, 10000D);

			mergeStone = builder
					.comment("Doesn't stop at different types of stones")
					.translation("ftbultimine.merge_stone")
					.define("merge_stone", true);

			toolBlacklist = builder
					.comment("Tools that won't let you active ultimine when held")
					.translation("ftbultimine.tool_blacklist")
					.defineList("tool_blacklist", Util.make(new ArrayList<>(), l -> l.add("mininggadgets:mininggadget")), o -> true);
		}
	}
}