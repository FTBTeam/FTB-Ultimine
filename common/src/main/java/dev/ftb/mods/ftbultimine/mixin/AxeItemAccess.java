package dev.ftb.mods.ftbultimine.mixin;

import net.minecraft.world.item.AxeItem;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Optional;

@Mixin(AxeItem.class)
public interface AxeItemAccess {
    @Invoker("getStripped")
    Optional<BlockState> invokeGetStripped(BlockState state);
}
