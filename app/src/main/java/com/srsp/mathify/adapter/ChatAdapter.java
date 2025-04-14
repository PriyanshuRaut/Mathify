package com.srsp.mathify.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.srsp.mathify.R;
import com.srsp.mathify.model.ChatMessage;
import java.util.ArrayList;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    ArrayList<ChatMessage> messages;
    final int HUMAN = 0;
    final int AI = 1;

    public ChatAdapter(ArrayList<ChatMessage> messages) {
        this.messages = messages;
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).type.equals("human") ? HUMAN : AI;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == HUMAN) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_list_human, parent, false);
            return new HumanViewHolder(view);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_list_ai, parent, false);
            return new AIViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        if (holder instanceof HumanViewHolder) {
            HumanViewHolder humanHolder = (HumanViewHolder) holder;
            humanHolder.text.setText(message.message);
            if (message.image != null) {
                humanHolder.image.setVisibility(View.VISIBLE);
                humanHolder.image.setImageBitmap(message.image);
            } else {
                humanHolder.image.setVisibility(View.GONE);
            }
        } else {
            ((AIViewHolder) holder).text.setText(message.message);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public class HumanViewHolder extends RecyclerView.ViewHolder {
        TextView text;
        ImageView image;

        public HumanViewHolder(@NonNull View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.your_text);
            image = itemView.findViewById(R.id.attached_image);
        }
    }

    public class AIViewHolder extends RecyclerView.ViewHolder {
        TextView text;

        public AIViewHolder(@NonNull View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.ai_text);
        }
    }
}
