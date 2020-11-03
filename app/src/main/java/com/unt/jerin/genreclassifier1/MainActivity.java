package com.unt.jerin.genreclassifier1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.MediaRecorder;

import android.os.CountDownTimer;
//import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.Gravity;
import android.view.View;

import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

//import android.support.v4.app.ActivityCompat;
import androidx.core.app.ActivityCompat;
import android.content.pm.PackageManager;
//import android.support.v4.content.ContextCompat;
import androidx.core.content.ContextCompat;


import java.io.File;


public class MainActivity extends AppCompatActivity {


    //Button buttonStart, buttonStop, buttonPlayLastRecordAudio, buttonStopPlayingRecording ;
    Button buttonStartWav, buttonStopWav, buttonPlayWav, buttonStopPlayWav, identifyGenre;
    TextView identifiedGenre, secsText;
    String AudioSavePathInDevice = null;
    MediaRecorder mediaRecorder ;
    Random random ;
    String RandomAudioFileName = "ABCDEFGHIJKLMNOP";
    public static final int RequestPermissionCode = 1;
    MediaPlayer mediaPlayer ;
    PredictionsStoreDB predictionsStoreDB;
    CountDownTimer ct = setupTimer();
    String fileDir;


    TableLayout table;


    String lastRecordedFile = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        fileDir = getExternalCacheDir() + File.separator + "AudioRecord";

        buttonStartWav = (Button) findViewById(R.id.button5);
        buttonStopWav = (Button) findViewById(R.id.button6);
        //uploadAudioToGCP = (Button) findViewById(R.id.button7);
        identifyGenre = (Button) findViewById(R.id.button8);

        buttonPlayWav = (Button) findViewById(R.id.button11);
        buttonStopPlayWav = (Button) findViewById(R.id.button12);

        buttonStopWav.setEnabled(false);
        buttonPlayWav.setEnabled(false);
        buttonStopPlayWav.setEnabled(false);
        identifiedGenre = (TextView)findViewById(R.id.identifiedGenre);
        secsText = (TextView)findViewById(R.id.secstext);

        // DB
        predictionsStoreDB = new PredictionsStoreDB(this);

        //Table
        table = (TableLayout) findViewById(R.id.table1);
        populatePredictionHistoryTable();

        random = new Random();




        /**
         * Wav recorder
         */
        //String fileDir = getExternalCacheDir() + File.separator + "AudioRecord";
        RecordWavFile recordWav = new RecordWavFile(fileDir);

        buttonStartWav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(checkPermission()) {

                    recordWav.startWavFileRecord();
                    ct.start();

                    buttonStartWav.setEnabled(false);
                    buttonStopWav.setEnabled(true);
                    buttonPlayWav.setEnabled(false);
                    buttonStopPlayWav.setEnabled(false);

                    Toast.makeText(MainActivity.this, "Recording Wav started",
                            Toast.LENGTH_LONG).show();
                } else {
                    requestPermission();
                }

            }
        });


        buttonStopWav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    //mediaRecorder.stop();

                    lastRecordedFile = recordWav.stopWavFileRecord();

                    ct.cancel();
                    ct.onFinish();
                    ct = setupTimer();

                    buttonStopWav.setEnabled(false);
                    //uploadAudioToGCP.setEnabled(true);
                    buttonStartWav.setEnabled(true);
                    buttonPlayWav.setEnabled(true);
                    buttonStopPlayWav.setEnabled(true);

                    Toast.makeText(MainActivity.this, "Recording Wav Completed",
                            Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


        buttonPlayWav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) throws IllegalArgumentException,
                    SecurityException, IllegalStateException {
                Log.d("buttonPlayWav", "Inside buttonPlayWav onClick");

                buttonStopWav.setEnabled(false);
                buttonStartWav.setEnabled(false);
                buttonStopPlayWav.setEnabled(true);

                mediaPlayer = new MediaPlayer();
                try {
                    Log.d("buttonPlayWav", "lastRecordedFile: "+lastRecordedFile);
                    mediaPlayer.setDataSource(lastRecordedFile);
                    mediaPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                mediaPlayer.start();
                Toast.makeText(MainActivity.this, "Recording Playing",
                        Toast.LENGTH_LONG).show();
            }
        });

        buttonStopPlayWav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonStopWav.setEnabled(false);
                buttonStartWav.setEnabled(true);
                buttonStopPlayWav.setEnabled(false);
                buttonPlayWav.setEnabled(true);

                if(mediaPlayer != null){
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    //MediaRecorderReady();
                }
            }
        });


        /**
         * Upload the audio file to GCP Bucket
         */
        /*uploadAudioToGCP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    //mediaRecorder.stop();
                    InputStream is = getResources().openRawResource(R.raw.genre_classifier_bucket_creds_1);


                    // The ID of your GCP project
                    String projectId = "genre-classifier-293000";

                    // The ID of your GCS bucket
                    String bucketName = "genre-classifier-audio-files-bucket";

                    // The path to your file to upload
                    // String filePath = "C:\\Jerin\\datasets\\genre-guesser1\\test-recorded-dataset\\classical-mozart.wav";
                    String filePath = lastRecordedFile;


                    // The ID of your GCS object
                    //String objectName = "testfile_"+currentTimestamp+".wav";
                    String objectName = filePath.substring(filePath.lastIndexOf("/") + 1);

                    Log.d("", "projectId: "+projectId+"; bucketName: "+bucketName+"; filePath: "+filePath+"; objectName: "+objectName);

                    FileUploadToGCPBucket.uploadObject(projectId, bucketName, objectName, filePath, is);
                    Log.d("Uploaded File URL: ", "https://storage.googleapis.com/genre-classifier-audio-files-bucket/"+objectName);
                    Toast.makeText(MainActivity.this, "File Uploaded",
                            Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });*/


        /**
         * Call GCP Cloud Function to identify genre
         */

        GoogleFunctionRESTClient gcpFunctionClient = new GoogleFunctionRESTClient();
        identifyGenre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {

                    uploadFileToGCPBucket(lastRecordedFile);
                    //mediaRecorder.stop();
                    Log.d("identifyGenre", "Inside identifyGenre onClick");

                    String baseFileName = lastRecordedFile.substring(lastRecordedFile.lastIndexOf("/") + 1);

                    String prediction = gcpFunctionClient.getPredictionFromCloudFunction(baseFileName);//"20201025035339.wav" baseFileName
                    Log.d("identifyGenre", "prediction: "+prediction);

                    identifiedGenre.setText(prediction);

                    // predictionsStoreDB.deleteAllPredictionData();

                    predictionsStoreDB.insertPredictionData(baseFileName, prediction);

                    populatePredictionHistoryTable();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        /**
         * Test read text file
         */


        /*InputStream is = this.getResources().openRawResource(R.raw.testfile);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String readLine = null;

        try {
            // While the BufferedReader readLine is not null
            while ((readLine = br.readLine()) != null) {
                Log.d("TEXT", readLine);
            }

            // Close the InputStream and BufferedReader
            is.close();
            br.close();

        } catch (IOException e) {
            e.printStackTrace();
        }*/

    }


    public void uploadFileToGCPBucket(String uploadFileName){
        try {
            //mediaRecorder.stop();
            InputStream is = getResources().openRawResource(R.raw.genre_classifier_bucket_creds_1);


            // The ID of your GCP project
            String projectId = "genre-classifier-293000";

            // The ID of your GCS bucket
            String bucketName = "genre-classifier-audio-files-bucket";

            // The path to your file to upload
            // String filePath = "C:\\Jerin\\datasets\\genre-guesser1\\test-recorded-dataset\\classical-mozart.wav";
            String filePath = uploadFileName;


            // The ID of your GCS object
            //String objectName = "testfile_"+currentTimestamp+".wav";
            String objectName = filePath.substring(filePath.lastIndexOf("/") + 1);

            Log.d("", "projectId: "+projectId+"; bucketName: "+bucketName+"; filePath: "+filePath+"; objectName: "+objectName);

            FileUploadToGCPBucket.uploadObject(projectId, bucketName, objectName, filePath, is);
            Log.d("Uploaded File URL: ", "https://storage.googleapis.com/genre-classifier-audio-files-bucket/"+objectName);
            Toast.makeText(MainActivity.this, "File Uploaded",
                    Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public View.OnClickListener getListenerToPlaySong(String songFileName){
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) throws IllegalArgumentException,
                    SecurityException, IllegalStateException {
                Log.d("buttonPlayWav", "Inside buttonPlayWav onClick");

                mediaPlayer = new MediaPlayer();
                try {
                    Log.d("buttonPlayWav", "songFileName: "+songFileName);
                    mediaPlayer.setDataSource(fileDir+ File.separator + songFileName);
                    mediaPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                mediaPlayer.start();
                Toast.makeText(MainActivity.this, "Recording Playing",
                        Toast.LENGTH_LONG).show();
            }
        };

        return onClickListener;
    }


    public List<Prediction> retrievePredictionDataFromDb(){

        Cursor dataCursor = predictionsStoreDB.getData();
        Log.d("retrieve", "dataCursor.getCount: "+dataCursor.getCount());

        List<Prediction> predictionList = new ArrayList<>();

        if (dataCursor != null) {
            // move cursor to first row
            if (dataCursor.moveToFirst()) {
                do {
                    // Get version from Cursor
                    String filename = dataCursor.getString(dataCursor.getColumnIndex("filename"));
                    String prediction = dataCursor.getString(dataCursor.getColumnIndex("prediction"));

                    Prediction prediction1 = new Prediction(filename, prediction);
                    predictionList.add(prediction1);
                } while (dataCursor.moveToNext());
            }
        }

        return predictionList;
    }


    public void populatePredictionHistoryTable(){

        List<Prediction> predictions = retrievePredictionDataFromDb();

        //table.removeAllViews();

        //table.get

        int count = table.getChildCount();
        for (int i = 1; i < count; i++) {
            View child = table.getChildAt(i);
            if (child instanceof TableRow) ((ViewGroup) child).removeAllViews();
        }


        for (int i = 1; i <=predictions.size(); i++) {

            Prediction prediction = predictions.get(i-1);

            TextView tv1 = new TextView(this);
            TextView tv2 = new TextView(this);
            TextView tv3 = new TextView(this);

            ImageButton playButton = new ImageButton(this);
            playButton.setImageResource(R.drawable.baseline_play_circle_outline_black_18dp);
            playButton.setBackground(null);
            playButton.setOnClickListener(getListenerToPlaySong(prediction.getFilename()));


            TableRow row= new TableRow(this);
            TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT);

            row.setLayoutParams(lp);
            /*checkBox = new CheckBox(this);
            tv = new TextView(this);
            addBtn = new ImageButton(this);
            addBtn.setImageResource(R.drawable.add);
            minusBtn = new ImageButton(this);
            minusBtn.setImageResource(R.drawable.minus);

            qty = new TextView(this);
            checkBox.setText("hello");
            qty.setText("10");
            row.addView(checkBox);
            row.addView(minusBtn);
            row.addView(qty);*/
            //row.addView(addBtn);

            //tv1.setTextAlignment(T);

            //tv1.setGravity(Gravity.CENTER_VERTICAL);
            tv2.setGravity(Gravity.CENTER_VERTICAL);
            tv3.setGravity(Gravity.CENTER_VERTICAL);

            tv1.setWidth(150);
            tv2.setWidth(200);
            tv3.setWidth(100);

            tv1.setMinWidth(150);
            tv2.setMinWidth(200);
            tv3.setMinWidth(100);

            //tv1.setText(""+i);
            tv2.setText(prediction.getFilename());
            tv3.setText(prediction.getPredictedGenre());

            row.setGravity(Gravity.CENTER_VERTICAL);

            row.addView(playButton,0);
            row.addView(tv2,1);
            row.addView(tv3,2);


            table.addView(row,i);
        }
    }

    public CountDownTimer setupTimer(){
        CountDownTimer ct = new CountDownTimer(30000, 1000) {
            int time=30;

            public void onTick(long millisUntilFinished) {
                secsText.setText("0:"+checkDigit(time));
                time--;
            }

            public void onFinish() {
                secsText.setText("0:00");
            }

        };

        return ct;

    }

    public String checkDigit(int number) {
        return number <= 9 ? "0" + number : String.valueOf(number);
    }

    public void MediaRecorderReady(){
        mediaRecorder=new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        mediaRecorder.setOutputFile(AudioSavePathInDevice);
        //mediaRecorder.setO
    }

    public String CreateRandomAudioFileName(int string){
        StringBuilder stringBuilder = new StringBuilder( string );
        int i = 0 ;
        while(i < string ) {
            stringBuilder.append(RandomAudioFileName.
                    charAt(random.nextInt(RandomAudioFileName.length())));

            i++ ;
        }
        return stringBuilder.toString();
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(MainActivity.this, new
                String[]{WRITE_EXTERNAL_STORAGE, RECORD_AUDIO}, RequestPermissionCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case RequestPermissionCode:
                if (grantResults.length> 0) {
                    boolean StoragePermission = grantResults[0] ==
                            PackageManager.PERMISSION_GRANTED;
                    boolean RecordPermission = grantResults[1] ==
                            PackageManager.PERMISSION_GRANTED;

                    if (StoragePermission && RecordPermission) {
                        Toast.makeText(MainActivity.this, "Permission Granted",
                                Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(MainActivity.this,"Permission Denied",Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }

    public boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(),
                WRITE_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(),
                RECORD_AUDIO);
        /*int result2 = ContextCompat.checkSelfPermission(getApplicationContext(),
                MANAGE_EXTERNAL_STORAGE);*/
        return result == PackageManager.PERMISSION_GRANTED &&
                result1 == PackageManager.PERMISSION_GRANTED;// &&
                //result2 == PackageManager.PERMISSION_GRANTED ;
    }


    private void writeToFile(String data, Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("config.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }
}