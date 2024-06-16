package dev.ftb.mods.ftbultimine;

import com.google.common.collect.BiMap;
import dev.ftb.mods.ftbultimine.config.FTBUltimineServerConfig;
import dev.ftb.mods.ftbultimine.mixin.AxeItemAccess;
import dev.ftb.mods.ftbultimine.mixin.ShovelItemAccess;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class RightClickHandlers {
    static int axeStripping(ServerPlayer player, InteractionHand hand, BlockPos clickPos, FTBUltiminePlayerData data) {
        Set<SoundEvent> sounds = new HashSet<>();
        Level level = player.level();

        ItemStack itemStack = player.getItemInHand(hand);
        AxeItemAccess axeItemAccess = (AxeItemAccess) itemStack.getItem();

        for (BlockPos pos : data.cachedPositions()) {
            BlockState state = player.level().getBlockState(pos);

            Optional<BlockState> stripping = axeItemAccess.invokeGetStripped(state);
            Optional<BlockState> scraping = WeatheringCopper.getPrevious(state);
            Optional<BlockState> waxing = Optional.ofNullable((Block) ((BiMap<?, ?>) HoneycombItem.WAX_OFF_BY_BLOCK.get()).get(state.getBlock())).map((block) -> block.withPropertiesOf(state));
            Optional<BlockState> actual = Optional.empty();

            if (stripping.isPresent()) {
                sounds.add(SoundEvents.AXE_STRIP);
                actual = stripping;
            } else if (scraping.isPresent()) {
                sounds.add(SoundEvents.AXE_SCRAPE);
                level.levelEvent(player, 3005, pos, 0);
                actual = scraping;
            } else if (waxing.isPresent()) {
                sounds.add(SoundEvents.AXE_WAX_OFF);
                level.levelEvent(player, 3004, pos, 0);
                actual = waxing;
            }
            if (actual.isPresent()) {
                CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(player, pos, itemStack);
                level.setBlock(pos, actual.get(), Block.UPDATE_ALL_IMMEDIATE);
                level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, actual.get()));

                var result = hurtItemAndCheckIfBroken(player, hand);
                if (result) {
                    break;
                }
            }
        }
        sounds.forEach(sound -> level.playSound(null, clickPos, sound, SoundSource.BLOCKS, 1f, 1f));
        return sounds.size();
    }

    static int shovelFlattening(ServerPlayer player, InteractionHand hand, BlockPos clickPos, FTBUltiminePlayerData data) {
        int didWork = 0;

        for (BlockPos pos : data.cachedPositions()) {
            if (!player.level().getBlockState(pos.above()).isAir()) {
                continue;
            }
            BlockState state = player.level().getBlockState(pos);

            BlockState newState = ShovelItemAccess.getFlattenables().get(state.getBlock());
            if (newState == null && state.is(FTBUltimine.FLATTENABLE_TAG)) {
                newState = Blocks.DIRT_PATH.defaultBlockState();
            }
            if (newState != null) {
                player.level().setBlock(pos, newState, Block.UPDATE_ALL_IMMEDIATE);
                didWork++;

                var result = hurtItemAndCheckIfBroken(player, hand);
                player.level().gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, newState));

                if (result || player.getFoodData().getFoodLevel() <= 0) {
                    break;
                }
            }
        }

        //noinspection ConstantConditions
        if (didWork > 0) {
            // suppress warning: didWork only looks false due to mixin
            player.level().playSound(player, clickPos, SoundEvents.SHOVEL_FLATTEN, SoundSource.BLOCKS, 1F, 1F);
        }

        return didWork;
    }

    static boolean hurtItemAndCheckIfBroken(ServerPlayer player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        itemStack.hurtAndBreak(1, player, hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
        return itemStack.isEmpty();
    }

    static int farmlandConversion(ServerPlayer player, InteractionHand hand, BlockPos clickPos, FTBUltiminePlayerData data) {
        int clicked = 0;

        for (BlockPos pos : data.cachedPositions()) {
            if (!player.level().getBlockState(pos.above()).isAir()) {
                continue;
            }
            BlockState state = player.level().getBlockState(pos);
            if (state.is(FTBUltimine.TILLABLE_TAG)) {
                player.level().setBlock(pos, Blocks.FARMLAND.defaultBlockState(), Block.UPDATE_ALL_IMMEDIATE);
                player.level().gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, Blocks.FARMLAND.defaultBlockState()));
                clicked++;

                if (!player.isCreative()) {
                    player.causeFoodExhaustion((float) (FTBUltimineServerConfig.EXHAUSTION_PER_BLOCK.get() * 0.005D));
                    var result = hurtItemAndCheckIfBroken(player, hand);

                    if (result || FTBUltimine.isTooExhausted(player)) {
                        break;
                    }
                }
            }
        }

        if (clicked > 0) {
            player.level().playSound(player, clickPos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1F, 1F);
        }

        return clicked;
    }

    static int cropHarvesting(ServerPlayer player, InteractionHand hand, BlockPos clickPos, Direction face, FTBUltiminePlayerData data) {
        int clicked = 0;

        ItemCollection itemCollection = new ItemCollection();

        for (BlockPos pos : data.cachedPositions()) {
            BlockState state = player.level().getBlockState(pos);

            if (isHarvestable(state)) {
                BlockEntity blockEntity = state.hasBlockEntity() ? player.level().getBlockEntity(pos) : null;
                List<ItemStack> drops = Block.getDrops(state, (ServerLevel) player.level(), pos, blockEntity, player, ItemStack.EMPTY);

                for (ItemStack stack : drops) {
                    // should work for most if not all modded crop blocks, hopefully
                    if (Block.byItem(stack.getItem()) == state.getBlock() && consumesItemToReplant(state)) {
                        stack.shrink(1);
                    }
                    itemCollection.add(stack);
                }

                resetAge(player.level(), pos, state);
                clicked++;
            }
        }

        itemCollection.drop(player.level(), face == null ? clickPos : clickPos.relative(face));

        return clicked;
    }

    private static boolean consumesItemToReplant(BlockState state) {
        return state.getBlock() != Blocks.SWEET_BERRY_BUSH;
    }

    private static boolean isHarvestable(BlockState state) {
        return state.getBlock() instanceof CropBlock cropBlock && cropBlock.isMaxAge(state)
                || state.getBlock() instanceof SweetBerryBushBlock && state.getValue(SweetBerryBushBlock.AGE) >= SweetBerryBushBlock.MAX_AGE
                || state.getBlock() instanceof CocoaBlock && state.getValue(CocoaBlock.AGE) >= CocoaBlock.MAX_AGE;
    }

    private static void resetAge(Level level, BlockPos pos, BlockState currentState) {
        if (currentState.getBlock() instanceof CropBlock cropBlock) {
            level.setBlock(pos, cropBlock.getStateForAge(0), Block.UPDATE_ALL);
        } else if (currentState.getBlock() instanceof SweetBerryBushBlock) {
            level.setBlock(pos, currentState.setValue(SweetBerryBushBlock.AGE, 1), Block.UPDATE_ALL);
        } else if (currentState.getBlock() instanceof CocoaBlock) {
            level.setBlock(pos, currentState.setValue(CocoaBlock.AGE, 0), Block.UPDATE_ALL);
        }
    }
}
