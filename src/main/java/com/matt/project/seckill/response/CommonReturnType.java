package com.matt.project.seckill.response;

/**
 * @author matt
 * @create 2020-12-06 14:07
 */
public class CommonReturnType {

    private String status;

    private Object data;

    public static CommonReturnType create(Object result) {
        return CommonReturnType.create(result,"success");
    }

    public static CommonReturnType create(Object result,String status) {
        CommonReturnType commonReturnType = new CommonReturnType();
        commonReturnType.setData(result);
        commonReturnType.setStatus(status);
        return commonReturnType;

    }


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
