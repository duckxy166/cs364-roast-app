package com.cs364.roastapp.history;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cs364.roastapp.R;
import com.cs364.roastapp.core.AppServices;
import com.cs364.roastapp.core.ConfirmDialog;
import com.cs364.roastapp.core.UsageSource;
import com.cs364.roastapp.settings.SettingsManager;
import com.cs364.roastapp.tracking.TrackedAppEntity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class HistoryActivity extends AppCompatActivity {

    private RoastEventDao roastEventDao;
    private HistoryAdapter adapter;

    private WeeklyChartView chartView;
    private TextView textTotalUsage;
    private Button btnClearAll;
    private LinearLayout layoutEmpty;
    private RecyclerView recyclerHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history_activity);

        roastEventDao = AppServices.getDatabase(this).roastEventDao();

        chartView = findViewById(R.id.chart_history_weekly);
        textTotalUsage = findViewById(R.id.text_history_weekly_total);
        btnClearAll = findViewById(R.id.btn_history_clear_all);
        layoutEmpty = findViewById(R.id.layout_history_empty);
        recyclerHistory = findViewById(R.id.recycler_history_events);

        adapter = new HistoryAdapter(this::confirmDeleteSingle);
        recyclerHistory.setLayoutManager(new LinearLayoutManager(this));
        recyclerHistory.setAdapter(adapter);

        btnClearAll.setOnClickListener(v -> confirmClearAll());
        findViewById(R.id.btn_history_back).setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshAll();
    }

    private void refreshAll() {
        refreshWeeklyChart();
        refreshEvents();
    }

    private void refreshEvents() {
        List<RoastEventEntity> events = roastEventDao.getAllEvents();
        adapter.submitList(events);
        boolean isEmpty = events.isEmpty();
        layoutEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerHistory.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        btnClearAll.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private void refreshWeeklyChart() {
        List<TrackedAppEntity> tracked = AppServices.getDatabase(this).trackedAppDao().getAll();
        Set<String> trackedPackages = new HashSet<>();
        for (TrackedAppEntity app : tracked) {
            if (app.packageName != null) {
                trackedPackages.add(app.packageName);
            }
        }

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long todayStart = cal.getTimeInMillis();

        cal.add(Calendar.DAY_OF_YEAR, -6);
        long sevenDaysStart = cal.getTimeInMillis();

        Map<Long, Long> millisByDay = new HashMap<>();
        UsageStatsManager usm = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        if (usm != null && !trackedPackages.isEmpty()) {
            List<UsageStats> stats = usm.queryUsageStats(
                    UsageStatsManager.INTERVAL_DAILY,
                    sevenDaysStart,
                    System.currentTimeMillis()
            );
            if (stats != null) {
                for (UsageStats stat : stats) {
                    if (trackedPackages.contains(stat.getPackageName())) {
                        long bucketDay = startOfDay(stat.getFirstTimeStamp());
                        long current = millisByDay.containsKey(bucketDay) ? millisByDay.get(bucketDay) : 0L;
                        millisByDay.put(bucketDay, current + stat.getTotalTimeInForeground());
                    }
                }
            }
        }

        UsageSource usageSource = AppServices.getUsageSource(this);
        long todayTotal = 0L;
        for (String pkg : trackedPackages) {
            todayTotal += usageSource.getUsedMillisToday(pkg);
        }
        if (todayTotal > 0L) {
            millisByDay.put(todayStart, todayTotal);
        }

        String lang = new SettingsManager(this).getLanguage();
        SimpleDateFormat dayFormat = new SimpleDateFormat("E", Locale.forLanguageTag(lang));
        List<WeeklyChartView.DayBar> bars = new ArrayList<>();
        int totalMinutesAllDays = 0;

        Calendar cursor = Calendar.getInstance();
        cursor.setTimeInMillis(sevenDaysStart);
        for (int i = 0; i < 7; i++) {
            long dayKey = cursor.getTimeInMillis();
            long dayMillis = millisByDay.containsKey(dayKey) ? millisByDay.get(dayKey) : 0L;
            int minutes = (int) (dayMillis / 60000L);
            totalMinutesAllDays += minutes;
            bars.add(new WeeklyChartView.DayBar(dayFormat.format(cursor.getTime()), minutes));
            cursor.add(Calendar.DAY_OF_YEAR, 1);
        }

        chartView.setData(bars, getString(R.string.history_chart_minute_unit));
        textTotalUsage.setText(getString(R.string.history_weekly_total, totalMinutesAllDays));
    }

    private static long startOfDay(long timestamp) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(timestamp);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    private void confirmDeleteSingle(RoastEventEntity item) {
        ConfirmDialog.show(
                this,
                R.string.history_delete_one_title,
                R.string.history_delete_one_message,
                () -> {
                    roastEventDao.delete(item);
                    refreshEvents();
                }
        );
    }

    private void confirmClearAll() {
        ConfirmDialog.show(
                this,
                R.string.history_clear_all_title,
                R.string.history_clear_all_message,
                () -> {
                    roastEventDao.deleteAll();
                    refreshEvents();
                }
        );
    }
}
