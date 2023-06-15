package dev.ftb.mods.ftbultimine.utils;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/*
 * This class is based on Chisels and Bits' AABBCompressor, the original source for which can be found here:
 * https://github.com/ChiselsAndBits/Chisels-and-Bits/blob/version/1.16.3/src/main/java/mod/chiselsandbits/aabb/AABBCompressor.java
 *
 * Original author: Marc Hermans <marc.hermans@ldtteam.com>
 * GitHub: https://github.com/OrionDevelopment
 *
 * Explicit permission was given to FTB by the original author of AABBCompressor to include this file in this project.
 */
public final class ShapeMerger {
	private static final Long2ObjectMap<Direction> BY_NORMAL = Arrays.stream(Direction.values())
			.collect(Collectors.toMap(
					dir -> new BlockPos(dir.getNormal()).asLong(),
					dir -> dir,
					(dir, dir2) -> {
						throw new IllegalArgumentException("Duplicate keys");
					}, Long2ObjectOpenHashMap::new)
			);

	private double regionBuildingAxis = Double.NEGATIVE_INFINITY;
	private double faceBuildingAxis = Double.NEGATIVE_INFINITY;

	private Vec3 lastCenterPoint = null;
	private AABB currentBox;

	private final Map<Vec3, AABB> boxAssignments = Maps.newHashMap();
	private final Multimap<AABB, Vec3> mergerAssignments = HashMultimap.create();

	public double getRegionBuildingAxisValue() {
		return regionBuildingAxis;
	}

	public void setRegionBuildingAxisValue(final double regionBuildingAxis) {
		this.regionBuildingAxis = regionBuildingAxis;
	}

	public double getFaceBuildingAxisValue() {
		return faceBuildingAxis;
	}

	public void setFaceBuildingAxisValue(final double faceBuildingAxis) {
		this.faceBuildingAxis = faceBuildingAxis;
	}

	public AABB getCurrentBox() {
		return currentBox;
	}

	public void setCurrentBox(final AABB currentBox, final Vec3 centerPoint) {
		this.currentBox = currentBox;
		if (currentBox != null) {
			boxAssignments.put(centerPoint, currentBox);
			mergerAssignments.put(currentBox, centerPoint);
		}
	}

	public Optional<AABB> getBoxFor(final Vec3 target) {
		return Optional.ofNullable(boxAssignments.get(target));
	}

	public Optional<Vec3> getLastCenter() {
		return Optional.ofNullable(lastCenterPoint);
	}

	public void onNextEntry(final Vec3 lastCenterPoint) {
		this.lastCenterPoint = lastCenterPoint;
	}

	public void expandCurrentBoxTowards(final AABB target, final Vec3 center) {
		final AABB current = this.getCurrentBox();
		if (current == null) {
			throw new IllegalStateException("Can not expand current box, if current is not set.");
		}

		final AABB expanded = current.minmax(target);

		final Collection<Vec3> currentlyAssignedToCurrent = mergerAssignments.removeAll(current);

		currentlyAssignedToCurrent.forEach(v -> boxAssignments.put(v, expanded));
		mergerAssignments.putAll(expanded, currentlyAssignedToCurrent);

		boxAssignments.put(center, expanded);
		mergerAssignments.put(expanded, center);

		this.currentBox = expanded;
	}

	public Collection<AABB> getBoxes() {
		return mergerAssignments.keySet();
	}

	public void expandBoxAt(final Vec3 neighborCenter, final AABB entryData, final Vec3 centerPoint) {
		final AABB current = boxAssignments.get(neighborCenter);
		if (current == null) {
			throw new IllegalStateException(String.format("Can not expand box at: %s, if current is not set.", neighborCenter));
		}

		final AABB expanded = current.minmax(entryData);

		final Collection<Vec3> currentlyAssignedToCurrent = mergerAssignments.removeAll(current);

		currentlyAssignedToCurrent.forEach(v -> boxAssignments.put(v, expanded));
		mergerAssignments.putAll(expanded, currentlyAssignedToCurrent);

		boxAssignments.put(centerPoint, expanded);
		mergerAssignments.put(expanded, centerPoint);
	}

	public boolean mergeNeighbors(final Vec3 centerPoint, final AABB aabb) {
		for (final Direction offsetDirection : Direction.values()) {
			final Vec3 neighborCenter = centerPoint.add(Vec3.atLowerCornerOf(offsetDirection.getNormal()));
			final Optional<AABB> potentialNeighborBox = getBoxFor(neighborCenter);

			if (potentialNeighborBox.isPresent()) {
				final AABB neighborBox = potentialNeighborBox.get();
				if (areBoxesNeighbors(aabb, neighborBox, offsetDirection)) {
					expandBoxAt(neighborCenter, aabb, centerPoint);
					return true;
				}
			}
		}
		return false;
	}

	public static boolean areBoxesNeighbors(final AABB l, final AABB r, final Direction direction) {
		final double endOfL = getDirectionalValue(l, direction);
		final double startOfR = getDirectionalValue(r, direction.getOpposite());

		if (endOfL != startOfR) {
			return false;
		}

		for (Direction d : Direction.values()) {
			if (d.getAxis() != direction.getAxis() && getDirectionalValue(l, d) != getDirectionalValue(r, d)) {
				return false;
			}
		}
		return true;
	}

	public static double getDirectionalValue(final AABB bb, final Direction direction) {
		return direction.getAxisDirection() == Direction.AxisDirection.POSITIVE ?
				bb.max(direction.getAxis()) :
				bb.min(direction.getAxis());
	}

	public static Collection<AABB> merge(Collection<BlockPos> positions, BlockPos origin) {
		final ShapeMerger merger = new ShapeMerger();
		positions.stream()
				.map(pos -> pos.subtract(origin))
				.sorted()
				.map(AABB::new)
				.forEachOrdered(aabb -> {
					if (merger.getRegionBuildingAxisValue() != aabb.minX) {
						merger.setCurrentBox(null, null);
					}
					merger.setRegionBuildingAxisValue(aabb.minX);

					if (merger.getFaceBuildingAxisValue() != aabb.minY) {
						merger.setCurrentBox(null, null);
					}
					merger.setFaceBuildingAxisValue(aabb.minY);

					final Optional<Vec3> previousCenterPoint = merger.getLastCenter();
					final Vec3 centerPoint = aabb.getCenter();
					merger.onNextEntry(centerPoint);

					Optional<Direction> moveNext = previousCenterPoint.map(
							v -> {
								Vec3 w = centerPoint.subtract(v);
								BlockPos onPos = BlockPos.containing(w);
								return dirFromNormal(onPos.getX(), onPos.getY(), onPos.getZ());
							}
					);

					if (merger.getCurrentBox() != null) {
						if (moveNext.map(dir -> ShapeMerger.areBoxesNeighbors(merger.getCurrentBox(), aabb, dir)).orElse(false)) {
							merger.expandCurrentBoxTowards(aabb, centerPoint);

							if (merger.mergeNeighbors(centerPoint, merger.getCurrentBox())) {
								return;
							}

							return;
						}
					}

					if (merger.mergeNeighbors(centerPoint, aabb)) {
						return;
					}

					merger.setCurrentBox(aabb, centerPoint);

				});
		return merger.getBoxes();
	}

	private static Direction dirFromNormal(int pX, int pY, int pZ) {
		return BY_NORMAL.get(BlockPos.asLong(pX, pY, pZ));
	}
}