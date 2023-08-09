class EnumTarg {
    static Coin myCoin = Coin.penny;
    public static void main(String[] args){
        System.out.println("Howdy!");
        System.out.println("Goodbye from EnumTarg!");
    }
}
public class EnumTest extends TestScaffold {
    ReferenceType targetClass;
    EnumTest (String args[]) {
        super(args);
    }
    public static void main(String[] args)      throws Exception {
        new EnumTest(args).startTests();
    }
    void fail(String reason) throws Exception {
        failure(reason);
    }
    protected void runTests() throws Exception {
        BreakpointEvent bpe = startToMain("EnumTarg");
        targetClass = bpe.location().declaringType();
        ReferenceType rt = findReferenceType("EnumTarg");
        Field myField = rt.fieldByName("myCoin");
        ObjectReference enumObject = (ObjectReference)rt.getValue(myField);
        ClassType enumClass =(ClassType) enumObject.referenceType();
        ClassType superClass = enumClass.superclass();
        if (!superClass.name().equals("java.lang.Enum")) {
            fail("failure: Superclass of enum class is not java.lang.Enum: " + superClass.name());
        }
        if (!enumClass.isEnum()) {
            fail("failure: isEnum() is false but should be true");
        }
        if (((ClassType)rt).isEnum()) {
            fail("failure: isEnum() is true for EnumTarg but should be false");
        }
        Field enumConstant = enumClass.fieldByName("penny");
        if (!enumConstant.isEnumConstant()) {
            fail("failure: The 'penny' field is not marked " +
                 "as an enum constant.");
        }
        List allFields = enumClass.fields();
        List enumConstantFields = new ArrayList();
        StringBuffer enumDecl = new StringBuffer("enum " + enumClass.name() + " {");
        char delim = ' ';
        for (Iterator iter = allFields.iterator(); iter.hasNext(); ) {
            Field aField = (Field)iter.next();
            if (aField.isEnumConstant()) {
                enumDecl.append(' ');
                enumDecl.append(aField.name());
                enumDecl.append(delim);
                delim = ',';
            }
        }
        enumDecl.append("; };");
        System.out.println("Enum decl is: " + enumDecl);
        listenUntilVMDisconnect();
        if (!testFailed) {
            println("EnumTest: passed");
        } else {
            throw new Exception("EnumTest: failed");
        }
    }
}
