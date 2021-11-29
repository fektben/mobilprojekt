package hun.unideb.inf.voicerecorder;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static int SHAKE_THRESHOLD = 3;
    private static final int TIME_BETWEEN_SHAKES = 3000;
    private long mShakeTime;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;

    private TextView playAudio, stopAudio, stopRecord, allapotJelzo;
    private MediaRecorder mRecorder;
    private MediaPlayer mPlayer;

    private static String mFileName = null;
    public static final int REQUEST_AUDIO_PERMISSION_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        playAudio = findViewById(R.id.playBtn);
        stopAudio = findViewById(R.id.stopBtn);
        stopRecord = findViewById(R.id.stopRecordBtn);
        allapotJelzo = findViewById(R.id.allapotText);


        stopRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pauseRecording();
            }
        });

        playAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playAudio();
            }
        });
        stopAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pauseAudio();
            }
        });
    }

    private void startRecording() {

        if (CheckPermissions()) {

            mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
            mFileName += "/AudioRecording.3gp";
            mRecorder = new MediaRecorder();

            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

            mRecorder.setOutputFile(mFileName);
            try {
                mRecorder.prepare();
            } catch (IOException e) {
                Log.e("TAG", "prepare() failed");
            }
            mRecorder.start();
            allapotJelzo.setText("Hangfelvétel elkezdve.");
        } else {
            RequestPermissions();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_AUDIO_PERMISSION_CODE:
                if (grantResults.length > 0) {
                    boolean permissionToRecord = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean permissionToStore = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (permissionToRecord && permissionToStore) {
                        Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Permission Denied", Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }

    public boolean CheckPermissions() {

        int result = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(), RECORD_AUDIO);
        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED;
    }

    private void RequestPermissions() {

        ActivityCompat.requestPermissions(MainActivity.this, new String[]{RECORD_AUDIO, WRITE_EXTERNAL_STORAGE}, REQUEST_AUDIO_PERMISSION_CODE);
    }


    public void playAudio() {

        mPlayer = new MediaPlayer();
        try {

            mPlayer.setDataSource(mFileName);
            mPlayer.prepare();
            mPlayer.start();
            allapotJelzo.setText("Lejátszás.");

        } catch (IOException e) {
            Log.e("TAG", "prepare() failed");
        }
    }

    public void pauseAudio() {

        mPlayer.release();
        mPlayer = null;
        allapotJelzo.setText("Lejátszás megállítva.");
    }

    public void pauseRecording() {

        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
        allapotJelzo.setText("Felvétel megállítva.");
    }

    @Override
    protected void onPause() {

        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    protected void onResume() {

        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_UI );
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            long curTime = System.currentTimeMillis();
            if ((curTime - mShakeTime) > TIME_BETWEEN_SHAKES) {

                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];

                double acceleration = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2)) - SensorManager.GRAVITY_EARTH;

                if (acceleration > SHAKE_THRESHOLD) {

                    startRecording();
                    mShakeTime = curTime;
                    //allapotJelzo.setText("hangpróbaegykettő");
                }
            }
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}