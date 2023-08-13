class MatchResultImpl implements MatchResult {
    private String text;
    private int[] offsets;
    MatchResultImpl(String text, int[] offsets) {
        this.text = text;
        this.offsets = offsets.clone();
    }
    public int end() {
        return end(0);
    }
    public int end(int group) {
        return offsets[2 * group + 1];
    }
    public String group() {
        return text.substring(start(), end());
    }
    public String group(int group) {
        int from = offsets[group * 2];
        int to = offsets[(group * 2) + 1];
        if (from == -1 || to == -1) {
            return null;
        } else {
            return text.substring(from, to);
        }
    }
    public int groupCount() {
        return (offsets.length / 2) - 1;
    }
    public int start() {
        return start(0);
    }
    public int start(int group) {
        return offsets[2 * group];
    }
}
