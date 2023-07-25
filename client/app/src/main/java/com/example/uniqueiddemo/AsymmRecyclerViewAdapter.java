package com.example.uniqueiddemo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class AsymmRecyclerViewAdapter extends RecyclerView.Adapter<AsymmRecyclerViewAdapter.MyViewHolder> {

    Context context;
    ArrayList<AsymmVerifiedMessage> verified;

    public AsymmRecyclerViewAdapter(Context context, ArrayList<AsymmVerifiedMessage> verified){
        this.context = context;
        this.verified = verified;
    }

    @NonNull
    @Override
    public AsymmRecyclerViewAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.verified_message_row,parent,false);
        return new AsymmRecyclerViewAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AsymmRecyclerViewAdapter.MyViewHolder holder, int position) {
        holder.message.setText(verified.get(position).getMessage());
        holder.hash.setText(verified.get(position).getHash());
        holder.check.setImageResource(R.drawable.baseline_verified_24);
    }

    @Override
    public int getItemCount() {
        return verified.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{

        private ImageView check;
        private TextView message, hash;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            check = itemView.findViewById(R.id.verified_icon);
            message = itemView.findViewById(R.id.message_verified);
            hash = itemView.findViewById(R.id.sha384);

        }
    }
}
