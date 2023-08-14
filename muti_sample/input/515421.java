import proguard.classfile.Clazz;
import proguard.classfile.attribute.CodeAttribute;
public interface BranchUnit
{
    public void branch(Clazz         clazz,
                       CodeAttribute codeAttribute,
                       int           offset,
                       int           branchTarget);
    public void branchConditionally(Clazz         clazz,
                                    CodeAttribute codeAttribute,
                                    int           offset,
                                    int           branchTarget,
                                    int           conditional);
    public void returnFromMethod();
    public void throwException();
}
