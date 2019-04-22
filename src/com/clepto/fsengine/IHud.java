package com.clepto.fsengine;

import com.clepto.fsengine.scene.actors.Actor;

public interface IHud {

	Actor[] getActors();
	
	default void cleanup() {
		Actor[] actors = getActors();
		for (Actor actor : actors) {
			actor.getMesh().cleanUp();
		}
	}
	
}
