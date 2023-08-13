public class ClassLoaderReferenceImpl
    extends ObjectReferenceImpl
    implements ClassLoaderReference
{
     private List visibleClassesCache;
     private List definedClassesCache;
     ClassLoaderReferenceImpl(VirtualMachine aVm, Instance oRef) {
         super(aVm, oRef);
     }
     protected String description() {
         return "ClassLoaderReference " + uniqueID();
     }
     public List definedClasses() {
         if (definedClassesCache == null) {
             definedClassesCache = new ArrayList();
             Iterator iter = vm.allClasses().iterator();
             while (iter.hasNext()) {
                 ReferenceType type = (ReferenceType)iter.next();
                 if (equals(type.classLoader())) {  
                     definedClassesCache.add(type);
                 }
             }
         }
         return definedClassesCache;
     }
     private SystemDictionary getSystemDictionary() {
         return vm.saSystemDictionary();
     }
     private Universe getUniverse() {
         return vm.saUniverse();
     }
     public List visibleClasses() {
         if (visibleClassesCache != null)
            return visibleClassesCache;
         visibleClassesCache = new ArrayList();
         SystemDictionary sysDict = getSystemDictionary();
         sysDict.classesDo(
                           new SystemDictionary.ClassAndLoaderVisitor() {
                                public void visit(Klass k, Oop loader) {
                                    if (ref().equals(loader)) {
                                        for (Klass l = k; l != null; l = l.arrayKlassOrNull()) {
                                            visibleClassesCache.add(vm.referenceType(l));
                                        }
                                    }
                                }
                           }
                           );
         sysDict.primArrayClassesDo(
                                    new SystemDictionary.ClassAndLoaderVisitor() {
                                         public void visit(Klass k, Oop loader) {
                                             if (ref().equals(loader)) {
                                                 visibleClassesCache.add(vm.referenceType(k));
                                             }
                                         }
                                     }
                                     );
         getUniverse().basicTypeClassesDo(
                            new SystemDictionary.ClassVisitor() {
                                public void visit(Klass k) {
                                    visibleClassesCache.add(vm.referenceType(k));
                                }
                            }
                            );
         return visibleClassesCache;
     }
     Type findType(String signature) throws ClassNotLoadedException {
         List types = visibleClasses();
         Iterator iter = types.iterator();
         while (iter.hasNext()) {
             ReferenceType type = (ReferenceType)iter.next();
             if (type.signature().equals(signature)) {
                 return type;
             }
         }
         JNITypeParser parser = new JNITypeParser(signature);
         throw new ClassNotLoadedException(parser.typeName(),
                                          "Class " + parser.typeName() + " not loaded");
     }
}
