public class LineBreakpointSpec extends BreakpointSpec {
    int lineNumber;
    LineBreakpointSpec(EventRequestSpecList specs,
                       ReferenceTypeSpec refSpec, int lineNumber) {
        super(specs, refSpec);
        this.lineNumber = lineNumber;
    }
    @Override
    void resolve(ReferenceType refType) throws InvalidTypeException,
                                             LineNotFoundException {
        if (!(refType instanceof ClassType)) {
            throw new InvalidTypeException();
        }
        Location location = location((ClassType)refType);
        setRequest(refType.virtualMachine().eventRequestManager()
                   .createBreakpointRequest(location));
    }
    private Location location(ClassType clazz) throws
                                            LineNotFoundException {
        Location location = null;
        try {
            List<Location> locs = clazz.locationsOfLine(lineNumber());
            if (locs.size() == 0) {
                throw new LineNotFoundException();
            }
            location = locs.get(0);
            if (location.method() == null) {
                throw new LineNotFoundException();
            }
        } catch (AbsentInformationException e) {
            throw new LineNotFoundException();
        }
        return location;
    }
    public int lineNumber() {
        return lineNumber;
    }
    @Override
    public int hashCode() {
        return refSpec.hashCode() + lineNumber;
    }
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof LineBreakpointSpec) {
            LineBreakpointSpec breakpoint = (LineBreakpointSpec)obj;
            return refSpec.equals(breakpoint.refSpec) &&
                   (lineNumber == breakpoint.lineNumber);
        } else {
            return false;
        }
    }
    @Override
    public String errorMessageFor(Exception e) {
        if (e instanceof LineNotFoundException) {
            return ("No code at line " + lineNumber() + " in " + refSpec);
        } else if (e instanceof InvalidTypeException) {
            return ("Breakpoints can be located only in classes. " +
                        refSpec + " is an interface or array");
        } else {
            return super.errorMessageFor( e);
        }
    }
    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer("breakpoint ");
        buffer.append(refSpec.toString());
        buffer.append(':');
        buffer.append(lineNumber);
        buffer.append(" (");
        buffer.append(getStatusString());
        buffer.append(')');
        return buffer.toString();
    }
}
