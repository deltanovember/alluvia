package com.alluvialtrading.tools;

import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;
import com.google.common.io.Files;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;


public class WebConnector {

	public static void main(String[] args) {
		HttpClient client = new DefaultHttpClient();
		HttpGet get = new HttpGet(
				"http://www.marketdatasystems.com/content/files/Tiered%20Margin.xls");

		try {


			HttpResponse response = client.execute(get);
			InputStream data = response.getEntity().getContent();
			try {
				OutputStream output = new FileOutputStream( new File("c:\\temp\\margin.xls"));
				ByteStreams.copy(data, output);
			}
			finally {
				Closeables.closeQuietly(data);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
