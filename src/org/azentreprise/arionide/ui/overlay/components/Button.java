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
package org.azentreprise.arionide.ui.overlay.components;

import java.awt.Cursor;
import java.util.Arrays;
import java.util.List;

import org.azentreprise.arionide.events.ActionEvent;
import org.azentreprise.arionide.events.ActionType;
import org.azentreprise.arionide.events.ClickEvent;
import org.azentreprise.arionide.events.Event;
import org.azentreprise.arionide.events.EventHandler;
import org.azentreprise.arionide.events.FocusEvent;
import org.azentreprise.arionide.events.FocusGainedEvent;
import org.azentreprise.arionide.events.FocusLostEvent;
import org.azentreprise.arionide.events.MoveEvent;
import org.azentreprise.arionide.events.ValidateEvent;
import org.azentreprise.arionide.ui.AppDrawingContext;
import org.azentreprise.arionide.ui.animations.Animation;
import org.azentreprise.arionide.ui.animations.FieldModifierAnimation;
import org.azentreprise.arionide.ui.overlay.View;

public class Button extends Label implements EventHandler {
	
	public static final int DEFAULT_ALPHA = 0x60;
	
	private static final int ANIMATION_TIME = 200;
	private static final Cursor DEFAULT_CURSOR = Cursor.getDefaultCursor();
	
	protected final Animation animation;
	protected boolean hasFocus;
	private boolean disabled = false;
	
	private boolean mouseOver = false;
	private Cursor overCursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
	
	private int colorKeepRef = 0x42CAFE;
	
	private ClickEvent event;
	
	public Button(View parent, String label) {
		super(parent, label);
		
		this.setColor(this.colorKeepRef);
		this.setAlpha(DEFAULT_ALPHA);
		
		this.animation = new FieldModifierAnimation(this.getAppManager(), "alpha", Label.class, this);
		
		this.getAppManager().getEventDispatcher().registerHandler(this);
	}
	
	public Button setSignal(String signal, Object... data) {
		this.event = new ClickEvent(this, signal, data);
		return this;
	}
	
	public Button setDisabled(boolean disabled) {
		this.disabled = disabled;
		
		if(this.disabled) {
			this.setColor(0xFF0000);
			this.setAlpha(DEFAULT_ALPHA);
			
			if(this.hasFocus) {
				this.getAppManager().getFocusManager().next();
			}
		} else {
			this.setColor(this.colorKeepRef);
		}
				
		return this;
	}

	protected void setOverCursor(Cursor cursor) {
		this.overCursor = cursor;
	}
		
	public void drawComponent(AppDrawingContext context) {
		super.drawComponent(context);
		context.getPrimitives().drawRoundRect(context, this.getBounds());
	}
	
	public boolean isFocusable() {
		return !this.disabled && !this.isHidden();
	}

	public <T extends Event> void handleEvent(T event) {
		if(this.disabled || this.isHidden() || this.getBounds() == null) {
			return;
		}
		
		if(event instanceof MoveEvent) {
			MoveEvent casted = (MoveEvent) event;
			
			if(this.getBounds().contains(casted.getPoint())) {
				if(!this.mouseOver) {
					this.mouseOver = true;
					
					this.getAppManager().getDrawingContext().setCursor(this.overCursor);

					if(!this.hasFocus) {
						this.animation.startAnimation(ANIMATION_TIME, 0xFF);
					}
				}
			} else {
				if(this.mouseOver) {
					this.mouseOver = false;
					
					this.getAppManager().getDrawingContext().setCursor(DEFAULT_CURSOR);

					if(!this.hasFocus) {
						this.animation.startAnimation(ANIMATION_TIME, DEFAULT_ALPHA);
					}
				}
			}
		} else if(event instanceof ActionEvent) {
			ActionEvent casted = (ActionEvent) event;
			
			if(this.getBounds().contains(casted.getPoint())) {
				if(casted.getType().equals(ActionType.PRESS)) {
					this.fireMouseClick();
				}
			}
		} else if(event instanceof ValidateEvent) {
			if(this.hasFocus) {
				this.fireMouseClick();
			}
		} else if(event instanceof FocusEvent) {
			if(((FocusEvent) event).isTargetting(this)) {
				if(event instanceof FocusGainedEvent) {
					this.onFocusGained();
				} else if(event instanceof FocusLostEvent) {
					this.onFocusLost();
				}
			}
		}
	}
	
	protected void fireMouseClick() {
		if(this.event != null) {
			this.getAppManager().getEventDispatcher().fire(this.event);
		}
	}
	
	protected void onFocusGained() {
		this.hasFocus = true;
		this.animation.startAnimation(ANIMATION_TIME, 0xFF);
	}
	
	protected void onFocusLost() {
		this.hasFocus = false;
		this.animation.startAnimation(ANIMATION_TIME, DEFAULT_ALPHA);
	}
	
	public void hide() {
		super.hide();
		this.getAppManager().getDrawingContext().setCursor(DEFAULT_CURSOR);
		this.onFocusLost();
	}

	public List<Class<? extends Event>> getHandleableEvents() {
		return Arrays.asList(MoveEvent.class, ActionEvent.class, FocusGainedEvent.class, FocusLostEvent.class, ValidateEvent.class);
	}
}