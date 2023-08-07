public class RuleConditionalExpression implements IExternalReferenceRule {
    @Override
    public Boolean Apply(IASTNode Node, StackDeclaration declDefined, Set<ExternalReference> externalRef, List<IASTNode> NotManaged) {
        if (!(Node instanceof IASTConditionalExpression)) return false;
        ListExpressionRule Rules = new ListExpressionRule();
        IASTConditionalExpression ConditionaleExpression = (IASTConditionalExpression) Node;
        Rules.Apply(ConditionaleExpression.getLogicalConditionExpression(), declDefined, externalRef, NotManaged);
        Rules.Apply(ConditionaleExpression.getNegativeResultExpression(), declDefined, externalRef, NotManaged);
        Rules.Apply(ConditionaleExpression.getPositiveResultExpression(), declDefined, externalRef, NotManaged);
        return true;
    }
}
