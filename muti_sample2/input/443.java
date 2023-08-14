public class test {
    public HashWrapper getHash() {
        return new HashWrapper(sha1.digest());
    }
}
