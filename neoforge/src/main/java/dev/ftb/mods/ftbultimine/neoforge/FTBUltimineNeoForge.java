package dev.ftb.mods.ftbultimine.neoforge;

import dev.ftb.mods.ftblibrary.util.neoforge.NeoEventHelper;
import dev.ftb.mods.ftbultimine.FTBUltimine;
import dev.ftb.mods.ftbultimine.FTBUltimineCommands;
import dev.ftb.mods.ftbultimine.api.FTBUltimineAPI;
import dev.ftb.mods.ftbultimine.api.blockbreaking.RegisterBlockBreakHandlerEvent;
import dev.ftb.mods.ftbultimine.api.blockselection.RegisterBlockSelectionHandlerEvent;
import dev.ftb.mods.ftbultimine.api.crop.RegisterCropLikeEvent;
import dev.ftb.mods.ftbultimine.api.neoforge.FTBUltimineEvent;
import dev.ftb.mods.ftbultimine.api.restriction.RegisterRestrictionHandlerEvent;
import dev.ftb.mods.ftbultimine.api.rightclick.RegisterRightClickHandlerEvent;
import dev.ftb.mods.ftbultimine.api.shape.RegisterShapeEvent;
import dev.ftb.mods.ftbultimine.integration.NeoIntegration;
import dev.ftb.mods.ftbultimine.registry.ModAttributes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@Mod(FTBUltimineAPI.MOD_ID)
public class FTBUltimineNeoForge {
	private final FTBUltimine ultimine;

	public FTBUltimineNeoForge(IEventBus modEventBus) {
		var bus = NeoForge.EVENT_BUS;

		ultimine = new FTBUltimine();

		modEventBus.addListener(this::addPlayerAttributes);

		bus.addListener(PlayerEvent.PlayerLoggedInEvent.class, event -> ultimine.playerJoined((ServerPlayer) event.getEntity()));
		bus.addListener(ServerStartingEvent.class, this::onServerStarting);
		bus.addListener(ServerStoppingEvent.class, event -> ultimine.serverStopping(event.getServer()));
		bus.addListener(ServerTickEvent.Post.class, event -> ultimine.serverTick(event.getServer()));
		bus.addListener(EventPriority.HIGH, BreakBlockEvent.class, event -> {
			if (event.getPlayer() instanceof ServerPlayer sp && ultimine.handleBlockBreak(event.getLevel(), event.getPos(), event.getState(), sp)) {
				event.setCanceled(true);
			}
		});
		bus.addListener(EventPriority.HIGH, PlayerInteractEvent.RightClickBlock.class, event -> {
			if (ultimine.blockRightClick(event.getEntity(), event.getHand(), event.getPos()).consumesAction()) {
				event.setCanceled(true);
			}
		});
		bus.addListener(PlayerTickEvent.Pre.class, event -> ultimine.playerTick(event.getEntity()));
		bus.addListener(EventPriority.HIGH, EntityJoinLevelEvent.class, event -> {
			if (ultimine.handleItemAndOrbJoining(event.getEntity(), event.getLevel())) {
				event.setCanceled(true);
			}
		});
		bus.addListener(RegisterCommandsEvent.class, event ->
				FTBUltimineCommands.registerCommands(event.getDispatcher(), event.getBuildContext(), event.getCommandSelection()));

		bus.addListener(FTBUltimineEvent.RegisterRightClickHandler.class, event ->
				ultimine.registerBuiltinRightClickHandlers(event.getEventData()));
		bus.addListener(FTBUltimineEvent.RegisterShape.class, event ->
				ultimine.registerBuiltinShapes(event.getEventData()));
		bus.addListener(FTBUltimineEvent.RegisterCropLike.class, event ->
				ultimine.registerBuiltinCropHandlers(event.getEventData()));

		registerNeoEventPosters(bus);

		NeoIntegration.init(bus);
	}

	private void onServerStarting(ServerStartingEvent event) {
		ultimine.serverStarting(event.getServer());

		FTBUltimine.commonSetup(false);
	}

	private void addPlayerAttributes(EntityAttributeModificationEvent event) {
		event.add(EntityTypes.PLAYER, ModAttributes.FixedHolder.MAX_BLOCKS_MODIFIER.get());
		event.add(EntityTypes.PLAYER, ModAttributes.FixedHolder.COOLDOWN_MODIFIER.get());
		event.add(EntityTypes.PLAYER, ModAttributes.FixedHolder.EXHAUSTION_MODIFIER.get());
		event.add(EntityTypes.PLAYER, ModAttributes.FixedHolder.EXPERIENCE_MODIFIER.get());
	}

	private static void registerNeoEventPosters(IEventBus bus) {
		NeoEventHelper.registerNeoEventPoster(bus, RegisterRightClickHandlerEvent.Data.class, FTBUltimineEvent.RegisterRightClickHandler::new);
		NeoEventHelper.registerNeoEventPoster(bus, RegisterShapeEvent.Data.class, FTBUltimineEvent.RegisterShape::new);
		NeoEventHelper.registerNeoEventPoster(bus, RegisterCropLikeEvent.Data.class, FTBUltimineEvent.RegisterCropLike::new);
		NeoEventHelper.registerNeoEventPoster(bus, RegisterRestrictionHandlerEvent.Data.class, FTBUltimineEvent.RegisterRestrictionHandler::new);
		NeoEventHelper.registerNeoEventPoster(bus, RegisterBlockBreakHandlerEvent.Data.class, FTBUltimineEvent.RegisterBlockBreakHandler::new);
		NeoEventHelper.registerNeoEventPoster(bus, RegisterBlockSelectionHandlerEvent.Data.class, FTBUltimineEvent.RegisterBlockSelectionHandler::new);
	}
}
