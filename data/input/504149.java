public class ExpandableListSimple extends ExpandableListScenario {
    private static final int[] NUM_CHILDREN = {4, 3, 2, 1, 0};
    @Override
    protected void init(ExpandableParams params) {
        params.setNumChildren(NUM_CHILDREN)
                .setItemScreenSizeFactor(0.14);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("Add item").setOnMenuItemClickListener(new OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                mGroups.add(0, new MyGroup(2));
                ((BaseExpandableListAdapter) mAdapter).notifyDataSetChanged();
                return true;
            }
        });
        return true;
    }
}
