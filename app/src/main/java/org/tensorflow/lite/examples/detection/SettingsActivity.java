package org.tensorflow.lite.examples.detection;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {

    private int tempTimerCounter;
    private float tempMinConfidence = 0f;
    private boolean isDeletedHistory = false;
    private boolean isTutorial = false;
    private ImageButton imgBtnAddDelay;
    private ImageButton imgBtnRemoveDelay;
    private ImageButton imgBtnAddAccuracy;
    private ImageButton imgBtnRemoveAccuracy;
    private ImageButton imgBtnOnOffSign;
    private ImageButton imgBtnRemoveHistory;
    private ImageButton imgBtnTutorial;
    private ImageButton imgBtnAllObject;
    private Button btnSettings;
    private TextView txtVDelay;
    private TextView txtVIsBoundingBoxes;
    private TextView txtVAccuracy;
    private TextView txtVHistory;
    private boolean tempIsBoundingBoxes;
    private ArrayList<String> tempTempMappedRecognitionsHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Bundle bundle =  getIntent().getExtras();
        int timerCounter = bundle.getInt("timerCounter");
        float minConfidence = bundle.getFloat("minConfidence");
        boolean isBoundingBoxes = bundle.getBoolean("isBoundingBoxes");
        ArrayList<String> tempMappedRecognitionsHistory = bundle.getStringArrayList("tempMappedRecognitionsHistory");

        tempTimerCounter = timerCounter;
        tempMinConfidence = minConfidence;
        tempIsBoundingBoxes = isBoundingBoxes;
        tempTempMappedRecognitionsHistory = tempMappedRecognitionsHistory;

        txtVDelay = findViewById(R.id.txtV_delay);
        txtVIsBoundingBoxes = findViewById(R.id.txtV_is_bounding_boxes);
        txtVAccuracy = findViewById(R.id.txtV_accuracy);
        imgBtnAddDelay = findViewById(R.id.imgBtn_add_delay);
        imgBtnRemoveDelay = findViewById(R.id.imgBtn_remove_delay);
        imgBtnAddAccuracy = findViewById(R.id.imgBtn_add_accuracy);
        imgBtnRemoveAccuracy = findViewById(R.id.imgBtn_remove_accuracy);
        imgBtnOnOffSign = findViewById(R.id.imgBtn_on_off_sign);
        imgBtnRemoveHistory = findViewById(R.id.imgBtn_remove_history);
        imgBtnTutorial = findViewById(R.id.imgBtn_tutorial);
        imgBtnAllObject = findViewById(R.id.imgBtn_all_object);
        txtVHistory = findViewById(R.id.txtV_history);

        txtVDelay.setText(tempTimerCounter + "s");
        txtVHistory.setText(tempTempMappedRecognitionsHistory.toString());
        if(isBoundingBoxes){
            txtVIsBoundingBoxes.setText("ON");
        }else{
            txtVIsBoundingBoxes.setText("OFF");
        }
        txtVAccuracy.setText(Math.round(minConfidence*100) + "%");
        imgBtnAddDelay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(tempTimerCounter < 20){
                    tempTimerCounter++;
                    txtVDelay.setText(tempTimerCounter + "s");
                }else{
                    Toast.makeText(getApplicationContext(), "Waktu delay tidak boleh lebih dari 20", Toast.LENGTH_LONG).show();
                }
            }
        });
        imgBtnRemoveDelay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(tempTimerCounter > 3){
                    tempTimerCounter--;
                    txtVDelay.setText(tempTimerCounter + "s");
                }else{
                    Toast.makeText(getApplicationContext(), "Waktu delay tidak boleh kurang dari 3", Toast.LENGTH_LONG).show();
                }
            }
        });
        imgBtnAddAccuracy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(tempMinConfidence < 0.98){
                    tempMinConfidence+=0.01;
                    txtVAccuracy.setText(Math.round(tempMinConfidence*100) + "%");
                }else{
                    Toast.makeText(getApplicationContext(), "Tingkat akurasi tidak boleh lebih dari 99%", Toast.LENGTH_LONG).show();
                }
            }
        });
        imgBtnRemoveAccuracy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(tempMinConfidence > 0.51){
                    tempMinConfidence-=0.01;
                    txtVAccuracy.setText(Math.round(tempMinConfidence*100) + "%");
                }else{
                    Toast.makeText(getApplicationContext(), "Tingkat akurasi tidak boleh kurang dari 50%", Toast.LENGTH_LONG).show();
                }
            }
        });
        imgBtnOnOffSign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tempIsBoundingBoxes = !tempIsBoundingBoxes;
                if(tempIsBoundingBoxes){
                    txtVIsBoundingBoxes.setText("ON");
                }else{
                    txtVIsBoundingBoxes.setText("OFF");
                }
            }
        });
        imgBtnRemoveHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtVHistory.setText("");
                isDeletedHistory = true;
            }
        });
        imgBtnTutorial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences mSettings = getApplicationContext().getSharedPreferences("Settings", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = mSettings.edit();
                isTutorial = true;
                editor.putBoolean("isTutorial", isTutorial);
                editor.apply();
                Intent intent = new Intent(SettingsActivity.this, DetectorActivity.class);
                startActivity(intent);
                finish();
            }
        });
        imgBtnAllObject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsActivity.this, AllObjectListActivity.class);
                intent.putExtra("timerCounter", timerCounter);
                intent.putExtra("minConfidence", minConfidence);
                intent.putExtra("isBoundingBoxes", isBoundingBoxes);
                intent.putStringArrayListExtra("tempMappedRecognitionsHistory", tempMappedRecognitionsHistory);
                startActivity(intent);
                finish();
            }
        });

        btnSettings = findViewById(R.id.btn_settings);
        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences mSettings = getApplicationContext().getSharedPreferences("Settings", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = mSettings.edit();
                editor.putInt("timerCounter", tempTimerCounter);
                editor.putBoolean("isBoundingBoxes", tempIsBoundingBoxes);
                editor.putFloat("minConfidence", tempMinConfidence);
                editor.putBoolean("isDeletedHistory", isDeletedHistory);
                editor.apply();
                Intent intent = new Intent(SettingsActivity.this, DetectorActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}