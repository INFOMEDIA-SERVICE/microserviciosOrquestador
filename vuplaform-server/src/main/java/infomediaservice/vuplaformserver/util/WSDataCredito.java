package infomediaservice.vuplaformserver.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.apache.http.Consts;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.components.crypto.CryptoFactory;
import org.apache.ws.security.message.WSSecHeader;
import org.apache.ws.security.message.WSSecSignature;
import org.apache.ws.security.message.WSSecTimestamp;
import org.apache.ws.security.message.WSSecUsernameToken;
import org.apache.ws.security.util.XMLUtils;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.annotation.PostConstruct;
import javax.net.ssl.SSLContext;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Properties;

@Component
public class WSDataCredito {

    @Value("${vuplaform.keyStoreFilename}")
    private String keyStoreFilename;

    @Value("${vuplaform.keyStorePassword}")
    private String keyStorePassword;

    @Value("${vuplaform.keyAlias}")
    private String keyAlias;

    @Value("${vuplaform.oktaUser}")
    private String oktaUser;

    @Value("${vuplaform.oktaPassword}")
    private String oktaPassword;

    @Value("${vuplaform.endpointUrl}")
    private String endpointUrl;

    @Value("${vuplaform.usuario}")
    private String usuario;

    @Value("${vuplaform.clave}")
    private String clave;

    @Value("${vuplaform.producto}")
    private String producto;

    private Crypto crypto;

    private HttpClient httpClient;

    @PostConstruct
    private void init() throws KeyStoreException, IOException, UnrecoverableKeyException
            , NoSuchAlgorithmException, CertificateException, KeyManagementException, WSSecurityException {
        Properties cryptoProperties = new Properties();
        cryptoProperties.setProperty("org.apache.ws.security.crypto.provider","org.apache.ws.security.components.crypto.Merlin");
        cryptoProperties.setProperty("org.apache.ws.security.crypto.merlin.keystore.type","pkcs12");
        cryptoProperties.setProperty("org.apache.ws.security.crypto.merlin.keystore.password",keyStorePassword);
        cryptoProperties.setProperty("org.apache.ws.security.crypto.merlin.file", keyStoreFilename);
        cryptoProperties.setProperty("org.apache.ws.security.crypto.merlin.keystore.alias",keyAlias);

        crypto = CryptoFactory.getInstance(cryptoProperties);

        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(new ClassPathResource(keyStoreFilename).getInputStream(), keyStorePassword.toCharArray());

        SSLContext sslContext = SSLContexts.custom()
                .loadKeyMaterial(keyStore, keyStorePassword.toCharArray())
                .build();

        httpClient = HttpClients.custom().setSSLContext(sslContext).build();
    }


    private Document documentFromString(String xml) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilder builder = getDocumentBuilder();
        Document document = builder.parse(new InputSource(new StringReader(xml)));
        return document;
    }

    private DocumentBuilder getDocumentBuilder() throws ParserConfigurationException {
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        builderFactory.setNamespaceAware(true);
        DocumentBuilder builder = builderFactory.newDocumentBuilder();
        return builder;
    }

    private String signSoap(String soapEnvelope) throws WSSecurityException, ParserConfigurationException, IOException, SAXException {

        Document doc = documentFromString(soapEnvelope);

        WSSecSignature sign = new WSSecSignature();
        sign.setUserInfo(keyAlias, keyStorePassword);
        sign.setKeyIdentifierType(WSConstants.BST_DIRECT_REFERENCE);
        sign.setUseSingleCertificate(true);
        sign.setDigestAlgo(DigestMethod.SHA1);

        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);
        sign.build(doc, crypto, secHeader);

        WSSecUsernameToken token = new WSSecUsernameToken();
        token.setPasswordType(WSConstants.PASSWORD_TEXT);
        token.setUserInfo(oktaUser, oktaPassword);
        token.addCreated();
        token.addNonce();
        token.build(doc, secHeader);

        WSSecTimestamp timestamp = new WSSecTimestamp();
        timestamp.setTimeToLive(60);
        timestamp.build(doc, secHeader);

        return XMLUtils.PrettyDocumentToString(doc);
    }

    public ConsultarHC2Response consultarHC2(Integer tipoIdentificacion, Long identificacion, String primerApellido) throws IOException, ParserConfigurationException
            , SAXException, WSSecurityException {

        String soap = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:v1=\"http://ws.hc2.dc.com/v1\">\n" +
                "   <soapenv:Header/>\n" +
                "   <soapenv:Body>\n" +
                "      <v1:consultarHC2>\n" +
                "          <v1:solicitud>\n" +
                "            <v1:clave>"+clave+"</v1:clave>\n" +
                "            <v1:identificacion>"+identificacion+"</v1:identificacion>\n" +
                "            <v1:primerApellido>"+primerApellido+"</v1:primerApellido>\n" +
                "            <v1:producto>"+producto+"</v1:producto>\n" +
                "            <v1:tipoIdentificacion>"+tipoIdentificacion+"</v1:tipoIdentificacion>\n" +
                "            <v1:usuario>"+usuario+"</v1:usuario>\n" +
                "         </v1:solicitud>\n" +
                "      </v1:consultarHC2>\n" +
                "   </soapenv:Body>\n" +
                "</soapenv:Envelope>";

        String signedSoap = signSoap(soap);
        HttpPost httpPost = new HttpPost(endpointUrl);

        httpPost.setHeader("Content-Type",
                ContentType.create("application/xml", Consts.UTF_8).toString());
        httpPost.setEntity(new ByteArrayEntity(signedSoap.getBytes()));

        HttpResponse response = httpClient.execute(httpPost);

        int responseCode = response.getStatusLine().getStatusCode();
        InputStream inputStream = response.getEntity().getContent();

        ConsultarHC2Response consultarHC2Response = new ConsultarHC2Response();
        consultarHC2Response.setCode(responseCode);
        String responseString = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        consultarHC2Response.setResponse(responseString);
        consultarHC2Response.setRequest(signedSoap);
        if(responseCode == 200){
            consultarHC2Response.setSuccess(true);
            consultarHC2Response.setResult(parseXMLResponse(responseString));
        }else{
            consultarHC2Response.setSuccess(false);
            consultarHC2Response.setError(parseXMLError(responseString));
        }

        return consultarHC2Response;
    }

    private String parseXMLResponse(String xmlResponse) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new ByteArrayInputStream(xmlResponse.getBytes(StandardCharsets.UTF_8)));
        Element root = doc.getDocumentElement();
        String elem = root.getElementsByTagName("ws:consultarHC2Return").item(0).getTextContent().replace("&lt;", "<");
        JSONObject xmlJSONObj = XML.toJSONObject(elem);
        return xmlJSONObj.toString();
    }

    private String parseXMLError(String xmlResponse) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new ByteArrayInputStream(xmlResponse.getBytes(StandardCharsets.UTF_8)));
        Element root = doc.getDocumentElement();

        NodeList nodeList = root.getElementsByTagName("faultstring");
        if(nodeList.getLength()>0){
            return nodeList.item(0).getTextContent();
        }
        return null;
    }


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ConsultarHC2Response{
        private boolean success;
        private int code;
        private String request;
        private String response;
        private String result;
        private String error;
    }
}
