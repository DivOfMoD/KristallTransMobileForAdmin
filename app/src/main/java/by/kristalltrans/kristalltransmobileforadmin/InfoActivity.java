package by.kristalltrans.kristalltransmobileforadmin;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

public class InfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        TextView textView = (TextView) findViewById(R.id.infoTextView);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
