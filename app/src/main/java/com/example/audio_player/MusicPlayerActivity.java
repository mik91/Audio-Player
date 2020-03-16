package com.example.audio_player;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.audio_player.Enum.PlaybackStatus;
import com.example.audio_player.Fragements.AudioListFragment;
import com.example.audio_player.MediaPlayer.MediaPlayerService;
import com.example.audio_player.Models.Audio;
import com.example.audio_player.Storage.StorageUtil;

import java.util.ArrayList;
import java.util.Collections;

public class MusicPlayerActivity extends AppCompatActivity implements AudioListFragment.OnListFragmentInteractionListener {
    SeekBar seekBar;
    TextView totalTime, currentTime;
    private ArrayList<Audio> audioList;
    private static Handler mHandler = new Handler();
    public static final String Broadcast_PLAY_NEW_AUDIO = "com.example.audio_player.PlayNewAudio";
    private MediaPlayerService player;
    boolean serviceBound = false;
    private ImageView playPauseBtn;
    private AudioListFragment fragmentLstAudio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);
        Load();
        handleCLickListener();
    }

    private void handleCLickListener() {
        playPauseBtn = findViewById(R.id.play_pause_btn);
        ImageView previousBtn = findViewById(R.id.previous_btn);
        ImageView nextBtn = findViewById(R.id.next_btn);
        final ImageView repeatBtn = findViewById(R.id.repeat_btn);
        final ImageView suffleBtn = findViewById(R.id.shuffle_btn);

        playPauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (player == null) {
                    playAudio(0, audioList);
                } else if (player.isPaused) {
                    setPlayPauseBtn(true);
                    player.resumeMedia();
                    player.buildNotification(PlaybackStatus.PLAYING);
                } else {
                    player.pauseMedia();
                    player.buildNotification(PlaybackStatus.PAUSED);
                    setPlayPauseBtn(true);
                }
            }
        });
        previousBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handlePreviousNext(true);
                setPlayPauseBtn(false);
            }
        });
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handlePreviousNext(false);
                setPlayPauseBtn(false);
            }
        });
        suffleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (player != null && player.isSchuffleMode) {
                    player.shuffleMusic(audioList);
                    suffleBtn.setColorFilter(ContextCompat.getColor(MusicPlayerActivity.this, R.color.color_white));
                    player.changeSchuffleMode(false);
                } else {
                    ArrayList<Audio> shuffleList = new ArrayList<>(audioList);
                    Collections.shuffle(shuffleList);
                    player.shuffleMusic(shuffleList);
                    player.changeSchuffleMode(true);
                    suffleBtn.setColorFilter(ContextCompat.getColor(MusicPlayerActivity.this, R.color.color_orange));
                }
            }
        });
        repeatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (player != null) {
                    if (!player.mediaPlayer.isLooping()) {
                        player.mediaPlayer.setLooping(true);
                        repeatBtn.setColorFilter(ContextCompat.getColor(MusicPlayerActivity.this, R.color.color_orange));
                    } else {
                        player.mediaPlayer.setLooping(false);
                        repeatBtn.setColorFilter(ContextCompat.getColor(MusicPlayerActivity.this, R.color.color_white));
                    }
                }
            }
        });
    }

    public void Load() {
        seekBar = findViewById(R.id.seekBar);
        totalTime = findViewById(R.id.totalTime);
        currentTime = findViewById(R.id.currentTime);
        audioList = Utility.getAllAudioFromDevice();
        FragmentManager mFragmentMagager = getSupportFragmentManager();
        FragmentTransaction ft = mFragmentMagager.beginTransaction();
        fragmentLstAudio = AudioListFragment.newInstance(audioList);
        ft.replace(R.id.container, fragmentLstAudio);
        ft.addToBackStack(null);
        ft.commit();
        checkPermissions();
    }

    public void checkPermissions() {
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 2);
        }
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 3);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
                    Utility.download(this);
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case 2:
            case 3: {
                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
                break;
            }
            default:
                throw new IllegalStateException("Unexpected value: " + requestCode);
        }
    }

//    public void checkCompletionListener() {
//        player.mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//            @Override
//            public void onCompletion(MediaPlayer mp) {
//                if (!mp.isLooping()) {
//                    handlePreviousNext(false);
//                    checkCompletionListener();
//                }
//            }
//        });
//    }

    public static void stopHandler() {
        mHandler.removeCallbacksAndMessages(null);
    }

    public void startHandler() {
        mHandler.postDelayed(notification, 10);
    }

    Runnable notification = new Runnable() {
        public void run() {
            if (player.mediaPlayer != null) {
                setMusicInfo();

                if (player.isPaused)
                    setPlayPauseBtn(true);
                else if ((Integer) playPauseBtn.getTag() == R.drawable.ic_play_vector)
                    setPlayPauseBtn(false);
            }
            mHandler.postDelayed(this, 100);
        }
    };

    private void setMusicInfo() {
        TextView audioName = findViewById(R.id.audioName);
        if (!audioName.getText().toString().equals(player.activeAudio.getTitle())) {
            audioName.setText(player.activeAudio.getTitle());
            TextView artistName = findViewById(R.id.artistName);
            artistName.setText(player.activeAudio.getArtist());
            seekBar.setMax(player.mediaPlayer.getDuration() / 10);
            totalTime.setText(Utility.getTimeString(player.mediaPlayer.getDuration()));
            fragmentLstAudio.changeMusicColor(findIndexOfTrack(player.activeAudio));
        }

        int mCurrentPosition = player.mediaPlayer.getCurrentPosition();
        currentTime.setText(Utility.getTimeString(mCurrentPosition));
        seekBar.setProgress(mCurrentPosition / 10);
    }

    public void setPlayPauseBtn(boolean pause) {
        if (pause) {
            playPauseBtn.setImageResource(R.drawable.ic_play_vector);
            playPauseBtn.setTag(R.drawable.ic_play_vector);
        } else {
            playPauseBtn.setImageResource(R.drawable.ic_pause_black_24dp);
            playPauseBtn.setTag(R.drawable.ic_pause_black_24dp);
        }
    }

    public void enableSeekBar() {
        startHandler();
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    player.mediaPlayer.seekTo(progress * 10);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    @Override
    public void onBackPressed() {
        player.stopMedia();
        super.onBackPressed();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean("ServiceState", serviceBound);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        serviceBound = savedInstanceState.getBoolean("ServiceState");
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serviceBound) {
            unbindService(serviceConnection);
            player.stopSelf();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Utility.uploadFile(data.getData(), this);
        }
    }

    private void handlePreviousNext(Boolean previousMode) {
        stopHandler();
        if (previousMode)
            player.skipToPrevious();
        else
            player.skipToNext();
        startHandler();
    }

    private void Filechooser() {
        Intent intent = new Intent();
        intent.setType("audioList/mpeg");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        try {
            startActivityForResult(Intent.createChooser(intent, "Select a File to Upload"), 1);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "Please install a File Manager.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onListFragmentInteraction(Audio item) {
        if (player != null)
            if (!player.isSchuffleMode)
                player.skipToTrack(findIndexOfTrack(item));
            else player.skipToTrack(findIndexOfTrackInShuffleMode(item));
        else
            playAudio(findIndexOfTrack(item), audioList);
    }

    public int findIndexOfTrack(Audio audio) {
        for (int i = 0; i < audioList.size(); i++) {
            if (audioList.get(i).getTitle().equals(audio.getTitle()))
                return i;
        }

        return -1;
    }

    public int findIndexOfTrackInShuffleMode(Audio audio) {
        StorageUtil storage = new StorageUtil(getApplicationContext());
        ArrayList<Audio> shuffleList = new ArrayList<>(storage.loadAudio());

        for (int i = 0; i < shuffleList.size(); i++) {
            if (shuffleList.get(i).getTitle().equals(audio.getTitle()))
                return i;
        }

        return -1;
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
            player = binder.getService();
            enableSeekBar();
            //checkCompletionListener();
            serviceBound = true;

            Toast.makeText(MusicPlayerActivity.this, "Service Bound", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };

    private void playAudio(int audioIndex, ArrayList<Audio> audioArrayList) {
        if (!serviceBound) {
            StorageUtil storage = new StorageUtil(getApplicationContext());
            storage.storeAudio(audioArrayList);
            storage.storeAudioIndex(audioIndex);

            Intent playerIntent = new Intent(this, MediaPlayerService.class);
            startService(playerIntent);
            bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        } else {
            StorageUtil storage = new StorageUtil(getApplicationContext());
            storage.storeAudioIndex(audioIndex);

            Intent broadcastIntent = new Intent(Broadcast_PLAY_NEW_AUDIO);
            sendBroadcast(broadcastIntent);
        }
        setPlayPauseBtn(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_upload) {
            Filechooser();
            return true;
        } else if (item.getItemId() == R.id.menu_download) {
            Utility.download(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
