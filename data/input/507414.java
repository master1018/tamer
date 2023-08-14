public final class CoordinateControls {
    private double mValue;
    private boolean mValueValidity = false;
    private Text mDecimalText;
    private Text mSexagesimalDegreeText;
    private Text mSexagesimalMinuteText;
    private Text mSexagesimalSecondText;
    private int mManualTextChange = 0;
    private ModifyListener mSexagesimalListener = new ModifyListener() {
        public void modifyText(ModifyEvent event) {
            if (mManualTextChange > 0) {
                return;
            }
            try {
                mValue = getValueFromSexagesimalControls();
                setValueIntoDecimalControl(mValue);
                mValueValidity = true;
            } catch (NumberFormatException e) {
                mValueValidity = false;
                resetDecimalControls();
            }
        }
    };
    public void createDecimalText(Composite parent) {
        mDecimalText = createTextControl(parent, "-199.999999", new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                if (mManualTextChange > 0) {
                    return;
                }
                try {
                    mValue = Double.parseDouble(mDecimalText.getText());
                    setValueIntoSexagesimalControl(mValue);
                    mValueValidity = true;
                } catch (NumberFormatException e) {
                    mValueValidity = false;
                    resetSexagesimalControls();
                }
            }
        });
    }
    public void createSexagesimalDegreeText(Composite parent) {
        mSexagesimalDegreeText = createTextControl(parent, "-199", mSexagesimalListener); 
    }
    public void createSexagesimalMinuteText(Composite parent) {
        mSexagesimalMinuteText = createTextControl(parent, "99", mSexagesimalListener); 
    }
    public void createSexagesimalSecondText(Composite parent) {
        mSexagesimalSecondText = createTextControl(parent, "99.999", mSexagesimalListener); 
    }
    public void setValue(double value) {
        mValue = value;
        mValueValidity = true;
        setValueIntoDecimalControl(value);
        setValueIntoSexagesimalControl(value);
    }
    public boolean isValueValid() {
        return mValueValidity;
    }
    public double getValue() {
        return mValue;
    }
    public void setEnabled(boolean enabled) {
        mDecimalText.setEnabled(enabled);
        mSexagesimalDegreeText.setEnabled(enabled);
        mSexagesimalMinuteText.setEnabled(enabled);
        mSexagesimalSecondText.setEnabled(enabled);
    }
    private void resetDecimalControls() {
        mManualTextChange++;
        mDecimalText.setText(""); 
        mManualTextChange--;
    }
    private void resetSexagesimalControls() {
        mManualTextChange++;
        mSexagesimalDegreeText.setText(""); 
        mSexagesimalMinuteText.setText(""); 
        mSexagesimalSecondText.setText(""); 
        mManualTextChange--;
    }
    private Text createTextControl(Composite parent, String defaultString,
            ModifyListener listener) {
        Text text = new Text(parent, SWT.BORDER | SWT.LEFT | SWT.SINGLE);
        text.addModifyListener(listener);
        mManualTextChange++;
        text.setText(defaultString);
        text.pack();
        Point size = text.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        text.setText(""); 
        mManualTextChange--;
        GridData gridData = new GridData();
        gridData.widthHint = size.x;
        text.setLayoutData(gridData);
        return text;
    }
    private double getValueFromSexagesimalControls() throws NumberFormatException {
        double degrees = Double.parseDouble(mSexagesimalDegreeText.getText());
        double minutes = Double.parseDouble(mSexagesimalMinuteText.getText());
        double seconds = Double.parseDouble(mSexagesimalSecondText.getText());
        boolean isPositive = (degrees >= 0.);
        degrees = Math.abs(degrees);
        double value = degrees + minutes / 60. + seconds / 3600.; 
        return isPositive ? value : - value;
    }
    private void setValueIntoDecimalControl(double value) {
        mManualTextChange++;
        mDecimalText.setText(String.format("%.6f", value));
        mManualTextChange--;
    }
    private void setValueIntoSexagesimalControl(double value) {
        boolean isPositive = (value >= 0.);
        value = Math.abs(value);
        double degrees = Math.floor(value);
        double minutes = Math.floor((value - degrees) * 60.);
        double seconds = (value - degrees) * 3600. - minutes * 60.;
        mManualTextChange++;
        mSexagesimalDegreeText.setText(
                Integer.toString(isPositive ? (int)degrees : (int)- degrees));
        mSexagesimalMinuteText.setText(Integer.toString((int)minutes));
        mSexagesimalSecondText.setText(String.format("%.3f", seconds)); 
        mManualTextChange--;
    }
}
