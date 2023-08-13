public class FontRenderContext {
    private transient AffineTransform tx;
    private transient Object aaHintValue;
    private transient Object fmHintValue;
    private transient boolean defaulting;
    protected FontRenderContext() {
        aaHintValue = VALUE_TEXT_ANTIALIAS_DEFAULT;
        fmHintValue = VALUE_FRACTIONALMETRICS_DEFAULT;
        defaulting = true;
    }
    public FontRenderContext(AffineTransform tx,
                            boolean isAntiAliased,
                            boolean usesFractionalMetrics) {
        if (tx != null && !tx.isIdentity()) {
            this.tx = new AffineTransform(tx);
        }
        if (isAntiAliased) {
            aaHintValue = VALUE_TEXT_ANTIALIAS_ON;
        } else {
            aaHintValue = VALUE_TEXT_ANTIALIAS_OFF;
        }
        if (usesFractionalMetrics) {
            fmHintValue = VALUE_FRACTIONALMETRICS_ON;
        } else {
            fmHintValue = VALUE_FRACTIONALMETRICS_OFF;
        }
    }
    public FontRenderContext(AffineTransform tx, Object aaHint, Object fmHint){
        if (tx != null && !tx.isIdentity()) {
            this.tx = new AffineTransform(tx);
        }
        try {
            if (KEY_TEXT_ANTIALIASING.isCompatibleValue(aaHint)) {
                aaHintValue = aaHint;
            } else {
                throw new IllegalArgumentException("AA hint:" + aaHint);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("AA hint:" +aaHint);
        }
        try {
            if (KEY_FRACTIONALMETRICS.isCompatibleValue(fmHint)) {
                fmHintValue = fmHint;
            } else {
                throw new IllegalArgumentException("FM hint:" + fmHint);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("FM hint:" +fmHint);
        }
    }
    public boolean isTransformed() {
        if (!defaulting) {
            return tx != null;
        } else {
            return !getTransform().isIdentity();
        }
    }
    public int getTransformType() {
        if (!defaulting) {
            if (tx == null) {
                return AffineTransform.TYPE_IDENTITY;
            } else {
                return tx.getType();
            }
        } else {
            return getTransform().getType();
        }
    }
    public AffineTransform getTransform() {
        return (tx == null) ? new AffineTransform() : new AffineTransform(tx);
    }
    public boolean isAntiAliased() {
        return !(aaHintValue == VALUE_TEXT_ANTIALIAS_OFF ||
                 aaHintValue == VALUE_TEXT_ANTIALIAS_DEFAULT);
    }
    public boolean usesFractionalMetrics() {
        return !(fmHintValue == VALUE_FRACTIONALMETRICS_OFF ||
                 fmHintValue == VALUE_FRACTIONALMETRICS_DEFAULT);
    }
    public Object getAntiAliasingHint() {
        if (defaulting) {
            if (isAntiAliased()) {
                 return VALUE_TEXT_ANTIALIAS_ON;
            } else {
                return VALUE_TEXT_ANTIALIAS_OFF;
            }
        }
        return aaHintValue;
    }
    public Object getFractionalMetricsHint() {
        if (defaulting) {
            if (usesFractionalMetrics()) {
                 return VALUE_FRACTIONALMETRICS_ON;
            } else {
                return VALUE_FRACTIONALMETRICS_OFF;
            }
        }
        return fmHintValue;
    }
    public boolean equals(Object obj) {
        try {
            return equals((FontRenderContext)obj);
        }
        catch (ClassCastException e) {
            return false;
        }
    }
    public boolean equals(FontRenderContext rhs) {
        if (this == rhs) {
            return true;
        }
        if (rhs == null) {
            return false;
        }
        if (!rhs.defaulting && !defaulting) {
            if (rhs.aaHintValue == aaHintValue &&
                rhs.fmHintValue == fmHintValue) {
                return tx == null ? rhs.tx == null : tx.equals(rhs.tx);
            }
            return false;
        } else {
            return
                rhs.getAntiAliasingHint() == getAntiAliasingHint() &&
                rhs.getFractionalMetricsHint() == getFractionalMetricsHint() &&
                rhs.getTransform().equals(getTransform());
        }
    }
    public int hashCode() {
        int hash = tx == null ? 0 : tx.hashCode();
        if (defaulting) {
            hash += getAntiAliasingHint().hashCode();
            hash += getFractionalMetricsHint().hashCode();
        } else {
            hash += aaHintValue.hashCode();
            hash += fmHintValue.hashCode();
        }
        return hash;
    }
}
