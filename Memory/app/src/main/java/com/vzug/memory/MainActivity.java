package com.vzug.memory;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.google.zxing.client.android.Intents;
import com.google.zxing.integration.android.IntentIntegrator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private List<Image> images;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        images = new ArrayList<>();
        //createButtons();
        setButtonClickEvent();
    }

    public void takeQrCodePicture() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setCaptureActivity(MyCaptureActivity.class);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
        integrator.setOrientationLocked(false);
        integrator.addExtra(Intents.Scan.BARCODE_IMAGE_ENABLED, true);
        integrator.initiateScan();
    }

    private void createButtons() {
        ImageButton imageButton = new ImageButton(null);
        imageButton.setImageResource(R.mipmap.capture);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        params.gravity = Gravity.LEFT;
        LinearLayout layout = (LinearLayout) findViewById(R.id.linearLayout1);
        imageButton.setLayoutParams(params);
        layout.addView(imageButton);

        params.gravity = Gravity.RIGHT;
        imageButton.setLayoutParams(params);
        layout.addView(imageButton);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == IntentIntegrator.REQUEST_CODE
                && resultCode == RESULT_OK) {

            Bundle extras = data.getExtras();

            String path = extras.getString(
                    Intents.Scan.RESULT_BARCODE_IMAGE_PATH);
            String message = data.getStringExtra("SCAN_RESULT");
            Image img = new Image();
            img.setMessage(message);
            img.setPath(path);
            images.add(img);

            Bitmap bmp = BitmapFactory.decodeFile(path);



            FileOutputStream outputStream = null;
            SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
            Date currentDateTime = new Date();
            String filename = "Memory" + sdfDate.format(currentDateTime);
            try {
                outputStream = getApplicationContext().openFileOutput(filename, Context.MODE_PRIVATE);

                bmp.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            } catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                try {
                    if (outputStream != null)
                    {
                        outputStream.close();
                    }
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }

            String code = extras.getString(
                    Intents.Scan.RESULT);
        }
    }

    private void setButtonClickEvent()
    {
        ((Button)findViewById(R.id.scanmemory)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takeQrCodePicture();
            }
        });
    }
}
