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
package ch.innovazion.arionide.ui;

import java.util.ArrayList;
import java.util.List;

import ch.innovazion.arionide.events.FocusGainedEvent;
import ch.innovazion.arionide.events.FocusLostEvent;
import ch.innovazion.arionide.events.dispatching.IEventDispatcher;
import ch.innovazion.arionide.ui.overlay.Component;

public class FocusManager {
	
	private static final int NOT_INITIALIZED = 0xC0FFEE;
	
	private final List<Component> components = new ArrayList<>();
	private final IEventDispatcher dispatcher;
	
	private List<Integer> cycle = null; // This array represents the values of a bijection of a modular Z/nZ space with itself.
	
	private int focus = FocusManager.NOT_INITIALIZED;
	
	public FocusManager(IEventDispatcher dispatcher) {
		this.dispatcher = dispatcher;
	}
	
	public void registerComponent(Component component) {
		this.components.add(component);
	}
	
	public int requestViewUID() {
		return this.components.size();
	}
	
	public void setupCycle(List<Integer> elements) {
		assert elements.size() > 0 && elements.size() <= this.components.size();
		
		this.cycle = elements;
		
		this.request(0);
	}
	
	public void request(Component component) {
		int index = this.cycle.indexOf(this.components.indexOf(component));

		if(index > -1) {
			this.request(index);
		} // Else ignore because the component is hidden
	}
	
	public void request(int id) {
		if(this.focus == id) {
			return;
		}
		
		this.loseFocus();

		this.focus = id;
		Component current = this.accessModular();
				
		if(current.isFocusable()) {
			this.dispatcher.fire(new FocusGainedEvent(current));
		} else if(id != -1) {
			this.focus++;
			this.tryIncrementalFocus(1);
		}
	}
	
	public void next() {
		this.loseFocus();
		
		this.focus++;
				
		this.tryIncrementalFocus(1);
	}
	
	public void prev() {
		this.loseFocus();
		
		this.focus--;
		
		this.tryIncrementalFocus(-1);
	}
	
	private void loseFocus() {
		if(this.focus != FocusManager.NOT_INITIALIZED) {
			this.dispatcher.fire(new FocusLostEvent(this.accessModular()));
		}
	}
	
	private void tryIncrementalFocus(int lambda) {
		
		int index = this.focus;
		
		while(!this.accessModular().isFocusable()) {
			this.focus += lambda;
			
			if(Math.abs(this.focus - index) >= this.cycle.size()) {
				return; // failed (no component is focusable)
			}
		}
		
		this.dispatcher.fire(new FocusGainedEvent(this.accessModular()));
	}

	private Component accessModular() {
		return this.components.get(this.cycle.get(Math.floorMod(this.focus, this.cycle.size())));
	}
}