package com.clepto.fsengine.graphics;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.lwjgl.system.MemoryUtil;

import com.clepto.fsengine.scene.actors.Actor;

public class Mesh {

	private final int vaoId;
	
	private final List<Integer> vboIdList;
	
	private final int vertexCount;
	
	private Material material;
	
	public Mesh(float[] positions, float[] texCoords, float[] normals, int[] indices) {
		FloatBuffer posBuffer = null;
		FloatBuffer texCoordsBuffer = null;
		FloatBuffer vecNormalsBuffer = null;
		IntBuffer indicesBuffer = null;
		try {
			vertexCount = indices.length;
			vboIdList = new ArrayList<>();
			
			vaoId = glGenVertexArrays();
			glBindVertexArray(vaoId);
			
			//Position
			int vboId = glGenBuffers();
			vboIdList.add(vboId);
			posBuffer = MemoryUtil.memAllocFloat(positions.length);
			posBuffer.put(positions).flip();
			glBindBuffer(GL_ARRAY_BUFFER, vboId);
			glBufferData(GL_ARRAY_BUFFER, posBuffer, GL_STATIC_DRAW);
			glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
			
			//Texture Coordinates
			vboId = glGenBuffers();
			vboIdList.add(vboId);
			texCoordsBuffer = MemoryUtil.memAllocFloat(texCoords.length);
			texCoordsBuffer.put(texCoords).flip();
			glBindBuffer(GL_ARRAY_BUFFER, vboId);
			glBufferData(GL_ARRAY_BUFFER, texCoordsBuffer, GL_STATIC_DRAW);
			glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
			
			//Vertex Normals
			vboId = glGenBuffers();
			vboIdList.add(vboId);
			vecNormalsBuffer = MemoryUtil.memAllocFloat(normals.length);
			vecNormalsBuffer.put(normals).flip();
			glBindBuffer(GL_ARRAY_BUFFER, vboId);
			glBufferData(GL_ARRAY_BUFFER, vecNormalsBuffer, GL_STATIC_DRAW);
			glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);
			
			//Index
			vboId = glGenBuffers();
			vboIdList.add(vboId);
			indicesBuffer = MemoryUtil.memAllocInt(indices.length);
			indicesBuffer.put(indices).flip();
			glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboId);
			glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STATIC_DRAW);
			
			glBindBuffer(GL_ARRAY_BUFFER, 0);
			glBindVertexArray(0);
		} finally {
			if (posBuffer != null)
				MemoryUtil.memFree(posBuffer);
			if (texCoordsBuffer != null)
				MemoryUtil.memFree(texCoordsBuffer);
			if (vecNormalsBuffer != null) 
				MemoryUtil.memFree(vecNormalsBuffer);
			if (indicesBuffer != null)
				MemoryUtil.memFree(indicesBuffer);
		}
	}
	
	public Material getMaterial() {
		return material;
	}
	
	public void setMaterial(Material material) {
		this.material = material;
	}
	
	public int getVaoId() {
		return vaoId;
	}
	
	public int getVertexCount() {
		return vertexCount;
	}
	
	private void initRender() {
		Texture texture = material.getTexture();
		if (texture != null) {
			glActiveTexture(GL_TEXTURE0);
			glBindTexture(GL_TEXTURE_2D, texture.getId());
		}
		
		Texture normalMap = material.getNormalMap();
		if (normalMap != null) {
			glActiveTexture(GL_TEXTURE1);
			glBindTexture(GL_TEXTURE_2D, normalMap.getId());
		}
		
		glBindVertexArray(getVaoId());
		glEnableVertexAttribArray(0);
		glEnableVertexAttribArray(1);
		glEnableVertexAttribArray(2);
	}
	
	private void endRender() {
		glDisableVertexAttribArray(0);
		glDisableVertexAttribArray(1);
		glDisableVertexAttribArray(2);
		glBindVertexArray(0);
		
		glBindTexture(GL_TEXTURE_2D, 0);
	}
	
	public void render() {
		initRender();
		
		glDrawElements(GL_TRIANGLES, getVertexCount(), GL_UNSIGNED_INT, 0);
		
		endRender();
	}
	
	public void renderList(List<Actor> actors, Consumer<Actor> consumer) {
		initRender();
		
		for (Actor actor : actors) {
			consumer.accept(actor);
			glDrawElements(GL_TRIANGLES, getVertexCount(), GL_UNSIGNED_INT, 0);
		}
		
		endRender();
	}
	
	public void cleanUp() {
		glDisableVertexAttribArray(0);
		
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		for (int vboId : vboIdList) {
			glDeleteBuffers(vboId);
		}
		
		Texture texture = material.getTexture();
		if (texture != null) {
			texture.cleanup();
		}
		
		glBindVertexArray(0);
		glDeleteVertexArrays(vaoId);
	}
	
	public void deleteBuffers() {
		glDisableVertexAttribArray(0);
		
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		for (int vboId : vboIdList) {
			glDeleteBuffers(vboId);
		}
		
		glBindVertexArray(0);
		glDeleteVertexArrays(vaoId);
	}
	
}
