package utils;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import configs.AwsS3ClientConfig;
import play.Logger;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

/**
 * S3Utils
 *
 * Utilities for interacting with AWS S3.
 * Simplified and optimized for readability and efficiency.
 *
 * @author Chang 1/13/25
 */
public class S3Utils {

    private S3Utils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Retrieves a bucket by its name if it exists.
     *
     * @param bucketName The name of the S3 bucket.
     * @return An Optional<Bucket> that is present if the bucket exists.
     */
    public static Optional<Bucket> getBucket(String bucketName) {
        final AmazonS3 s3 = AwsS3ClientConfig.getS3Client();
        List<Bucket> buckets = s3.listBuckets();
        return buckets.stream().filter(b -> b.getName().equals(bucketName)).findFirst();
    }

    /**
     * Creates a new bucket with the specified name if it doesn't already exist.
     *
     * @param bucketName The name of the bucket to create.
     * @return The created or existing bucket.
     */
    public static Optional<Bucket> createBucket(String bucketName) {
        final AmazonS3 s3 = AwsS3ClientConfig.getS3Client();
        if (s3.doesBucketExistV2(bucketName)) {
            Logger.debug("Bucket " + bucketName + " already exists.");
            return getBucket(bucketName);
        } else {
            try {
                Bucket b = s3.createBucket(bucketName);
                return Optional.of(b);
            } catch (AmazonS3Exception e) {
                Logger.error("Error creating bucket: " + e.getErrorMessage());
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Uploads a file to a specified bucket.
     *
     * @param bucketName      The name of the bucket to which the file will be uploaded.
     * @param file            file info
     * @param fileObjKeyName  Key name for the object in the bucket.
     * @param contentType     MIME type of the file.
     * @param fileDescription Description of the file.
     * @param fileLabel       Label for the file.
     */
    public static String uploadFile(String bucketName, File file, String fileObjKeyName,
                                    String contentType, String fileDescription, String fileLabel) {
        try {
            AmazonS3 s3Client = AwsS3ClientConfig.getS3Client();

            // Prepare the file to upload
            PutObjectRequest request = new PutObjectRequest(bucketName, fileObjKeyName, file);

            // Set metadata if necessary
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(contentType);
            metadata.addUserMetadata("description", fileDescription);
            metadata.addUserMetadata("label", fileLabel);
            request.setMetadata(metadata);

            // Upload the file
            PutObjectResult putObjectResult = s3Client.putObject(request);

            if (putObjectResult.getVersionId().isEmpty()){
                return null;
            }

            // Generate the public URL for the uploaded file
            return s3Client.getUrl(bucketName, fileObjKeyName).toString(); // Return the URL for the uploaded file
        } catch (AmazonServiceException e) {
            Logger.error("Amazon service error: " + e.getMessage());
            throw new RuntimeException(e);
        } catch (SdkClientException e) {
            Logger.error("SDK client error: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static String getObject(String bucketName, String fileObjKeyName){
        try {
            AmazonS3 s3Client = AwsS3ClientConfig.getS3Client();

            S3Object s3Object = s3Client.getObject(new GetObjectRequest(bucketName, fileObjKeyName));
            InputStream objectData = s3Object.getObjectContent();

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[16384];
            while ((nRead = objectData.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }

            return Base64.getEncoder().encodeToString(buffer.toByteArray());
        } catch (AmazonServiceException e) {
            Logger.error("Amazon service error: " + e.getMessage());
            throw new RuntimeException(e);
        } catch (SdkClientException e) {
            Logger.error("SDK client error: " + e.getMessage());
            throw new RuntimeException(e);
        } catch (IOException e) {
            Logger.error("File read error: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
