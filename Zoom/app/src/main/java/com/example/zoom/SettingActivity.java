package com.example.zoom;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import us.zoom.sdk.ZoomInstantSDK;

public class SettingActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);


        TextView leftView = findViewById(R.id.tvBack);
        leftView.setText(getString(R.string.actionbar_done));
        leftView.setTextColor(getResources().getColor(R.color.done_text));
        leftView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        ((TextView) findViewById(R.id.title)).setText(R.string.setting_title);
        ((TextView) findViewById(R.id.text_version)).setText(getString(R.string.launch_setting_version, ZoomInstantSDK.getInstance().getSDKVersion()));
    }

    public void onClickClearLog(View view) {
        File file = new File("/sdcard/Android/data/" + getPackageName() + "/logs");
        if (file.exists()) {
            for (File item : file.listFiles()) {
                try {
                    item.delete();
                } catch (Exception e) {
                }
            }
            boolean result = file.delete();
            if (result) {
                Toast.makeText(this, "Clear success", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Clear Fail", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "No log files found", Toast.LENGTH_SHORT).show();
        }
    }

    public void onClickSendLog(View view) {
        Intent email = new Intent(Intent.ACTION_SEND_MULTIPLE);
        File file = new File("/sdcard/Android/data/" + getPackageName() + "/logs");
        email.setType("application/octet-stream");
        String emailTitle = getString(R.string.app_name) + "_logs";
        email.putExtra(Intent.EXTRA_SUBJECT, emailTitle);
        email.putExtra(Intent.EXTRA_TEXT, "attach only");
        ArrayList<Parcelable> uris = new ArrayList<>();
        File[] files = file.listFiles();
        if (files == null){
            Toast.makeText(this, "No log files found", Toast.LENGTH_SHORT).show();
            return;
        }
        Arrays.sort(files, new Comparator<File>() {
            public int compare(File f1, File f2) {
                long diff = f1.lastModified() - f2.lastModified();
                if (diff < 0)
                    return 1;
                else if (diff == 0)
                    return 0;
                else
                    return -1;
            }

            public boolean equals(Object obj) {
                return true;
            }
        });

        boolean containUtil = true;
        if (files.length > 10) {
            containUtil = false;
        } else {
            long size = 0;
            for (File item : files) {
                size += item.getFreeSpace();
                if (size > 10 * 1024 * 1024) {
                    containUtil = false;
                    break;
                }
            }
        }
        for (File item : files) {
            if (item.getName().endsWith(".log")) {
                if (!containUtil && item.getName().startsWith("util")) {
                    continue;
                }
                Uri contentUri = FileProvider.getUriForFile(this, "us.zoom.InstantSDKPlaygroud.fileProvider", item);
                uris.add(contentUri);
            }
        }
        email.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        startActivity(Intent.createChooser(email, "Send Email"));
    }

}
