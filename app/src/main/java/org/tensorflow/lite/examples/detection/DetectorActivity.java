/*
 * Copyright 2019 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.tensorflow.lite.examples.detection;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.media.ImageReader.OnImageAvailableListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Size;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.tensorflow.lite.examples.detection.customview.OverlayView;
import org.tensorflow.lite.examples.detection.customview.OverlayView.DrawCallback;
import org.tensorflow.lite.examples.detection.env.BorderedText;
import org.tensorflow.lite.examples.detection.env.ImageUtils;
import org.tensorflow.lite.examples.detection.env.Logger;
import org.tensorflow.lite.examples.detection.tflite.Detector;
import org.tensorflow.lite.examples.detection.tflite.TFLiteObjectDetectionAPIModel;
import org.tensorflow.lite.examples.detection.tracking.MultiBoxTracker;

/**
 * An activity that uses a TensorFlowMultiBoxDetector and ObjectTracker to detect and then track
 * objects.
 */
public class DetectorActivity extends CameraActivity implements OnImageAvailableListener {
  private static final Logger LOGGER = new Logger();

  // Configuration values for the prepackaged SSD model.
  private static final int TF_OD_API_INPUT_SIZE = 300;
  private static final boolean TF_OD_API_IS_QUANTIZED = true;
  private static final String TF_OD_API_MODEL_FILE = "detect.tflite";
  private static final String TF_OD_API_LABELS_FILE = "labelmap.txt";
  private static final DetectorMode MODE = DetectorMode.TF_OD_API;
  // Minimum detection confidence to track a detection.
  private static final float MINIMUM_CONFIDENCE_TF_OD_API = 0.6f;
  private static final boolean MAINTAIN_ASPECT = false;
  private static final Size DESIRED_PREVIEW_SIZE = new Size(640, 480);
  private static final boolean SAVE_PREVIEW_BITMAP = false;
  private static final float TEXT_SIZE_DIP = 10;
  private static final int REQUEST_CODE_SPEECH_INPUT = 1000;
  OverlayView trackingOverlay;
  private Integer sensorOrientation;

  private Detector detector;

  private long lastProcessingTimeMs;
  private Bitmap rgbFrameBitmap = null;
  private Bitmap croppedBitmap = null;
  private Bitmap cropCopyBitmap = null;

  private boolean computingDetection = false;

  private long timestamp = 0;

  private Matrix frameToCropTransform;
  private Matrix cropToFrameTransform;

  private MultiBoxTracker tracker;

  private BorderedText borderedText;

  /* Custom variable */
  TextToSpeech tts;
    ArrayList<String> tempMappedRecognitionsHistory = new ArrayList<>();;
  List<String> tempMappedRecognitions =
          new ArrayList<String>();
  private Timer timer = new Timer();
  private boolean isReadySpeak = false;
  private boolean isBoundingBoxes = false;
  private boolean isTutorial = false;
  private boolean isReadyClickedTutorial1 = false;
  private boolean isReadyClickedTutorial2 = false;
  private boolean isReadyClickedTutorial3 = false;
  private boolean isReadyClickedTutorial4 = false;
  private TextView txtVListen;
  private TextView txtVSpeak;
  private int timerCounter = 3;
  private ImageButton imgBtnSpeak;
  private ImageButton imgBtnSettings;
  private float minConfidence = 0.6f;

  private String translate(String text){
    switch (text){
      case "person" : return "orang";
      case "bicycle" : return "sepeda";
      case "car" : return "mobil";
      case "motorcycle" : return "sepeda motor";
      case "airplane": return "pesawat terbang";
      case "bus": return "bis";
      case "train": return "kereta";
      case "truck": return "truk";
      case "boat": return "perahu";
      case "traffic light": return "lampu lalu lintas";
      case "fire hydrant": return "alat pemadam kebakaran";
      case "stop sign": return "tanda berhenti";
      case "parking meter": return "meteran parkir";
      case "bench": return "bangku";
      case "bird": return "burung";
      case "cat": return "kucing";
      case "dog": return "anjing";
      case "horse": return "kuda";
      case "sheep": return "domba";
      case "cow": return "lembu";
      case "elephant": return "gajah";
      case "bear": return "beruang";
      case "zebra": return "zebra";
      case "giraffe": return "jerapah";
      case "backpack": return "ransel";
      case "umbrella": return "payung";
      case "handbag": return "tas tangan";
      case "tie": return "dasi";
      case "suitcase": return "koper";
      case "frisbee": return "piring lempar";
      case "skis": return "ski";
      case "snowboard": return "papan seluncur";
      case "sports ball": return "bola sport";
      case "kite": return "layang-layang";
      case "baseball bat": return "tongkat pemukul baseball";
      case "baseball glove": return "sarung baseball";
      case "skateboard": return "papan skateboard";
      case "surfboard": return "papan selancar";
      case "tennis racket": return "raket tenis";
      case "bottle": return "botol";
      case "wine glass": return "gelas anggur";
      case "cup": return "cangkir";
      case "fork": return "garpu";
      case "knife": return "pisau";
      case "spoon": return "sendok";
      case "bowl": return "mangkok";
      case "banana": return "pisang";
      case "apple": return "apel";
      case "sandwich": return "sandwich";
      case "orange": return "jeruk";
      case "broccoli": return "brokoli";
      case "carrot": return "wortel";
      case "hot dog": return "Hot Dog";
      case "pizza": return "Pizza";
      case "donut": return "donat";
      case "cake": return "kue";
      case "chair": return "kursi";
      case "couch": return "sofa";
      case "potted plant": return "tanaman di dalam pot";
      case "bed": return "tempat tidur";
      case "dining table": return "meja makan";
      case "toilet": return "toilet";
      case "tv": return "televisi";
      case "laptop": return "laptop";
      case "mouse": return "mouse";
      case "remote": return "remote";
      case "keyboard": return "papan ketik";
      case "cell phone": return "telpon selular";
      case "microwave": return "microwave";
      case "oven": return "oven";
      case "toaster": return "pemanggang roti";
      case "sink": return "wastafel";
      case "refrigerator": return "kulkas";
      case "book": return "buku";
      case "clock": return "jam";
      case "vase": return "vas";
      case "scissors": return "gunting";
      case "teddy bear": return "boneka beruang";
      case "hair drier": return "pengering rambut";
      case "toothbrush": return "sikat gigi";
      default: return "tidak dikenali";
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    SharedPreferences mSettings = getApplicationContext().getSharedPreferences("Settings", Context.MODE_PRIVATE);
    if(mSettings.getInt("timerCounter", 0) != 0){
      timerCounter = mSettings.getInt("timerCounter", 0);
    }
    if(mSettings.getBoolean("isBoundingBoxes", false)){
      isBoundingBoxes = mSettings.getBoolean("isBoundingBoxes", false);
    }
    if(mSettings.getFloat("minConfidence", 0) != 0){
      minConfidence = mSettings.getFloat("minConfidence", 0);
    }
    if(mSettings.getBoolean("isTutorial", false)){
      boolean isTutorial = mSettings.getBoolean("isTutorial", false);
      if(isTutorial){
        this.isTutorial = true;

        setSharedPreferencesBoolean("isTutorial", false);
      }
    }

    if(mSettings.getStringSet("tempMappedRecognitionsHistory", null) != null){
      Set<String> set = mSettings.getStringSet("tempMappedRecognitionsHistory", null);
      tempMappedRecognitionsHistory.addAll(set);
    }
    if(mSettings.getBoolean("isDeletedHistory", false)){
      boolean isDeletedHistory = mSettings.getBoolean("isDeletedHistory", false);
      if(isDeletedHistory){
        tempMappedRecognitionsHistory.clear();
        setSharedPreferencesBoolean("isDeletedHistory", false);
      }
    }

    // tts intialization
    initializationTTS();

    imgBtnSpeak = findViewById(R.id.imgbtn_speak);
    txtVSpeak = findViewById(R.id.txtV_speak);
    imgBtnSpeak.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
          speak();
      }
    });

    txtVListen = findViewById(R.id.txtV_listen);
    imgBtnSettings = findViewById(R.id.imgBtn_settings);
    imgBtnSettings.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if(isTutorial){
            timer.cancel();
            timer = new Timer();
            tts.shutdown();
            textToSpeechFlush("Tutorial akan diakhiri");
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    textToSpeechFlush("Anda dapat membuka petunjuk suara ini lagi dengan menekan tombol pengaturan di sebelah pojok kanan atas sebanyak dua kali. Selamat mencoba!");
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            isTutorial = false;
                            setSharedPreferencesBoolean("isTutorial", false);
                            isReadyClickedTutorial1 = false;
                            isReadyClickedTutorial2 = false;
                            isReadyClickedTutorial3 = false;
                            isReadyClickedTutorial4 = false;
                            tempMappedRecognitions.clear();
                            timer.scheduleAtFixedRate(new TimerTask() {
                                @Override
                                public void run() {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if(tempMappedRecognitions.size() != 0 && !isReadySpeak){
                                                for(String tempMappedRecognition : tempMappedRecognitions){
                                                    tts.speak(translate(tempMappedRecognition), TextToSpeech.QUEUE_ADD, null);
                                                    Toast.makeText(getApplicationContext(), tempMappedRecognition, Toast.LENGTH_SHORT).show();
                                                }
                                                tempMappedRecognitions.clear();
                                            }
                                        }
                                    });
                                }
                            }, timerCounter*1000, timerCounter*1000);
                        }
                    }, 20*1000);
                }
            }, 2*1000);
        }else{
            tts.shutdown();
            timer.cancel();

            SharedPreferences.Editor editor = mSettings.edit();
            Set<String> set = new HashSet<String>();
            set.addAll(tempMappedRecognitionsHistory);
            editor.putStringSet("tempMappedRecognitionsHistory", set);
            editor.apply();

            Intent intent = new Intent(DetectorActivity.this, SettingsActivity.class);
            intent.putExtra("timerCounter", timerCounter);
            intent.putExtra("minConfidence", minConfidence);
            intent.putExtra("isBoundingBoxes", isBoundingBoxes);
            intent.putStringArrayListExtra("tempMappedRecognitionsHistory", tempMappedRecognitionsHistory);
            startActivity(intent);
            finish();
        }
      }
    });

    // set timer
    // save value in arraylist (in mehtod runnable below), if the past array contain the string don't add it
    // so, the 'tts' will not speak it after x seconds (default 10 seconds)
    if(!isTutorial){
      timer.scheduleAtFixedRate(new TimerTask() {
        @Override
        public void run() {
          runOnUiThread(new Runnable() {
            @Override
            public void run() {
              if(tempMappedRecognitions.size() != 0 && !isReadySpeak){
                for(String tempMappedRecognition : tempMappedRecognitions){
                  tts.speak(translate(tempMappedRecognition), TextToSpeech.QUEUE_ADD, null);
                  Toast.makeText(getApplicationContext(), tempMappedRecognition, Toast.LENGTH_SHORT).show();
                }
                tempMappedRecognitions.clear();
              }
            }
          });
          }
      }, 3*1000, timerCounter*1000);
    }

    // set tutorial tour
    // tts guide tour
    if(isTutorial){
      textToSpeechFlush("Aplikasi akan memulai tutorial. Ikuti petunjuk untuk melatih kemampuan dalam menggunakan aplikasi ini.");
      timer.schedule(new TimerTask() {
        @Override
        public void run() {
          textToSpeechFlush("Saya akan memandu Anda dengan menggunakan petunjuk suara dan penempatan posisi jari untuk mengaktifkan fungsi tertentu");
          timer.schedule(new TimerTask() {
            @Override
            public void run() {
              textToSpeechFlush("Pertama - tama, aplikasi ini terbatas hanya memiliki kemampuan dalam mendeteksi 80 objek, seperti kursi, laptop, televisi, orang, sepeda motor, dan lainnya. Anda mendapatkan informasi mengenai apa saja yang dapat dideteksi pada manual aplikasi");
              timer.schedule(new TimerTask() {
                @Override
                public void run() {
                  textToSpeechFlush("Kedua, aplikasi ini memiliki beberapa parameter yang dapat diatur yakni, waktu delay, penanda, dan akurasi");
                  timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                      textToSpeechFlush("Semua parameter dapat diatur menggunakan suara, dengan menekan tombol yang ada di bagian tengah bawah aplikasi ini. Namun, fitur ini membutuhkan koneksi internet, pastikan Anda memiliki koneksi internet yang baik.");
                      timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                          textToSpeechFlush("Mari, kita lakukan latihan pertama. Tekan tombol perekam perintah suara yang ada di bagian tengah bawah");
                          timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                              if(isReadyClickedTutorial1 == false){
                                textToSpeechFlush("Tombol belum ditekan, silahkan cari tombolnya sekali lagi");
                                timer.schedule(new TimerTask() {
                                  @Override
                                  public void run() {
                                    if(isReadyClickedTutorial1 == false){
                                      textToSpeechFlush("Tombol belum ditekan, tutorial akan diakhiri, coba mengulangi tutorial dengan menekan pojok kanan atas dua kali. Terima kasih");
                                      timer.schedule(new TimerTask() {
                                        @Override
                                        public void run() {
                                          isTutorial = false;
                                          timer.cancel();
                                          timer = new Timer();
                                          tempMappedRecognitions.clear();
                                          timer.scheduleAtFixedRate(new TimerTask() {
                                            @Override
                                            public void run() {
                                              runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                  if(tempMappedRecognitions.size() != 0 && !isReadySpeak){
                                                    for(String tempMappedRecognition : tempMappedRecognitions){
                                                      tts.speak(translate(tempMappedRecognition), TextToSpeech.QUEUE_ADD, null);
                                                      Toast.makeText(getApplicationContext(), tempMappedRecognition, Toast.LENGTH_SHORT).show();
                                                    }
                                                    tempMappedRecognitions.clear();
                                                  }
                                                }
                                              });
                                                }
                                          }, timerCounter*1000, timerCounter*1000);
                                        }
                                      }, 12*1000);
                                    }
                                  }
                                }, 15*1000); // ok
                              }
                            }
                          }, 15*1000); // ok
                        }
                      }, 15*1000); // ok
                    }
                  }, 10*1000); // ok
                }
              }, 20*1000); // ok
            }
          }, 10*1000); // ok
        }
      }, 10*1000); // ok
    }
  }

  public void initializationTTS(){
      try{
          tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
              @Override
              public void onInit(int status) {
                  if (status != TextToSpeech.ERROR && !isReadySpeak) {
                      tts.setLanguage(new Locale("id", "ID"));
                  }
              }
          });
      }catch(Exception e){
          LOGGER.d("ERROR = " + e.getMessage());
      }
  }

  @Override
  public void onPreviewSizeChosen(final Size size, final int rotation) {
    final float textSizePx =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, getResources().getDisplayMetrics());
    borderedText = new BorderedText(textSizePx);
    borderedText.setTypeface(Typeface.MONOSPACE);

    tracker = new MultiBoxTracker(this);

    int cropSize = TF_OD_API_INPUT_SIZE;

    try {
      detector =
          TFLiteObjectDetectionAPIModel.create(
              this,
              TF_OD_API_MODEL_FILE,
              TF_OD_API_LABELS_FILE,
              TF_OD_API_INPUT_SIZE,
              TF_OD_API_IS_QUANTIZED);
      cropSize = TF_OD_API_INPUT_SIZE;
    } catch (final IOException e) {
      e.printStackTrace();
      LOGGER.e(e, "Exception initializing Detector!");
      Toast toast =
          Toast.makeText(
              getApplicationContext(), "Detector could not be initialized", Toast.LENGTH_SHORT);
      toast.show();
      finish();
    }

    previewWidth = size.getWidth();
    previewHeight = size.getHeight();

    sensorOrientation = rotation - getScreenOrientation();
    LOGGER.i("Camera orientation relative to screen canvas: %d", sensorOrientation);

    LOGGER.i("Initializing at size %dx%d", previewWidth, previewHeight);
    rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Config.ARGB_8888);
    croppedBitmap = Bitmap.createBitmap(cropSize, cropSize, Config.ARGB_8888);

    frameToCropTransform =
        ImageUtils.getTransformationMatrix(
            previewWidth, previewHeight,
            cropSize, cropSize,
            sensorOrientation, MAINTAIN_ASPECT);

    cropToFrameTransform = new Matrix();
    frameToCropTransform.invert(cropToFrameTransform);

    trackingOverlay = (OverlayView) findViewById(R.id.tracking_overlay);
    trackingOverlay.addCallback(
        new DrawCallback() {
          @Override
          public void drawCallback(final Canvas canvas) {
            tracker.draw(canvas);
            if (isDebug()) {
              tracker.drawDebug(canvas);
            }
          }
        });

    tracker.setFrameConfiguration(previewWidth, previewHeight, sensorOrientation);
  }

  private void speak(){
    if(isTutorial && (isReadyClickedTutorial1 == false)){
      isReadyClickedTutorial1 = true;
      textToSpeechFlush("Selamat Anda telah berhasil menekan tombol perintah suara. Disini ada beberapa perintah yang perlu Anda hafal, diantaranya");
      timer.schedule(new TimerTask() {
        @Override
        public void run() {
          textToSpeechFlush("Pertama katakan timer 10, maka aplikasi akan mengatur waktu delay dengan jeda 10 detik, katakan timer 6, maka aplikasi akan mengautr waktu delay dengan jeda 6 detik");
          timer.schedule(new TimerTask() {
            @Override
            public void run() {
              textToSpeechFlush("Sekarang, coba tekan dan katakan suatu perintah");
              timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    checkTutorialProgress(isReadyClickedTutorial2);
                }
              }, 10*1000);
            }
          }, 12*1000);
        }
      }, 8*1000);
    }else{
      Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
      intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
      intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, new Locale("id", "ID"));
      intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Ada yang bisa saya bantu?");
      isReadySpeak = true;

      try {
        startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT);
      }catch(Exception e){
        isReadySpeak = false;
        Toast.makeText(this, "Error = " + e.getMessage(), Toast.LENGTH_SHORT).show();
      }
    }
  }

  private void listen(String[] arrayText){
    ArrayList<String> tempArrayText = new ArrayList<>();
    for(int i = 0; i < arrayText.length; i++){
      tempArrayText.add(arrayText[i].toLowerCase());
    }
    if(tempArrayText.contains("timer")){
      if(isTutorial){
        int index = tempArrayText.indexOf("timer");
        isReadyClickedTutorial2 = true;
        try{
          if(Integer.parseInt(arrayText[index + 1]) >= 6 && Integer.parseInt(arrayText[index + 1]) <= 20){
            timerCounter = Integer.parseInt(arrayText[index + 1]);
            // re-set timer
            timer.cancel();
            timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
              @Override
              public void run() {
                runOnUiThread(new Runnable() {
                  @Override
                  public void run() {
                    for(String tempMappedRecognition : tempMappedRecognitions){
                      tts.speak(translate(tempMappedRecognition), TextToSpeech.QUEUE_ADD, null);
                      Toast.makeText(getApplicationContext(), tempMappedRecognition, Toast.LENGTH_SHORT).show();
                    }
                    tempMappedRecognitions.clear();
                  }
                });
              }
            }, timerCounter*1000, timerCounter*1000);
            textToSpeechFlush("Timer telah diatur ke " + arrayText[index + 1]);
            if(isReadyClickedTutorial2){
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        textToSpeechFlush("Selanjutnya katakan akurasi 50, maka aplikasi akan mengatur akurasi atau ketepatan perkiraan sebesar 50 persen, katakan akurasi 90, maka aplikasi akan mengatur akurasi sebesar 90, jangan menggunakan akurasi yang terlalu besar atau terlalu kecil, secara normal akurasi berada di kisaran 60");
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                textToSpeechFlush("Sekarang, coba tekan dan katakan suatu perintah");
                                timer.schedule(new TimerTask() {
                                    @Override
                                    public void run() {
                                        checkTutorialProgress(isReadyClickedTutorial3);
                                    }
                                }, 10*1000);
                            }
                        }, 28*1000);
                    }
                }, 5*1000);
            }
          }else{
            textToSpeechFlush("Timer tidak bisa diatur ke " + arrayText[index + 1] + "gunakan format angka setelah mengucapkan timer");
          }
        }catch(Exception ex){
          textToSpeechFlush("Perintah tidak lengkap, coba katakan dengan benar dan ulangi sekali sampai berhasil");
        }
      }else{
        int index = tempArrayText.indexOf("timer");
        try{
          if(Integer.parseInt(arrayText[index + 1]) >= 6 && Integer.parseInt(arrayText[index + 1]) <= 20){
            timerCounter = Integer.parseInt(arrayText[index + 1]);
            // re-set timer
            timer.cancel();
            timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
              @Override
              public void run() {
                runOnUiThread(new Runnable() {
                  @Override
                  public void run() {
                    for(String tempMappedRecognition : tempMappedRecognitions){
                      tts.speak(translate(tempMappedRecognition), TextToSpeech.QUEUE_ADD, null);
                      Toast.makeText(getApplicationContext(), tempMappedRecognition, Toast.LENGTH_SHORT).show();
                    }
                    tempMappedRecognitions.clear();
                  }
                });
              }
            }, timerCounter*1000, timerCounter*1000);
            textToSpeechFlush("Timer telah diatur ke " + arrayText[index + 1]);
          }else{
            textToSpeechFlush("Timer tidak bisa diatur ke " + arrayText[index + 1]);
          }
        }catch(Exception ex){
          textToSpeechFlush("Perintah tidak lengkap");
        }
      }
    }else if(tempArrayText.contains("tutup") && tempArrayText.contains("aplikasi")){
      if(isTutorial){
          //
      }else{
          tts.shutdown();
          timer.cancel();
          finish();
          System.exit(0);
      }
    }else if(tempArrayText.contains("tampilkan") && tempArrayText.contains("penanda")){
      if(isTutorial){
          //
      }else{
          isBoundingBoxes = true;
          textToSpeechFlush("Menampilkan Penanda Objek");
      }
    }else if(tempArrayText.contains("hilangkan") && tempArrayText.contains("penanda")){
      if(isTutorial){
          //
      }else{
          isBoundingBoxes = false;
          textToSpeechFlush("Menghilangkan Penanda Objek");
      }
    }else if(tempArrayText.contains("akurasi") && tempArrayText.contains("normal")){
        if(isTutorial){
            isReadyClickedTutorial4 = true;
            minConfidence = MINIMUM_CONFIDENCE_TF_OD_API;
            textToSpeechFlush("Mengembalikan Akurasi ke 60 persen");
            if(isReadyClickedTutorial4){
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        textToSpeechFlush("Selamat Anda telah bisa menggunakan perekam suara. Lakukan latihan perintah yang lain. Setelah ini tutorial akan diakhiri. Anda juga dapat membuka petunjuk suara ini lagi dengan menekan tombol pengaturan di sebelah pojok kanan atas sebanyak dua kali. Selamat mencoba!");
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                isTutorial = false;
                                setSharedPreferencesBoolean("isTutorial", false);
                                isReadyClickedTutorial1 = false;
                                isReadyClickedTutorial2 = false;
                                isReadyClickedTutorial3 = false;
                                isReadyClickedTutorial4 = false;
                                timer.cancel();
                                timer = new Timer();
                                tempMappedRecognitions.clear();
                                timer.scheduleAtFixedRate(new TimerTask() {
                                    @Override
                                    public void run() {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                if(tempMappedRecognitions.size() != 0 && !isReadySpeak){
                                                    for(String tempMappedRecognition : tempMappedRecognitions){
                                                        tts.speak(translate(tempMappedRecognition), TextToSpeech.QUEUE_ADD, null);
                                                        Toast.makeText(getApplicationContext(), tempMappedRecognition, Toast.LENGTH_SHORT).show();
                                                    }
                                                    tempMappedRecognitions.clear();
                                                }
                                            }
                                        });
                                    }
                                }, timerCounter*1000, timerCounter*1000);
                            }
                        }, 24*1000);
                    }
                }, 2*1000);
            }
        }else{
            minConfidence = MINIMUM_CONFIDENCE_TF_OD_API;
            textToSpeechFlush("Mengembalikan Akurasi ke 60 persen");
        }
    }else if(tempArrayText.contains("akurasi")){
      int index = tempArrayText.indexOf("akurasi");
      if(isTutorial){
          isReadyClickedTutorial3 = true;
          try{
              if(Integer.parseInt(arrayText[index + 1]) >= 50 && Integer.parseInt(arrayText[index + 1]) <= 99){
                  minConfidence = Integer.parseInt(arrayText[index + 1]);
                  minConfidence = (Float) minConfidence/100;
                  textToSpeechFlush("Tingkat Akurasi diatur ke " + Integer.parseInt(arrayText[index + 1]));
                  if(isReadyClickedTutorial3){
                      timer.schedule(new TimerTask() {
                          @Override
                          public void run() {
                              textToSpeechFlush("Selanjutnya katakan akurasi normal, maka aplikasi akan mengatur akurasi atau ketepatan perkiraan sebesar 60 persen");
                              timer.schedule(new TimerTask() {
                                  @Override
                                  public void run() {
                                      textToSpeechFlush("Sekarang, coba tekan dan katakan suatu perintah");
                                      timer.schedule(new TimerTask() {
                                          @Override
                                          public void run() {
                                         checkTutorialProgress(isReadyClickedTutorial4);
                                          }
                                      }, 10*1000);
                                  }
                              }, 12*1000);
                          }
                      }, 12*1000);
                  }
              }else{
                  textToSpeechFlush("Tingkat Akurasi tidak bisa diatur ke " + Integer.parseInt(arrayText[index + 1]));
              }
          }catch(Exception ex){
              textToSpeechFlush("Perintah tidak lengkap");
          }
      }else{
          try{
              if(Integer.parseInt(arrayText[index + 1]) >= 50 && Integer.parseInt(arrayText[index + 1]) <= 99){
                  minConfidence = Integer.parseInt(arrayText[index + 1]);
                  minConfidence = (Float) minConfidence/100;
                  textToSpeechFlush("Tingkat Akurasi diatur ke " + Integer.parseInt(arrayText[index + 1]));
              }else{
                  textToSpeechFlush("Tingkat Akurasi tidak bisa diatur ke " + Integer.parseInt(arrayText[index + 1]));
              }
          }catch(Exception ex){
              textToSpeechFlush("Perintah tidak lengkap");
          }
      }
    }else if(tempArrayText.contains("putar") && tempArrayText.contains("tutorial")){
        if(isReadyClickedTutorial2 || isReadyClickedTutorial1 || isTutorial){
            //
        }else{
            isTutorial = true;
            setSharedPreferencesBoolean("isTutorial", true);
            tts.shutdown();
            timer.cancel();
            recreate();
        }
    }else if(tempArrayText.contains("hentikan") && tempArrayText.contains("tutorial")){
        if(isTutorial){
            textToSpeechFlush("Tutorial akan diakhiri");
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    textToSpeechFlush("Anda dapat membuka petunjuk suara ini lagi dengan menekan tombol pengaturan di sebelah pojok kanan atas sebanyak dua kali. Selamat mencoba!");
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            isTutorial = false;
                            setSharedPreferencesBoolean("isTutorial", false);
                            isReadyClickedTutorial1 = false;
                            isReadyClickedTutorial2 = false;
                            isReadyClickedTutorial3 = false;
                            isReadyClickedTutorial4 = false;
                            tts.shutdown();
                            initializationTTS();
                            timer.cancel();
                            timer = new Timer();
                            tempMappedRecognitions.clear();
                            timer.scheduleAtFixedRate(new TimerTask() {
                                @Override
                                public void run() {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if(tempMappedRecognitions.size() != 0 && !isReadySpeak){
                                                for(String tempMappedRecognition : tempMappedRecognitions){
                                                    tts.speak(translate(tempMappedRecognition), TextToSpeech.QUEUE_ADD, null);
                                                    Toast.makeText(getApplicationContext(), tempMappedRecognition, Toast.LENGTH_SHORT).show();
                                                }
                                                tempMappedRecognitions.clear();
                                            }
                                        }
                                    });
                                }
                            }, timerCounter*1000, timerCounter*1000);
                        }
                    }, 24*1000);
                }
            }, 2*1000);
        }else{
            textToSpeechFlush("Tidak ada tutorial berlangsung");
        }
    }else{
      textToSpeechFlush("Perintah tidak dikenali");
    }
  }

  private void textToSpeechFlush(String text){
    tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
      @Override
      public void onInit(int status) {
        if (status != TextToSpeech.ERROR && !isReadySpeak) {
          txtVListen.setText(text);
          tts.setLanguage(new Locale("id", "ID"));
          // get the next value of array to get value timer
          tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }
      }
    });
  }

  private void checkTutorialProgress(boolean isReady){
      if(isReady == false) {
          textToSpeechFlush("Tombol belum ditekan atau perintah belum benar, coba sekali lagi");
          timer.schedule(new TimerTask() {
              @Override
              public void run() {
                  if (isReady == false) {
                      textToSpeechFlush("Tutorial gagal, tutorial akan diakhiri, coba mengulangi tutorial dengan menekan pojok kanan atas dua kali. Terima kasih");
                      timer.schedule(new TimerTask() {
                          @Override
                          public void run() {
                              isTutorial = false;
                              timer.cancel();
                              timer = new Timer();
                              tempMappedRecognitions.clear();
                              timer.scheduleAtFixedRate(new TimerTask() {
                                  @Override
                                  public void run() {
                                      runOnUiThread(new Runnable() {
                                          @Override
                                          public void run() {
                                              if (tempMappedRecognitions.size() != 0 && !isReadySpeak) {
                                                  for (String tempMappedRecognition : tempMappedRecognitions) {
                                                      tts.speak(translate(tempMappedRecognition), TextToSpeech.QUEUE_ADD, null);
                                                      Toast.makeText(getApplicationContext(), tempMappedRecognition, Toast.LENGTH_SHORT).show();
                                                  }
                                                  tempMappedRecognitions.clear();
                                              }
                                          }
                                      });
                                  }
                              }, timerCounter * 1000, timerCounter * 1000);
                          }
                      }, 12 * 1000);
                  }
              }
          }, 10*1000);
      }
  }

  private void setSharedPreferencesBoolean(String name, boolean status){
      SharedPreferences mSettings = getApplicationContext().getSharedPreferences("Settings", Context.MODE_PRIVATE);
      SharedPreferences.Editor editor = mSettings.edit();
      editor.putBoolean(name, status);
      editor.apply();
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    switch (requestCode){
      case REQUEST_CODE_SPEECH_INPUT: {
        if(resultCode == RESULT_OK && data != null){
          ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
          String text = result.get(0);
          txtVSpeak.setText(text);
          listen(text.split(" "));
        }
        isReadySpeak = false;
        break;
      }
    }
  }

  @Override
  protected void processImage() {
    ++timestamp;
    final long currTimestamp = timestamp;
    trackingOverlay.postInvalidate();

    // No mutex needed as this method is not reentrant.
    if (computingDetection) {
      readyForNextImage();
      return;
    }
    computingDetection = true;
    LOGGER.i("Preparing image " + currTimestamp + " for detection in bg thread.");

    rgbFrameBitmap.setPixels(getRgbBytes(), 0, previewWidth, 0, 0, previewWidth, previewHeight);

    readyForNextImage();

    final Canvas canvas = new Canvas(croppedBitmap);
    canvas.drawBitmap(rgbFrameBitmap, frameToCropTransform, null);
    // For examining the actual TF input.
    if (SAVE_PREVIEW_BITMAP) {
      ImageUtils.saveBitmap(croppedBitmap);
    }

    runInBackground(
        new Runnable() {
          @Override
          public void run() {
            LOGGER.i("Running detection on image " + currTimestamp);
            final long startTime = SystemClock.uptimeMillis();
            final List<Detector.Recognition> results = detector.recognizeImage(croppedBitmap);
            lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime;

            cropCopyBitmap = Bitmap.createBitmap(croppedBitmap);
            final Canvas canvas = new Canvas(cropCopyBitmap);
            final Paint paint = new Paint();
            paint.setColor(Color.RED);
            paint.setStyle(Style.STROKE);
            paint.setStrokeWidth(2.0f);

            float minimumConfidence = MINIMUM_CONFIDENCE_TF_OD_API;
            switch (MODE) {
              case TF_OD_API:
                minimumConfidence = minConfidence;
                break;
            }

            final List<Detector.Recognition> mappedRecognitions =
                new ArrayList<Detector.Recognition>();

            for (final Detector.Recognition result : results) {
              final RectF location = result.getLocation();
              if (location != null && result.getConfidence() >= minimumConfidence) {

                if(!isTutorial){
                    if (!tempMappedRecognitions.contains(result.getTitle())) {
                        if (!tempMappedRecognitionsHistory.contains(translate(result.getTitle()))) {
                            tempMappedRecognitionsHistory.add(translate(result.getTitle()));
                        }
                        tempMappedRecognitions.add(result.getTitle());
                    }
                }else{
                    tempMappedRecognitions.clear();
                }

                if(isBoundingBoxes){
                  /* COMMENT/UNCOMMENT for bounding boxes */
                  canvas.drawRect(location, paint);
                  cropToFrameTransform.mapRect(location);

                  result.setLocation(location);
                  mappedRecognitions.add(result);
                  /* COMMENT/UNCOMMENT for bounding boxes */
                }
              }
            }

            tracker.trackResults(mappedRecognitions, currTimestamp);
            trackingOverlay.postInvalidate();

            computingDetection = false;

            runOnUiThread(
                new Runnable() {
                  @Override
                  public void run() {
                    showFrameInfo(previewWidth + "x" + previewHeight);
                    showCropInfo(cropCopyBitmap.getWidth() + "x" + cropCopyBitmap.getHeight());
                    showInference(lastProcessingTimeMs + "ms");

                    if(tempMappedRecognitions.size() != 0){
                      txtVListen.setText(tempMappedRecognitions.toString());
                    }else{
                      txtVListen.setText("Menunggu...");
                    }
                  }
                });
          }
        });
  }

  @Override
  protected int getLayoutId() {
    return R.layout.tfe_od_camera_connection_fragment_tracking;
  }

  @Override
  protected Size getDesiredPreviewFrameSize() {
    return DESIRED_PREVIEW_SIZE;
  }

  // Which detection model to use: by default uses Tensorflow Object Detection API frozen
  // checkpoints.
  private enum DetectorMode {
    TF_OD_API;
  }

  @Override
  protected void setUseNNAPI(final boolean isChecked) {
    runInBackground(
        () -> {
          try {
            detector.setUseNNAPI(isChecked);
          } catch (UnsupportedOperationException e) {
            LOGGER.e(e, "Failed to set \"Use NNAPI\".");
            runOnUiThread(
                () -> {
                  Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                });
          }
        });
  }

  @Override
  protected void setNumThreads(final int numThreads) {
    runInBackground(() -> detector.setNumThreads(numThreads));
  }
}
