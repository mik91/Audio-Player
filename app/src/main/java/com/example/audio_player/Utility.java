package com.example.audio_player;

import android.app.DownloadManager;
import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.audio_player.Models.Audio;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static android.os.Environment.getExternalStorageDirectory;

class Utility {
    private static FirebaseStorage storage = FirebaseStorage.getInstance();
    private static StorageReference storageRef = storage.getReference();

    static void uploadFile(Uri uri, final Context context) {
        UploadTask uploadTask;

        StorageReference riversRef = storageRef.child(Objects.requireNonNull(uri.getLastPathSegment()));
        uploadTask = riversRef.putFile(uri);

        // Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(context,"Error in operation", Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(context,"Successful operation", Toast.LENGTH_SHORT).show();
            }
        });
    }

    static void download(final Context context) {
        StorageReference musicRef = storage.getReference();

        // Now we get the references of these music
        musicRef.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult result) {
                for(final StorageReference fileRef : result.getItems()) {
                    fileRef.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                        @Override
                        public void onSuccess(final StorageMetadata storageMetadata) {
                            fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    // Download url of file
                                    downloadFile(context, storageMetadata.getPath(), uri.toString());
                                    //getAllAudioFromDevice();
                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {

                        }
                    });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
            }
        });
    }
    private static void downloadFile(Context context, String path, String url) {

        DownloadManager downloadmanager = (DownloadManager) context.
                getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(url);
        DownloadManager.Request request = new DownloadManager.Request(uri);

        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        File sdCard = getExternalStorageDirectory();
        String folder = sdCard.getAbsolutePath() + "/Music-Player" ;
        File dir = new File(folder);
        if(dir.exists()) {
            dir.delete();
        }
        if(!dir.mkdir()) {
            boolean test = true;
        }
        request.setDestinationInExternalPublicDir("/Music-Player", path);
        downloadmanager.enqueue(request);
    }

    static ArrayList<Audio> getAllAudioFromDevice() {
        String path = null;
        File sdCardRoot = getExternalStorageDirectory();
        File dir = new File(sdCardRoot.getAbsolutePath() + "/Music-Player/");
        ArrayList<Audio> audioList = new ArrayList<>();

        if (dir.exists()) {

            if (dir.listFiles() != null) {
                for (File f : Objects.requireNonNull(dir.listFiles())) {
                    if (f.isFile())
                        path = f.getName();

                    if (Objects.requireNonNull(path).contains(".mp3"))
                        audioList.add(CreateAudioModel(path, dir.getPath()));
                }
            }
        }
        return audioList;
    }

    private static Audio CreateAudioModel(String path, String dirPath) {
        MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
        metadataRetriever.setDataSource(dirPath + "/" + path);

        String data = dirPath + "/" + path;
        String title = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        String album = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
        String artist = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);

        if (title == null) title = path;
        if (artist == null) artist = "Unknown artist";

        Audio audio = new Audio(data, title, album, artist);

        metadataRetriever.release();

        return audio;
    }
    static String getTimeString(long millis) {
        StringBuffer buf = new StringBuffer();

        int minutes = (int) ((millis % (1000 * 60 * 60)) / (1000 * 60));
        int seconds = (int) (((millis % (1000 * 60 * 60)) % (1000 * 60)) / 1000);

         buf.append(String.format("%02d", minutes))
            .append(":")
            .append(String.format("%02d", seconds));

        return buf.toString();
    }
}
