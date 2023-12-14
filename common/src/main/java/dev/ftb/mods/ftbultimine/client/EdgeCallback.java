package dev.ftb.mods.ftbultimine.client;

import com.mojang.blaze3d.vertex.BufferBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.Shapes;
import org.joml.Matrix4f;

public class EdgeCallback implements Shapes.DoubleLineConsumer {
	private final BufferBuilder buffer;
	private final Matrix4f matrix;
	private final BlockPos pos;
	private final int alpha;

	public EdgeCallback(BufferBuilder b, Matrix4f m, BlockPos p, int a) {
		buffer = b;
		matrix = m;
		pos = p;
		alpha = a;
	}

	@Override
	public void consume(double x1, double y1, double z1, double x2, double y2, double z2) {
		buffer.vertex(matrix, (float) (x1 + pos.getX()), (float) (y1 + pos.getY()), (float) (z1 + pos.getZ())).color(255, 255, 255, alpha).endVertex();
		buffer.vertex(matrix, (float) (x2 + pos.getX()), (float) (y2 + pos.getY()), (float) (z2 + pos.getZ())).color(255, 255, 255, alpha).endVertex();
	}
}