public class ListItemFactory {
    public static View twoButtonsSeparatedByFiller(int position, Context context, int desiredHeight) {
        if (desiredHeight < 90) {
            throw new IllegalArgumentException("need at least 90 pixels of height " +
                    "to create the two buttons and leave 10 pixels for the filler");
        }
        final LinearLayout ll = new LinearLayout(context);
        ll.setOrientation(LinearLayout.VERTICAL);
        final LinearLayout.LayoutParams buttonLp =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        50);
        final Button topButton = new Button(context);
        topButton.setLayoutParams(
                buttonLp);
        topButton.setText("top (position " + position + ")");
        ll.addView(topButton);
        final TextView middleFiller = new TextView(context);
        middleFiller.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                desiredHeight - 100));
        middleFiller.setText("filler");
        ll.addView(middleFiller);
        final Button bottomButton = new Button(context);
        bottomButton.setLayoutParams(buttonLp);
        bottomButton.setText("bottom (position " + position + ")");
        ll.addView(bottomButton);
        ll.setTag("twoButtons");
        return ll;
    }
    public enum Slot {
        Left,
        Middle,
        Right
    }
    public static View horizontalButtonSlots(Context context, int desiredHeight, Slot... slots) {
        final LinearLayout ll = new LinearLayout(context);
        ll.setOrientation(LinearLayout.HORIZONTAL);
        final LinearLayout.LayoutParams lp
                = new LinearLayout.LayoutParams(0, desiredHeight);
        lp.setMargins(10, 0, 10, 0);
        lp.weight = 0.33f;
        boolean left = false;
        boolean middle = false;
        boolean right = false;
        for (Slot slot : slots) {
            switch (slot) {
                case Left:
                    left = true;
                    break;
                case Middle:
                    middle = true;
                    break;
                case Right:
                    right = true;
                    break;
            }
        }
        if (left) {
            final Button button = new Button(context);
            button.setText("left");
            ll.addView(button, lp);
        } else {
           ll.addView(new View(context), lp);
        }
        if (middle) {
            final Button button = new Button(context);
            button.setText("center");
            ll.addView(button, lp);
        } else {
           ll.addView(new View(context), lp);
        }
        if (right) {
            final Button button = new Button(context);
            button.setText("right");
            ll.addView(button, lp);
        } else {
           ll.addView(new View(context), lp);
        }
        return ll;
    }
    public static View button(int position, Context context, String text, int desiredHeight) {
        TextView result = new Button(context);
        result.setHeight(desiredHeight);
        result.setText(text);
        final ViewGroup.LayoutParams lp = new AbsListView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        result.setLayoutParams(lp);
        result.setId(position);
        result.setTag("button");
        return result;
    }
    public static View convertButton(View convertView, String text, int position) {
        if (((String) convertView.getTag()).equals("button")) {
            ((Button) convertView).setText(text);
            convertView.setId(position);
            return convertView;
        } else {
            return null;
        }
    }
    public static View text(int position, Context context, String text, int desiredHeight) {
        TextView result = new TextView(context);
        result.setHeight(desiredHeight);
        result.setText(text);
        final ViewGroup.LayoutParams lp = new AbsListView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        result.setLayoutParams(lp);
        result.setId(position);
        result.setTag("text");
        return result;
    }
    public static View convertText(View convertView, String text, int position) {
        if(convertView.getTag() != null && ((String) convertView.getTag()).equals("text")) {
            ((TextView) convertView).setText(text);
            convertView.setId(position);
            return convertView;
        } else {
            return null;
        }
    }
    public static View doubleText(int position, Context context, String text, int desiredHeight) {
        final LinearLayout ll = new LinearLayout(context);
        ll.setOrientation(LinearLayout.HORIZONTAL);
        final AbsListView.LayoutParams lp =
                new AbsListView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        desiredHeight);
        ll.setLayoutParams(lp);
        ll.setId(position);
        TextView t1 = new TextView(context);
        t1.setHeight(desiredHeight);
        t1.setText(text);
        t1.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        final ViewGroup.LayoutParams lp1 = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
        ll.addView(t1, lp1);
        TextView t2 = new TextView(context);
        t2.setHeight(desiredHeight);
        t2.setText(text);
        t2.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        final ViewGroup.LayoutParams lp2 = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1.0f);
        ll.addView(t2, lp2);
        ll.setTag("double");
        return ll;
    }
    public static View convertDoubleText(View convertView, String text, int position) {
        if (((String) convertView.getTag()).equals("double")) {
            TextView t1 = (TextView) ((LinearLayout) convertView).getChildAt(0);
            TextView t2 = (TextView) ((LinearLayout) convertView).getChildAt(1);
            t1.setText(text);
            t2.setText(text);
            convertView.setId(position);
            return convertView;
        } else {
            return null;
        }
    }
}
