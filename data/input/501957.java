public class FilterBuilder
{
    private JCheckBox[] checkBoxes;
    private char        separator;
    public FilterBuilder(JCheckBox[] checkBoxes, char separator)
    {
        this.checkBoxes = checkBoxes;
        this.separator  = separator;
    }
    public String buildFilter()
    {
        StringBuffer positive = new StringBuffer();
        StringBuffer negative = new StringBuffer();
        buildFilter("", positive, negative);
        return positive.length() <= negative.length() ?
            positive.toString() :
            negative.toString();
    }
    private void buildFilter(String       prefix,
                             StringBuffer positive,
                             StringBuffer negative)
    {
        int positiveCount = 0;
        int negativeCount = 0;
        for (int index = 0; index < checkBoxes.length; index++)
        {
            JCheckBox checkBox = checkBoxes[index];
            String    name     = checkBox.getText();
            if (name.startsWith(prefix))
            {
                if (checkBox.isSelected())
                {
                    positiveCount++;
                }
                else
                {
                    negativeCount++;
                }
            }
        }
        if (positiveCount == 0)
        {
            if (positive.length() > 0)
            {
                positive.append(',');
            }
            positive.append('!').append(prefix);
            if (prefix.length() == 0 ||
                prefix.charAt(prefix.length()-1) == separator)
            {
                positive.append('*');
            }
            return;
        }
        if (negativeCount == 0)
        {
            if (negative.length() > 0)
            {
                negative.append(',');
            }
            negative.append(prefix);
            if (prefix.length() == 0 ||
                prefix.charAt(prefix.length()-1) == separator)
            {
                negative.append('*');
            }
            return;
        }
        StringBuffer positiveFilter = new StringBuffer();
        StringBuffer negativeFilter = new StringBuffer();
        String newPrefix = null;
        for (int index = 0; index < checkBoxes.length; index++)
        {
            String name = checkBoxes[index].getText();
            if (name.startsWith(prefix))
            {
                if (newPrefix == null ||
                    !name.startsWith(newPrefix))
                {
                    int prefixIndex =
                        name.indexOf(separator, prefix.length()+1);
                    newPrefix = prefixIndex >= 0 ?
                        name.substring(0, prefixIndex+1) :
                        name;
                    buildFilter(newPrefix,
                                positiveFilter,
                                negativeFilter);
                }
            }
        }
        if (positiveFilter.length() <= negativeFilter.length() + prefix.length() + 3)
        {
            if (positive.length() > 0 &&
                positiveFilter.length() > 0)
            {
                positive.append(',');
            }
            positive.append(positiveFilter);
        }
        else
        {
            if (positive.length() > 0 &&
                negativeFilter.length() > 0)
            {
                positive.append(',');
            }
            positive.append(negativeFilter).append(",!").append(prefix).append('*');
        }
        if (negativeFilter.length() <= positiveFilter.length() + prefix.length() + 4)
        {
            if (negative.length() > 0 &&
                negativeFilter.length() > 0)
            {
                negative.append(',');
            }
            negative.append(negativeFilter);
        }
        else
        {
            if (negative.length() > 0 &&
                positiveFilter.length() > 0)
            {
                negative.append(',');
            }
            negative.append(positiveFilter).append(',').append(prefix).append('*');
        }
    }
}
