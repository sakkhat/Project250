package sakkhat.com.p250.jarvis;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

import ai.api.AIListener;
import ai.api.AIServiceException;
import ai.api.android.AIConfiguration;
import ai.api.android.AIService;
import ai.api.model.AIError;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.model.Result;
import sakkhat.com.p250.R;
import sakkhat.com.p250.helper.FragmentListener;

public class FragmentJarvis extends Fragment
        implements AIListener{
    private static final String TAG = "fragment_jarvis";

    /*
    * Fragment base
    * */
    private View root;
    private Context context;
    private FragmentListener fragmentListener;

    /*
    * AI Components
    * */
    private AIService aiService;
    private TextToSpeech tts;

    /*
    * UI Components
    * */
    private ImageView icJarvis, icSpeaker;
    private TextView textResult;
    private ImageView voiceInput;
    private EditText textInput;

    private boolean speaker;
    private Animation animation;

    /*
    * Data collections
    * */

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_jarvis,null, false);
        // instance of base context;
        context = getContext();

        speaker = true;

        initAI();
        initUI();
        return root;
    }

    public void setFragmentListener(FragmentListener fragmentListener){
        this.fragmentListener = fragmentListener;
    }

    private void initAI(){
        /*
        * instance and initialize of DialogFlow AI agent and Text-To-Speech API objects
        * */

        if(ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED)
             ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.RECORD_AUDIO},101);

        final AIConfiguration config=new AIConfiguration(Jarvis.TOKEN,
                AIConfiguration.SupportedLanguages.English,AIConfiguration.RecognitionEngine.System);
        aiService=AIService.getService(getActivity(),config);
        aiService.setListener(this);

        tts=new TextToSpeech(getActivity(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                tts.setLanguage(Locale.US);
            }
        });
    }

    private void initUI(){

        icJarvis = root.findViewById(R.id.frag_jarvis_icon);
        icJarvis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                icJarvis.startAnimation(animation);
            }
        });

        icSpeaker = root.findViewById(R.id.frag_jarvis_speaker);
        icSpeaker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(speaker){
                    speaker = false;
                    icSpeaker.setImageResource(R.drawable.ic_speaker_off);
                }
                else{
                    speaker = true;
                    icSpeaker.setImageResource(R.drawable.ic_speaker_on);
                }
            }
        });

        voiceInput = root.findViewById(R.id.frag_jarvis_voice_input);
        voiceInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                aiService.startListening();
                Toast.makeText(getActivity(),"start listening",Toast.LENGTH_SHORT).show();
            }
        });

        textInput = root.findViewById(R.id.frag_jarvis_text_input);
        textInput.setImeActionLabel("Ask", KeyEvent.KEYCODE_ENTER);
        textInput.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(keyCode == event.KEYCODE_ENTER){
                    if(textInput.getText().toString().isEmpty())
                        return true;
                    new TextTask().execute(textInput.getText().toString());
                    return true;
                }
                return false;
            }
        });

        textResult=root.findViewById(R.id.frag_jarvis_result);
        textResult.setText("Result");

        animation = AnimationUtils.loadAnimation(context, R.anim.jarvis);
        icJarvis.startAnimation(animation);
    }

    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults){

        if(requestCode==101)
        {
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
                ;
            else
                ;
        }
    }

    @Override
    public void onResult(AIResponse aiResult) {
        Result result = aiResult.getResult();
        switch (result.getAction().trim()){
            case Jarvis.Actions.JOKE:{
                textResult.setText(result.getFulfillment().getSpeech());
            } break;
            default:Jarvis.query(context, result, Jarvis.TAG);
        }
        if(speaker)
            tts.speak(result.getFulfillment().getSpeech(),TextToSpeech.QUEUE_FLUSH,null);

    }

    @Override
    public void onError(AIError error) {
        textResult.setText(error.toString());
    }

    @Override
    public void onAudioLevel(float level) {

    }

    @Override
    public void onListeningStarted() {
    }

    @Override
    public void onListeningCanceled() {
    }

    @Override
    public void onListeningFinished() {
    }

    @Override
    public void onPause() {
        if(tts!=null)
        {
            tts.stop();
            tts.shutdown();
        }
        super.onPause();
    }

    private class TextTask extends AsyncTask<String, Void, Void>{

        private AIResponse response;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            textResult.setEnabled(false);
            voiceInput.setEnabled(false);
            textInput.setVisibility(View.INVISIBLE);
            voiceInput.setVisibility(View.INVISIBLE);
        }

        @Override
        protected Void doInBackground(String... params) {
            try {
                response = aiService.textRequest(new AIRequest(params[0]));
            } catch (AIServiceException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            onResult(response);
            textInput.setText("");

            textResult.setEnabled(true);
            voiceInput.setEnabled(true);
            textInput.setVisibility(View.VISIBLE);
            voiceInput.setVisibility(View.VISIBLE);

        }
    }
}
