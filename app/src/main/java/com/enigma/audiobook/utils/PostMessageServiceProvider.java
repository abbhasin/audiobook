package com.enigma.audiobook.utils;

import com.enigma.audiobook.services.PostMessageService;

public interface PostMessageServiceProvider {
    PostMessageService getPostMessageService();
    boolean isServiceBound();
}
