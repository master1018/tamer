public final class IntHashtable {
    public IntHashtable () {
        initialize(3);
    }
    public IntHashtable (int initialSize) {
        initialize(leastGreaterPrimeIndex((int)(initialSize/HIGH_WATER_FACTOR)));
    }
    public int size() {
        return count;
    }
    public boolean isEmpty() {
        return count == 0;
    }
    public void put(int key, int value) {
        if (count > highWaterMark) {
            rehash();
        }
        int index = find(key);
        if (keyList[index] <= MAX_UNUSED) {      
            keyList[index] = key;
            ++count;
        }
        values[index] = value;                   
    }
    public int get(int key) {
        return values[find(key)];
    }
    public void remove(int key) {
        int index = find(key);
        if (keyList[index] > MAX_UNUSED) {       
            keyList[index] = DELETED;            
            values[index] = defaultValue;        
            --count;
            if (count < lowWaterMark) {
                rehash();
            }
        }
    }
    public int getDefaultValue() {
        return defaultValue;
    }
    public void setDefaultValue(int newValue) {
        defaultValue = newValue;
        rehash();
    }
    public boolean equals (Object that) {
        if (that.getClass() != this.getClass()) return false;
        IntHashtable other = (IntHashtable) that;
        if (other.size() != count || other.defaultValue != defaultValue) {
                return false;
        }
        for (int i = 0; i < keyList.length; ++i) {
            int key = keyList[i];
            if (key > MAX_UNUSED && other.get(key) != values[i])
                return false;
        }
        return true;
    }
    public int hashCode() {
        int result = 465;   
        int scrambler = 1362796821; 
        for (int i = 0; i < keyList.length; ++i) {
            result = (int)(result * scrambler + 1);
            result += keyList[i];
        }
        for (int i = 0; i < values.length; ++i) {
            result = (int)(result * scrambler + 1);
            result += values[i];
        }
        return result;
    }
    public Object clone ()
                    throws CloneNotSupportedException {
        IntHashtable result = (IntHashtable) super.clone();
        values = (int[]) values.clone();
        keyList = (int[])keyList.clone();
        return result;
    }
    private int defaultValue = 0;
    private int primeIndex;
    private static final float HIGH_WATER_FACTOR = 0.4F;
    private int highWaterMark;
    private static final float LOW_WATER_FACTOR = 0.0F;
    private int lowWaterMark;
    private int count;
    private int[] values;
    private int[] keyList;
    private static final int EMPTY   = Integer.MIN_VALUE;
    private static final int DELETED = EMPTY + 1;
    private static final int MAX_UNUSED = DELETED;
    private void initialize (int primeIndex) {
        if (primeIndex < 0) {
            primeIndex = 0;
        } else if (primeIndex >= PRIMES.length) {
            System.out.println("TOO BIG");
            primeIndex = PRIMES.length - 1;
        }
        this.primeIndex = primeIndex;
        int initialSize = PRIMES[primeIndex];
        values = new int[initialSize];
        keyList = new int[initialSize];
        for (int i = 0; i < initialSize; ++i) {
            keyList[i] = EMPTY;
            values[i] = defaultValue;
        }
        count = 0;
        lowWaterMark = (int)(initialSize * LOW_WATER_FACTOR);
        highWaterMark = (int)(initialSize * HIGH_WATER_FACTOR);
    }
    private void rehash() {
        int[] oldValues = values;
        int[] oldkeyList = keyList;
        int newPrimeIndex = primeIndex;
        if (count > highWaterMark) {
            ++newPrimeIndex;
        } else if (count < lowWaterMark) {
            newPrimeIndex -= 2;
        }
        initialize(newPrimeIndex);
        for (int i = oldValues.length - 1; i >= 0; --i) {
            int key = oldkeyList[i];
            if (key > MAX_UNUSED) {
                    putInternal(key, oldValues[i]);
            }
        }
    }
    public void putInternal (int key, int value) {
        int index = find(key);
        if (keyList[index] < MAX_UNUSED) {      
            keyList[index] = key;
            ++count;
        }
        values[index] = value;                  
    }
    private int find (int key) {
        if (key <= MAX_UNUSED)
            throw new IllegalArgumentException("key can't be less than 0xFFFFFFFE");
        int firstDeleted = -1;  
        int index = (key ^ 0x4000000) % keyList.length;
        if (index < 0) index = -index; 
        int jump = 0; 
        while (true) {
            int tableHash = keyList[index];
            if (tableHash == key) {                 
                return index;
            } else if (tableHash > MAX_UNUSED) {    
            } else if (tableHash == EMPTY) {        
                if (firstDeleted >= 0) {
                    index = firstDeleted;           
                }
                return index;
            } else if (firstDeleted < 0) {          
                    firstDeleted = index;
            }
            if (jump == 0) {                        
                jump = (key % (keyList.length - 1));
                if (jump < 0) jump = -jump;
                ++jump;
            }
            index = (index + jump) % keyList.length;
            if (index == firstDeleted) {
                return index;
            }
        }
    }
    private static int leastGreaterPrimeIndex(int source) {
        int i;
        for (i = 0; i < PRIMES.length; ++i) {
            if (source < PRIMES[i]) {
                break;
            }
        }
        return (i == 0) ? 0 : (i - 1);
    }
    private static final int[] PRIMES = {
        17, 37, 67, 131, 257,
        521, 1031, 2053, 4099, 8209, 16411, 32771, 65537,
        131101, 262147, 524309, 1048583, 2097169, 4194319, 8388617, 16777259,
        33554467, 67108879, 134217757, 268435459, 536870923, 1073741827, 2147483647
    };
}
