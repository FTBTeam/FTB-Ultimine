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
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropsBlock;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.HoeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;
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
	public final FTBUltimineConfig config;

	private Map<UUID, FTBUltiminePlayerData> cachedDataMap;
	private boolean isBreakingBlock;
	private int tempBlockDroppedXp;
	private ItemCollection tempBlockDropsList;

	public FTBUltimine()
	{
		instance = this;
		FTBUltimineNet.init();
		MinecraftForge.EVENT_BUS.register(this);

		//noinspection Convert2MethodRef
		proxy = DistExecutor.runForDist(() -> () -> new FTBUltimineClient(), () -> () -> new FTBUltimineCommon());
		config = new FTBUltimineConfig();

		Shape.register(new ShapelessShape());
		Shape.register(new SmallTunnelShape());
		Shape.register(new SmallSquareShape());
		Shape.register(new MiningTunnelShape());
		Shape.register(new EscapeTunnelShape());
	}

	public FTBUltiminePlayerData get(PlayerEntity player)
	{
		return cachedDataMap.computeIfAbsent(player.getUniqueID(), FTBUltiminePlayerData::new);
	}

	@SubscribeEvent
	public void serverAboutToStart(FMLServerAboutToStartEvent event)
	{
		cachedDataMap = new HashMap<>();
	}

	public void setKeyPressed(ServerPlayerEntity player, boolean pressed)
	{
		FTBUltiminePlayerData data = get(player);
		data.pressed = pressed;
		data.clearCache();

		if (!data.pressed)
		{
			FTBUltimineNet.MAIN.send(PacketDistributor.PLAYER.with(() -> player), new SendShapePacket(Collections.emptyList()));
		}
	}

	public void modeChanged(ServerPlayerEntity player)
	{
		FTBUltiminePlayerData data = get(player);
		data.clearCache();
		data.shape = data.shape.next();
		player.sendStatusMessage(new TranslationTextComponent("ftbultimine.shape_changed", new TranslationTextComponent("ftbultimine.shape." + data.shape.getName())), true);
	}

	private int getMaxBlocks(PlayerEntity player)
	{
		return config.maxBlocks;
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public void blockBroken(BlockEvent.BreakEvent event)
	{
		if (isBreakingBlock)
		{
			return;
		}

		if (!(event.getPlayer() instanceof ServerPlayerEntity) || event.getPlayer() instanceof FakePlayer || event.getPlayer().getUniqueID() == null)
		{
			return;
		}

		if (event.getPlayer().getFoodStats().getFoodLevel() <= 0 && !event.getPlayer().isCreative())
		{
			return;
		}

		ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
		FTBUltiminePlayerData data = get(player);

		if (!data.pressed)
		{
			return;
		}

		RayTraceResult result = FTBUltiminePlayerData.rayTrace(player);

		if (!(result instanceof BlockRayTraceResult) || result.getType() != RayTraceResult.Type.BLOCK)
		{
			return;
		}

		data.clearCache();
		data.updateBlocks(player, event.getPos(), ((BlockRayTraceResult) result).getFace(), false, getMaxBlocks(player));

		if (data.cachedBlocks == null || data.cachedBlocks.isEmpty())
		{
			return;
		}

		isBreakingBlock = true;
		tempBlockDropsList = new ItemCollection();
		tempBlockDroppedXp = 0;
		boolean hadItem = !player.getHeldItemMainhand().isEmpty();

		for (BlockPos p : data.cachedBlocks)
		{
			if (!player.interactionManager.tryHarvestBlock(p))
			{
				continue;
			}

			if (!player.isCreative())
			{
				player.addExhaustion(config.exhaustionPerBlock * 0.005F);

				if (player.getFoodStats().getFoodLevel() <= 0)
				{
					break;
				}
			}

			if (hadItem && player.getHeldItemMainhand().isEmpty())
			{
				break;
			}
		}

		isBreakingBlock = false;

		tempBlockDropsList.drop(player.world, event.getPos());

		if (tempBlockDroppedXp > 0)
		{
			player.world.addEntity(new ExperienceOrbEntity(player.world, event.getPos().getX() + 0.5D, event.getPos().getY() + 0.5D, event.getPos().getZ() + 0.5D, tempBlockDroppedXp));
		}

		data.clearCache();
		FTBUltimineNet.MAIN.send(PacketDistributor.PLAYER.with(() -> player), new SendShapePacket(Collections.emptyList()));
		event.setCanceled(true);
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public void blockRightClick(PlayerInteractEvent.RightClickBlock event)
	{
		if (!(event.getPlayer() instanceof ServerPlayerEntity) || event.getPlayer() instanceof FakePlayer || event.getPlayer().getUniqueID() == null)
		{
			return;
		}

		if (event.getPlayer().getFoodStats().getFoodLevel() <= 0 && !event.getPlayer().isCreative())
		{
			return;
		}

		ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
		RayTraceResult result = FTBUltiminePlayerData.rayTrace(player);

		if (!(result instanceof BlockRayTraceResult) || result.getType() != RayTraceResult.Type.BLOCK)
		{
			return;
		}

		FTBUltiminePlayerData data = get(player);
		data.clearCache();
		ShapeContext shapeContext = data.updateBlocks(player, event.getPos(), ((BlockRayTraceResult) result).getFace(), false, getMaxBlocks(player));

		if (shapeContext == null || !data.pressed || data.cachedBlocks == null || data.cachedBlocks.isEmpty())
		{
			return;
		}

		if (event.getItemStack().getItem() instanceof HoeItem)
		{
			ResourceLocation dirtTag = new ResourceLocation("forge", "dirt");

			if (!player.world.isRemote())
			{
				boolean playSound = false;
				BrokenItemHandler brokenItemHandler = new BrokenItemHandler();

				for (int i = 0; i < Math.min(data.cachedBlocks.size(), config.maxBlocks); i++)
				{
					BlockPos p = data.cachedBlocks.get(i);
					BlockState state = player.world.getBlockState(p);

					if (!state.getBlock().getTags().contains(dirtTag))
					{
						continue;
					}

					player.world.setBlockState(p, Blocks.FARMLAND.getDefaultState(), Constants.BlockFlags.DEFAULT_AND_RERENDER);
					playSound = true;

					if (!player.isCreative())
					{
						player.getHeldItemMainhand().damageItem(1, player, brokenItemHandler);
						player.addExhaustion(config.exhaustionPerBlock * 0.005F);

						if (brokenItemHandler.isBroken || player.getFoodStats().getFoodLevel() <= 0)
						{
							break;
						}
					}
				}

				if (playSound)
				{
					player.world.playSound(player, event.getPos(), SoundEvents.ITEM_HOE_TILL, SoundCategory.BLOCKS, 1F, 1F);
				}
			}

			player.swingArm(event.getHand());
			event.setCanceled(true);
		}
		else if (shapeContext.matcher == BlockMatcher.BUSH)
		{
			ItemCollection itemCollection = new ItemCollection();

			for (BlockPos pos : data.cachedBlocks)
			{
				BlockState state = player.world.getBlockState(pos);

				if (!(state.getBlock() instanceof CropsBlock))
				{
					continue;
				}

				CropsBlock c = (CropsBlock) state.getBlock();

				if (!c.isMaxAge(state))
				{
					continue;
				}

				if (player.world.isRemote())
				{
					event.setCanceled(true);
					player.swingArm(event.getHand());
					continue;
				}

				List<ItemStack> drops = Block.getDrops(state, (ServerWorld) player.world, pos, state.hasTileEntity() ? player.world.getTileEntity(pos) : null, player, ItemStack.EMPTY);
				Item seedItem = c.getItem(player.world, pos, state).getItem();

				for (ItemStack stack : drops)
				{
					if (stack.getItem() == seedItem)
					{
						stack.shrink(1);
					}

					itemCollection.add(stack);
				}

				player.world.setBlockState(pos, c.withAge(0), Constants.BlockFlags.DEFAULT);
			}

			itemCollection.drop(player.world, event.getFace() == null ? event.getPos() : event.getPos().offset(event.getFace()));
			player.swingArm(event.getHand());
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public void playerTick(TickEvent.PlayerTickEvent event)
	{
		if (event.phase == TickEvent.Phase.START && !event.player.world.isRemote())
		{
			FTBUltiminePlayerData data = get(event.player);
			data.checkBlocks((ServerPlayerEntity) event.player, true, getMaxBlocks(event.player));
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
		else if (isBreakingBlock && event.getEntity() instanceof ExperienceOrbEntity)
		{
			tempBlockDroppedXp += ((ExperienceOrbEntity) event.getEntity()).getXpValue();
			event.setCanceled(true);
		}
	}
}