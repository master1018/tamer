public class LexerRuleCriteria implements IModelElementCriteria {
    public boolean accept(IModelElement element) {
        IRule rule = element.getAdapter(IRule.class);
        return rule == null ? false : rule.isLexerRule();
    }
}
