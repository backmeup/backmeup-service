package org.backmeup.logic.impl.helper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.backmeup.utilities.qrcode.AccessTokenPdfQRCodeGenerator;
import org.backmeup.utilities.qrcode.QRCodeGenerator;
import org.junit.Test;

import com.google.zxing.EncodeHintType;
import com.google.zxing.NotFoundException;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

public class QRCodeGeneratorTest {

    QRCodeGenerator qrGen = new QRCodeGenerator();

    @Test
    public void testSamplePNGQRCodeGeneration() throws IOException, WriterException, NotFoundException {
        String qrCodeData = "Hello World!";
        //String filePath = "C://tmp//QRCode2.png";
        File f = File.createTempFile("QRCodePNGTest", ".png");
        String filePath = f.getAbsolutePath();
        String charset = "UTF-8"; // or "ISO-8859-1"
        Map hintMap = new HashMap();
        hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
        QRCodeGenerator.createQRCode(qrCodeData, filePath, charset, hintMap, 200, 200);
        assertTrue("QR Code image not created successfully! " + f.getAbsolutePath(), f.canRead());
        assertEquals("Data not properly read from QR Code: " + f.getAbsolutePath(), qrCodeData,
                QRCodeGenerator.readQRCode(filePath, charset, hintMap));
    }

    @Test
    public void createSamplePDFQRCodeGeneration() throws IOException {
        AccessTokenPdfQRCodeGenerator pdfGen = new AccessTokenPdfQRCodeGenerator();
        InputStream inStream = pdfGen.generateQRCodePDF("ABC123TOKEN");

        File f = File.createTempFile("QRCodePDFTest", ".pdf");
        OutputStream outStream = new FileOutputStream(f);
        IOUtils.copy(inStream, outStream);

        System.out.println("Find the pdf in " + f.getAbsolutePath());
        assertTrue("PDF File does not exist", f.canRead());

    }

}
