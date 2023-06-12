package org.eledge.domain.auto;

import org.eledge.domain.AssignmentQuestion;
import org.eledge.domain.EledgeDataObject;

/**
 * Class _AssignmentQuestionAttribute was generated by Cayenne.
 * It is probably a good idea to avoid changing this class manually,
 * since it may be overwritten next time code is regenerated.
 * If you need to make any customizations, please use subclass.
 */
public abstract class _AssignmentQuestionAttribute extends EledgeDataObject {

    public static final String NAME_PROPERTY = "name";

    public static final String VALUE_PROPERTY = "value";

    public static final String QUESTION_PROPERTY = "question";

    public static final String ID_PK_COLUMN = "id";

    public void setName(String name) {
        writeProperty("name", name);
    }

    public String getName() {
        return (String) readProperty("name");
    }

    public void setValue(String value) {
        writeProperty("value", value);
    }

    public String getValue() {
        return (String) readProperty("value");
    }

    public void setQuestion(AssignmentQuestion question) {
        setToOneTarget("question", question, true);
    }

    public AssignmentQuestion getQuestion() {
        return (AssignmentQuestion) readProperty("question");
    }
}
