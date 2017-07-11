/*******************************************************************************
 * This file is part of ArionIDE.
 *
 * ArionIDE is an IDE whose purpose is to build a language from assembly. It is the work of Arion Zimmermann in context of his TM.
 * Copyright (C) 2017 AZEntreprise Corporation. All rights reserved.
 *
 * ArionIDE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ArionIDE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with ArionIDE.  If not, see <http://www.gnu.org/licenses/>.
 *
 * The copy of the GNU General Public License can be found in the 'LICENSE.txt' file inside the JAR archive.
 *******************************************************************************/
package org.azentreprise.arionide.ui;

import java.awt.Canvas;
import java.awt.Font;
import java.awt.FontMetrics;
import java.util.ArrayList;
import java.util.List;

public class FontAdapter {
	
	// Please consider using powers of two for these variables
	private static final int MAX_FONT_SIZE = 64;
	private static final float STEP = 0.25f;
	
	// Faster than (int) (Math.log(MAX_FONT_SIZE / STEP) / Math.log(2));
	private static final int COMPLEXITY = 31 - Integer.numberOfLeadingZeros((int) (MAX_FONT_SIZE / STEP));

	// Approximation of the width ratio
	private static final String CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
	
	private static final double CORRECTION_WIDTH = 0.9d;
	private static final double CORRECTION_HEIGHT = 0.7d;
	
	/* 
	 * The cache info holds information about width per char and height per char.
	 * The point is that these two ratios are strictly monotonically increasing.
	 * The distribution should be approximately linear.
	 */
	private final List<CacheInfo> cache = new ArrayList<>();
	
	private FontMetrics lastMetrics;
	
	public FontAdapter(Font font) {
		Canvas fakeCanvas = new Canvas(); 

		for(float i = 0; i < MAX_FONT_SIZE; i += STEP) {
			font = font.deriveFont(i);
			
			FontMetrics metrics = fakeCanvas.getFontMetrics(font);
			
			double width = metrics.stringWidth(CHARS);
			double height = metrics.getHeight();
					
			this.cache.add(new CacheInfo(font, metrics, width / CHARS.length(), height));
		}
	}
	
	// linear interpolation
	public Font adapt(String str, double width, double height) {
		double iw = width * CORRECTION_WIDTH / str.length();
		double ih = height * CORRECTION_HEIGHT;
		
		CacheInfo info = this.makeInterpolation(iw, ih);
		
		this.lastMetrics = info.getFontMetrics();
		
		return info.getFont();
	}
	
	// dichotomy algorithm
	private CacheInfo makeInterpolation(double iw, double ih) {
		
		int current = (int) (MAX_FONT_SIZE / STEP / 2);
		CacheInfo sample = null;

		for(int i = 2; i < COMPLEXITY; i++) {
			sample = this.cache.get(current);
			
			if(sample.getWidthRatio() >= iw || sample.getHeightRatio() >= ih) {
				current -= 0x1 << (COMPLEXITY - i);
			} else {
				current += 0x1 << (COMPLEXITY - i);
			}
		}
		
		return sample;
	}
	
	public FontMetrics getLastMetrics() {
		return this.lastMetrics;
	}
	
	private static final class CacheInfo {
		private final Font font;
		private final FontMetrics metrics;
		private final double widthRatio;
		private final double heightRatio;
		
		private CacheInfo(Font font, FontMetrics metrics, double widthRatio, double heightRatio) {
			this.font = font;
			this.metrics = metrics;
			this.widthRatio = widthRatio;
			this.heightRatio = heightRatio;
		}
		
		private Font getFont() {
			return this.font;
		}
		
		private FontMetrics getFontMetrics() {
			return this.metrics;
		}
		
		private double getWidthRatio() {
			return this.widthRatio;
		}
		
		private double getHeightRatio() {
			return this.heightRatio;
		}
	}
}
