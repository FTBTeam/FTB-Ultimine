package dev.ftb.mods.ftbultimine;

import com.google.common.collect.BiMap;
import dev.ftb.mods.ftbultimine.config.FTBUltimineServerConfig;
import dev.ftb.mods.ftbultimine.crops.CropLikeRegistry;
import dev.ftb.mods.ftbultimine.mixin.AxeItemAccess;
import dev.ftb.mods.ftbultimine.mixin.ShovelItemAccess;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class RightClickHandlers {
    static int axeStripping(ServerPlayer player, InteractionHand hand, BlockPos clickPos, FTBUltiminePlayerData data) {
        Set<SoundEvent> sounds = new HashSet<>();
        BrokenItemHandler brokenItemHandler = new BrokenItemHandler();
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
                itemStack.hurtAndBreak(1, player, brokenItemHandler);

                if (brokenItemHandler.isBroken) {
                    break;
                }
            }
        }
        sounds.forEach(sound -> level.playSound(null, clickPos, sound, SoundSource.BLOCKS, 1f, 1f));
        return sounds.size();
    }

    static int shovelFlattening(ServerPlayer player, InteractionHand hand, BlockPos clickPos, FTBUltiminePlayerData data) {
        int didWork = 0;
        BrokenItemHandler brokenItemHandler = new BrokenItemHandler();

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

                player.getMainHandItem().hurtAndBreak(1, player, brokenItemHandler);
                player.level().gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, newState));
                if (brokenItemHandler.isBroken || player.getFoodData().getFoodLevel() <= 0) {
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

    static int farmlandConversion(ServerPlayer player, InteractionHand hand, BlockPos clickPos, FTBUltiminePlayerData data) {
        int clicked = 0;
        BrokenItemHandler brokenItemHandler = new BrokenItemHandler();

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
                    player.getMainHandItem().hurtAndBreak(1, player, brokenItemHandler);
                    if (brokenItemHandler.isBroken || FTBUltimine.isTooExhausted(player)) {
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
        MutableInt clicked = new MutableInt();
        ItemCollection itemCollection = new ItemCollection();

        for (BlockPos pos : data.cachedPositions()) {
            BlockState state = player.level().getBlockState(pos);
            CropLikeRegistry.INSTANCE.getHandlerFor(player.level(), pos, state).ifPresent(handler -> {
                handler.doHarvesting(player, pos, state, itemCollection);
                clicked.increment();
            });
        }

        itemCollection.drop(player.level(), face == null ? clickPos : clickPos.relative(face));

        return clicked.intValue();
    }
}
