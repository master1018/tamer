class GTKRegion extends Region {
    public static final Region HANDLE_BOX = new GTKRegion("HandleBox", null,
                                                          true);
    protected GTKRegion(String name, String ui, boolean subregion) {
        super(name, ui, subregion);
    }
}
