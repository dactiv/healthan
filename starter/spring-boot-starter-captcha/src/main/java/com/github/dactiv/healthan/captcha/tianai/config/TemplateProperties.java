package com.github.dactiv.healthan.captcha.tianai.config;

import java.io.Serializable;

public class TemplateProperties implements Serializable {
    private static final long serialVersionUID = -2399006088823656944L;

    /** 标签.*/
    private String tag;

    private ResourceProperties activeImage;

    private ResourceProperties fixedImage;

    private ResourceProperties maskImage;

    public TemplateProperties() {
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public ResourceProperties getActiveImage() {
        return activeImage;
    }

    public void setActiveImage(ResourceProperties activeImage) {
        this.activeImage = activeImage;
    }

    public ResourceProperties getFixedImage() {
        return fixedImage;
    }

    public void setFixedImage(ResourceProperties fixedImage) {
        this.fixedImage = fixedImage;
    }

    public ResourceProperties getMaskImage() {
        return maskImage;
    }

    public void setMaskImage(ResourceProperties maskImage) {
        this.maskImage = maskImage;
    }
}
