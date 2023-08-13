class SerializedForm {
    ListBuffer<MethodDoc> methods = new ListBuffer<MethodDoc>();
    private final ListBuffer<FieldDocImpl> fields = new ListBuffer<FieldDocImpl>();
    private boolean definesSerializableFields = false;
    private static final String SERIALIZABLE_FIELDS = "serialPersistentFields";
    private static final String READOBJECT  = "readObject";
    private static final String WRITEOBJECT = "writeObject";
    private static final String READRESOLVE  = "readResolve";
    private static final String WRITEREPLACE = "writeReplace";
    private static final String READOBJECTNODATA = "readObjectNoData";
    SerializedForm(DocEnv env, ClassSymbol def, ClassDocImpl cd) {
        if (cd.isExternalizable()) {
            String[] readExternalParamArr = { "java.io.ObjectInput" };
            String[] writeExternalParamArr = { "java.io.ObjectOutput" };
            MethodDoc md = cd.findMethod("readExternal", readExternalParamArr);
            if (md != null) {
                methods.append(md);
            }
            md = cd.findMethod("writeExternal", writeExternalParamArr);
            if (md != null) {
                methods.append(md);
                Tag tag[] = md.tags("serialData");
            }
        } else if (cd.isSerializable()) {
            VarSymbol dsf = getDefinedSerializableFields(def);
            if (dsf != null) {
                definesSerializableFields = true;
                FieldDocImpl dsfDoc = env.getFieldDoc(dsf);
                fields.append(dsfDoc);
                mapSerialFieldTagImplsToFieldDocImpls(dsfDoc, env, def);
            } else {
                computeDefaultSerializableFields(env, def, cd);
            }
            addMethodIfExist(env, def, READOBJECT);
            addMethodIfExist(env, def, WRITEOBJECT);
            addMethodIfExist(env, def, READRESOLVE);
            addMethodIfExist(env, def, WRITEREPLACE);
            addMethodIfExist(env, def, READOBJECTNODATA);
        }
    }
    private VarSymbol getDefinedSerializableFields(ClassSymbol def) {
        Names names = def.name.table.names;
        for (Scope.Entry e = def.members().lookup(names.fromString(SERIALIZABLE_FIELDS)); e.scope != null; e = e.next()) {
            if (e.sym.kind == Kinds.VAR) {
                VarSymbol f = (VarSymbol)e.sym;
                if ((f.flags() & Flags.STATIC) != 0 &&
                    (f.flags() & Flags.PRIVATE) != 0) {
                    return f;
                }
            }
        }
        return null;
    }
    private void computeDefaultSerializableFields(DocEnv env,
                                                  ClassSymbol def,
                                                  ClassDocImpl cd) {
        for (Scope.Entry e = def.members().elems; e != null; e = e.sibling) {
            if (e.sym != null && e.sym.kind == Kinds.VAR) {
                VarSymbol f = (VarSymbol)e.sym;
                if ((f.flags() & Flags.STATIC) == 0 &&
                    (f.flags() & Flags.TRANSIENT) == 0) {
                    FieldDocImpl fd = env.getFieldDoc(f);
                    fields.prepend(fd);
                }
            }
        }
    }
    private void addMethodIfExist(DocEnv env, ClassSymbol def, String methodName) {
        Names names = def.name.table.names;
        for (Scope.Entry e = def.members().lookup(names.fromString(methodName)); e.scope != null; e = e.next()) {
            if (e.sym.kind == Kinds.MTH) {
                MethodSymbol md = (MethodSymbol)e.sym;
                if ((md.flags() & Flags.STATIC) == 0) {
                    methods.append(env.getMethodDoc(md));
                }
            }
        }
    }
    private void mapSerialFieldTagImplsToFieldDocImpls(FieldDocImpl spfDoc,
                                                       DocEnv env,
                                                       ClassSymbol def) {
        Names names = def.name.table.names;
        SerialFieldTag[] sfTag = spfDoc.serialFieldTags();
        for (int i = 0; i < sfTag.length; i++) {
            Name fieldName = names.fromString(sfTag[i].fieldName());
            for (Scope.Entry e = def.members().lookup(fieldName); e.scope != null; e = e.next()) {
                if (e.sym.kind == Kinds.VAR) {
                    VarSymbol f = (VarSymbol)e.sym;
                    FieldDocImpl fdi = env.getFieldDoc(f);
                    ((SerialFieldTagImpl)(sfTag[i])).mapToFieldDocImpl(fdi);
                    break;
                }
            }
        }
    }
    FieldDoc[] fields() {
        return (FieldDoc[])fields.toArray(new FieldDocImpl[fields.length()]);
    }
    MethodDoc[] methods() {
        return methods.toArray(new MethodDoc[methods.length()]);
    }
    boolean definesSerializableFields() {
        return definesSerializableFields;
    }
}
