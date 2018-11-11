package com.clepto.fsengine.graphics;

import static org.lwjgl.opengl.GL11.*;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import com.clepto.fsengine.Window;
import com.clepto.fsengine.graphics.lighting.DirectionalLight;
import com.clepto.fsengine.graphics.lighting.PointLight;
import com.clepto.fsengine.graphics.lighting.SpotLight;
import com.clepto.fsengine.scene.Scene;
import com.clepto.fsengine.scene.SceneLight;
import com.clepto.fsengine.scene.actors.Actor;
import com.clepto.fsengine.util.Utils;

public class Renderer {
	
	private static final float FOV = (float) Math.toRadians(60.0f);
	
	private static final float Z_NEAR = 0.01f;
	
	private static final float Z_FAR = 1000.f;
	
	private static final int MAX_POINT_LIGHTS = 5;
	
	private static final int MAX_SPOT_LIGHTS = 5;
	
	private final Transformation transformation;
	
	private ShaderProgram sceneShaderProgram;
	
	private float specularPower;
	
	public Renderer() {
		transformation = new Transformation();
		specularPower = 10f;
	}
	
	public void init(Window window) throws Exception {
		setupGL();
		setupSceneShader();
	}
	
	private void setupGL() {
		glEnable(GL_DEPTH_TEST);
	}
	
	private void setupSceneShader() throws Exception {
		sceneShaderProgram = new ShaderProgram();
		sceneShaderProgram.createVertexShader(Utils.loadResource("shaders/scene_vertex.vs"));
		sceneShaderProgram.createFragmentShader(Utils.loadResource("shaders/scene_fragment.fs"));
		sceneShaderProgram.link();
		
		sceneShaderProgram.createUniform("projectionMatrix");
		sceneShaderProgram.createUniform("modelViewMatrix");
		sceneShaderProgram.createUniform("texture_sampler");
		
		sceneShaderProgram.createMaterialUniform("material");
		
		sceneShaderProgram.createUniform("specularPower");
		sceneShaderProgram.createUniform("ambientLight");
		sceneShaderProgram.createPointLightListUniform("pointLights", MAX_POINT_LIGHTS);
		sceneShaderProgram.createSpotLightListUniform("spotLights", MAX_SPOT_LIGHTS);
		sceneShaderProgram.createDirectionalLightUniform("directionalLight");
		
	}
	
	public void render(Window window, Camera camera, Scene scene) {
		clear();
		
		if (window.isResized()) {
			glViewport(0, 0, window.getWidth(), window.getHeight());
			window.setResized(false);
		}
		
		renderScene(window, camera, scene);
	}
	
	private void renderScene(Window window, Camera camera, Scene scene) {
		sceneShaderProgram.bind();
	
		Matrix4f projectionMatrix = transformation.getProjectionMatrix(FOV, window.getWidth(), window.getHeight(), Z_NEAR, Z_FAR);
		sceneShaderProgram.setUniform("projectionMatrix", projectionMatrix);
		
		Matrix4f viewMatrix = transformation.getViewMatrix(camera);
		
		SceneLight sceneLight = scene.getSceneLight();
		renderLights(viewMatrix, sceneLight);
	
		sceneShaderProgram.setUniform("texture_sampler", 0);
		
		Actor[] actors = scene.getActors();
		for (Actor actor : actors) {
			Mesh mesh = actor.getMesh();
			Matrix4f modelViewMatrix = transformation.getModelViewMatrix(actor, viewMatrix);
			sceneShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
			sceneShaderProgram.setUniform("material", mesh.getMaterial());
			mesh.render();
		}
		
		sceneShaderProgram.unbind();
	}
	
	private void renderLights(Matrix4f viewMatrix, SceneLight sceneLight) {
		sceneShaderProgram.setUniform("ambientLight", sceneLight.getAmbientLight());
		sceneShaderProgram.setUniform("specularPower", specularPower);
		
		PointLight[] pointLights = sceneLight.getPointLights();
		int numLights = pointLights != null ? pointLights.length : 0;
		for (int i = 0; i < numLights; i++) {
			PointLight currPointLight = new PointLight(pointLights[i]);
			Vector3f lightPos = currPointLight.getPosition();
			Vector4f aux = new Vector4f(lightPos, 1);
			aux.mul(viewMatrix);
			lightPos.x = aux.x;
			lightPos.y = aux.y;
			lightPos.z = aux.z;
			sceneShaderProgram.setUniform("pointLights[" + i + "]", currPointLight);
		}
		
		SpotLight[] spotLights = sceneLight.getSpotLights();
		numLights = spotLights != null ? spotLights.length : 0;
		for (int i = 0; i < numLights; i++) {
			SpotLight currSpotLight = new SpotLight(spotLights[i]);
			Vector4f dir = new Vector4f(currSpotLight.getConeDirection(), 0);
			dir.mul(viewMatrix);
			currSpotLight.setConeDirection(new Vector3f(dir.x, dir.y, dir.z));
			
			Vector3f spotLightPos = currSpotLight.getPointLight().getPosition();
			Vector4f auxSpot = new Vector4f(spotLightPos, 1);
			auxSpot.mul(viewMatrix);
			spotLightPos.x = auxSpot.x;
			spotLightPos.y = auxSpot.y;
			spotLightPos.z = auxSpot.z;
			sceneShaderProgram.setUniform("spotLights[" + i + "]", currSpotLight);
		}
		
		if (sceneLight.getDirectionalLight() != null) {
			DirectionalLight currDirLight = new DirectionalLight(sceneLight.getDirectionalLight());
			Vector4f dir = new Vector4f(currDirLight.getDirection(), 0);
			dir.mul(viewMatrix);
			currDirLight.setDirection(new Vector3f(dir.x, dir.y, dir.z));
			sceneShaderProgram.setUniform("directionalLight", currDirLight);
		}
	}
	
	public void clear() {
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
	}
	
	public void cleanup() {
		if (sceneShaderProgram != null) {
			sceneShaderProgram.cleanup();
		}
	}
	
}
