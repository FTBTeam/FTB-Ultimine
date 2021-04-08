package com.feed_the_beast.mods.ftbultimine.event;

import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.architectury.event.Event;
import me.shedaniel.architectury.event.EventFactory;

/**
 * This is a temp interface until World Render Events are implemented in Architectury.
 *
 * @see <a href=https://github.com/architectury/architectury-api/issues/6}>PR #6</a>
 * DO NOT use this outside of Ultimine (why would you?)
 */
public interface LevelRenderLastEvent {
	Event<LevelRenderLastEvent> EVENT = EventFactory.createLoop();

	// the pose stack is all we need so fuck it
	void onRenderLast(PoseStack stack);
}
