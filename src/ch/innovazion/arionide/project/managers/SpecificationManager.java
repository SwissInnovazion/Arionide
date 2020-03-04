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
package ch.innovazion.arionide.project.managers;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import ch.innovazion.arionide.events.MessageEvent;
import ch.innovazion.arionide.lang.Data;
import ch.innovazion.arionide.lang.symbols.Reference;
import ch.innovazion.arionide.lang.symbols.Specification;
import ch.innovazion.arionide.lang.symbols.Parameter;
import ch.innovazion.arionide.project.Manager;
import ch.innovazion.arionide.project.Storage;
import ch.innovazion.arionide.project.StructureMeta;

public class SpecificationManager extends Manager {
		
	protected SpecificationManager(Storage storage) {
		super(storage);
	}
	
	public MessageEvent addElement(Specification spec, Parameter element) {
		doForeachConnectedSpecification(spec, other -> other.getParameters().add(element));
		saveMeta();
		
		return success();
	}
	
	public MessageEvent deleteElement(Specification spec, int id) {
		doForeachConnectedSpecification(spec, l -> l.getParameters().remove(id));
		saveMeta();

		return success();
	}
	
	public MessageEvent refactorName(Specification spec, int id, String newName) {
		doForeachConnectedSpecification(spec, l -> l.getParameters().get(id).setName(newName));
		saveMeta();
		
		return success();
	}

	public MessageEvent refactorType(Specification spec, int id, int newType) {
		doForeachConnectedSpecification(spec, l -> ((Data) l.getParameters().get(id)).setType(newType));
		saveMeta();
		
		return success();
	}
	
	public MessageEvent refactorParameterName(Specification spec, int id, int data, String newName) {
		doForeachConnectedSpecification(spec, l -> ((Reference) l.getParameters().get(id)).getEagerParameters().get(data).setName(newName));
		saveMeta();
		
		return success();
	}
	
	public MessageEvent refactorParameterType(Specification spec, int id, int data, int newType) {
		doForeachConnectedSpecification(spec, l -> ((Data) ((Reference) l.getParameters().get(id)).getEagerParameters().get(data)).setType(newType));
		saveMeta();
		
		return success();
	}
	
	public MessageEvent bind(Parameter sourceParam, Parameter targetParam) {
		sourceParam.setValue(targetParam.getDisplayValue());
		saveMeta();
									
		return success();
	}
	
	public MessageEvent toggleCallability(Reference reference) {
		if(reference.getLazyParameters() != null) {
			reference.setCustomParameters(null);
		} else {
			reference.setCustomParameters(new ArrayList<>());
		}
		
		saveMeta();
		
		return success();
	}
	
	public MessageEvent remove(Specification spec, Parameter element) {
		if(spec.getParameters().remove(element)) {
			saveMeta();
			return success();
		} else {
			return warn();
		}
	}
	
	public MessageEvent addParam(List<Parameter> parameters, Parameter newParam) {
		parameters.add(newParam);
		saveMeta();
		
		return success();
	}
	
	public MessageEvent setValue(Parameter element, String value) {
		element.setValue(value);
		saveMeta();
		
		return success();
	}
	
	public MessageEvent bindParameter(Parameter source, Parameter target) {
		target.setValue(Parameter.VAR + source.getName());
		source.setValue(null);
		saveMeta();
		
		return success();
	}
	
	private void doForeachConnectedSpecification(Specification spec, Consumer<Specification> action) {
		getMeta().values().stream()
			.map(StructureMeta::getSpecification)
			.filter(spec::hasSameOrigin)
			.forEach(action);
	}
}
