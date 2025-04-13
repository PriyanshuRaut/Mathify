package com.srsp.mathify;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.srsp.mathify.adapter.ChatAdapter;
import com.srsp.mathify.model.ChatMessage;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    EditText queryView;
    ImageView addButton;
    View micButtonLayout, galleryButtonLayout, historyButton;
    RecyclerView recyclerView;
    ChatAdapter chatAdapter;
    ArrayList<ChatMessage> chatList = new ArrayList<>();
    OkHttpClient client = new OkHttpClient();
    final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    String apiKey = "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";
    String url = "https://api.groq.com/openai/v1/chat/completions";
    SharedPreferences sharedPref;
    SharedPreferences.Editor prefEditor;
    boolean extraButtonsVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        queryView = findViewById(R.id.query_view);
        addButton = findViewById(R.id.add_button);
        micButtonLayout = findViewById(R.id.mic_button_layout);
        galleryButtonLayout = findViewById(R.id.gallery_button_layout);
        historyButton = findViewById(R.id.history_layout);
        recyclerView = findViewById(R.id.recycler_view);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatAdapter = new ChatAdapter(chatList);
        recyclerView.setAdapter(chatAdapter);

        sharedPref = getSharedPreferences("chat_pref", Context.MODE_PRIVATE);
        prefEditor = sharedPref.edit();

        queryView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_DONE ||
                        (keyEvent != null && keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER
                                && keyEvent.getAction() == KeyEvent.ACTION_DOWN)) {
                    String query = queryView.getText().toString().trim();
                    if (!query.isEmpty()) {
                        chatList.add(new ChatMessage("human", query));
                        chatAdapter.notifyItemInserted(chatList.size() - 1);
                        saveHistory("Human: " + query);
                        callChatAPI(query);
                        queryView.setText("");
                    }
                    return true;
                }
                return false;
            }
        });

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!extraButtonsVisible) {
                    micButtonLayout.setAlpha(0f);
                    micButtonLayout.setVisibility(View.VISIBLE);
                    micButtonLayout.animate().alpha(1f).setDuration(300).setListener(null);
                    galleryButtonLayout.setAlpha(0f);
                    galleryButtonLayout.setVisibility(View.VISIBLE);
                    galleryButtonLayout.animate().alpha(1f).setDuration(300).setListener(null);
                    extraButtonsVisible = true;
                } else {
                    micButtonLayout.animate().alpha(0f).setDuration(300)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    micButtonLayout.setVisibility(View.GONE);
                                }
                            });
                    galleryButtonLayout.animate().alpha(0f).setDuration(300)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    galleryButtonLayout.setVisibility(View.GONE);
                                }
                            });
                    extraButtonsVisible = false;
                }
            }
        });

        historyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String history = sharedPref.getString("history", "");
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Chat History");
                builder.setMessage(history.isEmpty() ? "No history available" : history);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) { }
                });
                builder.create().show();
            }
        });
    }

    void saveHistory(String message) {
        String history = sharedPref.getString("history", "");
        history = history + message + "\n";
        prefEditor.putString("history", history);
        prefEditor.apply();
    }

    void callChatAPI(String query) {
        try {
            JSONObject json = new JSONObject();
            json.put("model", "llama-3.3-70b-versatile");
            JSONArray messages = new JSONArray();
            JSONObject message = new JSONObject();
            message.put("role", "user");
            message.put("content", query);
            messages.put(message);
            json.put("messages", messages);
            RequestBody body = RequestBody.create(JSON, json.toString());
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .post(body)
                    .build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) { }
                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    String resStr = response.body().string();
                    try {
                        JSONObject resJson = new JSONObject(resStr);
                        String aiResponse = resJson.getString("response");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                chatList.add(new ChatMessage("ai", aiResponse));
                                chatAdapter.notifyItemInserted(chatList.size() - 1);
                                recyclerView.scrollToPosition(chatList.size() - 1);
                                saveHistory("AI: " + aiResponse);
                            }
                        });
                    } catch (Exception e) { }
                }
            });
        } catch (Exception e) { }
    }
}
