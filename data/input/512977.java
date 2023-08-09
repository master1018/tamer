package proguard.classfile;
import proguard.classfile.util.ClassUtil;
import proguard.classfile.visitor.*;
import java.util.*;
public class ClassPool
{
    private final Map classes = new HashMap();
    public void clear()
    {
        classes.clear();
    }
    public void addClass(Clazz clazz)
    {
        classes.put(clazz.getName(), clazz);
    }
    public void removeClass(Clazz clazz)
    {
        classes.remove(clazz.getName());
    }
    public Clazz getClass(String className)
    {
        return (Clazz)classes.get(ClassUtil.internalClassNameFromClassType(className));
    }
    public Iterator classNames()
    {
        return classes.keySet().iterator();
    }
    public int size()
    {
        return classes.size();
    }
    public void accept(ClassPoolVisitor classPoolVisitor)
    {
        classPoolVisitor.visitClassPool(this);
    }
    public void classesAccept(ClassVisitor classVisitor)
    {
        Iterator iterator = classes.values().iterator();
        while (iterator.hasNext())
        {
            Clazz clazz = (Clazz)iterator.next();
            clazz.accept(classVisitor);
        }
    }
    public void classesAcceptAlphabetically(ClassVisitor classVisitor)
    {
        TreeMap sortedClasses = new TreeMap(classes);
        Iterator iterator = sortedClasses.values().iterator();
        while (iterator.hasNext())
        {
            Clazz clazz = (Clazz)iterator.next();
            clazz.accept(classVisitor);
        }
    }
    public void classAccept(String className, ClassVisitor classVisitor)
    {
        Clazz clazz = getClass(className);
        if (clazz != null)
        {
            clazz.accept(classVisitor);
        }
    }
}
