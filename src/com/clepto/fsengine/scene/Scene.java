package com.clepto.fsengine.scene;

import com.clepto.fsengine.scene.actors.Actor;

public class Scene {

	private Actor[] actors;
	
	public SceneLight sceneLight;

	public Actor[] getActors() {
		return actors;
	}

	public void setActors(Actor[] actors) {
		this.actors = actors;
	}

	public SceneLight getSceneLight() {
		return sceneLight;
	}

	public void setSceneLight(SceneLight sceneLight) {
		this.sceneLight = sceneLight;
	}
	
}
