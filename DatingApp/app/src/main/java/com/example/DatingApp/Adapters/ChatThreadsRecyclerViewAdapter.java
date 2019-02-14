package com.example.DatingApp.Adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.example.DatingApp.Chat.Message;
import com.example.DatingApp.R;

import java.util.Collections;
import java.util.List;

public class ChatThreadsRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
	private List<Message> messagesList;
	private Boolean isItMe;
	private int pos;
	
	
	public ChatThreadsRecyclerViewAdapter(List<Message> messagesList) {
		this.messagesList = messagesList;
		Collections.sort(messagesList);
	}
	
	@NonNull
	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
		isItMe = messagesList.get(pos).getIsItMe();
		if(isItMe){
			View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.my_chat, viewGroup, false);
			return new MyViewHolder(view);
		}else {
			View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.chat, viewGroup, false);
			return new ViewHolder(view);
		}
	}
	
	@Override
	public void onBindViewHolder(@NonNull RecyclerView.ViewHolder recyclerViewHolder, final int i) {
		pos = i+1;
		ViewHolder viewHolder;
		MyViewHolder myViewHolder;
		if(isItMe){
			myViewHolder = (MyViewHolder) recyclerViewHolder;
			myViewHolder.content.setText( messagesList.get(i).getContent() );
			myViewHolder.time.setText(messagesList.get(i).getTimestamp().toDate().toString());
			
			final View view = (View) myViewHolder.content.getParent();
			view.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
					Toast.makeText(view.getContext(), "Long pressed on "+ messagesList.get(i).getContent(), Toast.LENGTH_LONG).show();
					return true;
				}
			});
		}else{
			viewHolder = (ViewHolder) recyclerViewHolder;
			viewHolder.content.setText( messagesList.get(i).getContent() );
			viewHolder.time.setText(messagesList.get(i).getTimestamp().toDate().toString());
			
			final View view = (View) viewHolder.content.getParent();
			view.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
					Toast.makeText(view.getContext(), "Long pressed on "+ messagesList.get(i).getContent(), Toast.LENGTH_LONG).show();
					return true;
				}
			});
		}
	}
	
	@Override
	public int getItemCount() {
		return messagesList.size();
	}
	
	public class ViewHolder extends RecyclerView.ViewHolder {
		
		TextView content, time;
		
		public ViewHolder(View itemView) {
			super(itemView);
			content = itemView.findViewById(R.id.txtChat);
			time = itemView.findViewById(R.id.txtTime);
		}
	}
	public class MyViewHolder extends RecyclerView.ViewHolder {
		
		TextView content, time;
		
		public MyViewHolder(View itemView) {
			super(itemView);
			content = itemView.findViewById(R.id.txtChat);
			time = itemView.findViewById(R.id.txtTime);
		}
		
	}
	
	public void bubbleSort(Integer[] arr){
		boolean isSorted = false;
		int upTo = arr.length - 1;
		while (!isSorted){
			isSorted = true;
			for (int i = 0; i < upTo; i++) {
				if(arr[i] > arr[i+1]){
					int temp = arr[i];
					arr[i] = arr[i+1];
					arr[i+1] = temp;
					isSorted = false;
				}
			}
			upTo--;
		}
	}
	
}
