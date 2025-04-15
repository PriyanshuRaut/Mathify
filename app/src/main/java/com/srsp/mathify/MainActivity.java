package com.srsp.mathify;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.srsp.mathify.adapter.ChatAdapter;
import com.srsp.mathify.adapter.HistoryAdapter;
import com.srsp.mathify.model.ChatMessage;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import android.util.Base64;

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
    Vibrator vibrator;
    private final int REQ_CODE_SPEECH_INPUT = 100;
    private final int REQ_CODE_IMAGE_CAPTURE = 101;
    private final int REQ_CODE_GALLERY_PICK = 102;
    private static final int PERMISSION_REQUEST_CODE = 200;
    private ImageView imageAttachment;
    private ImageView removeAttachment;
    private View attachmentContainer;
    private Bitmap attachedBitmap = null;
    private boolean waitingForResponse = false;
    String IMGDB_API_KEY = "4ae96be64fc39a9eb2ac57422223064b";

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
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        attachmentContainer = findViewById(R.id.attachment_container);
        imageAttachment = findViewById(R.id.image_attachment);
        removeAttachment = findViewById(R.id.remove_attachment);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatAdapter = new ChatAdapter(chatList);
        recyclerView.setAdapter(chatAdapter);
        sharedPref = getSharedPreferences("chat_pref", Context.MODE_PRIVATE);
        prefEditor = sharedPref.edit();
        checkAndRequestPermissions();
        queryView.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE || (keyEvent != null && keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER && keyEvent.getAction() == KeyEvent.ACTION_DOWN)) {
                sendQuery();
                return true;
            }
            return false;
        });
        queryView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (queryView.getRight() - queryView.getCompoundDrawables()[2].getBounds().width())) {
                    sendQuery();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(45, VibrationEffect.DEFAULT_AMPLITUDE));
                    } else {
                        vibrator.vibrate(45);
                    }
                    return true;
                }
            }
            return false;
        });
        addButton.setOnClickListener(v -> {
            if (!extraButtonsVisible) {
                micButtonLayout.bringToFront();
                galleryButtonLayout.bringToFront();
                micButtonLayout.setAlpha(0f);
                micButtonLayout.setVisibility(View.VISIBLE);
                micButtonLayout.animate().alpha(1f).setDuration(300).setListener(null);
                galleryButtonLayout.setAlpha(0f);
                galleryButtonLayout.setVisibility(View.VISIBLE);
                galleryButtonLayout.animate().alpha(1f).setDuration(300).setListener(null);
                extraButtonsVisible = true;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(45, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    vibrator.vibrate(45);
                }
            } else {
                micButtonLayout.animate().alpha(0f).setDuration(300).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        micButtonLayout.setVisibility(View.GONE);
                    }
                });
                galleryButtonLayout.animate().alpha(0f).setDuration(300).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        galleryButtonLayout.setVisibility(View.GONE);
                    }
                });
                extraButtonsVisible = false;
            }
        });
        historyButton.setOnClickListener(view -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(45, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(45);
            }
            showHistoryDialog();
        });
        micButtonLayout.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(45, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(45);
            }
            startSpeechInput();
        });
        galleryButtonLayout.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(45, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(45);
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Select Option");
            String[] options = {"Take Photo", "Choose from Gallery"};
            builder.setItems(options, (dialog, which) -> {
                if (which == 0) {
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, REQ_CODE_IMAGE_CAPTURE);
                } else {
                    Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(galleryIntent, REQ_CODE_GALLERY_PICK);
                }
            });
            builder.show();
        });
        imageAttachment.setOnClickListener(v -> {
            if (attachedBitmap != null) {
                showImageFullScreen(attachedBitmap);
            }
        });
        removeAttachment.setOnClickListener(v -> {
            attachedBitmap = null;
            attachmentContainer.setVisibility(View.GONE);
        });
    }

    void sendQuery() {
        if (waitingForResponse) {
            Toast.makeText(MainActivity.this, "Waiting for AI response. Please wait...", Toast.LENGTH_SHORT).show();
            return;
        }
        String textQuery = queryView.getText().toString().trim();
        if (textQuery.isEmpty() && attachedBitmap == null) {
            return;
        }
        waitingForResponse = true;
        if (attachedBitmap != null) {
            chatList.add(new ChatMessage("human", textQuery, attachedBitmap));
        } else {
            chatList.add(new ChatMessage("human", textQuery));
        }
        chatAdapter.notifyItemInserted(chatList.size() - 1);
        saveHistory("Human: " + textQuery);
        queryView.setText("");
        if (attachedBitmap != null) {
            uploadImage(attachedBitmap, new UploadCallback() {
                @Override
                public void onSuccess(String imageUrl) {
                    callChatAPI(textQuery, imageUrl);
                }
                @Override
                public void onFailure(String error) {
                    waitingForResponse = false;
                    Toast.makeText(MainActivity.this, "Image upload failed: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            callChatAPI(textQuery, null);
        }
        attachedBitmap = null;
        attachmentContainer.setVisibility(View.GONE);
    }

    void saveHistory(String message) {
        String history = sharedPref.getString("history", "");
        history = history + message + "\n";
        prefEditor.putString("history", history);
        prefEditor.apply();
    }

    private String encodeImageToBase64(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 90, baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.NO_WRAP);
    }

    interface UploadCallback {
        void onSuccess(String imageUrl);
        void onFailure(String error);
    }

    void uploadImage(Bitmap image, UploadCallback callback) {
        String base64Image = encodeImageToBase64(image);
        MultipartBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("image", base64Image)
                .build();
        String uploadUrl = "https://api.imgbb.com/1/upload?key=" + IMGDB_API_KEY;
        Request request = new Request.Builder()
                .url(uploadUrl)
                .post(requestBody)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> callback.onFailure(e.getMessage()));
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String resStr = response.body().string();
                try {
                    JSONObject resJson = new JSONObject(resStr);
                    String imageUrl = resJson.getJSONObject("data").getString("url");
                    runOnUiThread(() -> callback.onSuccess(imageUrl));
                } catch (Exception e) {
                    runOnUiThread(() -> callback.onFailure(e.getMessage()));
                }
            }
        });
    }

    void callChatAPI(String query, String imageUrl) {
        try {
            JSONObject json = new JSONObject();
            json.put("model", "meta-llama/llama-4-scout-17b-16e-instruct");

            JSONArray messages = new JSONArray();
            JSONObject message = new JSONObject();
            message.put("role", "user");

            JSONArray contentArray = new JSONArray();
            JSONObject textObject = new JSONObject();
            textObject.put("type", "text");
            textObject.put("text", query);
            contentArray.put(textObject);

            if (imageUrl != null) {
                JSONObject imageObject = new JSONObject();
                imageObject.put("type", "image_url");
                JSONObject imageUrlObject = new JSONObject();
                imageUrlObject.put("url", imageUrl);
                imageObject.put("image_url", imageUrlObject);
                contentArray.put(imageObject);
            }
            message.put("content", contentArray);
            messages.put(message);
            json.put("messages", messages);

            json.put("temperature", 1);
            json.put("max_completion_tokens", 1024);
            json.put("top_p", 1);
            json.put("stream", false);
            json.put("stop", JSONObject.NULL);

            RequestBody body = RequestBody.create(JSON, json.toString());
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.e("ChatAPI", "Failure: " + e.getMessage());
                    Toast.makeText(MainActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    runOnUiThread(() -> waitingForResponse = false);
                }
                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    String resStr = response.body().string();
                    Log.d("ChatAPI", "Response Code: " + response.code() + " Body: " + resStr);
                    try {
                        JSONObject resJson = new JSONObject(resStr);
                        JSONArray choices = resJson.getJSONArray("choices");
                        JSONObject firstChoice = choices.getJSONObject(0);
                        JSONObject messageObj = firstChoice.getJSONObject("message");
                        String aiResponse = messageObj.getString("content");
                        runOnUiThread(() -> {
                            chatList.add(new ChatMessage("ai", aiResponse));
                            chatAdapter.notifyItemInserted(chatList.size() - 1);
                            recyclerView.scrollToPosition(chatList.size() - 1);
                            saveHistory("AI: " + aiResponse);
                            waitingForResponse = false;
                        });
                    } catch (Exception e) {
                        Log.e("ChatAPI", "Parsing error: " + e.getMessage());
//                        Toast.makeText(MainActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        runOnUiThread(() -> waitingForResponse = false);
                    }
                }
            });
        } catch (Exception e) {
            Log.e("ChatAPI", "Exception: " + e.getMessage());
//            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            waitingForResponse = false;
        }
    }

    void showHistoryDialog() {
        String historyData = sharedPref.getString("history", "");
        ArrayList<String> historyList = new ArrayList<>();
        if (!historyData.isEmpty()) {
            String[] items = historyData.split("\n");
            for (String item : items) {
                if (!item.trim().isEmpty()) {
                    historyList.add(item);
                }
            }
        }
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_history, null);
        RecyclerView historyListView = dialogView.findViewById(R.id.history_list_view);
        historyListView.setLayoutManager(new LinearLayoutManager(this));
        HistoryAdapter historyAdapter = new HistoryAdapter(this, historyList);
        historyListView.setAdapter(historyAdapter);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chat History");
        builder.setView(dialogView);
        builder.setPositiveButton("OK", (dialog, which) -> {});
        builder.create().show();
    }

    private void startSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak something...");
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (Exception e) {
            Toast.makeText(this, "Speech not supported on this device", Toast.LENGTH_SHORT).show();
        }
    }

    private void showImageFullScreen(Bitmap bitmap) {
        Dialog dialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_fullscreen_image);
        ImageView fullImageView = dialog.findViewById(R.id.full_image_view);
        fullImageView.setImageBitmap(bitmap);
        fullImageView.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CODE_SPEECH_INPUT && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            queryView.setText(result.get(0));
        } else if ((requestCode == REQ_CODE_IMAGE_CAPTURE || requestCode == REQ_CODE_GALLERY_PICK) && resultCode == RESULT_OK && data != null) {
            if (requestCode == REQ_CODE_IMAGE_CAPTURE) {
                attachedBitmap = (Bitmap) data.getExtras().get("data");
            } else if (requestCode == REQ_CODE_GALLERY_PICK) {
                try {
                    Uri imageUri = data.getData();
                    attachedBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                } catch (IOException e) {
                }
            }
            if (attachedBitmap != null) {
                imageAttachment.setImageBitmap(attachedBitmap);
                attachmentContainer.setVisibility(View.VISIBLE);
            }
        }
    }

    private void checkAndRequestPermissions() {
        String[] permissions = {
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
        };
        ArrayList<String> listPermissionsNeeded = new ArrayList<>();
        for (String perm : permissions) {
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(perm);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), PERMISSION_REQUEST_CODE);
        }
    }
}
