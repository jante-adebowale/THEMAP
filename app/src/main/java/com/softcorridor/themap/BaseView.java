package com.softcorridor.themap;

import android.app.Activity;

public interface BaseView<T> {
    void setPresenter(T presenter);
    Activity getViewActivity();
}
