public class ListViewTest extends InstrumentationTestCase {
    @MediumTest
    public void testRequestLayout() throws Exception {
        MockContext context = new MockContext2();
        ListView listView = new ListView(context);
        List<String> items = Lists.newArrayList("hello");
        Adapter<String> adapter = new Adapter<String>(context, 0, items);
        listView.setAdapter(adapter);
        int measureSpec = View.MeasureSpec.makeMeasureSpec(100, View.MeasureSpec.EXACTLY);
        adapter.notifyDataSetChanged();
        listView.measure(measureSpec, measureSpec);
        listView.layout(0, 0, 100, 100);
        MockView childView = (MockView) listView.getChildAt(0);
        childView.requestLayout();
        childView.onMeasureCalled = false;
        listView.measure(measureSpec, measureSpec);
        listView.layout(0, 0, 100, 100);
        Assert.assertTrue(childView.onMeasureCalled);
    }
    @MediumTest
    public void testNoSelectableItems() throws Exception {
        MockContext context = new MockContext2();
        ListView listView = new ListView(context);
        listView.addHeaderView(new View(context), null, false);
        List<String> items = Lists.newArrayList("hello");
        Adapter<String> adapter = new Adapter<String>(context, 0, items);
        listView.setAdapter(adapter);
        listView.setSelection(1);
        int measureSpec = View.MeasureSpec.makeMeasureSpec(100, View.MeasureSpec.EXACTLY);
        adapter.notifyDataSetChanged();
        listView.measure(measureSpec, measureSpec);
        listView.layout(0, 0, 100, 100);
        items.remove(0);
        adapter.notifyDataSetChanged();
        listView.measure(measureSpec, measureSpec);
        listView.layout(0, 0, 100, 100);
    }
    private class MockContext2 extends MockContext {
        @Override
        public Resources getResources() {
            return getInstrumentation().getTargetContext().getResources();
        }
        @Override
        public Resources.Theme getTheme() {
            return getInstrumentation().getTargetContext().getTheme();
        }
        @Override
        public Object getSystemService(String name) {
            if (Context.LAYOUT_INFLATER_SERVICE.equals(name)) {
                return getInstrumentation().getTargetContext().getSystemService(name);
            }
            return super.getSystemService(name);
        }
    }
    private class MockView extends View {
        public boolean onMeasureCalled = false;
        public MockView(Context context) {
            super(context);
        }
        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            onMeasureCalled = true;
        }
    }
    private class Adapter<T> extends ArrayAdapter<T> {
        public Adapter(Context context, int resource, List<T> objects) {
            super(context, resource, objects);
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return new MockView(getContext());
        }
    }
}
