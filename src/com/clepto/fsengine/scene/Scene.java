package com.clepto.fsengine.scene;

import com.clepto.fsengine.scene.actors.Actor;
import com.clepto.fsengine.scene.actors.SkyBox;

public class Scene {

	private Actor[] actors;
	
	private SkyBox skyBox;
	
	public SceneLight sceneLight;

	public Actor[] getActors() {
		return actors;
	}

	public void setActors(Actor[] actors) {
		this.actors = actors;
	}

	public SkyBox getSkyBox() {
		return skyBox;
	}

	public void setSkyBox(SkyBox skyBox) {
		this.skyBox = skyBox;
	}

	public SceneLight getSceneLight() {
		return sceneLight;
	}

	public void setSceneLight(SceneLight sceneLight) {
		this.sceneLight = sceneLight;
	}
	
}
