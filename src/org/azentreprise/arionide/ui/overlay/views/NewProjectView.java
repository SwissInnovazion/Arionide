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
package org.azentreprise.arionide.ui.overlay.views;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.azentreprise.arionide.debugging.Debug;
import org.azentreprise.arionide.events.ClickEvent;
import org.azentreprise.arionide.events.Event;
import org.azentreprise.arionide.events.EventHandler;
import org.azentreprise.arionide.ui.AppManager;
import org.azentreprise.arionide.ui.ApplicationTints;
import org.azentreprise.arionide.ui.layout.LayoutManager;
import org.azentreprise.arionide.ui.overlay.View;
import org.azentreprise.arionide.ui.overlay.Views;
import org.azentreprise.arionide.ui.overlay.components.Button;
import org.azentreprise.arionide.ui.overlay.components.Input;
import org.azentreprise.arionide.ui.overlay.components.Label;

public class NewProjectView extends View implements EventHandler {
	
	private final Input projectName = new Input(this, "Project name");
	
	public NewProjectView(AppManager appManager, LayoutManager layoutManager) {
		super(appManager, layoutManager);
		
		layoutManager.register(this, null, 0.1f, 0.1f, 0.9f, 0.9f);
		
		this.setBorderColor(ApplicationTints.MAIN_COLOR);
				
		this.add(new Label(this, "New project"), 0, 0.05f, 1.0f, 0.3f);
		
		this.add(this.projectName, 0.1f, 0.4f, 0.9f, 0.6f);
		
		this.add(new Button(this, "Create").setSignal("create"), 0.1f, 0.8f, 0.45f, 0.9f);
		this.add(new Button(this, "Cancel").setSignal("cancel"), 0.55f, 0.8f, 0.9f, 0.9f);
		
		this.getAppManager().getEventDispatcher().registerHandler(this);
	}
	
	public void show() {
		super.show();
		this.setupFocusCycle();
	}

	public <T extends Event> void handleEvent(T event) {
		if(event instanceof ClickEvent) {
			
			ClickEvent click = (ClickEvent) event;
			
			if(click.isTargetting(this, "create")) {
				String name = this.projectName.toString();
				
				if(!name.isEmpty()) {
					try {
						this.getAppManager().getWorkspace().createProject(name);
						this.openView(Views.code);
						
						this.projectName.setText(new String()); // Reset field
					} catch(IOException exception) {
						Debug.exception(exception);
					}
				}
			} else if(click.isTargetting(this, "cancel")) {
				this.openView(Views.main);
				this.projectName.setText(new String()); // Reset field
			}
		}
	}

	public List<Class<? extends Event>> getHandleableEvents() {
		return Arrays.asList(ClickEvent.class);
	}
}