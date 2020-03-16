//package com.example.audio_player.Services;
//
//import android.content.Context;
//import android.media.MediaPlayer;
//import android.net.Uri;
//import android.widget.Toast;
//
//import com.example.audio_player.Models.Audio;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Random;
//
//import static java.util.Collections.*;
//
//public class MusicServices {
//    private List<Audio> initPlayList;
//    private List<Audio> playList;
//    private List<Audio> lstSongs;
//    private Audio currentSong;
//    public MediaPlayer mediaPlayer;
//    private Context context;
//    public Boolean isShuffleMode;
//    private int playListPosition;
//    private int shufflePlayListPosition;
//    private int realShufflePlayListPosition;
//
//    public MusicServices(List<Audio> p_playList, Context context) {
//        playList = p_playList;
//        initPlayList = new ArrayList<>();
//        initPlayList.addAll(p_playList);
//        this.context = context;
//        lstSongs = new ArrayList<>();
//        isShuffleMode = false;
//    }
//
//    public void startPlaying(){
//        if (currentSong != null) {
//            if (currentSong.getInPause()) {
//                mediaPlayer.start();
//                currentSong.setInPause(false);
//                return;
//            } else {
//                mediaPlayer.release();
//                mediaPlayer = null;
//            }
//        }
//        handleNextSong();
//        Toast.makeText(context, "Playing from device; "+ currentSong.getaPath(),
//                Toast.LENGTH_SHORT).show();
//        mediaPlayer = MediaPlayer.create(context, Uri.parse(currentSong.getaPath()));
//        mediaPlayer.start();
//    }
//
//    private void handleNextSong() {
//        int position = isShuffleMode?++realShufflePlayListPosition:playListPosition;
//
//        if (position >= lstSongs.size()) {
//            findNextSong();
//        }
//        currentSong = lstSongs.get(position);
//    }
//
//    public void continuePlaying() {
//        if (!isShuffleMode) {
//            if(playListPosition==0){
//                if(playList.size()>1){
//                    playListPosition++;
//                }
//            } else if((lstSongs.size()+1)==playList.size()){
//                playListPosition=0;
//            } else{
//                playListPosition++;
//            }
//        } else
//            shufflePlayListPosition++;
//        currentSong = null;
//        startPlaying();
//    }
//
//    public void goBackPlaying() {
//        if(playListPosition!=0){
//            --playListPosition;
//        }
//        currentSong = null;
//        startPlaying();
//    }
//
//    public void stopPlaying(){
//        if(mediaPlayer!=null){
//            mediaPlayer.stop();
//            mediaPlayer.release();
//        }
//    }
//
//    public void pausePlaying(){
//        if(mediaPlayer!=null){
//            if (isShuffleMode)
//                lstSongs.get(realShufflePlayListPosition).setInPause(true);
//            else
//                lstSongs.get(playListPosition).setInPause(true);
//            mediaPlayer.pause();
//        }
//    }
//    private void findNextSong() {
//        if (isShuffleMode)
//            lstSongs.add(playList.get(shufflePlayListPosition));
//        else
//            lstSongs.add(playList.get(playListPosition));
//    }
//
//    public void shuffleMusic() {
//        shuffle(playList, new Random(System.nanoTime()));
//        isShuffleMode = true;
//        realShufflePlayListPosition = playListPosition;
//        shufflePlayListPosition = -1;
//    }
//
//    public void stopShuffleMusic() {
//        playList.clear();
//        playList.addAll(initPlayList);
//        playListPosition = initPlayList.lastIndexOf(currentSong);
//        isShuffleMode = false;
//    }
//}
