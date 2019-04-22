package com.clepto.fsengine.graphics.weather;

import org.joml.Vector3f;

public class Fog {

	private boolean active;
	
	private Vector3f color;
	
	private float density;
	
	public static Fog NOFOG = new Fog();
	
	public Fog() {
		active = false;
		this.color = new Vector3f(0, 0, 0);
		this.density = 0;
	}
	
	public Fog(boolean active, Vector3f color, float density) {
		this.color = color;
		this.density = density;
		this.active = active;
	}
	
	public boolean isActive() {
		return active;
	}
	
	public void setActive(boolean active) {
		this.active = active;
	}
	
	public Vector3f getColor() {
		return color;
	}
	
	public void setColor(Vector3f color) {
		this.color = color;
	}
	
	public float getDensity() {
		return density;
	}
	
	public void setDensity(float density) {
		this.density = density;
	}
	
}
