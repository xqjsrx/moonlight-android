package com.limelight;

import com.limelight.nvstream.http.ComputerDetails;
import com.limelight.nvstream.http.LimelightCryptoProvider;

import java.security.cert.X509Certificate;

/**
 * Description
 * Date: 2024-08-07
 * Time: 17:48
 */
public class StreamReqBean {
   private ComputerDetails.AddressTuple activeAddress;
   private int httpsPort;
   private String appName;
   private String uniqueId;
   private X509Certificate serverCert;
   private LimelightCryptoProvider cryptoProvider;

   public ComputerDetails.AddressTuple getActiveAddress() {
      return activeAddress;
   }

   public void setActiveAddress(ComputerDetails.AddressTuple activeAddress) {
      this.activeAddress = activeAddress;
   }

   public int getHttpsPort() {
      return httpsPort;
   }

   public void setHttpsPort(int httpsPort) {
      this.httpsPort = httpsPort;
   }

   public String getAppName() {
      return appName;
   }

   public void setAppName(String appName) {
      this.appName = appName;
   }

   public String getUniqueId() {
      return uniqueId;
   }

   public void setUniqueId(String uniqueId) {
      this.uniqueId = uniqueId;
   }

   public X509Certificate getServerCert() {
      return serverCert;
   }

   public void setServerCert(X509Certificate serverCert) {
      this.serverCert = serverCert;
   }

   public LimelightCryptoProvider getCryptoProvider() {
      return cryptoProvider;
   }

   public void setCryptoProvider(LimelightCryptoProvider cryptoProvider) {
      this.cryptoProvider = cryptoProvider;
   }
}
