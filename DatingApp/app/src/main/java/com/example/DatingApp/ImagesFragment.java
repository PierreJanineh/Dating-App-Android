package com.example.DatingApp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class ImagesFragment extends Fragment {

    private ImageView imageView;
    private TextView textView;

    public ImagesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_images, container, false);
        imageView = view.findViewById(R.id.imgView);
        textView = view.findViewById(R.id.txtView);
        assert getArguments() != null;
        String pageNum = getArguments().getString("Message");
        int img = getArguments().getInt("Images");
        imageView.setImageResource(img);
        textView.setText(pageNum);
        return view;
    }
}
