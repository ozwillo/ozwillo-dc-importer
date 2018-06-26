package org.ozwillo.dcimporter.web.marchesecurise;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class CreateConsultationByJava {
    public static String sendSOAPByJava(String SOAPUrl, String soapMessage)
            throws Exception {
        URL url = new URL(SOAPUrl);
        URLConnection connection = url.openConnection();
        HttpURLConnection httpConn = (HttpURLConnection) connection;


        byte[] byteArray = soapMessage.getBytes("UTF-8");

        httpConn.setRequestProperty("Content-Length", String
                .valueOf(byteArray.length));
        httpConn.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
        httpConn.setRequestProperty("SOAPAction", "");
        httpConn.setRequestMethod("POST");

        httpConn.setDoOutput(true);
        httpConn.setDoInput(true);

        OutputStream out = httpConn.getOutputStream();
        out.write(byteArray);
        out.close();
        BufferedReader in = null;
        StringBuffer resultMessage= new StringBuffer();
        try {
            InputStreamReader isr = new InputStreamReader(httpConn
                    .getInputStream());
            in = new BufferedReader(isr);
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                resultMessage.append(inputLine);
            }

        } finally {
            if (in != null) {
                in.close();
            }
        }
        return resultMessage.toString();
    }
}
