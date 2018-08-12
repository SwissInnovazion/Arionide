/*******************************************************************************
 * This file is part of Arionide.
 *
 * Arionide is an IDE whose purpose is to build a language from scratch. It is the work of Arion Zimmermann in context of his TM.
 * Copyright (C) 2018 AZEntreprise Corporation. All rights reserved.
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
package org.azentreprise.arionide.ui.primitives.font;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.function.Function;

public class TextTessellator {
	
	private final Metrics metrics;
	
	private final Function<CharMeta, float[]> uv0;
	private final Function<CharMeta, float[]> uv1;
	private final Function<CharMeta, float[]> uv2;
	private final Function<CharMeta, float[]> uv3;
	
	protected TextTessellator(Metrics metrics) {
		this.metrics = metrics;
		
		float size = metrics.getTextureSize();
		
		this.uv0 = meta -> new float[] {(meta.getX1() + 0.5f) / size, 1.0f - (meta.getY2() - 0.5f) / size};
		this.uv1 = meta -> new float[] {(meta.getX1() + 0.5f) / size, 1.0f - (meta.getY1() + 0.5f) / size};
		this.uv2 = meta -> new float[] {(meta.getX2() - 0.5f) / size, 1.0f - (meta.getY2() - 0.5f) / size};
		this.uv3 = meta -> new float[] {(meta.getX2() - 0.5f) / size, 1.0f - (meta.getY1() + 0.5f) / size};
	}
	
	public int getWidth(String input) {
		if(input.isEmpty()) {
			return 0;
		}
		
		return input.substring(0, input.length() - 1).chars().mapToObj(this.metrics::fetchMeta).mapToInt(CharMeta::getAdvance).sum() 
			 + this.metrics.fetchMeta(input.charAt(input.length() - 1)).getWidth();
	}
	
	public Metrics getMetrics() {
		return this.metrics;
	}
	
	public TessellationOutput tessellateString(String input) {
		char[] data = input.toCharArray();
		
		FloatBuffer vertices = FloatBuffer.allocate(8 * data.length);
		FloatBuffer textures = FloatBuffer.allocate(8 * data.length);
		IntBuffer indices = IntBuffer.allocate(6 * data.length);
		
		this.generateIndices(data.length, indices);

		int width = this.getWidth(input);
		int height = input.chars().mapToObj(this.metrics::fetchMeta).mapToInt(CharMeta::getAscent).max().orElse(0) + input.chars().mapToObj(this.metrics::fetchMeta).mapToInt(CharMeta::getDescent).max().orElse(0);
		
		int line = input.chars().mapToObj(this.metrics::fetchMeta).mapToInt(CharMeta::getAscent).max().orElse(0) - input.chars().mapToObj(this.metrics::fetchMeta).mapToInt(CharMeta::getDescent).max().orElse(0);
		
		int mainDimension = Math.max(width, height);
		
		float halfFactor = 1.0f / mainDimension;
		float factor = 2.0f * halfFactor;
		
		this.tessellateString(data, factor, -width * halfFactor, -line * halfFactor, vertices, textures);
		
		vertices.flip();
		textures.flip();
		indices.flip();
		
		return new TessellationOutput(vertices, textures, indices, width * factor, height * factor, data.length);
	}
	
	private void tessellateString(char[] data, float factor, float xOrigin, float yOrigin, FloatBuffer vertices, FloatBuffer textures) {
		int cursor = 0;
		
		for(char ch : data) {
			CharMeta meta = this.metrics.fetchMeta(ch);
			
			cursor = this.generateVertices(meta, vertices, factor, xOrigin, yOrigin, cursor);
			this.generateTextures(meta, textures);
		}
	}
	
	private int generateVertices(CharMeta meta, FloatBuffer vertices, float factor, float xOrigin, float yOrigin, int cursor) {
		vertices.put(xOrigin + cursor * factor);
		vertices.put(yOrigin - meta.getDescent() * factor);

		vertices.put(xOrigin + cursor * factor);
		vertices.put(yOrigin + meta.getAscent() * factor);
		
		vertices.put(xOrigin + (cursor + meta.getWidth()) * factor);
		vertices.put(yOrigin - meta.getDescent() * factor);
		
		vertices.put(xOrigin + (cursor + meta.getWidth()) * factor);
		vertices.put(yOrigin + meta.getAscent() * factor);

		return cursor + meta.getAdvance();
	}
	
	private void generateTextures(CharMeta meta, FloatBuffer textures) {
		textures.put(this.uv0.apply(meta));
		textures.put(this.uv1.apply(meta));
		textures.put(this.uv2.apply(meta));
		textures.put(this.uv3.apply(meta));
	}
	
	private void generateIndices(int count, IntBuffer indices) {
		for(int i = 0; i < count; i++) {
			indices.put(4 * i);
			indices.put(4 * i + 1);
			indices.put(4 * i + 2);
			indices.put(4 * i + 1);
			indices.put(4 * i + 2);
			indices.put(4 * i + 3);
		}
	}
}