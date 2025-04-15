package com.srsp.mathify.adapter;

import android.content.Context;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.srsp.mathify.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private Context context;
    private ArrayList<Pair<String, String>> conversationPairs;

    public HistoryAdapter(Context context, ArrayList<String> historyItems) {
        this.context = context;
        conversationPairs = new ArrayList<>();
        for (int i = 0; i < historyItems.size(); i += 2) {
            String human = "";
            String ai = "";
            if (i < historyItems.size()) {
                human = historyItems.get(i);
                if (human.startsWith("Human:")) {
                    human = human.substring("Human:".length()).trim();
                }
            }
            if (i + 1 < historyItems.size()) {
                ai = historyItems.get(i + 1);
                if (ai.startsWith("AI:")) {
                    ai = ai.substring("AI:".length()).trim();
                }
            }
            conversationPairs.add(new Pair<>(human, ai));
        }
    }

    @NonNull
    @Override
    public HistoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_history_chat, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryAdapter.ViewHolder holder, int position) {
        Pair<String, String> pair = conversationPairs.get(position);
        holder.humanMsg.setText(pair.first);
        holder.aiMsg.setText(pair.second);
    }

    @Override
    public int getItemCount() {
        return conversationPairs.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView humanMsg, aiMsg;

        ViewHolder(View itemView) {
            super(itemView);
            humanMsg = itemView.findViewById(R.id.tv_human_msg);
            aiMsg = itemView.findViewById(R.id.tv_ai_msg);
        }
    }
}
