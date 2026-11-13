package com.cs364.roastapp.ui;

import android.app.Activity;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.widget.RadioButton;
import androidx.core.content.ContextCompat;
import com.cs364.roastapp.R;
import com.google.android.material.navigation.NavigationBarView;

/** Presentation only: persona selection and custom-line storage remain owned by the roast feature. */
public final class PersonaPresentation {
    private PersonaPresentation() {}
    public static void bind(Activity activity) {
        PutItDownNavigation.bind(activity, R.id.btn_nav_settings);
        NavigationBarView navigation = activity.findViewById(R.id.pid_bottom_nav);
        navigation.setOnItemReselectedListener(item -> PutItDownNavigation.open(activity, item.getItemId()));
        for (int id : new int[]{R.id.radio_persona_strict,R.id.radio_persona_sarcastic,R.id.radio_persona_mom}) {
            RadioButton radio = activity.findViewById(id);
            String text = radio.getText().toString(); int split = text.indexOf('\n');
            if (split < 0) continue;
            SpannableString label = new SpannableString(text);
            label.setSpan(new StyleSpan(android.graphics.Typeface.BOLD),0,split,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            label.setSpan(new RelativeSizeSpan(0.86f),split+1,text.length(),Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            label.setSpan(new ForegroundColorSpan(ContextCompat.getColor(activity,R.color.pid_on_surface_variant)),split+1,text.length(),Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            radio.setText(label);
        }
    }
}
