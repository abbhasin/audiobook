package com.enigma.audiobook.models;

public class GodPageDetailsModel {
    private String htmlDescription;

    public GodPageDetailsModel(String htmlDescription) {
        this.htmlDescription = htmlDescription;
    }

    public String getHtmlDescription() {
//        return Html.fromHtml(htmlDescription, FROM_HTML_MODE_LEGACY).toString();
        return htmlDescription;
    }
}
