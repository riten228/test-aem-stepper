package com.riten.test.core.servlets;

public class NewsletterSubscriptionResponse {

    public static final int STATUS_SUBSCRIBED = 1;
    public static final int STATUS_ALREADY_SUBSCRIBED = 2;
    public static final int STATUS_ERROR = 0;

    private int status;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
