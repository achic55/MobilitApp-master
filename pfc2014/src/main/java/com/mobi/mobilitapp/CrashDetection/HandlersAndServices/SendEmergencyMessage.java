package com.mobi.mobilitapp.CrashDetection.HandlersAndServices;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import android.widget.Toast;

import com.mobi.mobilitapp.CrashDetection.DifferentTypes.PreferencesTypes;
import com.mobi.mobilitapp.CrashDetection.Entities.EmergencyData;
import com.mobi.mobilitapp.CrashDetection.DifferentTypes.EmergencyTypes;
import com.mobi.mobilitapp.CrashDetection.Entities.IEmergencyData;
import com.mobi.mobilitapp.R;

import java.util.ArrayList;

/**
 * Created by toni_ on 31/05/2016.
 */

public class SendEmergencyMessage {

    private static final String MyTAG = "MyTAG";

    public static void sendMessageToTheServer(String time, String lat, String lon, String type, Context context){

        double latitude = Double.parseDouble(lat);
        double longitude = Double.parseDouble(lon);

        IEmergencyData IEmergencyData = new EmergencyData();
        IEmergencyData.setData(time, latitude,longitude, type);

        new EmergencyDataStorage(context).execute(IEmergencyData);
    }

    public static void sendSMS(String lat, String lon, String type, Context context, String phone, String medicalInfo){

        if(type.equalsIgnoreCase(EmergencyTypes.EmergencyButton.toString())){
            sendSmsEmergencyButton(lat, lon, context, phone, medicalInfo);
        }

        else if(type.equalsIgnoreCase(EmergencyTypes.EmergencyAccelerometer.toString())){
            sendSmsPossibleEmergency(lat,lon, context, phone, medicalInfo);
        }
    }

    private static void sendSmsEmergencyButton (String lat, String lon, Context context, String phone, String medicalInfo){

        if(!phone.equalsIgnoreCase(PreferencesTypes.no_phone.toString()) && phone.length()>0){
            String message = context.getString(R.string.alertMessageSMSButton);
            message = message +" "+ lat + ", " + lon;

            if(!medicalInfo.equalsIgnoreCase("") || !medicalInfo.isEmpty()){
                message = message + ". Medical Info: " + medicalInfo;
            }

            composeAndSendTheSms(message, phone, context);
        }
    }

    private static void sendSmsPossibleEmergency (String lat, String lon, Context context, String phone, String medicalInfo){

        if(!phone.equalsIgnoreCase(PreferencesTypes.no_phone.toString()) && phone.length()>0){
            String message = context.getString(R.string.possibleAlertMessageSMS);
            message = message +" "+ lat + ", " + lon;

            if(!medicalInfo.equalsIgnoreCase("") || !medicalInfo.isEmpty()){
                message = message + ". Medical Info: " + medicalInfo;
            }

            composeAndSendTheSms(message, phone, context);
        }
    }

    private static void composeAndSendTheSms(String message, String phone, Context context) {
        final String SENT_SMS_ACTION 			= 	"SENT_SMS_ACTION";
        final String DELIVERED_SMS_ACTION 		= 	"DELIVERED_SMS_ACTION";

        //Create the setIntent parameter
        Intent sentIntent = new Intent(SENT_SMS_ACTION);
        PendingIntent sentPI = PendingIntent.getBroadcast(context, 0, sentIntent, 0);
        //Create the deliveryIntent parameter
        Intent deliveryIntent = new Intent(DELIVERED_SMS_ACTION);
        PendingIntent deliverPI = PendingIntent.getBroadcast(context, 0, deliveryIntent, 0);

        SmsManager smsManager = SmsManager.getDefault();

        try{
            ArrayList<String> multipartSmsText = smsManager.divideMessage(message);
            int smsSize = multipartSmsText.size();

            //Create the arraylist PendingIntents for use it.
            ArrayList<PendingIntent> sentPiList = new ArrayList<PendingIntent>(smsSize);
            ArrayList<PendingIntent> deliverPiList = new ArrayList<PendingIntent>(smsSize);

            for (int i=0; i<smsSize; i++) {
                sentPiList.add(sentPI);
                deliverPiList.add(deliverPI);
            }

            //Try to send the sms message
            smsManager.sendMultipartTextMessage(phone, null, multipartSmsText, sentPiList, deliverPiList);
        }catch (Exception ex){
            Toast.makeText(context,"Error sending the SMS", Toast.LENGTH_SHORT).show();
        }
    }
}
