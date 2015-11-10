/**
 * File Downloader
 * <p/>
 * 2015-11-10
 * by mindwing (mindwing@gmail.com)
 */
package kr.mindwing.filedownloader;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = "MP3_Downloader";

    private ArrayList<String> urlList = new ArrayList<>();
    private ArrayList<TextView> progressList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        urlList.add("http://dl.bgms.kr/download/cddgf/mp3/Bittersweet_Symphony_2");
        urlList.add("http://dl.bgms.kr/download/pQYzz/mp3/Mika+-+Grace+Kelly");
        urlList.add("http://xxx.com/xxx.mp3");

        progressList.add((TextView) findViewById(R.id.progress0));
        progressList.add((TextView) findViewById(R.id.progress1));
        progressList.add((TextView) findViewById(R.id.progress2));
    }

    @Override
    protected void onStart() {
        super.onStart();

        showSnackbar("다운로드할 MP3 파일을 눌러주세요.");
    }

    public void downloadMP3(View button) {
        int index = Integer.parseInt((String) button.getTag());
        String urlString = urlList.get(index);
        TextView progress = progressList.get(index);

        progress.setText("0");
        showSnackbar(urlString);

        try {
            URL url = new URL(urlString);

            AsyncDownloadTask downloadTask = new AsyncDownloadTask(progress);
            downloadTask.execute(url);
        } catch (MalformedURLException e) {
            showSnackbar("URL 에 문제가 있습니다.");
            Log.e(LOG_TAG, "URL 문제: " + urlString, e);
            progress.setText("X");
        }
    }

    private void showSnackbar(String snack) {
        Snackbar.make(findViewById(android.R.id.content),
                snack, Snackbar.LENGTH_SHORT)
                .show();
    }

    class AsyncDownloadTask extends AsyncTask<URL, Integer, String> {
        private TextView progress;

        AsyncDownloadTask(TextView _progress) {
            progress = _progress;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

            progress.setText(values[0].toString());
        }

        @Override
        protected String doInBackground(URL... urls) {
            String mp3FileName = null;

            try {
                mp3FileName = storeMP3toAsset(urls[0]);
            } catch (IOException ioe) {
                showSnackbar("I/O 문제가 발생했습니다.");
                Log.e(LOG_TAG, "I/O 문제: ", ioe);
            }

            return mp3FileName;
        }

        private String storeMP3toAsset(URL url) throws IOException {
            URL retVal = null;
            String fileName = url.getFile().replace("/", "_");

            FileOutputStream fos = openFileOutput(fileName, Context.MODE_PRIVATE);

            URLConnection urlConn = url.openConnection();
            urlConn.setConnectTimeout(1000);
            urlConn.setReadTimeout(1000);

            int contentLength = urlConn.getContentLength();
            BufferedInputStream bis = new BufferedInputStream(urlConn.getInputStream());
            byte[] buf = new byte[1024];

            for (int downloadSize = 0, readSize; (readSize = bis.read(buf)) != -1; ) {
                downloadSize += readSize;
                publishProgress(downloadSize * 100 / contentLength);
                fos.write(buf, 0, readSize);
            }

            fos.close();

            FileInputStream fis = openFileInput(fileName);
            if (fis == null || fis.read() == -1) {
                throw new IOException("저장파일이 존재하지 않음.");
            }

            fis.close();

            return fileName;
        }

        @Override
        protected void onPostExecute(String fileName) {
            super.onPostExecute(fileName);

            if (fileName != null) {
                showSnackbar("다운로드가 완료되었습니다: " + fileName);
            }
        }
    }
}
