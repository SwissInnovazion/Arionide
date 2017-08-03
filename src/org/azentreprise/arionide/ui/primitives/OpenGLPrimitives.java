/*******************************************************************************
 * This file is part of Arionide.
 *
 * Arionide is an IDE whose purpose is to build a language from scratch. It is the work of Arion Zimmermann in context of his TM.
 * Copyright (C) 2017 AZEntreprise Corporation. All rights reserved.
 *
 * Arionide is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Arionide is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with Arionide.  If not, see <http://www.gnu.org/licenses/>.
 *
 * The copy of the GNU General Public License can be found in the 'LICENSE.txt' file inside the src directory or inside the JAR archive.
 *******************************************************************************/
package org.azentreprise.arionide.ui.primitives;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.IntBuffer;
import java.nio.charset.Charset;

import org.azentreprise.arionide.ui.AppDrawingContext;
import org.azentreprise.arionide.ui.OpenGLDrawingContext;

import com.jogamp.opengl.GL4;
import com.jogamp.opengl.util.glsl.ShaderUtil;

public class OpenGLPrimitives implements IPrimitives {
	
	private VAOManager manager;
	
	private int uiShader;
	
	private int rgb;
	private int alpha;
	private int pixelSize;
	
	private double pixelWidth;
	private double pixelHeight;
	
	public void init(GL4 gl) {
		try {
			int vert = this.loadShader(gl, "gui.vert", GL4.GL_VERTEX_SHADER);
			int frag = this.loadShader(gl, "gui.frag", GL4.GL_FRAGMENT_SHADER);
			int geom = this.loadShader(gl, "gui.geom", GL4.GL_GEOMETRY_SHADER);
			
			this.uiShader = gl.glCreateProgram();
			
			gl.glAttachShader(this.uiShader, vert);
			gl.glAttachShader(this.uiShader, frag);
			gl.glAttachShader(this.uiShader, geom);
			
			gl.glBindFragDataLocation(this.uiShader, 0, "color");
			
			gl.glLinkProgram(this.uiShader);
			
			this.rgb = gl.glGetUniformLocation(this.uiShader, "rgb");
			this.alpha = gl.glGetUniformLocation(this.uiShader, "alpha");
			this.pixelSize = gl.glGetUniformLocation(this.uiShader, "pixelSize");
			
			this.manager = new VAOManager(gl, this.uiShader);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void viewportChanged(int width, int height) {
		this.pixelWidth = 2.0d / width;
		this.pixelHeight = 2.0d / height;
	}
	
	public void beginUI(GL4 gl) {
		gl.glUseProgram(this.uiShader);
		
		gl.glEnable(GL4.GL_BLEND);
		gl.glBlendFunc(GL4.GL_SRC_ALPHA, GL4.GL_ONE_MINUS_SRC_ALPHA);
		
		gl.glUniform2d(this.pixelSize, this.pixelWidth, this.pixelHeight);
	}
	
	public void endUI(GL4 gl) {
		gl.glDisable(GL4.GL_BLEND);
		gl.glUseProgram(0);
	}
	
	/* WARNING: Using the OpenGL implementation, the Rectangle2D bounds is actually a data structure representing the data (x1, y1, x2, y2). */
	
	public void drawRect(AppDrawingContext context, Rectangle2D bounds) {
		// Disable the geometry shader in order to use regular rectangles
		
		GL4 gl = this.getGL(context);
		GLCoordinates coords = new GLCoordinates(bounds);
		
		this.manager.loadVAO(coords.getUUID(), () -> coords.allocDataBuffer(8).putBoundingPoints().getDataBuffer(), (nil, id) -> {
			gl.glVertexAttribPointer(id, 2, GL4.GL_DOUBLE, false, 0, 0);
		}, "position");

		gl.glDrawArrays(GL4.GL_LINE_LOOP, 0, 4);
	}

	@Override
	public void fillRect(AppDrawingContext context, Rectangle2D bounds) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void drawRoundRect(AppDrawingContext context, Rectangle2D bounds) {		
		GL4 gl = this.getGL(context);
		GLCoordinates coords = new GLCoordinates(bounds);
		
		this.manager.loadVAO(coords.getUUID(), () -> coords.allocDataBuffer(8).putBoundingPoints().getDataBuffer(), (nil, id) -> {
			gl.glVertexAttribPointer(id, 2, GL4.GL_DOUBLE, false, 0, 0);
		}, "position");

		gl.glDrawArrays(GL4.GL_LINES_ADJACENCY, 0, 4);
	}

	@Override
	public void fillRoundRect(AppDrawingContext context, Rectangle2D bounds) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void drawLine(AppDrawingContext context, double x1, double y1, double x2, double y2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Point2D drawText(AppDrawingContext context, String text, Rectangle2D bounds) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Point2D drawText(AppDrawingContext context, String text, Rectangle2D bounds, int yCorrection) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void setColor(GL4 gl, float r, float g, float b) {
		gl.glUniform3f(this.rgb, r, g, b);
	}
	
	public void setAlpha(GL4 gl, float alpha) {
		gl.glUniform1f(this.alpha, alpha);
	}
	
	private GL4 getGL(AppDrawingContext context) {
		assert context instanceof OpenGLDrawingContext;
		return ((OpenGLDrawingContext) context).getRenderer();
	}
	
	private int loadShader(GL4 gl, String name, int type) throws IOException {
		InputStream input = this.getClass().getResourceAsStream(name);
		
		byte[] buffer = new byte[128];
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int count = 0;
		
		while((count = input.read(buffer)) != -1) {
			baos.write(buffer, 0, count);
		}
		
		String code = new String(baos.toByteArray(), Charset.forName("utf8"));

		IntBuffer shaderID = IntBuffer.allocate(1);
		ShaderUtil.createAndCompileShader(gl, shaderID, type, new String[][] {{ code }}, System.err);
		
		return shaderID.get(0);
	}
}