 public class ClassDocCatalog {
     private Set<String> packageSet;
     private Map<String,Set<ClassDoc>> allClasses;
     private Map<String,Set<ClassDoc>> ordinaryClasses;
     private Map<String,Set<ClassDoc>> exceptions;
    private Map<String,Set<ClassDoc>> enums;
    private Map<String,Set<ClassDoc>> annotationTypes;
     private Map<String,Set<ClassDoc>> errors;
     private Map<String,Set<ClassDoc>> interfaces;
     private Configuration configuration;
     public ClassDocCatalog (ClassDoc[] classdocs, Configuration config) {
         init();
         this.configuration = config;
         for (int i = 0; i < classdocs.length; i++) {
             addClassDoc(classdocs[i]);
         }
     }
     public ClassDocCatalog () {
         init();
     }
     private void init() {
         allClasses = new HashMap<String,Set<ClassDoc>>();
         ordinaryClasses = new HashMap<String,Set<ClassDoc>>();
         exceptions = new HashMap<String,Set<ClassDoc>>();
         enums = new HashMap<String,Set<ClassDoc>>();
         annotationTypes = new HashMap<String,Set<ClassDoc>>();
         errors = new HashMap<String,Set<ClassDoc>>();
         interfaces = new HashMap<String,Set<ClassDoc>>();
         packageSet = new HashSet<String>();
     }
      public void addClassDoc(ClassDoc classdoc) {
        if (classdoc == null) {
            return;
        }
        addClass(classdoc, allClasses);
        if (classdoc.isOrdinaryClass()) {
            addClass(classdoc, ordinaryClasses);
        } else if (classdoc.isException()) {
            addClass(classdoc, exceptions);
        } else if (classdoc.isEnum()) {
            addClass(classdoc, enums);
        } else if (classdoc.isAnnotationType()) {
            addClass(classdoc, annotationTypes);
        } else if (classdoc.isError()) {
            addClass(classdoc, errors);
        } else if (classdoc.isInterface()) {
            addClass(classdoc, interfaces);
        }
      }
      private void addClass(ClassDoc classdoc, Map<String,Set<ClassDoc>> map) {
          PackageDoc pkg = classdoc.containingPackage();
          if (pkg.isIncluded() || (configuration.nodeprecated && Util.isDeprecated(pkg))) {
              return;
          }
          String key = Util.getPackageName(pkg);
          Set<ClassDoc> s = map.get(key);
          if (s == null) {
              packageSet.add(key);
              s = new HashSet<ClassDoc>();
          }
          s.add(classdoc);
          map.put(key, s);
      }
      private ClassDoc[] getArray(Map<String,Set<ClassDoc>> m, String key) {
          Set<ClassDoc> s = m.get(key);
          if (s == null) {
              return new ClassDoc[] {};
          } else {
              return s.toArray(new ClassDoc[] {});
          }
      }
      public ClassDoc[] allClasses(PackageDoc pkgDoc) {
          return pkgDoc.isIncluded() ?
                pkgDoc.allClasses() :
                getArray(allClasses, Util.getPackageName(pkgDoc));
      }
      public ClassDoc[] allClasses(String packageName) {
          return getArray(allClasses, packageName);
      }
     public String[] packageNames() {
         return packageSet.toArray(new String[] {});
     }
     public boolean isKnownPackage(String packageName) {
         return packageSet.contains(packageName);
     }
      public ClassDoc[] errors(String packageName) {
          return getArray(errors, packageName);
      }
      public ClassDoc[] exceptions(String packageName) {
          return getArray(exceptions, packageName);
      }
      public ClassDoc[] enums(String packageName) {
          return getArray(enums, packageName);
      }
      public ClassDoc[] annotationTypes(String packageName) {
          return getArray(annotationTypes, packageName);
      }
      public ClassDoc[] interfaces(String packageName) {
          return getArray(interfaces, packageName);
      }
      public ClassDoc[] ordinaryClasses(String packageName) {
          return getArray(ordinaryClasses, packageName);
      }
}
