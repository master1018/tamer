package proguard.classfile.attribute.preverification.visitor;
import proguard.classfile.*;
import proguard.classfile.attribute.CodeAttribute;
import proguard.classfile.attribute.preverification.*;
public interface VerificationTypeVisitor
{
    public void visitIntegerType(          Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, IntegerType           integerType);
    public void visitFloatType(            Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, FloatType             floatType);
    public void visitLongType(             Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, LongType              longType);
    public void visitDoubleType(           Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, DoubleType            doubleType);
    public void visitTopType(              Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, TopType               topType);
    public void visitObjectType(           Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, ObjectType            objectType);
    public void visitNullType(             Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, NullType              nullType);
    public void visitUninitializedType(    Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, UninitializedType     uninitializedType);
    public void visitUninitializedThisType(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, UninitializedThisType uninitializedThisType);
    public void visitStackIntegerType(          Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, int index, IntegerType           integerType);
    public void visitStackFloatType(            Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, int index, FloatType             floatType);
    public void visitStackLongType(             Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, int index, LongType              longType);
    public void visitStackDoubleType(           Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, int index, DoubleType            doubleType);
    public void visitStackTopType(              Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, int index, TopType               topType);
    public void visitStackObjectType(           Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, int index, ObjectType            objectType);
    public void visitStackNullType(             Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, int index, NullType              nullType);
    public void visitStackUninitializedType(    Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, int index, UninitializedType     uninitializedType);
    public void visitStackUninitializedThisType(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, int index, UninitializedThisType uninitializedThisType);
    public void visitVariablesIntegerType(          Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, int index, IntegerType           integerType);
    public void visitVariablesFloatType(            Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, int index, FloatType             floatType);
    public void visitVariablesLongType(             Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, int index, LongType              longType);
    public void visitVariablesDoubleType(           Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, int index, DoubleType            doubleType);
    public void visitVariablesTopType(              Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, int index, TopType               topType);
    public void visitVariablesObjectType(           Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, int index, ObjectType            objectType);
    public void visitVariablesNullType(             Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, int index, NullType              nullType);
    public void visitVariablesUninitializedType(    Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, int index, UninitializedType     uninitializedType);
    public void visitVariablesUninitializedThisType(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, int index, UninitializedThisType uninitializedThisType);
}
