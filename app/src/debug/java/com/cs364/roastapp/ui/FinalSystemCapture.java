package com.cs364.roastapp.ui;

import android.app.Activity;
import android.app.Instrumentation;
import android.app.NotificationManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import androidx.core.widget.NestedScrollView;
import com.cs364.roastapp.R;
import com.cs364.roastapp.core.AppServices;
import com.cs364.roastapp.core.HomeActivity;
import com.cs364.roastapp.history.HistoryActivity;
import com.cs364.roastapp.history.RoastEventEntity;
import com.cs364.roastapp.onboarding.OnboardingPrefs;
import com.cs364.roastapp.roast.RoastNotifier;
import com.cs364.roastapp.settings.SettingsManager;
import java.io.File;
import java.io.FileOutputStream;
import java.util.*;

/** Explicit dedicated-emulator inspection; fixtures never run in production. */
@android.annotation.TargetApi(28)
public final class FinalSystemCapture extends Instrumentation {
    private Bundle args; private String prefix;
    private final List<Activity> opened = new ArrayList<>();
    @Override public void onCreate(Bundle options) { super.onCreate(options); args=options; start(); }
    @Override public void onStart() {
        Bundle result=new Bundle();
        SettingsManager settings=new SettingsManager(getTargetContext());
        String lang=settings.getLanguage(); boolean night=settings.isNightMode();
        Set<Long> original=new HashSet<>();
        for(RoastEventEntity item:AppServices.getDatabase(getTargetContext()).roastEventDao().getAllEvents())original.add(item.id);
        prefix=args.getString("prefix","phone390-day");
        try {
            getTargetContext().getSharedPreferences("onboarding",0).edit().putBoolean("done",true).commit(); // Dedicated emulator only.
            if("prepare".equals(args.getString("phase"))) { result.putString("stream","Final capture prepared"); finish(Activity.RESULT_OK,result); return; }
            runOnMainSync(()->{settings.setLanguage(args.getString("language","th")); settings.setNightMode("night".equals(args.getString("theme","day")));}); pause();
            HomeActivity home=launch(HomeActivity.class); capture("home-live");
            verifyReviewFixes(home);
            if(home.findViewById(R.id.btn_nav_history)==null)throw new AssertionError("History entry missing from Home");
            if(home.findViewById(R.id.pid_monitoring_panel).getVisibility()!=View.GONE)throw new AssertionError("Granted permissions still show warning");
            ActivityMonitor monitor=addMonitor(HistoryActivity.class.getName(),null,false);
            runOnMainSync(()->home.findViewById(R.id.btn_nav_history).performClick());
            Activity history=waitForMonitorWithTimeout(monitor,5000);removeMonitor(monitor);
            if(history==null)throw new AssertionError("Home history navigation failed");opened.add(history);pause();capture("history-live");
            if(original.isEmpty() && history.findViewById(R.id.layout_history_empty).getVisibility()!=View.VISIBLE)throw new AssertionError("History empty state missing");
            runOnMainSync(history::finish);pause();
            RoastNotifier notifier=new RoastNotifier(getTargetContext());
            NotificationManager nm=getTargetContext().getSystemService(NotificationManager.class);
            for(int level=1;level<=3;level++) {
                if(level==1)notifier.showLevel1("com.android.chrome");
                else if(level==2)notifier.showLevel2("com.android.chrome");
                else notifier.showLevel3("com.android.chrome",0);
                pause(); final int expected=level;
                boolean posted=Arrays.stream(nm.getActiveNotifications()).anyMatch(n->n.getNotification().getChannelId().equals("roast_level_"+expected)&&n.getNotification().getSmallIcon().getResId()==R.drawable.pid_ic_notification);
                if(!posted)throw new AssertionError("Notification level "+level+" or branded icon missing");
                getUiAutomation().executeShellCommand("cmd statusbar expand-notifications").close();pause();capture("notification-"+level);
                getUiAutomation().executeShellCommand("cmd statusbar collapse").close();notifier.cancel("com.android.chrome");pause();
            }
            HistoryActivity filled=launch(HistoryActivity.class);capture("history-filled");
            if(filled.findViewById(R.id.recycler_history_events).getVisibility()!=View.VISIBLE)throw new AssertionError("Posted notifications absent from history");
            runOnMainSync(()->((NestedScrollView)filled.findViewById(R.id.pid_scroll)).fullScroll(View.FOCUS_DOWN));pause();capture("history-list");
            int count=AppServices.getDatabase(getTargetContext()).roastEventDao().getAllEvents().size();
            runOnMainSync(()->filled.findViewById(R.id.btn_history_clear_all).performClick());pause();capture("history-clear-confirm");
            sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);pause();
            if(AppServices.getDatabase(getTargetContext()).roastEventDao().getAllEvents().size()!=count)throw new AssertionError("Cancel cleared history");
            result.putString("stream","Final system checks passed: "+prefix);
        }catch(Throwable error){result.putString("stream",android.util.Log.getStackTraceString(error));result.putString("error",error.toString());}
        finally {
            for(RoastEventEntity item:AppServices.getDatabase(getTargetContext()).roastEventDao().getAllEvents())if(!original.contains(item.id))AppServices.getDatabase(getTargetContext()).roastEventDao().delete(item);
            runOnMainSync(()->{for(Activity a:opened)if(!a.isFinishing())a.finish();settings.setLanguage(lang);settings.setNightMode(night);});
        }
        finish(result.containsKey("error")?1:Activity.RESULT_OK,result);
    }
    private void verifyReviewFixes(HomeActivity home)throws Exception{
        java.util.List<Long> dates=new java.util.ArrayList<>();java.util.Calendar day=java.util.Calendar.getInstance();day.set(java.util.Calendar.HOUR_OF_DAY,0);day.set(java.util.Calendar.MINUTE,0);day.set(java.util.Calendar.SECOND,0);day.set(java.util.Calendar.MILLISECOND,0);day.add(java.util.Calendar.DAY_OF_YEAR,-6);
        for(int i=0;i<7;i++){dates.add(day.getTimeInMillis());day.add(java.util.Calendar.DAY_OF_YEAR,1);}
        java.util.Set<String> packages=new java.util.HashSet<>(java.util.Arrays.asList("a","b"));
        java.util.Map<Long,java.util.Map<String,Long>> buckets=new java.util.HashMap<>();
        buckets.put(dates.get(0),new java.util.HashMap<>());buckets.get(dates.get(0)).put("a",0L);buckets.get(dates.get(0)).put("b",0L);
        buckets.put(dates.get(1),java.util.Collections.singletonMap("a",120000L));
        java.util.List<com.cs364.roastapp.history.WeeklyUsageData.Day> rows=com.cs364.roastapp.history.WeeklyUsageData.build(dates,packages,buckets);
        if(rows.get(0).usedMillis==null||rows.get(0).usedMillis!=0L||rows.get(1).usedMillis!=null||rows.get(2).usedMillis!=null)throw new AssertionError("Missing buckets became zero or partial totals");
        runOnMainSync(()->home.renderMonitoringState(MonitoringStatusUi.State.STARTING,null));
        if(home.findViewById(R.id.btn_nav_onboarding).isEnabled())throw new AssertionError("Starting action is enabled");capture("monitor-starting");
        java.lang.reflect.Method failure=HomeActivity.class.getDeclaredMethod("showMonitoringFailure");failure.setAccessible(true);
        runOnMainSync(()->{try{failure.invoke(home);}catch(Exception e){throw new RuntimeException(e);}});capture("monitor-failed");
        runOnMainSync(()->home.findViewById(R.id.btn_nav_onboarding).performClick());pause();pause();
        if(home.findViewById(R.id.pid_monitoring_panel).getVisibility()!=View.GONE)throw new AssertionError("Real retry with no tracked apps did not settle READY");capture("monitor-retry-success");
        runOnMainSync(()->home.renderAppUsage(java.util.Arrays.asList(new AppLimitsUi.App("fixture.instagram","Instagram",home.getDrawable(R.drawable.pid_ic_apps),18,30),new AppLimitsUi.App("fixture.youtube","YouTube",home.getDrawable(R.drawable.pid_ic_play),12,45))));capture("home-priorities");
        runOnMainSync(()->((android.widget.ScrollView)home.findViewById(R.id.pid_scroll)).fullScroll(View.FOCUS_DOWN));pause();capture("home-summary");
        com.cs364.roastapp.settings.SettingsActivity settings=launch(com.cs364.roastapp.settings.SettingsActivity.class);
        runOnMainSync(()->((android.widget.ScrollView)settings.findViewById(R.id.pid_scroll)).fullScroll(View.FOCUS_DOWN));pause();capture("settings-persona");
        if(settings.findViewById(R.id.btn_nav_roast_persona)==null||home.findViewById(R.id.btn_nav_roast_persona)!=null)throw new AssertionError("Persona not moved to Settings");runOnMainSync(settings::finish);pause();
        com.cs364.roastapp.history.HistoryActivity history=launch(com.cs364.roastapp.history.HistoryActivity.class);
        java.lang.reflect.Constructor<com.cs364.roastapp.history.UsageHistorySnapshot> constructor=com.cs364.roastapp.history.UsageHistorySnapshot.class.getDeclaredConstructor(com.cs364.roastapp.history.UsageHistorySnapshot.State.class,java.util.List.class,long.class,long.class);constructor.setAccessible(true);
        java.util.List<com.cs364.roastapp.history.WeeklyUsageData.Day> mixed=new java.util.ArrayList<>();
        Long[] values={1200000L,0L,null,5400000L,null,2700000L,1800000L};for(int i=0;i<7;i++)mixed.add(new com.cs364.roastapp.history.WeeklyUsageData.Day(dates.get(i),values[i]));
        com.cs364.roastapp.history.UsageHistorySnapshot partial=constructor.newInstance(com.cs364.roastapp.history.UsageHistorySnapshot.State.AVAILABLE,mixed,dates.get(0),dates.get(6));
        if(partial.knownDays!=5||partial.knownMillis!=11100000L)throw new AssertionError("Coverage/known total mismatch");
        runOnMainSync(()->history.renderUsage(partial));capture("history-partial");
        runOnMainSync(()->{history.findViewById(R.id.history_daily_toggle).performClick();((NestedScrollView)history.findViewById(R.id.pid_scroll)).fullScroll(View.FOCUS_DOWN);});pause();capture("history-daily-values");
        if(((android.widget.LinearLayout)history.findViewById(R.id.history_daily_values)).getChildCount()!=7)throw new AssertionError("Daily values not exposed");
        for(com.cs364.roastapp.history.UsageHistorySnapshot.State state:com.cs364.roastapp.history.UsageHistorySnapshot.State.values())if(state!=com.cs364.roastapp.history.UsageHistorySnapshot.State.AVAILABLE){
            com.cs364.roastapp.history.UsageHistorySnapshot fixture=constructor.newInstance(state,java.util.Collections.emptyList(),dates.get(0),dates.get(6));runOnMainSync(()->history.renderUsage(fixture));capture("history-"+state.name().toLowerCase(java.util.Locale.US));
            if(history.findViewById(R.id.chart_history_weekly).getVisibility()!=View.GONE)throw new AssertionError("Unusable data still shows a chart");
        }
        runOnMainSync(history::finish);pause();
    }
    private void pause(){try{Thread.sleep(700);}catch(InterruptedException e){Thread.currentThread().interrupt();}}
    private <T extends Activity>T launch(Class<T> type){Activity a=startActivitySync(new Intent(getTargetContext(),type).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));opened.add(a);pause();return type.cast(a);}
    private void capture(String name)throws Exception{pause();Bitmap bitmap=getUiAutomation().takeScreenshot();if(bitmap==null)throw new IllegalStateException("No screenshot");File folder=getTargetContext().getExternalFilesDir("final-ui");try(FileOutputStream out=new FileOutputStream(new File(folder,prefix+"-"+name+".png"))){bitmap.compress(Bitmap.CompressFormat.PNG,100,out);}bitmap.recycle();}
}
