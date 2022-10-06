package spring.service;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.UUID;

@Service
@Slf4j
public class FileService {
    @Autowired
    AccountService accountService;

    private final Marker auditDone = MarkerFactory.getMarker("AUDIT_DONE");
    private final Marker auditTry = MarkerFactory.getMarker("AUDIT_TRY");
    private final Marker auditError = MarkerFactory.getMarker("AUDIT_ERROR");

    public String addTempImage(HttpServletRequest request) {
        HttpSession session = request.getSession();
        String sessionData = accountService.getSession(session);
        String image = request.getParameter("image");

        String result = null;
        String error = null;
        if (sessionData != null) {
            if (image != null) {
                Base64.Decoder decoder = Base64.getDecoder();
                byte[] decoded = decoder.decode(image);
                result = decoded.toString();
            } else {
                error = "no image";
            }
        } else {
            error = "session not found";
        }

        if (error == null) {
            log.info(auditDone, request.getRemoteAddr() + " downloaded image " + image);
            return result;
        } else {
            log.info(auditError, request.getRemoteAddr() + " failed image download " + image);
            return error;
        }
    }

    public File uploadImage(String origin) {
        try {
            byte[] imageDecoded = Base64Utils.decodeFromUrlSafeString(origin);
            ByteArrayInputStream byteArrayInput = new ByteArrayInputStream(imageDecoded);
            File image = new File("C:/board-saves/profiles/" + UUID.randomUUID() + ".png");
            try {
                BufferedImage bufferedImage = ImageIO.read(byteArrayInput);
                ImageIO.write(bufferedImage, "png", image);
                ImageIO.read(image);
                log.info(auditDone, "Image " + image.getAbsolutePath() + " uploaded");
                return image;
            } catch (IOException e) {
//                log.info("image is not valid");
                log.info(auditError, "Image " + image.getAbsolutePath() + " upload failed");
                image.delete();
                return null;
            }
        } catch (IllegalArgumentException e) {
//            log.info("base64 string is not valid:\n" + e);
            return null;
        }
    }
}
