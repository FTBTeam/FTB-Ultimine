package dev.ftb.mods.ftbultimine;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.*;
import dev.architectury.hooks.level.entity.PlayerHooks;
import dev.architectury.platform.Platform;
import dev.architectury.registry.ReloadListenerRegistry;
import dev.architectury.utils.EnvExecutor;
import dev.architectury.utils.value.IntValue;
import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;
import dev.ftb.mods.ftbultimine.client.FTBUltimineClient;
import dev.ftb.mods.ftbultimine.config.FTBUltimineCommonConfig;
import dev.ftb.mods.ftbultimine.config.FTBUltimineServerConfig;
import dev.ftb.mods.ftbultimine.integration.FTBRanksIntegration;
import dev.ftb.mods.ftbultimine.integration.FTBUltiminePlugins;
import dev.ftb.mods.ftbultimine.net.FTBUltimineNet;
import dev.ftb.mods.ftbultimine.net.SendShapePacket;
import dev.ftb.mods.ftbultimine.net.SyncConfigFromServerPacket;
import dev.ftb.mods.ftbultimine.net.SyncUltimineTimePacket;
import dev.ftb.mods.ftbultimine.net.SyncUltimineTimePacket.TimeType;
import dev.ftb.mods.ftbultimine.shape.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

public class FTBUltimine {
	public static FTBUltimine instance;

	public static final String MOD_ID = "ftbultimine";
	public static final Logger LOGGER = LogManager.getLogger();

	public final FTBUltimineCommon proxy;

	public static boolean ranksMod = false;

	private Map<UUID, FTBUltiminePlayerData> cachedDataMap;
	private boolean isBreakingBlock;
	private int tempBlockDroppedXp;
	private ItemCollection tempBlockDropsList;

	public static final TagKey<Item> DENY_TAG = TagKey.create(Registries.ITEM, new ResourceLocation(MOD_ID, "excluded_tools"));
	public static final TagKey<Item> STRICT_DENY_TAG = TagKey.create(Registries.ITEM, new ResourceLocation(MOD_ID, "excluded_tools/strict"));
	public static final TagKey<Item> ALLOW_TAG = TagKey.create(Registries.ITEM, new ResourceLocation(MOD_ID, "included_tools"));

	public static final TagKey<Block> EXCLUDED_BLOCKS = TagKey.create(Registries.BLOCK, new ResourceLocation(MOD_ID, "excluded_blocks"));
	public static final TagKey<Block> BLOCK_WHITELIST = TagKey.create(Registries.BLOCK, new ResourceLocation(MOD_ID, "block_whitelist"));
	public static final TagKey<Block> TILLABLE_TAG = TagKey.create(Registries.BLOCK, new ResourceLocation(MOD_ID, "farmland_tillable"));
	public static final TagKey<Block> FLATTENABLE_TAG = TagKey.create(Registries.BLOCK, new ResourceLocation(MOD_ID, "shovel_flattenable"));

	private static Predicate<Player> permissionOverride = player -> true;

	public static void setPermissionOverride(Predicate<Player> p) {
		permissionOverride = p;
	}

	public FTBUltimine() {
		instance = this;
		FTBUltimineNet.init();

		if (Platform.isModLoaded("ftbranks")) {
			ranksMod = true;
			FTBRanksIntegration.init();
		}

		proxy = EnvExecutor.getEnvSpecific(() -> FTBUltimineClient::new, () -> FTBUltimineCommon::new);

		FTBUltimineCommonConfig.load();
		FTBUltiminePlugins.init();

		ReloadListenerRegistry.register(PackType.SERVER_DATA, new DataReloadListener());

		ShapeRegistry.register(new ShapelessShape(), true);
		ShapeRegistry.register(new SmallTunnelShape());
		ShapeRegistry.register(new SmallSquareShape());
		ShapeRegistry.register(new LargeTunnelShape());
		ShapeRegistry.register(new MiningTunnelShape());
		ShapeRegistry.register(new EscapeTunnelShape());

		PlayerEvent.PLAYER_JOIN.register(this::playerJoined);
		LifecycleEvent.SERVER_BEFORE_START.register(this::serverStarting);
		BlockEvent.BREAK.register(this::blockBroken);
		InteractionEvent.RIGHT_CLICK_BLOCK.register(this::blockRightClick);
		TickEvent.PLAYER_PRE.register(this::playerTick);
		EntityEvent.ADD.register(this::entityJoinedWorld);
		CommandRegistrationEvent.EVENT.register(FTBUltimineCommands::registerCommands);
	}

	@NotNull
	public FTBUltiminePlayerData getOrCreatePlayerData(Player player) {
		return cachedDataMap.computeIfAbsent(player.getUUID(), FTBUltiminePlayerData::new);
	}

	private void playerJoined(ServerPlayer serverPlayer) {
		SNBTCompoundTag config = new SNBTCompoundTag();
		FTBUltimineServerConfig.CONFIG.write(config);
		new SyncConfigFromServerPacket(config).sendTo(serverPlayer);
		new SyncUltimineTimePacket(FTBUltimineServerConfig.getUltimineCooldown(serverPlayer), TimeType.COOLDOWN).sendTo(serverPlayer);
	}

	private void serverStarting(MinecraftServer server) {
		ShapeRegistry.freeze();
		cachedDataMap = new HashMap<>();
		FTBUltimineServerConfig.load(server);
	}

	public void setKeyPressed(ServerPlayer player, boolean pressed) {
		FTBUltiminePlayerData data = getOrCreatePlayerData(player);
		data.setPressed(pressed);
		data.clearCache();

		if (!data.isPressed()) {
			new SendShapePacket(data.getCurrentShapeIndex(), Collections.emptyList()).sendTo(player);
		}
	}

	public void modeChanged(ServerPlayer player, boolean next) {
		FTBUltiminePlayerData data = getOrCreatePlayerData(player);
		data.cycleShape(next);
		data.clearCache();
		new SendShapePacket(data.getCurrentShapeIndex(), Collections.emptyList()).sendTo(player);
	}

	/**
	 * Validates if a tool is correct to use. If the strict tag is on an item it applies to the main and offhand slots.
	 * If to deny tag is on an item it'll deny the main hand item, not sure where this would be required... If the required
	 * tool config is on, we have to be either a {@link TieredItem} or have a max damage, or be added to the ALLOW_TAG.
	 *
	 * If no strict deny and no normal deny, and we do not require a tool via config then let everything through
	 *
	 * @param mainHand item in the main hand
	 * @param offHand  item in the offhand
	 *
	 * @return if the tool is valid to be used
	 */
	public static boolean isValidTool(ItemStack mainHand, ItemStack offHand) {
		if (mainHand.is(STRICT_DENY_TAG) || offHand.is(STRICT_DENY_TAG) || mainHand.is(DENY_TAG)) {
			return false;
		}

		if (FTBUltimineServerConfig.REQUIRE_TOOL.get()) {
			if (mainHand.isEmpty()) {
				return false;
			}

			return mainHand.getItem() instanceof TieredItem || mainHand.getMaxDamage() > 0 || mainHand.is(ALLOW_TAG);
		}

		return true;
	}

	public boolean canUltimine(Player player) {
		if (PlayerHooks.isFake(player) || player.getUUID() == null || CooldownTracker.isOnCooldown(player)) {
			return false;
		}

		if (player.getFoodData().getFoodLevel() <= 0 && !player.isCreative()) {
			return false;
		}

		if (!permissionOverride.test(player)) {
			return false;
		}

		var mainHand = player.getMainHandItem();
		var offHand = player.getOffhandItem();
		return isValidTool(mainHand, offHand) && FTBUltiminePlugins.canUltimine(player);
	}

	public EventResult blockBroken(Level world, BlockPos pos, BlockState state, ServerPlayer player, @Nullable IntValue xp) {
		if (isBreakingBlock || !canUltimine(player)) {
			return EventResult.pass();
		}

		FTBUltiminePlayerData data = getOrCreatePlayerData(player);

		if (!data.isPressed()) {
			return EventResult.pass();
		}

		HitResult result = FTBUltiminePlayerData.rayTrace(player);

		if (!(result instanceof BlockHitResult) || result.getType() != HitResult.Type.BLOCK) {
			return EventResult.pass();
		}

		data.clearCache();
		data.updateBlocks(player, pos, ((BlockHitResult) result).getDirection(), false, FTBUltimineServerConfig.getMaxBlocks(player));

		if (!data.hasCachedPositions()) {
			return EventResult.pass();
		}

		if (player.totalExperience < data.cachedPositions().size() * FTBUltimineServerConfig.EXPERIENCE_PER_BLOCK.get()) {
			return EventResult.pass();
		}

		isBreakingBlock = true;
		tempBlockDropsList = new ItemCollection();
		tempBlockDroppedXp = 0;
		boolean hadItem = !player.getMainHandItem().isEmpty();

		float baseSpeed = state.getDestroySpeed(world, pos);
		int blocksMined = 0;
		for (BlockPos p : data.cachedPositions()) {
			BlockState state1 = world.getBlockState(p);
			float destroySpeed = state1.getDestroySpeed(world, p);
			if (!player.isCreative() && (destroySpeed < 0 || destroySpeed > baseSpeed || !player.hasCorrectToolForDrops(state1))) {
				continue;
			}
			if (!player.gameMode.destroyBlock(p) && FTBUltimineServerConfig.CANCEL_ON_BLOCK_BREAK_FAIL.get()) {
				break;
			}

			if (!player.isCreative()) {
				player.causeFoodExhaustion((float) (FTBUltimineServerConfig.EXHAUSTION_PER_BLOCK.get() * 0.005D));
				if (isTooExhausted(player)) {
					break;
				}
			}

			ItemStack stack = player.getMainHandItem();
			if (hadItem && stack.isEmpty()) {
				break;
			} else if (hadItem && stack.hasTag() && stack.getTag().getBoolean("tic_broken")) {
				break;
			} else if (hadItem && FTBUltimineServerConfig.PREVENT_TOOL_BREAK.get() > 0 && stack.isDamageableItem() && stack.getDamageValue() >= stack.getMaxDamage() - FTBUltimineServerConfig.PREVENT_TOOL_BREAK.get()) {
				break;
			}

			blocksMined++;
		}

		if (!player.isCreative()) {
			CooldownTracker.setLastUltimineTime(player, System.currentTimeMillis());
			data.addPendingXPCost(Math.max(0, blocksMined - 1));
		}

		isBreakingBlock = false;

		tempBlockDropsList.drop(player.level(), pos);

		if (tempBlockDroppedXp > 0) {
			player.level().addFreshEntity(new ExperienceOrb(player.level(), pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, tempBlockDroppedXp));
		}

		data.clearCache();
		new SendShapePacket(data.getCurrentShapeIndex(), Collections.emptyList()).sendTo(player);

		return EventResult.interruptFalse();
	}

	public EventResult blockRightClick(Player player, InteractionHand hand, BlockPos clickPos, Direction face) {
		if (!(player instanceof ServerPlayer serverPlayer) || PlayerHooks.isFake(player) || player.getUUID() == null) {
			return EventResult.pass();
		}

		FTBUltiminePlayerData data = getOrCreatePlayerData(player);
		if (!data.isPressed()) {
			return EventResult.pass();
		}

		if (player.getFoodData().getFoodLevel() <= 0 && !player.isCreative()) {
			return EventResult.pass();
		}

		HitResult result = FTBUltiminePlayerData.rayTrace(serverPlayer);

		if (!(result instanceof BlockHitResult blockHitResult) || result.getType() != HitResult.Type.BLOCK) {
			return EventResult.pass();
		}

		data.clearCache();
		ShapeContext shapeContext = data.updateBlocks(serverPlayer, clickPos, blockHitResult.getDirection(), false, FTBUltimineServerConfig.getMaxBlocks(serverPlayer));

		if (shapeContext == null || !data.isPressed() || !data.hasCachedPositions()) {
			return EventResult.pass();
		}

		int didWork = 0;
		if (FTBUltimineServerConfig.RIGHT_CLICK_HARVESTING.get() && shapeContext.matcher() == BlockMatcher.CROP_LIKE) {
			didWork = RightClickHandlers.cropHarvesting(serverPlayer, hand, clickPos, face, data);
		} else if (FTBUltimineServerConfig.RIGHT_CLICK_HOE.get() && serverPlayer.getItemInHand(hand).getItem() instanceof HoeItem) {
			didWork = RightClickHandlers.farmlandConversion(serverPlayer, hand, clickPos, data);
		} else if (FTBUltimineServerConfig.RIGHT_CLICK_AXE.get() && serverPlayer.getItemInHand(hand).getItem() instanceof AxeItem) {
			didWork = RightClickHandlers.axeStripping(serverPlayer, hand, clickPos, data);
		} else if (FTBUltimineServerConfig.RIGHT_CLICK_SHOVEL.get() && serverPlayer.getItemInHand(hand).getItem() instanceof ShovelItem) {
			didWork = RightClickHandlers.shovelFlattening(serverPlayer, hand, clickPos, data);
		}

		if (didWork > 0) {
			player.swing(hand);
			if (!player.isCreative()) {
				CooldownTracker.setLastUltimineTime(player, System.currentTimeMillis());
				data.addPendingXPCost(Math.max(0, didWork - 1));
			}
			return EventResult.interruptFalse();
		} else {
			return EventResult.pass();
		}
	}

	public void playerTick(Player player) {
		if (player instanceof ServerPlayer serverPlayer) {
			FTBUltiminePlayerData data = getOrCreatePlayerData(player);
			data.checkBlocks(serverPlayer, true, FTBUltimineServerConfig.getMaxBlocks(serverPlayer));
			data.takePendingXP(serverPlayer);
		}
	}

	public EventResult entityJoinedWorld(Entity entity, Level level) {
		// Other mods may have already intercepted this event to do similar absorption;
		//  the only way to be sure if the entity is still valid is to check if it's alive,
		//  and hope other mods killed the entity if they've absorbed it.
		if (entity.isAlive()) {
			if (isBreakingBlock && entity instanceof ItemEntity item) {
				if (!item.getItem().isEmpty()) {
					tempBlockDropsList.add(item.getItem());
					item.setItem(ItemStack.EMPTY);
				}
				return EventResult.interruptFalse();
			} else if (isBreakingBlock && entity instanceof ExperienceOrb orb) {
				tempBlockDroppedXp += orb.getValue();
				entity.kill();
				return EventResult.interruptFalse();
			}
		}
		return EventResult.pass();
	}

	public static ResourceLocation id(String path) {
		return new ResourceLocation(MOD_ID, path);
	}

	public static boolean isTooExhausted(ServerPlayer player) {
		FoodData data = player.getFoodData();
		return data.getExhaustionLevel() / 4f > data.getSaturationLevel() + data.getFoodLevel();
	}

	private static class DataReloadListener implements ResourceManagerReloadListener {
		@Override
		public void onResourceManagerReload(ResourceManager resourceManager) {
			BlockMatcher.TagCache.onReload();
		}
	}
}
