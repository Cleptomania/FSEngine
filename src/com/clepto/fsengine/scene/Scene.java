package com.clepto.fsengine.scene;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.clepto.fsengine.graphics.Mesh;
import com.clepto.fsengine.graphics.weather.Fog;
import com.clepto.fsengine.scene.actors.Actor;
import com.clepto.fsengine.scene.actors.SkyBox;

public class Scene {

	private Map<Mesh, List<Actor>> meshMap;
	
	private SkyBox skybox;
	
	public SceneLight sceneLight;
	
	private Fog fog;

	public Scene() {
		meshMap = new HashMap<Mesh, List<Actor>>();
		fog = Fog.NOFOG;
	}
	
	public Map<Mesh, List<Actor>> getMeshes() {
		return meshMap;
	}
	
	public void setActors(Actor[] actors) {
		int numActors = actors != null ? actors.length : 0;
		for (int i=0; i < numActors; i++) {
			Actor actor = actors[i];
			Mesh mesh = actor.getMesh();
			List<Actor> list = meshMap.get(mesh);
			if (list == null) {
				list = new ArrayList<>();
				meshMap.put(mesh, list);
			}
			list.add(actor);
		}
	}

	public void setSkybox(SkyBox skybox) {
		this.skybox = skybox;
	}
	
	public SkyBox getSkybox() {
		return skybox;
	}
	
	public void setSceneLight(SceneLight sceneLight) {
		this.sceneLight = sceneLight;
	}
	
	public SceneLight getSceneLight() {
		return sceneLight;
	}
	
	public Fog getFog() {
		return fog;
	}
	
	public void setFog(Fog fog) {
		this.fog = fog;
	}
}
