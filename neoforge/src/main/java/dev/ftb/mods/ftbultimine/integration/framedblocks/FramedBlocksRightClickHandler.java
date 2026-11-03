package dev.ftb.mods.ftbultimine.integration.framedblocks;

import dev.ftb.mods.ftbultimine.api.rightclick.RightClickHandler;
import dev.ftb.mods.ftbultimine.api.shape.ShapeContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Collection;

public enum FramedBlocksRightClickHandler implements RightClickHandler {
    INSTANCE;

    public static final String FRAMED_BLOCKS_MOD = "framedblocks";

    @Override
    public int handleRightClickBlock(ShapeContext shapeContext, InteractionHand hand, Collection<BlockPos> positions) {
        ServerPlayer player = shapeContext.player();
        ServerLevel level = player.level();
        double range = player.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE);

        if (player.pick(player.isCreative() ? range + 0.5 : range, 1.0F, false) instanceof BlockHitResult bhr) {
            Vec3 relativeHitLocation = bhr.getLocation().subtract(bhr.getBlockPos().getCenter());
            Direction hitFace = bhr.getDirection();
            BlockState originalState = level.getBlockState(bhr.getBlockPos());
            Direction originalFace = getBlockstateDirection(originalState);

            int affected = 0;
            for (BlockPos pos : positions) {
                BlockState state = level.getBlockState(pos);
                if (BuiltInRegistries.BLOCK.getKey(state.getBlock()).getNamespace().equals(FRAMED_BLOCKS_MOD)) {
                    Direction facing = getBlockstateDirection(state);
                    Vec3 rotatedHitLocation = new Vec3(relativeHitLocation.toVector3f().rotate(rotation(originalFace, facing)));
                    Direction rotatedFace = rotateDirection(hitFace, originalFace, facing);
                    BlockHitResult rotatedHit = new BlockHitResult(pos.getCenter().add(rotatedHitLocation), rotatedFace, pos, false);
                    InteractionResult interaction = state.useItemOn(player.getItemInHand(hand), level, player, hand, rotatedHit);
                    if (interaction.consumesAction()) {
                        affected++;
                    }
                }
            }
            return affected;
        }
        return 0;
    }

    private static Quaternionf rotation(Direction from, Direction to) {
        return to.getRotation().mul(from.getRotation().invert(new Quaternionf()));
    }

    private static Direction rotateDirection(Direction dir, Direction from, Direction to) {
        Vector3f normal = dir.step().rotate(rotation(from, to));
        return Direction.getNearest(Math.round(normal.x()), Math.round(normal.y()), Math.round(normal.z()), null);
    }

    private static Direction getBlockstateDirection(BlockState state) {
        if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            return state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        } else if (state.hasProperty(BlockStateProperties.FACING)) {
            return state.getValue(BlockStateProperties.FACING);
        } else {
            return Direction.NORTH;
        }
    }
}
