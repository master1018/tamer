class ExceptNeverThrown {
    void m(int i) {
        try {
            i++;
        } catch (IOException e) {
        }
    }
}
