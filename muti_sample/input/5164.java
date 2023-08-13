abstract class AbstractLocationTarg {
    abstract void foo();
}
class LocationTarg extends AbstractLocationTarg {
    public static void main(String[] args){
        System.out.println("Howdy!");  
    }
    void foo() {
        System.out.println("Never here!");  
    }
}
public class LocationTest extends TestScaffold {
    ReferenceType targetClass;
    ThreadReference mainThread;
    LocationTest (String args[]) {
        super(args);
    }
    public static void main(String[] args)      throws Exception {
        new LocationTest(args).startTests();
    }
    Location getLocation(String refName, String methodName) {
        List refs = vm().classesByName(refName);
        if (refs.size() != 1) {
            failure("Test failure: " + refs.size() +
                    " ReferenceTypes named: " + refName);
            return null;
        }
        ReferenceType refType = (ReferenceType)refs.get(0);
        List meths = refType.methodsByName(methodName);
        if (meths.size() != 1) {
            failure("Test failure: " + meths.size() +
                    " methods named: " + methodName);
            return null;
        }
        Method meth = (Method)meths.get(0);
        return meth.location();
    }
    protected void runTests() throws Exception {
        Location loc;
        startToMain("LocationTarg");
        loc = getLocation("AbstractLocationTarg", "foo");
        if (loc != null) {
            failure("location of AbstractLocationTarg.foo() should have " +
                    "been null, but was: " + loc);
        }
        loc = getLocation("java.util.List", "clear");
        if (loc != null) {
            failure("location of java.util.List.clear() " +
                    "should have been null, but was: " + loc);
        }
        loc = getLocation("java.lang.Object", "getClass");
        if (loc == null) {
            failure("location of Object.getClass() " +
                    "should have been non-null, but was: " + loc);
        } else {
            if (!loc.declaringType().name().equals("java.lang.Object")) {
                failure("location.declaringType() of Object.getClass() " +
                        "should have been java.lang.Object, but was: " +
                        loc.declaringType());
            }
            if (!loc.method().name().equals("getClass")) {
                failure("location.method() of Object.getClass() " +
                        "should have been getClass, but was: " +
                        loc.method());
            }
            if (loc.codeIndex() != -1) {
                failure("location.codeIndex() of Object.getClass() " +
                        "should have been -1, but was: " +
                        loc.codeIndex());
            }
            if (loc.lineNumber() != -1) {
                failure("location.lineNumber() of Object.getClass() " +
                        "should have been -1, but was: " +
                        loc.lineNumber());
            }
        }
        Location mainLoc = getLocation("LocationTarg", "main");
        loc = getLocation("LocationTarg", "foo");
        if (loc == null) {
            failure("location of LocationTarg.foo() " +
                    "should have been non-null, but was: " + loc);
        } else {
            if (!loc.declaringType().name().equals("LocationTarg")) {
                failure("location.declaringType() of LocationTarg.foo() " +
                        "should have been LocationTarg, but was: " +
                        loc.declaringType());
            }
            if (!loc.method().name().equals("foo")) {
                failure("location.method() of LocationTarg.foo() " +
                        "should have been foo, but was: " +
                        loc.method());
            }
            if (loc.codeIndex() != 0) {  
                failure("location.codeIndex() of LocationTarg.foo() " +
                        "should have been 0, but was: " +
                        loc.codeIndex());
            }
            if (loc.lineNumber() != (mainLoc.lineNumber() + 3)) {
                failure("location.lineNumber() of LocationTarg.foo() " +
                        "should have been " + (mainLoc.lineNumber() + 3) +
                        ", but was: " + loc.lineNumber());
            }
        }
        listenUntilVMDisconnect();
        if (!testFailed) {
            println("LocationTest: passed");
        } else {
            throw new Exception("LocationTest: failed");
        }
    }
}
