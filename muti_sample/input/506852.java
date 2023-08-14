import proguard.classfile.ClassConstants;
public class InstructionOffsetValue extends Category1Value
{
    public static final InstructionOffsetValue EMPTY_VALUE = new InstructionOffsetValue();
    private int[] values;
    private InstructionOffsetValue()
    {
    }
    public InstructionOffsetValue(int value)
    {
        this.values = new int[] { value };
    }
    public InstructionOffsetValue(int[] values)
    {
        this.values = values;
    }
    public int instructionOffsetCount()
    {
        return values == null ? 0 : values.length;
    }
    public int instructionOffset(int index)
    {
        return values[index];
    }
    public boolean contains(int value)
    {
        if (values != null)
        {
            for (int index = 0; index < values.length; index++)
            {
                if (values[index] == value)
                {
                    return true;
                }
            }
        }
        return false;
    }
    public int minimumValue()
    {
        int minimumValue = Integer.MAX_VALUE;
        if (values != null)
        {
            for (int index = 0; index < values.length; index++)
            {
                int value = values[index];
                if (minimumValue > value)
                {
                    minimumValue = value;
                }
            }
        }
        return minimumValue;
    }
    public int maximumValue()
    {
        int maximumValue = Integer.MIN_VALUE;
        if (values != null)
        {
            for (int index = 0; index < values.length; index++)
            {
                int value = values[index];
                if (maximumValue < value)
                {
                    maximumValue = value;
                }
            }
        }
        return maximumValue;
    }
    public final Value generalize(InstructionOffsetValue other)
    {
        if (this.values == null)
        {
            return other;
        }
        if (other.values == null)
        {
            return this;
        }
        int newLength = this.values.length;
        for (int index = 0; index < other.values.length; index++)
        {
            if (!this.contains(other.values[index]))
            {
                newLength++;
            }
        }
        if (newLength == other.values.length)
        {
            return other;
        }
        int[] newValues = new int[newLength];
        int newIndex = 0;
        for (int index = 0; index < this.values.length; index++)
        {
            if (!other.contains(this.values[index]))
            {
                newValues[newIndex++] = this.values[index];
            }
        }
        for (int index = 0; index < other.values.length; index++)
        {
            newValues[newIndex++] = other.values[index];
        }
        return new InstructionOffsetValue(newValues);
    }
    public final InstructionOffsetValue instructionOffsetValue()
    {
        return this;
    }
    public boolean isSpecific()
    {
        return true;
    }
    public boolean isParticular()
    {
        return true;
    }
    public final Value generalize(Value other)
    {
        return this.generalize(other.instructionOffsetValue());
    }
    public final int computationalType()
    {
        return TYPE_INSTRUCTION_OFFSET;
    }
    public final String internalType()
    {
        return String.valueOf(ClassConstants.INTERNAL_TYPE_INT);
    }
    public boolean equals(Object object)
    {
        if (object == null ||
            this.getClass() != object.getClass())
        {
            return false;
        }
        InstructionOffsetValue other = (InstructionOffsetValue)object;
        if (this.values == other.values)
        {
            return true;
        }
        if (this.values  == null ||
            other.values == null ||
            this.values.length != other.values.length)
        {
            return false;
        }
        for (int index = 0; index < other.values.length; index++)
        {
            if (!this.contains(other.values[index]))
            {
                return false;
            }
        }
        return true;
    }
    public int hashCode()
    {
        int hashCode = this.getClass().hashCode();
        if (values != null)
        {
            for (int index = 0; index < values.length; index++)
            {
                hashCode ^= values[index];
            }
        }
        return hashCode;
    }
    public String toString()
    {
        StringBuffer buffer = new StringBuffer();
        if (values != null)
        {
            for (int index = 0; index < values.length; index++)
            {
                if (index > 0)
                {
                    buffer.append(',');
                }
                buffer.append(values[index]);
            }
        }
        return buffer.append(':').toString();
    }
}
