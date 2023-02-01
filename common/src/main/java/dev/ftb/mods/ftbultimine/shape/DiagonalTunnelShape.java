package dev.ftb.mods.ftbultimine.shape;

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
            BlockPos pTop = context.pos().offset(-face.getStepX() * i, yDirection() * i, -face.getStepZ() * i);

            if (!context.check(pTop)) {
                break;
            }

            list.add(pTop);
        }

        return list;
    }

    protected abstract int yDirection();
}
