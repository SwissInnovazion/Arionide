package ch.innovazion.arionide.lang.avr.special;

import java.util.List;

import ch.innovazion.arionide.lang.ApplicationMemory;
import ch.innovazion.arionide.lang.Environment;
import ch.innovazion.arionide.lang.EvaluationException;
import ch.innovazion.arionide.lang.SpecialInstruction;
import ch.innovazion.arionide.lang.symbols.Specification;
import ch.innovazion.arionide.project.StructureModel;
import ch.innovazion.arionide.project.StructureModelFactory;
import ch.innovazion.arionide.ui.ApplicationTints;

public class Spacer extends SpecialInstruction {

	public void validate(Specification spec, List<String> validationErrors) {

	}

	public void evaluate(Environment env, Specification spec, ApplicationMemory programMemory) throws EvaluationException {
		env.getProgramCounter().incrementAndGet();
	}

	public StructureModel createStructureModel() {
		return StructureModelFactory
			.draft("space")
			.withColor(ApplicationTints.getColorIDByName("Black"))
			.withComment("Some space for your beautiful code")
			.beginSignature("default")
			.endSignature()
			.build();
	}
}
