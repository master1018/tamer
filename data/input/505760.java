public class Variables
{
    private static final TopValue TOP_VALUE = new TopValue();
    protected Value[] values;
    protected int     size;
    public Variables(int size)
    {
        this.values = new Value[size];
        this.size   = size;
    }
    public Variables(Variables variables)
    {
        this(variables.size);
        initialize(variables);
    }
    public void reset(int size)
    {
        if (size > values.length)
        {
            values = new Value[size];
        }
        else
        {
            for (int index = 0; index < values.length; index++)
            {
                values[index] = null;
            }
        }
        this.size = size;
    }
    public void initialize(Variables other)
    {
        if (this.size < other.size)
        {
            throw new IllegalArgumentException("Variable frame is too small ["+this.size+"] compared to other frame ["+other.size+"]");
        }
        System.arraycopy(other.values, 0, this.values, 0, other.size);
    }
    public boolean generalize(Variables other,
                              boolean   clearConflictingOtherVariables)
    {
        if (this.size != other.size)
        {
            throw new IllegalArgumentException("Variable frames have different sizes ["+this.size+"] and ["+other.size+"]");
        }
        boolean changed = false;
        for (int index = 0; index < size; index++)
        {
            Value thisValue  = this.values[index];
            Value otherValue = other.values[index];
            if (thisValue  != null &&
                otherValue != null &&
                thisValue.computationalType() == otherValue.computationalType())
            {
                Value newValue = thisValue.generalize(otherValue);
                changed = changed || !thisValue.equals(newValue);
                this.values[index] = newValue;
            }
            else
            {
                changed = changed || thisValue != null;
                this.values[index] = null;
                if (clearConflictingOtherVariables)
                {
                    other.values[index] = null;
                }
            }
        }
        return changed;
    }
    public int size()
    {
        return size;
    }
    public Value getValue(int index)
    {
        if (index < 0 ||
            index >= size)
        {
            throw new IndexOutOfBoundsException("Variable index ["+index+"] out of bounds ["+size+"]");
        }
        return values[index];
    }
    public void store(int index, Value value)
    {
        if (index < 0 ||
            index >= size)
        {
            throw new IndexOutOfBoundsException("Variable index ["+index+"] out of bounds ["+size+"]");
        }
        values[index] = value;
        if (value.isCategory2())
        {
            values[index + 1] = TOP_VALUE;
        }
    }
    public Value load(int index)
    {
        if (index < 0 ||
            index >= size)
        {
            throw new IndexOutOfBoundsException("Variable index ["+index+"] out of bounds ["+size+"]");
        }
        return values[index];
    }
    public IntegerValue iload(int index)
    {
        return load(index).integerValue();
    }
    public LongValue lload(int index)
    {
        return load(index).longValue();
    }
    public FloatValue fload(int index)
    {
        return load(index).floatValue();
    }
    public DoubleValue dload(int index)
    {
        return load(index).doubleValue();
    }
    public ReferenceValue aload(int index)
    {
        return load(index).referenceValue();
    }
    public InstructionOffsetValue oload(int index)
    {
        return load(index).instructionOffsetValue();
    }
    public boolean equals(Object object)
    {
        if (object == null ||
            this.getClass() != object.getClass())
        {
            return false;
        }
        Variables other = (Variables)object;
        if (this.size != other.size)
        {
            return false;
        }
        for (int index = 0; index < size; index++)
        {
            Value thisValue  = this.values[index];
            Value otherValue = other.values[index];
            if (thisValue  != null &&
                otherValue != null &&
                thisValue.computationalType() == otherValue.computationalType() &&
                !thisValue.equals(otherValue))
            {
                return false;
            }
        }
        return true;
    }
    public int hashCode()
    {
        int hashCode = size;
        for (int index = 0; index < size; index++)
        {
            Value value = values[index];
            if (value != null)
            {
                hashCode ^= value.hashCode();
            }
        }
        return hashCode;
    }
    public String toString()
    {
        StringBuffer buffer = new StringBuffer();
        for (int index = 0; index < size; index++)
        {
            Value value = values[index];
            buffer = buffer.append('[')
                           .append(value == null ? "empty" : value.toString())
                           .append(']');
        }
        return buffer.toString();
    }
}
