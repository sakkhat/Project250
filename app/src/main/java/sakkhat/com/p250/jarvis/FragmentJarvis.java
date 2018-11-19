package sakkhat.com.p250.jarvis;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonElement;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;

import ai.api.AIListener;
import ai.api.android.AIConfiguration;
import ai.api.android.AIService;
import ai.api.model.AIError;
import ai.api.model.AIResponse;
import ai.api.model.ResponseMessage;
import ai.api.model.Result;
import sakkhat.com.p250.Home;
import sakkhat.com.p250.R;
import sakkhat.com.p250.helper.FragmentListener;

public class FragmentJarvis extends Fragment
        implements AIListener{

    private static final String TOKEN = "c93846a85d5044d09e0db0efc99108ff";
    private static final String TAG = "fragment_jarvis";

    private View root;
    private Context context;
    private Home home=new Home();
    // AI components


    private AIService aiService;
    private FragmentListener fragmentListener;
    private TextToSpeech tts;

    private Button button;
    private TextView textResult;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_jarvis,null, false);
        // instance of base context;
        context = getContext();

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

        if(ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED)
             ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.RECORD_AUDIO},101);

        final AIConfiguration config=new AIConfiguration(TOKEN,
                AIConfiguration.SupportedLanguages.English,AIConfiguration.RecognitionEngine.System);
        aiService=AIService.getService(getActivity(),config);
        aiService.setListener(this);

        button=(Button)(Button) root.findViewById(R.id.frag_jarvis_icon);;
        textResult=(TextView)root.findViewById(R.id.frag_jarvis_result);
        textResult.setText("Result");


        tts=new TextToSpeech(getActivity(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                tts.setLanguage(Locale.US);
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                aiService.startListening();
                Toast.makeText(getActivity(),"start listening",Toast.LENGTH_SHORT).show();
            }
        });


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
    public void onResult(AIResponse result1) {
        Result result = result1.getResult();

        String parameterString = "";
        String app="";
        if (result.getParameters() != null && !result.getParameters().isEmpty()) {
            for (final Map.Entry<String, JsonElement> entry : result.getParameters().entrySet()) {
                if(entry.getKey().contains("app_name"))
                    app=entry.getValue().toString();
                parameterString += "(" + entry.getKey() + ", " + entry.getValue() + ") ";
            }
        }
        textResult.setText(result.getAction()+" rest : "+result.getResolvedQuery()+" : "+result.getFulfillment().getSpeech());

        if(result.getAction().equals("app_switch"))
        {
            app=app.substring(1,app.length()-1);
            List<PackageInfo>pack=getActivity().getPackageManager().getInstalledPackages(0);
            for(PackageInfo p : pack)
            {
                String app_name=p.applicationInfo.loadLabel(getActivity().getPackageManager()).toString();
                String pkg=p.packageName;
                Toast.makeText(getActivity(),app_name+" "+pkg,Toast.LENGTH_SHORT).show();
                if(app_name.compareToIgnoreCase(app)==0)
                {
                    Intent intent=new Intent();
                    intent.setPackage(pkg);
                    startActivity(intent);
                    break;
                }
            }
        }
        else if(result.getAction().equals("joke"))
        {
            tts.speak(result.getFulfillment().getSpeech(),TextToSpeech.QUEUE_FLUSH,null);
        }

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
}
