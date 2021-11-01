package dev.ftb.mods.ftbultimine.utils;

import dev.ftb.mods.ftblibrary.snbt.config.SNBTConfig;
import me.shedaniel.architectury.hooks.LevelResourceHooks;
import me.shedaniel.architectury.platform.Platform;
import net.minecraft.world.level.storage.LevelResource;

import java.nio.file.Path;

import static dev.ftb.mods.ftbultimine.FTBUltimine.MOD_ID;

public interface IOUtil {
	Path ROOT_DIR = Platform.getGameFolder();

	Path DEFAULT_CONFIG_DIR = ROOT_DIR.resolve("defaultconfigs");
	Path CONFIG_DIR = ROOT_DIR.resolve("config");
	Path LOCAL_DIR = ROOT_DIR.resolve("local");

	LevelResource SERVER_CONFIG_DIR = LevelResourceHooks.create("serverconfig");

	static void loadDefaulted(SNBTConfig config, Path configDir) {
		String filename = config.key + ".snbt";
		Path configPath = configDir.resolve(filename);
		Path defaultPath = DEFAULT_CONFIG_DIR.resolve(MOD_ID).resolve(filename);
		config.load(
				configPath,
				defaultPath,
				() -> new String[]{
						"Default config file that will be copied to " + configPath + " if it doesn't exist!",
						"Just copy any values you wish to override in here!",
				}
		);

	}
}
