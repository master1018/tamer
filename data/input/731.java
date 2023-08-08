public class TrPostDataEditExprPanel extends TrPostDataEditCommonPanel {
    CtuluValueEditorDouble editor_;
    CtuluExpr expr_;
    final String initExpr_;
    BuTextField tfExpr_;
    Map varVar2d_;
    public TrPostDataEditExprPanel(final TrPostSource _src, final H2dVariableTypeCreated _var) {
        super(_src, _var);
        expr_ = new CtuluExpr();
        final TrPostDataCreatedExpr userExpr = (TrPostDataCreatedExpr) _src.getUserCreatedVar(_var);
        initExpr_ = userExpr == null ? null : userExpr.getFormule();
        src_.fillWithConstantVar(expr_);
        varVar2d_ = new HashMap();
        for (final Iterator iter = varToAdd_.iterator(); iter.hasNext(); ) {
            final H2dVariableType v = (H2dVariableType) iter.next();
            varVar2d_.put(expr_.addVar(v.getShortName(), v.getName()), v);
        }
        if (initExpr_ != null) {
            expr_.getParser().parseExpression(initExpr_);
        }
        editor_ = new CtuluValueEditorDouble(expr_);
        tfExpr_ = (BuTextField) editor_.createEditorComponent();
        tfExpr_.setText(initExpr_);
        final BuLabel lb = addLabel(CtuluLib.getS("Expression:"));
        lb.setLabelFor(tfExpr_);
        add(tfExpr_);
        updateExprState();
    }
    protected final void updateExprState() {
        final boolean valide = isExprOk();
        tfExpr_.setForeground(valide ? CtuluLibSwing.getDefaultTextFieldForegroundColor() : Color.RED);
        final JComponent c = (JComponent) tfExpr_.getClientProperty("labeledBy");
        if (c != null) {
            c.setForeground(valide ? CtuluLibSwing.getDefaultLabelForegroundColor() : Color.RED);
        }
        if (valide) {
            tfExpr_.setToolTipText(c == null ? null : c.getToolTipText());
        } else {
            tfExpr_.setToolTipText(expr_.getParser().getHtmlError());
        }
    }
    @Override
    public void apply() {
    }
    public final boolean isExprOk() {
        expr_.getParser().parseExpression(tfExpr_.getText());
        return !editor_.isEmpty(tfExpr_) && editor_.isValueValidFromComponent(tfExpr_);
    }
    @Override
    public boolean isModified() {
        if (var_ == null || initExpr_ == null) {
            return true;
        }
        return isVarModified() || (!initExpr_.equals(tfExpr_.getText()));
    }
    @Override
    public boolean addError(final StringBuffer _b) {
        if (!isExprOk()) {
            if (_b.length() > 0) {
                _b.append(CtuluLibString.LINE_SEP);
            }
            _b.append(CtuluLib.getS("L'expression est invalide"));
            return true;
        }
        return false;
    }
    @Override
    public TrPostDataCreated createData() {
        return TrPostDataCreatedExpr.createData(src_, expr_.getParser(), varVar2d_);
    }
}
