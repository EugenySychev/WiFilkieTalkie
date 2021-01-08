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

    private static final int RECORDING_RATE = 8000;
    private static final int CHANNEL = AudioFormat.CHANNEL_IN_MONO;
    private static final int FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private static final int BUFFER_SIZE = AudioRecord.getMinBufferSize(
            RECORDING_RATE, CHANNEL, FORMAT);
    private boolean mIsRecording = false;
    private DataHandler mDataHandler = null;

    public interface DataHandler {
        void recordedData(byte[] array, int size);

    }

    public AudioEngine(DataHandler handler) {
        mDataHandler = handler;

        mPlayer = new AudioTrack.Builder()
                .setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build())
                .setAudioFormat(new AudioFormat.Builder()
                        .setEncoding(FORMAT)
                        .setSampleRate(RECORDING_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build())
                .setBufferSizeInBytes(BUFFER_SIZE)
                .build();
        mPlayer.play();

    }

    public void startStreaming() {
        Thread mRecordThread = new Thread(new Runnable() {
            @Override
            public void run() {

                if (mRecorder == null)
                    mRecorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                            RECORDING_RATE, CHANNEL, FORMAT, BUFFER_SIZE * 10);

                if (mRecorder.getState()  == AudioRecord.STATE_INITIALIZED) {
                    mRecorder.startRecording();
                    Log.d(TAG, "AudioRecord recording...");

                    while (mIsRecording) {
                        byte[] buffer = new byte[BUFFER_SIZE];
                        int read = mRecorder.read(buffer, 0, buffer.length);
                        if (mDataHandler != null)
                            mDataHandler.recordedData(buffer, read);
                    }
                }

            }
        });
        mIsRecording = true;
        mRecordThread.start();
    }

    public void stopStreaming() {
        mIsRecording = false;
        if (mRecorder != null)
            mRecorder.release();
            mRecorder = null;
    }

    public void startPlayer() {
        mPlayer.play();
    }

    public void stopPlayer() {
        mPlayer.stop();
    }

    public void playData(byte[] buffer, int size) {
        Log.d(TAG, buffer.toString());
        mPlayer.write(buffer, 0, size);
    }

}
