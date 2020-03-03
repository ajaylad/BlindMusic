package com.ajay.blindmusic;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;


import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;


public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener, GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener, AudioManager.OnAudioFocusChangeListener {

    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;
    int vis = 0;
    TextView textView, title, artist, instruWords;
    ImageView iv;
    TextToSpeech speaker, startphrase;
    CardView instr;
    MediaPlayer mediaPlayer;
    AudioManager am;
    private ArrayList<Song> songList;
    private GestureDetector gestureDetector;
    int musicFlag = 0;
    int length;
    int pos = 0;
    boolean musicOn= false;
    private static final int SWIPE_THRESHOLD =100 ;
    private static final int SWIPE_VELOCITY_THRESHOLD = 100;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); //will hide the title
        getSupportActionBar().hide(); // hide the title bar
        setContentView(R.layout.activity_main);
        am = (AudioManager)getSystemService(AUDIO_SERVICE);
        int result = am.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);
        if (result==AudioManager.AUDIOFOCUS_REQUEST_GRANTED){

        }
        iv = findViewById(R.id.imageView);
        textView = findViewById(R.id.textView);
        title = findViewById(R.id.title);
        artist = findViewById(R.id.artist);
        instr = findViewById(R.id.cardview);
        instruWords = findViewById(R.id.instructionWords);
        this.gestureDetector = new GestureDetector(this,this);
        gestureDetector.setOnDoubleTapListener(this);
        mediaPlayer = new MediaPlayer();
        songList = new ArrayList<Song>();
        title.setText("");
        artist.setText("");
        startphrase = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {

                    instr.setVisibility(View.VISIBLE);
//            textView.setVisibility(View.INVISIBLE);
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            instr.setVisibility(View.INVISIBLE);
                            textView.setVisibility(View.VISIBLE);
                        }
                    }, 15000);
                    startphrase.setLanguage(Locale.ENGLISH);
                    String toSpeak = instruWords.getText().toString();
                    startphrase.speak(toSpeak,TextToSpeech.QUEUE_ADD,null);
                }
            }
        });

        speaker = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    speaker.setLanguage(Locale.ENGLISH);
                }
            }
        });


        getSongList();
        Log.d("size", Integer.toString(songList.size()));
//        Log.d("check", songList.get(0).getTitle());


        for (int i=0; i < songList.size(); i++){
            Log.d("array", songList.get(i).getID()+" "+ songList.get(i).getTitle() + " " + songList.get(i).getArtist());
        }
//        try {
//            playSong();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }


        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {

                Log.d("oncompletion", "in Oncompletion");

                try {

                    Log.d("musicOn", " " + musicOn);

                    if (musicOn) {
                        mediaPlayer.reset();

                        pos = pos + 1;

                        if (pos >= songList.size()) {
                            pos = 0;
                        }

                        Song playSong = songList.get(pos);
                        long currSong = playSong.getID();

                        title.setText(songList.get(pos).getTitle());
                        artist.setText(songList.get(pos).getArtist());

                        //set uri
                        Uri trackUri = ContentUris.withAppendedId(
                                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                currSong);

                        try {
                            mediaPlayer.setDataSource(getApplicationContext(), trackUri);
                        } catch (Exception e) {
                            Log.e("MUSIC SERVICE", "Error setting data source", e);
                        }
                        mediaPlayer.prepare();
                        mediaPlayer.start();
                    }


                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                } catch (IOException io) {
                    io.printStackTrace();
                }
            }
        });





    }





    @Override
    public void onInit(int status) {

    }


    public boolean onTouchEvent(MotionEvent event) {
        this.gestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);

    }


    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {

        if (musicOn){

            if (musicFlag == 1){
                mediaPlayer.pause();
                length = mediaPlayer.getCurrentPosition();
                musicFlag = 0;
                iv.setImageResource(R.drawable.play);
                //textView.setText("paused");
            }
            else{
                iv.setImageResource(R.drawable.pause);
                mediaPlayer.seekTo(length);
                mediaPlayer.start();
                musicFlag = 1;
            }

        }

        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {


        if (musicFlag==0){
            musicOn = true;
            //textView.setText("music on");
            try {
                playSong();
                iv.setImageResource(R.drawable.pause);
                title.setText(songList.get(pos).getTitle());
                artist.setText(songList.get(pos).getArtist());

                musicFlag = 1;
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        else{
            musicOn = false;
            //textView.setText("music off");
            musicFlag = 0;
            mediaPlayer.stop();
            iv.setImageResource(R.drawable.play);
        }


        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDown(MotionEvent e) {

        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

        //textView.setText("longpressed");

            instr.setVisibility(View.VISIBLE);
//            textView.setVisibility(View.INVISIBLE);
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    instr.setVisibility(View.INVISIBLE);
                    textView.setVisibility(View.VISIBLE);
                }
            }, 10000);

            String toSpeak = instruWords.getText().toString();

            speaker.speak(toSpeak,TextToSpeech.QUEUE_ADD,null);



    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {


        boolean result = false;
        try {
            float diffY = e2.getY() - e1.getY();
            float diffX = e2.getX() - e1.getX();
            if (Math.abs(diffX) > Math.abs(diffY)) {
                if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffX > 0) {
                        onSwipeRight();
                    } else {
                        onSwipeLeft();
                    }
                    result = true;
                }
            }
            else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                if (diffY > 0) {
                    onSwipeBottom();
                } else {
                    onSwipeTop();
                }
                result = true;
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return result;


    }
    public void onSwipeBottom() {

    }
    public void onSwipeTop() {

    }
    public void onSwipeRight() {

        try{

            if (musicOn){

                mediaPlayer.reset();

                pos = pos - 1;
                if (pos < 0){
                    pos = songList.size()-1;
                }

                Song playSong = songList.get(pos);
                long currSong = playSong.getID();
                //set uri
                Uri trackUri = ContentUris.withAppendedId(
                        android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        currSong);

                try {
                    mediaPlayer.setDataSource(getApplicationContext(), trackUri);
                } catch (Exception e) {
                    Log.e("MUSIC SERVICE", "Error setting data source", e);
                }
                mediaPlayer.prepare();
                mediaPlayer.start();
                iv.setImageResource(R.drawable.pause);

                title.setText(songList.get(pos).getTitle());
                artist.setText(songList.get(pos).getArtist());


            }



        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException io) {
            io.printStackTrace();
        }



    }

    public void onSwipeLeft() {

        try{
            if(musicOn){
                mediaPlayer.reset();

                pos=pos+1;

                if (pos >= songList.size()){
                    pos=0;
                }

                Song playSong = songList.get(pos);
                long currSong = playSong.getID();

                //set uri
                Uri trackUri = ContentUris.withAppendedId(
                        android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        currSong);

                try {
                    mediaPlayer.setDataSource(getApplicationContext(), trackUri);
                } catch (Exception e) {
                    Log.e("MUSIC SERVICE", "Error setting data source", e);
                }
                mediaPlayer.prepare();
                mediaPlayer.start();
                iv.setImageResource(R.drawable.pause);

                title.setText(songList.get(pos).getTitle());
                artist.setText(songList.get(pos).getArtist());


            }


        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException io) {
            io.printStackTrace();
        }

    }



    public void getSongList(){
        try {
            int long_press_for_instructions = startphrase.speak("Long press for instructions", TextToSpeech.QUEUE_FLUSH, null);
            Log.d("msg", " " + long_press_for_instructions);
        }
        catch (Exception e){
            Log.d("catch", "catch");
        }

        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor =musicResolver.query(musicUri,null,null,null,null);
        if (musicCursor != null){ //&& musicCursor.moveToPosition(3)) {
            //get columns
            musicCursor.moveToFirst();
            int titleColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ARTIST);
            //add songs to list

            Log.d("TAG",musicCursor.getCount()+" "+musicCursor.getPosition());
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                songList.add(new Song(thisId, thisTitle, thisArtist));
            }
            while (musicCursor.moveToNext());
        }
    }


    private void playSong() throws IOException {
        try{
            mediaPlayer.reset();
            Song playSong = songList.get(pos);
            long currSong = playSong.getID();
            //set uri
            Uri trackUri = ContentUris.withAppendedId(
                    android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    currSong);

            try {
                mediaPlayer.setDataSource(getApplicationContext(), trackUri);
            } catch (Exception e) {
                Log.e("MUSIC SERVICE", "Error setting data source", e);
            }
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException io) {
            io.printStackTrace();
        }
    }

    @Override
    public void onAudioFocusChange(int focusChange) {

        if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT){
            mediaPlayer.pause();
        }
        else if (focusChange == AudioManager.AUDIOFOCUS_GAIN){
            mediaPlayer.start();
        }
        else if(focusChange == AudioManager.AUDIOFOCUS_LOSS)
        {
            mediaPlayer.stop();
        }
    }
}
