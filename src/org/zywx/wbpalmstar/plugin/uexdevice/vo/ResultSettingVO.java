package org.zywx.wbpalmstar.plugin.uexdevice.vo;

import java.io.Serializable;

public class ResultSettingVO extends FunctionDataVO implements Serializable{
    private static final long serialVersionUID = -2247584233056313072L;
    private int errorCode;

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }
}
