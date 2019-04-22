package com.clepto.fsengine.scene.actors;

import com.clepto.fsengine.graphics.Material;
import com.clepto.fsengine.graphics.Mesh;
import com.clepto.fsengine.graphics.OBJLoader;
import com.clepto.fsengine.graphics.Texture;

public class SkyBox extends Actor {

	public SkyBox(String objModel, String textureFile) throws Exception {
		super();
		Mesh skyboxMesh = OBJLoader.loadMesh(objModel);
		Texture skyboxTexture = new Texture(textureFile);
		skyboxMesh.setMaterial(new Material(skyboxTexture, 0.0f));
		setMesh(skyboxMesh);
		setPosition(0, 0, 0);
	}
	
}
