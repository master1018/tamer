public class ClassUseMapper {
    private final ClassTree classtree;
    public Map<String,Set<PackageDoc>> classToPackage = new HashMap<String,Set<PackageDoc>>();
    public Map<String,List<PackageDoc>> classToPackageAnnotations = new HashMap<String,List<PackageDoc>>();
    public Map<String,Set<ClassDoc>> classToClass = new HashMap<String,Set<ClassDoc>>();
    public Map<String,List<ClassDoc>> classToSubclass = new HashMap<String,List<ClassDoc>>();
    public Map<String,List<ClassDoc>> classToSubinterface = new HashMap<String,List<ClassDoc>>();
    public Map<String,List<ClassDoc>> classToImplementingClass = new HashMap<String,List<ClassDoc>>();
    public Map<String,List<FieldDoc>> classToField = new HashMap<String,List<FieldDoc>>();
    public Map<String,List<MethodDoc>> classToMethodReturn = new HashMap<String,List<MethodDoc>>();
    public Map<String,List<ExecutableMemberDoc>> classToMethodArgs = new HashMap<String,List<ExecutableMemberDoc>>();
    public Map<String,List<ExecutableMemberDoc>> classToMethodThrows = new HashMap<String,List<ExecutableMemberDoc>>();
    public Map<String,List<ExecutableMemberDoc>> classToConstructorArgs = new HashMap<String,List<ExecutableMemberDoc>>();
    public Map<String,List<ExecutableMemberDoc>> classToConstructorThrows = new HashMap<String,List<ExecutableMemberDoc>>();
    public Map<String,List<ConstructorDoc>> classToConstructorAnnotations = new HashMap<String,List<ConstructorDoc>>();
    public Map<String,List<ExecutableMemberDoc>> classToConstructorParamAnnotation = new HashMap<String,List<ExecutableMemberDoc>>();
    public Map<String,List<ExecutableMemberDoc>> classToConstructorDocArgTypeParam = new HashMap<String,List<ExecutableMemberDoc>>();
    public Map<String,List<ClassDoc>> classToClassTypeParam = new HashMap<String,List<ClassDoc>>();
    public Map<String,List<ClassDoc>> classToClassAnnotations = new HashMap<String,List<ClassDoc>>();
    public Map<String,List<MethodDoc>> classToExecMemberDocTypeParam = new HashMap<String,List<MethodDoc>>();
    public Map<String,List<ExecutableMemberDoc>> classToExecMemberDocArgTypeParam = new HashMap<String,List<ExecutableMemberDoc>>();
    public Map<String,List<MethodDoc>> classToExecMemberDocAnnotations = new HashMap<String,List<MethodDoc>>();
    public Map<String,List<MethodDoc>> classToExecMemberDocReturnTypeParam = new HashMap<String,List<MethodDoc>>();
    public Map<String,List<ExecutableMemberDoc>> classToExecMemberDocParamAnnotation = new HashMap<String,List<ExecutableMemberDoc>>();
    public Map<String,List<FieldDoc>> classToFieldDocTypeParam = new HashMap<String,List<FieldDoc>>();
    public Map<String,List<FieldDoc>> annotationToFieldDoc = new HashMap<String,List<FieldDoc>>();
    public ClassUseMapper(RootDoc root, ClassTree classtree) {
        this.classtree = classtree;
        for (Iterator<ClassDoc> it = classtree.baseclasses().iterator(); it.hasNext();) {
            subclasses(it.next());
        }
        for (Iterator<ClassDoc> it = classtree.baseinterfaces().iterator(); it.hasNext();) {
            implementingClasses(it.next());
        }
        ClassDoc[] classes = root.classes();
        for (int i = 0; i < classes.length; i++) {
            PackageDoc pkg = classes[i].containingPackage();
            mapAnnotations(classToPackageAnnotations, pkg, pkg);
            ClassDoc cd = classes[i];
            mapTypeParameters(classToClassTypeParam, cd, cd);
            mapAnnotations(classToClassAnnotations, cd, cd);
            FieldDoc[] fields = cd.fields();
            for (int j = 0; j < fields.length; j++) {
                FieldDoc fd = fields[j];
                mapTypeParameters(classToFieldDocTypeParam, fd, fd);
                mapAnnotations(annotationToFieldDoc, fd, fd);
                if (! fd.type().isPrimitive()) {
                    add(classToField, fd.type().asClassDoc(), fd);
                }
            }
            ConstructorDoc[] cons = cd.constructors();
            for (int j = 0; j < cons.length; j++) {
                mapAnnotations(classToConstructorAnnotations, cons[j], cons[j]);
                mapExecutable(cons[j]);
            }
            MethodDoc[] meths = cd.methods();
            for (int j = 0; j < meths.length; j++) {
                MethodDoc md = meths[j];
                mapExecutable(md);
                mapTypeParameters(classToExecMemberDocTypeParam, md, md);
                mapAnnotations(classToExecMemberDocAnnotations, md, md);
                if (! (md.returnType().isPrimitive() || md.returnType() instanceof TypeVariable)) {
                    mapTypeParameters(classToExecMemberDocReturnTypeParam,
                        md.returnType(), md);
                    add(classToMethodReturn, md.returnType().asClassDoc(), md);
                }
            }
        }
    }
    private Collection<ClassDoc> subclasses(ClassDoc cd) {
        Collection<ClassDoc> ret = classToSubclass.get(cd.qualifiedName());
        if (ret == null) {
            ret = new TreeSet<ClassDoc>();
            List<ClassDoc> subs = classtree.subclasses(cd);
            if (subs != null) {
                ret.addAll(subs);
                for (Iterator<ClassDoc> it = subs.iterator(); it.hasNext();) {
                    ret.addAll(subclasses(it.next()));
                }
            }
            addAll(classToSubclass, cd, ret);
        }
        return ret;
    }
    private Collection<ClassDoc> subinterfaces(ClassDoc cd) {
        Collection<ClassDoc> ret = classToSubinterface.get(cd.qualifiedName());
        if (ret == null) {
            ret = new TreeSet<ClassDoc>();
            List<ClassDoc> subs = classtree.subinterfaces(cd);
            if (subs != null) {
                ret.addAll(subs);
                for (Iterator<ClassDoc> it = subs.iterator(); it.hasNext();) {
                    ret.addAll(subinterfaces(it.next()));
                }
            }
            addAll(classToSubinterface, cd, ret);
        }
        return ret;
    }
    private Collection<ClassDoc> implementingClasses(ClassDoc cd) {
        Collection<ClassDoc> ret = classToImplementingClass.get(cd.qualifiedName());
        if (ret == null) {
            ret = new TreeSet<ClassDoc>();
            List<ClassDoc> impl = classtree.implementingclasses(cd);
            if (impl != null) {
                ret.addAll(impl);
                for (Iterator<ClassDoc> it = impl.iterator(); it.hasNext();) {
                    ret.addAll(subclasses(it.next()));
                }
            }
            for (Iterator<ClassDoc> it = subinterfaces(cd).iterator(); it.hasNext();) {
                ret.addAll(implementingClasses(it.next()));
            }
            addAll(classToImplementingClass, cd, ret);
        }
        return ret;
    }
    private void mapExecutable(ExecutableMemberDoc em) {
        Parameter[] params = em.parameters();
        boolean isConstructor = em.isConstructor();
        List<Type> classArgs = new ArrayList<Type>();
        for (int k = 0; k < params.length; k++) {
            Type pcd = params[k].type();
            if ((! params[k].type().isPrimitive()) &&
                 ! classArgs.contains(pcd) &&
                 ! (pcd instanceof TypeVariable)) {
                add(isConstructor? classToConstructorArgs :classToMethodArgs,
                        pcd.asClassDoc(), em);
                classArgs.add(pcd);
                mapTypeParameters(isConstructor?
                   classToConstructorDocArgTypeParam : classToExecMemberDocArgTypeParam,
                   pcd, em);
            }
            mapAnnotations(
                isConstructor ?
                    classToConstructorParamAnnotation :
                    classToExecMemberDocParamAnnotation,
                params[k], em);
        }
        ClassDoc[] thr = em.thrownExceptions();
        for (int k = 0; k < thr.length; k++) {
            add(isConstructor? classToConstructorThrows : classToMethodThrows,
                    thr[k], em);
        }
    }
    private <T> List<T> refList(Map<String,List<T>> map, ClassDoc cd) {
        List<T> list = map.get(cd.qualifiedName());
        if (list == null) {
            List<T> l = new ArrayList<T>();
            list = l;
            map.put(cd.qualifiedName(), list);
        }
        return list;
    }
    private Set<PackageDoc> packageSet(ClassDoc cd) {
        Set<PackageDoc> pkgSet = classToPackage.get(cd.qualifiedName());
        if (pkgSet == null) {
            pkgSet = new TreeSet<PackageDoc>();
            classToPackage.put(cd.qualifiedName(), pkgSet);
        }
        return pkgSet;
    }
    private Set<ClassDoc> classSet(ClassDoc cd) {
        Set<ClassDoc> clsSet = classToClass.get(cd.qualifiedName());
        if (clsSet == null) {
            Set<ClassDoc> s = new TreeSet<ClassDoc>();
            clsSet = s;
            classToClass.put(cd.qualifiedName(), clsSet);
        }
        return clsSet;
    }
    private <T extends ProgramElementDoc> void add(Map<String,List<T>> map, ClassDoc cd, T ref) {
        refList(map, cd).add(ref);
        packageSet(cd).add(ref.containingPackage());
        classSet(cd).add(ref instanceof MemberDoc?
                ((MemberDoc)ref).containingClass() :
                    (ClassDoc)ref);
    }
    private void addAll(Map<String,List<ClassDoc>> map, ClassDoc cd, Collection<ClassDoc> refs) {
        if (refs == null) {
            return;
        }
        refList(map, cd).addAll(refs);
        Set<PackageDoc> pkgSet = packageSet(cd);
        Set<ClassDoc> clsSet = classSet(cd);
        for (Iterator<ClassDoc> it = refs.iterator(); it.hasNext();) {
            ClassDoc cls = it.next();
            pkgSet.add(cls.containingPackage());
            clsSet.add(cls);
        }
    }
    private <T extends ProgramElementDoc> void mapTypeParameters(Map<String,List<T>> map, Object doc,
            T holder) {
        TypeVariable[] typeVariables;
        if (doc instanceof ClassDoc) {
            typeVariables = ((ClassDoc) doc).typeParameters();
        } else if (doc instanceof WildcardType) {
            Type[] extendsBounds = ((WildcardType) doc).extendsBounds();
            for (int k = 0; k < extendsBounds.length; k++) {
                addTypeParameterToMap(map, extendsBounds[k], holder);
            }
            Type[] superBounds = ((WildcardType) doc).superBounds();
            for (int k = 0; k < superBounds.length; k++) {
                addTypeParameterToMap(map, superBounds[k], holder);
            }
            return;
        } else if (doc instanceof ParameterizedType) {
            Type[] typeArguments = ((ParameterizedType) doc).typeArguments();
            for (int k = 0; k < typeArguments.length; k++) {
                addTypeParameterToMap(map, typeArguments[k], holder);
            }
            return;
        } else if (doc instanceof ExecutableMemberDoc) {
            typeVariables = ((ExecutableMemberDoc) doc).typeParameters();
        } else if (doc instanceof FieldDoc) {
            Type fieldType = ((FieldDoc) doc).type();
            mapTypeParameters(map, fieldType, holder);
            return;
        } else {
            return;
        }
        for (int i = 0; i < typeVariables.length; i++) {
            Type[] bounds = typeVariables[i].bounds();
            for (int j = 0; j < bounds.length; j++) {
                addTypeParameterToMap(map, bounds[j], holder);
            }
        }
    }
    private <T extends ProgramElementDoc> void mapAnnotations(Map<String,List<T>> map, Object doc,
            T holder) {
        AnnotationDesc[] annotations;
        boolean isPackage = false;
        if (doc instanceof ProgramElementDoc) {
            annotations = ((ProgramElementDoc) doc).annotations();
        } else if (doc instanceof PackageDoc) {
            annotations = ((PackageDoc) doc).annotations();
            isPackage = true;
        } else if (doc instanceof Parameter) {
            annotations = ((Parameter) doc).annotations();
        } else {
            throw new DocletAbortException();
        }
        for (int i = 0; i < annotations.length; i++) {
            AnnotationTypeDoc annotationDoc = annotations[i].annotationType();
            if (isPackage)
                refList(map, annotationDoc).add(holder);
            else
                add(map, annotationDoc, holder);
        }
    }
    private <T extends PackageDoc> void mapAnnotations(Map<String,List<T>> map, PackageDoc doc,
            T holder) {
        AnnotationDesc[] annotations;
        annotations = doc.annotations();
        for (int i = 0; i < annotations.length; i++) {
            AnnotationTypeDoc annotationDoc = annotations[i].annotationType();
            refList(map, annotationDoc).add(holder);
        }
    }
    private <T extends ProgramElementDoc> void addTypeParameterToMap(Map<String,List<T>> map, Type type,
            T holder) {
        if (type instanceof ClassDoc) {
            add(map, (ClassDoc) type, holder);
        } else if (type instanceof ParameterizedType) {
            add(map, ((ParameterizedType) type).asClassDoc(), holder);
        }
        mapTypeParameters(map, type, holder);
    }
}
