package iec61970.generation.production.validation;

import iec61970.generation.production.Reservoir;

/**
 * A sample validator interface for {@link iec61970.generation.production.TargetLevelSchedule}.
 * This doesn't really do anything, and it's not a real EMF artifact.
 * It was generated by the org.eclipse.emf.examples.generator.validator plug-in to illustrate how EMF's code generator can be extended.
 * This can be disabled with -vmargs -Dorg.eclipse.emf.examples.generator.validator=false.
 */
public interface TargetLevelScheduleValidator {

    boolean validate();

    boolean validateHighLevelLimit(String value);

    boolean validateLowLevelLimit(String value);

    boolean validateReservoir(Reservoir value);
}