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
package ch.innovazion.arionide.lang.avr.arithmetic;

import java.util.List;

import ch.innovazion.arionide.lang.Environment;
import ch.innovazion.arionide.lang.EvaluationException;
import ch.innovazion.arionide.lang.Instruction;
import ch.innovazion.arionide.lang.Skeleton;
import ch.innovazion.arionide.lang.avr.AVREnums;
import ch.innovazion.arionide.lang.avr.device.AVRSRAM;
import ch.innovazion.arionide.lang.symbols.Bit;
import ch.innovazion.arionide.lang.symbols.Enumeration;
import ch.innovazion.arionide.lang.symbols.Node;
import ch.innovazion.arionide.lang.symbols.Numeric;
import ch.innovazion.arionide.lang.symbols.Parameter;
import ch.innovazion.arionide.lang.symbols.Specification;
import ch.innovazion.arionide.project.StructureModel;
import ch.innovazion.arionide.project.StructureModelFactory;

public class SubtractionImmediate extends Instruction {
	
	public void validate(Specification spec, List<String> validationErrors) {
		;
	}

	public void evaluate(Environment env, Specification spec, Skeleton skeleton) throws EvaluationException {		
		Numeric d = (Numeric) ((Enumeration) getConstant(spec, 0)).getValue();
		Numeric k = (Numeric) getConstant(spec, 1);

		AVRSRAM sram = env.getPeripheral("sram");
		
		int dPtr = (int) Bit.toInteger(d.getRawStream());

		int sreg = sram.get(AVRSRAM.SREG) & 0b11000000;
		int dValue = sram.getRegister(dPtr);
		int kValue = (int) Bit.toInteger(k.getRawStream());
		int value = (dValue - kValue) & 0xFF;
		
		sram.set(dPtr, value);
				
		int h = ~(dValue >> 3) & (kValue >> 3) | (kValue >> 3) & (value >> 3) | (value >> 3) & ~(dValue >> 3);
		int v = (dValue >> 7) & ~(kValue >> 7) & ~(kValue >> 7) | ~(dValue >> 7) & (kValue >> 7) & (value >> 7);
		int n = value >> 7;
		int s = n ^ v;
		int z = value == 0 ? 1 : 0;
		int c = ~(dValue >> 7) & (kValue >> 7) | (kValue >> 7) & (value >> 7) | (value >> 7) & ~(dValue >> 7);
		
		int mask = ((h & 1) << 5) | ((s & 1) << 4) | ((v & 1) << 3) | ((n & 1) << 2) | ((z & 1) << 1) | (c & 1);
		
		sram.set(AVRSRAM.SREG, sreg | mask);
		
		env.getProgramCounter().incrementAndGet();
		env.getClock().incrementAndGet();
	}

	public Node assemble(Specification spec, Skeleton skeleton, List<String> assemblyErrors) {
		return new Numeric(0).cast(16);
	}

	public StructureModel createStructureModel() {
		return StructureModelFactory
			.draft("subi")
			.withColor(0.13f)
			.withComment("Subtract an immediate value from a register without the carry flag")
			.beginSignature("default")
			.withParameter(new Parameter("Destination").asConstant(AVREnums.HIGH_REGISTER))
			.withParameter(new Parameter("Subtrahend").asConstant(new Numeric(0)))
			.endSignature()
			.build();
	}

	public int getLength() {
		return 2;
	}
}