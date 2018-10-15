package sakkhat.com.p250.jarvis;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.JsonElement;

import java.util.HashMap;
import java.util.Map;

import ai.api.AIListener;
import ai.api.android.AIConfiguration;
import ai.api.android.AIService;
import ai.api.model.AIError;
import ai.api.model.AIResponse;
import ai.api.model.ResponseMessage;
import ai.api.model.Result;
import sakkhat.com.p250.R;
import sakkhat.com.p250.helper.FragmentListener;

public class FragmentJarvis extends Fragment
        implements AIListener{

    private static final String TOKEN = "c93846a85d5044d09e0db0efc99108ff";
    private static final String TAG = "fragment_jarvis";

    private View root;
    private Context context;

    // AI components
    private AIService aiService;

    private FragmentListener fragmentListener;

    private ImageView imgJarvis;
    private TextView textResult;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_jarvis,null, false);
        init();
        return root;
    }

    public void setFragmentListener(FragmentListener fragmentListener){
        this.fragmentListener = fragmentListener;
    }

    private void init(){
        /*
        * instance and initialize
        * */

        // instance of base context;
        context = getContext();

        final AIConfiguration config = new AIConfiguration(TOKEN,AIConfiguration.
                SupportedLanguages.English,AIConfiguration.RecognitionEngine.System);
        aiService = AIService.getService(context,config);
        aiService.setListener(this);

        imgJarvis = (ImageView) root.findViewById(R.id.frag_jarvis_icon);
        imgJarvis.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                aiService.startListening();
                return true;
            }
        });

        textResult = (TextView)root.findViewById(R.id.frag_jarvis_result);
        textResult.setText("Result");
    }

    /*
    * AI Listener interface implementation
    * */
    @Override
    public void onResult(AIResponse response) {
        Result result = response.getResult();

        // action
        String action = result.getAction();
        String query = result.getResolvedQuery();
        final HashMap<String, JsonElement> params = result.getParameters();
        for(Map.Entry<String, JsonElement> entry : params.entrySet()){
            textResult.append("\n"+entry.getKey()+" : "+entry.getValue());
            Log.i(TAG,entry.getKey()+" : "+entry.getValue());
        }
        Log.d(TAG,action);
        Log.d(TAG, query);

        textResult.append("\nYou: "+query);
        textResult.append("\nJarvis: "+result.getFulfillment().getSpeech());
        Log.w(TAG,result.getFulfillment().getSpeech());

        try{
            for(ResponseMessage mss :result.getFulfillment().getMessages()){
                Log.w(TAG, mss.getClass().getName());
            }
        } catch (Exception ex){
            Log.e(TAG, ex.getMessage());
        }
    }

    @Override
    public void onError(AIError error) {
        Log.e(TAG, error.getMessage());
    }

    @Override
    public void onAudioLevel(float level) {
        //Log.d(TAG, Float.toString(level));
    }

    @Override
    public void onListeningStarted() {
        Log.d(TAG,"listening started");
    }

    @Override
    public void onListeningCanceled() {
        Log.w(TAG,"listening canceled");
    }

    @Override
    public void onListeningFinished() {
        Log.d(TAG, "listening finished");
    }

}
