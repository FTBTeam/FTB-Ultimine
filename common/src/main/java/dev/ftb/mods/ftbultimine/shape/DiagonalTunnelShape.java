package dev.ftb.mods.ftbultimine.shape;

import dev.ftb.mods.ftbultimine.api.shape.Shape;
import dev.ftb.mods.ftbultimine.api.shape.ShapeContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.ArrayList;
import java.util.List;

public abstract class DiagonalTunnelShape implements Shape {
    @Override
    public List<BlockPos> getBlocks(ShapeContext context) {
        Direction face = context.face().getAxis().isVertical() ? context.player().getDirection().getOpposite() : context.face();

        List<BlockPos> list = new ArrayList<>(context.maxBlocks());

        for (int i = 0; i < context.maxBlocks(); i++) {
            BlockPos pTop = context.origPos().offset(-face.getStepX() * i, yDirection() * i, -face.getStepZ() * i);

            if (!context.check(pTop)) {
                break;
            }

            list.add(pTop);
        }

        return list;
    }

    protected abstract int yDirection();
}
