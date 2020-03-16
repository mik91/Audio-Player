package com.example.audio_player.Fragements;

import android.content.Context;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.audio_player.Models.Audio;
import com.example.audio_player.R;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class AudioListFragment extends Fragment {

    private static final String ARG_LST_AUDIO = "lesAudio";
    private List<Audio> AudioList;
    private OnListFragmentInteractionListener mListener;
    private RecyclerView recyclerView;
    public AudioListFragment() {
    }

    @SuppressWarnings("unused")
    public static AudioListFragment newInstance(List<Audio> audioList) {
        AudioListFragment fragment = new AudioListFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_LST_AUDIO, (Serializable) audioList);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            AudioList =  (List<Audio>) getArguments().getSerializable(ARG_LST_AUDIO);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_audio_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
           recyclerView = (RecyclerView) view;
           recyclerView.setAdapter(new AudioRecyclerViewAdapter(AudioList, mListener));
           recyclerView.addItemDecoration(
                   new DividerItemDecoration(ContextCompat.getDrawable(Objects.requireNonNull(getContext()),
                           R.drawable.recycler_horizontal_divider)));
        }
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }
    public void changeMusicColor(int position) {
        AudioRecyclerViewAdapter audioRecyclerViewAdapter = (AudioRecyclerViewAdapter) recyclerView.getAdapter();
        assert audioRecyclerViewAdapter != null;
        audioRecyclerViewAdapter.changeTextMusicColor(position);
    }
    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(Audio item);
    }
}
