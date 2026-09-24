package com.shreema.gramshikayat;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView textView = new TextView(this);
        textView.setText("ग्राम श्रीमा शिकायत ऐप\n\nस्वागत है");
        textView.setTextSize(22);
        textView.setPadding(40, 80, 40, 40);

        setContentView(textView);
    }
}
