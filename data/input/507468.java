package proguard.classfile.editor;
import proguard.classfile.*;
import proguard.classfile.attribute.*;
import java.util.Arrays;
public class InterfacesEditor
{
    private final ProgramClass targetClass;
    public InterfacesEditor(ProgramClass targetClass)
    {
        this.targetClass = targetClass;
    }
    public void addInterface(int interfaceConstantIndex)
    {
        if (findInterfaceIndex(interfaceConstantIndex) < 0)
        {
            int   interfacesCount = targetClass.u2interfacesCount++;
            int[] interfaces      = targetClass.u2interfaces;
            if (interfaces.length <= interfacesCount)
            {
                int[] newinterfaces = new int[interfacesCount + 1];
                System.arraycopy(interfaces, 0, newinterfaces, 0, interfacesCount);
                interfaces = newinterfaces;
                targetClass.u2interfaces = interfaces;
            }
            interfaces[interfacesCount] = interfaceConstantIndex;
        }
    }
    public void deleteInterface(int interfaceConstantIndex)
    {
        int interfaceIndex = findInterfaceIndex(interfaceConstantIndex);
        if (interfaceIndex >= 0)
        {
            int   interfacesCount = --targetClass.u2interfacesCount;
            int[] interfaces      = targetClass.u2interfaces;
            for (int index = interfaceIndex; index < interfacesCount; index++)
            {
                interfaces[index] = interfaces[index + 1];
            }
            interfaces[interfacesCount] = 0;
        }
    }
    private int findInterfaceIndex(int interfaceConstantIndex)
    {
        int   interfacesCount = targetClass.u2interfacesCount;
        int[] interfaces      = targetClass.u2interfaces;
        for (int index = 0; index < interfacesCount; index++)
        {
            if (interfaces[index] == interfaceConstantIndex)
              {
                  return index;
              }
        }
        return -1;
    }
}