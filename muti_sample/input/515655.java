public final class Merger {
    private Merger() {
    }
    public static OneLocalsArray mergeLocals(OneLocalsArray locals1,
                                          OneLocalsArray locals2) {
        if (locals1 == locals2) {
            return locals1;
        }
        int sz = locals1.getMaxLocals();
        OneLocalsArray result = null;
        if (locals2.getMaxLocals() != sz) {
            throw new SimException("mismatched maxLocals values");
        }
        for (int i = 0; i < sz; i++) {
            TypeBearer tb1 = locals1.getOrNull(i);
            TypeBearer tb2 = locals2.getOrNull(i);
            TypeBearer resultType = mergeType(tb1, tb2);
            if (resultType != tb1) {
                if (result == null) {
                    result = locals1.copy();
                }
                if (resultType == null) {
                    result.invalidate(i);
                } else {
                    result.set(i, resultType);
                }
            }
        }
        if (result == null) {
            return locals1;
        }
        result.setImmutable();
        return result;
    }
    public static ExecutionStack mergeStack(ExecutionStack stack1,
                                            ExecutionStack stack2) {
        if (stack1 == stack2) {
            return stack1;
        }
        int sz = stack1.size();
        ExecutionStack result = null;
        if (stack2.size() != sz) {
            throw new SimException("mismatched stack depths");
        }
        for (int i = 0; i < sz; i++) {
            TypeBearer tb1 = stack1.peek(i);
            TypeBearer tb2 = stack2.peek(i);
            TypeBearer resultType = mergeType(tb1, tb2);
            if (resultType != tb1) {
                if (result == null) {
                    result = stack1.copy();
                }
                try {
                    if (resultType == null) {
                        throw new SimException("incompatible: " + tb1 + ", " +
                                               tb2);
                    } else {
                        result.change(i, resultType);
                    }
                } catch (SimException ex) {
                    ex.addContext("...while merging stack[" + Hex.u2(i) + "]");
                    throw ex;
                }
            }
        }
        if (result == null) {
            return stack1;
        }
        result.setImmutable();
        return result;
    }
    public static TypeBearer mergeType(TypeBearer ft1, TypeBearer ft2) {
        if ((ft1 == null) || ft1.equals(ft2)) {
            return ft1;
        } else if (ft2 == null) {
            return null;
        } else {
            Type type1 = ft1.getType();
            Type type2 = ft2.getType();
            if (type1 == type2) {
                return type1;
            } else if (type1.isReference() && type2.isReference()) {
                if (type1 == Type.KNOWN_NULL) {
                    return type2;
                } else if (type2 == Type.KNOWN_NULL) {
                    return type1;
                } else if (type1.isArray() && type2.isArray()) {
                    TypeBearer componentUnion =
                        mergeType(type1.getComponentType(),
                                type2.getComponentType());
                    if (componentUnion == null) {
                        return Type.OBJECT;
                    }
                    return ((Type) componentUnion).getArrayType();
                } else {
                    return Type.OBJECT;
                }
            } else if (type1.isIntlike() && type2.isIntlike()) {
                return Type.INT;
            } else {
                return null;
            }
        }
    }
    public static boolean isPossiblyAssignableFrom(TypeBearer supertypeBearer,
            TypeBearer subtypeBearer) {
        Type supertype = supertypeBearer.getType();
        Type subtype = subtypeBearer.getType();
        if (supertype.equals(subtype)) {
            return true;
        }
        int superBt = supertype.getBasicType();
        int subBt = subtype.getBasicType();
        if (superBt == Type.BT_ADDR) {
            supertype = Type.OBJECT;
            superBt = Type.BT_OBJECT;
        }
        if (subBt == Type.BT_ADDR) {
            subtype = Type.OBJECT;
            subBt = Type.BT_OBJECT;
        }
        if ((superBt != Type.BT_OBJECT) || (subBt != Type.BT_OBJECT)) {
            return supertype.isIntlike() && subtype.isIntlike();
        }
        if (supertype == Type.KNOWN_NULL) {
            return false;
        } else if (subtype == Type.KNOWN_NULL) {
            return true;
        } else if (supertype == Type.OBJECT) {
            return true;
        } else if (supertype.isArray()) {
            if (! subtype.isArray()) {
                return false;
            }
            do {
                supertype = supertype.getComponentType();
                subtype = subtype.getComponentType();
            } while (supertype.isArray() && subtype.isArray());
            return isPossiblyAssignableFrom(supertype, subtype);
        } else if (subtype.isArray()) {
            return (supertype == Type.SERIALIZABLE) ||
                (supertype == Type.CLONEABLE);
        } else {
            return true;
        }
    }
}
