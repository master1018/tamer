package proguard.classfile.editor;
import proguard.classfile.ProgramClass;
import proguard.classfile.util.SimplifiedVisitor;
import proguard.classfile.visitor.ClassVisitor;
public class ClassElementSorter
extends      SimplifiedVisitor
implements   ClassVisitor
{
    private final ClassVisitor interfaceSorter    = new InterfaceSorter();
    private final ClassVisitor constantPoolSorter = new ConstantPoolSorter();
    private final ClassVisitor attributeSorter    = new AttributeSorter();
    public void visitProgramClass(ProgramClass programClass)
    {
        programClass.accept(constantPoolSorter);
        programClass.accept(interfaceSorter);
        programClass.accept(attributeSorter);
    }
}
