package cl.duoc.cursos.service;

import cl.duoc.cursos.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;

/**
 * Material de los cursos en AWS S3.
 * Estructura del bucket: {bucketName}/materiales/{codigoCurso}/{nombreArchivo}
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class S3Service {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    /** Sube el material (PDF u otro) de un curso y devuelve la key en S3. */
    public String subirMaterial(String codigoCurso, MultipartFile archivo) throws IOException {
        String nombre = archivo.getOriginalFilename() == null ? "material" : archivo.getOriginalFilename();
        String key = "materiales/" + codigoCurso + "/" + nombre;
        log.info("Subiendo material a S3: {}/{}", bucketName, key);
        s3Client.putObject(
            PutObjectRequest.builder()
                .bucket(bucketName).key(key)
                .contentType(archivo.getContentType())
                .contentLength(archivo.getSize())
                .build(),
            RequestBody.fromBytes(archivo.getBytes()));
        return key;
    }

    /** Descarga el material de un curso por su key. */
    public byte[] descargarPorKey(String key) {
        log.info("Descargando material desde S3: {}/{}", bucketName, key);
        try {
            ResponseBytes<GetObjectResponse> r = s3Client.getObjectAsBytes(
                GetObjectRequest.builder().bucket(bucketName).key(key).build());
            return r.asByteArray();
        } catch (NoSuchKeyException e) {
            throw new ResourceNotFoundException("Material no encontrado en S3: " + key);
        }
    }
}
