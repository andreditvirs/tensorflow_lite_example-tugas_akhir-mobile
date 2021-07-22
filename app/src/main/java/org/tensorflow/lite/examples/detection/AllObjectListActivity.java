package org.tensorflow.lite.examples.detection;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;

import org.tensorflow.lite.examples.detection.env.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;

public class AllObjectListActivity extends AppCompatActivity {

    private static final Logger LOGGER = new Logger();
    private ImageButton imgBtnTutorial;
    private Button btnBackToSettings;
    private boolean isTutorial = false;
    private final String allObject = "orang, sepeda, mobil, sepeda motor, pesawat terbang, bis, kereta, truk, perahu, lampu lalu lintas, alat pemadam kebakaran, tanda berhenti, meteran parkir, bangku, burung, kucing, anjing, kuda, domba, lembu, gajah, beruang, zebra, jerapah, ransel, payung, tas tangan, dasi, koper, piring lempar, ski, papan seluncur, bola sport, layang - layang, tongkat pemukul baseball, sarung baseball, papan skateboard, papan selancar, raket tenis, botol, gelas anggur, cangkir, garpu, pisau sendok, mangkok, pisang, apel, sandwich, jeruk, brokoli, wortel, hot dog, pizza, donat, kue, kursi, sofa, tanaman di dalam pot, tempat tidur, meja makan, toilet, televisi, laptop, mouse, remote, papan ketik, telpon selular, microwave, oven, pemanggang roti, wastafel, kulkas, buku, jam, vas, gunting, boneka beruang, pengering rambut, sikat gigi.\n" +
            "\n";
    private String[] allObjectArray = {"orang", "sepeda", "mobil", "sepeda motor", "pesawat terbang", "bis", "kereta", "truk", "perahu", "lampu lalu lintas", "alat pemadam kebakaran", "tanda berhenti", "meteran parkir", "bangku", "burung", "kucing", "anjing", "kuda", "domba", "lembu", "gajah", "beruang", "zebra", "jerapah", "ransel", "payung", "tas tangan", "dasi", "koper", "piring lempar", "ski", "papan seluncur", "bola sport", "layang - layang", "tongkat pemukul baseball", "sarung baseball", "papan skateboard", "papan selancar", "raket tenis", "botol", "gelas anggur", "cangkir", "garpu", "pisau sendok", "mangkok", "pisang", "apel", "sandwich", "jeruk", "brokoli", "wortel", "hot dog", "pizza", "donat", "kue", "kursi", "sofa", "tanaman di dalam pot", "tempat tidur", "meja makan", "toilet", "televisi", "laptop", "mouse", "remote", "papan ketik", "telpon selular", "microwave", "oven", "pemanggang roti", "wastafel", "kulkas", "buku", "jam", "vas", "gunting", "boneka beruang", "pengering rambut", "sikat gigi"};
    private ArrayList<String> allObjectList;
    TextToSpeech tts;
    private int tempTimerCounter;
    private float tempMinConfidence = 0f;
    private boolean tempIsBoundingBoxes;
    private ArrayList<String> tempTempMappedRecognitionsHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_object_list);

        ListView listView = findViewById(R.id.listV_all_object_list);
        allObjectList = new ArrayList<>();
        Collections.addAll(allObjectList, allObjectArray);
        ArrayAdapter adapter = new ArrayAdapter<>(this, R.layout.listview_all_object_style, allObjectList);
        listView.setAdapter(adapter);

        Bundle bundle =  getIntent().getExtras();
        int timerCounter = bundle.getInt("timerCounter");
        float minConfidence = bundle.getFloat("minConfidence");
        boolean isBoundingBoxes = bundle.getBoolean("isBoundingBoxes");
        ArrayList<String> tempMappedRecognitionsHistory = bundle.getStringArrayList("tempMappedRecognitionsHistory");

        tempTimerCounter = timerCounter;
        tempMinConfidence = minConfidence;
        tempIsBoundingBoxes = isBoundingBoxes;
        tempTempMappedRecognitionsHistory = tempMappedRecognitionsHistory;

        // tts intialization
        try{
            tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if (status != TextToSpeech.ERROR) {
                        tts.setLanguage(new Locale("id", "ID"));
                    }
                }
            });
        }catch(Exception e){
            LOGGER.d("ERROR = " + e.getMessage());
        }

        imgBtnTutorial = findViewById(R.id.imgBtn_tutorial_all_object_list);
        btnBackToSettings = findViewById(R.id.btn_back_to_settings);

        imgBtnTutorial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isTutorial = true;
                tts.speak(allObject, TextToSpeech.QUEUE_FLUSH, null);
            }
        });
        btnBackToSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tts.shutdown();
                Intent intent = new Intent(AllObjectListActivity.this, SettingsActivity.class);
                intent.putExtra("timerCounter", timerCounter);
                intent.putExtra("minConfidence", minConfidence);
                intent.putExtra("isBoundingBoxes", isBoundingBoxes);
                intent.putStringArrayListExtra("tempMappedRecognitionsHistory", tempMappedRecognitionsHistory);
                startActivity(intent);
                finish();
            }
        });

    }
}