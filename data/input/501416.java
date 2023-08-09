public abstract class MemberIdsSection extends UniformItemSection {
    public MemberIdsSection(String name, DexFile file) {
        super(name, file, 4);
    }
    @Override
    protected void orderItems() {
        int idx = 0;
        for (Object i : items()) {
            ((MemberIdItem) i).setIndex(idx);
            idx++;
        }
    }
}
