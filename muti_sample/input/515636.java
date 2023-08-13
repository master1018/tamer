import proguard.classfile.ProgramClass;
import proguard.classfile.constant.visitor.ConstantVisitor;
import proguard.classfile.util.SimplifiedVisitor;
import proguard.classfile.visitor.ClassVisitor;
public class VerticalClassMerger
extends      SimplifiedVisitor
implements   ClassVisitor
{
    private final boolean      allowAccessModification;
    private final boolean      mergeInterfacesAggressively;
    private final ClassVisitor extraClassVisitor;
    public VerticalClassMerger(boolean allowAccessModification,
                               boolean mergeInterfacesAggressively)
    {
        this(allowAccessModification, mergeInterfacesAggressively, null);
    }
    public VerticalClassMerger(boolean      allowAccessModification,
                               boolean      mergeInterfacesAggressively,
                               ClassVisitor extraClassVisitor)
    {
        this.allowAccessModification     = allowAccessModification;
        this.mergeInterfacesAggressively = mergeInterfacesAggressively;
        this.extraClassVisitor           = extraClassVisitor;
    }
    public void visitProgramClass(ProgramClass programClass)
    {
        programClass.subclassesAccept(new ClassMerger(programClass,
                                                      allowAccessModification,
                                                      mergeInterfacesAggressively,
                                                      extraClassVisitor));
    }
}