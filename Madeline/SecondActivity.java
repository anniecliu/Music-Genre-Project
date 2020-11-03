package com.example.genreclassifier;

import java.io.File;
import java.io.IOException;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class SecondActivity extends Activity {
    MediaRecorder recorder;
    File audio = null;
    static final String TAG = "MediaRecording";
    Button recordButton,stopButton, playButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        recordButton = (Button) findViewById(R.id.button2);
        stopButton = (Button) findViewById(R.id.button3);
        playButton = (Button) findViewById(R.id.button5);
    }

    public void startRecording(View view) throws IOException {
        recordButton.setEnabled(false);
        stopButton.setEnabled(true);
        //Creating file
        File dir = Environment.getExternalStorageDirectory();
        try {
            String audio = getExternalCacheDir().getAbsolutePath()+"filename.wav";
        } catch (Exception e) {
            e.printStackTrace();
        }
        //Creating MediaRecorder and specifying audio source, output format, encoder & output format
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorder.setOutputFile(audio.getAbsolutePath());
        recorder.prepare();
        recorder.start();
    }

    public void stopRecording(View view) {
        recordButton.setEnabled(true);
        stopButton.setEnabled(false);
        //stopping recorder
        recorder.stop();
        //after stopping the recorder, create the sound file and add it to media library.
        addToLibrary();
        recorder.release();
    }

    public void guessTheGenre(View view) {
        //the genre guessing function
    }

    public MediaPlayer playRecording(View view) {
        recordButton.setEnabled(false);
        stopButton.setEnabled(false);
        playButton.setEnabled(true);
        MediaPlayer mp = MediaPlayer.create(this, R.id.button3);
        return mp;
    }

    protected void addToLibrary() {
        //creating content values of size 4
        ContentValues values = new ContentValues(4);
        long current = System.currentTimeMillis();
        values.put(MediaStore.Audio.Media.TITLE, "audio" + audio.getName());
        values.put(MediaStore.Audio.Media.DATE_ADDED, (int) (current / 1000));
        values.put(MediaStore.Audio.Media.MIME_TYPE, "audio/3gpp");
        values.put(MediaStore.Audio.Media.DATA, audio.getAbsolutePath());

        //creating content resolver and storing it in the external content uri
        ContentResolver contentResolver = getContentResolver();
        Uri base = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Uri newUri = contentResolver.insert(base, values);

        //sending broadcast message to scan the media file so that it can be available
        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, newUri));
        Toast.makeText(this, "Added File " + newUri, Toast.LENGTH_LONG).show();
    }
}