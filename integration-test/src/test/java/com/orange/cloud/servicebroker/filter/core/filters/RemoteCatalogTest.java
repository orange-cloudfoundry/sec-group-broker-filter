package com.orange.cloud.servicebroker.filter.core.filters;

import com.sun.org.apache.xml.internal.resolver.Catalog;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.junit.Test;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import sun.net.www.http.HttpClient;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

/**
 * @author Sebastien Bortolussi
 */
public class RemoteCatalogTest {
    @Test
    public void should_get_remote_catalog() throws Exception {
        SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(new SSLContextBuilder().loadTrustMaterial(null, new TrustSelfSignedStrategy()).build());

        CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(socketFactory).build();


        HttpComponentsClientHttpRequestFactory requestFactory =
                new HttpComponentsClientHttpRequestFactory();

        requestFactory.setHttpClient(httpClient);

        TestRestTemplate restTemplate = new TestRestTemplate();

        customize(restTemplate.getRestTemplate());


        //final RestTemplate restTemplate = new RestTemplate(requestFactory);

        Catalog catalog = restTemplate.getForObject("https://mysql-mocked-broker.cf.redacted-domain/v2/catalog", Catalog.class);
        System.out.println("catalog = " +catalog);
    }

    public void customize(RestTemplate restTemplate) throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(
                new SSLContextBuilder().loadTrustMaterial(null,
                        new TrustSelfSignedStrategy()).build());

        CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(socketFactory)
                .build();
        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory(httpClient));
    }
}
