package com.clepto.fsengine.graphics;

import static org.lwjgl.opengl.GL11.*;

import java.util.List;
import java.util.Map;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import com.clepto.fsengine.IHud;
import com.clepto.fsengine.Window;
import com.clepto.fsengine.graphics.lighting.DirectionalLight;
import com.clepto.fsengine.graphics.lighting.PointLight;
import com.clepto.fsengine.graphics.lighting.SpotLight;
import com.clepto.fsengine.graphics.shader.ShaderProgram;
import com.clepto.fsengine.scene.Scene;
import com.clepto.fsengine.scene.SceneLight;
import com.clepto.fsengine.scene.actors.Actor;
import com.clepto.fsengine.scene.actors.SkyBox;
import com.clepto.fsengine.util.Utils;

public class Renderer {
	
	private static final float FOV = (float) Math.toRadians(60.0f);
	
	private static final float Z_NEAR = 0.01f;
	
	private static final float Z_FAR = 1000.f;
	
	private static final int MAX_POINT_LIGHTS = 5;
	
	private static final int MAX_SPOT_LIGHTS = 5;
	
	private final Transformation transformation;
	
	private ShaderProgram sceneShaderProgram;
	
	private ShaderProgram skyboxShaderProgram;
	
	private ShaderProgram hudShaderProgram;
	
	private final float specularPower;
	
	public Renderer() {
		transformation = new Transformation();
		specularPower = 10f;
	}
	
	public void init(Window window) throws Exception {
		setupGL();
		setupSceneShader();
		setupSkyboxShader();
		setupHudShader();
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
		sceneShaderProgram.createUniform("normalMap");
		
		sceneShaderProgram.createMaterialUniform("material");
		
		sceneShaderProgram.createUniform("specularPower");
		sceneShaderProgram.createUniform("ambientLight");
		sceneShaderProgram.createPointLightListUniform("pointLights", MAX_POINT_LIGHTS);
		sceneShaderProgram.createSpotLightListUniform("spotLights", MAX_SPOT_LIGHTS);
		sceneShaderProgram.createDirectionalLightUniform("directionalLight");
		sceneShaderProgram.createFogUniform("fog");
	}
	
	private void setupSkyboxShader() throws Exception {
		skyboxShaderProgram = new ShaderProgram();
		skyboxShaderProgram.createVertexShader(Utils.loadResource("shaders/sb_vertex.vs"));
		skyboxShaderProgram.createFragmentShader(Utils.loadResource("shaders/sb_fragment.fs"));
		skyboxShaderProgram.link();
		
		skyboxShaderProgram.createUniform("projectionMatrix");
		skyboxShaderProgram.createUniform("modelViewMatrix");
		skyboxShaderProgram.createUniform("texture_sampler");
		skyboxShaderProgram.createUniform("ambientLight");
	}
	
	private void setupHudShader() throws Exception {
		hudShaderProgram = new ShaderProgram();
		hudShaderProgram.createVertexShader(Utils.loadResource("shaders/hud_vertex.vs"));
		hudShaderProgram.createFragmentShader(Utils.loadResource("shaders/hud_fragment.fs"));
		hudShaderProgram.link();
		
		hudShaderProgram.createUniform("projModelMatrix");
		hudShaderProgram.createUniform("color");
		hudShaderProgram.createUniform("hasTexture");
	}
	
	public void render(Window window, Camera camera, Scene scene, IHud hud) {
		clear();
		
		if (window.isResized()) {
			glViewport(0, 0, window.getWidth(), window.getHeight());
			window.setResized(false);
		}
		
		transformation.updateProjectionMatrix(FOV, window.getWidth(), window.getHeight(), Z_NEAR, Z_FAR);
		transformation.updateViewMatrix(camera);
		
		renderScene(window, camera, scene);
		renderSkybox(window, camera, scene);
		renderHud(window, hud);
	}
	
	private void renderScene(Window window, Camera camera, Scene scene) {
		sceneShaderProgram.bind();
	
		Matrix4f projectionMatrix = transformation.getProjectionMatrix();
		sceneShaderProgram.setUniform("projectionMatrix", projectionMatrix);
		
		Matrix4f viewMatrix = transformation.getViewMatrix();
		
		SceneLight sceneLight = scene.getSceneLight();
		renderLights(viewMatrix, sceneLight);
		
		sceneShaderProgram.setUniform("texture_sampler", 0);
		sceneShaderProgram.setUniform("normalMap", 1);
		sceneShaderProgram.setUniform("fog", scene.getFog());
		
		Map<Mesh, List<Actor>> mapMeshes = scene.getMeshes();
		for (Mesh mesh : mapMeshes.keySet()) {
			sceneShaderProgram.setUniform("material", mesh.getMaterial());
			mesh.renderList(mapMeshes.get(mesh), (Actor actor) -> {
				Matrix4f modelViewMatrix = transformation.buildModelViewMatrix(actor, viewMatrix);
				sceneShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
			});
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
	
	private void renderSkybox(Window window, Camera camera, Scene scene) {
		skyboxShaderProgram.bind();
		skyboxShaderProgram.setUniform("texture_sampler", 0);
		
		Matrix4f projectionMatrix = transformation.getProjectionMatrix();
		skyboxShaderProgram.setUniform("projectionMatrix", projectionMatrix);
		SkyBox skyBox = scene.getSkybox();
		Matrix4f viewMatrix = transformation.getViewMatrix();
		viewMatrix.m30(0);
		viewMatrix.m31(0);
		viewMatrix.m32(0);
		Matrix4f modelViewMatrix = transformation.buildModelViewMatrix(skyBox, viewMatrix);
		skyboxShaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
		skyboxShaderProgram.setUniform("ambientLight", scene.getSceneLight().getAmbientLight());
		
		scene.getSkybox().getMesh().render();
		
		skyboxShaderProgram.unbind();
	}
	
	private void renderHud(Window window, IHud hud) {
		hudShaderProgram.bind();
		
		Matrix4f ortho = transformation.getOrthoProjectionMatrix(0, window.getWidth(), window.getHeight(), 0);
		for (Actor actor : hud.getActors()) {
			Mesh mesh = actor.getMesh();
			Matrix4f projModelMatrix = transformation.buildOrthoProjModelMatrix(actor, ortho);
			hudShaderProgram.setUniform("projModelMatrix", projModelMatrix);
			hudShaderProgram.setUniform("color", actor.getMesh().getMaterial().getAmbientColor());
			hudShaderProgram.setUniform("hasTexture", actor.getMesh().getMaterial().isTextured() ? 1 : 0);
			mesh.render();
		}
		
		hudShaderProgram.unbind();
	}
	
	public void clear() {
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
	}
	
	public void cleanup() {
		if (sceneShaderProgram != null) {
			sceneShaderProgram.cleanup();
		}
		if (skyboxShaderProgram != null) {
			skyboxShaderProgram.cleanup();
		}
		if (hudShaderProgram != null) {
			hudShaderProgram.cleanup();
		}
	}
	
}
