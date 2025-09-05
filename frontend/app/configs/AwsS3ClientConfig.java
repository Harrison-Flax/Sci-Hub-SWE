package configs;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import javax.inject.Inject;
import java.io.File;

public class AwsS3ClientConfig {

    private static AmazonS3 s3Client = null;

    @Inject
    static Config config;

    public static AmazonS3 getS3Client() {
        if (s3Client == null) {
            synchronized (AwsS3ClientConfig.class) {
                if (s3Client == null) {
                    String configFilePath = System.getProperty("config.file");
                    Config config = configFilePath != null ? ConfigFactory.parseFile(new File(configFilePath)) : ConfigFactory.load();

                    String accessKeyId = config.getString("aws.accessKeyId");
                    String secretAccessKey = config.getString("aws.secretAccessKey");
                    String region = config.getString("aws.region");

                    BasicAWSCredentials awsCreds = new BasicAWSCredentials(accessKeyId, secretAccessKey);
                    s3Client = AmazonS3ClientBuilder.standard()
                            .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                            .withRegion(region)
                            .build();
                }
            }
        }
        return s3Client;
    }
}
