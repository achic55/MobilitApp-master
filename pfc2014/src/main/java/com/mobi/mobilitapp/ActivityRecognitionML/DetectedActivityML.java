package com.mobi.mobilitapp.ActivityRecognitionML;

import android.app.Fragment;

import com.github.mikephil.charting.charts.PieChart;

import java.util.Map;
import java.util.Set;

public class DetectedActivityML extends Fragment {
    public String activityDescription ;
    public Integer activityID;
    public float reliability;
    public Map<String, Integer> activityEstimations;
    public Integer numberOfSamples;
    public String resultsResume;
    public String error;

    public DetectedActivityML(String activityDescription, Integer activityID, float reliability,Map<String, Integer> activityEstimations) {
        this.activityDescription = activityDescription;
        this.activityID = activityID;
        this.reliability = reliability;
        this.activityEstimations = activityEstimations;
    }

    public void setNumberOfSamples(Integer numberOfSamples) {
        this.numberOfSamples = numberOfSamples;
    }

    public DetectedActivityML() {
    }

    public String getActivityDescription() {
        return activityDescription;
    }

    public void setActivityDescription(String activityDescription) {
        this.activityDescription = activityDescription;
    }

    public void setActivityEstimations(Map<String, Integer> activityEstimations) {
        this.activityEstimations = activityEstimations;
    }

    public void setError(String error) {
        this.error = error;
    }

    public void setResultsResume(){
        this.resultsResume = getActivityMLRecognitionPercentages();
    }

    public String getResultsResume(){
        return resultsResume;
    }

    public String getEstimationWithMapActivityEstimations(){
        String result= "";
        Integer max = 0;
        Set<String> setString = activityEstimations.keySet();
        for (String word : setString){
            Integer value = activityEstimations.get(word);
            if(value > max){
                result = word;
                max = value;
            }
        }
        return result;
    }









    public String getActivityMLRecognitionPercentages() {
        StringBuilder sb = new StringBuilder();
        Set<String> set = activityEstimations.keySet();


        for (String activity: set){
            sb.append(activity).append(" ").append(String.valueOf(activityEstimations.get(activity))).append(" ");
        }


        return sb.toString();

    }


}
