package edu.harvard.fas.rregan.requel.ui.annotation;

import java.text.MessageFormat;
import java.util.Set;
import java.util.TreeSet;
import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.TextArea;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import org.apache.log4j.Logger;
import org.hibernate.validator.InvalidStateException;
import org.hibernate.validator.InvalidValue;
import echopointng.text.StringDocumentEx;
import edu.harvard.fas.rregan.command.CommandHandler;
import edu.harvard.fas.rregan.repository.EntityException;
import edu.harvard.fas.rregan.requel.annotation.Annotatable;
import edu.harvard.fas.rregan.requel.annotation.Annotation;
import edu.harvard.fas.rregan.requel.annotation.Argument;
import edu.harvard.fas.rregan.requel.annotation.ArgumentPositionSupportLevel;
import edu.harvard.fas.rregan.requel.annotation.Issue;
import edu.harvard.fas.rregan.requel.annotation.Position;
import edu.harvard.fas.rregan.requel.annotation.command.AnnotationCommandFactory;
import edu.harvard.fas.rregan.requel.annotation.command.DeleteArgumentCommand;
import edu.harvard.fas.rregan.requel.annotation.command.EditArgumentCommand;
import edu.harvard.fas.rregan.requel.project.Project;
import edu.harvard.fas.rregan.requel.project.ProjectOrDomainEntity;
import edu.harvard.fas.rregan.requel.project.StakeholderPermissionType;
import edu.harvard.fas.rregan.requel.project.UserStakeholder;
import edu.harvard.fas.rregan.requel.ui.AbstractRequelEditorPanel;
import edu.harvard.fas.rregan.requel.user.User;
import net.sf.echopm.navigation.event.DeletedEntityEvent;
import net.sf.echopm.navigation.event.UpdateEntityEvent;
import net.sf.echopm.panel.editor.CombinedListModel;

/**
 * @author ron
 */
public class ArgumentEditorPanel extends AbstractRequelEditorPanel {

    private static final Logger log = Logger.getLogger(ArgumentEditorPanel.class);

    static final long serialVersionUID = 0L;

    /**
	 * The name to use in the ArgumentEditorPanel.properties file to set the
	 * label of the argument text field. If the property is undefined "Argument"
	 * is used.
	 */
    public static final String PROP_LABEL_ARGUMENT = "Argument.Label";

    /**
	 * The name to use in the ArgumentEditorPanel.properties file to set the
	 * label of the position support level field. If the property is undefined
	 * "Position Support Level" is used.
	 */
    public static final String PROP_LABEL_POSITION_SUPPORT_LEVEL = "PositionSupportLevel.Label";

    private final AnnotationCommandFactory annotationCommandFactory;

    private UpdateListener updateListener;

    private boolean deleted = false;

    /**
	 * @param commandHandler
	 * @param annotationCommandFactory
	 */
    public ArgumentEditorPanel(CommandHandler commandHandler, AnnotationCommandFactory annotationCommandFactory) {
        this(ArgumentEditorPanel.class.getName(), commandHandler, annotationCommandFactory);
    }

    /**
	 * @param resourceBundleName
	 * @param commandHandler
	 * @param annotationCommandFactory
	 */
    public ArgumentEditorPanel(String resourceBundleName, CommandHandler commandHandler, AnnotationCommandFactory annotationCommandFactory) {
        super(resourceBundleName, Argument.class, commandHandler);
        this.annotationCommandFactory = annotationCommandFactory;
    }

    /**
	 * If the editor is editing an existing argument the title specified in the
	 * properties file as PROP_EXISTING_OBJECT_PANEL_TITLE If that property is
	 * not set it then tries the standard PROP_PANEL_TITLE and if that does not
	 * exist it defaults to:<br>
	 * "Edit Argument"<br>
	 * For a new argument it first tries PROP_NEW_OBJECT_PANEL_TITLE, then
	 * PROP_PANEL_TITLE and finally defaults to:<br>
	 * "New Argument"<br>
	 * 
	 * @see AbstractEditorPanel.PROP_EXISTING_OBJECT_PANEL_TITLE
	 * @see AbstractEditorPanel.PROP_NEW_OBJECT_PANEL_TITLE
	 * @see Panel.PROP_PANEL_TITLE
	 * @see net.sf.echopm.panel.AbstractPanel#getTitle()
	 */
    @Override
    public String getTitle() {
        if (getArgument() != null) {
            String msgPattern = getResourceBundleHelper(getLocale()).getString(PROP_EXISTING_OBJECT_PANEL_TITLE, getResourceBundleHelper(getLocale()).getString(PROP_PANEL_TITLE, "Edit Argument"));
            return MessageFormat.format(msgPattern, getPosition().toString());
        } else {
            String msg = getResourceBundleHelper(getLocale()).getString(PROP_NEW_OBJECT_PANEL_TITLE, getResourceBundleHelper(getLocale()).getString(PROP_PANEL_TITLE, "New Argument"));
            return msg;
        }
    }

    @Override
    public void setup() {
        super.setup();
        Argument argument = getArgument();
        if (argument != null) {
            addInput("text", PROP_LABEL_ARGUMENT, "Argument", new TextArea(), new StringDocumentEx(argument.getText()));
            addInput("supportLevel", PROP_LABEL_POSITION_SUPPORT_LEVEL, "Position Support Level", new SelectField(), new CombinedListModel(getArgumentPositionSupportLevelNames(), argument.getSupportLevel().toString(), true));
        } else {
            addInput("text", PROP_LABEL_ARGUMENT, "Position", new TextArea(), new StringDocumentEx());
            addInput("supportLevel", PROP_LABEL_POSITION_SUPPORT_LEVEL, "Position Support Level", new SelectField(), new CombinedListModel(getArgumentPositionSupportLevelNames(), "", true));
        }
        if (updateListener != null) {
            getEventDispatcher().removeEventTypeActionListener(UpdateEntityEvent.class, updateListener);
        }
        updateListener = new UpdateListener(this);
        getEventDispatcher().addEventTypeActionListener(UpdateEntityEvent.class, updateListener);
    }

    @Override
    public void dispose() {
        super.dispose();
        removeAll();
        if (updateListener != null) {
            getEventDispatcher().removeEventTypeActionListener(UpdateEntityEvent.class, updateListener);
            updateListener = null;
        }
    }

    private Set<String> getArgumentPositionSupportLevelNames() {
        Set<String> supportLevelNames = new TreeSet<String>();
        for (ArgumentPositionSupportLevel supportLevel : ArgumentPositionSupportLevel.values()) {
            supportLevelNames.add(supportLevel.toString());
        }
        return supportLevelNames;
    }

    @Override
    public boolean isReadOnlyMode() {
        User user = (User) getApp().getUser();
        Project project = getProject();
        if (project != null) {
            UserStakeholder stakeholder = project.getUserStakeholder(user);
            if (stakeholder != null) {
                return !stakeholder.hasPermission(Annotation.class, StakeholderPermissionType.Edit);
            }
        }
        return false;
    }

    @Override
    public void cancel() {
        super.cancel();
        if (updateListener != null) {
            getEventDispatcher().removeEventTypeActionListener(UpdateEntityEvent.class, updateListener);
        }
    }

    @Override
    public void save() {
        try {
            super.save();
            EditArgumentCommand command = getAnnotationCommandFactory().newEditArgumentCommand();
            command.setArgument(getArgument());
            command.setPosition(getPosition());
            command.setEditedBy(getCurrentUser());
            command.setText(getInputValue("text", String.class));
            command.setSupportLevelName(getInputValue("supportLevel", String.class));
            command = getCommandHandler().execute(command);
            setValid(true);
            if (updateListener != null) {
                getEventDispatcher().removeEventTypeActionListener(UpdateEntityEvent.class, updateListener);
            }
            getEventDispatcher().dispatchEvent(new UpdateEntityEvent(this, command.getArgument()));
        } catch (EntityException e) {
            if ((e.getEntityPropertyNames() != null) && (e.getEntityPropertyNames().length > 0)) {
                for (String propertyName : e.getEntityPropertyNames()) {
                    setValidationMessage(propertyName, e.getMessage());
                }
            } else if ((e.getCause() != null) && (e.getCause() instanceof InvalidStateException)) {
                InvalidStateException ise = (InvalidStateException) e.getCause();
                for (InvalidValue invalidValue : ise.getInvalidValues()) {
                    String propertyName = invalidValue.getPropertyName();
                    setValidationMessage(propertyName, invalidValue.getMessage());
                }
            } else {
                setGeneralMessage(e.toString());
            }
        } catch (Exception e) {
            log.error("could not save the argument: " + e, e);
            setGeneralMessage("Could not save: " + e);
        }
    }

    @Override
    public void delete() {
        try {
            DeleteArgumentCommand deleteArgumentCommand = getAnnotationCommandFactory().newDeleteArgumentCommand();
            deleteArgumentCommand.setArgument(getArgument());
            deleteArgumentCommand.setEditedBy(getCurrentUser());
            deleteArgumentCommand = getCommandHandler().execute(deleteArgumentCommand);
            deleted = true;
            getEventDispatcher().dispatchEvent(new DeletedEntityEvent(this, getPosition()));
        } catch (Exception e) {
            setGeneralMessage("Could not delete entity: " + e);
        }
    }

    private Project getProject() {
        Project project = null;
        if (getTargetObject() != null) {
            Position position = null;
            if (getTargetObject() instanceof Position) {
                position = (Position) getTargetObject();
            } else if (getTargetObject() instanceof Argument) {
                position = ((Argument) getTargetObject()).getPosition();
            }
            if ((position != null) && !position.getIssues().isEmpty()) {
                for (Issue issue : position.getIssues()) {
                    for (Annotatable annotatable : issue.getAnnotatables()) {
                        if (annotatable instanceof Project) {
                            project = (Project) annotatable;
                            break;
                        } else if (annotatable instanceof ProjectOrDomainEntity) {
                            ProjectOrDomainEntity podEntity = (ProjectOrDomainEntity) annotatable;
                            if (podEntity.getProjectOrDomain() instanceof Project) {
                                project = (Project) podEntity.getProjectOrDomain();
                                break;
                            }
                        }
                    }
                }
            }
        }
        return project;
    }

    private Position getPosition() {
        if (getTargetObject() instanceof Position) {
            return (Position) getTargetObject();
        } else if (getTargetObject() instanceof Argument) {
            return ((Argument) getTargetObject()).getPosition();
        }
        return null;
    }

    private Argument getArgument() {
        if (getTargetObject() instanceof Argument) {
            return (Argument) getTargetObject();
        }
        return null;
    }

    private AnnotationCommandFactory getAnnotationCommandFactory() {
        return annotationCommandFactory;
    }

    private static class UpdateListener implements ActionListener {

        static final long serialVersionUID = 0L;

        private final ArgumentEditorPanel panel;

        private UpdateListener(ArgumentEditorPanel panel) {
            this.panel = panel;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (panel.deleted) {
                return;
            }
            Argument existingArgument = panel.getArgument();
            if ((e instanceof UpdateEntityEvent) && (existingArgument != null)) {
                UpdateEntityEvent event = (UpdateEntityEvent) e;
                Argument updatedArgument = null;
                if (event.getObject() instanceof Argument) {
                    updatedArgument = (Argument) event.getObject();
                    if (existingArgument.equals(updatedArgument)) {
                        if (event instanceof DeletedEntityEvent) {
                            panel.deleted = true;
                            panel.getEventDispatcher().dispatchEvent(new DeletedEntityEvent(this, panel, existingArgument));
                        } else {
                            panel.setInputValue("text", updatedArgument.getText());
                            panel.setTargetObject(updatedArgument);
                        }
                    }
                } else if ((event.getObject() instanceof Position) && (existingArgument.getPosition() != null) && existingArgument.getPosition().equals(event.getObject()) && (event instanceof DeletedEntityEvent)) {
                    panel.deleted = true;
                    panel.getEventDispatcher().dispatchEvent(new DeletedEntityEvent(this, panel, existingArgument));
                }
            }
        }
    }
}
