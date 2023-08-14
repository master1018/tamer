public abstract class Sensor {
    private Object  lock;
    private String  name;
    private long    count;
    private boolean on;
    public Sensor(String name) {
        this.name = name;
        this.count = 0;
        this.on = false;
        this.lock = new Object();
    }
    public String getName() {
        return name;
    }
    public long getCount() {
        synchronized (lock) {
            return count;
        }
    }
    public boolean isOn() {
        synchronized (lock) {
            return on;
        }
    }
    public void trigger() {
        synchronized (lock) {
            on = true;
            count++;
        }
        triggerAction();
    }
    public void trigger(int increment) {
        synchronized (lock) {
            on = true;
            count += increment;
        }
        triggerAction();
    }
    public void trigger(int increment, MemoryUsage usage) {
        synchronized (lock) {
            on = true;
            count += increment;
        }
        triggerAction(usage);
    }
    public void clear() {
        synchronized (lock) {
            on = false;
        }
        clearAction();
    }
    public void clear(int increment) {
        synchronized (lock) {
            on = false;
            count += increment;
        }
        clearAction();
    }
    public String toString() {
        return "Sensor - " + getName() +
            (isOn() ? " on " : " off ") +
            " count = " + getCount();
    }
    abstract void triggerAction();
    abstract void triggerAction(MemoryUsage u);
    abstract void clearAction();
}
