package proguard.classfile.editor;
import proguard.classfile.*;
import proguard.classfile.constant.*;
import proguard.classfile.constant.visitor.ConstantVisitor;
import proguard.classfile.util.SimplifiedVisitor;
class      ComparableConstant
extends    SimplifiedVisitor
implements Comparable, ConstantVisitor
{
    private static final int[] PRIORITIES = new int[13];
    static
    {
        PRIORITIES[ClassConstants.CONSTANT_Integer]            = 0; 
        PRIORITIES[ClassConstants.CONSTANT_Float]              = 1;
        PRIORITIES[ClassConstants.CONSTANT_String]             = 2;
        PRIORITIES[ClassConstants.CONSTANT_Class]              = 3;
        PRIORITIES[ClassConstants.CONSTANT_Long]               = 4; 
        PRIORITIES[ClassConstants.CONSTANT_Double]             = 5;
        PRIORITIES[ClassConstants.CONSTANT_Fieldref]           = 6; 
        PRIORITIES[ClassConstants.CONSTANT_Methodref]          = 7;
        PRIORITIES[ClassConstants.CONSTANT_InterfaceMethodref] = 8;
        PRIORITIES[ClassConstants.CONSTANT_NameAndType]        = 9;
        PRIORITIES[ClassConstants.CONSTANT_Utf8]               = 10;
    }
    private final Clazz    clazz;
    private final int      thisIndex;
    private final Constant thisConstant;
    private Constant otherConstant;
    private int      result;
    public ComparableConstant(Clazz clazz, int index, Constant constant)
    {
        this.clazz        = clazz;
        this.thisIndex    = index;
        this.thisConstant = constant;
    }
    public int getIndex()
    {
        return thisIndex;
    }
    public Constant getConstant()
    {
        return thisConstant;
    }
    public int compareTo(Object other)
    {
        ComparableConstant otherComparableConstant = (ComparableConstant)other;
        otherConstant = otherComparableConstant.thisConstant;
        if (thisConstant == otherConstant)
        {
            int otherIndex = otherComparableConstant.thisIndex;
            return thisIndex <  otherIndex ? -1 :
                   thisIndex == otherIndex ?  0 :
                                              1;
        }
        int thisTag  = thisConstant.getTag();
        int otherTag = otherConstant.getTag();
        if (thisTag != otherTag)
        {
            return PRIORITIES[thisTag] < PRIORITIES[otherTag] ? -1 : 1;
        }
        thisConstant.accept(clazz, this);
        return result;
    }
    public void visitIntegerConstant(Clazz clazz, IntegerConstant integerConstant)
    {
        result = new Integer(integerConstant.getValue()).compareTo(new Integer(((IntegerConstant)otherConstant).getValue()));
    }
    public void visitLongConstant(Clazz clazz, LongConstant longConstant)
    {
        result = new Long(longConstant.getValue()).compareTo(new Long(((LongConstant)otherConstant).getValue()));
    }
    public void visitFloatConstant(Clazz clazz, FloatConstant floatConstant)
    {
        result = new Float(floatConstant.getValue()).compareTo(new Float(((FloatConstant)otherConstant).getValue()));
    }
    public void visitDoubleConstant(Clazz clazz, DoubleConstant doubleConstant)
    {
        result = new Double(doubleConstant.getValue()).compareTo(new Double(((DoubleConstant)otherConstant).getValue()));
    }
    public void visitStringConstant(Clazz clazz, StringConstant stringConstant)
    {
        result = stringConstant.getString(clazz).compareTo(((StringConstant)otherConstant).getString(clazz));
    }
    public void visitUtf8Constant(Clazz clazz, Utf8Constant utf8Constant)
    {
        result = utf8Constant.getString().compareTo(((Utf8Constant)otherConstant).getString());
    }
    public void visitAnyRefConstant(Clazz clazz, RefConstant refConstant)
    {
        RefConstant otherRefConstant = (RefConstant)otherConstant;
        result = (refConstant.getClassName(clazz) + ' ' +
                  refConstant.getName(clazz)      + ' ' +
                  refConstant.getType(clazz))
                 .compareTo
                 (otherRefConstant.getClassName(clazz) + ' ' +
                  otherRefConstant.getName(clazz)      + ' ' +
                  otherRefConstant.getType(clazz));
    }
    public void visitClassConstant(Clazz clazz, ClassConstant classConstant)
    {
        result = classConstant.getName(clazz).compareTo(((ClassConstant)otherConstant).getName(clazz));
    }
    public void visitNameAndTypeConstant(Clazz clazz, NameAndTypeConstant nameAndTypeConstant)
    {
        NameAndTypeConstant otherNameAndTypeConstant = (NameAndTypeConstant)otherConstant;
        result = (nameAndTypeConstant.getName(clazz) + ' ' +
                  nameAndTypeConstant.getType(clazz))
                 .compareTo
                 (otherNameAndTypeConstant.getName(clazz) + ' ' +
                  otherNameAndTypeConstant.getType(clazz));
    }
    public boolean equals(Object other)
    {
        return other != null &&
               this.getClass().equals(other.getClass()) &&
               this.getConstant().getClass().equals(((ComparableConstant)other).getConstant().getClass()) &&
               this.compareTo(other) == 0;
    }
    public int hashCode()
    {
        return this.getClass().hashCode();
    }
}
