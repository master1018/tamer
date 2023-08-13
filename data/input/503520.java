import proguard.classfile.*;
import proguard.classfile.util.SimplifiedVisitor;
import proguard.classfile.visitor.*;
public class HorizontalClassMerger
extends      SimplifiedVisitor
implements   ClassVisitor
{
    private final boolean      allowAccessModification;
    private final boolean      mergeInterfacesAggressively;
    private final ClassVisitor extraClassVisitor;
    public HorizontalClassMerger(boolean allowAccessModification,
                                 boolean mergeInterfacesAggressively)
    {
        this(allowAccessModification, mergeInterfacesAggressively, null);
    }
    public HorizontalClassMerger(boolean      allowAccessModification,
                                 boolean      mergeInterfacesAggressively,
                                 ClassVisitor extraClassVisitor)
    {
        this.allowAccessModification     = allowAccessModification;
        this.mergeInterfacesAggressively = mergeInterfacesAggressively;
        this.extraClassVisitor           = extraClassVisitor;
    }
    public void visitProgramClass(ProgramClass programClass)
    {
        programClass.superClassConstantAccept(new ReferencedClassVisitor(
                                              new SubclassTraveler(
                                              new ProgramClassFilter(
                                              new ClassMerger(programClass,
                                                              allowAccessModification,
                                                              mergeInterfacesAggressively,
                                                              extraClassVisitor)))));
    }
}