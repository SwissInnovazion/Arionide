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
package ch.innovazion.arionide.ui.overlay.views;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import ch.innovazion.arionide.Utils;
import ch.innovazion.arionide.Workspace;
import ch.innovazion.arionide.debugging.Debug;
import ch.innovazion.arionide.events.ClickEvent;
import ch.innovazion.arionide.events.Event;
import ch.innovazion.arionide.events.EventHandler;
import ch.innovazion.arionide.events.TimerEvent;
import ch.innovazion.arionide.project.Project;
import ch.innovazion.arionide.ui.AppDrawingContext;
import ch.innovazion.arionide.ui.AppManager;
import ch.innovazion.arionide.ui.ApplicationTints;
import ch.innovazion.arionide.ui.animations.Animation;
import ch.innovazion.arionide.ui.animations.FieldModifierAnimation;
import ch.innovazion.arionide.ui.layout.LayoutManager;
import ch.innovazion.arionide.ui.overlay.AlphaLayer;
import ch.innovazion.arionide.ui.overlay.AlphaLayeringSystem;
import ch.innovazion.arionide.ui.overlay.Component;
import ch.innovazion.arionide.ui.overlay.View;
import ch.innovazion.arionide.ui.overlay.Views;
import ch.innovazion.arionide.ui.overlay.components.Button;
import ch.innovazion.arionide.ui.overlay.components.Deformable;
import ch.innovazion.arionide.ui.overlay.components.Label;
import ch.innovazion.arionide.ui.render.AffineTransformable;
import ch.innovazion.arionide.ui.topology.Affine;
import ch.innovazion.arionide.ui.topology.Bounds;

public class MainView extends View implements EventHandler {

	private static enum SwipeDirection {
		LEFT,
		RIGHT
	}
	
	private final List<Component> body = new ArrayList<>();
	private final List<Component> page = new ArrayList<>();;
	private final List<Component> commons = new ArrayList<>();;

	private final Animation transformWidthAnimation;
	private final Animation bodyAlphaAnimation;
	private final Button prev;
	private final Button next;
	
	private int pageID = 0;
		
	private float transformWidth = 1.0f;
	private int bodyAlpha = 0xFF;
	
	public MainView(AppManager appManager, LayoutManager layoutManager) {
		super(appManager, layoutManager);
		
		this.transformWidthAnimation = new FieldModifierAnimation(this.getAppManager(), "transformWidth", MainView.class, this);
		this.bodyAlphaAnimation = new FieldModifierAnimation(this.getAppManager(), "bodyAlpha", MainView.class, this);
		
		layoutManager.register(this, null, 0.1f, 0.1f, 0.9f, 0.9f);
		
		this.setBorderColor(ApplicationTints.MAIN_COLOR);
		
		this.add(new Label(this, "Home").enclose(this.commons), 0.0f, 0.05f, 1.0f, 0.2f);
		
		this.add(new Button(this, "Undefined").enclose(this.body).enclose(this.page), 0.1f, 0.23f, 0.9f, 0.33f);
		this.add(new Button(this, "Undefined").enclose(this.body).enclose(this.page), 0.1f, 0.38f, 0.9f, 0.48f);
		this.add(new Button(this, "Undefined").enclose(this.body).enclose(this.page), 0.1f, 0.53f, 0.9f, 0.63f);
		this.add(new Button(this, "Undefined").enclose(this.body).enclose(this.page), 0.1f, 0.68f, 0.9f, 0.78f);

		this.add(new Button(this, "New project").setSignal("new").enclose(this.commons), 0.1f, 0.83f, 0.33f, 0.92f);
		this.add(new Button(this, "Collaborate").setSignal("connect").enclose(this.commons), 0.38f, 0.83f, 0.61f, 0.92f);
		this.add(new Button(this, "Import").setSignal("import").enclose(this.commons), 0.66f, 0.83f, 0.90f, 0.92f);
		
		this.add(this.prev = new Button(this, "<").setSignal("prev"), 0.2f / 7.0f, 0.43f, 0.5f / 7.0f, 0.58f);
		this.add(this.next = new Button(this, ">").setSignal("next"), 6.5f / 7.0f, 0.43f, 6.8f / 7.0f, 0.58f);
		
		this.prev.enclose(this.body);
		this.next.enclose(this.body);
		
		this.getAppManager().getEventDispatcher().registerHandler(this);

		this.pageID = this.getMaxPage();
	}
	
	private void loadWorkspace() {		
		Workspace theWorkspace = this.getAppManager().getWorkspace();
		
		try {
			theWorkspace.load();
		} catch (IOException exception) {
			Debug.exception(exception);
		}
		
		List<? super Project> projects = theWorkspace.getProjectList();
		
		if(this.pageID > this.getMaxPage()) {
			this.pageID = this.getMaxPage();
		}
		
		prev.setVisible(this.pageID > 0);
		next.setVisible(this.pageID < this.getMaxPage());
		
		if(this.pageID > 0) {
			int scalar = 4 * (this.pageID - 1);
			int displacement = 0;
			
			for(Component component : this.page) {
				Button button = (Button) component;
				
				if(scalar + displacement < projects.size()) {
					Project project = (Project) projects.get(scalar + displacement);
					button.setSignal("open", project).setLabel("Open " + project.getName());
					button.setVisible(true);
				} else {
					button.setVisible(false);
				}
				
				displacement++;
			}
		} else {
			((Button) this.page.get(0)).setSignal("browse", "https://azentreprise.org").setLabel("AZEntreprise.org").setVisible(true);
			((Button) this.page.get(1)).setSignal("browse", "https://azentreprise.org/Arionide/bugreport.php").setLabel("Arionide bug report").setVisible(true);
			((Button) this.page.get(2)).setSignal("browse", "https://azentreprise.org/Arionide/tutorials.php").setLabel("Arionide tutorials").setVisible(true);
			((Button) this.page.get(3)).setSignal("browse", "https://azentreprise.org/Arionide").setLabel("Arionide community").setVisible(true);
		}
		
		setupFocusCycle();
		setupFocus();
	}
	
	private void setupFocus() {		
		if(this.pageID > 0) {
			this.getAppManager().getFocusManager().request(this.getAppManager().getWorkspace().getProjectList().size() % 4);
		} else {
			this.getAppManager().getFocusManager().request(5);
		}
	}
	
	private int getMaxPage() {
		return (this.getAppManager().getWorkspace().getProjectList().size() + 3) / 4;
	}
	
	public void viewWillAppear() {				
		loadWorkspace();
	}
	
	public void drawSurface(AppDrawingContext context) {
		for(Component component : this.page) {
			Bounds bounds = component.getBounds();
			
			if(bounds != null) {
				float leftToOrigin = Math.abs((float) bounds.getX() - 1.0f);
				float rightToOrigin = Math.abs((float) bounds.getX() + (float) bounds.getWidth() - 1.0f);
			
				if(component instanceof Deformable) {
					for(AffineTransformable primitive : ((Deformable) component).getDeformablePrimitives()) {
						if(this.transformWidth < 0.0d) {
							primitive.updateAffine(new Affine(-this.transformWidth, 1.0f, rightToOrigin * (this.transformWidth + 1.0f), 0.0f));
						} else {
							primitive.updateAffine(new Affine(this.transformWidth, 1.0f, leftToOrigin * (this.transformWidth - 1.0f), 0.0f));
						}	
					}
				}
			}
		}
		
		super.drawSurface(context);
	}
	
	public void drawComponents(AppDrawingContext context) {
		AlphaLayeringSystem layering = this.getAppManager().getAlphaLayering();
		
		layering.push(AlphaLayer.CONTAINER, Math.abs(this.bodyAlpha));
		
		for(Component component : this.body) {
			component.draw(context);
		}
		
		layering.pop(AlphaLayer.CONTAINER);
		
		for(Component component : this.commons) {
			component.draw(context);
		}
	}
	
	private void makeHorizontalSwipe(SwipeDirection direction, Consumer<Void> completionHandler) {
		if(this.transformWidth == 1.0d) {
			this.getAppManager().getFocusManager().request(-1);
					
			float sign = direction.equals(SwipeDirection.LEFT) ? 1.0f : -1.0f;
			
			this.transformWidth *= sign;
			
			this.transformWidthAnimation.startAnimation(500, after -> {
				this.transformWidth = 1.0f;
			}, -sign);
			
			this.bodyAlphaAnimation.startAnimation(500, after -> {
				if(completionHandler != null) {
					completionHandler.accept(null);
				}
				
				this.bodyAlpha = 0xFF;
			}, -0xFF);
		}
	}

	public <T extends Event> void handleEvent(T event) {
		if(event instanceof ClickEvent) {
			
			ClickEvent click = (ClickEvent) event;
			
			if(click.isTargetting(this, "open")) {
				Object[] data = click.getData();
				
				if(data.length > 0) {
					Object element = data[0];
					
					if(element instanceof Project) {
						getAppManager().getWorkspace().loadProject((Project) element);
						navigateTo(Views.code);
					}
				}
			} else if(click.isTargetting(this, "browse")) {
				try {
					Desktop.getDesktop().browse(new URL((String) click.getData()[0]).toURI());
				} catch (Exception e) {
					System.err.println("Could not open link");
				}
			} else if(click.isTargetting(this, "new")) {
				navigateTo(Views.newProject);
			} else if(click.isTargetting(this, "connect")) {
				// TODO
			} else if(click.isTargetting(this, "import")) {
				// TODO
			} else if(click.isTargetting(this, "prev")) {
				this.makeHorizontalSwipe(SwipeDirection.RIGHT, null);
				this.getAppManager().getSystemTimer().schedule(this, 250L);
				this.pageID--;
			} else if(click.isTargetting(this, "next")) {
				this.makeHorizontalSwipe(SwipeDirection.LEFT, null);
				this.getAppManager().getSystemTimer().schedule(this, 250L);
				this.pageID++;
			}
		} else if(event instanceof TimerEvent) {
			if(((TimerEvent) event).isTargetting(this)) {
				loadWorkspace();
			}
		}
	}

	public Set<Class<? extends Event>> getHandleableEvents() {
		return Utils.asSet(ClickEvent.class, TimerEvent.class);
	}
}