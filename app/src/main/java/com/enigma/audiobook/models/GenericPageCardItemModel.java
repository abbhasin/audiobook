package com.enigma.audiobook.models;

import com.enigma.audiobook.adapters.GodPageRVAdapter;

public class GenericPageCardItemModel<T extends Enum<T> & ModelClassRetriever> {
    private Object cardItem;
    private T type;

    public GenericPageCardItemModel(Object cardItem, T type) {
        this.type = type;
        if (!type.getModelClazz().isAssignableFrom(cardItem.getClass())) {
            throw new IllegalStateException("wrong type of object being assigned");
        }
        this.cardItem = cardItem;

    }

    public Object getCardItem() {
        return cardItem;
    }

    public T getType() {
        return type;
    }

    public static void test() {
        new GenericPageCardItemModel<GodPageRVAdapter.GodPageViewTypes>(new GodPageDetailsModel("asd"), GodPageRVAdapter.GodPageViewTypes.DETAILS);
    }
}
