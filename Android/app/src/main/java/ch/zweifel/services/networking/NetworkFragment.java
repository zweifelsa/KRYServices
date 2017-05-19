/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.zweifel.services.networking;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import ch.zweifel.services.model.Service;

/**
 * Implementation of headless Fragment that runs an AsyncTask to fetch data from the network.
 */
public class NetworkFragment extends Fragment {
    public static final String TAG = "NetworkFragment";
    public static final String METHOD_GET = "GET";
    public static final String METHOD_POST = "POST";
    public static final String METHOD_DELETE = "DELETE";

    private RequestCallback callback;
    private NetworkTask networkTask;

    /**
     * Static initializer for NetworkFragment that sets the URL of the host it will be downloading
     * from.
     */
    public static NetworkFragment getInstance(FragmentManager fragmentManager) {
        // Recover NetworkFragment in case we are re-creating the Activity due to a config change.
        // This is necessary because NetworkFragment might have a task that began running before
        // the config change and has not finished yet.
        // The NetworkFragment is recoverable via this method because it calls
        // setRetainInstance(true) upon creation.
        NetworkFragment networkFragment = (NetworkFragment) fragmentManager
                .findFragmentByTag(NetworkFragment.TAG);
        if (networkFragment == null) {
            networkFragment = new NetworkFragment();
            fragmentManager.beginTransaction().add(networkFragment, TAG).commit();
        }
        return networkFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retain this Fragment across configuration changes in the host Activity.
        setRetainInstance(true);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Host Activity will handle callbacks from task.
        callback = (RequestCallback) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        // Clear reference to host Activity.
        callback = null;
    }

    @Override
    public void onDestroy() {
        // Cancel task when Fragment is destroyed.
        cancelRequest();
        super.onDestroy();
    }

    /**
     * Start non-blocking execution of NetworkTask.
     */
    public void startRequest(String method) {
        startRequest(null, method);
    }

    /**
     * Start non-blocking execution of NetworkTask.
     */
    public void startRequest(Service service, String method) {
        cancelRequest();
        networkTask = new NetworkTask(new ServiceRequest(service, method));
        networkTask.execute();
    }

    /**
     * Cancel (and interrupt if necessary) any ongoing NetworkTask execution.
     */
    public void cancelRequest() {
        if (networkTask != null) {
            networkTask.cancel(true);
            networkTask = null;
        }
    }

    /**
     * Implementation of AsyncTask that runs a network operation on a background thread.
     */
    private class NetworkTask extends AsyncTask<Void, Integer, NetworkTask.Result> {

        private ServiceRequest request;

        private NetworkTask(ServiceRequest request) {
            this.request = request;
        }

        /**
         * Wrapper class that serves as a union of a result value and an exception. When the
         * download task has completed, either the result value or exception can be a non-null
         * value. This allows you to pass exceptions to the UI thread that were thrown during
         * doInBackground().
         */
        class Result {
            String resultValue;
            Exception exception;

            Result(String resultValue) {
                this.resultValue = resultValue;
            }

            Result(Exception exception) {
                this.exception = exception;
            }
        }

        /**
         * Cancel background network operation if we do not have network connectivity.
         */
        @Override
        protected void onPreExecute() {
            if (callback != null) {
                NetworkInfo networkInfo = callback.getActiveNetworkInfo();
                if (networkInfo == null || !networkInfo.isConnected() ||
                        (networkInfo.getType() != ConnectivityManager.TYPE_WIFI
                                && networkInfo.getType() != ConnectivityManager.TYPE_MOBILE)) {
                    // If no connectivity, cancel task and update Callback with null data.
                    callback.onFail(request, "No network connectivity");
                    cancel(true);
                }
            }
        }

        /**
         * Defines work to perform on the background thread.
         */
        @Override
        protected Result doInBackground(Void... args) {
            Result result;
            String urlString = request.getUrl();
            String methodString = request.getMethod();
            String bodyString = request.getBodyAsString();
            try {
                URL url = new URL(urlString);
                String resultString = request(url, methodString, bodyString);
                if (resultString != null) {
                    result = new Result(resultString);
                } else {
                    throw new IOException("No response received.");
                }
            } catch (Exception e) {
                result = new Result(e);
            }
            return result;
        }

        /**
         * Send RequestCallback a progress update.
         */
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            if (values.length >= 2) {
                callback.onProgressUpdate(values[0], values[1]);
            }
        }

        /**
         * Updates the RequestCallback with the result.
         */
        @Override
        protected void onPostExecute(Result result) {
            if (result != null && callback != null) {
                if (result.exception != null) {
                    callback.onFail(request, result.exception.getMessage());
                } else if (result.resultValue != null) {
                    callback.onSuccess(request, result.resultValue);
                }
                callback.finishRequest();
            }
        }

        /**
         * Override to add special behavior for cancelled AsyncTask.
         */
        @Override
        protected void onCancelled(Result result) {
        }

        /**
         * Given a URL, sets up a connection and gets the HTTP response body from the server.
         * If the network request is successful, it returns the response body in String form. Otherwise,
         * it will throw an IOException.
         */
        private String request(URL url, String methodString, String body) throws IOException {
            InputStream stream = null;
            HttpURLConnection connection = null;
            String result = null;
            try {
                connection = (HttpURLConnection) url.openConnection();
                // Timeout for reading InputStream arbitrarily set to 3000ms.
                connection.setReadTimeout(3000);
                // Timeout for connection.connect() arbitrarily set to 3000ms.
                connection.setConnectTimeout(3000);
                // For this use case, set HTTP method to GET.
                connection.setRequestMethod(methodString);
                // Already true by default but setting just in case; needs to be true since this request
                // is carrying an input (response) body.
                if (methodString.equals(METHOD_GET)) {
                    connection.setDoInput(true);
                }
                byte[] outputInBytes = null;
                if (body != null && !body.isEmpty()) {
                    connection.setDoOutput(true);
                    connection.setRequestProperty("Content-Type", "application/json");
                    outputInBytes = body.getBytes("UTF-8");
                    connection.setFixedLengthStreamingMode(outputInBytes.length);
                }
                // Open communications link (network traffic occurs here).
                connection.connect();
                publishProgress(RequestCallback.Progress.CONNECT_SUCCESS);

                if (outputInBytes != null && connection.getDoOutput()) {
                    OutputStream os = connection.getOutputStream();
                    os.write(outputInBytes);
                    os.close();
                }

                int responseCode = connection.getResponseCode();
                if (responseCode >= 400) {
                    throw new IOException("HTTP error code: " + responseCode);
                }
                if (connection.getDoInput()) {
                    // Retrieve the response body as an InputStream.
                    stream = connection.getInputStream();
                    publishProgress(RequestCallback.Progress.GET_INPUT_STREAM_SUCCESS, 0);
                    if (stream != null) {
                        // Converts Stream to String with max length of 500.
                        int length = connection.getContentLength();
                        result = readStream(stream, length);
                    }
                } else {
                    result = "";
                }
                publishProgress(RequestCallback.Progress.PROCESS_INPUT_STREAM_SUCCESS, 0);
            } finally {
                // Close Stream and disconnect HTTPS connection.
                if (stream != null) {
                    stream.close();
                }
                if (connection != null) {
                    connection.disconnect();
                }
            }
            return result;
        }

        /**
         * Converts the contents of an InputStream to a String.
         */
        private String readStream(InputStream stream, int maxLength) throws IOException {
            String result = null;
            // Read InputStream using the UTF-8 charset.
            InputStreamReader reader = new InputStreamReader(stream, "UTF-8");
            // Create temporary buffer to hold Stream data with specified max length.
            char[] buffer = new char[maxLength];
            // Populate temporary buffer with Stream data.
            int numChars = 0;
            int readSize = 0;
            while (numChars < maxLength && readSize != -1) {
                numChars += readSize;
                int pct = (100 * numChars) / maxLength;
                publishProgress(RequestCallback.Progress.PROCESS_INPUT_STREAM_IN_PROGRESS, pct);
                readSize = reader.read(buffer, numChars, buffer.length - numChars);
            }
            if (numChars != -1) {
                // The stream was not empty.
                // Create String that is actual length of response body if actual length was less than
                // max length.
                numChars = Math.min(numChars, maxLength);
                result = new String(buffer, 0, numChars);
            }
            return result;
        }
    }
}
