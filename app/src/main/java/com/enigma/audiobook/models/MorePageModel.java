package com.enigma.audiobook.models;

import com.enigma.audiobook.backend.models.responses.Page;

public class MorePageModel {
    private PageModel pageModel;

    public MorePageModel(Page page) {
        this.pageModel = new PageModel(page, null);
    }

    public MorePageModel(Page page, int drawableResourceId) {
        this.pageModel = new PageModel(page, drawableResourceId);
    }

    public Page getPage() {
        return pageModel.getPage();
    }

    public Integer getDrawableResourceId() {
        return pageModel.getDrawableResourceId();
    }
}
