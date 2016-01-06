package org.zywx.wbpalmstar.plugin.uexdevice.vo;

import java.io.Serializable;

public class ResultIsEnableVO extends FunctionDataVO implements Serializable{
    private static final long serialVersionUID = -5175035358106782186L;
    private boolean isEnable;

    public boolean isEnable() {
        return isEnable;
    }

    public void setIsEnable(boolean isEnable) {
        this.isEnable = isEnable;
    }
}
