public class Test5063390 {
    private int alpha;
    private int foxtrot;
    private int zulu;
    public int getZulu() {
        return zulu;
    }
    public void setZulu(int zulu) {
        this.zulu = zulu;
    }
    public int getAlpha() {
        return alpha;
    }
    public void setAlpha(int alpha) {
        this.alpha = alpha;
    }
    public int getFoxtrot() {
        return foxtrot;
    }
    public void setFoxtrot(int foxtrot) {
        this.foxtrot = foxtrot;
    }
    public static void main(String[] args) {
        String[] names = {"alpha", "class", "foxtrot", "zulu"};
        PropertyDescriptor[] pd = BeanUtils.getPropertyDescriptors(Test5063390.class);
        if (pd.length != names.length)
            throw new Error("unexpected count of properties: " + pd.length);
        for (int i = 0; i < pd.length; i++) {
            String name = pd[i].getName();
            System.out.println("property: " + name);
            if (!name.equals(names[i])) {
                System.out.println("expected: " + names[i]);
                throw new Error("unexpected order of properties");
            }
        }
    }
}
