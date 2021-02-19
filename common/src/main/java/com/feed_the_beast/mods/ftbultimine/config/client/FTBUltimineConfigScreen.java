package com.feed_the_beast.mods.ftbultimine.config.client;

import com.feed_the_beast.mods.ftbultimine.config.FTBUltimineConfig;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.network.chat.TranslatableComponent;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static me.shedaniel.autoconfig.util.Utils.*;

public class FTBUltimineConfigScreen
{
	public static void init()
	{
		ConfigEntryBuilder ENTRY_BUILDER = ConfigEntryBuilder.create();

		AutoConfig.getGuiRegistry(FTBUltimineConfig.class).registerPredicateProvider(
				(i13n, field, config, defaults, guiProvider) -> Collections.singletonList(
						ENTRY_BUILDER.startStrList(new TranslatableComponent(i13n), getUnsafely(field, config))
								.setDefaultValue(() -> getUnsafely(field, defaults))
								.setSaveConsumer(newValue -> setUnsafely(field, config, newValue))
								.setErrorSupplier(FTBUltimineConfig::validateItems)
								.build()
				),
				field -> field.isAnnotationPresent(ToolList.class) && isStringList(field)
		);
	}

	private static boolean isStringList(Field field)
	{
		if (List.class.isAssignableFrom(field.getType()) && field.getGenericType() instanceof ParameterizedType)
		{
			Type[] args = ((ParameterizedType) field.getGenericType()).getActualTypeArguments();
			return args.length == 1 && Objects.equals(args[0], String.class);
		}
		else
		{
			return false;
		}
	}

}
