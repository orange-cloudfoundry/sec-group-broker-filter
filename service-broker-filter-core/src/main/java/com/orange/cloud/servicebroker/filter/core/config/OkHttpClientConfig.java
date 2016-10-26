/*
 * <!--
 *
 *     Copyright (C) 2015 Orange
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 * -->
 */

package com.orange.cloud.servicebroker.filter.core.config;

import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;


@Configuration
@ConditionalOnClass(OkHttpClient.class)
@Slf4j
public class OkHttpClientConfig {

    private static final Interceptor LOGGING_INTERCEPTOR = new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();

            long t1 = System.nanoTime();
            log.info(String.format("Sending request %s on %s%n%s",
                    request.url(), chain.connection(), request.headers()));

            Response response = chain.proceed(request);

            long t2 = System.nanoTime();
            log.info(String.format("Received response for %s in %.1fms%n%s",
                    response.request().url(), (t2 - t1) / 1e6d, response.headers()));

            return response;
        }
    };
    @Value("${director.proxyHost:}")
    private String proxyHost;
    @Value("${director.proxyPort:0}")
    private int proxyPort;

    @Bean
    public OkHttpClient squareHttpClient() {
        HostnameVerifier hostnameVerifier = new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };
        TrustManager[] trustAllCerts = new TrustManager[]{new TrustAllCerts()};

        SSLSocketFactory sslSocketFactory = null;
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new SecureRandom());
            sslSocketFactory = (SSLSocketFactory) sc.getSocketFactory();
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            new IllegalArgumentException(e);
        }

        log.info("===> configuring OkHttp");
        OkHttpClient.Builder ohc = new OkHttpClient.Builder()
                .protocols(Arrays.asList(Protocol.HTTP_1_1))
                .followRedirects(true)
                .followSslRedirects(true)
                .hostnameVerifier(hostnameVerifier)
                .sslSocketFactory(sslSocketFactory)
                .addInterceptor(LOGGING_INTERCEPTOR);

        if ((this.proxyHost != null) && (this.proxyHost.length() > 0)) {
            log.info("Activating proxy on host {} port {}", this.proxyHost, this.proxyPort);
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(this.proxyHost, this.proxyPort));
            ohc.proxy(proxy);
            ohc.proxySelector(new ProxySelector() {
                @Override
                public List<Proxy> select(URI uri) {
                    return Arrays.asList(proxy);
                }

                @Override
                public void connectFailed(URI uri, SocketAddress socket, IOException e) {
                    throw new IllegalArgumentException("connection to proxy failed", e);
                }
            });
        }

        return ohc.build();
    }

    public static class TrustAllCerts extends X509ExtendedTrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[]{} ; //see http://stackoverflow.com/questions/25509296/trusting-all-certificates-with-okhttp
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType, Socket socket)
                throws CertificateException {
            // TODO Auto-generated method stub

        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType, SSLEngine engine)
                throws CertificateException {
            // TODO Auto-generated method stub

        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType, Socket socket)
                throws CertificateException {
            // TODO Auto-generated method stub

        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType, SSLEngine engine)
                throws CertificateException {
            // TODO Auto-generated method stub

        }

    }


}