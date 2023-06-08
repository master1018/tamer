package org.apache.jetspeed.util.ant;

import java.io.File;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;
import org.apache.jetspeed.util.OverwriteProperties;

/**
public class OverwritePropertiesTask extends Task {

    /** File to merge properties into */
    private File mergeBaseProperties;

    /** File to merge properties from */
    private File mergeProperties;

    /** Directory to look for includes in */
    private File includesDir;

    /** Fail on error flag */
    private boolean failonerror = true;

    /**
    public void setMergeBaseProperties(File mergeBaseProperties) {
        this.mergeBaseProperties = mergeBaseProperties;
    }

    /**
    public void setMergeProperties(File mergeProperties) {
        this.mergeProperties = mergeProperties;
    }

    /**
    public void setIncludesDir(File includesDir) {
        this.includesDir = includesDir;
    }

    /**
    public void setFailOnError(boolean failonerror) {
        this.failonerror = failonerror;
    }

    /**
    public File getMergeBaseProperties() {
        return mergeBaseProperties;
    }

    /**
    public File getMergeProperties() {
        return mergeProperties;
    }

    /**
    public File getIncludesDir() {
        return includesDir;
    }

    /**
    public void execute() throws BuildException {
        try {
            OverwriteProperties overwriteProperties = new OverwriteProperties();
            overwriteProperties.setBaseProperties(getMergeBaseProperties());
            overwriteProperties.setProperties(getMergeProperties());
            overwriteProperties.setIncludeRoot(getIncludesDir());
            overwriteProperties.execute();
        } catch (Exception e) {
            if (!this.failonerror) {
                log(e.toString());
            } else {
                throw new BuildException(e.toString());
            }
        }
    }
}