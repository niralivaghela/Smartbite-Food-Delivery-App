package com.smartbite.utils;
import android.app.Activity;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import java.util.ArrayList;
import java.util.Locale;
public class VoiceSearchManager {
    private Activity activity;
    private static final int VOICE_REQUEST_CODE=999;
    public interface VoiceResultCallback{
        void onResult(String query);
        void onError(String error);
        void onListening();
    }
    private VoiceResultCallback callback;
    public VoiceSearchManager(Activity activity){this.activity=activity;}
    public void startListening(VoiceResultCallback callback){
        this.callback=callback;
        if(!SpeechRecognizer.isRecognitionAvailable(activity)){
            callback.onError("Voice recognition not available"); return;
        }
        Intent intent=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,"Say what food you want...");
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,1);
        activity.startActivityForResult(intent,VOICE_REQUEST_CODE);
        callback.onListening();
    }
    public void handleResult(int requestCode,int resultCode,Intent data){
        if(requestCode==VOICE_REQUEST_CODE&&resultCode==Activity.RESULT_OK&&data!=null){
            ArrayList<String> results=data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if(results!=null&&!results.isEmpty()) processVoiceCommand(results.get(0));
        }
    }
    private void processVoiceCommand(String query){
        String lower=query.toLowerCase();
        if(lower.contains("order")||lower.contains("want")||lower.contains("get"))
            callback.onResult("ORDER:"+extractFoodItem(lower));
        else if(lower.contains("search")||lower.contains("find")||lower.contains("show"))
            callback.onResult("SEARCH:"+extractFoodItem(lower));
        else callback.onResult("SEARCH:"+query);
    }
    private String extractFoodItem(String query){
        return query.replace("order","").replace("want","").replace("get me","")
            .replace("search for","").replace("find","").replace("show me","").trim();
    }
    public static int getVoiceRequestCode(){return VOICE_REQUEST_CODE;}
}