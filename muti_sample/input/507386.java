public class TracedStack extends Stack
{
    private Value producerValue;
    private Stack producerStack;
    public TracedStack(int maxSize)
    {
        super(maxSize);
        producerStack = new Stack(maxSize);
    }
    public TracedStack(TracedStack tracedStack)
    {
        super(tracedStack);
        producerStack = new Stack(tracedStack.producerStack);
    }
    public void setProducerValue(Value producerValue)
    {
        this.producerValue = producerValue;
    }
    public Value getBottomProducerValue(int index)
    {
        return producerStack.getBottom(index);
    }
    public void setBottomProducerValue(int index, Value value)
    {
        producerStack.setBottom(index, value);
    }
    public Value getTopProducerValue(int index)
    {
        return producerStack.getTop(index);
    }
    public void setTopProducerValue(int index, Value value)
    {
        producerStack.setTop(index, value);
    }
    public void reset(int size)
    {
        super.reset(size);
        producerStack.reset(size);
    }
    public void copy(TracedStack other)
    {
        super.copy(other);
        producerStack.copy(other.producerStack);
    }
    public boolean generalize(TracedStack other)
    {
        return
            super.generalize(other) |
            producerStack.generalize(other.producerStack);
    }
    public void clear()
    {
        super.clear();
        producerStack.clear();
    }
    public void removeTop(int index)
    {
        super.removeTop(index);
        producerStack.removeTop(index);
    }
    public void push(Value value)
    {
        super.push(value);
        producerPush();
        if (value.isCategory2())
        {
            producerPush();
        }
    }
    public Value pop()
    {
        Value value = super.pop();
        producerPop();
        if (value.isCategory2())
        {
            producerPop();
        }
        return value;
    }
    public void pop1()
    {
        super.pop1();
        producerPop();
    }
    public void pop2()
    {
        super.pop2();
        producerPop();
        producerPop();
    }
    public void dup()
    {
        super.dup();
        producerPop();
        producerPush();
        producerPush();
    }
    public void dup_x1()
    {
        super.dup_x1();
        producerPop();
        producerPop();
        producerPush();
        producerPush();
        producerPush();
    }
    public void dup_x2()
    {
        super.dup_x2();
        producerPop();
        producerPop();
        producerPop();
        producerPush();
        producerPush();
        producerPush();
        producerPush();
    }
    public void dup2()
    {
        super.dup2();
        producerPop();
        producerPop();
        producerPush();
        producerPush();
        producerPush();
        producerPush();
    }
    public void dup2_x1()
    {
        super.dup2_x1();
        producerPop();
        producerPop();
        producerPop();
        producerPush();
        producerPush();
        producerPush();
        producerPush();
        producerPush();
    }
    public void dup2_x2()
    {
        super.dup2_x2();
        producerPop();
        producerPop();
        producerPop();
        producerPop();
        producerPush();
        producerPush();
        producerPush();
        producerPush();
        producerPush();
        producerPush();
    }
    public void swap()
    {
        super.swap();
        producerPop();
        producerPop();
        producerPush();
        producerPush();
    }
    public boolean equals(Object object)
    {
        if (object == null ||
            this.getClass() != object.getClass())
        {
            return false;
        }
        TracedStack other = (TracedStack)object;
        return super.equals(object) &&
               this.producerStack.equals(other.producerStack);
    }
    public int hashCode()
    {
        return super.hashCode() ^
               producerStack.hashCode();
    }
    public String toString()
    {
        StringBuffer buffer = new StringBuffer();
        for (int index = 0; index < this.size(); index++)
        {
            Value value         = this.values[index];
            Value producerValue = producerStack.getBottom(index);
            buffer = buffer.append('[')
                           .append(producerValue == null ? "empty:" : producerValue.toString())
                           .append(value         == null ? "empty"  : value.toString())
                           .append(']');
        }
        return buffer.toString();
    }
    private void producerPush()
    {
        producerStack.push(producerValue);
    }
    private void producerPop()
    {
        producerStack.pop();
    }
}
