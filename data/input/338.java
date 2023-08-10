public class SwitchBlockBuilder extends ConditionalBlockBuilder<SwitchBlockInfo, UnresolvedSwitchBlockInfo> {
    public SwitchBlockBuilder(final BuildDataManager targetDataManager, final ExpressionElementManager expressionManager, final LocalVariableDeclarationStatementBuilder variableBuilder) {
        super(targetDataManager, new SwitchBlockStateManager(), expressionManager, variableBuilder);
    }
    @Override
    public void stateChanged(final StateChangeEvent<AstVisitEvent> event) {
        super.stateChanged(event);
        final StateChangeEventType type = event.getType();
        if (this.getCurrentSpace() instanceof UnresolvedSwitchBlockInfo) {
            final UnresolvedSwitchBlockInfo currentSwitch = (UnresolvedSwitchBlockInfo) this.getCurrentSpace();
            final AstVisitEvent trigger = event.getTrigger();
            if (type.equals(SWITCH_BLOCK_STATE_CHANGE.ENTER_CASE_ENTRY)) {
            } else if (type.equals(SWITCH_BLOCK_STATE_CHANGE.EXIT_CASE_ENTYR)) {
                final UnresolvedCaseLabelInfo caseLabel = new UnresolvedCaseLabelInfo(this.expressionManager.getLastPoppedExpressionElement().getUsage());
                final UnresolvedCaseEntryInfo caseEntry = new UnresolvedCaseEntryInfo(currentSwitch, caseLabel);
                caseLabel.setOwnerCaseEntry(caseEntry);
                caseLabel.setOuterUnit(caseEntry.getOwnerSwitchBlock());
                caseEntry.setOuterUnit(this.buildManager.getCurrentUnit());
                caseEntry.setFromLine(trigger.getStartLine());
                caseEntry.setFromColumn(trigger.getStartColumn());
                caseEntry.setToLine(trigger.getEndLine());
                caseEntry.setToColumn(trigger.getEndColumn());
                currentSwitch.addStatement(caseEntry);
            } else if (type.equals(SWITCH_BLOCK_STATE_CHANGE.ENTER_DEFAULT_ENTRY)) {
                final UnresolvedDefaultEntryInfo defaultEntry = new UnresolvedDefaultEntryInfo(currentSwitch, this.buildManager.getCurrentUnit());
                defaultEntry.setFromLine(trigger.getStartLine());
                defaultEntry.setFromColumn(trigger.getStartColumn());
                defaultEntry.setToLine(trigger.getEndLine());
                defaultEntry.setToColumn(trigger.getEndColumn());
                currentSwitch.addStatement(defaultEntry);
            } else if (type.equals(SWITCH_BLOCK_STATE_CHANGE.EXIT_DEFAULT_ENTRY)) {
            }
        }
    }
    @Override
    protected UnresolvedSwitchBlockInfo createUnresolvedBlockInfo(final UnresolvedLocalSpaceInfo<?> outerSpace) {
        return new UnresolvedSwitchBlockInfo(outerSpace);
    }
}
