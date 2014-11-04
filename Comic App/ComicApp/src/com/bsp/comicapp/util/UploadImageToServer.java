package com.bsp.comicapp.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import android.util.Log;

public class UploadImageToServer {
	private int serverResponseCode = 0;
	public String urlImage = "";

	public UploadImageToServer() {
		serverResponseCode = 0;
	}

	public int uploadFile(String uploadFilePath, String uploadFileName) {
		// String fileName = "alo.png";
	
		HttpURLConnection conn = null;
		DataOutputStream dos = null;
		String lineEnd = "\r\n";
		String twoHyphens = "--";
		String boundary = "*****";
		int bytesRead, bytesAvailable, bufferSize;
		byte[] buffer;
		int maxBufferSize = 1 * 1024 * 1024;
		File sourceFile = new File(uploadFilePath);

		if (!sourceFile.isFile()) {
			Log.e("uploadFile", "Source File not exist :" + "" + uploadFilePath);

			// runOnUiThread(new Runnable() {
			// public void run() {
			// messageText.setText("Source File not exist :"
			// + uploadFilePath + "" + uploadFileName);
			// }
			// });

			return 0;

		} else {
			try {

				// open a URL connection to the Servlet
				FileInputStream fileInputStream = new FileInputStream(
						sourceFile);
				URL url = new URL(Config.API_UPLOAD_FILE);

				// Open a HTTP connection to the URL
				conn = (HttpURLConnection) url.openConnection();
				conn.setDoInput(true); // Allow Inputs
				conn.setDoOutput(true); // Allow Outputs
				conn.setUseCaches(false); // Don't use a Cached Copy
				conn.setRequestMethod("POST");
				conn.setRequestProperty("Connection", "Keep-Alive");
				conn.setRequestProperty("ENCTYPE", "multipart/form-data");
				conn.setRequestProperty("Content-Type",
						"multipart/form-data;boundary=" + boundary);
				conn.setRequestProperty("files", uploadFilePath);

				dos = new DataOutputStream(conn.getOutputStream());

				dos.writeBytes(twoHyphens + boundary + lineEnd);
				dos.writeBytes("Content-Disposition: form-data; name=\"files\";filename=\""
						+ uploadFileName + "\"" + lineEnd);
				// TODO filename
				Log.i("fileName", uploadFileName);

				dos.writeBytes(lineEnd);

				// create a buffer of maximum size
				bytesAvailable = fileInputStream.available();

				bufferSize = Math.min(bytesAvailable, maxBufferSize);
				buffer = new byte[bufferSize];

				// read file and write it into form...
				bytesRead = fileInputStream.read(buffer, 0, bufferSize);

				while (bytesRead > 0) {

					dos.write(buffer, 0, bufferSize);
					bytesAvailable = fileInputStream.available();
					bufferSize = Math.min(bytesAvailable, maxBufferSize);
					bytesRead = fileInputStream.read(buffer, 0, bufferSize);

				}

				// send multipart form data necesssary after file data...
				dos.writeBytes(lineEnd);
				dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

				// Responses from the server (code and message)
				serverResponseCode = conn.getResponseCode();
				String serverResponseMessage = conn.getResponseMessage();
				// conn.getRequestMethod()
				String line;
				StringBuilder builder = new StringBuilder();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(conn.getInputStream()));
				while ((line = reader.readLine()) != null) {
					builder.append(line);
				}
				urlImage = builder.toString();
				Log.i("uploadFile", "HTTP Response is : " + urlImage + ": "
						+ serverResponseCode);

				// if (serverResponseCode == HttpURLConnection.HTTP_OK) {
				//
				// runOnUiThread(new Runnable() {
				// public void run() {
				//
				// String msg =
				// "File Upload Completed.\n\n See uploaded file here : \n\n"
				// + " http://www.androidexample.com/media/uploads/"
				// + uploadFileName;
				//
				// messageText.setText(msg);
				// Toast.makeText(UploadToServer.this,
				// "File Upload Complete.", Toast.LENGTH_SHORT)
				// .show();
				// }
				// });
				// }

				// close the streams //
				fileInputStream.close();
				dos.flush();
				dos.close();

			} catch (Exception e) {
				e.printStackTrace();
				Log.e("Upload file to server Exception",
						"Exception : " + e.getMessage(), e);
			}
			return serverResponseCode;

		} // End else block
	}
}
