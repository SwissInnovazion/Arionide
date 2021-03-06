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
package ch.innovazion.arionide.ui.core;

public class TeleportInfo {
	
	private int destination;
	private int focus;
	private long expires;
	
	public TeleportInfo(int destination, int focus) {
		this.destination = destination;
		this.focus = focus;
	}
	
	public void updateDestination(int destination) {
		this.destination = destination;
	}
	
	public void updateFocus(int focus) {
		this.focus = focus;
	}
	
	public void updateLifeTime(long lifeTime) {
		this.expires = System.currentTimeMillis() + lifeTime;
	}
	
	public int getDestination() {
		return this.destination;
	}
	
	public int getFocus() {
		return this.focus;
	}
	
	public boolean isAlive() {
		return System.currentTimeMillis() < this.expires && ~(focus & destination) != 0;
	}
	
	public String toString() {
		return "<TeleportInfo | Destination: " + this.destination + "; Focus: " + this.focus + ">";
	}
}
