package com.enigma.audiobook.models;

import com.enigma.audiobook.backend.models.responses.Page;

public class PageModel {
    private Page page;
    private Integer drawableResourceId;

    public PageModel(Page page, Integer drawableResourceId) {
        this.page = page;
        this.drawableResourceId = drawableResourceId;
    }

    public Page getPage() {
        return page;
    }

    public Integer getDrawableResourceId() {
        return drawableResourceId;
    }
}
