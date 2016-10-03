package com.vzug.memory;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.zxing.client.android.Intents;
import com.google.zxing.integration.android.IntentIntegrator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == IntentIntegrator.REQUEST_CODE
                && resultCode == RESULT_OK) {

            Bundle extras = data.getExtras();
            String path = extras.getString(
                    Intents.Scan.RESULT_BARCODE_IMAGE_PATH);

            Bitmap bmp = BitmapFactory.decodeFile(path);

            FileOutputStream outputStream = null;
            try {
                outputStream = getApplicationContext().openFileOutput("test.png", Context.MODE_PRIVATE);

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
