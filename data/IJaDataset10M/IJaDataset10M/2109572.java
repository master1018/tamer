package com.astrium.faceo.client.bean.programming.sps2.confirm;

import java.io.Serializable;

/**
public class ConfirmRequestBean implements Serializable {

    /**
    private static final long serialVersionUID = -8811539059920934267L;

    /** sensor urn identifier */
    private String sensorUrn = null;

    /** task identifier */
    private String taskId = null;

    /**
    public ConfirmRequestBean() {
    }

    /** 
    public String getTaskId() {
        return (this.taskId != null) ? this.taskId : "";
    }

    /** 
    public String getSensorUrn() {
        return (this.sensorUrn != null) ? this.sensorUrn : "";
    }

    /** 
    public void setTaskId(String _taskId) {
        this.taskId = _taskId;
    }

    /** 
    public void setSensorUrn(String _sensorUrn) {
        this.sensorUrn = _sensorUrn;
    }
}