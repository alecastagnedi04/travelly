package it.uniroma3.siw.travelly.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


@Service
public class ImageStorageService {

    @Value("${travelly.upload-dir:uploads}")
    private String uploadDir;


    /*
     * Salva la foto di copertina di un viaggio.
     *
     * Nel database NON salviamo il file,
     * ma solamente il suo nome.
     */
    public String saveTripCover(
            MultipartFile file) {

        if (file == null || file.isEmpty()) {

            return null;
        }


        /*
         * Accettiamo solamente immagini.
         */
        String contentType =
                file.getContentType();


        if (contentType == null ||
                !isAllowedImageType(contentType)) {

            throw new IllegalArgumentException(
                    "Formato immagine non supportato"
            );
        }


        String extension =
                getExtension(contentType);


        /*
         * Generiamo un nome unico.
         *
         * In questo modo due utenti possono caricare
         * tranquillamente file con lo stesso nome.
         */
        String fileName =
                UUID.randomUUID()
                        + extension;


        try {

            Path tripUploadDirectory =
                    Paths.get(
                            uploadDir,
                            "trips"
                    )
                    .toAbsolutePath()
                    .normalize();


            Files.createDirectories(
                    tripUploadDirectory
            );


            Path destination =
                    tripUploadDirectory
                            .resolve(fileName)
                            .normalize();


            Files.copy(
                    file.getInputStream(),
                    destination,
                    StandardCopyOption.REPLACE_EXISTING
            );


            return fileName;
        }

        catch (IOException e) {

            throw new IllegalStateException(
                    "Errore durante il salvataggio dell'immagine",
                    e
            );
        }
    }


    /*
     * Elimina una vecchia copertina.
     *
     * Utile quando l'utente cambia immagine
     * oppure elimina completamente il viaggio.
     */
    public void deleteTripCover(
            String fileName) {

        if (fileName == null ||
                fileName.isBlank()) {

            return;
        }


        try {

            Path file =
                    Paths.get(
                            uploadDir,
                            "trips",
                            fileName
                    )
                    .toAbsolutePath()
                    .normalize();


            Files.deleteIfExists(file);
        }

        catch (IOException e) {

            /*
             * Non blocchiamo l'applicazione
             * se la vecchia immagine non può essere
             * eliminata.
             */
            System.err.println(
                    "Impossibile eliminare immagine: "
                    + fileName
            );
        }
    }


    private boolean isAllowedImageType(
            String contentType) {

        return contentType.equals("image/jpeg")
                || contentType.equals("image/png")
                || contentType.equals("image/webp");
    }


    private String getExtension(
            String contentType) {

        return switch (contentType) {

            case "image/png" ->
                    ".png";

            case "image/webp" ->
                    ".webp";

            default ->
                    ".jpg";
        };
    }
}