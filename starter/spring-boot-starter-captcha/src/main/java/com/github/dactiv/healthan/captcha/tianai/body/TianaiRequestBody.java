package com.github.dactiv.healthan.captcha.tianai.body;

import cloud.tianai.captcha.common.constant.CaptchaTypeConstant;

import java.io.Serializable;

public class TianaiRequestBody implements Serializable {

    private static final long serialVersionUID = 1802399111245035485L;

    /**
     * 生成验证码的图片类型，
     */
    private String generateImageType = CaptchaTypeConstant.SLIDER;

    public TianaiRequestBody() {
    }

    public String getGenerateImageType() {
        return generateImageType;
    }

    public void setGenerateImageType(String generateImageType) {
        this.generateImageType = generateImageType;
    }
}
