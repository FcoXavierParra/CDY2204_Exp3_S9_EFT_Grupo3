package cl.duoc.cursos.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.*;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * Cliente de AWS S3. Usa las credenciales estaticas si se definen (aws.access-key/secret/session),
 * o la cadena por defecto del SDK (variables de entorno AWS_*) recomendado en EC2/AWS Academy.
 */
@Configuration
public class AwsS3Config {

    @Value("${aws.s3.region}")
    private String region;
    @Value("${aws.access-key:}")
    private String accessKey;
    @Value("${aws.secret-key:}")
    private String secretKey;
    @Value("${aws.session-token:}")
    private String sessionToken;

    @Bean
    public S3Client s3Client() {
        var builder = S3Client.builder().region(Region.of(region));
        if (!accessKey.isBlank() && !secretKey.isBlank()) {
            AwsCredentials credentials = sessionToken.isBlank()
                ? AwsBasicCredentials.create(accessKey, secretKey)
                : AwsSessionCredentials.create(accessKey, secretKey, sessionToken);
            builder.credentialsProvider(StaticCredentialsProvider.create(credentials));
        } else {
            builder.credentialsProvider(DefaultCredentialsProvider.create());
        }
        return builder.build();
    }
}
