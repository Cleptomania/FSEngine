package com.clepto.fsengine.scene.actors;

import com.clepto.fsengine.graphics.Material;
import com.clepto.fsengine.graphics.Mesh;
import com.clepto.fsengine.graphics.OBJLoader;
import com.clepto.fsengine.graphics.Texture;

public class SkyBox extends Actor {

	public SkyBox(String modelFile, String textureFile) throws Exception {
		super();
		Mesh mesh = OBJLoader.loadMesh(modelFile);
		Texture texture = new Texture(textureFile);
		mesh.setMaterial(new Material(texture, 0.0f));
		setMesh(mesh);
	}
	
}
