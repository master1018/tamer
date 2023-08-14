public class AgendaItemView extends RelativeLayout {
    Paint mPaint = new Paint();
    public AgendaItemView(Context context) {
        super(context);
    }
    public AgendaItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        ViewHolder holder = (ViewHolder) getTag();
        if (holder != null) {
            mPaint.setColor(holder.calendarColor);
            canvas.drawRect(0, 0, 5, getHeight(), mPaint);
            if (holder.overLayColor != 0) {
                mPaint.setColor(holder.overLayColor);
                canvas.drawRect(0, 0, getWidth(), getHeight(), mPaint);
            }
        }
    }
}
