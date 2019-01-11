package sakkhat.com.p250.accessories;

import android.content.DialogInterface;
import android.graphics.Color;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.marcoscg.licenser.Library;
import com.marcoscg.licenser.License;
import com.marcoscg.licenser.LicenserDialog;

import sakkhat.com.p250.R;

public class About extends AppCompatActivity {

    private static final String TAG = "about_p250";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);




        final TextView aboutAkash = (TextView) findViewById(R.id.about_akash);
        aboutAkash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse("https://github.com/i-akash"));
                startActivity(Intent.createChooser(i,"Action chooser"));
            }
        });

        final TextView aboutRafiul = (TextView) findViewById(R.id.about_rafiul);
        aboutRafiul.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse("https://github.com/rafiulgits"));
                startActivity(Intent.createChooser(i, "Action chooser"));
            }
        });

        final TextView aboutSource = (TextView) findViewById(R.id.about_source);
        aboutSource.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse("https://github.com/sakkhat/Project250"));
                startActivity(Intent.createChooser(i, "Action chooser"));
            }
        });

        final TextView aboutLicense = (TextView) findViewById(R.id.about_license);
        aboutLicense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               new LicenserDialog(About.this)
                        .setTitle("Licenses")
                        .setCustomNoticeTitle("Notices for files:")
                        .setBackgroundColor(R.color.jarvis) // Optional
                        .setLibrary(new Library("Android Support Libraries",
                                "https://developer.android.com/topic/libraries/support-library/index.html",
                                License.APACHE))
                        .setLibrary(new Library("Licenser",
                                "https://github.com/marcoscgdev",
                                License.APACHE))
                        .setLibrary(new Library("Cursor Wheel Layout",
                                "https://github.com/BCsl/CursorWheelLayout",
                                License.APACHE))
                        .setLibrary(new Library("Circle Image View",
                                "https://github.com/hdodenhof/CircleImageView",
                                License.APACHE))
                        .setLibrary(new Library("DialogFlow",
                                "https://github.com/dialogflow/dialogflow-android-client",
                                License.APACHE))
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        }).show();
            }
        });

    }

}
