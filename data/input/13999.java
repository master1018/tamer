abstract class FieldAccessorImpl extends MagicAccessorImpl
    implements FieldAccessor {
    public abstract Object get(Object obj)
        throws IllegalArgumentException;
    public abstract boolean getBoolean(Object obj)
        throws IllegalArgumentException;
    public abstract byte getByte(Object obj)
        throws IllegalArgumentException;
    public abstract char getChar(Object obj)
        throws IllegalArgumentException;
    public abstract short getShort(Object obj)
        throws IllegalArgumentException;
    public abstract int getInt(Object obj)
        throws IllegalArgumentException;
    public abstract long getLong(Object obj)
        throws IllegalArgumentException;
    public abstract float getFloat(Object obj)
        throws IllegalArgumentException;
    public abstract double getDouble(Object obj)
        throws IllegalArgumentException;
    public abstract void set(Object obj, Object value)
        throws IllegalArgumentException, IllegalAccessException;
    public abstract void setBoolean(Object obj, boolean z)
        throws IllegalArgumentException, IllegalAccessException;
    public abstract void setByte(Object obj, byte b)
        throws IllegalArgumentException, IllegalAccessException;
    public abstract void setChar(Object obj, char c)
        throws IllegalArgumentException, IllegalAccessException;
    public abstract void setShort(Object obj, short s)
        throws IllegalArgumentException, IllegalAccessException;
    public abstract void setInt(Object obj, int i)
        throws IllegalArgumentException, IllegalAccessException;
    public abstract void setLong(Object obj, long l)
        throws IllegalArgumentException, IllegalAccessException;
    public abstract void setFloat(Object obj, float f)
        throws IllegalArgumentException, IllegalAccessException;
    public abstract void setDouble(Object obj, double d)
        throws IllegalArgumentException, IllegalAccessException;
}
