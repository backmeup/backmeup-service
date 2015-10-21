package org.backmeup.utilities.qrcode;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import org.backmeup.configuration.cdi.ConfigurationFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

public class AccessTokenPdfQRCodeGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(AccessTokenPdfQRCodeGenerator.class);

    private final String title;
    private final String body1, body2;

    Font titleFont, bodyFont, codeFont;
    Document document;

    public AccessTokenPdfQRCodeGenerator() {
        this.titleFont = new Font(Font.FontFamily.HELVETICA, 22, Font.BOLD);
        this.bodyFont = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.NORMAL);
        this.codeFont = new Font(Font.FontFamily.TIMES_ROMAN, 18, Font.BOLD);

        this.title = "Aktivierung Digitales Erbe";
        this.body1 = "Vererben bedeutet teilen mit der Zukunft. Das Themis Projekt stellt eine sichere und skalierbare Plattformlösung bereit, die es dem Endanwender erlaubt, regelmäßig und automatisch persönliche Backups aus online Diensten wie Facebook, Moodle, E-Mail, Dropbox, dem Smartphone und Desktopquellen zu erzeugen. Themis gibt dem Benutzer die Kontrolle über seine Daten zurück und stellt darüber hinaus nützliche Anwendungen im Hinblick auf das private digitale Erbe bereit. "
                + "Hierzu zählen etwa eine leistungsstarke Volltext- und Metadatensuche über den gesamten archivierten Datenbestand des Benutzers, innovative Formen der Datenaufbereitung über Raum und Zeit sowie die Möglichkeit, Datensätze individuell zu arrangieren und diese mit anderen Freunden und Familienmitgliedern auf der Plattform sicher zu teilen, ohne hierbei die Kontrolle darüber zu verlieren."
                + "Themis erlaubt es gespeicherte Daten im Falle des Ablebens sicher an die Nachfahren weiterzuvererben. Privacy und Security sind notwendige Grundlagen eines solchen Dienstes und zentrale Punkte der Themis Sicherheitsarchitektur.";
        this.body2 = "Mit diesem Code erhältst du Zugriff auf ein mit dir geteiltes digitales Erbe, kannst dieses online ansehen, aktivieren, durchsuchen und herunterladen.";
    }

    public InputStream generateQRCodePDF(String accessCode) throws MalformedURLException, IOException {
        try {
            ByteArrayOutputStream osPdf = new ByteArrayOutputStream();

            this.document = new Document();
            PdfWriter.getInstance(this.document, osPdf);

            this.document.open();
            this.document.addTitle(this.title);
            this.document.addKeywords("Backmeup Access Token");
            this.document.addAuthor("backmeup");
            this.document.addCreator("www.backmeup.at");

            Paragraph content = new Paragraph();
            content.add(new Paragraph(" "));
            content.add(new Paragraph(this.title, this.titleFont));
            content.add(new Paragraph(" "));
            content.add(new Paragraph(this.body1, this.bodyFont));
            content.add(new Paragraph(this.body2, this.bodyFont));
            this.document.add(content);
            Image img;
            try {

                //get the host address the system is currently running on to generate the URL for the QRCode
                String callbackURL = getCallbackURL();
                img = Image
                        .getInstance(this.generateQRCode(callbackURL + "/page/loginToken.html?authCode=" + accessCode).getAbsolutePath());
                this.document.add(img);
            } catch (WriterException e) {
                LOGGER.debug("Error creating QRCode PNG file" + e.toString());
            }
            this.document.add(new Paragraph("Activation Code: " + accessCode, this.codeFont));
            this.document.close();

            LOGGER.debug("created QRCode PDF with access token");
            return new ByteArrayInputStream(osPdf.toByteArray());
        } catch (DocumentException e) {
            throw new RuntimeException(e);
        }
    }

    private String getCallbackURL() {
        Properties props = ConfigurationFactory.getProperties();
        if ((props != null) && (props.get("backmeup.callbackUrl") != null)) {
            String propCallbackUrl = props.get("backmeup.callbackUrl").toString();

            if (propCallbackUrl.equals("###REPLACE_ME###")) {
                return "http://localhost:8080";
            } else {
                //something like http://themis-dev01.backmeup.at/page/create_backup_oAuthHandler.html
                return propCallbackUrl.substring(0, propCallbackUrl.indexOf("/", 8));
            }
        }
        return "";
    }

    private File generateQRCode(String qrCodeData) throws IOException, WriterException {
        Random randomGenerator = new Random();
        File f = File.createTempFile("QRCode" + randomGenerator.nextInt(100000), ".png");
        f.deleteOnExit();
        String filePath = f.getAbsolutePath();
        String charset = "UTF-8"; // or "ISO-8859-1"
        Map hintMap = new HashMap();
        hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
        QRCodeGenerator.createQRCode(qrCodeData, filePath, charset, hintMap, 200, 200);
        return f;
    }
}
