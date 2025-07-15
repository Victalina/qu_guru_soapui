import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.spring.guides.gs_producing_web_service.GetCountryRequest;
import io.spring.guides.gs_producing_web_service.GetCountryResponse;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.soap.*;
import org.junit.jupiter.api.Test;

import java.io.*;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SoapTests {

  @Test
  void getCountriesTest() throws Exception {
// InputStream is = SoapTests.class.getClassLoader().getResourceAsStream("getCountryRequest.xml");
// final String request = new String(IOUtils.toByteArray(is));

    GetCountryRequest request = new GetCountryRequest();
    request.setName("Spain");

    MessageFactory messageFactory = MessageFactory.newInstance();
    SOAPMessage soapMessage = messageFactory.createMessage();
    SOAPPart soapPart = soapMessage.getSOAPPart();
    SOAPEnvelope soapEnvelope = soapPart.getEnvelope();
    SOAPBody soapBody = soapEnvelope.getBody();
    JAXBContext jbCtxRequest = JAXBContext.newInstance(GetCountryRequest.class);
    Marshaller marshaller = jbCtxRequest.createMarshaller();
    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
    marshaller.marshal(request, soapBody);
    soapMessage.saveChanges();
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    soapMessage.writeTo(baos);
    String requestResult = baos.toString();

    RestAssured.baseURI = "http://localhost:8080/ws";
    Response response = given()
            .header("Content-Type", "text/xml")
            .and()
            .body(requestResult)
            .when()
            .post("/getCountry")
            .then()
            .statusCode(200)
            .extract().response();

    SOAPMessage messageResponse = MessageFactory.newInstance().createMessage(null, new ByteArrayInputStream(response.asByteArray()));
    JAXBContext jbCtx = JAXBContext.newInstance(GetCountryResponse.class);
    Unmarshaller unmarshaller = jbCtx.createUnmarshaller();
    GetCountryResponse getCountryResponse = (GetCountryResponse) unmarshaller.unmarshal(messageResponse.getSOAPBody().extractContentAsDocument());
    assertTrue(getCountryResponse.getCountry().getName().equals("Spain"));
    assertTrue(getCountryResponse.getCountry().getPopulation() == 46704314);
    assertTrue(getCountryResponse.getCountry().getCapital().equals("Madrid"));
    assertTrue(getCountryResponse.getCountry().getCurrency().value().equals("EUR"));
  }
}
