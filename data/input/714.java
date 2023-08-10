import takatuka.classreader.dataObjs.*;
import takatuka.classreader.logic.util.*;
import takatuka.verifier.logic.exception.*;
public class AccessVerifier {
    private static final AccessVerifier accessVerifier = new AccessVerifier();
    private AccessVerifier() {
        super();
    }
    public static final AccessVerifier getInstanceOf() {
        return accessVerifier;
    }
    public void execute(ClassFile classD, ClassFile classC, FieldInfo fieldR) {
        try {
            if (!checkClassAccess(classD, classC)) {
                throw new VerifyErrorExt(Messages.INVALID_CLASS_ACCESS + ", " + classD.getFullyQualifiedClassName() + " cannot access " + classC.getFullyQualifiedClassName());
            } else if (!checkFieldMethodAccess(classD, classC, fieldR)) {
                throw new VerifyErrorExt(Messages.INVALID_CLASS_ACCESS + ", " + classD.getFullyQualifiedClassName() + " cannot access a field/method from class" + classC.getFullyQualifiedClassName());
            }
        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }
    }
    public boolean checkFieldMethodAccess(ClassFile classD, ClassFile classC, FieldInfo fieldR) throws Exception {
        if (classD.equals(classC)) {
            return true;
        }
        AccessFlags rAccess = fieldR.getAccessFlags();
        if (rAccess.isPublic() || (rAccess.isProtected() && Oracle.getInstanceOf().isSubClass(classD, classC)) || ((rAccess.isProtected() || rAccess.isPackagePrivate()) && samePackage(classD, classC))) {
            return true;
        }
        return false;
    }
    private boolean samePackage(ClassFile classD, ClassFile classC) throws Exception {
        String classDPackage = Oracle.getInstanceOf().getPackage(classD);
        String classCPackage = Oracle.getInstanceOf().getPackage(classC);
        return classCPackage.equals(classDPackage);
    }
    public boolean checkClassAccess(ClassFile classD, ClassFile classC) throws Exception {
        if (classC.getAccessFlags().isPublic() || samePackage(classD, classC)) {
            return true;
        }
        return false;
    }
}
