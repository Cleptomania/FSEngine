package com.clepto.fsengine.scene.actors;

import java.util.ArrayList;
import java.util.List;

import com.clepto.fsengine.graphics.FontTexture;
import com.clepto.fsengine.graphics.Material;
import com.clepto.fsengine.graphics.Mesh;
import com.clepto.fsengine.util.Utils;

public class TextActor extends Actor {

	private static final float ZPOS = 0.0f;
	
	private static final int VERTICES_PER_QUAD = 4;
	
	private final FontTexture fontTexture;
	
	private String text;
	
	public TextActor(String text, FontTexture fontTexture) throws Exception {
		super();
		this.text = text;
		this.fontTexture = fontTexture;
		setMesh(buildMesh());
	}
	
	private Mesh buildMesh() {
		List<Float> positions = new ArrayList<Float>();
		List<Float> textCoords = new ArrayList<Float>();
		float[] normals = new float[0];
		List<Integer> indices = new ArrayList<Integer>();
		char[] characters = text.toCharArray();
		int numChars = characters.length;
		
		float startx = 0;
		for (int i = 0; i < numChars; i++) {
			FontTexture.CharInfo charInfo = fontTexture.getCharInfo(characters[i]);
			
			//Left Top
			positions.add(startx);
			positions.add(0.0f);
			positions.add(ZPOS);
			textCoords.add((float) charInfo.getStartX() / (float) fontTexture.getWidth());
			textCoords.add(0.0f);
			indices.add(i * VERTICES_PER_QUAD);
			
			//Left Bottom
			positions.add(startx);
			positions.add((float) fontTexture.getHeight());
			positions.add(ZPOS);
			textCoords.add((float) charInfo.getStartX() / (float) fontTexture.getWidth());
			textCoords.add(1.0f);
			indices.add(i * VERTICES_PER_QUAD + 1);
			
			//Right Bottom
			positions.add(startx + charInfo.getWidth());
			positions.add((float) fontTexture.getHeight());
			positions.add(ZPOS);
			textCoords.add((float) (charInfo.getStartX() + charInfo.getWidth()) / (float) fontTexture.getWidth());
			textCoords.add(1.0f);
			indices.add(i * VERTICES_PER_QUAD + 2);
			
			//Right Top
			positions.add(startx + charInfo.getWidth());
			positions.add(0.0f);
			positions.add(ZPOS);
			textCoords.add((float) (charInfo.getStartX() + charInfo.getWidth()) / (float) fontTexture.getWidth());
			textCoords.add(0.0f);
			indices.add(i * VERTICES_PER_QUAD + 3);
			
			indices.add(i * VERTICES_PER_QUAD);
			indices.add(i * VERTICES_PER_QUAD + 2);
			
			startx += charInfo.getWidth();
		}
		
		float[] posArr = Utils.listToArray(positions);
		float[] textCoordsArr = Utils.listToArray(textCoords);
		int[] indicesArr = indices.stream().mapToInt(i->i).toArray();
		Mesh mesh = new Mesh(posArr, textCoordsArr, normals, indicesArr);
		mesh.setMaterial(new Material(fontTexture.getTexture()));
		return mesh;
	}
	
	public String getText() {
		return text;
	}
	
	public void setText(String text) {
		this.text = text;
		this.getMesh().deleteBuffers();
		this.setMesh(buildMesh());
	}
}
