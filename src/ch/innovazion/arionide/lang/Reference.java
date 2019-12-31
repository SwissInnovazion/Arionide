/*******************************************************************************
 * This file is part of Arionide.
 *
 * Arionide is an IDE used to conceive applications and algorithms in a three-dimensional environment. 
 * It is the work of Arion Zimmermann for his final high-school project at Calvin College (Geneva, Switzerland).
 * Copyright (C) 2016-2019 Innovazion. All rights reserved.
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
package ch.innovazion.arionide.lang;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Reference extends SpecificationElement {

	private static final long serialVersionUID = -3387831385820354646L;

	private List<SpecificationElement> neededParameters;
	private List<SpecificationElement> specificationParameters;

	public Reference(String name, String value, List<SpecificationElement> neededParameters, List<SpecificationElement> specificationParameters) {
		super(name, value);
		this.neededParameters = neededParameters;
		this.specificationParameters = specificationParameters;
	}
	
	public List<SpecificationElement> getEagerParameters() {
		return this.neededParameters;
	}
	
	public List<SpecificationElement> getLazyParameters() {
		return this.specificationParameters;
	}
	
	public void setSpecificationParameters(List<SpecificationElement> specificationParameters) {
		this.specificationParameters = specificationParameters;
	}
	
	public String toString() {
		return super.toString() + " <" + String.join("; ", Stream.concat(this.neededParameters.stream(), this.specificationParameters.stream())
				.map(SpecificationElement::toString).toArray(String[]::new)) + ">";
	}
	
	public boolean equals(java.lang.Object other) {
		if(other instanceof Reference) {
			Reference casted = (Reference) other;
			return super.equals(other) && this.neededParameters.equals(casted.neededParameters);
		} else {
			return false;
		}
	}
	
	public Reference clone() {
		return new Reference(this.getName(), this.getRawValue(), 
				this.neededParameters.stream().map(SpecificationElement::clone).collect(Collectors.toList()),
				this.specificationParameters.stream().map(SpecificationElement::clone).collect(Collectors.toList()));
	}
}