package dev.ftb.mods.ftbultimine.forge.plugin.losttrinkets;

//import net.minecraft.ChatFormatting;
//import net.minecraft.network.chat.Component;
//import net.minecraft.network.chat.TextColor;
//import net.minecraft.network.chat.TextComponent;
//import net.minecraft.network.chat.TranslatableComponent;
//import net.minecraft.world.item.Item;
//import net.minecraft.world.item.ItemStack;
//import org.jetbrains.annotations.NotNull;
//import owmii.losttrinkets.api.trinket.Rarity;
//import owmii.losttrinkets.api.trinket.Trinket;
//import owmii.losttrinkets.item.ItemGroups;
//
//import java.util.List;
//
//public class UltiminerTrinket extends Trinket<UltiminerTrinket> {
//
//	public UltiminerTrinket() {
//		super(Rarity.EPIC, new Item.Properties().tab(ItemGroups.MAIN));
//	}
//
//	@Override
//	public void addTrinketDescription(@NotNull ItemStack stack, List<Component> lines) {
//		lines.add((new TranslatableComponent("item.ftbultimine.ultiminer.tooltip",
//				new TextComponent("FTB Ultimine").withStyle(s -> s.withColor(TextColor.fromRgb(0xfcb95b)))))
//				.withStyle(ChatFormatting.GRAY));
//	}
//}
