package com.vzug.schrittzaehler;


import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.zxing.client.android.Intents;
import com.google.zxing.integration.android.IntentIntegrator;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
            String message = data.getStringExtra("SCAN_RESULT");
        }
    }


}
