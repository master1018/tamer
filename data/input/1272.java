public abstract class AssignmentASImpl extends EObjectImpl implements AssignmentAS {
    protected ExpressionAS expression = null;
    protected AssignmentASImpl() {
        super();
    }
    protected EClass eStaticClass() {
        return AssignastPackage.Literals.ASSIGNMENT_AS;
    }
    public ExpressionAS getExpression() {
        return expression;
    }
    public NotificationChain basicSetExpression(ExpressionAS newExpression, NotificationChain msgs) {
        ExpressionAS oldExpression = expression;
        expression = newExpression;
        if (eNotificationRequired()) {
            ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, AssignastPackage.ASSIGNMENT_AS__EXPRESSION, oldExpression, newExpression);
            if (msgs == null) msgs = notification; else msgs.add(notification);
        }
        return msgs;
    }
    public void setExpression(ExpressionAS newExpression) {
        if (newExpression != expression) {
            NotificationChain msgs = null;
            if (expression != null) msgs = ((InternalEObject) expression).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - AssignastPackage.ASSIGNMENT_AS__EXPRESSION, null, msgs);
            if (newExpression != null) msgs = ((InternalEObject) newExpression).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - AssignastPackage.ASSIGNMENT_AS__EXPRESSION, null, msgs);
            msgs = basicSetExpression(newExpression, msgs);
            if (msgs != null) msgs.dispatch();
        } else if (eNotificationRequired()) eNotify(new ENotificationImpl(this, Notification.SET, AssignastPackage.ASSIGNMENT_AS__EXPRESSION, newExpression, newExpression));
    }
    public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
        switch(featureID) {
            case AssignastPackage.ASSIGNMENT_AS__EXPRESSION:
                return basicSetExpression(null, msgs);
        }
        return super.eInverseRemove(otherEnd, featureID, msgs);
    }
    public Object eGet(int featureID, boolean resolve, boolean coreType) {
        switch(featureID) {
            case AssignastPackage.ASSIGNMENT_AS__EXPRESSION:
                return getExpression();
        }
        return super.eGet(featureID, resolve, coreType);
    }
    public void eSet(int featureID, Object newValue) {
        switch(featureID) {
            case AssignastPackage.ASSIGNMENT_AS__EXPRESSION:
                setExpression((ExpressionAS) newValue);
                return;
        }
        super.eSet(featureID, newValue);
    }
    public void eUnset(int featureID) {
        switch(featureID) {
            case AssignastPackage.ASSIGNMENT_AS__EXPRESSION:
                setExpression((ExpressionAS) null);
                return;
        }
        super.eUnset(featureID);
    }
    public boolean eIsSet(int featureID) {
        switch(featureID) {
            case AssignastPackage.ASSIGNMENT_AS__EXPRESSION:
                return expression != null;
        }
        return super.eIsSet(featureID);
    }
}
