import proguard.classfile.*;
import proguard.classfile.util.*;
import proguard.classfile.visitor.MemberVisitor;
import java.util.Map;
public class MemberNameConflictFixer implements MemberVisitor
{
    private final boolean          allowAggressiveOverloading;
    private final Map              descriptorMap;
    private final WarningPrinter   warningPrinter;
    private final MemberObfuscator memberObfuscator;
    public MemberNameConflictFixer(boolean          allowAggressiveOverloading,
                                   Map              descriptorMap,
                                   WarningPrinter   warningPrinter,
                                   MemberObfuscator memberObfuscator)
    {
        this.allowAggressiveOverloading = allowAggressiveOverloading;
        this.descriptorMap              = descriptorMap;
        this.warningPrinter             = warningPrinter;
        this.memberObfuscator           = memberObfuscator;
    }
    public void visitProgramField(ProgramClass programClass, ProgramField programField)
    {
        visitMember(programClass, programField, true);
    }
    public void visitProgramMethod(ProgramClass programClass, ProgramMethod programMethod)
    {
        String name = programMethod.getName(programClass);
        if (name.equals(ClassConstants.INTERNAL_METHOD_NAME_CLINIT) ||
            name.equals(ClassConstants.INTERNAL_METHOD_NAME_INIT))
        {
            return;
        }
        visitMember(programClass, programMethod, false);
    }
    public void visitLibraryField(LibraryClass libraryClass, LibraryField libraryField) {}
    public void visitLibraryMethod(LibraryClass libraryClass, LibraryMethod libraryMethod) {}
    private void visitMember(Clazz   clazz,
                             Member  member,
                             boolean isField)
    {
        String name       = member.getName(clazz);
        String descriptor = member.getDescriptor(clazz);
        if (!allowAggressiveOverloading)
        {
            descriptor = descriptor.substring(0, descriptor.indexOf(')')+1);
        }
        Map nameMap = MemberObfuscator.retrieveNameMap(descriptorMap, descriptor);
        String newName = MemberObfuscator.newMemberName(member);
        String previousName = (String)nameMap.get(newName);
        if (previousName != null &&
            !name.equals(previousName))
        {
            if (MemberObfuscator.hasFixedNewMemberName(member) &&
                warningPrinter != null)
            {
                descriptor = member.getDescriptor(clazz);
                warningPrinter.print(clazz.getName(),
                                     "Warning: " + ClassUtil.externalClassName(clazz.getName()) +
                                                   (isField ?
                                                       ": field '" + ClassUtil.externalFullFieldDescription(0, name, descriptor) :
                                                       ": method '" + ClassUtil.externalFullMethodDescription(clazz.getName(), 0, name, descriptor)) +
                                     "' can't be mapped to '" + newName +
                                     "' because it would conflict with " +
                                     (isField ?
                                         "field '" :
                                         "method '" ) + previousName +
                                     "', which is already being mapped to '" + newName + "'");
            }
            MemberObfuscator.setNewMemberName(member, null);
            member.accept(clazz, memberObfuscator);
        }
    }
}
