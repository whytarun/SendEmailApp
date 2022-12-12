package net.simplifiedcoding.myemailsender;

import android.app.Application;
import android.content.Context;
import android.util.Base64;

import com.google.android.gms.common.internal.Constants;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.channels.SocketChannel;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.CertificatePinner;
import okhttp3.CipherSuite;
import okhttp3.ConnectionSpec;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.TlsVersion;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {


    private static final String BASE_URL = "https://api.mailgun.net/v3/sandbox2905c51c976e44dd9437086ddbd700c0.mailgun.org/";

    private static final String API_USERNAME = "api";

    //you need to change the value to your API key
    private static final String API_PASSWORD = "37ecf507fefb95ffb8e58a8beaa9d0d2-f2340574-add19eff";

    private static final String AUTH = "Basic " + Base64.encodeToString((API_USERNAME+":"+API_PASSWORD).getBytes(), Base64.NO_WRAP);

    private static RetrofitClient mInstance;
    private Retrofit retrofit;
    private SSLContext sslContext;
    private TrustManagerFactory tmf ;
    {
        try {
            sslContext = SSLContext.getInstance("TLSv1.2");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private RetrofitClient() {
        String hostname = "api.mailgun.net";
        CertificatePinner certificatePinner = new CertificatePinner.Builder()
                .add(hostname, "sha1/MIIGxjCCBa6gAwIBAgIQDzqm6ot5SmrQb6AopFzD5zANBgkqhkiG9w0BAQsFADBP\n" +
                        "MQswCQYDVQQGEwJVUzEVMBMGA1UEChMMRGlnaUNlcnQgSW5jMSkwJwYDVQQDEyBE\n" +
                        "aWdpQ2VydCBUTFMgUlNBIFNIQTI1NiAyMDIwIENBMTAeFw0yMjA0MDQwMDAwMDBa\n" +
                        "Fw0yMzA0MDQyMzU5NTlaMG8xCzAJBgNVBAYTAlVTMQ4wDAYDVQQIEwVUZXhhczEU\n" +
                        "MBIGA1UEBxMLU2FuIEFudG9uaW8xIjAgBgNVBAoTGU1BSUxHVU4gVEVDSE5PTE9H\n" +
                        "SUVTLCBJTkMxFjAUBgNVBAMMDSoubWFpbGd1bi5uZXQwggEiMA0GCSqGSIb3DQEB\n" +
                        "AQUAA4IBDwAwggEKAoIBAQDRcY2Thh1ODRWx4rlta00CkNde/wbG0bfbzn+C+Uwl\n" +
                        "8tbBcD1uM3OfrmJ0PBkpyq29aXOTxQHBroeEKF00U/L0dMgEsui6oSORrn8GAL96\n" +
                        "vzuPcZyYlqks6eMKxpsjwebdnQL1KCGiiRlGpfk0KaAGFKqHZY8DbZ8a7eIc2GML\n" +
                        "rBIK50EGiXCVuw6IDTCbDxzotifNzb7W/3FtmQkWRc6vWVpspxH55Oqw7gu/Azp4\n" +
                        "+vv0zHqCUqwvKX6qA3jz8g5+VCoP9REvK3mHtNrOQwWkR7bZRqDLTKGOsu0hnnti\n" +
                        "/wmkTn8sVsBAVmiyPXuIUXirtwQqljgxxU/s1sE06IRJAgMBAAGjggN8MIIDeDAf\n" +
                        "BgNVHSMEGDAWgBS3a6LqqKqEjHnqtNoPmLLFlXa59DAdBgNVHQ4EFgQUhgoyZbGK\n" +
                        "0uRPcf5fNPvnLRkZ5ukwJQYDVR0RBB4wHIINKi5tYWlsZ3VuLm5ldIILbWFpbGd1\n" +
                        "bi5uZXQwDgYDVR0PAQH/BAQDAgWgMB0GA1UdJQQWMBQGCCsGAQUFBwMBBggrBgEF\n" +
                        "BQcDAjCBjwYDVR0fBIGHMIGEMECgPqA8hjpodHRwOi8vY3JsMy5kaWdpY2VydC5j\n" +
                        "b20vRGlnaUNlcnRUTFNSU0FTSEEyNTYyMDIwQ0ExLTQuY3JsMECgPqA8hjpodHRw\n" +
                        "Oi8vY3JsNC5kaWdpY2VydC5jb20vRGlnaUNlcnRUTFNSU0FTSEEyNTYyMDIwQ0Ex\n" +
                        "LTQuY3JsMD4GA1UdIAQ3MDUwMwYGZ4EMAQICMCkwJwYIKwYBBQUHAgEWG2h0dHA6\n" +
                        "Ly93d3cuZGlnaWNlcnQuY29tL0NQUzB/BggrBgEFBQcBAQRzMHEwJAYIKwYBBQUH\n" +
                        "MAGGGGh0dHA6Ly9vY3NwLmRpZ2ljZXJ0LmNvbTBJBggrBgEFBQcwAoY9aHR0cDov\n" +
                        "L2NhY2VydHMuZGlnaWNlcnQuY29tL0RpZ2lDZXJ0VExTUlNBU0hBMjU2MjAyMENB\n" +
                        "MS0xLmNydDAJBgNVHRMEAjAAMIIBgAYKKwYBBAHWeQIEAgSCAXAEggFsAWoAdwDo\n" +
                        "PtDaPvUGNTLnVyi8iWvJA9PL0RFr7Otp4Xd9bQa9bgAAAX/2VDhAAAAEAwBIMEYC\n" +
                        "IQDNdEpBidPpaEzt8rxkDc3vAEf2iPc4ziWE5TbWOKr8yAIhANkZeHzUB462VAtf\n" +
                        "onn0yFDgyDa9JE7Rc/BHVmREqlHuAHYANc8ZG7+xbFe/D61MbULLu7YnICZR6j/h\n" +
                        "Ku+oA8M71kwAAAF/9lQ4cAAABAMARzBFAiEAot/xtw6+6Dap65NPQe5R7/i+8qg5\n" +
                        "qL//RC+YmGjd0G4CIECnWGOsfGFZO9Hbi9p+iuCc3pjASmIXS5YLVQ2UnjmdAHcA\n" +
                        "s3N3B+GEUPhjhtYFqdwRCUp5LbFnDAuH3PADDnk2pZoAAAF/9lQ4VgAABAMASDBG\n" +
                        "AiEA04TP4RpIk1uVZFeS2WCcti2ly18NCiSsvfPB2jlvQRECIQCp8kVL1dkpB1qd\n" +
                        "724hplzHaoMuvjtAmHy76o770fSEejANBgkqhkiG9w0BAQsFAAOCAQEAmXpI2FeU\n" +
                        "tw4+/JFwGT8jmO6+4hsOtuPhlcbPS67e3OOpqFwtQQzgqw7xS778au9l2fn0lAY6\n" +
                        "PErCagXrwIgoVbO/xvLAXMOR/DHenZpTwc34jx3fO7lX0jnluIMRKD+wcuG2PkJS\n" +
                        "mfBXVYslHk9UrETxT2dl0QgVRgF9PD7CbIONUl+Vd/nShk2Vj+Q+VuumXXOHmQPt\n" +
                        "glwNR5K3gTJhkE8HRjKacV0RbsD1kZ9jTZslDPgipG3PmToulKxqq/nXSp2rYMiY\n" +
                        "Y92hhD+CUO5aj/RT8ExL6P39Ib0f653ei1isepvrsK1SaP2BGPPMyKw/XxkAm848\n" +
                        "5yvJq83TxXwR9w==")
                .build();
        ConnectionSpec spec = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                .supportsTlsExtensions(true)
                .tlsVersions(TlsVersion.TLS_1_1, TlsVersion.TLS_1_2, TlsVersion.TLS_1_0)
                .cipherSuites(
                        CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
                        CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
                        CipherSuite.TLS_DHE_RSA_WITH_AES_128_GCM_SHA256,
                        CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA,
                        CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA,
                        CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA,
                        CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA,
                        CipherSuite.TLS_ECDHE_ECDSA_WITH_RC4_128_SHA,
                        CipherSuite.TLS_ECDHE_RSA_WITH_RC4_128_SHA,
                        CipherSuite.TLS_DHE_RSA_WITH_AES_128_CBC_SHA,
                        CipherSuite.TLS_DHE_DSS_WITH_AES_128_CBC_SHA,
                        CipherSuite.TLS_DHE_RSA_WITH_AES_256_CBC_SHA)
                .build();

        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            InputStream instream = MainApplication.context.getResources().openRawResource(R.raw.certificate);
            Certificate ca;
            ca = cf.generateCertificate(instream);
            KeyStore kStore = KeyStore.getInstance(KeyStore.getDefaultType());
            kStore.load(null, null);
            kStore.setCertificateEntry("ca", ca);
            tmf= TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(kStore);
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, tmf.getTrustManagers(), null);
        } catch (CertificateException | KeyStoreException | NoSuchAlgorithmException | IOException | KeyManagementException e) {
            e.printStackTrace();
        }

        OkHttpClient okClient = null;
        //try {
            okClient = new OkHttpClient.Builder()

                    .addInterceptor(
                            new Interceptor() {
                                @Override
                                public Response intercept(Chain chain) throws IOException {
                                    Request original = chain.request();

                                    //Adding basic auth
                                    Request.Builder requestBuilder = original.newBuilder()
                                            .header("Authorization", AUTH)
                                            .method(original.method(), original.body());

                                    Request request = requestBuilder.build();
                                    return chain.proceed(request);
                                }
                            })
                    //.certificatePinner(certificatePinner)
                    //.connectionSpecs(Collections.singletonList(spec))
                    //.sslSocketFactory(new MyTLSSocketFactory())
                    .sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) tmf.getTrustManagers()[0])
                    .build();
//        } catch (KeyManagementException e) {
//            e.printStackTrace();
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//        }

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okClient)
                .build();
    }

    public static synchronized RetrofitClient getInstance() {
        if (mInstance == null) {
            mInstance = new RetrofitClient();
        }
        return mInstance;
    }

    public Retrofit getClient() {
        return retrofit;
    }

    public Api getApi() {
        return retrofit.create(Api.class);
    }
}

//class NoSSLv3SocketFactory extends SSLSocketFactory{
//    private final SSLSocketFactory delegate;
//
//    public NoSSLv3SocketFactory() {
//        this.delegate = HttpsURLConnection.getDefaultSSLSocketFactory();
//    }
//
//    public NoSSLv3SocketFactory(SSLSocketFactory delegate) {
//        this.delegate = delegate;
//    }
//
//    @Override
//    public String[] getDefaultCipherSuites() {
//        return delegate.getDefaultCipherSuites();
//    }
//
//    @Override
//    public String[] getSupportedCipherSuites() {
//        return delegate.getSupportedCipherSuites();
//    }
//
//    private Socket makeSocketSafe(Socket socket) {
//        if (socket instanceof SSLSocket) {
//            socket = new NoSSLv3SSLSocket((SSLSocket) socket);
//        }
//        return socket;
//    }
//
//    @Override
//    public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
//        return makeSocketSafe(delegate.createSocket(s, host, port, autoClose));
//    }
//
//    @Override
//    public Socket createSocket(String host, int port) throws IOException {
//        return makeSocketSafe(delegate.createSocket(host, port));
//    }
//
//    @Override
//    public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException {
//        return makeSocketSafe(delegate.createSocket(host, port, localHost, localPort));
//    }
//
//    @Override
//    public Socket createSocket(InetAddress host, int port) throws IOException {
//        return makeSocketSafe(delegate.createSocket(host, port));
//    }
//
//    @Override
//    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
//        return makeSocketSafe(delegate.createSocket(address, port, localAddress, localPort));
//    }
//
//    private class NoSSLv3SSLSocket extends DelegateSSLSocket {
//
//        private NoSSLv3SSLSocket(SSLSocket delegate) {
//            super(delegate);
//
//        }
//
//        @Override
//        public void setEnabledProtocols(String[] protocols) {
//            if (protocols != null && protocols.length == 1 && "SSLv3".equals(protocols[0])) {
//
//                List<String> enabledProtocols = new ArrayList<String>(Arrays.asList(delegate.getEnabledProtocols()));
//                if (enabledProtocols.size() > 1) {
//                    enabledProtocols.remove("SSLv3");
//                    System.out.println("Removed SSLv3 from enabled protocols");
//                } else {
//                    System.out.println("SSL stuck with protocol available for " + String.valueOf(enabledProtocols));
//                }
//                protocols = enabledProtocols.toArray(new String[enabledProtocols.size()]);
//            }
//
//            super.setEnabledProtocols(protocols);
//        }
//    }
//
//    public class DelegateSSLSocket extends SSLSocket {
//
//        protected final SSLSocket delegate;
//
//        DelegateSSLSocket(SSLSocket delegate) {
//            this.delegate = delegate;
//        }
//
//        @Override
//        public String[] getSupportedCipherSuites() {
//            return delegate.getSupportedCipherSuites();
//        }
//
//        @Override
//        public String[] getEnabledCipherSuites() {
//            return delegate.getEnabledCipherSuites();
//        }
//
//        @Override
//        public void setEnabledCipherSuites(String[] suites) {
//            delegate.setEnabledCipherSuites(suites);
//        }
//
//        @Override
//        public String[] getSupportedProtocols() {
//            return delegate.getSupportedProtocols();
//        }
//
//        @Override
//        public String[] getEnabledProtocols() {
//            return delegate.getEnabledProtocols();
//        }
//
//        @Override
//        public void setEnabledProtocols(String[] protocols) {
//            delegate.setEnabledProtocols(protocols);
//        }
//
//        @Override
//        public SSLSession getSession() {
//            return delegate.getSession();
//        }
//
//        @Override
//        public void addHandshakeCompletedListener(HandshakeCompletedListener listener) {
//            delegate.addHandshakeCompletedListener(listener);
//        }
//
//        @Override
//        public void removeHandshakeCompletedListener(HandshakeCompletedListener listener) {
//            delegate.removeHandshakeCompletedListener(listener);
//        }
//
//        @Override
//        public void startHandshake() throws IOException {
//            delegate.startHandshake();
//        }
//
//        @Override
//        public void setUseClientMode(boolean mode) {
//            delegate.setUseClientMode(mode);
//        }
//
//        @Override
//        public boolean getUseClientMode() {
//            return delegate.getUseClientMode();
//        }
//
//        @Override
//        public void setNeedClientAuth(boolean need) {
//            delegate.setNeedClientAuth(need);
//        }
//
//        @Override
//        public void setWantClientAuth(boolean want) {
//            delegate.setWantClientAuth(want);
//        }
//
//        @Override
//        public boolean getNeedClientAuth() {
//            return delegate.getNeedClientAuth();
//        }
//
//        @Override
//        public boolean getWantClientAuth() {
//            return delegate.getWantClientAuth();
//        }
//
//        @Override
//        public void setEnableSessionCreation(boolean flag) {
//            delegate.setEnableSessionCreation(flag);
//        }
//
//        @Override
//        public boolean getEnableSessionCreation() {
//            return delegate.getEnableSessionCreation();
//        }
//
//        @Override
//        public void bind(SocketAddress localAddr) throws IOException {
//            delegate.bind(localAddr);
//        }
//
//        @Override
//        public synchronized void close() throws IOException {
//            delegate.close();
//        }
//
//        @Override
//        public void connect(SocketAddress remoteAddr) throws IOException {
//            delegate.connect(remoteAddr);
//        }
//
//        @Override
//        public void connect(SocketAddress remoteAddr, int timeout) throws IOException {
//            delegate.connect(remoteAddr, timeout);
//        }
//
//        @Override
//        public SocketChannel getChannel() {
//            return delegate.getChannel();
//        }
//
//        @Override
//        public InetAddress getInetAddress() {
//            return delegate.getInetAddress();
//        }
//
//        @Override
//        public InputStream getInputStream() throws IOException {
//            return delegate.getInputStream();
//        }
//
//        @Override
//        public boolean getKeepAlive() throws SocketException {
//            return delegate.getKeepAlive();
//        }
//
//        @Override
//        public InetAddress getLocalAddress() {
//            return delegate.getLocalAddress();
//        }
//
//        @Override
//        public int getLocalPort() {
//            return delegate.getLocalPort();
//        }
//
//        @Override
//        public SocketAddress getLocalSocketAddress() {
//            return delegate.getLocalSocketAddress();
//        }
//
//        @Override
//        public boolean getOOBInline() throws SocketException {
//            return delegate.getOOBInline();
//        }
//
//        @Override
//        public OutputStream getOutputStream() throws IOException {
//            return delegate.getOutputStream();
//        }
//
//        @Override
//        public int getPort() {
//            return delegate.getPort();
//        }
//
//        @Override
//        public synchronized int getReceiveBufferSize() throws SocketException {
//            return delegate.getReceiveBufferSize();
//        }
//
//        @Override
//        public SocketAddress getRemoteSocketAddress() {
//            return delegate.getRemoteSocketAddress();
//        }
//
//        @Override
//        public boolean getReuseAddress() throws SocketException {
//            return delegate.getReuseAddress();
//        }
//
//        @Override
//        public synchronized int getSendBufferSize() throws SocketException {
//            return delegate.getSendBufferSize();
//        }
//
//        @Override
//        public int getSoLinger() throws SocketException {
//            return delegate.getSoLinger();
//        }
//
//        @Override
//        public synchronized int getSoTimeout() throws SocketException {
//            return delegate.getSoTimeout();
//        }
//
//        @Override
//        public boolean getTcpNoDelay() throws SocketException {
//            return delegate.getTcpNoDelay();
//        }
//
//        @Override
//        public int getTrafficClass() throws SocketException {
//            return delegate.getTrafficClass();
//        }
//
//        @Override
//        public boolean isBound() {
//            return delegate.isBound();
//        }
//
//        @Override
//        public boolean isClosed() {
//            return delegate.isClosed();
//        }
//
//        @Override
//        public boolean isConnected() {
//            return delegate.isConnected();
//        }
//
//        @Override
//        public boolean isInputShutdown() {
//            return delegate.isInputShutdown();
//        }
//
//        @Override
//        public boolean isOutputShutdown() {
//            return delegate.isOutputShutdown();
//        }
//
//        @Override
//        public void sendUrgentData(int value) throws IOException {
//            delegate.sendUrgentData(value);
//        }
//
//        @Override
//        public void setKeepAlive(boolean keepAlive) throws SocketException {
//            delegate.setKeepAlive(keepAlive);
//        }
//
//        @Override
//        public void setOOBInline(boolean oobinline) throws SocketException {
//            delegate.setOOBInline(oobinline);
//        }
//
//        @Override
//        public void setPerformancePreferences(int connectionTime, int latency, int bandwidth) {
//            delegate.setPerformancePreferences(connectionTime, latency, bandwidth);
//        }
//
//        @Override
//        public synchronized void setReceiveBufferSize(int size) throws SocketException {
//            delegate.setReceiveBufferSize(size);
//        }
//
//        @Override
//        public void setReuseAddress(boolean reuse) throws SocketException {
//            delegate.setReuseAddress(reuse);
//        }
//
//        @Override
//        public synchronized void setSendBufferSize(int size) throws SocketException {
//            delegate.setSendBufferSize(size);
//        }
//
//        @Override
//        public void setSoLinger(boolean on, int timeout) throws SocketException {
//            delegate.setSoLinger(on, timeout);
//        }
//
//        @Override
//        public synchronized void setSoTimeout(int timeout) throws SocketException {
//            delegate.setSoTimeout(timeout);
//        }
//
//        @Override
//        public void setTcpNoDelay(boolean on) throws SocketException {
//            delegate.setTcpNoDelay(on);
//        }
//
//        @Override
//        public void setTrafficClass(int value) throws SocketException {
//            delegate.setTrafficClass(value);
//        }
//
//        @Override
//        public void shutdownInput() throws IOException {
//            delegate.shutdownInput();
//        }
//
//        @Override
//        public void shutdownOutput() throws IOException {
//            delegate.shutdownOutput();
//        }
//
//        @Override
//        public String toString() {
//            return delegate.toString();
//        }
//
//        @Override
//        public boolean equals(Object o) {
//            return delegate.equals(o);
//        }
//    }
//}

class MyTLSSocketFactory extends SSLSocketFactory {

    private SSLSocketFactory internalSSLSocketFactory;

    public MyTLSSocketFactory() throws KeyManagementException, NoSuchAlgorithmException {
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, null, null);
        internalSSLSocketFactory = context.getSocketFactory();
    }

    @Override
    public String[] getDefaultCipherSuites() {
        return internalSSLSocketFactory.getDefaultCipherSuites();
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return internalSSLSocketFactory.getSupportedCipherSuites();
    }

    @Override
    public Socket createSocket() throws IOException {
        return enableTLSOnSocket(internalSSLSocketFactory.createSocket());
    }

    @Override
    public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
        return enableTLSOnSocket(internalSSLSocketFactory.createSocket(s, host, port, autoClose));
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
        return enableTLSOnSocket(internalSSLSocketFactory.createSocket(host, port));
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException, UnknownHostException {
        return enableTLSOnSocket(internalSSLSocketFactory.createSocket(host, port, localHost, localPort));
    }

    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
        return enableTLSOnSocket(internalSSLSocketFactory.createSocket(host, port));
    }

    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
        return enableTLSOnSocket(internalSSLSocketFactory.createSocket(address, port, localAddress, localPort));
    }

    private Socket enableTLSOnSocket(Socket socket) {
        if(socket != null && (socket instanceof SSLSocket)) {
            ((SSLSocket)socket).setEnabledProtocols(new String[] {"TLSv1.1", "TLSv1.2"});
        }
        return socket;
    }

}
