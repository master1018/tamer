package proguard.classfile.editor;
import proguard.classfile.*;
import proguard.classfile.util.SimplifiedVisitor;
import proguard.classfile.visitor.ClassVisitor;
import java.util.Arrays;
public class InterfaceSorter
extends      SimplifiedVisitor
implements   ClassVisitor
{
    public void visitProgramClass(ProgramClass programClass)
    {
        int[] interfaces      = programClass.u2interfaces;
        int   interfacesCount = programClass.u2interfacesCount;
        Arrays.sort(interfaces, 0, interfacesCount);
        int newInterfacesCount     = 0;
        int previousInterfaceIndex = 0;
        for (int index = 0; index < interfacesCount; index++)
        {
            int interfaceIndex = interfaces[index];
            if (interfaceIndex != previousInterfaceIndex)
            {
                interfaces[newInterfacesCount++] = interfaceIndex;
                previousInterfaceIndex = interfaceIndex;
            }
        }
        programClass.u2interfacesCount = newInterfacesCount;
    }
}
