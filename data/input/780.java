public class ChainingHash {
    private ArrayList<ArrayList> hashArray;
    private int arraySize;
    private int index;
    public ChainingHash(int size) {
        arraySize = size;
        hashArray = new ArrayList<ArrayList>(arraySize);
        for (int i = 0; i < arraySize; i++) {
            hashArray.add(i, new ArrayList<ChecksumPair>());
        }
    }
    public int hashFunction(ChecksumPair pKey) {
        return pKey.hashCode() % arraySize;
    }
    public void insert(ChecksumPair pKey) {
        int hashValue = hashFunction(pKey);
        hashArray.get(hashValue).add(pKey);
    }
    public void delete(ChecksumPair pKey) {
        int hashValue = hashFunction(pKey);
        hashArray.get(hashValue).remove(pKey);
    }
    public ChecksumPair find(ChecksumPair pKey) {
        int hashValue = hashFunction(pKey);
        ChecksumPair p = null;
        for (int i = 0; i < hashArray.get(hashValue).size(); i++) {
            p = (ChecksumPair) hashArray.get(hashValue).get(i);
            if (p.getWeak() == pKey.getWeak()) {
                index = i;
                return p;
            }
        }
        return null;
    }
    public ChecksumPair findMatch(ChecksumPair pKey) {
        int hashValue = hashFunction(pKey);
        ChecksumPair p = (ChecksumPair) hashArray.get(hashValue).get(index);
        if (p.getWeak() == pKey.getWeak() && Arrays.equals(p.getStrong(), pKey.getStrong())) {
            return p;
        } else {
            p = null;
        }
        for (int i = 0; i < hashArray.get(hashValue).size(); i++) {
            p = (ChecksumPair) hashArray.get(hashValue).get(i);
            if (p.getWeak() == pKey.getWeak() && Arrays.equals(p.getStrong(), pKey.getStrong())) {
                return p;
            }
        }
        return null;
    }
    public void displayTable() {
        for (int l = 0; l < hashArray.size(); l++) {
            for (int i = 0; i < hashArray.get(l).size(); i++) {
                System.out.println(l + ". list: " + ((ChecksumPair) (hashArray.get(l).get(i))).toString());
            }
        }
    }
}
