package dev.ftb.mods.ftbultimine.shape;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.SaplingBlock;

import java.util.HashMap;
import java.util.Map;

public class CropLikeTypeMatcher {
    private static final Map<ResourceLocation, Type> TYPES = new HashMap<>();

    public static Type getCroplikeType(Block block) {
        ResourceLocation key = BuiltInRegistries.BLOCK.getKey(block);
        return TYPES.getOrDefault(key, getByClass(block));
    }

    private static Type getByClass(Block block) {
        if (block instanceof CropBlock) {
            return Type.CROP;
        } else if (block instanceof SaplingBlock) {
            return Type.SAPLING;
        } else if (block instanceof CocoaBlock) {
            return Type.COCOA;
        }
        return Type.NOT_CROPLIKE;
    }

    public static void registerType(ResourceLocation key, Type type) {
        TYPES.put(key, type);
    }

    public enum Type {
        NOT_CROPLIKE,
        CROP,
        SAPLING,
        COCOA
    }
}
