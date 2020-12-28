package com.feed_the_beast.mods.ftbultimine;

import com.feed_the_beast.mods.ftbultimine.client.FTBUltimineClient;
import com.feed_the_beast.mods.ftbultimine.net.FTBUltimineNet;
import com.feed_the_beast.mods.ftbultimine.net.SendShapePacket;
import com.feed_the_beast.mods.ftbultimine.shape.BlockMatcher;
import com.feed_the_beast.mods.ftbultimine.shape.EscapeTunnelShape;
import com.feed_the_beast.mods.ftbultimine.shape.MiningTunnelShape;
import com.feed_the_beast.mods.ftbultimine.shape.Shape;
import com.feed_the_beast.mods.ftbultimine.shape.ShapeContext;
import com.feed_the_beast.mods.ftbultimine.shape.ShapelessShape;
import com.feed_the_beast.mods.ftbultimine.shape.SmallSquareShape;
import com.feed_the_beast.mods.ftbultimine.shape.SmallTunnelShape;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author LatvianModder
 */
@Mod("ftbultimine")
public class FTBUltimine
{
	public static FTBUltimine instance;

	public final FTBUltimineCommon proxy;

	private Map<UUID, FTBUltiminePlayerData> cachedDataMap;
	private boolean isBreakingBlock;
	private int tempBlockDroppedXp;
	private ItemCollection tempBlockDropsList;

	public FTBUltimine()
	{
		instance = this;
		FTBUltimineNet.init();
		MinecraftForge.EVENT_BUS.register(this);

		proxy = DistExecutor.safeRunForDist(() -> FTBUltimineClient::new, () -> FTBUltimineCommon::new);
		FTBUltimineConfig.init();

		Shape.register(new ShapelessShape());
		Shape.register(new SmallTunnelShape());
		Shape.register(new SmallSquareShape());
		Shape.register(new MiningTunnelShape());
		Shape.register(new EscapeTunnelShape());

		Shape.postinit();
	}

	public FTBUltiminePlayerData get(Player player)
	{
		return cachedDataMap.computeIfAbsent(player.getUUID(), FTBUltiminePlayerData::new);
	}

	@SubscribeEvent
	public void serverAboutToStart(FMLServerAboutToStartEvent event)
	{
		cachedDataMap = new HashMap<>();
	}

	public void setKeyPressed(ServerPlayer player, boolean pressed)
	{
		FTBUltiminePlayerData data = get(player);
		data.pressed = pressed;
		data.clearCache();

		if (!data.pressed)
		{
			FTBUltimineNet.MAIN.send(PacketDistributor.PLAYER.with(() -> player), new SendShapePacket(data.shape, Collections.emptyList()));
		}
	}

	public void modeChanged(ServerPlayer player, boolean next)
	{
		FTBUltiminePlayerData data = get(player);
		data.shape = next ? data.shape.next : data.shape.prev;
		data.clearCache();
		FTBUltimineNet.MAIN.send(PacketDistributor.PLAYER.with(() -> player), new SendShapePacket(data.shape, Collections.emptyList()));
	}

	private int getMaxBlocks(Player player)
	{
		return FTBUltimineConfig.maxBlocks;
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public void blockBroken(BlockEvent.BreakEvent event)
	{
		if (isBreakingBlock)
		{
			return;
		}

		if (!(event.getPlayer() instanceof ServerPlayer) || event.getPlayer() instanceof FakePlayer || event.getPlayer().getUniqueID() == null)
		{
			return;
		}

		ServerPlayer player = (ServerPlayer) event.getPlayer();

		if (player.getFoodData().getFoodLevel() <= 0 && !player.isCreative())
		{
			return;
		}

		if (FTBUltimineConfig.toolBlacklist.contains(player.getMainHandItem().getItem().getRegistryName()))
		{
			return;
		}

		FTBUltiminePlayerData data = get(player);

		if (!data.pressed)
		{
			return;
		}

		HitResult result = FTBUltiminePlayerData.rayTrace(player);

		if (!(result instanceof BlockHitResult) || result.getType() != RayTraceResult.Type.BLOCK)
		{
			return;
		}

		data.clearCache();
		data.updateBlocks(player, event.getPos(), ((BlockHitResult) result).getDirection(), false, getMaxBlocks(player));

		if (data.cachedBlocks == null || data.cachedBlocks.isEmpty())
		{
			return;
		}

		isBreakingBlock = true;
		tempBlockDropsList = new ItemCollection();
		tempBlockDroppedXp = 0;
		boolean hadItem = !player.getMainHandItem().isEmpty();

		for (BlockPos p : data.cachedBlocks)
		{
			if (!player.gameMode.destroyBlock(p))
			{
				continue;
			}

			if (!player.isCreative())
			{
				player.causeFoodExhaustion((float) (FTBUltimineConfig.exhaustionPerBlock * 0.005D));

				if (player.getFoodData().getFoodLevel() <= 0)
				{
					break;
				}
			}

			if (hadItem && player.getMainHandItem().isEmpty())
			{
				break;
			}
		}

		isBreakingBlock = false;

		tempBlockDropsList.drop(player.level, event.getPos());

		if (tempBlockDroppedXp > 0)
		{
			player.level.addFreshEntity(new ExperienceOrb(player.level, event.getPos().getX() + 0.5D, event.getPos().getY() + 0.5D, event.getPos().getZ() + 0.5D, tempBlockDroppedXp));
		}

		data.clearCache();
		FTBUltimineNet.MAIN.send(PacketDistributor.PLAYER.with(() -> player), new SendShapePacket(data.shape, Collections.emptyList()));
		event.setCanceled(true);
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public void blockRightClick(PlayerInteractEvent.RightClickBlock event)
	{
		if (!(event.getPlayer() instanceof ServerPlayer) || event.getPlayer() instanceof FakePlayer || event.getPlayer().getUniqueID() == null)
		{
			return;
		}

		if (event.getPlayer().getFoodStats().getFoodLevel() <= 0 && !event.getPlayer().isCreative())
		{
			return;
		}

		ServerPlayer player = (ServerPlayer) event.getPlayer();
		HitResult result = FTBUltiminePlayerData.rayTrace(player);

		if (!(result instanceof BlockHitResult) || result.getType() != RayTraceResult.Type.BLOCK)
		{
			return;
		}

		FTBUltiminePlayerData data = get(player);
		data.clearCache();
		ShapeContext shapeContext = data.updateBlocks(player, event.getPos(), ((BlockHitResult) result).getDirection(), false, getMaxBlocks(player));

		if (shapeContext == null || !data.pressed || data.cachedBlocks == null || data.cachedBlocks.isEmpty())
		{
			return;
		}

		if (event.getItemStack().getItem() instanceof HoeItem)
		{
			ResourceLocation dirtTag = new ResourceLocation("forge", "dirt");

			if (!player.level.isClientSide())
			{
				boolean playSound = false;
				BrokenItemHandler brokenItemHandler = new BrokenItemHandler();

				for (int i = 0; i < Math.min(data.cachedBlocks.size(), FTBUltimineConfig.maxBlocks); i++)
				{
					BlockPos p = data.cachedBlocks.get(i);
					BlockState state = player.level.getBlockState(p);

					if (!state.getBlock().getTags().contains(dirtTag))
					{
						continue;
					}

					player.level.setBlock(p, Blocks.FARMLAND.defaultBlockState(), Constants.BlockFlags.DEFAULT_AND_RERENDER);
					playSound = true;

					if (!player.isCreative())
					{
						player.getMainHandItem().hurtAndBreak(1, player, brokenItemHandler);
						player.causeFoodExhaustion((float) (FTBUltimineConfig.exhaustionPerBlock * 0.005D));

						if (brokenItemHandler.isBroken || player.getFoodData().getFoodLevel() <= 0)
						{
							break;
						}
					}
				}

				if (playSound)
				{
					player.level.playSound(player, event.getPos(), SoundEvents.HOE_TILL, SoundCategory.BLOCKS, 1F, 1F);
				}
			}

			player.swing(event.getHand());
			event.setCanceled(true);
		}
		else if (shapeContext.matcher == BlockMatcher.BUSH)
		{
			ItemCollection itemCollection = new ItemCollection();

			for (BlockPos pos : data.cachedBlocks)
			{
				BlockState state = player.level.getBlockState(pos);

				if (!(state.getBlock() instanceof CropBlock))
				{
					continue;
				}

				CropBlock c = (CropBlock) state.getBlock();

				if (!c.isMaxAge(state))
				{
					continue;
				}

				if (player.level.isClientSide())
				{
					event.setCanceled(true);
					player.swing(event.getHand());
					continue;
				}

				List<ItemStack> drops = Block.getDrops(state, (ServerLevel) player.level, pos, state.hasTileEntity() ? player.level.getBlockEntity(pos) : null, player, ItemStack.EMPTY);
				Item seedItem = c.getCloneItemStack(player.level, pos, state).getItem();

				for (ItemStack stack : drops)
				{
					if (stack.getItem() == seedItem)
					{
						stack.shrink(1);
					}

					itemCollection.add(stack);
				}

				player.level.setBlock(pos, c.getStateForAge(0), Constants.BlockFlags.DEFAULT);
			}

			itemCollection.drop(player.level, event.getFace() == null ? event.getPos() : event.getPos().offset(event.getFace()));
			player.swing(event.getHand());
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public void playerTick(TickEvent.PlayerTickEvent event)
	{
		if (event.phase == TickEvent.Phase.START && !event.player.world.isRemote())
		{
			FTBUltiminePlayerData data = get(event.player);
			data.checkBlocks((ServerPlayer) event.player, true, getMaxBlocks(event.player));
		}
	}

	@SubscribeEvent
	public void playerLoaded(PlayerEvent.LoadFromFile event)
	{
	}

	@SubscribeEvent
	public void playerSaved(PlayerEvent.SaveToFile event)
	{
	}

	@SubscribeEvent
	public void entityJoinedWorld(EntityJoinWorldEvent event)
	{
		if (isBreakingBlock && event.getEntity() instanceof ItemEntity)
		{
			tempBlockDropsList.add(((ItemEntity) event.getEntity()).getItem());
			event.setCanceled(true);
		}
		else if (isBreakingBlock && event.getEntity() instanceof ExperienceOrb)
		{
			tempBlockDroppedXp += ((ExperienceOrb) event.getEntity()).getValue();
			event.setCanceled(true);
		}
	}
}