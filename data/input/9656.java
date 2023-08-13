abstract class DynAnyConstructedImpl extends DynAnyImpl
{
    protected static final byte REPRESENTATION_NONE = 0;
    protected static final byte REPRESENTATION_TYPECODE = 1;
    protected static final byte REPRESENTATION_ANY = 2;
    protected static final byte REPRESENTATION_COMPONENTS = 4;
    protected static final byte RECURSIVE_UNDEF = -1;
    protected static final byte RECURSIVE_NO = 0;
    protected static final byte RECURSIVE_YES = 1;
    protected static final DynAny[] emptyComponents = new DynAny[0];
    DynAny[] components = emptyComponents;
    byte representations = REPRESENTATION_NONE;
    byte isRecursive = RECURSIVE_UNDEF;
    private DynAnyConstructedImpl() {
        this(null, (Any)null, false);
    }
    protected DynAnyConstructedImpl(ORB orb, Any any, boolean copyValue) {
        super(orb, any, copyValue);
        if (this.any != null) {
            representations = REPRESENTATION_ANY;
        }
        index = 0;
    }
    protected DynAnyConstructedImpl(ORB orb, TypeCode typeCode) {
        super(orb, typeCode);
        if (typeCode != null) {
            representations = REPRESENTATION_TYPECODE;
        }
        index = NO_INDEX;
    }
    protected boolean isRecursive() {
        if (isRecursive == RECURSIVE_UNDEF) {
            TypeCode typeCode = any.type();
            if (typeCode instanceof TypeCodeImpl) {
                if (((TypeCodeImpl)typeCode).is_recursive())
                    isRecursive = RECURSIVE_YES;
                else
                    isRecursive = RECURSIVE_NO;
            } else {
                isRecursive = RECURSIVE_NO;
            }
        }
        return (isRecursive == RECURSIVE_YES);
    }
    public org.omg.DynamicAny.DynAny current_component()
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch
    {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed() ;
        }
        if (index == NO_INDEX) {
            return null;
        }
        return (checkInitComponents() ? components[index] : null);
    }
    public int component_count() {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed() ;
        }
        return (checkInitComponents() ? components.length : 0);
    }
    public boolean next() {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed() ;
        }
        if (checkInitComponents() == false) {
            return false;
        }
        index++;
        if (index >= 0 && index < components.length) {
            return true;
        } else {
            index = NO_INDEX;
            return false;
        }
    }
    public boolean seek(int newIndex) {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed() ;
        }
        if (newIndex < 0) {
            this.index = NO_INDEX;
            return false;
        }
        if (checkInitComponents() == false) {
            return false;
        }
        if (newIndex < components.length) {
            index = newIndex;
            return true;
        }
        return false;
    }
    public void rewind() {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed() ;
        }
        this.seek(0);
    }
    protected void clearData() {
        super.clearData();
        components = emptyComponents;
        index = NO_INDEX;
        representations = REPRESENTATION_NONE;
    }
    protected void writeAny(OutputStream out) {
        checkInitAny();
        super.writeAny(out);
    }
    protected boolean checkInitComponents() {
        if ((representations & REPRESENTATION_COMPONENTS) == 0) {
            if ((representations & REPRESENTATION_ANY) != 0) {
                if (initializeComponentsFromAny()) {
                    representations |= REPRESENTATION_COMPONENTS;
                } else {
                    return false;
                }
            } else if ((representations & REPRESENTATION_TYPECODE) != 0) {
                if (initializeComponentsFromTypeCode()) {
                    representations |= REPRESENTATION_COMPONENTS;
                } else {
                    return false;
                }
            }
        }
        return true;
    }
    protected void checkInitAny() {
        if ((representations & REPRESENTATION_ANY) == 0) {
            if ((representations & REPRESENTATION_COMPONENTS) != 0) {
                if (initializeAnyFromComponents()) {
                    representations |= REPRESENTATION_ANY;
                }
            } else if ((representations & REPRESENTATION_TYPECODE) != 0) {
                if (representations == REPRESENTATION_TYPECODE && isRecursive())
                    return;
                if (initializeComponentsFromTypeCode()) {
                    representations |= REPRESENTATION_COMPONENTS;
                }
                if (initializeAnyFromComponents()) {
                    representations |= REPRESENTATION_ANY;
                }
            }
        } else {
        }
        return;
    }
    protected abstract boolean initializeComponentsFromAny();
    protected abstract boolean initializeComponentsFromTypeCode();
    protected boolean initializeAnyFromComponents() {
        OutputStream out = any.create_output_stream();
        for (int i=0; i<components.length; i++) {
            if (components[i] instanceof DynAnyImpl) {
                ((DynAnyImpl)components[i]).writeAny(out);
            } else {
                components[i].to_any().write_value(out);
            }
        }
        any.read_value(out.create_input_stream(), any.type());
        return true;
    }
    public void assign (org.omg.DynamicAny.DynAny dyn_any)
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch
    {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed() ;
        }
        clearData();
        super.assign(dyn_any);
        representations = REPRESENTATION_ANY;
        index = 0;
    }
    public void from_any (org.omg.CORBA.Any value)
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch,
               org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed() ;
        }
        clearData();
        super.from_any(value);
        representations = REPRESENTATION_ANY;
        index = 0;
    }
    public org.omg.CORBA.Any to_any() {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed() ;
        }
        checkInitAny();
        return DynAnyUtil.copy(any, orb);
    }
    public boolean equal (org.omg.DynamicAny.DynAny dyn_any) {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed() ;
        }
        if (dyn_any == this) {
            return true;
        }
        if ( ! any.type().equal(dyn_any.type())) {
            return false;
        }
        if (checkInitComponents() == false) {
            return false;
        }
        DynAny currentComponent = null;
        try {
            currentComponent = dyn_any.current_component();
            for (int i=0; i<components.length; i++) {
                if (dyn_any.seek(i) == false)
                    return false;
                if ( ! components[i].equal(dyn_any.current_component())) {
                    return false;
                }
            }
        } catch (TypeMismatch tm) {
        } finally {
            DynAnyUtil.set_current_component(dyn_any, currentComponent);
        }
        return true;
    }
    public void destroy() {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed() ;
        }
        if (status == STATUS_DESTROYABLE) {
            status = STATUS_DESTROYED;
            for (int i=0; i<components.length; i++) {
                if (components[i] instanceof DynAnyImpl) {
                    ((DynAnyImpl)components[i]).setStatus(STATUS_DESTROYABLE);
                }
                components[i].destroy();
            }
        }
    }
    public org.omg.DynamicAny.DynAny copy() {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed() ;
        }
        checkInitAny();
        try {
            return DynAnyUtil.createMostDerivedDynAny(any, orb, true);
        } catch (InconsistentTypeCode ictc) {
            return null; 
        }
    }
    public void insert_boolean(boolean value)
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch,
               org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed() ;
        }
        if (index == NO_INDEX)
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
        DynAny currentComponent = current_component();
        if (DynAnyUtil.isConstructedDynAny(currentComponent))
            throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
        currentComponent.insert_boolean(value);
    }
    public void insert_octet(byte value)
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch,
               org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed() ;
        }
        if (index == NO_INDEX)
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
        DynAny currentComponent = current_component();
        if (DynAnyUtil.isConstructedDynAny(currentComponent))
            throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
        currentComponent.insert_octet(value);
    }
    public void insert_char(char value)
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch,
               org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed() ;
        }
        if (index == NO_INDEX)
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
        DynAny currentComponent = current_component();
        if (DynAnyUtil.isConstructedDynAny(currentComponent))
            throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
        currentComponent.insert_char(value);
    }
    public void insert_short(short value)
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch,
               org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed() ;
        }
        if (index == NO_INDEX)
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
        DynAny currentComponent = current_component();
        if (DynAnyUtil.isConstructedDynAny(currentComponent))
            throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
        currentComponent.insert_short(value);
    }
    public void insert_ushort(short value)
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch,
               org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed() ;
        }
        if (index == NO_INDEX)
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
        DynAny currentComponent = current_component();
        if (DynAnyUtil.isConstructedDynAny(currentComponent))
            throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
        currentComponent.insert_ushort(value);
    }
    public void insert_long(int value)
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch,
               org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed() ;
        }
        if (index == NO_INDEX)
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
        DynAny currentComponent = current_component();
        if (DynAnyUtil.isConstructedDynAny(currentComponent))
            throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
        currentComponent.insert_long(value);
    }
    public void insert_ulong(int value)
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch,
               org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed() ;
        }
        if (index == NO_INDEX)
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
        DynAny currentComponent = current_component();
        if (DynAnyUtil.isConstructedDynAny(currentComponent))
            throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
        currentComponent.insert_ulong(value);
    }
    public void insert_float(float value)
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch,
               org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed() ;
        }
        if (index == NO_INDEX)
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
        DynAny currentComponent = current_component();
        if (DynAnyUtil.isConstructedDynAny(currentComponent))
            throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
        currentComponent.insert_float(value);
    }
    public void insert_double(double value)
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch,
               org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed() ;
        }
        if (index == NO_INDEX)
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
        DynAny currentComponent = current_component();
        if (DynAnyUtil.isConstructedDynAny(currentComponent))
            throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
        currentComponent.insert_double(value);
    }
    public void insert_string(String value)
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch,
               org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed() ;
        }
        if (index == NO_INDEX)
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
        DynAny currentComponent = current_component();
        if (DynAnyUtil.isConstructedDynAny(currentComponent))
            throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
        currentComponent.insert_string(value);
    }
    public void insert_reference(org.omg.CORBA.Object value)
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch,
               org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed() ;
        }
        if (index == NO_INDEX)
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
        DynAny currentComponent = current_component();
        if (DynAnyUtil.isConstructedDynAny(currentComponent))
            throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
        currentComponent.insert_reference(value);
    }
    public void insert_typecode(org.omg.CORBA.TypeCode value)
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch,
               org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed() ;
        }
        if (index == NO_INDEX)
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
        DynAny currentComponent = current_component();
        if (DynAnyUtil.isConstructedDynAny(currentComponent))
            throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
        currentComponent.insert_typecode(value);
    }
    public void insert_longlong(long value)
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch,
               org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed() ;
        }
        if (index == NO_INDEX)
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
        DynAny currentComponent = current_component();
        if (DynAnyUtil.isConstructedDynAny(currentComponent))
            throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
        currentComponent.insert_longlong(value);
    }
    public void insert_ulonglong(long value)
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch,
               org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed() ;
        }
        if (index == NO_INDEX)
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
        DynAny currentComponent = current_component();
        if (DynAnyUtil.isConstructedDynAny(currentComponent))
            throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
        currentComponent.insert_ulonglong(value);
    }
    public void insert_wchar(char value)
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch,
               org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed() ;
        }
        if (index == NO_INDEX)
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
        DynAny currentComponent = current_component();
        if (DynAnyUtil.isConstructedDynAny(currentComponent))
            throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
        currentComponent.insert_wchar(value);
    }
    public void insert_wstring(String value)
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch,
               org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed() ;
        }
        if (index == NO_INDEX)
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
        DynAny currentComponent = current_component();
        if (DynAnyUtil.isConstructedDynAny(currentComponent))
            throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
        currentComponent.insert_wstring(value);
    }
    public void insert_any(org.omg.CORBA.Any value)
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch,
               org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed() ;
        }
        if (index == NO_INDEX)
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
        DynAny currentComponent = current_component();
        if (DynAnyUtil.isConstructedDynAny(currentComponent))
            throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
        currentComponent.insert_any(value);
    }
    public void insert_dyn_any (org.omg.DynamicAny.DynAny value)
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch,
               org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed() ;
        }
        if (index == NO_INDEX)
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
        DynAny currentComponent = current_component();
        if (DynAnyUtil.isConstructedDynAny(currentComponent))
            throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
        currentComponent.insert_dyn_any(value);
    }
    public void insert_val(java.io.Serializable value)
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch,
               org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed() ;
        }
        if (index == NO_INDEX)
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
        DynAny currentComponent = current_component();
        if (DynAnyUtil.isConstructedDynAny(currentComponent))
            throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
        currentComponent.insert_val(value);
    }
    public java.io.Serializable get_val()
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch,
               org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed() ;
        }
        if (index == NO_INDEX)
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
        DynAny currentComponent = current_component();
        if (DynAnyUtil.isConstructedDynAny(currentComponent))
            throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
        return currentComponent.get_val();
    }
    public boolean get_boolean()
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch,
               org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed() ;
        }
        if (index == NO_INDEX)
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
        DynAny currentComponent = current_component();
        if (DynAnyUtil.isConstructedDynAny(currentComponent))
            throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
        return currentComponent.get_boolean();
    }
    public byte get_octet()
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch,
               org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed() ;
        }
        if (index == NO_INDEX)
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
        DynAny currentComponent = current_component();
        if (DynAnyUtil.isConstructedDynAny(currentComponent))
            throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
        return currentComponent.get_octet();
    }
    public char get_char()
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch,
               org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed() ;
        }
        if (index == NO_INDEX)
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
        DynAny currentComponent = current_component();
        if (DynAnyUtil.isConstructedDynAny(currentComponent))
            throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
        return currentComponent.get_char();
    }
    public short get_short()
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch,
               org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed() ;
        }
        if (index == NO_INDEX)
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
        DynAny currentComponent = current_component();
        if (DynAnyUtil.isConstructedDynAny(currentComponent))
            throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
        return currentComponent.get_short();
    }
    public short get_ushort()
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch,
               org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed() ;
        }
        if (index == NO_INDEX)
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
        DynAny currentComponent = current_component();
        if (DynAnyUtil.isConstructedDynAny(currentComponent))
            throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
        return currentComponent.get_ushort();
    }
    public int get_long()
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch,
               org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed() ;
        }
        if (index == NO_INDEX)
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
        DynAny currentComponent = current_component();
        if (DynAnyUtil.isConstructedDynAny(currentComponent))
            throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
        return currentComponent.get_long();
    }
    public int get_ulong()
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch,
               org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed() ;
        }
        if (index == NO_INDEX)
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
        DynAny currentComponent = current_component();
        if (DynAnyUtil.isConstructedDynAny(currentComponent))
            throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
        return currentComponent.get_ulong();
    }
    public float get_float()
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch,
               org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed() ;
        }
        if (index == NO_INDEX)
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
        DynAny currentComponent = current_component();
        if (DynAnyUtil.isConstructedDynAny(currentComponent))
            throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
        return currentComponent.get_float();
    }
    public double get_double()
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch,
               org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed() ;
        }
        if (index == NO_INDEX)
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
        DynAny currentComponent = current_component();
        if (DynAnyUtil.isConstructedDynAny(currentComponent))
            throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
        return currentComponent.get_double();
    }
    public String get_string()
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch,
               org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed() ;
        }
        if (index == NO_INDEX)
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
        DynAny currentComponent = current_component();
        if (DynAnyUtil.isConstructedDynAny(currentComponent))
            throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
        return currentComponent.get_string();
    }
    public org.omg.CORBA.Object get_reference()
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch,
               org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed() ;
        }
        if (index == NO_INDEX)
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
        DynAny currentComponent = current_component();
        if (DynAnyUtil.isConstructedDynAny(currentComponent))
            throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
        return currentComponent.get_reference();
    }
    public org.omg.CORBA.TypeCode get_typecode()
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch,
               org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed() ;
        }
        if (index == NO_INDEX)
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
        DynAny currentComponent = current_component();
        if (DynAnyUtil.isConstructedDynAny(currentComponent))
            throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
        return currentComponent.get_typecode();
    }
    public long get_longlong()
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch,
               org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed() ;
        }
        if (index == NO_INDEX)
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
        DynAny currentComponent = current_component();
        if (DynAnyUtil.isConstructedDynAny(currentComponent))
            throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
        return currentComponent.get_longlong();
    }
    public long get_ulonglong()
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch,
               org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed() ;
        }
        if (index == NO_INDEX)
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
        DynAny currentComponent = current_component();
        if (DynAnyUtil.isConstructedDynAny(currentComponent))
            throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
        return currentComponent.get_ulonglong();
    }
    public char get_wchar()
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch,
               org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed() ;
        }
        if (index == NO_INDEX)
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
        DynAny currentComponent = current_component();
        if (DynAnyUtil.isConstructedDynAny(currentComponent))
            throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
        return currentComponent.get_wchar();
    }
    public String get_wstring()
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch,
               org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed() ;
        }
        if (index == NO_INDEX)
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
        DynAny currentComponent = current_component();
        if (DynAnyUtil.isConstructedDynAny(currentComponent))
            throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
        return currentComponent.get_wstring();
    }
    public org.omg.CORBA.Any get_any()
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch,
               org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed() ;
        }
        if (index == NO_INDEX)
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
        DynAny currentComponent = current_component();
        if (DynAnyUtil.isConstructedDynAny(currentComponent))
            throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
        return currentComponent.get_any();
    }
    public org.omg.DynamicAny.DynAny get_dyn_any()
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch,
               org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed() ;
        }
        if (index == NO_INDEX)
            throw new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
        DynAny currentComponent = current_component();
        if (DynAnyUtil.isConstructedDynAny(currentComponent))
            throw new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
        return currentComponent.get_dyn_any();
    }
}
