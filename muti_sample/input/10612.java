public class MethodBreakpointSpec extends BreakpointSpec {
    String methodId;
    List<String> methodArgs;
    MethodBreakpointSpec(EventRequestSpecList specs,
                         ReferenceTypeSpec refSpec,
                         String methodId, List<String> methodArgs) {
        super(specs, refSpec);
        this.methodId = methodId;
        this.methodArgs = methodArgs;
    }
    @Override
    void resolve(ReferenceType refType) throws MalformedMemberNameException,
                                             AmbiguousMethodException,
                                             InvalidTypeException,
                                             NoSuchMethodException,
                                             NoSessionException {
        if (!isValidMethodName(methodId)) {
            throw new MalformedMemberNameException(methodId);
        }
        if (!(refType instanceof ClassType)) {
            throw new InvalidTypeException();
        }
        Location location = location((ClassType)refType);
        setRequest(refType.virtualMachine().eventRequestManager()
                   .createBreakpointRequest(location));
    }
    private Location location(ClassType clazz) throws
                                               AmbiguousMethodException,
                                               NoSuchMethodException,
                                               NoSessionException {
        Method method = findMatchingMethod(clazz);
        Location location = method.location();
        return location;
    }
    public String methodName() {
        return methodId;
    }
    public List<String> methodArgs() {
        return methodArgs;
    }
    @Override
    public int hashCode() {
        return refSpec.hashCode() +
            ((methodId != null) ? methodId.hashCode() : 0) +
            ((methodArgs != null) ? methodArgs.hashCode() : 0);
    }
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MethodBreakpointSpec) {
            MethodBreakpointSpec breakpoint = (MethodBreakpointSpec)obj;
            return methodId.equals(breakpoint.methodId) &&
                   methodArgs.equals(breakpoint.methodArgs) &&
                   refSpec.equals(breakpoint.refSpec);
        } else {
            return false;
        }
    }
    @Override
    public String errorMessageFor(Exception e) {
        if (e instanceof AmbiguousMethodException) {
            return ("Method " + methodName() + " is overloaded; specify arguments");
        } else if (e instanceof NoSuchMethodException) {
            return ("No method " + methodName() + " in " + refSpec);
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
        buffer.append('.');
        buffer.append(methodId);
        if (methodArgs != null) {
            boolean first = true;
            buffer.append('(');
            for (String name : methodArgs) {
                if (!first) {
                    buffer.append(',');
                }
                buffer.append(name);
                first = false;
            }
            buffer.append(")");
        }
        buffer.append(" (");
        buffer.append(getStatusString());
        buffer.append(')');
        return buffer.toString();
    }
    private boolean isValidMethodName(String s) {
        return isJavaIdentifier(s) ||
               s.equals("<init>") ||
               s.equals("<clinit>");
    }
    private boolean compareArgTypes(Method method, List<String> nameList) {
        List<String> argTypeNames = method.argumentTypeNames();
        if (argTypeNames.size() != nameList.size()) {
            return false;
        }
        int nTypes = argTypeNames.size();
        for (int i = 0; i < nTypes; ++i) {
            String comp1 = argTypeNames.get(i);
            String comp2 = nameList.get(i);
            if (! comp1.equals(comp2)) {
                if (i != nTypes - 1 ||
                    !method.isVarArgs()  ||
                    !comp2.endsWith("...")) {
                    return false;
                }
                int comp1Length = comp1.length();
                if (comp1Length + 1 != comp2.length()) {
                    return false;
                }
                if (!comp1.regionMatches(0, comp2, 0, comp1Length - 2)) {
                    return false;
                }
                return true;
            }
        }
        return true;
    }
  private VirtualMachine vm() {
    return request.virtualMachine();
  }
    private String normalizeArgTypeName(String name) throws NoSessionException {
        int i = 0;
        StringBuffer typePart = new StringBuffer();
        StringBuffer arrayPart = new StringBuffer();
        name = name.trim();
        int nameLength = name.length();
        boolean isVarArgs = name.endsWith("...");
        if (isVarArgs) {
            nameLength -= 3;
        }
        while (i < nameLength) {
            char c = name.charAt(i);
            if (Character.isWhitespace(c) || c == '[') {
                break;      
            }
            typePart.append(c);
            i++;
        }
        while (i < nameLength) {
            char c = name.charAt(i);
            if ( (c == '[') || (c == ']')) {
                arrayPart.append(c);
            } else if (!Character.isWhitespace(c)) {
                throw new IllegalArgumentException(
                                                "Invalid argument type name");
            }
            i++;
        }
        name = typePart.toString();
        if ((name.indexOf('.') == -1) || name.startsWith("*.")) {
            try {
                List<?> refs = specs.runtime.findClassesMatchingPattern(name);
                if (refs.size() > 0) {  
                    name = ((ReferenceType)(refs.get(0))).name();
                }
            } catch (IllegalArgumentException e) {
            }
        }
        name += arrayPart.toString();
        if (isVarArgs) {
            name += "...";
        }
        return name;
    }
    private Method findMatchingMethod(ClassType clazz)
                                        throws AmbiguousMethodException,
                                               NoSuchMethodException,
                                               NoSessionException  {
        List<String> argTypeNames = null;
        if (methodArgs() != null) {
            argTypeNames = new ArrayList<String>(methodArgs().size());
            for (String name : methodArgs()) {
                name = normalizeArgTypeName(name);
                argTypeNames.add(name);
            }
        }
        Method firstMatch = null;  
        Method exactMatch = null;  
        int matchCount = 0;        
        for (Method candidate : clazz.methods()) {
            if (candidate.name().equals(methodName())) {
                matchCount++;
                if (matchCount == 1) {
                    firstMatch = candidate;
                }
                if ((argTypeNames != null)
                        && compareArgTypes(candidate, argTypeNames) == true) {
                    exactMatch = candidate;
                    break;
                }
            }
        }
        Method method = null;
        if (exactMatch != null) {
            method = exactMatch;
        } else if ((argTypeNames == null) && (matchCount > 0)) {
            if (matchCount == 1) {
                method = firstMatch;       
            } else {
                throw new AmbiguousMethodException();
            }
        } else {
            throw new NoSuchMethodException(methodName());
        }
        return method;
    }
}
