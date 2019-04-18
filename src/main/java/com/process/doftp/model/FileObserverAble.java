package com.process.doftp.model;

/**
 * @author ：mayy
 * @date ：Created in 2019/4/18
 */
public class FileObserverAble {

    private String name;
    private double finishPercent;
    private String upload;

    public void setKeyValue(String name, double finishPercent, String upload) {
        this.name = name;
        this.finishPercent = finishPercent;
        this.upload = upload;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getFinishPercent() {
        return finishPercent;
    }

    public void setFinishPercent(double finishPercent) {
        this.finishPercent = finishPercent;
    }

    public String getUpload() {
        return upload;
    }

    public void setUpload(String upload) {
        this.upload = upload;
    }
}
