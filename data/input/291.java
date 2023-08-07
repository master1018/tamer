public class MyDecimalMappingCondition implements IMappingCondition {
    @Override
    public boolean isMatch(Object widget) {
        boolean result = false;
        if (widget instanceof Text) {
            final Text text = (Text) widget;
            result = SnverUIConstant.RIDGET_MY_DECIMAL_TEXT.equals(text.getData(SnverUIConstant.UI_TYPE));
        }
        return result;
    }
}
