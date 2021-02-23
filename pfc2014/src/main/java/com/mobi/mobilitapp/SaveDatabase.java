package com.mobi.mobilitapp;

import android.os.AsyncTask;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.List;
import java.util.ArrayList;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;

import android.content.SharedPreferences;
import android.content.Context;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;

import android.util.Log;


public class SaveDatabase extends AsyncTask<Void, Void, String> {
	
	double inicioLat, inicioLon, finLat, finLon;
    boolean guardausuario;
    boolean guardarRegid = false;
	String comments;
	private String result = "OK";
	Runnable runn;
	
	InputStream raw;
	String filename, hashUsuario, ruta, nombreUsuario,apellidoUsuario,pesoUsuario,nacimientoUsuario,
            rutaPkey,mailUsuario,generoUsuario,dateFile,regid;
	SharedPreferences prefsGoogle, prefsFace, first, prefs;
    SharedPreferences.Editor editor;
    Context mContext;
    int  contadorOkLocations=0;



	/*public SaveDatabase(double inicioLat, double inicioLon,double finLat, double finLon, String comments) {
		
		this.inicioLat = inicioLat;
		this.inicioLon = inicioLon;
		this.comments = comments;
		this.finLat = finLat;
		this.finLon = finLon;		
	}*/

	public SaveDatabase(String hashUsuario, String filename, String ruta, String nombreUsuario, String apellidoUsuario,String pesoUsuario,String nacimientoUsuario, String mailUsuario,String generoUsuario , String dateFile, Context context){
	//leer de Json y guardar en database
    //11 parameters

		this.filename = filename;
		this.hashUsuario = hashUsuario;
		this.nombreUsuario = nombreUsuario;
		this.apellidoUsuario = apellidoUsuario;
		this.pesoUsuario = pesoUsuario;
		this.nacimientoUsuario = nacimientoUsuario;
        this.mailUsuario = mailUsuario;
        this.generoUsuario = generoUsuario;
		this.ruta = ruta;
		this.rutaPkey = ruta +"/certs/public.der";
        this.dateFile = dateFile;
        this.mContext = context;
        this.prefs = context.getSharedPreferences("prefs",Context.MODE_PRIVATE);
        this.editor = prefs.edit();


	}

    public SaveDatabase(String ruta, String hashUsuario, String nombreUsuario, String apellidoUsuario,String pesoUsuario,String nacimientoUsuario, String mailUsuario,String generoUsuario,boolean guardausuario){
    //9 parameters

        this.hashUsuario = hashUsuario;
        this.nombreUsuario = nombreUsuario;
        this.apellidoUsuario = apellidoUsuario;
        this.pesoUsuario = pesoUsuario;
        this.nacimientoUsuario = nacimientoUsuario;
        this.mailUsuario = mailUsuario;
        this.generoUsuario = generoUsuario;
        this.guardausuario = guardausuario;
        this.ruta = ruta;
        this.rutaPkey = ruta +"/certs/public.der";


    }

    public SaveDatabase(String ruta,String regid, String hashUsuario, Context context){
    //4 parameters

        this.ruta = ruta;
        this.rutaPkey = ruta +"/certs/public.der";
        this.hashUsuario = hashUsuario;
        this.mContext = context;
        this.regid = regid;
        guardarRegid = true;
        this.prefs = context.getSharedPreferences("com.mobi.mobilitapp",Context.MODE_PRIVATE);

    }
	
	@Override
	protected String doInBackground(Void...parameters) {
        //Download PublicKey from server and save it in "public.der" file
        downloaFilefromUrl(ruta + "/certs", "http://mobilitat.upc.edu/certs/public.der", "public.der");

        JSONParser jsonParser = new JSONParser();
        String url_create_segment = "http://mobilitat.upc.edu/mysqlQueries/insert_segment2.php";
        String url_create_usuario = "http://mobilitat.upc.edu/mysqlQueries/insert_usuario.php";
        String url_create_location = "http://mobilitat.upc.edu/mysqlQueries/insert_location2.php";
        String url_create_regid = "http://mobilitat.upc.edu/mysqlQueries/insert_regid.php";
        String url_update_segment = "http://mobilitat.upc.edu/mysqlQueries/update_segment.php";
        String TAG_SUCCESS = "success";

        //Open PublicKey given by the server and code a message with it

        //Save the PublicKey in pubKeyFile
        //When we have downloaded it above these lines, we have saved the PublicKey in the route described by rutaPkey
        File pubKeyFile = new File(rutaPkey);
        DataInputStream dis;
        byte[] encrypted;

        if (guardarRegid){

                try{

                //crea objeto cipher con la clave publica del servidor
                String reg_id = prefs.getString("registration_id","0");

                    dis = new DataInputStream(new FileInputStream(pubKeyFile)); // FileInputStream in = new FileInputStream(pubKeyFile), first part is not necessary in this case
                    byte[] keyBytes = new byte[(int) pubKeyFile.length()]; // create a buffer to store the data on it
                    dis.readFully(keyBytes); // read all data and store it in the buffer
                    dis.close();

                    //Creates a new X509EncodedKeySpec with the specified encoded key bytes.
                    X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
                    //Returns a new instance(new object) of KeyFactory that utilizes the specified algorithm
                    KeyFactory keyFactory;
                    keyFactory = KeyFactory.getInstance("RSA");
                    //Cipher´s Instance with "RSA/ECB/PKCS1PADDING" transformation
                    Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1PADDING");
                    //Here we obtain the publicKey
                    RSAPublicKey publicKey = (RSAPublicKey) keyFactory.generatePublic(keySpec);
                    //Cretaes the cipher object with the specified publickKey
                    cipher.init(Cipher.ENCRYPT_MODE, publicKey);

                    //encode hashUsuario and regid
                    String usu_hash_enc = bytesToHex(cipher.doFinal(hashUsuario.toString().getBytes("UTF-8")));
                    String reg_id_enc = bytesToHex(cipher.doFinal(reg_id.toString().getBytes("UTF-8")));
                    //String reg_id_enc = bytesToHex(cipher.doFinal(regid.toString().getBytes("UTF-8")));

                List<NameValuePair> params = new ArrayList<NameValuePair>();

                params.add(new BasicNameValuePair("usu_hash_enc", usu_hash_enc));
                params.add(new BasicNameValuePair("reg_id_enc", reg_id_enc));

                JSONObject json = jsonParser.makeHttpRequest(url_create_regid, "POST", params);

                int success = 0;

                    success = json.getInt(TAG_SUCCESS);


                if (success == 1) {
                    Log.v("BBDD", "usuario insertado correctamente");
                    result = "OK";
                    return result;
                } else {
                    Log.v("BBDD", "fail insert usuario");
                    result = "KO";
                    return result;
                }

                } catch (JSONException e) {
                        e.printStackTrace();
                        result = "KO";
                        return result;

                } catch (NoSuchPaddingException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InvalidKeyException e) {
                    e.printStackTrace();
                } catch (InvalidKeySpecException e) {
                    e.printStackTrace();
                } catch (BadPaddingException e) {
                    e.printStackTrace();
                } catch (IllegalBlockSizeException e) {
                    e.printStackTrace();
                }
                catch (Exception e) {
                    Log.v("BBDD", "Error doInBackground");
                }

        }

        else {


            if (guardausuario) {
                //guardar usuario
                try {
                    //crea objeto cipher con la clave publica del servidor
                    //Same steps as before
                    dis = new DataInputStream(new FileInputStream(pubKeyFile));
                    byte[] keyBytes = new byte[(int) pubKeyFile.length()];
                    dis.readFully(keyBytes);
                    dis.close();
                    X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
                    KeyFactory keyFactory;
                    keyFactory = KeyFactory.getInstance("RSA");
                    Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1PADDING");
                    RSAPublicKey publicKey = (RSAPublicKey) keyFactory.generatePublic(keySpec);
                    cipher.init(Cipher.ENCRYPT_MODE, publicKey);

                    //guarda en las variables_enc los string codificados con la clave publica
                    String usu_hash_enc = bytesToHex(cipher.doFinal(hashUsuario.toString().getBytes("UTF-8")));
                    String usu_nombre_enc = bytesToHex(cipher.doFinal(nombreUsuario.toString().getBytes("UTF-8")));
                    String usu_apellido_enc = bytesToHex(cipher.doFinal(apellidoUsuario.toString().getBytes("UTF-8")));
                    String usu_peso_enc = bytesToHex(cipher.doFinal(pesoUsuario.toString().getBytes("UTF-8")));
                    String usu_nacimiento_enc = bytesToHex(cipher.doFinal(nacimientoUsuario.toString().getBytes("UTF-8")));
                    String usu_mail_enc = bytesToHex(cipher.doFinal(mailUsuario.toString().getBytes("UTF-8")));
                    String usu_genero_enc = bytesToHex(cipher.doFinal(generoUsuario.toString().getBytes("UTF-8")));

                    List<NameValuePair> params = new ArrayList<NameValuePair>();

                    params.add(new BasicNameValuePair("usu_hash_enc", usu_hash_enc));
                    params.add(new BasicNameValuePair("usu_nombre_enc", usu_nombre_enc));
                    params.add(new BasicNameValuePair("usu_apellido_enc", usu_apellido_enc));
                    params.add(new BasicNameValuePair("usu_peso_enc", usu_peso_enc));
                    params.add(new BasicNameValuePair("usu_nacimiento_enc", usu_nacimiento_enc));
                    params.add(new BasicNameValuePair("usu_mail_enc", usu_mail_enc));
                    params.add(new BasicNameValuePair("usu_genero_enc", usu_genero_enc));

                    JSONObject json = jsonParser.makeHttpRequest(url_create_usuario, "POST", params);
                    //subida segmento a base de datos

                    try {
                        int success = 0;
                        success = json.getInt(TAG_SUCCESS);

                        if (success == 1) {
                            Log.v("BBDD", "usuario insertado correctamente");
                            result = "OK";
                            return result;
                        } else {
                            Log.v("BBDD", "fail insert usuario");
                            result = "KO";
                            return result;

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        result = "KO";
                        return result;
                    }


                } catch (NoSuchPaddingException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InvalidKeyException e) {
                    e.printStackTrace();
                } catch (InvalidKeySpecException e) {
                    e.printStackTrace();
                } catch (BadPaddingException e) {
                    e.printStackTrace();
                } catch (IllegalBlockSizeException e) {
                    e.printStackTrace();
                }
            }

            else {
                //guardar segmento

                //Creates a string with the content of the filename
                String input = readJson();

                try {

                    //crea objeto cipher con la clave publica del servidor
                    //Same steps as before
                    dis = new DataInputStream(new FileInputStream(pubKeyFile));
                    byte[] keyBytes = new byte[(int) pubKeyFile.length()];
                    dis.readFully(keyBytes);
                    dis.close();
                    X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
                    KeyFactory keyFactory;
                    keyFactory = KeyFactory.getInstance("RSA");
                    Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1PADDING");
                    RSAPublicKey publicKey = (RSAPublicKey) keyFactory.generatePublic(keySpec);
                    cipher.init(Cipher.ENCRYPT_MODE, publicKey);


                    int segmentsInserted=0;


                    //recorre el json y guarda los valores en las variables
                    //creamos un JSON con los valores de la sting input
                    JSONObject jObj = new JSONObject(input);
                    //Devolvemos la array segments
                    JSONArray segments = jObj.getJSONArray("segments");
                    Log.v("bbdd", "segment length: " + segments.length() + "");

                    for (int i = 0; i < segments.length(); i++) {

                    //Devolvemos cada objecto que encontramos en la array de segmentos
                        JSONObject c = segments.getJSONObject(i);
                        Log.v("bbdd", "segment #: " + i + "");

               /* String activity = c.getString("activity");
                String distance = c.getString("distance (m)");
                String duration = c.getString("duration (s)");
                String speed = c.getString("speed (Km/h)");
                String firsttime = c.getString("first time");
                String lasttime = c.getString("last time");
                */

                        //guarda en las variables_enc los string codificados con la clave publica
                        String usu_hash_enc = bytesToHex(cipher.doFinal(hashUsuario.toString().getBytes("UTF-8")));
                        String activity_enc = bytesToHex(cipher.doFinal(c.getString("activity").getBytes("UTF-8")));
                        String distance_enc = bytesToHex(cipher.doFinal(c.getString("distance (m)").getBytes("UTF-8")));
                        String duration_enc = bytesToHex(cipher.doFinal(c.getString("duration (s)").getBytes("UTF-8")));
                        String speed_enc = bytesToHex(cipher.doFinal(c.getString("speed (Km/h)").getBytes("UTF-8")));
                        String firsttime_enc = bytesToHex(cipher.doFinal(c.getString("first time").getBytes("UTF-8")));
                        String lasttime_enc = bytesToHex(cipher.doFinal(c.getString("last time").getBytes("UTF-8")));

                      //  String cellpower_enc = bytesToHex(cipher.doFinal(c.getString("cellPower").getBytes("UTF-8")));
                       // String wifipower_enc = bytesToHex(cipher.doFinal(c.getString("wifiPower").getBytes("UTF-8")));


                        //guardar parametros de segment en un array
                        List<NameValuePair> params = new ArrayList<NameValuePair>();

                        params.add(new BasicNameValuePair("seg_activity_enc", activity_enc));
                        params.add(new BasicNameValuePair("seg_distance_enc", distance_enc));
                        params.add(new BasicNameValuePair("seg_duration_enc", duration_enc));
                        params.add(new BasicNameValuePair("seg_speed_enc", speed_enc));
                        params.add(new BasicNameValuePair("seg_firsttime_enc", firsttime_enc));
                        params.add(new BasicNameValuePair("seg_lasttime_enc", lasttime_enc));
                        params.add(new BasicNameValuePair("usu_hash_enc", usu_hash_enc));
                     //   params.add(new BasicNameValuePair("cellpower_enc", cellpower_enc));
                      //  params.add(new BasicNameValuePair("wifipower_enc", wifipower_enc));


                        JSONObject json = jsonParser.makeHttpRequest(url_create_segment, "POST", params);
                        //subida segmento a base de datos

                        try {
                            int success = 0;
                            String exists = "";
                            success = json.getInt(TAG_SUCCESS);
                            exists = json.getString("existe");

                            if (success == 1) {
                                Log.v("BBDD", "Existe: " + exists + "");
                                Log.v("BBDD", "segmento insertado correctamente");
                                result = "OK";
                                segmentsInserted++;
                                Log.v("bbdd", "Segments inserted counter: "+segmentsInserted);

                            } else {
                                Log.v("BBDD", "fail insert segmento");
                                result = "KO";
                                Log.v("BBDD", "Existe: " + exists + "");
                                if (exists.equalsIgnoreCase("Si")){
                                    segmentsInserted++;
                                    Log.v("bbdd", "Segments inserted counter (existe): "+segmentsInserted);
                                }

                                if (segmentsInserted == segments.length()) {

                                    String lastupload = AppUtils.getDay2(System.currentTimeMillis());
                                    DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd");
                                    LocalDate date = LocalDate.parse(lastupload, formatter);

                                    editor.putString("lastupload", date.toString("dd-MM-yyyy"));
                                    editor.commit();
                                    Log.v("bbdd", "Todos los segmentos guardados");
                                }

                                continue; //si falla el insert del segmento, salta al siguiente segmento

                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            result = "KO";

                        }

                        String segment_id = json.getString("segment_id");
                        JSONArray location = c.getJSONArray("location");
                        //String currentday = AppUtils.getDay2(System.currentTimeMillis()); //formato YYYYMMDD
                        String currentday = dateFile;

                        Log.v("encryption: ", currentday);

                        Log.v("bbdd", "segmentid" + segment_id);


                        for (int j = 0; j < location.length(); j++) {

                            Log.v("bbdd", "location legnth: " + j + "");

                            String latitude_enc = bytesToHex(cipher.doFinal(location.getString(j).getBytes("UTF-8")));
                            String longitude_enc = bytesToHex(cipher.doFinal(location.getString(j + 1).getBytes("UTF-8")));
                            String timeLoc_enc = bytesToHex(cipher.doFinal(location.getString(j + 2).getBytes("UTF-8")));
                            String power_enc = bytesToHex(cipher.doFinal(location.getString(j + 3).getBytes("UTF-8")));

                            String currentday_enc = bytesToHex(cipher.doFinal(currentday.getBytes("UTF-8")));
                            String segment_id_enc = bytesToHex(cipher.doFinal(segment_id.getBytes("UTF-8")));


                            //guardar parametros de location en un array
                            List<NameValuePair> params2 = new ArrayList<NameValuePair>();
                            params2.add(new BasicNameValuePair("loc_latitude", latitude_enc));
                            params2.add(new BasicNameValuePair("loc_longitude", longitude_enc));
                            params2.add(new BasicNameValuePair("loc_time", timeLoc_enc));
                            params2.add(new BasicNameValuePair("loc_date", currentday_enc));
                            params2.add(new BasicNameValuePair("seg_id", segment_id_enc));
                            params2.add(new BasicNameValuePair("loc_power", power_enc));



                            JSONObject json2 = jsonParser.makeHttpRequest(url_create_location, "POST", params2);    //subida location a base de datos

                            try {


                                int success = json2.getInt(TAG_SUCCESS);
                                if (success == 1) {
                                    // successfully created product
                                    Log.v("BBDD", "location insertado correctamente");
                                    result = "OK";
                                    contadorOkLocations++;

                                } else {
                                    // failed to create product
                                    Log.v("BBDD", "fail location");
                                    result = "KO";

                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Log.v("BBDD", "Error parsing data " + e.toString());
                                result = "KO";

                            }

                            j = j + 3;
                            Log.v("BBDD", "Location length/4 = "+location.length()/4);

                            if (contadorOkLocations == location.length()/4){
                                Log.v("BBDD", "All locations inserted OK ");
                                //insert
                                List<NameValuePair> params3 = new ArrayList<NameValuePair>();
                                params3.add(new BasicNameValuePair("seg_id", segment_id));
                                JSONObject json3 = jsonParser.makeHttpRequest(url_update_segment, "POST", params3);

                                try {
                                    int success = json3.getInt(TAG_SUCCESS);
                                    if (success == 1) {
                                        // successfully created product
                                        Log.v("BBDD", "Updated segment");
                                        result = "OK";
                                        contadorOkLocations=0;


                                    } else {
                                        // failed to create product
                                        Log.v("BBDD", "fail update segment");
                                        result = "KO";

                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    Log.v("BBDD", "Error parsing data " + e.toString());
                                    result = "KO";

                                }
                            }
                        }


                    }
                } catch (JSONException e) {
                    Log.v("BBDD", "Error parsing data " + e.toString());
                    result = "KO";

                } catch (FileNotFoundException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                    Log.v("BBDD", "Error parsing data " + e1.toString());
                    result = "KO";

                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    Log.v("BBDD", "Error parsing data " + e.toString());
                    result = "KO";

                } catch (NoSuchAlgorithmException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    Log.v("BBDD", "Error parsing data " + e.toString());
                    result = "KO";

                } catch (InvalidKeySpecException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    Log.v("BBDD", "Error parsing data " + e.toString());
                    result = "KO";

                } catch (InvalidKeyException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    Log.v("BBDD", "Error parsing data " + e.toString());
                    result = "KO";

                } catch (NoSuchPaddingException e2) {
                    // TODO Auto-generated catch block
                    e2.printStackTrace();
                    Log.v("BBDD", "Error parsing data " + e2.toString());
                    result = "KO";

                } catch (IllegalBlockSizeException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                    Log.v("BBDD", "Error parsing data " + e1.toString());
                    result = "KO";

                } catch (BadPaddingException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                    Log.v("BBDD", "Error parsing data " + e1.toString());
                    result = "KO";
                }
            }
        }
        return result;
    }
	
	public void downloaFilefromUrl(String ruta,String url,String filename){
		try {

			//ruta: mobile´s folder route
            //url: server address
            //filename: file to download

			File folder = new File(ruta);
			boolean success = true;
			if (!folder.exists()) {
                //if file doesn´t exists, create it
			    success = folder.mkdirs();
			}
			if (success) {
			    // Do something on success
				//set the download URL, a url that points to a file on the internet
		        //this is the file to be downloaded, PublicKey
		        URL urlPKey = new URL(url);

		        //create the new connection
		        HttpURLConnection urlConnection = (HttpURLConnection) urlPKey.openConnection();

		        //set up some things on the connection
		        urlConnection.setRequestMethod("GET");
		        urlConnection.setDoOutput(true);

		        //and connect!
		        urlConnection.connect();

		        //set the path where we want to save the file
		        //in this case, going to save it on the root directory of the
		        //sd card.
		       // File SDCardRoot = Environment.getExternalStorageDirectory();
		        //create a new file, specifying the path, and the filename
		        //which we want to save the file as.
		        File file = new File(ruta,filename);

		        //this will be used to write the downloaded data into the file we created
		        FileOutputStream fileOutput = new FileOutputStream(file);

		        //this will be used in reading the data from the internet
		        InputStream inputStream = urlConnection.getInputStream();

		        //this is the total size of the file
		        int totalSize = urlConnection.getContentLength();
		        //variable to store total downloaded bytes
		        int downloadedSize = 0;

		        //create a buffer...
		        byte[] buffer = new byte[1024];
		        int bufferLength = 0; //used to store a temporary size of the buffer

		        //now, read through the input buffer and write the contents to the file
		        while ( (bufferLength = inputStream.read(buffer)) > 0 ) {
		                //add the data in the buffer to the file in the file output stream (the file on the sd card)
		                fileOutput.write(buffer, 0, bufferLength);
		                //add up the size so we know how much is downloaded
		               // downloadedSize += bufferLength;
		                //this is where you would do something to report the prgress, like this maybe
		                //updateProgress(downloadedSize, totalSize);
		        }
		        //close the output stream when done
		        fileOutput.close();
				
			} else {
			    // Do something else on failure 
				Log.v("download: ","no se puede crear carpeta certs");
			}

	//catch some possible errors...
	} catch (MalformedURLException e) {
	        e.printStackTrace();
	} catch (IOException e) {
	        e.printStackTrace();
	}

	}
	
	public static String bytesToHex(byte[] data)

    {
        if (data==null) {
            return null;
        }

        int len = data.length;
        String str = "";
        for (int i=0; i<len; i++) {
            if ((data[i]&0xFF)<16) {
                str = str + "0" + java.lang.Integer.toHexString(data[i]&0xFF);
            }
            else {
                str = str + java.lang.Integer.toHexString(data[i]&0xFF);
            }
        }
        return str;
    }
	
	
	public String readJson(){
		//Log.v("BBDD",filename);
		//Read JSON content
		StringBuilder builder = new StringBuilder();
		try {
		FileInputStream stream = new FileInputStream (filename);
		//try {
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
        String line;
        while ((line = reader.readLine()) != null) {
          builder.append(line);
        }
		    }
		catch (IOException e) {
		      e.printStackTrace();
		}
		return builder.toString();

	}


}
