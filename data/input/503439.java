public class AnnotationMember implements Serializable {
    protected static final char ERROR = '!';
    protected static final char ARRAY = '[';
    protected static final char OTHER = '*';
    private enum DefaultValues {NO_VALUE}
    protected static final Object NO_VALUE = DefaultValues.NO_VALUE;
    protected final String name;
    protected final Object value; 
    protected final char tag; 
    protected transient Class<?> elementType; 
    protected transient Method definingMethod;
    public AnnotationMember(String name, Object val) {
        this.name = name;
        value = val == null ? NO_VALUE : val;
        if (value instanceof Throwable) {
            tag = ERROR;
        } else if (value.getClass().isArray()) {
            tag = ARRAY;
        } else {
            tag = OTHER;
        }
    }
    public AnnotationMember(String name, Object val, Class type, Method m) {
        this(name, val);
        definingMethod = m;
        if (type == int.class) {
            elementType = Integer.class;
        } else if (type == boolean.class) {
            elementType = Boolean.class;
        } else if (type == char.class) {
            elementType = Character.class;
        } else if (type == float.class) {
            elementType = Float.class;
        } else if (type == double.class) {
            elementType = Double.class;
        } else if (type == long.class) {
            elementType = Long.class;
        } else if (type == short.class) {
            elementType = Short.class;
        } else if (type == byte.class) {
            elementType = Byte.class;
        } else {
            elementType = type;
        }
    }
    protected AnnotationMember setDefinition(AnnotationMember copy) {
        definingMethod = copy.definingMethod;
        elementType = copy.elementType;
        return this;
    }
    public String toString() {
        if (tag == ARRAY) {
            StringBuilder sb = new StringBuilder(80);
            sb.append(name).append("=[");
            int len = Array.getLength(value);
            for (int i = 0; i < len; i++) {
                if (i != 0) sb.append(", ");
                sb.append(Array.get(value, i));
            }
            return sb.append("]").toString();
        } else {
            return name+ "=" +value;
        }
    }
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof AnnotationMember) {
            AnnotationMember that = (AnnotationMember)obj;
            if (name.equals(that.name) && tag == that.tag) {
                if (tag == ARRAY) {
                    return equalArrayValue(that.value);
                } else if (tag == ERROR) {
                    return false;
                } else {
                    return value.equals(that.value);
                }
            }
        }
        return false;
    }
    public boolean equalArrayValue(Object otherValue) {
        if (value instanceof Object[] && otherValue instanceof Object[]) {
            return Arrays.equals((Object[])value, (Object[])otherValue);
        }
        Class type = value.getClass();
        if (type != otherValue.getClass()) {
            return false;
        }
        if (type == int[].class) {
            return Arrays.equals((int[])value, (int[])otherValue);
        } else if (type == byte[].class) {
            return Arrays.equals((byte[])value, (byte[])otherValue);
        } else if (type == short[].class) {
            return Arrays.equals((short[])value, (short[])otherValue);
        } else if (type == long[].class) {
            return Arrays.equals((long[])value, (long[])otherValue);
        } else if (type == char[].class) {
            return Arrays.equals((char[])value, (char[])otherValue);
        } else if (type == boolean[].class) {
            return Arrays.equals((boolean[])value, (boolean[])otherValue);
        } else if (type == float[].class) {
            return Arrays.equals((float[])value, (float[])otherValue);
        } else if (type == double[].class) {
            return Arrays.equals((double[])value, (double[])otherValue);
        }
        return false;
    }
    public int hashCode() {
        int hash = name.hashCode() * 127;
        if (tag == ARRAY) {
            Class type = value.getClass();
            if (type == int[].class) {
                return hash ^ Arrays.hashCode((int[])value);
            } else if (type == byte[].class) {
                return hash ^ Arrays.hashCode((byte[])value);
            } else if (type == short[].class) {
                return hash ^ Arrays.hashCode((short[])value);
            } else if (type == long[].class) {
                return hash ^ Arrays.hashCode((long[])value);
            } else if (type == char[].class) {
                return hash ^ Arrays.hashCode((char[])value);
            } else if (type == boolean[].class) {
                return hash ^ Arrays.hashCode((boolean[])value);
            } else if (type == float[].class) {
                return hash ^ Arrays.hashCode((float[])value);
            } else if (type == double[].class) {
                return hash ^ Arrays.hashCode((double[])value);
            }
            return hash ^ Arrays.hashCode((Object[])value);
        } else {
            return hash ^ value.hashCode();
        }
    }
    public void rethrowError() throws Throwable {
        if (tag == ERROR) {
            if (value instanceof TypeNotPresentException) {
                TypeNotPresentException tnpe = (TypeNotPresentException)value;
                throw new TypeNotPresentException(tnpe.typeName(), tnpe.getCause());
            } else if (value instanceof EnumConstantNotPresentException) {
                EnumConstantNotPresentException ecnpe = (EnumConstantNotPresentException)value;
                throw new EnumConstantNotPresentException(ecnpe.enumType(), ecnpe.constantName());
            } else if (value instanceof ArrayStoreException) {
                ArrayStoreException ase = (ArrayStoreException)value;
                throw new ArrayStoreException(ase.getMessage());
            }
            Throwable error = (Throwable)value;
            StackTraceElement[] ste = error.getStackTrace();
            ByteArrayOutputStream bos = new ByteArrayOutputStream(
                    ste == null ? 512 : (ste.length + 1) * 80);
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(error);
            oos.flush();
            oos.close();
            ByteArrayInputStream bis = new ByteArrayInputStream(bos
                    .toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bis);
            error = (Throwable)ois.readObject();
            ois.close();
            throw error;
        }
    }
    public Object validateValue() throws Throwable {
        if (tag == ERROR) {
            rethrowError();
        } 
        if (value == NO_VALUE) {
            return null;
        }
        if (elementType == value.getClass() 
                || elementType.isInstance(value)) { 
            return copyValue();
        } else {
            throw new AnnotationTypeMismatchException(definingMethod, 
                    value.getClass().getName());
        }
    }        
    public Object copyValue() throws Throwable
    {
        if (tag != ARRAY || Array.getLength(value) == 0) {
            return value;
        }
        Class type = value.getClass();
        if (type == int[].class) {
            return ((int[])value).clone();
        } else if (type == byte[].class) {
            return ((byte[])value).clone();
        } else if (type == short[].class) {
            return ((short[])value).clone();
        } else if (type == long[].class) {
            return ((long[])value).clone();
        } else if (type == char[].class) {
            return ((char[])value).clone();
        } else if (type == boolean[].class) {
            return ((boolean[])value).clone();
        } else if (type == float[].class) {
            return ((float[])value).clone();
        } else if (type == double[].class) {
            return ((double[])value).clone();
        }
        return ((Object[])value).clone();
    }
}
