public class ValueUtility {
    public static final short PRIVATE_MEMBER = 0;
    public static final short PUBLIC_MEMBER = 1;
    private static final String primitiveConstants[] = {
        null,       
        null,           
        "S",            
        "I",            
        "S",            
        "I",            
        "F",            
        "D",            
        "Z",            
        "C",            
        "B",            
        null,           
        null,           
        null,           
        null,           
        null,           
        null,           
        null,           
        null,           
        null,           
        null,           
        null,           
        null,           
        "J",            
        "J",            
        "D",            
        "C",            
        null,           
        null,       
        null,       
        null,       
        null,       
        null,       
    };
    public static String getSignature(ValueMember member)
        throws ClassNotFoundException {
        if (member.type.kind().value() == TCKind._tk_value_box ||
            member.type.kind().value() == TCKind._tk_value ||
            member.type.kind().value() == TCKind._tk_objref) {
            Class c = RepositoryId.cache.getId(member.id).getClassFromType();
            return ObjectStreamClass.getSignature(c);
        } else {
            return primitiveConstants[member.type.kind().value()];
        }
    }
    public static FullValueDescription translate(ORB orb, ObjectStreamClass osc, ValueHandler vh){
        FullValueDescription result = new FullValueDescription();
        Class className = osc.forClass();
        ValueHandlerImpl vhandler = (com.sun.corba.se.impl.io.ValueHandlerImpl) vh;
        String repId = vhandler.createForAnyType(className);
        result.name = vhandler.getUnqualifiedName(repId);
        if (result.name == null)
            result.name = "";
        result.id = vhandler.getRMIRepositoryID(className);
        if (result.id == null)
            result.id = "";
        result.is_abstract = ObjectStreamClassCorbaExt.isAbstractInterface(className);
        result.is_custom = osc.hasWriteObject() || osc.isExternalizable();
        result.defined_in = vhandler.getDefinedInId(repId);
        if (result.defined_in == null)
            result.defined_in = "";
        result.version = vhandler.getSerialVersionUID(repId);
        if (result.version == null)
            result.version = "";
        result.operations = new OperationDescription[0];
        result.attributes = new AttributeDescription[0];
        IdentityKeyValueStack createdIDs = new IdentityKeyValueStack();
        result.members = translateMembers(orb, osc, vh, createdIDs);
        result.initializers = new Initializer[0];
        Class interfaces[] = osc.forClass().getInterfaces();
        int abstractCount = 0;
        result.supported_interfaces =  new String[interfaces.length];
        for (int interfaceIndex = 0; interfaceIndex < interfaces.length;
             interfaceIndex++) {
            result.supported_interfaces[interfaceIndex] =
                vhandler.createForAnyType(interfaces[interfaceIndex]);
            if ((!(java.rmi.Remote.class.isAssignableFrom(interfaces[interfaceIndex]))) ||
                (!Modifier.isPublic(interfaces[interfaceIndex].getModifiers())))
                abstractCount++;
        }
        result.abstract_base_values = new String[abstractCount];
        for (int interfaceIndex = 0; interfaceIndex < interfaces.length;
             interfaceIndex++) {
            if ((!(java.rmi.Remote.class.isAssignableFrom(interfaces[interfaceIndex]))) ||
                (!Modifier.isPublic(interfaces[interfaceIndex].getModifiers())))
                result.abstract_base_values[interfaceIndex] =
                    vhandler.createForAnyType(interfaces[interfaceIndex]);
        }
        result.is_truncatable = false;
        Class superClass = osc.forClass().getSuperclass();
        if (java.io.Serializable.class.isAssignableFrom(superClass))
            result.base_value = vhandler.getRMIRepositoryID(superClass);
        else
            result.base_value = "";
        result.type = orb.get_primitive_tc(TCKind.tk_value); 
        return result;
    }
    private static ValueMember[] translateMembers (ORB orb,
                                                   ObjectStreamClass osc,
                                                   ValueHandler vh,
                                                   IdentityKeyValueStack createdIDs)
    {
        ValueHandlerImpl vhandler = (com.sun.corba.se.impl.io.ValueHandlerImpl) vh;
        ObjectStreamField fields[] = osc.getFields();
        int fieldsLength = fields.length;
        ValueMember[] members = new ValueMember[fieldsLength];
        for (int i = 0; i < fieldsLength; i++) {
            String valRepId = vhandler.getRMIRepositoryID(fields[i].getClazz());
            members[i] = new ValueMember();
            members[i].name = fields[i].getName();
            members[i].id = valRepId; 
            members[i].defined_in = vhandler.getDefinedInId(valRepId);
            members[i].version = "1.0";
            members[i].type_def = new _IDLTypeStub(); 
            if (fields[i].getField() == null) {
                members[i].access = PRIVATE_MEMBER;
            } else {
                int m = fields[i].getField().getModifiers();
                if (Modifier.isPublic(m))
                    members[i].access = PUBLIC_MEMBER;
                else
                    members[i].access = PRIVATE_MEMBER;
            }
            switch (fields[i].getTypeCode()) {
            case 'B':
                members[i].type = orb.get_primitive_tc(TCKind.tk_octet); 
                break;
            case 'C':
                members[i].type
                    = orb.get_primitive_tc(vhandler.getJavaCharTCKind()); 
                break;
            case 'F':
                members[i].type = orb.get_primitive_tc(TCKind.tk_float); 
                break;
            case 'D' :
                members[i].type = orb.get_primitive_tc(TCKind.tk_double); 
                break;
            case 'I':
                members[i].type = orb.get_primitive_tc(TCKind.tk_long); 
                break;
            case 'J':
                members[i].type = orb.get_primitive_tc(TCKind.tk_longlong); 
                break;
            case 'S':
                members[i].type = orb.get_primitive_tc(TCKind.tk_short); 
                break;
            case 'Z':
                members[i].type = orb.get_primitive_tc(TCKind.tk_boolean); 
                break;
            default:
                members[i].type = createTypeCodeForClassInternal(orb, fields[i].getClazz(), vhandler,
                                  createdIDs);
                members[i].id = vhandler.createForAnyType(fields[i].getType());
                break;
            } 
        } 
        return members;
    }
    private static boolean exists(String str, String strs[]){
        for (int i = 0; i < strs.length; i++)
            if (str.equals(strs[i]))
                return true;
        return false;
    }
    public static boolean isAssignableFrom(String clzRepositoryId, FullValueDescription type,
                                           com.sun.org.omg.SendingContext.CodeBase sender){
        if (exists(clzRepositoryId, type.supported_interfaces))
            return true;
        if (clzRepositoryId.equals(type.id))
            return true;
        if ((type.base_value != null) &&
            (!type.base_value.equals(""))) {
            FullValueDescription parent = sender.meta(type.base_value);
            return isAssignableFrom(clzRepositoryId, parent, sender);
        }
        return false;
    }
    public static TypeCode createTypeCodeForClass (ORB orb, java.lang.Class c, ValueHandler vh) {
        IdentityKeyValueStack createdIDs = new IdentityKeyValueStack();
        TypeCode tc = createTypeCodeForClassInternal(orb, c, vh, createdIDs);
        return tc;
    }
    private static TypeCode createTypeCodeForClassInternal (ORB orb,
                                                            java.lang.Class c,
                                                            ValueHandler vh,
                                                            IdentityKeyValueStack createdIDs)
    {
        TypeCode tc = null;
        String id = (String)createdIDs.get(c);
        if (id != null) {
            return orb.create_recursive_tc(id);
        } else {
            id = vh.getRMIRepositoryID(c);
            if (id == null) id = "";
            createdIDs.push(c, id);
            tc = createTypeCodeInternal(orb, c, vh, id, createdIDs);
            createdIDs.pop();
            return tc;
        }
    }
    private static class IdentityKeyValueStack {
        private static class KeyValuePair {
            Object key;
            Object value;
            KeyValuePair(Object key, Object value) {
                this.key = key;
                this.value = value;
            }
            boolean equals(KeyValuePair pair) {
                return pair.key == this.key;
            }
        }
        Stack pairs = null;
        Object get(Object key) {
            if (pairs == null) {
                return null;
            }
            for (Iterator i = pairs.iterator(); i.hasNext();) {
                KeyValuePair pair = (KeyValuePair)i.next();
                if (pair.key == key) {
                    return pair.value;
                }
            }
            return null;
        }
        void push(Object key, Object value) {
            if (pairs == null) {
                pairs = new Stack();
            }
            pairs.push(new KeyValuePair(key, value));
        }
        void pop() {
            pairs.pop();
        }
    }
    private static TypeCode createTypeCodeInternal (ORB orb,
                                                    java.lang.Class c,
                                                    ValueHandler vh,
                                                    String id,
                                                    IdentityKeyValueStack createdIDs)
    {
        if ( c.isArray() ) {
            Class componentClass = c.getComponentType();
            TypeCode embeddedType;
            if ( componentClass.isPrimitive() ){
                embeddedType
                    = ValueUtility.getPrimitiveTypeCodeForClass(orb,
                                                                componentClass,
                                                                vh);
            } else {
                embeddedType = createTypeCodeForClassInternal(orb, componentClass, vh,
                                                              createdIDs);
            }
            TypeCode t = orb.create_sequence_tc (0, embeddedType);
            return orb.create_value_box_tc (id, "Sequence", t);
        } else if ( c == java.lang.String.class ) {
            TypeCode t = orb.create_string_tc (0);
            return orb.create_value_box_tc (id, "StringValue", t);
        } else if (java.rmi.Remote.class.isAssignableFrom(c)) {
            return orb.get_primitive_tc(TCKind.tk_objref);
        } else if (org.omg.CORBA.Object.class.isAssignableFrom(c)) {
            return orb.get_primitive_tc(TCKind.tk_objref);
        }
        ObjectStreamClass osc = ObjectStreamClass.lookup(c);
        if (osc == null) {
            return orb.create_value_box_tc (id, "Value", orb.get_primitive_tc (TCKind.tk_value));
        }
        short modifier = (osc.isCustomMarshaled() ? org.omg.CORBA.VM_CUSTOM.value : org.omg.CORBA.VM_NONE.value);
        TypeCode base = null;
        Class superClass = c.getSuperclass();
        if (superClass != null && java.io.Serializable.class.isAssignableFrom(superClass)) {
            base = createTypeCodeForClassInternal(orb, superClass, vh, createdIDs);
        }
        ValueMember[] members = translateMembers (orb, osc, vh, createdIDs);
        return orb.create_value_tc(id, c.getName(), modifier, base, members);
    }
    public static TypeCode getPrimitiveTypeCodeForClass (ORB orb,
                                                         Class c,
                                                         ValueHandler vh) {
        if (c == Integer.TYPE) {
            return orb.get_primitive_tc (TCKind.tk_long);
        } else if (c == Byte.TYPE) {
            return orb.get_primitive_tc (TCKind.tk_octet);
        } else if (c == Long.TYPE) {
            return orb.get_primitive_tc (TCKind.tk_longlong);
        } else if (c == Float.TYPE) {
            return orb.get_primitive_tc (TCKind.tk_float);
        } else if (c == Double.TYPE) {
            return orb.get_primitive_tc (TCKind.tk_double);
        } else if (c == Short.TYPE) {
            return orb.get_primitive_tc (TCKind.tk_short);
        } else if (c == Character.TYPE) {
            return orb.get_primitive_tc (((ValueHandlerImpl)vh).getJavaCharTCKind());
        } else if (c == Boolean.TYPE) {
            return orb.get_primitive_tc (TCKind.tk_boolean);
        } else {
            return orb.get_primitive_tc (TCKind.tk_any);
        }
    }
}
