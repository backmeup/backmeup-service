package org.backmeup.utilities.qrcode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import javax.imageio.ImageIO;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;

/**
 * Uses the google ZXING library to create a PNG image from a base64 input String
 *
 */
public class QRCodeGenerator {

    public static void createQRCode(String qrCodeData, String filePath, String charset, Map hintMap, int qrCodeheight,
            int qrCodewidth) throws WriterException, IOException {
        BitMatrix matrix = new MultiFormatWriter().encode(new String(qrCodeData.getBytes(charset), charset),
                BarcodeFormat.QR_CODE, qrCodewidth, qrCodeheight, hintMap);
        MatrixToImageWriter.writeToFile(matrix, filePath.substring(filePath.lastIndexOf('.') + 1), new File(filePath));
    }

    public static String readQRCode(String filePath, String charset, Map hintMap) throws FileNotFoundException,
            IOException, NotFoundException {
        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(
                ImageIO.read(new FileInputStream(filePath)))));
        Result qrCodeResult = new MultiFormatReader().decode(binaryBitmap, hintMap);
        return qrCodeResult.getText();
    }

    /*public void createSampleQRCode() throws WriterException, IOException, NotFoundException {
    String qrCodeData = "Hello World!";
    //String filePath = "C://tmp//QRCode2.png";
    File f = File.createTempFile("QRCodeTest", ".png");
    String filePath = f.getAbsolutePath();
    String charset = "UTF-8"; // or "ISO-8859-1"
    Map hintMap = new HashMap();
    hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);

    createQRCode(qrCodeData, filePath, charset, hintMap, 200, 200);
    System.out.println("QR Code image created successfully!: " + f.getAbsolutePath());
    System.out.println("Data read from QR Code: " + f.getAbsolutePath() + ": "
            + readQRCode(filePath, charset, hintMap));
    }*/

}
