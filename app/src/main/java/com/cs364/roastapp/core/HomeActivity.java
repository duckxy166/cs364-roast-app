package com.cs364.roastapp.core;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.cs364.roastapp.R;
import com.cs364.roastapp.focus.FocusActivity;
import com.cs364.roastapp.onboarding.OnboardingActivity;
import com.cs364.roastapp.settings.SettingsActivity;
import com.cs364.roastapp.tracking.TrackedAppsActivity;

public class HomeActivity extends AppCompatActivity {
    private int monitorRequest;
    public void renderAppUsage(java.util.List<com.cs364.roastapp.ui.AppLimitsUi.App> apps) {
        new com.cs364.roastapp.ui.AppLimitsUi(this, null).render(apps, true);
    }
    public void renderPermissionWarning(boolean missing) {
        renderMonitoringState(missing ? com.cs364.roastapp.ui.MonitoringStatusUi.State.MISSING_PERMISSIONS
                : com.cs364.roastapp.ui.MonitoringStatusUi.State.READY,
                () -> startActivity(new Intent(this, OnboardingActivity.class)));
    }


    public void renderMonitoringState(com.cs364.roastapp.ui.MonitoringStatusUi.State state, Runnable recovery) {
        com.cs364.roastapp.ui.MonitoringStatusUi.render(this, state, recovery);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // First launch: onboarding comes first; it brings the user back here once permissions are granted.
        if (!com.cs364.roastapp.onboarding.OnboardingPrefs.isDone(this)) {
            startActivity(new Intent(this, OnboardingActivity.class));
            finish();
            return;
        }
        setContentView(R.layout.core_activity_home);

        com.cs364.roastapp.ui.PutItDownNavigation.bind(this, R.id.pid_nav_home);
        findViewById(R.id.pid_monitor_settings).setOnClickListener(v -> startActivity(new Intent(this, OnboardingActivity.class)));
        findViewById(R.id.btn_nav_onboarding).setOnClickListener(v ->
                startActivity(new Intent(this, OnboardingActivity.class)));
        findViewById(R.id.pid_home_manage).setOnClickListener(v ->
                startActivity(new Intent(this, TrackedAppsActivity.class)));
        findViewById(R.id.pid_home_add).setOnClickListener(v ->
                startActivity(new Intent(this, TrackedAppsActivity.class)));
        findViewById(R.id.pid_home_focus).setOnClickListener(v ->
                startActivity(new Intent(this, FocusActivity.class)));


        findViewById(R.id.btn_nav_history).setOnClickListener(v ->
                startActivity(new Intent(this, com.cs364.roastapp.history.HistoryActivity.class)));

        findViewById(R.id.btn_roast_test).setOnClickListener(v ->
                com.cs364.roastapp.roast.RoastTestHelper.nextStep(this));
    }

    @Override protected void onResume() {
        super.onResume();
        if(findViewById(R.id.pid_monitoring_panel)!=null) reloadHome(false);
    }
    @Override protected void onStop(){super.onStop();monitorRequest++;}
    private void reloadHome(boolean retry) {
        int token=++monitorRequest;
        if(retry)renderMonitoringState(com.cs364.roastapp.ui.MonitoringStatusUi.State.STARTING,null);
        com.cs364.roastapp.tracking.TrackedAppsRepository.IO.execute(()->{
            try {
                boolean requested=com.cs364.roastapp.tracking.TrackingServiceController.sync(this);
                java.util.List<com.cs364.roastapp.ui.AppLimitsUi.App> apps=new com.cs364.roastapp.tracking.TrackedAppsRepository(this).loadTracked();
                runOnUiThread(()->{
                    if(!currentRequest(token))return;
                    renderAppUsage(apps);
                    if(!com.cs364.roastapp.tracking.PermissionChecker.hasAll(this))renderPermissionWarning(true);
                    else if(!requested)showMonitoringFailure();
                    else confirmMonitoring(token,apps.isEmpty(),0);
                });
            }catch(RuntimeException error){android.util.Log.w("HomeMonitoring","Could not refresh monitoring",error);runOnUiThread(()->{if(currentRequest(token))showMonitoringFailure();});}
        });
    }
    private boolean currentRequest(int token){return token==monitorRequest&&!isFinishing()&&!isDestroyed();}
    private void confirmMonitoring(int token,boolean noApps,int attempt){
        if(!currentRequest(token))return;
        if(!com.cs364.roastapp.tracking.PermissionChecker.hasAll(this)){renderPermissionWarning(true);return;}
        if(noApps||com.cs364.roastapp.tracking.UsageTrackingService.isRunning()){renderPermissionWarning(false);return;}
        if(attempt>=10){showMonitoringFailure();return;}
        renderMonitoringState(com.cs364.roastapp.ui.MonitoringStatusUi.State.STARTING,null);
        findViewById(R.id.pid_monitoring_panel).postDelayed(()->confirmMonitoring(token,noApps,attempt+1),200);
    }
    private void showMonitoringFailure(){renderMonitoringState(com.cs364.roastapp.ui.MonitoringStatusUi.State.FAILED,()->reloadHome(true));}

    @Override
    protected void onStart() {
        super.onStart();
        RoastTrigger trigger = AppServices.getRoastTrigger(this);
        if (trigger instanceof com.cs364.roastapp.roast.RoastManager rm) {
            rm.resetPackage(null);
        }
    }
}
