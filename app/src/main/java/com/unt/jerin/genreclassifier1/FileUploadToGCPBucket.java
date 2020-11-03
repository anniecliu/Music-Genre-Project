package com.unt.jerin.genreclassifier1;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Acl;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class FileUploadToGCPBucket {









	public static void uploadObject(String projectId, String bucketName, String objectName, String filePath, InputStream bucketCredsInputStream)
			throws IOException {
		// The ID of your GCP project
		// String projectId = "your-project-id";

		// The ID of your GCS bucket
		// String bucketName = "your-unique-bucket-name";

		// The ID of your GCS object
		// String objectName = "your-object-name";

		// The path to your file to upload
		// String filePath = "path/to/your/file"

		
	    /*StorageOptions storageOptions = StorageOptions.newBuilder()
	    	       .setProjectId(projectId)
	    	        .setCredentials(GoogleCredentials.fromStream(new 
	    	         FileInputStream("C:\\Users\\jerin\\Google Drive\\JP_NOTEBOOKS\\CSCE-5214-SD-for-AI\\Genre-Classifier\\genre-classifier-51ce6f486f8b.json"))).build();*/

		StorageOptions storageOptions = StorageOptions.newBuilder()
				.setProjectId(projectId)
				.setCredentials(GoogleCredentials.fromStream(bucketCredsInputStream)).build();

	    Storage storage = storageOptions.getService();
	    
	    
		/*Acl acl  = new Acl("");
		storage.createAcl(bucketName, acl  );*/
	    
	    
	    // Map<String, String> newMetadata = new HashMap<>();
	    // newMetadata.put("content-type", "audio/wav");
	    	    

		//Storage storage = StorageOptions.newBuilder().setProjectId(projectId).build().getService();
		BlobId blobId = BlobId.of(bucketName, objectName);
		
		//BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
		BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("audio/wav").build();
		Blob blobReponse = storage.create(blobInfo, Files.readAllBytes(Paths.get(filePath)));
		
		//blobReponse.toBuilder().setMetadata(newMetadata).build().update(null);

		System.out.println("File " + filePath + " uploaded to bucket " + bucketName + " as " + objectName);
		
		System.out.println("blobReponse: "+blobReponse);
	}



	public static void main(String[] args) {

		String currentTimestamp = System.currentTimeMillis()+"";

		// The ID of your GCP project
		String projectId = "genre-classifier-293000";

		// The ID of your GCS bucket
		String bucketName = "genre-classifier-audio-files-bucket";

		// The ID of your GCS object
		String objectName = "testfile_"+currentTimestamp+".wav";

		// The path to your file to upload
		String filePath = "C:\\Jerin\\datasets\\genre-guesser1\\test-recorded-dataset\\classical-mozart.wav";


		try {
			uploadObject(projectId, bucketName, objectName, filePath, null);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
