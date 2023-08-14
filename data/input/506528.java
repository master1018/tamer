import proguard.classfile.*;
import proguard.classfile.attribute.*;
import proguard.classfile.attribute.visitor.AttributeVisitor;
import proguard.classfile.editor.ConstantPoolEditor;
import proguard.classfile.util.SimplifiedVisitor;
import proguard.classfile.visitor.ClassVisitor;
public class SourceFileRenamer
extends      SimplifiedVisitor
implements   ClassVisitor,
             AttributeVisitor
{
    private final String newSourceFileAttribute;
    public SourceFileRenamer(String newSourceFileAttribute)
    {
        this.newSourceFileAttribute = newSourceFileAttribute;
    }
    public void visitProgramClass(ProgramClass programClass)
    {
        programClass.attributesAccept(this);
    }
    public void visitAnyAttribute(Clazz clazz, Attribute attribute) {}
    public void visitSourceFileAttribute(Clazz clazz, SourceFileAttribute sourceFileAttribute)
    {
        sourceFileAttribute.u2sourceFileIndex =
            new ConstantPoolEditor((ProgramClass)clazz).addUtf8Constant(newSourceFileAttribute);
    }
    public void visitSourceDirAttribute(Clazz clazz, SourceDirAttribute sourceDirAttribute)
    {
        sourceDirAttribute.u2sourceDirIndex =
            new ConstantPoolEditor((ProgramClass)clazz).addUtf8Constant(newSourceFileAttribute);
    }
}
