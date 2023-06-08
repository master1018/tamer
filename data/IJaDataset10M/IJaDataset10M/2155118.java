package iec61970.wires.validation;

import iec61970.wires.VoltageControlZone;

/**
 * A sample validator interface for {@link iec61970.wires.BusbarSection}.
 * This doesn't really do anything, and it's not a real EMF artifact.
 * It was generated by the org.eclipse.emf.examples.generator.validator plug-in to illustrate how EMF's code generator can be extended.
 * This can be disabled with -vmargs -Dorg.eclipse.emf.examples.generator.validator=false.
 */
public interface BusbarSectionValidator {

    boolean validate();

    boolean validateVoltageControlZone(VoltageControlZone value);
}