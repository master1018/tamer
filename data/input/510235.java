package proguard.classfile.constant.visitor;
import proguard.classfile.*;
import proguard.classfile.editor.*;
import proguard.classfile.attribute.*;
import proguard.classfile.attribute.annotation.*;
import proguard.classfile.attribute.preverification.*;
import proguard.classfile.constant.ClassConstant;
import proguard.classfile.constant.visitor.ConstantVisitor;
import proguard.classfile.util.SimplifiedVisitor;
public class ExceptClassConstantFilter
extends      SimplifiedVisitor
implements   ConstantVisitor
{
    private final String           exceptClassName;
    private final ConstantVisitor constantVisitor;
        public ExceptClassConstantFilter(String          exceptClassName,
                                         ConstantVisitor constantVisitor)
    {
            this.exceptClassName = exceptClassName;
            this.constantVisitor = constantVisitor;
    }
    public void visitClassConstant(Clazz clazz, ClassConstant classConstant)
    {
        if (!classConstant.getName(clazz).equals(exceptClassName))
        {
            constantVisitor.visitClassConstant(clazz, classConstant);
        }
    }
}