package com.cs364.roastapp.history;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.widget.TextViewCompat;
import com.cs364.roastapp.R;
import com.cs364.roastapp.core.AppDbHelper;
import com.cs364.roastapp.core.AppServices;
import com.cs364.roastapp.core.ConfirmDialog;
import com.cs364.roastapp.onboarding.OnboardingActivity;
import com.cs364.roastapp.tracking.TrackedAppsActivity;
import com.cs364.roastapp.tracking.TrackedAppsRepository;
import com.cs364.roastapp.settings.SettingsManager;
import com.cs364.roastapp.ui.PutItDownNavigation;
import com.cs364.roastapp.ui.UsageTimeText;
import java.text.SimpleDateFormat;
import java.util.*;

public class HistoryActivity extends AppCompatActivity {
    private AppDbHelper dbHelper;
    private HistoryAdapter adapter;
    private WeeklyChartView chartView;
    private TextView textTotalUsage,usageNote,dateRange;
    private Button btnClearAll,dailyToggle,usageAction;
    private LinearLayout layoutEmpty,dailyValues;
    private RecyclerView recyclerHistory;
    private int request;
    private boolean dailyExpanded;
    @Override protected void onCreate(Bundle state){
        super.onCreate(state);setContentView(R.layout.history_activity);
        PutItDownNavigation.bind(this,R.id.pid_nav_home);
        ((com.google.android.material.navigation.NavigationBarView)findViewById(R.id.pid_bottom_nav)).setOnItemReselectedListener(item->PutItDownNavigation.open(this,item.getItemId()));
        dbHelper=AppServices.getDatabase(this);
        chartView=findViewById(R.id.chart_history_weekly);textTotalUsage=findViewById(R.id.text_history_weekly_total);
        usageNote=findViewById(R.id.history_usage_note);dateRange=findViewById(R.id.history_date_range);
        btnClearAll=findViewById(R.id.btn_history_clear_all);layoutEmpty=findViewById(R.id.layout_history_empty);
        recyclerHistory=findViewById(R.id.recycler_history_events);dailyValues=findViewById(R.id.history_daily_values);
        dailyToggle=findViewById(R.id.history_daily_toggle);usageAction=findViewById(R.id.history_usage_action);
        findViewById(R.id.history_empty_apps).setOnClickListener(v->startActivity(new Intent(this,TrackedAppsActivity.class)));
        adapter=new HistoryAdapter(this::confirmDeleteSingle);recyclerHistory.setLayoutManager(new LinearLayoutManager(this));recyclerHistory.setAdapter(adapter);
        dailyExpanded=state!=null&&state.getBoolean("daily_expanded",false);
        dailyToggle.setOnClickListener(v->{dailyExpanded=!dailyExpanded;renderDailyExpansion();});
        btnClearAll.setOnClickListener(v->confirmClearAll());findViewById(R.id.btn_history_back).setOnClickListener(v->finish());
    }
    @Override protected void onResume(){super.onResume();refreshAll();}
    @Override protected void onSaveInstanceState(Bundle state){super.onSaveInstanceState(state);state.putBoolean("daily_expanded",dailyExpanded);}
    private void refreshAll(){
        int token=++request;textTotalUsage.setText(R.string.review_usage_loading);
        usageAction.setEnabled(false);
        TrackedAppsRepository.IO.execute(()->{
            UsageHistorySnapshot snapshot=UsageHistorySnapshot.load(this);
            List<RoastEventEntity> events=dbHelper.getAllRoastEvents();
            runOnUiThread(()->{if(isFinishing()||isDestroyed()||token!=request)return;
                renderUsage(snapshot);adapter.submitList(events);boolean empty=events.isEmpty();
                layoutEmpty.setVisibility(empty?View.VISIBLE:View.GONE);recyclerHistory.setVisibility(empty?View.GONE:View.VISIBLE);btnClearAll.setVisibility(empty?View.GONE:View.VISIBLE);
            });
        });
    }
    public void renderUsage(UsageHistorySnapshot data){
        Locale locale=Locale.forLanguageTag(new SettingsManager(this).getLanguage());
        SimpleDateFormat range=new SimpleDateFormat("d MMM yyyy",locale),label=new SimpleDateFormat("EEE",locale),full=new SimpleDateFormat("EEEE d MMM yyyy",locale);
        dateRange.setText(getString(R.string.review_date_range,range.format(new Date(data.startMillis)),range.format(new Date(data.endMillis))));
        usageAction.setEnabled(true);usageAction.setVisibility(View.GONE);chartView.setVisibility(View.GONE);dailyToggle.setVisibility(View.GONE);dailyValues.removeAllViews();dailyValues.setVisibility(View.GONE);
        if(data.state!=UsageHistorySnapshot.State.AVAILABLE){
            int title,note,action;
            Class<?> target;
            if(data.state==UsageHistorySnapshot.State.NO_APPS){title=R.string.review_no_apps;note=R.string.review_no_apps_note;action=R.string.review_choose_apps;target=TrackedAppsActivity.class;}
            else if(data.state==UsageHistorySnapshot.State.PERMISSION_REQUIRED){title=R.string.review_usage_permission;note=R.string.review_usage_permission_note;action=R.string.review_enable_usage;target=OnboardingActivity.class;}
            else{title=R.string.review_usage_unavailable;note=R.string.review_usage_unavailable_note;action=R.string.review_usage_retry;target=null;}
            textTotalUsage.setText(title);usageNote.setText(note);usageAction.setText(action);usageAction.setVisibility(View.VISIBLE);
            usageAction.setOnClickListener(v->{if(target==null)refreshAll();else startActivity(new Intent(this,target));});return;
        }
        textTotalUsage.setText(data.knownDays==0?getString(R.string.review_no_daily_data):getString(R.string.review_known_total,UsageTimeText.format(this,data.knownMillis)));
        usageNote.setText(data.knownDays==0?getString(R.string.review_no_daily_note):getString(R.string.review_coverage,data.knownDays));
        List<WeeklyChartView.DayBar> bars=new ArrayList<>();
        for(WeeklyUsageData.Day day:data.days){
            boolean known=day.usedMillis!=null;
            bars.add(new WeeklyChartView.DayBar(label.format(new Date(day.startMillis)),known?(int)(day.usedMillis/60000):0,known));
            // Date on the left, value on the right, like a settings row; read out as one sentence.
            String date=full.format(new Date(day.startMillis));
            String value=known?UsageTimeText.format(this,day.usedMillis):getString(R.string.review_day_incomplete);
            android.widget.LinearLayout row=new android.widget.LinearLayout(this);
            row.setOrientation(android.widget.LinearLayout.HORIZONTAL);row.setGravity(android.view.Gravity.CENTER_VERTICAL);
            row.setMinimumHeight(getResources().getDimensionPixelSize(R.dimen.pid_touch_target));
            row.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);
            row.setContentDescription(known?getString(R.string.review_day_value,date,value):getString(R.string.review_day_unknown,date));
            TextView dateText=new TextView(this);TextViewCompat.setTextAppearance(dateText,R.style.TextAppearance_PutItDown_Label);dateText.setText(date);
            dateText.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
            TextView valueText=new TextView(this);TextViewCompat.setTextAppearance(valueText,known?R.style.TextAppearance_PutItDown_Label:R.style.TextAppearance_PutItDown_Supporting);valueText.setText(value);
            valueText.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
            row.addView(dateText,new android.widget.LinearLayout.LayoutParams(0,android.view.ViewGroup.LayoutParams.WRAP_CONTENT,1f));
            row.addView(valueText);
            dailyValues.addView(row);
        }
        chartView.setData(bars,getString(R.string.history_chart_minute_unit));chartView.setVisibility(data.knownDays>0?View.VISIBLE:View.GONE);
        dailyToggle.setVisibility(View.VISIBLE);renderDailyExpansion();
    }
    private void renderDailyExpansion(){dailyValues.setVisibility(dailyExpanded?View.VISIBLE:View.GONE);dailyToggle.setText(dailyExpanded?R.string.review_daily_hide:R.string.review_daily_show);}

    private void confirmDeleteSingle(RoastEventEntity item) {
        ConfirmDialog.show(this, R.string.history_delete_one_title, R.string.history_delete_one_message,
                () -> TrackedAppsRepository.IO.execute(() -> {
                    dbHelper.deleteRoastEvent(item.id);
                    reloadAfterDelete();
                }));
    }

    private void confirmClearAll() {
        ConfirmDialog.show(this, R.string.history_clear_all_title, R.string.history_clear_all_message,
                () -> TrackedAppsRepository.IO.execute(() -> {
                    dbHelper.deleteAllRoastEvents();
                    reloadAfterDelete();
                }));
    }

    /** The list on screen only shows what SQLite holds, so read it again after a delete. */
    private void reloadAfterDelete() {
        runOnUiThread(() -> {
            if (!isFinishing() && !isDestroyed()) {
                refreshAll();
            }
        });
    }
}
