package com.cs364.roastapp.settings;

import android.app.Activity;
import android.app.Application;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class SettingsInitProvider extends ContentProvider {

    @Override
    public boolean onCreate() {
        Context context = getContext();
        if (context != null) {
            Context appContext = context.getApplicationContext();
            if (appContext instanceof Application app) {
                app.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
                    @Override
                    public void onActivityPreCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
                        new SettingsManager(activity).applySavedSettings();
                    }

                    @Override
                    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
                        new SettingsManager(activity).applySavedSettings();
                    }

                    @Override
                    public void onActivityStarted(@NonNull Activity activity) {
                    }

                    @Override
                    public void onActivityResumed(@NonNull Activity activity) {
                    }

                    @Override
                    public void onActivityPaused(@NonNull Activity activity) {
                    }

                    @Override
                    public void onActivityStopped(@NonNull Activity activity) {
                    }

                    @Override
                    public void onActivitySaveInstanceState(@NonNull Activity activity, @Nullable Bundle outState) {
                    }

                    @Override
                    public void onActivityDestroyed(@NonNull Activity activity) {
                    }
                });
            }
        }
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        return null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        return 0;
    }
}
