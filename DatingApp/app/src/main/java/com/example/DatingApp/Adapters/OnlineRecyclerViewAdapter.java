package com.example.DatingApp.Adapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.DatingApp.OwnProfileActivity;
import com.example.DatingApp.ProfileActivity;
import com.example.DatingApp.R;

import java.util.ArrayList;

public class OnlineRecyclerViewAdapter extends RecyclerView.Adapter<OnlineRecyclerViewAdapter.ViewHolder> {

    private static final String TAG = "RecyclerViewAdapter";

    private ArrayList<String> mImageNames = new ArrayList<>();
    private ArrayList<String> mImages = new ArrayList<>();
    private ArrayList<String> mDistances = new ArrayList<>();
    private Context mContext;

    public OnlineRecyclerViewAdapter(Context context, ArrayList<String> imageNames, ArrayList<String> images, ArrayList<String> distance) {
        mImageNames = imageNames;
        mImages = images;
        mContext = context;
        mDistances = distance;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view, parent, false);
        OnlineRecyclerViewAdapter.ViewHolder holder = new OnlineRecyclerViewAdapter.ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(OnlineRecyclerViewAdapter.ViewHolder holder, final int position) {
        Log.d(TAG, "onBindViewHolder: called.");

        holder.userName.setText(mImageNames.get(position));
        holder.image.setImageResource(R.drawable.ic_launcher_background);
        holder.distance.setText(mDistances.get(position));

        holder.parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: clicked on: ");
                Log.d(TAG, "onClick: clicked on: " + mImageNames.get(position));
                Intent intent;
                if(position == 0){
                    intent = new Intent(mContext, OwnProfileActivity.class);
                    mContext.startActivity(intent);
                }else {
                    intent = new Intent(mContext, ProfileActivity.class);
                    mContext.startActivity(intent);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mImageNames.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder{

        ImageView image;
        TextView userName;
        TextView distance;
        ConstraintLayout parentLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.newGuy);
            userName = itemView.findViewById(R.id.txtUser);
            distance = itemView.findViewById(R.id.txtDistance);
            parentLayout = itemView.findViewById(R.id.parentLayout);
        }
    }
}
