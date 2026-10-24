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
     * =====================================================
     * CARTELLA DELLE COPERTINE
     * =====================================================
     */
    private Path getTripUploadPath() {


        Path path =
                Paths.get(
                        uploadDir,
                        "trips"
                )
                .toAbsolutePath()
                .normalize();


        try {

            Files.createDirectories(
                    path
            );

        }
        catch (IOException e) {

            throw new IllegalStateException(
                    "Impossibile creare la cartella delle immagini",
                    e
            );
        }


        return path;
    }



    /*
     * =====================================================
     * SALVA UNA NUOVA COPERTINA
     * =====================================================
     */
    public String saveTripCover(
            MultipartFile file) {


        if (file == null ||
                file.isEmpty()) {

            return null;
        }


        /*
         * Controlliamo il tipo del file.
         */
        String contentType =
                file.getContentType();


        String extension;


        if ("image/jpeg".equals(contentType)) {

            extension = ".jpg";
        }

        else if ("image/png".equals(contentType)) {

            extension = ".png";
        }

        else if ("image/webp".equals(contentType)) {

            extension = ".webp";
        }

        else {

            throw new IllegalArgumentException(
                    "Formato immagine non supportato. Usa JPG, PNG o WEBP."
            );
        }



        /*
         * Generiamo un nome univoco.
         */
        String fileName =
                UUID.randomUUID()
                        + extension;



        Path destination =
                getTripUploadPath()
                        .resolve(
                                fileName
                        )
                        .normalize();



        try {

            Files.copy(
                    file.getInputStream(),
                    destination,
                    StandardCopyOption.REPLACE_EXISTING
            );

        }
        catch (IOException e) {

            throw new IllegalStateException(
                    "Errore durante il salvataggio dell'immagine",
                    e
            );
        }


        return fileName;
    }



    /*
     * =====================================================
     * COPIA UNA COPERTINA ESISTENTE
     * =====================================================
     *
     * Utilizzato quando un utente sceglie:
     *
     * "Usa questo itinerario"
     *
     * Il nuovo viaggio riceve una COPIA FISICA
     * dell'immagine del template.
     *
     * In questo modo template e viaggio personale
     * non condividono lo stesso file.
     */
    public String copyTripCover(
            String originalFileName) {


        if (originalFileName == null ||
                originalFileName.isBlank()) {

            return null;
        }


        Path directory =
                getTripUploadPath();


        Path originalFile =
                directory
                        .resolve(
                                originalFileName
                        )
                        .normalize();



        /*
         * Se per qualche motivo il file
         * non esiste più, utilizziamo
         * semplicemente la copertina standard.
         */
        if (!Files.exists(
                originalFile
        )) {

            return null;
        }



        /*
         * Recuperiamo l'estensione originale.
         *
         * Esempio:
         *
         * abc123.jpg -> .jpg
         */
        String extension =
                "";


        int dotIndex =
                originalFileName
                        .lastIndexOf('.');


        if (dotIndex >= 0) {

            extension =
                    originalFileName
                            .substring(
                                    dotIndex
                            );
        }



        /*
         * Nuovo nome indipendente.
         */
        String newFileName =
                UUID.randomUUID()
                        + extension;



        Path newFile =
                directory
                        .resolve(
                                newFileName
                        )
                        .normalize();



        try {

            Files.copy(
                    originalFile,
                    newFile,
                    StandardCopyOption.REPLACE_EXISTING
            );

        }
        catch (IOException e) {

            throw new IllegalStateException(
                    "Errore durante la copia dell'immagine",
                    e
            );
        }


        return newFileName;
    }



    /*
     * =====================================================
     * ELIMINA COPERTINA
     * =====================================================
     */
    public void deleteTripCover(
            String fileName) {


        if (fileName == null ||
                fileName.isBlank()) {

            return;
        }


        Path file =
                getTripUploadPath()
                        .resolve(
                                fileName
                        )
                        .normalize();


        try {

            Files.deleteIfExists(
                    file
            );

        }
        catch (IOException e) {

            throw new IllegalStateException(
                    "Errore durante l'eliminazione dell'immagine",
                    e
            );
        }
    }
}