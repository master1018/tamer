import proguard.classfile.*;
import proguard.classfile.util.*;
import proguard.classfile.visitor.*;
import proguard.optimize.KeepMarker;
import java.util.List;
public class DescriptorKeepChecker
extends      SimplifiedVisitor
implements   MemberVisitor,
             ClassVisitor
{
    private final ClassPool      programClassPool;
    private final ClassPool      libraryClassPool;
    private final WarningPrinter notePrinter;
    private Clazz   referencingClass;
    private Member  referencingMember;
    private boolean isField;
    public DescriptorKeepChecker(ClassPool      programClassPool,
                                 ClassPool      libraryClassPool,
                                 WarningPrinter notePrinter)
    {
        this.programClassPool = programClassPool;
        this.libraryClassPool = libraryClassPool;
        this.notePrinter      = notePrinter;
    }
    public void checkClassSpecifications(List keepSpecifications)
    {
        programClassPool.classesAccept(new ClassCleaner());
        libraryClassPool.classesAccept(new ClassCleaner());
        KeepMarker keepMarker = new KeepMarker();
        ClassPoolVisitor classPoolvisitor =
            ClassSpecificationVisitorFactory.createClassPoolVisitor(keepSpecifications,
                                                                    keepMarker,
                                                                    keepMarker,
                                                                    false,
                                                                    true,
                                                                    true);
        programClassPool.accept(classPoolvisitor);
        libraryClassPool.accept(classPoolvisitor);
        programClassPool.classesAccept(new AllMemberVisitor(this));
    }
    public void visitProgramField(ProgramClass programClass, ProgramField programField)
    {
        if (KeepMarker.isKept(programField))
        {
            referencingClass  = programClass;
            referencingMember = programField;
            isField           = true;
        }
    }
    public void visitProgramMethod(ProgramClass programClass, ProgramMethod programMethod)
    {
        if (KeepMarker.isKept(programMethod))
        {
            referencingClass  = programClass;
            referencingMember = programMethod;
            isField           = false;
            Clazz[] referencedClasses = programMethod.referencedClasses;
            if (referencedClasses != null)
            {
                for (int index = 0; index < referencedClasses.length-1; index++)
                {
                    if (referencedClasses[index] != null)
                    {
                        referencedClasses[index].accept(this);
                    }
                }
            }
        }
    }
    public void visitProgramClass(ProgramClass programClass)
    {
        if (!KeepMarker.isKept(programClass))
        {
            notePrinter.print(referencingClass.getName(),
                              programClass.getName(),
                              "Note: the configuration keeps the entry point '" +
                              ClassUtil.externalClassName(referencingClass.getName()) +
                              " { " +
                              (isField ?
                                   ClassUtil.externalFullFieldDescription(0,
                                                                          referencingMember.getName(referencingClass),
                                                                          referencingMember.getDescriptor(referencingClass)) :
                                   ClassUtil.externalFullMethodDescription(referencingClass.getName(),
                                                                           0,
                                                                           referencingMember.getName(referencingClass),
                                                                           referencingMember.getDescriptor(referencingClass))) +
                              "; }', but not the descriptor class '" +
                              ClassUtil.externalClassName(programClass.getName()) +
                              "'");
        }
    }
    public void visitLibraryClass(LibraryClass libraryClass) {}
}
