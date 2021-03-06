package com.clepto.fsengine;

public interface IApplication {

	void init(Window window) throws Exception;
	
	void input(Window window, MouseInput mouseInput);
	
	void update(float interval, MouseInput mouseInput);
	
	void render(Window window);
	
	void cleanup();
}
