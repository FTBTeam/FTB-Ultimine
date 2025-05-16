package dev.ftb.mods.ftbultimine;

import dev.ftb.mods.ftbultimine.api.restriction.RegisterRestrictionHandlerEvent;
import dev.ftb.mods.ftbultimine.api.restriction.RestrictionHandler;
import net.minecraft.world.entity.player.Player;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

public enum RestrictionHandlerRegistry implements RegisterRestrictionHandlerEvent.Registry {
	INSTANCE;

	private final Collection<RestrictionHandler> handlers = new CopyOnWriteArrayList<>();

	@Override
	public void register(RestrictionHandler handler) {
		handlers.add(handler);
	}

	public boolean canUltimine(Player player) {
        return handlers.stream().allMatch(h -> h.canUltimine(player));
	}
}
