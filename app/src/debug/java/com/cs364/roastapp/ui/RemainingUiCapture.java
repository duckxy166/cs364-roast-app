package com.cs364.roastapp.ui;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import com.cs364.roastapp.R;
import com.cs364.roastapp.core.AppServices;
import com.cs364.roastapp.core.HomeActivity;
import com.cs364.roastapp.focus.FocusActivity;
import com.cs364.roastapp.roast.RoastNotifier;
import com.cs364.roastapp.settings.SettingsActivity;
import com.cs364.roastapp.settings.SettingsManager;
import com.google.android.material.textfield.TextInputLayout;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/** Dedicated-emulator UI verification. Restores the preferences it changes. */
public final class RemainingUiCapture extends Instrumentation {
    private Bundle args;
    private String prefix;
    private final List<Activity> opened = new ArrayList<>();
    @Override public void onCreate(Bundle options) { super.onCreate(options); args=options; start(); }
    @Override public void onStart() {
        SettingsManager settings=new SettingsManager(getTargetContext());
        String language=settings.getLanguage(); boolean night=settings.isNightMode();
        prefix=args.getString("prefix","phone-day"); Bundle result=new Bundle();
        try {
            runOnMainSync(()->{settings.setLanguage(args.getString("language","th"));settings.setNightMode("night".equals(args.getString("theme","day")));}); pause();
            HomeActivity home=launch(HomeActivity.class);
            runOnMainSync(()->home.renderAppUsage(java.util.Arrays.asList(
                new AppLimitsUi.App("fixture.instagram","Instagram",home.getDrawable(R.drawable.pid_ic_apps),18,30),
                new AppLimitsUi.App("fixture.youtube","YouTube",home.getDrawable(R.drawable.pid_ic_play),12,45))));
            runOnMainSync(()->home.renderPermissionWarning(true)); capture("monitor-permission");
            runOnMainSync(()->home.renderMonitoringState(MonitoringStatusUi.State.STOPPED,()->home.renderMonitoringState(MonitoringStatusUi.State.READY,null)));
            capture("monitor-stopped"); click(home,R.id.btn_nav_onboarding);
            if(home.findViewById(R.id.pid_monitoring_panel).getVisibility()!=View.GONE)throw new AssertionError("Ready state still shows warning");
            capture("monitor-ready");
            FocusActivity focus=launch(FocusActivity.class);
            runOnMainSync(()->((EditText)focus.findViewById(R.id.edit_focus_minutes)).setText("0"));click(focus,R.id.btn_focus_start);
            if(((TextInputLayout)focus.findViewById(R.id.input_layout_focus_minutes)).getError()==null)throw new AssertionError("Focus error is not inline");
            capture("focus-error");
            runOnMainSync(()->((EditText)focus.findViewById(R.id.edit_focus_minutes)).setText("500"));click(focus,R.id.btn_focus_start);
            Method pauseTicker=FocusActivity.class.getDeclaredMethod("pauseTickerAndSaveRemaining");pauseTicker.setAccessible(true);
            Method display=FocusActivity.class.getDeclaredMethod("updateTimerDisplay",long.class);display.setAccessible(true);
            runOnMainSync(()->{try{pauseTicker.invoke(focus);}catch(Exception error){throw new RuntimeException(error);}});
            long[] millis={3599000L,3600000L,29993000L,59999000L};String[] expected={"59:59","1:00:00","8:19:53","16:39:59"};
            for(int i=0;i<millis.length;i++){
                final int index=i;
                runOnMainSync(()->{try{display.invoke(focus,millis[index]);}catch(Exception error){throw new RuntimeException(error);}});
                if(!expected[i].equals(((TextView)focus.findViewById(R.id.text_focus_timer)).getText().toString()))throw new AssertionError("Clock formatting failed: "+expected[i]);
                if(i==2)capture("focus-hours");
            }
            capture("focus-long-hours");
            Field active=FocusActivity.class.getDeclaredField("isSessionActive");active.setAccessible(true);
            Field completed=FocusActivity.class.getDeclaredField("showCompletedFeedback");completed.setAccessible(true);
            Method sync=FocusActivity.class.getDeclaredMethod("syncUiWithState");sync.setAccessible(true);
            runOnMainSync(()->{try{
                active.setBoolean(focus,false);completed.setBoolean(focus,true);sync.invoke(focus);
                ((EditText)focus.findViewById(R.id.edit_focus_minutes)).setText("25");
                ((TextView)focus.findViewById(R.id.text_focus_completed_count)).setText(focus.getString(R.string.pid_session_count,1));
                ((TextView)focus.findViewById(R.id.text_focus_history_list)).setText(focus.getString(R.string.focus_history_item_format,25L,"09:41"));
            }catch(Exception error){throw new RuntimeException(error);}});capture("focus-success");
            SettingsActivity setting=launch(SettingsActivity.class);runOnMainSync(()->ReminderPreviewUi.show(setting));capture("reminder-preview");
            String[] voiceLines=setting.getResources().getStringArray(R.array.roast_voice_lines);
            nodeClick("com.cs364.roastapp:id/pid_preview_next");
            if(!voiceLines[1].equals(nodeText("com.cs364.roastapp:id/pid_preview_notification_body")))throw new AssertionError("Next line did not advance the preview text");
            nodeClick("com.cs364.roastapp:id/pid_preview_play");capture("reminder-playing");
            if(!setting.getString(R.string.pid_preview_stop).equals(nodeText("com.cs364.roastapp:id/pid_preview_play")))throw new AssertionError("Play did not start the voice line");
            Thread.sleep(5000);
            if(!setting.getString(R.string.pid_preview_play).equals(nodeText("com.cs364.roastapp:id/pid_preview_play")))throw new AssertionError("Playback did not finish and reset the button");
            back();
            if("true".equals(args.getString("notifications","false"))){
                RoastNotifier notifier=new RoastNotifier(getTargetContext());notifier.showAlert("com.android.chrome",0);pause();
                android.app.NotificationManager notificationManager=getTargetContext().getSystemService(android.app.NotificationManager.class);
                int posted=notificationManager.getActiveNotifications().length;
                if(posted==0)throw new AssertionError("Notification not posted; permission="+androidx.core.content.ContextCompat.checkSelfPermission(getTargetContext(),android.Manifest.permission.POST_NOTIFICATIONS)+" enabled="+notificationManager.areNotificationsEnabled());
                getUiAutomation().executeShellCommand("cmd statusbar expand-notifications").close();pause();capture("android-notification");
                getUiAutomation().executeShellCommand("cmd statusbar collapse").close();notifier.cancel("com.android.chrome");
            }
            result.putString("stream","Remaining UI checks passed: "+prefix);
        }catch(Throwable error){result.putString("stream",android.util.Log.getStackTraceString(error));result.putString("error",error.toString());}
        finally{
            runOnMainSync(()->{for(Activity activity:opened)if(!activity.isFinishing())activity.finish();settings.setLanguage(language);settings.setNightMode(night);});
        }
        finish(result.containsKey("error")?1:Activity.RESULT_OK,result);
    }
    private void pause(){try{Thread.sleep(650);}catch(InterruptedException error){Thread.currentThread().interrupt();}}
    private <T extends Activity>T launch(Class<T> type){Activity activity=startActivitySync(new Intent(getTargetContext(),type).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));opened.add(activity);pause();return type.cast(activity);}
    private void click(Activity activity,int id){runOnMainSync(()->activity.findViewById(id).performClick());pause();}
    private void bottom(Activity activity){runOnMainSync(()->((ScrollView)activity.findViewById(R.id.pid_scroll)).fullScroll(View.FOCUS_DOWN));pause();}
    private void back(){sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);pause();}
    private String nodeText(String id){AccessibilityNodeInfo root=getUiAutomation().getRootInActiveWindow();List<AccessibilityNodeInfo> nodes=root.findAccessibilityNodeInfosByViewId(id);if(nodes.isEmpty())throw new AssertionError("View not found: "+id);CharSequence text=nodes.get(0).getText();return text==null?"":text.toString();}
    private void nodeClick(String id){AccessibilityNodeInfo root=getUiAutomation().getRootInActiveWindow();List<AccessibilityNodeInfo> nodes=root.findAccessibilityNodeInfosByViewId(id);if(nodes.isEmpty()||!nodes.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK))throw new AssertionError("Action not found: "+id);pause();}
    private void capture(String name)throws Exception{pause();Bitmap bitmap=getUiAutomation().takeScreenshot();if(bitmap==null)throw new IllegalStateException("Screenshot unavailable");File folder=getTargetContext().getExternalFilesDir("ui-completion");try(FileOutputStream out=new FileOutputStream(new File(folder,prefix+"-"+name+".png"))){bitmap.compress(Bitmap.CompressFormat.PNG,100,out);}bitmap.recycle();}
}
