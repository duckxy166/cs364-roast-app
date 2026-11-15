package com.cs364.roastapp.ui;
import android.content.Context;
import com.cs364.roastapp.R;

public final class UsageTimeText {
    private UsageTimeText() {}
    public static String format(Context context,long millis) {
        if(millis>0&&millis<60000)return context.getString(R.string.review_duration_under_minute);
        long minutes=Math.max(0,millis)/60000;
        return minutes<60?context.getString(R.string.review_duration_short_minutes,minutes)
                :context.getString(R.string.review_duration_short_hours,minutes/60,minutes%60);
    }
}
