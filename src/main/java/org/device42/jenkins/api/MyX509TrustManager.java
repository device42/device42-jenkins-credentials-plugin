package org.device42.jenkins.api;
import java.security.cert.CertificateException;

import javax.net.ssl.X509TrustManager;

public class MyX509TrustManager implements X509TrustManager
	{
        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
        }

        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
        }

        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
          return new java.security.cert.X509Certificate[]{};
        }
      }
	
