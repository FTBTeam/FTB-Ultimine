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
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.HoeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShearsItem;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
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

	public FTBUltimine()
	{
		instance = this;
		FTBUltimineNet.init();
		MinecraftForge.EVENT_BUS.addListener(EventPriority.LOW, this::blockBroken);
		MinecraftForge.EVENT_BUS.addListener(EventPriority.LOW, this::blockRightClick);
		MinecraftForge.EVENT_BUS.addListener(this::serverAboutToStart);
		MinecraftForge.EVENT_BUS.addListener(this::playerTick);
		MinecraftForge.EVENT_BUS.addListener(this::playerLoaded);
		MinecraftForge.EVENT_BUS.addListener(this::playerSaved);

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

	private void serverAboutToStart(FMLServerAboutToStartEvent event)
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

	private void blockBroken(BlockEvent.BreakEvent event)
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
		FTBUltiminePlayerData data = get(player);

		if (!data.pressed)
		{
			return;
		}

		RayTraceResult result = player.pick(4.5D, 1F, false);

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

		//data.cachedBlocks.remove(event.getPos());
		mineBlocks(player, event.getPos(), data.cachedBlocks);
		data.clearCache();
		FTBUltimineNet.MAIN.send(PacketDistributor.PLAYER.with(() -> player), new SendShapePacket(Collections.emptyList()));
		event.setCanceled(true);
	}

	private int canHarvestBlock(ServerPlayerEntity player, BlockState state, BlockPos pos)
	{
		if (player.isCreative())
		{
			return 1;
		}
		else if (ForgeHooks.canHarvestBlock(state, player, player.world, pos))
		{
			return 2;
		}
		else if (state.getBlock() == Blocks.SNOW)
		{
			return 1;
		}

		return 0;
	}

	private void mineBlocks(ServerPlayerEntity player, BlockPos pos, List<BlockPos> list)
	{
		ServerWorld world = player.getServerWorld();

		if (canHarvestBlock(player, world.getBlockState(pos), pos) == 0)
		{
			return;
		}

		int droppedXp = 0;
		int silktouch = EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, player.getHeldItemMainhand());
		int fortune = EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, player.getHeldItemMainhand());

		BrokenItemHandler brokenItemHandler = new BrokenItemHandler();
		ItemCollection itemCollection = new ItemCollection();

		for (int i = 0; i < Math.min(list.size(), config.maxBlocks); i++)
		{
			BlockPos p = list.get(i);
			BlockState state = world.getBlockState(p);
			int harvest = canHarvestBlock(player, state, p);

			if (harvest == 0)
			{
				continue;
			}
			else if (!player.isCreative() && (player.getHeldItemMainhand().getItem() instanceof ShearsItem || state.getBlockHardness(world, pos) != 0.0F))
			{
				player.getHeldItemMainhand().damageItem(1, player, brokenItemHandler);
			}

			IFluidState fluidState = world.getFluidState(p);

			if (harvest == 2)
			{
				for (ItemStack stack : Block.getDrops(state, world, p, state.hasTileEntity() ? world.getTileEntity(p) : null, player, player.getHeldItemMainhand()))
				{
					itemCollection.add(stack);
				}

				state.spawnAdditionalDrops(world, p, ItemStack.EMPTY);
				droppedXp += state.getExpDrop(world, p, fortune, silktouch);
			}

			world.setBlockState(p, fluidState.getBlockState(), Constants.BlockFlags.DEFAULT);

			if (brokenItemHandler.isBroken)
			{
				break;
			}

			if (!player.isCreative())
			{
				player.addExhaustion(config.exhaustionPerBlock * 0.005F);

				if (player.getFoodStats().getFoodLevel() <= 0)
				{
					break;
				}
			}
		}

		itemCollection.drop(world, pos);

		if (droppedXp > 0)
		{
			world.addEntity(new ExperienceOrbEntity(world, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, Integer.MAX_VALUE));
		}
	}

	private void blockRightClick(PlayerInteractEvent.RightClickBlock event)
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
		RayTraceResult result = player.pick(4.5D, 1F, false);

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

	private void playerTick(TickEvent.PlayerTickEvent event)
	{
		if (event.phase == TickEvent.Phase.START && !event.player.world.isRemote())
		{
			FTBUltiminePlayerData data = get(event.player);
			data.checkBlocks((ServerPlayerEntity) event.player, true, getMaxBlocks(event.player));
		}
	}

	private void playerLoaded(PlayerEvent.LoadFromFile event)
	{
	}

	private void playerSaved(PlayerEvent.SaveToFile event)
	{
	}
}