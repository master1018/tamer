import proguard.classfile.*;
import proguard.classfile.constant.visitor.*;
import proguard.classfile.editor.*;
import proguard.classfile.util.*;
import proguard.classfile.visitor.*;
import proguard.optimize.KeepMarker;
import proguard.optimize.info.*;
import proguard.util.*;
import java.util.*;
public class ClassMerger
extends      SimplifiedVisitor
implements   ClassVisitor,
             ConstantVisitor
{
    private static final boolean DEBUG = false;
    public ClassMerger(ProgramClass targetClass,
                       boolean      allowAccessModification,
                       boolean      mergeInterfacesAggressively)
    {
        this(targetClass, allowAccessModification, mergeInterfacesAggressively, null);
    }
    public ClassMerger(ProgramClass targetClass,
                       boolean      allowAccessModification,
                       boolean      mergeInterfacesAggressively,
                       ClassVisitor extraClassVisitor)
    {
        this.targetClass                 = targetClass;
        this.allowAccessModification     = allowAccessModification;
        this.mergeInterfacesAggressively = mergeInterfacesAggressively;
        this.extraClassVisitor           = extraClassVisitor;
    }
    public void visitProgramClass(ProgramClass programClass)
    {
        try
        {
            visitProgramClass0(programClass);
        }
        catch (RuntimeException ex)
        {
            System.err.println("Unexpected error while merging classes:");
            System.err.println("  Class        = ["+programClass.getName()+"]");
            System.err.println("  Target class = ["+targetClass.getName()+"]");
            System.err.println("  Exception    = ["+ex.getClass().getName()+"] ("+ex.getMessage()+")");
            if (DEBUG)
            {
                programClass.accept(new ClassPrinter());
                targetClass.accept(new ClassPrinter());
            }
            throw ex;
        }
    }
    public void visitProgramClass0(ProgramClass programClass)
    {
        if (!programClass.equals(targetClass) &&
            !KeepMarker.isKept(programClass) &&
            !KeepMarker.isKept(targetClass)  &&
            getTargetClass(programClass) == null &&
            getTargetClass(targetClass)  == null &&
            (programClass.getAccessFlags() & ClassConstants.INTERNAL_ACC_ANNOTATTION) == 0 &&
            (allowAccessModification                                                        ||
             ((programClass.getAccessFlags() &
               targetClass.getAccessFlags()  &
               ClassConstants.INTERNAL_ACC_PUBLIC) != 0 &&
              !PackageVisibleMemberContainingClassMarker.containsPackageVisibleMembers(programClass) &&
              !PackageVisibleMemberInvokingClassMarker.invokesPackageVisibleMembers(programClass)) ||
             ClassUtil.internalPackageName(programClass.getName()).equals(
             ClassUtil.internalPackageName(targetClass.getName()))) &&
            ((programClass.getAccessFlags() &
              (ClassConstants.INTERNAL_ACC_INTERFACE |
               ClassConstants.INTERNAL_ACC_ABSTRACT)) ==
             (targetClass.getAccessFlags()  &
              (ClassConstants.INTERNAL_ACC_INTERFACE |
               ClassConstants.INTERNAL_ACC_ABSTRACT)) ||
             (isOnlySubClass(programClass, targetClass) &&
              (programClass.getSuperClass().equals(targetClass) ||
               programClass.getSuperClass().equals(targetClass.getSuperClass())))) &&
            !indirectlyImplementedInterfaces(programClass).contains(targetClass) &&
            !targetClass.extendsOrImplements(programClass) &&
            initializedSuperClasses(programClass).equals(initializedSuperClasses(targetClass))   &&
            instanceofedSuperClasses(programClass).equals(instanceofedSuperClasses(targetClass)) &&
            caughtSuperClasses(programClass).equals(caughtSuperClasses(targetClass)) &&
            !(DotClassMarker.isDotClassed(programClass) &&
              DotClassMarker.isDotClassed(targetClass)) &&
            !introducesUnwantedFields(programClass, targetClass) &&
            !introducesUnwantedFields(targetClass, programClass) &&
            !haveAnyIdenticalInitializers(programClass, targetClass) &&
            (mergeInterfacesAggressively ||
             (!introducesUnwantedAbstractMethods(programClass, targetClass) &&
              !introducesUnwantedAbstractMethods(targetClass, programClass))) &&
            !overridesAnyMethods(programClass, targetClass) &&
            !overridesAnyMethods(targetClass, programClass) &&
            !shadowsAnyMethods(programClass, targetClass) &&
            !shadowsAnyMethods(targetClass, programClass))
        {
            if (DEBUG)
            {
                System.out.println("ClassMerger ["+programClass.getName()+"] -> ["+targetClass.getName()+"]");
                System.out.println("  Source interface? ["+((programClass.getAccessFlags() & ClassConstants.INTERNAL_ACC_INTERFACE)!=0)+"]");
                System.out.println("  Target interface? ["+((targetClass.getAccessFlags() & ClassConstants.INTERNAL_ACC_INTERFACE)!=0)+"]");
                System.out.println("  Source subclasses ["+programClass.subClasses+"]");
                System.out.println("  Target subclasses ["+targetClass.subClasses+"]");
                System.out.println("  Source superclass ["+programClass.getSuperClass().getName()+"]");
                System.out.println("  Target superclass ["+targetClass.getSuperClass().getName()+"]");
            }
            int targetAccessFlags = targetClass.getAccessFlags();
            int sourceAccessFlags = programClass.getAccessFlags();
            targetClass.u2accessFlags =
                ((targetAccessFlags &
                  sourceAccessFlags) &
                 (ClassConstants.INTERNAL_ACC_INTERFACE  |
                  ClassConstants.INTERNAL_ACC_ABSTRACT)) |
                ((targetAccessFlags |
                  sourceAccessFlags) &
                 (ClassConstants.INTERNAL_ACC_PUBLIC     |
                  ClassConstants.INTERNAL_ACC_ANNOTATTION |
                  ClassConstants.INTERNAL_ACC_ENUM));
            programClass.interfaceConstantsAccept(
                new ExceptClassConstantFilter(targetClass.getName(),
                new ImplementedClassConstantFilter(targetClass,
                new ImplementingClassConstantFilter(targetClass,
                new InterfaceAdder(targetClass)))));
            MemberAdder memberAdder =
                new MemberAdder(targetClass);
            programClass.fieldsAccept(memberAdder);
            programClass.methodsAccept(memberAdder);
            programClass.attributesAccept(
                new AttributeAdder(targetClass, true));
            ClassOptimizationInfo info =
                ClassOptimizationInfo.getClassOptimizationInfo(targetClass);
            if (info != null)
            {
                info.merge(ClassOptimizationInfo.getClassOptimizationInfo(programClass));
            }
            setTargetClass(programClass, targetClass);
            if (extraClassVisitor != null)
            {
                extraClassVisitor.visitProgramClass(programClass);
            }
        }
    }
    private boolean isOnlySubClass(Clazz        subClass,
                                   ProgramClass clazz)
    {
        return clazz.subClasses != null     &&
               clazz.subClasses.length == 1 &&
               clazz.subClasses[0].equals(subClass);
    }
    private Set indirectlyImplementedInterfaces(Clazz clazz)
    {
        Set set = new HashSet();
        ReferencedClassVisitor referencedInterfaceCollector =
            new ReferencedClassVisitor(
            new ClassHierarchyTraveler(false, false, true, false,
            new ClassCollector(set)));
        clazz.superClassConstantAccept(referencedInterfaceCollector);
        clazz.interfaceConstantsAccept(referencedInterfaceCollector);
        return set;
    }
    private Set initializedSuperClasses(Clazz clazz)
    {
        Set set = new HashSet();
        clazz.hierarchyAccept(true, true, true, false,
                              new NamedMethodVisitor(ClassConstants.INTERNAL_METHOD_NAME_CLINIT,
                                                     ClassConstants.INTERNAL_METHOD_TYPE_INIT,
                              new MemberToClassVisitor(
                              new ClassCollector(set))));
        return set;
    }
    private Set instanceofedSuperClasses(Clazz clazz)
    {
        Set set = new HashSet();
        clazz.hierarchyAccept(true, true, true, false,
                              new InstanceofClassFilter(
                              new ClassCollector(set)));
        return set;
    }
    private Set caughtSuperClasses(Clazz clazz)
    {
        Set set = new HashSet();
        clazz.hierarchyAccept(true, true, false, false,
                              new CaughtClassFilter(
                              new ClassCollector(set)));
        return set;
    }
    private boolean introducesUnwantedFields(ProgramClass programClass,
                                             ProgramClass targetClass)
    {
        return
            programClass.u2fieldsCount != 0 &&
            (InstantiationClassMarker.isInstantiated(targetClass) ||
             (targetClass.subClasses != null &&
              !isOnlySubClass(programClass, targetClass)));
    }
    private boolean haveAnyIdenticalInitializers(Clazz clazz, Clazz targetClass)
    {
        MemberCounter counter = new MemberCounter();
        clazz.methodsAccept(
                            new SimilarMemberVisitor(targetClass, true, false, false, false,
                            new MemberAccessFilter(0, ClassConstants.INTERNAL_ACC_ABSTRACT,
                            counter)));
        return counter.getCount() > 0;
    }
    private boolean introducesUnwantedAbstractMethods(Clazz        clazz,
                                                      ProgramClass targetClass)
    {
        if ((targetClass.getAccessFlags() &
             (ClassConstants.INTERNAL_ACC_ABSTRACT |
              ClassConstants.INTERNAL_ACC_INTERFACE)) != 0 &&
            (targetClass.subClasses == null ||
             isOnlySubClass(clazz, targetClass)))
        {
            return false;
        }
        MemberCounter counter   = new MemberCounter();
        Set           targetSet = new HashSet();
        clazz.methodsAccept(new MemberAccessFilter(ClassConstants.INTERNAL_ACC_ABSTRACT, 0,
                            new MultiMemberVisitor(new MemberVisitor[]
                            {
                                counter,
                                new SimilarMemberVisitor(targetClass, true, true, true, false,
                                                         new MemberAccessFilter(ClassConstants.INTERNAL_ACC_ABSTRACT, 0,
                                                         new MemberCollector(targetSet)))
                            })));
        return targetSet.size() < counter.getCount();
    }
    private boolean overridesAnyMethods(Clazz clazz, Clazz targetClass)
    {
        MemberCounter counter = new MemberCounter();
        clazz.methodsAccept(new MemberAccessFilter(0, ClassConstants.INTERNAL_ACC_PRIVATE | ClassConstants.INTERNAL_ACC_STATIC | ClassConstants.INTERNAL_ACC_ABSTRACT,
                            new MemberNameFilter(new NotMatcher(new FixedStringMatcher(ClassConstants.INTERNAL_METHOD_NAME_CLINIT)),
                            new MemberNameFilter(new NotMatcher(new FixedStringMatcher(ClassConstants.INTERNAL_METHOD_NAME_INIT)),
                            new SimilarMemberVisitor(targetClass, true, true, false, false,
                            new MemberAccessFilter(0, ClassConstants.INTERNAL_ACC_PRIVATE | ClassConstants.INTERNAL_ACC_STATIC | ClassConstants.INTERNAL_ACC_ABSTRACT,
                            counter))))));
        return counter.getCount() > 0;
    }
    private boolean shadowsAnyMethods(Clazz clazz, Clazz targetClass)
    {
        MemberCounter counter = new MemberCounter();
        clazz.hierarchyAccept(true, false, false, true,
                              new AllMethodVisitor(
                              new MemberAccessFilter(ClassConstants.INTERNAL_ACC_PRIVATE, 0,
                              new MemberNameFilter(new NotMatcher(new FixedStringMatcher(ClassConstants.INTERNAL_METHOD_NAME_INIT)),
                              new SimilarMemberVisitor(targetClass, true, true, true, false,
                              new MemberAccessFilter(0, ClassConstants.INTERNAL_ACC_PRIVATE,
                              counter))))));
        clazz.hierarchyAccept(true, false, false, true,
                              new AllMethodVisitor(
                              new MemberAccessFilter(ClassConstants.INTERNAL_ACC_STATIC, 0,
                              new MemberNameFilter(new NotMatcher(new FixedStringMatcher(ClassConstants.INTERNAL_METHOD_NAME_CLINIT)),
                              new SimilarMemberVisitor(targetClass, true, true, true, false,
                              new MemberAccessFilter(0, ClassConstants.INTERNAL_ACC_PRIVATE,
                              counter))))));
        return counter.getCount() > 0;
    }
    public static void setTargetClass(Clazz clazz, Clazz targetClass)
    {
        ClassOptimizationInfo info = ClassOptimizationInfo.getClassOptimizationInfo(clazz);
        if (info != null)
        {
            info.setTargetClass(targetClass);
        }
    }
    public static Clazz getTargetClass(Clazz clazz)
    {
        Clazz targetClass = null;
        while (true)
        {
            ClassOptimizationInfo info = ClassOptimizationInfo.getClassOptimizationInfo(clazz);
            if (info == null)
            {
                return targetClass;
            }
            clazz = info.getTargetClass();
            if (clazz == null)
            {
                return targetClass;
            }
            targetClass = clazz;
        }
    }
}