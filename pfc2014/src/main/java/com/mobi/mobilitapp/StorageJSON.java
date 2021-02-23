package com.mobi.mobilitapp;

import android.content.res.Resources.NotFoundException;
import android.os.AsyncTask;
import android.os.Environment;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Lists;
import com.google.api.services.storage.Storage;
import com.google.api.services.storage.model.ObjectAccessControl;
import com.google.api.services.storage.model.StorageObject;
import com.google.common.collect.ImmutableMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Collections;
import java.util.List;

public class StorageJSON extends AsyncTask<Void, Void, String> {

	InputStream raw;
	String filename, foldername;
	private String result = "OK";

	public StorageJSON(InputStream raw, String foldername, String filename) {
		this.raw = raw;
		this.filename = filename;
		this.foldername = foldername;
	}

	@Override
	protected String doInBackground(Void... params) {
		String BUCKET_NAME = "atm_json";
		String STORAGE_SCOPE = "https://www.googleapis.com/auth/devstorage.read_write";
		String SERVICE_ACCOUNT_EMAIL = "892686673584-68ocfevvjfnn2a4u6g9t3sdvsm49l5j0@developer.gserviceaccount.com";
		final String ruta = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/MobilitApp/";
		JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

		HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();

		KeyStore keystore;
		PrivateKey key = null;

		try {
			keystore = KeyStore.getInstance("PKCS12");

			keystore.load(raw, "notasecret".toCharArray());
			key = (PrivateKey) keystore.getKey("privatekey", "password".toCharArray());
		} catch (KeyStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CertificateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnrecoverableKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		GoogleCredential credential = new GoogleCredential.Builder().setTransport(httpTransport)
				.setJsonFactory(JSON_FACTORY).setServiceAccountPrivateKey(key)
				.setServiceAccountId(SERVICE_ACCOUNT_EMAIL)
				.setServiceAccountScopes(Collections.singleton(STORAGE_SCOPE)).build();
		try {
			credential.refreshToken();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Storage stor = new Storage.Builder(httpTransport, JSON_FACTORY, credential).setApplicationName("pfc2014")
				.build();

		Storage.Objects.Insert ins;
		StorageObject newobj = null;
		InputStreamContent isc = null;
		File file = new File(filename);

		try {
			InputStream is = new FileInputStream(file);
			isc = new InputStreamContent("application/json", is);
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		List<ObjectAccessControl> acl = Lists.newArrayList(); // empty acl (seems default acl).
		newobj = new StorageObject()
				.setName(foldername + "/" + filename.substring(ruta.length(), filename.length() - 21) + ".json")
				.setMetadata(ImmutableMap.of("key1", "value1", "key2", "value2")).setAcl(acl)
				.setContentDisposition("attachment");

		try {

			ins = stor.objects().insert(BUCKET_NAME, newobj, isc);
			ins.getMediaHttpUploader().setDirectUploadEnabled(true);
			ins.execute();

		} catch (IOException e) {
			result="KO";
			e.printStackTrace();
		}

		return result;
	}
}