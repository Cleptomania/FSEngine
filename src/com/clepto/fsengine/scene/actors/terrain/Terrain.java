package com.clepto.fsengine.scene.actors.terrain;

import java.nio.ByteBuffer;

import org.joml.Vector3f;

import com.clepto.fsengine.scene.actors.Actor;
import com.clepto.fsengine.util.Box2D;

import de.matthiasmann.twl.utils.PNGDecoder;

public class Terrain {

	private final Actor[] actors;
	
	private final int terrainSize;
	
	private final int verticesPerCol;
	
	private final int verticesPerRow;
	
	private final HeightMapMesh heightMapMesh;
	
	private final Box2D[][] boundingBoxes;
	
	public Terrain(int terrainSize, float scale, float minY, float maxY, String heightMapFile, String textureFile, int textInc) throws Exception {
		this.terrainSize = terrainSize;
		actors = new Actor[terrainSize * terrainSize];
		
		PNGDecoder decoder = new PNGDecoder(Terrain.class.getClassLoader().getResourceAsStream(heightMapFile));
		int height = decoder.getHeight();
		int width = decoder.getWidth();
		ByteBuffer buf = ByteBuffer.allocateDirect(4 * decoder.getWidth() * decoder.getHeight());
		decoder.decode(buf, decoder.getWidth() * 4, PNGDecoder.Format.RGBA);
		buf.flip();
		
		verticesPerCol = width - 1;
		verticesPerRow = height - 1;
		
		heightMapMesh = new HeightMapMesh(minY, maxY, buf, width, height, textureFile, textInc);
		boundingBoxes = new Box2D[terrainSize][terrainSize];	

		for (int row = 0; row < terrainSize; row++) {
			for (int col = 0; col < terrainSize; col++) {
				float xDisplacement = (col - ((float) terrainSize - 1) / (float) 2) * scale * HeightMapMesh.getXLength();
				float zDisplacement = (row - ((float) terrainSize - 1) / (float) 2) * scale * HeightMapMesh.getZLength();
				
				Actor terrainBlock = new Actor(heightMapMesh.getMesh());
				terrainBlock.setScale(scale);
				terrainBlock.setPosition(xDisplacement, 0, zDisplacement);
				actors[row * terrainSize + col] = terrainBlock;
				
				boundingBoxes[row][col] = getBoundingBox(terrainBlock);
			}
		}
	}
	
	public float getHeight(Vector3f position) {
		float result = Float.MIN_VALUE;
		Box2D boundingBox = null;
		boolean found = false;
		Actor terrainBlock = null;
		for (int row = 0; row < terrainSize && !found; row++) {
			for (int col = 0; col < terrainSize && !found; col++) {
				terrainBlock = actors[row * terrainSize + col];
				boundingBox = boundingBoxes[row][col];
				found = boundingBox.contains(position.x, position.z);
			}
		}
		
		if (found) {
			Vector3f[] triangle = getTriangle(position, boundingBox, terrainBlock);
			result = interpolateHeight(triangle[0], triangle[1], triangle[2], position.x, position.z);
		}
		
		return result;
	}
	
	protected Vector3f[] getTriangle(Vector3f position, Box2D boundingBox, Actor terrainBlock) {
		float cellWidth = boundingBox.width / (float) verticesPerCol;
		float cellHeight = boundingBox.height / (float) verticesPerRow;
		int col = (int) ((position.x - boundingBox.x) / cellWidth);
		int row = (int) ((position.z - boundingBox.y) / cellWidth);
		
		Vector3f[] triangle = new Vector3f[3];
		triangle[1] = new Vector3f(
				boundingBox.x + col * cellWidth,
				getWorldHeight(row + 1, col, terrainBlock),
				boundingBox.y + (row + 1) * cellHeight);
		triangle[2] = new Vector3f(
				boundingBox.x + (col + 1) * cellWidth,
				getWorldHeight(row, col + 1, terrainBlock),
				boundingBox.y + row * cellHeight);
		if (position.z < getDiagonalZCoord(triangle[1].x, triangle[1].z, triangle[2].x, triangle[2].z, position.x)) {
			triangle[0] = new Vector3f(
					boundingBox.x + col * cellWidth,
					getWorldHeight(row, col, terrainBlock),
					boundingBox.y + row * cellHeight);
		} else {
			triangle[0] = new Vector3f(
					boundingBox.x + (col + 1) * cellWidth,
					getWorldHeight(row + 2, col + 1, terrainBlock),
					boundingBox.y + (row + 1) * cellHeight);
		}
		
		return triangle;
	}
	
	protected float getDiagonalZCoord(float x1, float z1, float x2, float z2, float x) {
		return ((z1 - z2) / (x1 - x2)) * (x - x1) + z1;
	}
	
	protected float getWorldHeight(int row, int col, Actor actor) {
		float y = heightMapMesh.getHeight(row, col);
		return y * actor.getScale() + actor.getPosition().y;
	}
	
	protected float interpolateHeight(Vector3f pA, Vector3f pB, Vector3f pC, float x, float z) {
		float a = (pB.y - pA.y) * (pC.z - pA.z) - (pC.y - pA.y) * (pB.z - pA.z);
		float b = (pB.z - pA.z) * (pC.x - pA.x) - (pC.z - pA.z) * (pB.x - pA.x);
		float c = (pB.x - pA.x) * (pC.y - pA.y) - (pC.x - pA.x) * (pB.y - pA.y);
		float d = -(a * pA.x + b * pA.y + c * pA.z);
		return (-d -a * x - c * z) / b;
	}
	
	private Box2D getBoundingBox(Actor terrainBlock) {
		float scale = terrainBlock.getScale();
		Vector3f position = terrainBlock.getPosition();
		
		float topLeftX = HeightMapMesh.STARTX * scale + position.x;
		float topLeftZ = HeightMapMesh.STARTZ * scale + position.z;
		float width = Math.abs(HeightMapMesh.STARTX * 2) * scale;
		float height = Math.abs(HeightMapMesh.STARTX * 2) * scale;
		Box2D boundingBox = new Box2D(topLeftX, topLeftZ, width, height);
		return boundingBox;
	}
	
	public Actor[] getActors() {
		return actors;
	}
	
}
