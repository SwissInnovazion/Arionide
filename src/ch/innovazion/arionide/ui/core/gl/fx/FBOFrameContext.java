/*******************************************************************************
 * This file is part of Arionide.
 *
 * Arionide is an IDE used to conceive applications and algorithms in a three-dimensional environment. 
 * It is the work of Arion Zimmermann for his final high-school project at Calvin College (Geneva, Switzerland).
 * Copyright (C) 2016-2020 Innovazion. All rights reserved.
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
package ch.innovazion.arionide.ui.core.gl.fx;

import com.jogamp.opengl.GL4;

import ch.innovazion.arionide.ui.core.gl.Context;

public class FBOFrameContext implements Context {
	
	private final int positionAttribute;
	private final int colorTextureUniform;
	private final int depthTextureUniform;
	private final int flareTextureUniform;
	private final int currentToPreviousViewportMatrixUniform;
	private final int lightPositionUniform;
	private final int exposureUniform;
	private final int pixelSizeUniform;
	
	public FBOFrameContext(GL4 gl, int shader) {
		this.positionAttribute = gl.glGetAttribLocation(shader, "position");
		
		this.colorTextureUniform = gl.glGetUniformLocation(shader, "colorTexture");
		this.depthTextureUniform = gl.glGetUniformLocation(shader, "depthTexture");
		this.flareTextureUniform = gl.glGetUniformLocation(shader, "flareTexture");
		this.currentToPreviousViewportMatrixUniform = gl.glGetUniformLocation(shader, "currentToPreviousViewportMatrix");
		this.lightPositionUniform = gl.glGetUniformLocation(shader, "lightPosition");
		this.exposureUniform = gl.glGetUniformLocation(shader, "exposure");
		this.pixelSizeUniform = gl.glGetUniformLocation(shader, "pixelSize");
	}
		
	protected int getPositionAttribute() {
		return positionAttribute;
	}

	protected int getColorTextureUniform() {
		return colorTextureUniform;
	}

	protected int getDepthTextureUniform() {
		return depthTextureUniform;
	}
	
	protected int getFlareTextureUniform() {
		return flareTextureUniform;
	}

	protected int getCurrentToPreviousViewportMatrixUniform() {
		return currentToPreviousViewportMatrixUniform;
	}

	protected int getLightPositionUniform() {
		return lightPositionUniform;
	}

	protected int getExposureUniform() {
		return exposureUniform;
	}

	protected int getPixelSizeUniform() {
		return pixelSizeUniform;
	}
}