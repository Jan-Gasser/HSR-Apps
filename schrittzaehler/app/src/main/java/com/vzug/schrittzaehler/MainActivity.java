package com.vzug.schrittzaehler;


import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.client.android.Intents;
import com.google.zxing.integration.android.IntentIntegrator;

import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private TextToSpeech text2speech;

    private static final String ACTION_LEFT  = "links";
    private static final String ACTION_RIGHT = "rechts";
    private static final int REQUEST_CODE_COUNTER    = 1;
    public static final String MESSAGE = "com.vzug.schrittzaehler.STEPS_TO_WALK";

    private List<Action> actions;
    private int counter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        text2speech = new TextToSpeech(this,this);
    }

    public void takeQrCodePicture() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setCaptureActivity(MyCaptureActivity.class);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
        integrator.setOrientationLocked(false);
        integrator.addExtra(Intents.Scan.BARCODE_IMAGE_ENABLED, true);
        integrator.initiateScan();
    }

    private void handleActions(List<Action> actions) {
        this.actions = actions;
        doAction(actions.get(0));
    }


    private void doAction(Action action) {
        String text = String.format(getResources().getString(R.string.speak_message), action.getSchritte(), action.getDirection());
        outputText(text);

        Intent counterActivity = new Intent(this,CountActivity.class);
        counterActivity.putExtra(MESSAGE,action.getSchritte());
        startActivityForResult(counterActivity,REQUEST_CODE_COUNTER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == IntentIntegrator.REQUEST_CODE
                && resultCode == RESULT_OK) {
            String message = data.getStringExtra("SCAN_RESULT");
        }
        else if(requestCode == REQUEST_CODE_COUNTER && resultCode == RESULT_OK) {
            counter++;
            if (actions.size() > counter) {
                doAction(actions.get(counter));
            }
            else {
                outputText(getResources().getString(R.string.endstation_message));
            }
        }
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = text2speech.setLanguage(Locale.GERMAN);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(this,"Your language is not supported",Toast.LENGTH_LONG).show();
            }
            text2speech.setSpeechRate(0.8f);
        }
    }

    private void outputText(String text) {
        TextView textView = (TextView) findViewById(R.id.displayText);
        textView.setText(text);
        text2speech.speak(text,TextToSpeech.QUEUE_FLUSH,null, "id");
    }
}
