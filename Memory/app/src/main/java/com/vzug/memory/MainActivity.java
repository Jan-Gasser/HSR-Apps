package com.vzug.memory;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.zxing.client.android.Intents;
import com.google.zxing.integration.android.IntentIntegrator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private List<Image> images;
    private CardView cardView;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        images = new ArrayList<>();
        createButtons();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mymenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        log();
        return true;
    }

    /*
        Calls the QR-Code Scanner
     */
    public void takeQrCodePicture() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setCaptureActivity(MyCaptureActivity.class);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
        integrator.setOrientationLocked(false);
        integrator.addExtra(Intents.Scan.BARCODE_IMAGE_ENABLED, true);
        integrator.initiateScan();
    }

    /*
        Create two buttons in a linear layout
     */
    private void createButtons() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,1
        );
        LinearLayout innerLayout = new LinearLayout(this);
        innerLayout.setLayoutParams(params);

        CardView left = createCardView(1, params);
        CardView right = createCardView(2, params);

        innerLayout.addView(left, params);
        innerLayout.addView(right, params);

        LinearLayout linearLayout = (LinearLayout)findViewById(R.id.lLayout);
        linearLayout.addView(innerLayout, params);
    }

    /**
      *  Create a cardview to display both text and button
      *  @return The cardview
     */
    private CardView createCardView(int number, LinearLayout.LayoutParams params) {
        CardView cardView = new CardView(this);

        ImageButton ig = new ImageButton(this);
        ig.setImageResource(R.mipmap.capture);
        setClickEvent(ig);
        ig.setLayoutParams(params);
        TextView textView = new TextView(this);
        textView.setId(images.size() + number);

        cardView.addView(ig, params);
        cardView.addView(textView,params);
        return cardView;
    }

    /**
        Log the message in the Logbook-App
     */
    private void log() {
        Intent intent = new Intent("ch.appquest.intent.LOG");

        if (getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).isEmpty()) {
            Toast.makeText(this, "Logbook App not Installed", Toast.LENGTH_LONG).show();
            return;
        }

        intent.putExtra("ch.appquest.logmessage", buildMessage().toString());
        startActivity(intent);
    }

    /**
     * Build the JSON-Object from the cached images.
     * @return The JsonObject
     */
    private JSONObject buildMessage() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("task", "Memory");
            JSONArray imagesArray = new JSONArray();
            for (int i = 0; i < images.size(); i += 2) {
                JSONArray imgJson = new JSONArray();
                imgJson.put(images.get(i).getMessage());
                imgJson.put(images.get(i+1).getMessage());
                imagesArray.put(imgJson);
            }
            jsonObject.put("solution", imagesArray);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    private void setClickEvent(ImageButton btn) {
        btn.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   //Save this view, to set the image and the text after
                   cardView = (CardView)v.getParent();
                   takeQrCodePicture();
               }
           }
        );
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

            /* Set Button background & create new button */

            //For future little layout changes, we dont have to change the code
            if (cardView.getChildAt(0) instanceof ImageButton) {
                ((ImageButton)cardView.getChildAt(0)).setImageBitmap(bmp);
                ((TextView)cardView.getChildAt(1)).setText(img.getMessage());
            }
            if(images.size() % 2 == 0) {
                createButtons();
            }

            //Save function for future updates with After-Kill-Load
            saveImage(bmp);
        }
    }

    private void saveImage(Bitmap bmp) {
        FileOutputStream outputStream = null;
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        Date currentDateTime = new Date();
        String filename = "Memory" + sdfDate.format(currentDateTime);
        try {
            outputStream = getApplicationContext().openFileOutput(filename, Context.MODE_PRIVATE);

            bmp.compress(Bitmap.CompressFormat.PNG, 100, outputStream);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.vzug.memory/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.vzug.memory/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }
}
