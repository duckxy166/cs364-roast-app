package com.cs364.roastapp.history;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import com.cs364.roastapp.R;
import java.util.ArrayList;
import java.util.List;

/** Visual chart paired with the screen-reader accessible daily-value list. */
public class WeeklyChartView extends View {
    public static final class DayBar {
        public final String label;
        public final int minutes;
        public final boolean known;
        public DayBar(String label,int minutes){this(label,minutes,true);}
        public DayBar(String label,int minutes,boolean known){this.label=label;this.minutes=Math.max(0,minutes);this.known=known;}
    }
    private final List<DayBar> bars=new ArrayList<>();
    private final Paint fill=new Paint(Paint.ANTI_ALIAS_FLAG),label=new Paint(Paint.ANTI_ALIAS_FLAG),line=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect=new RectF();
    public WeeklyChartView(Context c){super(c);init(c);}
    public WeeklyChartView(Context c,@Nullable AttributeSet a){super(c,a);init(c);}
    public WeeklyChartView(Context c,@Nullable AttributeSet a,int style){super(c,a,style);init(c);}
    private void init(Context c){
        fill.setColor(ContextCompat.getColor(c,R.color.pid_progress_color));
        label.setColor(ContextCompat.getColor(c,R.color.pid_on_surface_variant));
        label.setTextAlign(Paint.Align.CENTER);
        label.setTypeface(androidx.core.content.res.ResourcesCompat.getFont(c,R.font.pid_noto_sans_thai));
        line.setColor(ContextCompat.getColor(c,R.color.pid_outline_variant));
    }
    public void setData(List<DayBar> days,String unit){bars.clear();if(days!=null)bars.addAll(days);invalidate();}
    @Override protected void onDraw(Canvas canvas){
        super.onDraw(canvas);if(bars.isEmpty())return;
        float dp=getResources().getDisplayMetrics().density;
        float sp=getResources().getDisplayMetrics().scaledDensity;
        label.setTextSize(getResources().getDimension(R.dimen.pid_type_chart));line.setStrokeWidth(dp);
        float width=getWidth()-getPaddingLeft()-getPaddingRight();
        float bottom=getHeight()-getPaddingBottom()-Math.max(28*dp,22*sp);
        float top=getPaddingTop()+12*dp;
        if(width<=0||bottom<=top)return;
        int max=1;for(DayBar d:bars)if(d.known)max=Math.max(max,d.minutes);
        float slot=width/bars.size(),barWidth=Math.min(24*dp,slot*.52f);
        canvas.drawLine(getPaddingLeft(),bottom,getWidth()-getPaddingRight(),bottom,line);
        for(int i=0;i<bars.size();i++){
            DayBar d=bars.get(i);float x=getPaddingLeft()+slot*(i+.5f);
            if(d.known&&d.minutes>0){float h=(bottom-top)*d.minutes/max;rect.set(x-barWidth/2,bottom-h,x+barWidth/2,bottom);canvas.drawRoundRect(rect,4*dp,4*dp,fill);}
            else if(d.known){canvas.drawCircle(x,bottom-2*dp,2*dp,fill);}
            else {canvas.drawText("—",x,bottom-8*dp,label);}
            if(label.measureText(d.label)<=slot-2*dp)canvas.drawText(d.label,x,bottom+18*sp,label);
        }
    }
}
