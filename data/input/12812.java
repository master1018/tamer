abstract class AbstractTestVars {
    abstract float test1(String blah, int i);
    native int test2(double k, boolean b);
    String test3(short sh, long lo) {
        String st = "roses";
        return st;
    }
}
class TestVars extends AbstractTestVars {
    float test1(String blah, int i) {
        return (float)1.1;
    }
    void hi() {
        return;
    }
    public static void main(String[] args) throws Exception {
        new TestVars().hi();
        return;
    }
}
public class Vars extends JDIScaffold {
    final String[] args;
    boolean failed = false;
    Vars(String args[]) {
        super();
        this.args = args;
    }
    public static void main(String[] args) throws Exception {
        new Vars(args).runTests();
    }
    static final int VARIABLES = 1;
    static final int BYNAME = 2;
    static final int ARGUMENTS = 3;
    String testCase(Method method, int which) {
        try {
            List vars;
            switch (which) {
                case VARIABLES:
                    vars = method.variables();
                    break;
                case BYNAME:
                    vars = method.variablesByName("st");
                    break;
                case ARGUMENTS:
                    vars = method.arguments();
                    break;
                default:
                    throw new InternalException("should not happen");
            }
            StringBuffer sb = new StringBuffer();
            for (Iterator it = vars.iterator(); it.hasNext(); ) {
                LocalVariable lv = (LocalVariable)it.next();
                if (sb.length() > 0) {
                    sb.append(",");
                }
                sb.append(lv.name());
            }
            return sb.toString();
        } catch (Exception exc) {
            String st = exc.getClass().getName();
            int inx = st.lastIndexOf('.');
            return st.substring(inx+1);
        }
    }
    void test(Method method, int which, String name, String expected) {
        String got = testCase(method, which);
        if (got.equals(expected)) {
            System.out.println(name + ": got expected: " + got);
        } else {
            failed = true;
            System.out.println(name + ": ERROR expected: " + expected);
            System.out.println("      got: " + got);
        }
    }
    void test2(Method method, int which, String name, String expected, String expected2) {
        String got = testCase(method, which);
        if (got.equals(expected) || got.equals(expected2)) {
            System.out.println(name + ": got expected: " + got);
        } else {
            failed = true;
            System.out.println(name + ": ERROR expected: " + expected);
            System.out.println("      got: " + got);
        }
    }
    protected void runTests() throws Exception {
        List argList = new ArrayList(Arrays.asList(args));
        argList.add("TestVars");
        System.out.println("run args: " + argList);
        connect((String[])argList.toArray(args));
        waitForVMStart();
        BreakpointEvent bp = resumeTo("TestVars", "hi", "()V");
        ReferenceType rt = findReferenceType("AbstractTestVars");
        if (rt == null) {
            throw new Exception("AbstractTestVars: not loaded");
        }
        Method method = findMethod(rt, "test1", "(Ljava/lang/String;I)F");
        if (method == null) {
            throw new Exception("Method not found");
        }
        test(method, VARIABLES, "abstract/variables",
             "AbsentInformationException");
        test(method, BYNAME, "abstract/variablesByName",
             "AbsentInformationException");
        test(method, ARGUMENTS, "abstract/arguments",
             "AbsentInformationException");
        method = findMethod(rt, "test2", "(DZ)I");
        if (method == null) {
            throw new Exception("Method not found");
        }
        test(method, VARIABLES, "native/variables",
             "AbsentInformationException");
        test(method, BYNAME, "native/variablesByName",
             "AbsentInformationException");
        test(method, ARGUMENTS, "native/arguments",
             "AbsentInformationException");
        method = findMethod(rt, "test3", "(SJ)Ljava/lang/String;");
        if (method == null) {
            throw new Exception("Method not found");
        }
        test2(method, VARIABLES, "normal/variables", "sh,lo,st", "st,sh,lo");
        test(method, BYNAME, "normal/variablesByName", "st");
        test(method, ARGUMENTS, "normal/arguments", "sh,lo");
        resumeToVMDeath();
        if (failed) {
            throw new Exception("Vars: failed");
        } else {
            System.out.println("Vars: passed");
        }
    }
}
