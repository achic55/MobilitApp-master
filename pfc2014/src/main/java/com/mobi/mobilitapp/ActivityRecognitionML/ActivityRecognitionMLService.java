package com.mobi.mobilitapp.ActivityRecognitionML;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.annotation.Nullable;
import android.util.Log;

import com.chaquo.python.PyException;
import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.mobi.mobilitapp.Capture.SensorSample;
import com.mobi.mobilitapp.MainActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.widget.Toast;

import static android.app.Activity.RESULT_OK;


public class ActivityRecognitionMLService extends IntentService {



    public static final String FILE_NAME = "modelV0.sav";
    private List<String> listDetectedActivityML = new ArrayList<>();
    private static final String TAG = "ActivityRecognitionML";

    public ActivityRecognitionMLService() {
        super("ActivityRecognitionMLService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.v(TAG , "onHandleIntent started");
        assert intent != null;
        //Bundle bListActivities = intent.getBundleExtra("listActivities");
        List<SensorSample> data = new ArrayList<>();
        List listActivities = Arrays.asList(new String[]{"Bicycle", "Bus", "Car", "Metro","Motorbike", "Run", "Stationary", "Train", "Tram", "Walk"});
        Float[][] matrix = (Float[][]) intent.getExtras().getSerializable("data_matrix");
        final ResultReceiver receiver = intent.getParcelableExtra("receiver");
        /* If the indent has an extra with data_matrix we apply the machine learning prediction*/
        if (intent.hasExtra("data_matrix") && !listActivities.isEmpty()){
            Log.v(TAG , "Some activity ready to analyse");
            setListDetectedActivityML(listActivities);
            DetectedActivityML detectedActivityML = getMostProbableActivity(matrix);
            Log.v(TAG , "Activity detected ML");


            //retrieves datas as string
            String whole_datas_str = detectedActivityML.getResultsResume();
            intent.putExtra("datas",whole_datas_str);
            MainActivity.datas_results = whole_datas_str;



            /*Bundle b = new Bundle();
            b.putString("activityML", detectedActivityML.getResultsResume() ); //displays results here
            receiver.send(1,b);
            */



        }
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        Log.v(TAG , "onDestroy");

    }
    private void sendBroadcastIntent(Intent intent, String activity){
        intent.putExtra("predictedActivity", activity);
        sendBroadcast(intent);
        Log.v(TAG , "sendBroadcastIntent");
    }

    public void setListDetectedActivityML(List<String> listDetectedActivityML) {
        this.listDetectedActivityML = listDetectedActivityML;
        Log.v(TAG , "setListDetectedActivityML");
    }

    /*Method to parse the output of the chaquopy sci kit learn predict*/
    private Map<String,Integer> calculateMapWithEstimations(String list){
        Map <String, Integer> hashMap = new HashMap<>();
        hashMap.put("Bicycle", 0);
        hashMap.put("Bus", 0);
        hashMap.put("Car", 0);
        hashMap.put("Metro", 0);
        hashMap.put("Motorbike", 0);
        hashMap.put("Run", 0);
        hashMap.put("Stationary", 0);
        hashMap.put("Train", 0);
        hashMap.put("Tram", 0);
        hashMap.put("Walk", 0);
        if(!list.isEmpty()){
            /*Regex to take only words on a String
            (ex: input: "['Car','Walk','Run' ]"
            out: [Car, Walk, Run])*/
            String[] words =list.split("[ !\"\\#$%&'()*+,-./:;<=>?@\\[\\]^_`{|}~]+");
            for (String word : words){
                if(hashMap.containsKey(word)){
                    Integer integer = hashMap.get(word) + 1 ;
                    hashMap.put(word, integer);
                }
            }

        }

        return hashMap;
    }

    public DetectedActivityML getMostProbableActivity(Float[][] matrix) {
        Log.v(TAG , "getMostProbableActivity init");
        DetectedActivityML detectedActivityML = new DetectedActivityML();
        //TODO: Use chaquopy to estimate the activity

        // "context" must be an Activity, Service or Application object from your app.
        String predictions = "";

        if (Python.isStarted()) {
            //First we get an Instance of python
            Python py = Python.getInstance();

            /*Then we get python modules to apply machine learning basically sci-kit and pickle ,
            * we recover the model on run time importing package1*/

            //Imports
            PyObject osPath = py.getModule("os.path");
            PyObject package1 = py.getModule("package1");
            PyObject pickle  = py.getModule("pickle");
            PyObject pyBuiltins = py.getBuiltins();
            PyObject numpy = py.getModule("numpy");
            /* The function to apply prediction requires a python object wih array-like shape
             * then we construct a matrix with numpy functions and with fromJava function from
               * Chaquopy API we make visible the object on python domain*/
            PyObject np = PyObject.fromJava(matrix);
            PyObject numpy_matrix = numpy.callAttr("matrix", np);
            PyObject package_name = osPath.callAttr("dirname" , package1.get("__file__"));
            PyObject joined_path = osPath.callAttr("join", package_name, FILE_NAME);


            /*Now we deserialize the model_file and if we haven't errors we apply the predictfunction*/
            try (PyObject model_file = pyBuiltins.callAttr("open", joined_path, "rb")) {
                PyObject model = pickle.callAttr("load", model_file);
                PyObject result_py = model.callAttr("predict",numpy_matrix.get("A").callAttr("tolist")); //here is the prediction thing
                predictions = numpy.callAttr("array2string", result_py).toString();
                detectedActivityML.setActivityEstimations(calculateMapWithEstimations(predictions));
                detectedActivityML.setNumberOfSamples(matrix.length);
                String result = detectedActivityML.getEstimationWithMapActivityEstimations();
                detectedActivityML.setActivityDescription(result);

                detectedActivityML.setResultsResume();








            }catch (PyException ex){
                detectedActivityML.setError("Unable to estimate the activity");
            }

        }
        Log.v(TAG , "getMostProbableActivity final");

        return detectedActivityML;
    }



}
