package com.example.musicapp;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class PlaySong extends AppCompatActivity {
    TextView songname, playtime, songLength;
    ImageView play, previous, next;
    SeekBar seekBar;
    ArrayList<File> songs;
    MediaPlayer mediaPlayer;
    String textContent;
    int position;
    Thread updateSeek;
    private volatile boolean isThreadRunning = true;
    private Handler mHandler = new Handler();
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_song);
        songname = findViewById(R.id.songname);
        play = findViewById(R.id.play);
        previous = findViewById(R.id.previous);
        next = findViewById(R.id.next);
        seekBar = findViewById(R.id.seekBar);
        playtime = findViewById(R.id.playtime);
        songLength = findViewById(R.id.songLength);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        songs = (ArrayList)bundle.getParcelableArrayList("songList");
        textContent = intent.getStringExtra("currentSong");
        songname.setText(textContent);
        songname.setSelected(true);
        position = intent.getIntExtra("position", 0);
        Uri uri = Uri.parse(songs.get(position).toString());
//        star mediaPlayer for playing music
        mediaPlayer = MediaPlayer.create(this, uri);
        mediaPlayer.start();
        seekBar.setMax(mediaPlayer.getDuration());
        updateSeekBar();
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.seekTo(seekBar.getProgress());
            }
        });
//        update seekbar together playing music
//        updateSeek = new Thread(){
//            @Override
//            public void run() {
//                int currentPosition = 0;
//                try {
//                    while(currentPosition<mediaPlayer.getDuration()){
//                            currentPosition = mediaPlayer.getCurrentPosition();
//                            seekBar.setProgress(currentPosition);
//                            sleep(800);
//                    }
//                }
//                catch (Exception e){
//                    e.printStackTrace();
//                }
//            }
//        };
//        updateSeek.start();
////        after complete first song then play next song
//        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//            @Override
//            public void onCompletion(MediaPlayer mp) {
//                mediaPlayer.stop();
//                mediaPlayer.release();
//                if (position != songs.size() - 1){
//                    position = position + 1;
//                }
//                else {
//                    position = 0;
//                }
//                Uri uri = Uri.parse(songs.get(position).toString());
//                mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
//                mediaPlayer.start();
//                play.setImageResource(R.drawable.play);
//                seekBar.setMax(mediaPlayer.getDuration());
//                textContent = songs.get(position).getName().toString();
//                textView.setText(textContent);
//            }
//        });


        updateSeek = new Thread() {
            @Override
            public void run() {
                try {
                    while (isThreadRunning) {
                        int currentPosition = mediaPlayer.getCurrentPosition();
                        seekBar.setProgress(currentPosition);
                        sleep(800);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        updateSeek.start();

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mediaPlayer.stop();
                mediaPlayer.release();
                if (position != songs.size() - 1) {
                    position = position + 1;
                } else {
                    position = 0;
                }
                isThreadRunning = false;
                try {
                    updateSeek.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Uri uri = Uri.parse(songs.get(position).toString());
                mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
                mediaPlayer.start();
                play.setImageResource(R.drawable.play);
                seekBar.setMax(mediaPlayer.getDuration());
                textContent = songs.get(position).getName().toString();
                songname.setText(textContent);

                isThreadRunning = true;
                updateSeek = new Thread() {
                    @Override
                    public void run() {
                        try {
                            while (isThreadRunning) {
                                int currentPosition = mediaPlayer.getCurrentPosition();
                                seekBar.setProgress(currentPosition);
                                sleep(800);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                };
                updateSeek.start();
            }
        });



        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mediaPlayer.isPlaying()){
                    play.setImageResource(R.drawable.pause);
                    mediaPlayer.pause();
                }
                else{
                    play.setImageResource(R.drawable.play);
                    mediaPlayer.start();
                    updateSeekBar();
                }
            }
        });
        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.stop();
                mediaPlayer.release();
                if (position!=0){
                    position = position - 1;
                }
                else{
                    position = songs.size()-1;
                }
                Uri uri = Uri.parse(songs.get(position).toString());
                mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
                mediaPlayer.start();
                play.setImageResource(R.drawable.play);
                seekBar.setMax(mediaPlayer.getDuration());
                textContent = songs.get(position).getName().toString();
                songname.setText(textContent);
                updateSeekBar();
            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.stop();
                mediaPlayer.release();
                if(position != songs.size()-1){
                    position = position + 1;
                }
                else{
                    position = 0;
                }
                Uri uri = Uri.parse(songs.get(position).toString());
                mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
                mediaPlayer.start();
                play.setImageResource(R.drawable.play);
                seekBar.setMax(mediaPlayer.getDuration());
                textContent = songs.get(position).getName().toString();
                songname.setText(textContent);
                updateSeekBar();

            }
        });
    }
//    song length and song play time method
    private void updateSeekBar() {
        mHandler.postDelayed(mRunnable, 1000);
    }
    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            if(mediaPlayer != null){
                int currentPosition = mediaPlayer.getCurrentPosition();
                seekBar.setProgress(currentPosition);
                updatePlayTime(currentPosition);
                mHandler.postDelayed(this, 1000);
            }
        }
    };

    private void updatePlayTime(int currentPosition){
        long minutes = TimeUnit.MILLISECONDS.toMinutes(currentPosition);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(currentPosition) - TimeUnit.MINUTES.toSeconds(minutes);
        playtime.setText(String.format("%02d:%02d", minutes, seconds));

        long songMinutes = TimeUnit.MILLISECONDS.toMinutes(mediaPlayer.getDuration());
        long songSeconds = TimeUnit.MILLISECONDS.toSeconds(mediaPlayer.getDuration()) - TimeUnit.MINUTES.toSeconds(songMinutes);
        songLength.setText(String.format("%02d:%02d", songMinutes, songSeconds));
    }
    @Override
    protected void onDestroy() {
//        destroy music when back the main activity
        super.onDestroy();
        isThreadRunning = false;
        mediaPlayer.stop();
        mediaPlayer.release();
        mHandler.removeCallbacks(mRunnable);
//        updateSeek.interrupt();
        try {
            updateSeek.join();
        }
        catch (InterruptedException e){
            e.printStackTrace();
        }
    }
}
