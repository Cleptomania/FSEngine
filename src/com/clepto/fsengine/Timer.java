package com.clepto.fsengine;

public class Timer {

	private double lastLoopTime;
	
	public void init() {
		lastLoopTime = getTime();
	}
	
	public double getTime() {
		return System.nanoTime() / 1000000000.0;
	}
	
	public float getElapsedTime() {
		double time = getTime();
		float elapsedTime = (float) (time - lastLoopTime);
		lastLoopTime = time;
		return elapsedTime;
	}
	
	public double getLastLoopTime() {
		return lastLoopTime;
	}
	
}