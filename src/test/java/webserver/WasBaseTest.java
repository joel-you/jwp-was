package webserver;

import webserver.http.BufferedReaderAsHttpRequest;
import webserver.http.CachedHttpRequest;
import webserver.http.HttpRequest;
import webserver.http.HttpRequestPipe;

import java.io.*;

public class WasBaseTest {
    private String testDirectory = "./src/test/resources/";

    private String outputDirectory = "./out/";

    protected HttpRequest request(String fileName) throws Exception {
        InputStream in = new FileInputStream(new File(testDirectory + fileName));
        HttpRequestPipe httpRequestPipe = new CachedHttpRequest(new BufferedReaderAsHttpRequest(in));
        return httpRequestPipe.request();
    }

    protected OutputStream createOutputStream(String filename) throws FileNotFoundException {
        createOutputDirectoryDontExist();
        return new FileOutputStream(new File(outputDirectory + filename));
    }

    private void createOutputDirectoryDontExist() {
        File directory = new File(outputDirectory);
        if (! directory.exists()){
            directory.mkdir();
        }
    }
}
