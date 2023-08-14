public class ListHeterogeneous extends ListScenario {
    @Override
    protected void init(Params params) {
        params.setNumItems(50)
                .setItemScreenSizeFactor(1.0 / 8)
                .setItemsFocusable(true)
                .setHeaderViewCount(3)
                .setFooterViewCount(2);
    }
    @Override
    protected View createView(int position, ViewGroup parent, int desiredHeight) {
        switch (position % 3) {
        case 0:
            return ListItemFactory.text(
                    position, parent.getContext(), getValueAtPosition(position), desiredHeight);
        case 1:
            return ListItemFactory.button(
                    position, parent.getContext(), getValueAtPosition(position), desiredHeight);
        case 2:
            return ListItemFactory.doubleText(
                    position, parent.getContext(), getValueAtPosition(position), desiredHeight);
        }
        return null;
    }
    @Override
    public View convertView(int position, View convertView, ViewGroup parent) {
        switch (position % 3) {
        case 0:
            return ListItemFactory.convertText(convertView, getValueAtPosition(position), position);
        case 1:
            return ListItemFactory.convertButton(convertView, getValueAtPosition(position),
                    position);
        case 2:
            return ListItemFactory.convertDoubleText(convertView, getValueAtPosition(position),
                    position);
        }
        return null;
    }
    @Override
    public int getItemViewType(int position) {
        return position % 3;
    }
    @Override
    public int getViewTypeCount() {
        return 3;
    }
}
