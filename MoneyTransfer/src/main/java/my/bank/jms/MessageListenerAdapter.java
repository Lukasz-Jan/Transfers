package my.bank.jms;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;
import my.bank.services.IncomeService;
import my.bank.services.ResponseService;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import my.bank.gen.xsd.mappings.transfer.OutcomeType;
import my.bank.gen.xsd.mappings.transfer.TransferRequestType;

@Component
public class MessageListenerAdapter implements MessageListener {

	private static final Logger logger = LoggerFactory.getLogger(MessageListenerAdapter.class);
	private Schema requestResponseSchema;

	private IncomeService reqSrv;
	private ResponseService respSrv;

	@Autowired
	public MessageListenerAdapter(IncomeService reqSrv, ResponseService respSrv, @Value("${messageXsd}") String requestResponseXsdPath) {

		this.reqSrv = reqSrv;
		this.respSrv = respSrv;

		SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		StreamSource source = null;

		try(InputStream inputStream = new FileInputStream(requestResponseXsdPath)) {
			
			source = new StreamSource(	new InputStreamReader(inputStream, StandardCharsets.UTF_8)	);
			requestResponseSchema = schemaFactory.newSchema(source);
		} catch (IOException | SAXException e2) {
			
			logger.info("IO exception while xsd reading");
		}
	}

	@JmsListener(destination = "${srcQueue}")
	@Override
	public void onMessage(Message mss) {

		if (mss instanceof TextMessage) {

			TextMessage textXmlMsg = (TextMessage) mss;
			TransferRequestType requestInstance = handleMessage(textXmlMsg);

			if (requestInstance != null) {

				logger.info("Request " + requestInstance.getRequestId() + " being processed");
				OutcomeType outcome = reqSrv.processRequest(requestInstance);
				respSrv.sendResponseMessage(requestInstance, outcome);
			} else
				logger.info("errors occured for request");
		}
	}

	private TransferRequestType handleMessage(TextMessage jmsTxtMsg) {

		TransferRequestType requestInstance = null;
		JAXBElement<TransferRequestType> jaxEl = null;
		InputStream inputStream = null;
		XMLStreamReader reader = null;

		try {
			String requestXmlStr = jmsTxtMsg.getText();
			inputStream = new ByteArrayInputStream(requestXmlStr.getBytes());

			JAXBContext ctx = JAXBContext.newInstance(TransferRequestType.class);
			Unmarshaller u = ctx.createUnmarshaller();
			u.setSchema(requestResponseSchema);

			final XMLInputFactory xif = XMLInputFactory.newInstance();

			reader = xif.createXMLStreamReader(inputStream);
			jaxEl = u.unmarshal(reader, TransferRequestType.class);
			requestInstance = jaxEl.getValue();

		} catch (JMSException | JAXBException | XMLStreamException e) {

			logger.info("Parsing Exception caught, incoming message not processed");
			logger.error(e.toString());
		} finally {

			try {
				inputStream.close();
				reader.close();
			} catch (IOException | XMLStreamException e) {
				logger.error(e.toString());
				return null;
			}
		}
		return requestInstance;
	}
}