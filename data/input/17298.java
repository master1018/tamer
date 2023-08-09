public class DynAnyUtil
{
    static boolean isConsistentType(TypeCode typeCode) {
        int kind = typeCode.kind().value();
        return (kind != TCKind._tk_Principal &&
                kind != TCKind._tk_native &&
                kind != TCKind._tk_abstract_interface);
    }
    static boolean isConstructedDynAny(DynAny dynAny) {
        int kind = dynAny.type().kind().value();
        return (kind == TCKind._tk_sequence ||
                kind == TCKind._tk_struct ||
                kind == TCKind._tk_array ||
                kind == TCKind._tk_union ||
                kind == TCKind._tk_enum ||
                kind == TCKind._tk_fixed ||
                kind == TCKind._tk_value ||
                kind == TCKind._tk_value_box);
    }
    static DynAny createMostDerivedDynAny(Any any, ORB orb, boolean copyValue)
        throws org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode
    {
        if (any == null || ! DynAnyUtil.isConsistentType(any.type()))
            throw new org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode();
        switch (any.type().kind().value()) {
            case TCKind._tk_sequence:
                return new DynSequenceImpl(orb, any, copyValue);
            case TCKind._tk_struct:
                return new DynStructImpl(orb, any, copyValue);
            case TCKind._tk_array:
                return new DynArrayImpl(orb, any, copyValue);
            case TCKind._tk_union:
                return new DynUnionImpl(orb, any, copyValue);
            case TCKind._tk_enum:
                return new DynEnumImpl(orb, any, copyValue);
            case TCKind._tk_fixed:
                return new DynFixedImpl(orb, any, copyValue);
            case TCKind._tk_value:
                return new DynValueImpl(orb, any, copyValue);
            case TCKind._tk_value_box:
                return new DynValueBoxImpl(orb, any, copyValue);
            default:
                return new DynAnyBasicImpl(orb, any, copyValue);
        }
    }
    static DynAny createMostDerivedDynAny(TypeCode typeCode, ORB orb)
        throws org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode
    {
        if (typeCode == null || ! DynAnyUtil.isConsistentType(typeCode))
            throw new org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode();
        switch (typeCode.kind().value()) {
            case TCKind._tk_sequence:
                return new DynSequenceImpl(orb, typeCode);
            case TCKind._tk_struct:
                return new DynStructImpl(orb, typeCode);
            case TCKind._tk_array:
                return new DynArrayImpl(orb, typeCode);
            case TCKind._tk_union:
                return new DynUnionImpl(orb, typeCode);
            case TCKind._tk_enum:
                return new DynEnumImpl(orb, typeCode);
            case TCKind._tk_fixed:
                return new DynFixedImpl(orb, typeCode);
            case TCKind._tk_value:
                return new DynValueImpl(orb, typeCode);
            case TCKind._tk_value_box:
                return new DynValueBoxImpl(orb, typeCode);
            default:
                return new DynAnyBasicImpl(orb, typeCode);
        }
    }
    static Any extractAnyFromStream(TypeCode memberType, InputStream input, ORB orb) {
        return AnyImpl.extractAnyFromStream(memberType, input, orb);
    }
    static Any createDefaultAnyOfType(TypeCode typeCode, ORB orb) {
        ORBUtilSystemException wrapper = ORBUtilSystemException.get( orb,
            CORBALogDomains.RPC_PRESENTATION ) ;
        Any returnValue = orb.create_any();
        switch (typeCode.kind().value()) {
            case TCKind._tk_boolean:
                returnValue.insert_boolean(false);
                break;
            case TCKind._tk_short:
                returnValue.insert_short((short)0);
                break;
            case TCKind._tk_ushort:
                returnValue.insert_ushort((short)0);
                break;
            case TCKind._tk_long:
                returnValue.insert_long(0);
                break;
            case TCKind._tk_ulong:
                returnValue.insert_ulong(0);
                break;
            case TCKind._tk_longlong:
                returnValue.insert_longlong((long)0);
                break;
            case TCKind._tk_ulonglong:
                returnValue.insert_ulonglong((long)0);
                break;
            case TCKind._tk_float:
                returnValue.insert_float((float)0.0);
                break;
            case TCKind._tk_double:
                returnValue.insert_double((double)0.0);
                break;
            case TCKind._tk_octet:
                returnValue.insert_octet((byte)0);
                break;
            case TCKind._tk_char:
                returnValue.insert_char((char)0);
                break;
            case TCKind._tk_wchar:
                returnValue.insert_wchar((char)0);
                break;
            case TCKind._tk_string:
                returnValue.type(typeCode);
                returnValue.insert_string("");
                break;
            case TCKind._tk_wstring:
                returnValue.type(typeCode);
                returnValue.insert_wstring("");
                break;
            case TCKind._tk_objref:
                returnValue.insert_Object(null);
                break;
            case TCKind._tk_TypeCode:
                returnValue.insert_TypeCode(returnValue.type());
                break;
            case TCKind._tk_any:
                returnValue.insert_any(orb.create_any());
                break;
            case TCKind._tk_struct:
            case TCKind._tk_union:
            case TCKind._tk_enum:
            case TCKind._tk_sequence:
            case TCKind._tk_array:
            case TCKind._tk_except:
            case TCKind._tk_value:
            case TCKind._tk_value_box:
                returnValue.type(typeCode);
                break;
            case TCKind._tk_fixed:
                returnValue.insert_fixed(new BigDecimal("0.0"), typeCode);
                break;
            case TCKind._tk_native:
            case TCKind._tk_alias:
            case TCKind._tk_void:
            case TCKind._tk_Principal:
            case TCKind._tk_abstract_interface:
                returnValue.type(typeCode);
                break;
            case TCKind._tk_null:
                break;
            case TCKind._tk_longdouble:
                throw wrapper.tkLongDoubleNotSupported() ;
            default:
                throw wrapper.typecodeNotSupported() ;
        }
        return returnValue;
    }
    static Any copy(Any inAny, ORB orb) {
        return new AnyImpl(orb, inAny);
    }
    static DynAny convertToNative(DynAny dynAny, ORB orb) {
        if (dynAny instanceof DynAnyImpl) {
            return dynAny;
        } else {
            try {
                return createMostDerivedDynAny(dynAny.to_any(), orb, true);
            } catch (InconsistentTypeCode ictc) {
                return null;
            }
        }
    }
    static boolean isInitialized(Any any) {
        boolean isInitialized = ((AnyImpl)any).isInitialized();
        switch (any.type().kind().value()) {
            case TCKind._tk_string:
                return (isInitialized && (any.extract_string() != null));
            case TCKind._tk_wstring:
                return (isInitialized && (any.extract_wstring() != null));
        }
        return isInitialized;
    }
    static boolean set_current_component(DynAny dynAny, DynAny currentComponent) {
        if (currentComponent != null) {
            try {
                dynAny.rewind();
                do {
                    if (dynAny.current_component() == currentComponent)
                        return true;
                } while (dynAny.next());
            } catch (TypeMismatch tm) {  }
        }
        return false;
    }
}
