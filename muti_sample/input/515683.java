public class SinceTagger {
    private final Map<String, String> xmlToName
            = new LinkedHashMap<String, String>();
    public void addVersion(String file, String name) {
        xmlToName.put(file, name);
    }
    public void tagAll(ClassInfo[] classDocs) {
        for (Map.Entry<String, String> versionSpec : xmlToName.entrySet()) {
            String xmlFile = versionSpec.getKey();
            String versionName = versionSpec.getValue();
            ApiInfo specApi = new ApiCheck().parseApi(xmlFile);
            applyVersionsFromSpec(versionName, specApi, classDocs);
        }
        if (!xmlToName.isEmpty()) {
            warnForMissingVersions(classDocs);
        }
    }
    public void writeVersionNames(HDF data) {
        int index = 1;
        for (String version : xmlToName.values()) {
            data.setValue("since." + index + ".name", version);
            index++;
        }
    }
    private void applyVersionsFromSpec(String versionName,
            ApiInfo specApi, ClassInfo[] classDocs) {
        for (ClassInfo classDoc : classDocs) {
            com.android.apicheck.PackageInfo packageSpec
                    = specApi.getPackages().get(classDoc.containingPackage().name());
            if (packageSpec == null) {
                continue;
            }
            com.android.apicheck.ClassInfo classSpec
                    = packageSpec.allClasses().get(classDoc.name());
            if (classSpec == null) {
                continue;
            }
            versionPackage(versionName, classDoc.containingPackage());
            versionClass(versionName, classDoc);
            versionConstructors(versionName, classSpec, classDoc);
            versionFields(versionName, classSpec, classDoc);
            versionMethods(versionName, classSpec, classDoc);
        }
    }
    private void versionPackage(String versionName, PackageInfo doc) {
        if (doc.getSince() == null) {
            doc.setSince(versionName);
        }
    }
    private void versionClass(String versionName, ClassInfo doc) {
        if (doc.getSince() == null) {
            doc.setSince(versionName);
        }
    }
    private void versionConstructors(String versionName,
            com.android.apicheck.ClassInfo spec, ClassInfo doc) {
        for (MethodInfo constructor : doc.constructors()) {
            if (constructor.getSince() == null
                    && spec.allConstructors().containsKey(constructor.getHashableName())) {
                constructor.setSince(versionName);
            }
        }
    }
    private void versionFields(String versionName,
            com.android.apicheck.ClassInfo spec, ClassInfo doc) {
        for (FieldInfo field : doc.fields()) {
            if (field.getSince() == null
                    && spec.allFields().containsKey(field.name())) {
                field.setSince(versionName);
            }
        }
    }
    private void versionMethods(String versionName,
            com.android.apicheck.ClassInfo spec, ClassInfo doc) {
        for (MethodInfo method : doc.methods()) {
            if (method.getSince() != null) {
                continue;
            }
            for (com.android.apicheck.ClassInfo superclass : spec.hierarchy()) {
                if (superclass.allMethods().containsKey(method.getHashableName())) {
                    method.setSince(versionName);
                    break;
                }
            }
        }
    }
    private void warnForMissingVersions(ClassInfo[] classDocs) {
        for (ClassInfo claz : classDocs) {
            if (!checkLevelRecursive(claz)) {
                continue;
            }
            if (claz.getSince() == null) {
                Errors.error(Errors.NO_SINCE_DATA, claz.position(),
                        "XML missing class " + claz.qualifiedName());
            }
            for (FieldInfo field : missingVersions(claz.fields())) {
                Errors.error(Errors.NO_SINCE_DATA, field.position(),
                        "XML missing field " + claz.qualifiedName()
                                + "#" + field.name());
            }
            for (MethodInfo constructor : missingVersions(claz.constructors())) {
                Errors.error(Errors.NO_SINCE_DATA, constructor.position(),
                        "XML missing constructor " + claz.qualifiedName()
                                + "#" + constructor.getHashableName());
            }
            for (MethodInfo method : missingVersions(claz.methods())) {
                Errors.error(Errors.NO_SINCE_DATA, method.position(),
                        "XML missing method " + claz.qualifiedName()
                                + "#" + method.getHashableName());
            }
        }
    }
    private <T extends MemberInfo> Iterable<T> missingVersions(T[] all) {
        List<T> result = Collections.emptyList();
        for (T t : all) {
            if (t.getSince() != null
                    || t.isHidden()
                    || !checkLevelRecursive(t.realContainingClass())) {
                continue;
            }
            if (result.isEmpty()) {
                result = new ArrayList<T>(); 
            }
            result.add(t);
        }
        return result;
    }
    private boolean checkLevelRecursive(ClassInfo claz) {
        for (ClassInfo c = claz; c != null; c = c.containingClass()) {
            if (!c.checkLevel()) {
                return false;
            }
        }
        return true;
    }
}
