package com.unt.jerin.genreclassifier1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaRecorder;

import android.os.Environment;
//import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;

import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
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


    Button buttonStart, buttonStop, buttonPlayLastRecordAudio,
            buttonStopPlayingRecording ;
    Button buttonStartWav, buttonStopWav, uploadAudioToGCP, identifyGenre;
    TextView identifiedGenre;
    String AudioSavePathInDevice = null;
    MediaRecorder mediaRecorder ;
    Random random ;
    String RandomAudioFileName = "ABCDEFGHIJKLMNOP";
    public static final int RequestPermissionCode = 1;
    MediaPlayer mediaPlayer ;
    PredictionsStoreDB predictionsStoreDB;

    String lastRecordedFile = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        buttonStart = (Button) findViewById(R.id.button);
        buttonStop = (Button) findViewById(R.id.button2);
        buttonPlayLastRecordAudio = (Button) findViewById(R.id.button3);
        buttonStopPlayingRecording = (Button)findViewById(R.id.button4);


        buttonStartWav = (Button) findViewById(R.id.button5);
        buttonStopWav = (Button) findViewById(R.id.button6);
        uploadAudioToGCP = (Button) findViewById(R.id.button7);
        identifyGenre = (Button) findViewById(R.id.button8);


        buttonStop.setEnabled(false);
        buttonPlayLastRecordAudio.setEnabled(false);
        buttonStopPlayingRecording.setEnabled(false);
        identifiedGenre = (TextView)findViewById(R.id.identifiedGenre);

        // DB
        predictionsStoreDB = new PredictionsStoreDB(this);

        random = new Random();

        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(checkPermission()) {

                    /*AudioSavePathInDevice =Environment.getStorageDirectory().getAbsolutePath() + "/" +
                            CreateRandomAudioFileName(5) + "AudioRecording.3gp";*/

                    AudioSavePathInDevice =
                            Environment.getExternalStorageDirectory().getAbsolutePath() + "/" +
                                    CreateRandomAudioFileName(5) + "AudioRecording.3gp";

                    AudioSavePathInDevice = getExternalCacheDir().getAbsolutePath();
                    AudioSavePathInDevice += "/"  + CreateRandomAudioFileName(5) + "AudioRecording.wav";

                    System.out.println("AudioSavePathInDevice: "+AudioSavePathInDevice);

                    MediaRecorderReady();

                    try {
                        mediaRecorder.prepare();
                        mediaRecorder.start();
                    } catch (IllegalStateException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    buttonStart.setEnabled(false);
                    buttonStop.setEnabled(true);

                    Toast.makeText(MainActivity.this, "Recording started",
                            Toast.LENGTH_LONG).show();
                } else {
                    requestPermission();
                }

            }
        });

        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    mediaRecorder.stop();
                    buttonStop.setEnabled(false);
                    buttonPlayLastRecordAudio.setEnabled(true);
                    buttonStart.setEnabled(true);
                    buttonStopPlayingRecording.setEnabled(false);

                    Toast.makeText(MainActivity.this, "Recording Completed",
                            Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        buttonPlayLastRecordAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) throws IllegalArgumentException,
                    SecurityException, IllegalStateException {

                buttonStop.setEnabled(false);
                buttonStart.setEnabled(false);
                buttonStopPlayingRecording.setEnabled(true);

                mediaPlayer = new MediaPlayer();
                try {
                    mediaPlayer.setDataSource(AudioSavePathInDevice);
                    mediaPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                mediaPlayer.start();
                Toast.makeText(MainActivity.this, "Recording Playing",
                        Toast.LENGTH_LONG).show();
            }
        });

        buttonStopPlayingRecording.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonStop.setEnabled(false);
                buttonStart.setEnabled(true);
                buttonStopPlayingRecording.setEnabled(false);
                buttonPlayLastRecordAudio.setEnabled(true);

                if(mediaPlayer != null){
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    MediaRecorderReady();
                }
            }
        });


        /**
         * Wav recorder
         */
        String fileDir = getExternalCacheDir() + File.separator + "AudioRecord";
        RecordWavFile recordWav = new RecordWavFile(fileDir);

        buttonStartWav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(checkPermission()) {

                    recordWav.startWavFileRecord();

                    buttonStartWav.setEnabled(false);
                    buttonStopWav.setEnabled(true);

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

                    buttonStopWav.setEnabled(false);
                    uploadAudioToGCP.setEnabled(true);
                    buttonStartWav.setEnabled(true);
                    buttonStopPlayingRecording.setEnabled(false);

                    Toast.makeText(MainActivity.this, "Recording Wav Completed",
                            Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


        /**
         * Upload the audio file to GCP Bucket
         */
        uploadAudioToGCP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    //mediaRecorder.stop();
                    InputStream is = getResources().openRawResource(R.raw.genre_classifier_bucket_creds);


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
        });


        /**
         * Call GCP Cloud Function to identify genre
         */

        GoogleFunctionRESTClient gcpFunctionClient = new GoogleFunctionRESTClient();
        identifyGenre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    //mediaRecorder.stop();
                    Log.d("identifyGenre", "Inside identifyGenre onClick");

                    String baseFileName = lastRecordedFile.substring(lastRecordedFile.lastIndexOf("/") + 1);

                    String prediction = gcpFunctionClient.getPredictionFromCloudFunction(baseFileName);//"20201025035339.wav" baseFileName
                    Log.d("identifyGenre", "prediction: "+prediction);

                    identifiedGenre.setText(prediction);

                    predictionsStoreDB.insertPredictionData(baseFileName, prediction);

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