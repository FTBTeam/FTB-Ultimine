package dev.ftb.mods.ftbultimine.rightclick;

import com.google.common.collect.BiMap;
import dev.ftb.mods.ftbultimine.api.rightclick.RightClickHandler;
import dev.ftb.mods.ftbultimine.config.FTBUltimineServerConfig;
import dev.ftb.mods.ftbultimine.mixin.AxeItemAccess;
import dev.ftb.mods.ftbultimine.api.shape.ShapeContext;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public enum AxeStripping implements RightClickHandler {
    INSTANCE;

    @Override
    public int handleRightClickBlock(ShapeContext shapeContext, InteractionHand hand, Collection<BlockPos> positions) {
        ServerPlayer player = shapeContext.player();

        if (!FTBUltimineServerConfig.RIGHT_CLICK_SHOVEL.get() || !(player.getItemInHand(hand).getItem() instanceof AxeItem)) {
            return 0;
        }

        Set<SoundEvent> sounds = new HashSet<>();
        Level level = player.level();

        ItemStack itemStack = player.getItemInHand(hand);
        AxeItemAccess axeItemAccess = (AxeItemAccess) itemStack.getItem();

        for (BlockPos pos : positions) {
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
        sounds.forEach(sound -> level.playSound(null, shapeContext.pos(), sound, SoundSource.BLOCKS, 1f, 1f));
        return sounds.size();
    }
}
