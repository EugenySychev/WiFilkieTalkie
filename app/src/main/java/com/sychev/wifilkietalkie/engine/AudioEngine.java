package com.sychev.wifilkietalkie.engine;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.util.Log;

public class AudioEngine {
    private static final String TAG = "AudioEng";
    private AudioRecord mRecorder = null;
    private AudioTrack mPlayer = null;

    private static final int RECORDING_RATE = 44100;
    private static final int CHANNEL = AudioFormat.CHANNEL_IN_MONO;
    private static final int FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private static final int BUFFER_SIZE = AudioRecord.getMinBufferSize(
            RECORDING_RATE, CHANNEL, FORMAT);
    private boolean mIsRecording = false;
    private DataHandler mDataHandler = null;

    public interface DataHandler {
        void sendData(byte[] array, int size);

    }

    public AudioEngine(DataHandler handler) {
        mDataHandler = handler;
        mRecorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                RECORDING_RATE, CHANNEL, FORMAT, BUFFER_SIZE * 10);

        mPlayer = new AudioTrack.Builder()
                .setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build())
                .setAudioFormat(new AudioFormat.Builder()
                        .setEncoding(FORMAT)
                        .setSampleRate(RECORDING_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build())
                .setBufferSizeInBytes(BUFFER_SIZE)
                .build();
    }

    public void startStreaming() {
        Thread mRecordThread = new Thread(new Runnable() {
            @Override
            public void run() {

                Log.d(TAG, "AudioRecord recording...");
                mRecorder.startRecording();

                while (mIsRecording) {
                    byte[] buffer = new byte[BUFFER_SIZE];
                    int read = mRecorder.read(buffer, 0, buffer.length);
                    if (mDataHandler != null)
                        mDataHandler.sendData(buffer, read);
                }
                ;

            }
        });
        mIsRecording = true;
        mRecordThread.start();
    }

    public void stopStreaming() {
        mIsRecording = false;
        mRecorder.release();
    }

    public void playData(byte[] buffer, int size) {
        mPlayer.play();
        mPlayer.write(buffer, 0, size);
        try {
            mPlayer.wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mPlayer.stop();
    }

}
