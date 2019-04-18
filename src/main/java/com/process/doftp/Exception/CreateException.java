package com.process.doftp.Exception;

import com.process.doftp.emun.UploadStatus;

/**
 * @author ：mayy
 * @date ：Created in 2019/4/18
 */
public class CreateException extends Exception{
    private static final long serialVersionUID = 1L;

    private Integer errCode;
    private String errMessage;

    public CreateException(Throwable cause, Integer errCode, String errMessage) {
        super(cause);
        this.errCode = errCode;
        this.errMessage = errMessage;
    }

    public CreateException(Integer errCode, String errMessage) {
        this.errCode = errCode;
        this.errMessage = errMessage;
    }
    public CreateException(Integer errCode, UploadStatus uploadStatus) {
        this.errCode = errCode;
        this.errMessage = uploadStatus.toString();
    }


    public Integer getErrCode() {
        return errCode;
    }

    public String getErrMessage() {
        return errMessage;
    }
}