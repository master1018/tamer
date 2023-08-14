public class TracedVariables extends Variables
{
    public static final int NONE = -1;
    private Value     producerValue;
    private Variables producerVariables;
    private int       initializationIndex;
    public TracedVariables(int size)
    {
        super(size);
        producerVariables = new Variables(size);
    }
    public TracedVariables(TracedVariables tracedVariables)
    {
        super(tracedVariables);
        producerVariables = new Variables(tracedVariables.producerVariables);
    }
    public void setProducerValue(Value producerValue)
    {
        this.producerValue = producerValue;
    }
    public void resetInitialization()
    {
        initializationIndex = NONE;
    }
    public int getInitializationIndex()
    {
        return initializationIndex;
    }
    public Value getProducerValue(int index)
    {
        return producerVariables.getValue(index);
    }
    public void setProducerValue(int index, Value value)
    {
        producerVariables.store(index, value);
    }
    public void reset(int size)
    {
        super.reset(size);
        producerVariables.reset(size);
    }
    public void initialize(TracedVariables other)
    {
        super.initialize(other);
        producerVariables.initialize(other.producerVariables);
    }
    public boolean generalize(TracedVariables other,
                              boolean         clearConflictingOtherVariables)
    {
        boolean variablesChanged = super.generalize(other, clearConflictingOtherVariables);
        boolean producersChanged = producerVariables.generalize(other.producerVariables, clearConflictingOtherVariables);
        if (variablesChanged)
        {
            for (int index = 0; index < size; index++)
            {
                if (values[index] == null)
                {
                    producerVariables.values[index] = null;
                    if (clearConflictingOtherVariables)
                    {
                        other.producerVariables.values[index] = null;
                    }
                }
            }
        }
        return variablesChanged || producersChanged;
    }
    public void store(int index, Value value)
    {
        Value previousValue = super.load(index);
        if (previousValue == null ||
            previousValue.computationalType() != value.computationalType())
        {
            initializationIndex = index;
        }
        super.store(index, value);
        producerVariables.store(index, producerValue);
        if (value.isCategory2())
        {
            producerVariables.store(index+1, producerValue);
        }
    }
    public boolean equals(Object object)
    {
        if (object == null ||
            this.getClass() != object.getClass())
        {
            return false;
        }
        TracedVariables other = (TracedVariables)object;
        return super.equals(object) &&
               this.producerVariables.equals(other.producerVariables);
    }
    public int hashCode()
    {
        return super.hashCode() ^
               producerVariables.hashCode();
    }
    public String toString()
    {
        StringBuffer buffer = new StringBuffer();
        for (int index = 0; index < this.size(); index++)
        {
            Value value         = this.values[index];
            Value producerValue = producerVariables.getValue(index);
            buffer = buffer.append('[')
                           .append(producerValue == null ? "empty:" : producerValue.toString())
                           .append(value         == null ? "empty"  : value.toString())
                           .append(']');
        }
        return buffer.toString();
    }
}
