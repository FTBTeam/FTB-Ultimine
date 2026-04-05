package dev.ftb.mods.ftbultimine.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import dev.ftb.mods.ftbultimine.FTBUltimine;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;

public class ModAttributes {
    public static final DeferredRegister<Attribute> ATTRIBUTES
            = DeferredRegister.create(FTBUltimine.MOD_ID, Registries.ATTRIBUTE);

    public static final RegistrySupplier<Attribute> MAX_BLOCKS_MODIFIER
            = ATTRIBUTES.register("max_blocks_modifier", () -> new RangedAttribute("ftbultimine.modifier.max_blocks", 0.0, -Double.MAX_VALUE, Double.MAX_VALUE));
    public static final RegistrySupplier<Attribute> COOLDOWN_MODIFIER
            = ATTRIBUTES.register("cooldown_modifier", () -> new RangedAttribute("ftbultimine.modifier.cooldown", 0.0, -Double.MAX_VALUE, Double.MAX_VALUE));
    public static final RegistrySupplier<Attribute> EXHAUSTION_MODIFIER
            = ATTRIBUTES.register("exhaustion_modifier", () -> new RangedAttribute("ftbultimine.modifier.cooldown", 0.0, -Double.MAX_VALUE, Double.MAX_VALUE));
    public static final RegistrySupplier<Attribute> EXPERIENCE_MODIFIER
            = ATTRIBUTES.register("experience_modifier", () -> new RangedAttribute("ftbultimine.modifier.cooldown", 0.0, -Double.MAX_VALUE, Double.MAX_VALUE));

    public static void init() {
        ATTRIBUTES.register();
    }
}
