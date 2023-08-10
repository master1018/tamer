public class ExpressionImpl extends GenericExpressionImpl implements Expression {
    protected static final String SIGN_EDEFAULT = null;
    protected String sign = SIGN_EDEFAULT;
    protected EList<DottedExpression> termList;
    protected EList<String> operatorList;
    protected ExpressionImpl() {
        super();
    }
    @Override
    protected EClass eStaticClass() {
        return SwrtjPackage.Literals.EXPRESSION;
    }
    public String getSign() {
        return sign;
    }
    public void setSign(String newSign) {
        String oldSign = sign;
        sign = newSign;
        if (eNotificationRequired()) eNotify(new ENotificationImpl(this, Notification.SET, SwrtjPackage.EXPRESSION__SIGN, oldSign, sign));
    }
    public EList<DottedExpression> getTermList() {
        if (termList == null) {
            termList = new EObjectContainmentEList<DottedExpression>(DottedExpression.class, this, SwrtjPackage.EXPRESSION__TERM_LIST);
        }
        return termList;
    }
    public EList<String> getOperatorList() {
        if (operatorList == null) {
            operatorList = new EDataTypeEList<String>(String.class, this, SwrtjPackage.EXPRESSION__OPERATOR_LIST);
        }
        return operatorList;
    }
    @Override
    public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
        switch(featureID) {
            case SwrtjPackage.EXPRESSION__TERM_LIST:
                return ((InternalEList<?>) getTermList()).basicRemove(otherEnd, msgs);
        }
        return super.eInverseRemove(otherEnd, featureID, msgs);
    }
    @Override
    public Object eGet(int featureID, boolean resolve, boolean coreType) {
        switch(featureID) {
            case SwrtjPackage.EXPRESSION__SIGN:
                return getSign();
            case SwrtjPackage.EXPRESSION__TERM_LIST:
                return getTermList();
            case SwrtjPackage.EXPRESSION__OPERATOR_LIST:
                return getOperatorList();
        }
        return super.eGet(featureID, resolve, coreType);
    }
    @SuppressWarnings("unchecked")
    @Override
    public void eSet(int featureID, Object newValue) {
        switch(featureID) {
            case SwrtjPackage.EXPRESSION__SIGN:
                setSign((String) newValue);
                return;
            case SwrtjPackage.EXPRESSION__TERM_LIST:
                getTermList().clear();
                getTermList().addAll((Collection<? extends DottedExpression>) newValue);
                return;
            case SwrtjPackage.EXPRESSION__OPERATOR_LIST:
                getOperatorList().clear();
                getOperatorList().addAll((Collection<? extends String>) newValue);
                return;
        }
        super.eSet(featureID, newValue);
    }
    @Override
    public void eUnset(int featureID) {
        switch(featureID) {
            case SwrtjPackage.EXPRESSION__SIGN:
                setSign(SIGN_EDEFAULT);
                return;
            case SwrtjPackage.EXPRESSION__TERM_LIST:
                getTermList().clear();
                return;
            case SwrtjPackage.EXPRESSION__OPERATOR_LIST:
                getOperatorList().clear();
                return;
        }
        super.eUnset(featureID);
    }
    @Override
    public boolean eIsSet(int featureID) {
        switch(featureID) {
            case SwrtjPackage.EXPRESSION__SIGN:
                return SIGN_EDEFAULT == null ? sign != null : !SIGN_EDEFAULT.equals(sign);
            case SwrtjPackage.EXPRESSION__TERM_LIST:
                return termList != null && !termList.isEmpty();
            case SwrtjPackage.EXPRESSION__OPERATOR_LIST:
                return operatorList != null && !operatorList.isEmpty();
        }
        return super.eIsSet(featureID);
    }
    @Override
    public String toString() {
        if (eIsProxy()) return super.toString();
        StringBuffer result = new StringBuffer(super.toString());
        result.append(" (sign: ");
        result.append(sign);
        result.append(", operatorList: ");
        result.append(operatorList);
        result.append(')');
        return result.toString();
    }
}
