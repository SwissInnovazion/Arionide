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
package ch.innovazion.arionide.menu.params;

import ch.innovazion.arionide.events.TargetUpdateEvent;
import ch.innovazion.arionide.lang.symbols.Parameter;
import ch.innovazion.arionide.menu.Menu;
import ch.innovazion.arionide.menu.MenuDescription;
import ch.innovazion.arionide.menu.MenuManager;
import ch.innovazion.arionide.project.Structure;
import ch.innovazion.automaton.Export;
import ch.innovazion.automaton.Inherit;

public class ParameterSelector extends Menu {
	
	private final boolean mutable;
	
	@Export
	@Inherit
	protected Structure target;
	
	@Export
	protected Parameter parameter;
		
	public ParameterSelector(MenuManager manager, boolean mutable) {
		super(manager, mutable ? "<Add>" : null);
		
		this.mutable = mutable;
	}
	
	protected void onEnter() {		
		setDynamicElements(target.getSpecification().getParameters().stream().map(Parameter::getName).toArray(String[]::new));
		super.onEnter();
	}
	
	protected void updateCursor(int cursor) {
		super.updateCursor(cursor);
		
		if(mutable && id == 0) {
			this.description = new MenuDescription("Add a new parameter to '" + target.getName() + "'");
		} else {
			this.parameter = target.getSpecification().getParameters().get(id);
			this.description = new MenuDescription(parameter.getDisplayValue().toArray(new String[0]));

			dispatch(new TargetUpdateEvent(((id + 1) << 24) | target.getIdentifier()));	
		}
	}
	
	public void onAction(String action) {
		if(mutable) {
			if(id == 0) {
				go("create");
			} else {
				go("edit");
			}
		} else {
			go(EditorMultiplexer.findDestination(parameter.getValue()));
		}
	}
}