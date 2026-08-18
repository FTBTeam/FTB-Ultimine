package dev.ftb.mods.ftbultimine;

import dev.ftb.mods.ftblibrary.config.manager.ConfigManager;
import dev.ftb.mods.ftblibrary.platform.Platform;
import dev.ftb.mods.ftblibrary.platform.event.NativeEventPosting;
import dev.ftb.mods.ftblibrary.platform.network.Server2PlayNetworking;
import dev.ftb.mods.ftblibrary.util.Lazy;
import dev.ftb.mods.ftbultimine.api.FTBUltimineAPI;
import dev.ftb.mods.ftbultimine.api.FTBUltimineTags;
import dev.ftb.mods.ftbultimine.api.blockbreaking.RegisterBlockBreakHandlerEvent;
import dev.ftb.mods.ftbultimine.api.blockselection.RegisterBlockSelectionHandlerEvent;
import dev.ftb.mods.ftbultimine.api.crop.RegisterCropLikeEvent;
import dev.ftb.mods.ftbultimine.api.restriction.RegisterRestrictionHandlerEvent;
import dev.ftb.mods.ftbultimine.api.rightclick.RegisterRightClickHandlerEvent;
import dev.ftb.mods.ftbultimine.api.shape.RegisterShapeEvent;
import dev.ftb.mods.ftbultimine.api.shape.Shape;
import dev.ftb.mods.ftbultimine.api.shape.ShapeContext;
import dev.ftb.mods.ftbultimine.api.util.CanUltimineResult;
import dev.ftb.mods.ftbultimine.config.FTBUltimineClientConfig;
import dev.ftb.mods.ftbultimine.config.FTBUltimineServerConfig;
import dev.ftb.mods.ftbultimine.crops.CropLikeRegistry;
import dev.ftb.mods.ftbultimine.crops.VanillaCropLikeHandler;
import dev.ftb.mods.ftbultimine.integration.IntegrationHandler;
import dev.ftb.mods.ftbultimine.net.FTBUltimineNet;
import dev.ftb.mods.ftbultimine.net.SendShapePacket;
import dev.ftb.mods.ftbultimine.net.SyncUltimineTimePacket;
import dev.ftb.mods.ftbultimine.net.SyncUltimineTimePacket.TimeType;
import dev.ftb.mods.ftbultimine.registry.ModAttributes;
import dev.ftb.mods.ftbultimine.rightclick.*;
import dev.ftb.mods.ftbultimine.shape.*;
import dev.ftb.mods.ftbultimine.utils.ItemCollector;
import dev.ftb.mods.ftbultimine.utils.XPUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.Nullable;

import java.util.*;

public class FTBUltimine {
	@Nullable
	private static FTBUltimine instance;

	public static final Logger LOGGER = LogManager.getLogger();

	private final Map<UUID, FTBUltiminePlayerData> cachedDataMap = new HashMap<>();
	private final DropsAccumulator dropsAccumulator = new DropsAccumulator();
	private boolean playerTimeSyncNeeded;

	public FTBUltimine() {
		instance = this;

		FTBUltimineAPI._init(FTBUltimineAPIImpl.INSTANCE);

		ConfigManager.getInstance().registerClientConfig(FTBUltimineClientConfig.CONFIG, FTBUltimineAPI.MOD_ID + ".client_settings");
		ConfigManager.getInstance().registerServerConfig(FTBUltimineServerConfig.CONFIG, FTBUltimineAPI.MOD_ID + ".server_settings",
				true, FTBUltimineServerConfig::onConfigChanged);

		FTBUltimineNet.init();
		ModAttributes.init();
		IntegrationHandler.init();

		Platform.get().addDataPackReloadListener(FTBUltimineAPI.MOD_ID, FTBUltimineAPI.id("data_reload"), new DataReloadListener());
	}

	public static FTBUltimine getInstance() {
		return Objects.requireNonNull(instance);
	}

	public static void commonSetup(boolean clientSide) {
		NativeEventPosting.get().postEvent(new RegisterRestrictionHandlerEvent.Data(RestrictionHandlerRegistry.getInstance(clientSide)::register));
		NativeEventPosting.get().postEvent(new RegisterShapeEvent.Data(ShapeRegistry.getInstance(clientSide)::register));
	}

	public void registerBuiltinShapes(RegisterShapeEvent.Data event) {
		event.register(new ShapelessShape());
		event.register(new SmallTunnelShape());
		event.register(new SmallSquareShape());
		event.register(new LargeTunnelShape());
		event.register(new MiningTunnelShape());
		event.register(new EscapeTunnelShape());
	}

	public void registerBuiltinCropHandlers(RegisterCropLikeEvent.Data event) {
		event.registerHandler(VanillaCropLikeHandler.INSTANCE);
	}

	public void registerBuiltinRightClickHandlers(RegisterRightClickHandlerEvent.Data event) {
		event.registerHandler(AxeStripping.INSTANCE);
		event.registerHandler(ShovelFlattening.INSTANCE);
		event.registerHandler(FarmlandConversion.INSTANCE);
		event.registerHandler(CropHarvesting.INSTANCE);
	}

	public FTBUltiminePlayerData getOrCreatePlayerData(Player player) {
		return cachedDataMap.computeIfAbsent(player.getUUID(), FTBUltiminePlayerData::new);
	}

	public void playerJoined(ServerPlayer serverPlayer) {
		Server2PlayNetworking.send(serverPlayer, new SyncUltimineTimePacket(FTBUltimineServerConfig.getUltimineCooldown(serverPlayer), TimeType.COOLDOWN));
	}

	public void serverStarting(MinecraftServer ignoredServer) {
		NativeEventPosting.get().postEvent(new RegisterRightClickHandlerEvent.Data(RightClickDispatcher.getInstance()::registerHandler));
		NativeEventPosting.get().postEvent(new RegisterCropLikeEvent.Data(CropLikeRegistry.getInstance()::registerHandler));
		NativeEventPosting.get().postEvent(new RegisterBlockBreakHandlerEvent.Data(BlockBreakingRegistry.getInstance()::registerHandler));
		NativeEventPosting.get().postEvent(new RegisterBlockSelectionHandlerEvent.Data(BlockSelectionRegistry.getInstance()::registerHandler));
	}

	public void serverStopping(MinecraftServer ignoredServer) {
		cachedDataMap.clear();
		RightClickDispatcher.getInstance().clear();
		CropLikeRegistry.getInstance().clear();
		BlockBreakingRegistry.getInstance().clear();
		BlockSelectionRegistry.getInstance().clear();
	}

	public void serverTick(MinecraftServer server) {
		if (playerTimeSyncNeeded) {
			server.getPlayerList().getPlayers().forEach(sp ->
					Server2PlayNetworking.send(sp, new SyncUltimineTimePacket(FTBUltimineServerConfig.getUltimineCooldown(sp), TimeType.COOLDOWN))
			);
			playerTimeSyncNeeded = false;
		}
	}

	public void setKeyPressed(ServerPlayer player, boolean pressed) {
		FTBUltiminePlayerData data = getOrCreatePlayerData(player);
		data.setPressed(pressed);
		data.clearCache();

		if (!data.isPressed()) {
			Server2PlayNetworking.send(player, SendShapePacket.adjustShapeOnly(data.getCurrentShapeIndex()));
		}
	}

	public void modeChanged(ServerPlayer player, boolean next) {
		FTBUltiminePlayerData data = getOrCreatePlayerData(player);
		data.cycleShape(next);
		data.clearCache();
		Server2PlayNetworking.send(player, SendShapePacket.adjustShapeOnly(data.getCurrentShapeIndex()));
	}

	/// Validates if a tool is correct to use. If the strict tag is on an item it applies to the main and offhand slots.
	/// If to deny tag is on an item it'll deny the main hand item, not sure where this would be required... If the required
	/// tool config is on, the held item must either have a `Tool` component or have a max damage, or be added to
	/// the ALLOW_TAG item tag.
	///
	/// If no strict deny and no normal deny, and we do not require a tool via config then let everything through
	///
	/// @param player player being checked
	///
	/// @return if the player's equipped tool is valid to be used
	public static boolean isValidTool(Player player, BlockPos pos, BlockState state) {
		ItemStack mainHand = player.getMainHandItem();

		boolean hasAnyTool = !FTBUltimineServerConfig.REQUIRE_TOOL.get()
				|| !mainHand.isEmpty() && (mainHand.has(DataComponents.TOOL) || mainHand.getMaxDamage() > 0 || mainHand.is(FTBUltimineTags.Items.ALLOW_TAG));
		if (!hasAnyTool) {
			return false;
		}

		return !FTBUltimineServerConfig.REQUIRE_VALID_TOOL_FOR_BLOCK.get()
				|| !state.requiresCorrectToolForDrops()
				|| Platform.get().misc().playerHasCorrectTool(player, pos, state);
	}

	/// Determines if the player is currently allowed to use Ultimine. Provides the reason it is not allowed as relevant.
	///
	/// Does not determine if there is a valid target for ultimine, hence the "ALLOWED" result pointing at the
	/// "no valid block" reason. If there is a result of ALLOWED and valid blocks returned from
	/// [FTBUltiminePlayerData#updateBlocks], ultimine should be active and working.
	///
	/// @param player Player to check status for
	/// @param state the blockstate being broken (or about to be broken)
	/// @return Result object with the reason that ultimine is not allowed.
	public CanUltimineResult canUltimine(Player player, BlockPos pos, BlockState state) {
		if (Platform.get().misc().isFakePlayer(player)) {
			return CanUltimineResult.OTHER_RESTRICTION;
		}

		if (CooldownTracker.isOnCooldown(player)) {
			return CanUltimineResult.ON_COOLDOWN;
		}

		if (player.getFoodData().getFoodLevel() <= 0 && !player.isCreative()) {
			return CanUltimineResult.NO_FOOD;
		}

		var mainHand = player.getMainHandItem();
		var offHand = player.getOffhandItem();
		/* Check if the current tool has a deny tag. strict deny applies to either hand. */
		if (mainHand.is(FTBUltimineTags.Items.STRICT_DENY_TAG) || offHand.is(FTBUltimineTags.Items.STRICT_DENY_TAG) || mainHand.is(FTBUltimineTags.Items.DENY_TAG)) {
			return CanUltimineResult.BLOCKED_TOOL;
		}

		return isValidTool(player, pos, state) ?
				RestrictionHandlerRegistry.getInstance(player.level().isClientSide()).canUltimine(player) :
				CanUltimineResult.NO_TOOL;
	}

	public boolean handleBlockBreak(LevelAccessor level, BlockPos origPos, BlockState state, ServerPlayer player) {
		if (dropsAccumulator.isActive() || !canUltimine(player, origPos, state).isAllowed()) {
			return false;
		}

		FTBUltiminePlayerData data = getOrCreatePlayerData(player);
		if (!data.isPressed()) {
			return false;
		}

		HitResult hitResult = FTBUltiminePlayerData.rayTrace(player);
		if (!(hitResult instanceof BlockHitResult bhr) || hitResult.getType() != HitResult.Type.BLOCK) {
			return false;
		}

		data.clearCache();
		data.updateBlocks(player, origPos, bhr.getDirection(), false, FTBUltimineServerConfig.getMaxBlocks(player));

		if (!data.hasCachedPositions()) {
			return false;
		}

		double xpPerBlock = FTBUltimineServerConfig.getExperiencePerBlock(player);
		if (xpPerBlock > 0.0 && XPUtils.getPlayerXP(player) < Objects.requireNonNull(data.cachedPositions()).size() * xpPerBlock) {
			return false;
		}

		try {
			dropsAccumulator.start();
			breakBlocksInShape(level, origPos, state, player, bhr, data);
		} finally {
			dropsAccumulator.finish(player.level(), origPos);
		}

		data.clearCache();
		Server2PlayNetworking.send(player, SendShapePacket.adjustShapeAndBlockPos(data.getCurrentShapeIndex(), List.of()));

		return true;
	}

	private static void breakBlocksInShape(LevelAccessor level, BlockPos origPos, BlockState state, ServerPlayer player, BlockHitResult bhr, FTBUltiminePlayerData data) {
		int blocksMined = 0;
		boolean hadItem = !player.getMainHandItem().isEmpty();
		Shape shape = data.getCurrentShape();
		float baseSpeed = state.getDestroySpeed(level, origPos);

		for (BlockPos pos : Objects.requireNonNull(data.cachedPositions())) {
			BlockState state1 = level.getBlockState(pos);

			float destroySpeed = state1.getDestroySpeed(level, pos);
			if (!player.isCreative() && (destroySpeed < 0 || destroySpeed > baseSpeed || !isValidTool(player, pos, state1))) {
				continue;
			}

			if (!tryBreakOneBlock(player, pos, state, shape, bhr) && FTBUltimineServerConfig.CANCEL_ON_BLOCK_BREAK_FAIL.get()) {
				break;
			}

			if (!player.isCreative()) {
				player.causeFoodExhaustion((float) (FTBUltimineServerConfig.getExhaustionPerBlock(player) * 0.005D));
				if (FTBUltimineAPI.isTooExhausted(player)) {
					break;
				}
			}

			ItemStack stack = player.getMainHandItem();
			if (hadItem && stack.isEmpty()) {
				break;
			} else if (hadItem && FTBUltimineServerConfig.PREVENT_TOOL_BREAK.get() > 0 && stack.isDamageableItem() && stack.getDamageValue() >= stack.getMaxDamage() - FTBUltimineServerConfig.PREVENT_TOOL_BREAK.get()) {
				break;
			}

			blocksMined++;
		}

		BlockBreakingRegistry.INSTANCE.getHandlers().forEach(h -> h.postBreak(player));

		if (!player.isCreative()) {
			CooldownTracker.setLastUltimineTime(player, System.currentTimeMillis());
			data.addPendingXPCost(player, Math.max(0, blocksMined - 1));
		}
	}

	private static boolean tryBreakOneBlock(ServerPlayer player, BlockPos pos, BlockState state, Shape shape, BlockHitResult bhr) {
		try {
			for (var handler : BlockBreakingRegistry.INSTANCE.getHandlers()) {
				switch (handler.breakBlock(player, pos, state, shape, bhr)) {
					case SUCCESS:
						return true;
					case FAIL:
						return false;
				}
			}
			return player.gameMode.destroyBlock(pos);
		} catch (Exception ex) {
			FTBUltimine.LOGGER.error("caught exception while trying to break block {} @ {} / {}",
					state, player.level().dimension().identifier(), pos);
			ex.printStackTrace();
			return false;
		}
	}

	public InteractionResult blockRightClick(Player player, InteractionHand hand, BlockPos clickPos) {
		if (Platform.get().misc().isFakePlayer(player) && CropLikeRegistry.checkForSingleCropHarvesting(player, clickPos)) {
			// minor kludge: we don't normally allow fake players for ultimining, but single crop harvesting is an exception
			return InteractionResult.SUCCESS_SERVER;
		}

		if (!(player instanceof ServerPlayer serverPlayer) || Platform.get().misc().isFakePlayer(player)) {
			return InteractionResult.PASS;
		}

		FTBUltiminePlayerData data = getOrCreatePlayerData(player);
		if (!data.isPressed()) {
			if (CropLikeRegistry.checkForSingleCropHarvesting(player, clickPos)) {
				return InteractionResult.SUCCESS;
			} else {
				return InteractionResult.PASS;
			}
		}

		if (player.getFoodData().getFoodLevel() <= 0 && !player.isCreative()) {
			return InteractionResult.PASS;
		}

		HitResult result = FTBUltiminePlayerData.rayTrace(serverPlayer);
		if (!(result instanceof BlockHitResult blockHitResult) || result.getType() != HitResult.Type.BLOCK) {
			return InteractionResult.PASS;
		}

		data.clearCache();

		ShapeContext shapeContext = data.updateBlocks(serverPlayer, clickPos, blockHitResult.getDirection(), false, FTBUltimineServerConfig.getMaxBlocks(serverPlayer));
		if (shapeContext == null || !data.hasCachedPositions()) {
			return InteractionResult.PASS;
		}

		int didWork = RightClickDispatcher.INSTANCE.dispatchRightClick(shapeContext, hand, data);
		if (didWork > 0) {
			serverPlayer.swing(hand);
			if (!player.isCreative()) {
				CooldownTracker.setLastUltimineTime(player, System.currentTimeMillis());
				data.addPendingXPCost(serverPlayer, Math.max(0, didWork - 1));
			}
			return InteractionResult.SUCCESS_SERVER;
		} else {
			return InteractionResult.PASS;
		}
	}

	public void playerTick(Player player) {
		if (player instanceof ServerPlayer serverPlayer) {
			FTBUltiminePlayerData data = getOrCreatePlayerData(player);
			data.checkBlocks(serverPlayer, true, () -> FTBUltimineServerConfig.getMaxBlocks(serverPlayer));
			data.takePendingXP(serverPlayer);
		}
	}

	public boolean handleItemAndOrbJoining(Entity entity, Level level) {
		return dropsAccumulator.handleEntity(entity, level);
	}

	public void schedulePlayerTimeSync() {
		playerTimeSyncNeeded = true;
	}

	private static class DataReloadListener implements ResourceManagerReloadListener {
		@Override
		public void onResourceManagerReload(ResourceManager resourceManager) {
			BlockMatcher.TagCache.onReload();
		}
	}

	/// Keeps track of blocks and exp orbs that are dropped while an ultimining operation is in progress.
	private static class DropsAccumulator {
		private boolean isBreakingBlock;
		private int tempBlockDroppedXp;
		private final Lazy<ItemCollector> tempBlockDropsList = Lazy.of(ItemCollector::new);

		public void start() {
			isBreakingBlock = true;
			tempBlockDroppedXp = 0;
			tempBlockDropsList.get().clear();
		}

		public void finish(ServerLevel level, BlockPos origPos) {
			isBreakingBlock = false;
			tempBlockDropsList.get().drop(level, origPos);
			tempBlockDropsList.get().clear();
			if (tempBlockDroppedXp > 0) {
				level.addFreshEntity(new ExperienceOrb(level, origPos.getX() + 0.5D, origPos.getY() + 0.5D, origPos.getZ() + 0.5D, tempBlockDroppedXp));
			}
		}

		public boolean handleEntity(Entity entity, Level level) {
			// Other mods may have already intercepted this event to do similar absorption;
			//  the only way to be sure if the entity is still valid is to check if it's alive,
			//  and hope other mods killed the entity if they've absorbed it.
			if (entity.isAlive() && level instanceof ServerLevel serverLevel && isBreakingBlock) {
				if (entity instanceof ItemEntity item) {
					if (!item.getItem().isEmpty()) {
						tempBlockDropsList.get().accept(item.getItem());
						item.setItem(ItemStack.EMPTY);
					}
					return true;
				} else if (entity instanceof ExperienceOrb orb) {
					tempBlockDroppedXp += orb.getValue();
					entity.kill(serverLevel);
					return true;
				}
			}
			return false;
		}

		public boolean isActive() {
			return isBreakingBlock;
		}
	}
}
