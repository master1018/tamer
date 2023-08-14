public class Stack
{
    private static final TopValue TOP_VALUE = new TopValue();
    protected Value[] values;
    protected int     currentSize;
    protected int     actualMaxSize;
    public Stack(int maxSize)
    {
        values = new Value[maxSize];
    }
    public Stack(Stack stack)
    {
        this(stack.values.length);
        copy(stack);
    }
    public int getActualMaxSize()
    {
        return actualMaxSize;
    }
    public void reset(int maxSize)
    {
        if (maxSize > values.length)
        {
            values = new Value[maxSize];
        }
        clear();
        actualMaxSize = 0;
    }
    public void copy(Stack other)
    {
        if (other.values.length > values.length)
        {
            values = new Value[other.values.length];
        }
        System.arraycopy(other.values, 0, this.values, 0, other.currentSize);
        currentSize   = other.currentSize;
        actualMaxSize = other.actualMaxSize;
    }
    public boolean generalize(Stack other)
    {
        if (this.currentSize != other.currentSize)
        {
            throw new IllegalArgumentException("Stacks have different current sizes ["+this.currentSize+"] and ["+other.currentSize+"]");
        }
        boolean changed = false;
        for (int index = 0; index < currentSize; index++)
        {
            Value thisValue  = this.values[index];
            if (thisValue != null)
            {
                Value newValue = null;
                Value otherValue = other.values[index];
                if (otherValue != null)
                {
                    newValue = thisValue.generalize(otherValue);
                }
                changed = changed || !thisValue.equals(newValue);
                values[index] = newValue;
            }
        }
        if (this.actualMaxSize < other.actualMaxSize)
        {
            this.actualMaxSize = other.actualMaxSize;
        }
        return changed;
    }
    public void clear()
    {
        for (int index = 0; index < currentSize; index++)
        {
            values[index] = null;
        }
        currentSize = 0;
    }
    public int size()
    {
        return currentSize;
    }
    public Value getBottom(int index)
    {
        return values[index];
    }
    public void setBottom(int index, Value value)
    {
        values[index] = value;
    }
    public Value getTop(int index)
    {
        return values[currentSize - index - 1];
    }
    public void setTop(int index, Value value)
    {
        values[currentSize - index - 1] = value;
    }
    public void removeTop(int index)
    {
        System.arraycopy(values, currentSize - index,
                         values, currentSize - index - 1,
                         index);
        currentSize--;
    }
    public void push(Value value)
    {
        if (value.isCategory2())
        {
            values[currentSize++] = TOP_VALUE;
        }
        values[currentSize++] = value;
        if (actualMaxSize < currentSize)
        {
            actualMaxSize = currentSize;
        }
    }
    public Value pop()
    {
        Value value = values[--currentSize];
        values[currentSize] = null;
        if (value.isCategory2())
        {
            values[--currentSize] = null;
        }
        return value;
    }
    public IntegerValue ipop()
    {
        return pop().integerValue();
    }
    public LongValue lpop()
    {
        return pop().longValue();
    }
    public FloatValue fpop()
    {
        return pop().floatValue();
    }
    public DoubleValue dpop()
    {
        return pop().doubleValue();
    }
    public ReferenceValue apop()
    {
        return pop().referenceValue();
    }
    public InstructionOffsetValue opop()
    {
        return pop().instructionOffsetValue();
    }
    public void pop1()
    {
        values[--currentSize] = null;
    }
    public void pop2()
    {
        values[--currentSize] = null;
        values[--currentSize] = null;
    }
    public void dup()
    {
        values[currentSize] = values[currentSize - 1].category1Value();
        currentSize++;
        if (actualMaxSize < currentSize)
        {
            actualMaxSize = currentSize;
        }
    }
    public void dup_x1()
    {
        values[currentSize]     = values[currentSize - 1].category1Value();
        values[currentSize - 1] = values[currentSize - 2].category1Value();
        values[currentSize - 2] = values[currentSize    ];
        currentSize++;
        if (actualMaxSize < currentSize)
        {
            actualMaxSize = currentSize;
        }
    }
    public void dup_x2()
    {
        values[currentSize]     = values[currentSize - 1].category1Value();
        values[currentSize - 1] = values[currentSize - 2];
        values[currentSize - 2] = values[currentSize - 3];
        values[currentSize - 3] = values[currentSize    ];
        currentSize++;
        if (actualMaxSize < currentSize)
        {
            actualMaxSize = currentSize;
        }
    }
    public void dup2()
    {
        values[currentSize    ] = values[currentSize - 2];
        values[currentSize + 1] = values[currentSize - 1];
        currentSize += 2;
        if (actualMaxSize < currentSize)
        {
            actualMaxSize = currentSize;
        }
    }
    public void dup2_x1()
    {
        values[currentSize + 1] = values[currentSize - 1];
        values[currentSize    ] = values[currentSize - 2];
        values[currentSize - 1] = values[currentSize - 3];
        values[currentSize - 2] = values[currentSize + 1];
        values[currentSize - 3] = values[currentSize    ];
        currentSize += 2;
        if (actualMaxSize < currentSize)
        {
            actualMaxSize = currentSize;
        }
    }
    public void dup2_x2()
    {
        values[currentSize + 1] = values[currentSize - 1];
        values[currentSize    ] = values[currentSize - 2];
        values[currentSize - 1] = values[currentSize - 3];
        values[currentSize - 2] = values[currentSize - 4];
        values[currentSize - 3] = values[currentSize + 1];
        values[currentSize - 4] = values[currentSize    ];
        currentSize += 2;
        if (actualMaxSize < currentSize)
        {
            actualMaxSize = currentSize;
        }
    }
    public void swap()
    {
        Value value1 = values[currentSize - 1].category1Value();
        Value value2 = values[currentSize - 2].category1Value();
        values[currentSize - 1] = value2;
        values[currentSize - 2] = value1;
    }
    public boolean equals(Object object)
    {
        if (object == null ||
            this.getClass() != object.getClass())
        {
            return false;
        }
        Stack other = (Stack)object;
        if (this.currentSize != other.currentSize)
        {
            return false;
        }
        for (int index = 0; index < currentSize; index++)
        {
            Value thisValue  = this.values[index];
            Value otherValue = other.values[index];
            if (thisValue == null ? otherValue != null :
                                    !thisValue.equals(otherValue))
            {
                return false;
            }
        }
        return true;
    }
    public int hashCode()
    {
        int hashCode = currentSize;
        for (int index = 0; index < currentSize; index++)
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
        for (int index = 0; index < currentSize; index++)
        {
            Value value = values[index];
            buffer = buffer.append('[')
                           .append(value == null ? "empty" : value.toString())
                           .append(']');
        }
        return buffer.toString();
    }
}
