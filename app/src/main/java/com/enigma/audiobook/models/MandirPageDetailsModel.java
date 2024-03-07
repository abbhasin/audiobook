package com.enigma.audiobook.models;

import static android.text.Html.FROM_HTML_SEPARATOR_LINE_BREAK_PARAGRAPH;

import android.text.Html;

public class MandirPageDetailsModel {
    private String htmlDescription;

    public MandirPageDetailsModel(String htmlDescription) {
        this.htmlDescription = htmlDescription;
    }

    public String getHtmlDescription() {
//        return Html.fromHtml(htmlDescription, FROM_HTML_SEPARATOR_LINE_BREAK_PARAGRAPH).toString();
        return htmlDescription;
    }
}
