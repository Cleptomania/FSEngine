package com.clepto.fsengine.graphics;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;

import java.io.InputStream;
import java.nio.ByteBuffer;

import de.matthiasmann.twl.utils.PNGDecoder;
import de.matthiasmann.twl.utils.PNGDecoder.Format;

public class Texture {

	private final int id;
	
	private final int width;
	
	private final int height;
	
	public Texture(String fileName) throws Exception {
		this(Texture.class.getClassLoader().getResourceAsStream(fileName));
	}
	
	public Texture(InputStream is) throws Exception {
		PNGDecoder decoder = new PNGDecoder(is);
		
		this.width = decoder.getWidth();
		this.height = decoder.getHeight();
		
		ByteBuffer buf = ByteBuffer.allocateDirect(4 * decoder.getWidth() * decoder.getHeight());
		decoder.decode(buf, decoder.getWidth() * 4, Format.RGBA);
		buf.flip();
		
		this.id = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, this.id);
		
		glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
		
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, this.width, this.height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buf);
		glGenerateMipmap(GL_TEXTURE_2D);
	}
	
	public int getWidth() {
		return this.width;
	}
	
	public int getHeight() {
		return this.height;
	}
	
	public void bind() {
		glBindTexture(GL_TEXTURE_2D, id);
	}
	
	public int getId() {
		return id;
	}
	
	public void cleanup() {
		glDeleteTextures(id);
	}
	
}
