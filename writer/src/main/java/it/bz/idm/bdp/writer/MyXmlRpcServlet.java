package it.bz.idm.bdp.writer;
import java.io.IOException;
import java.net.URL;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.metadata.XmlRpcSystemImpl;
import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcHandlerMapping;
import org.apache.xmlrpc.webserver.XmlRpcServlet;

public class MyXmlRpcServlet extends XmlRpcServlet{

	private static final long serialVersionUID = 1L;

	@Override
	protected XmlRpcHandlerMapping newXmlRpcHandlerMapping()
			throws XmlRpcException {
		URL url = MyXmlRpcServlet.class.getClassLoader().getResource("XmlRpcServlet.properties");
		if (url == null) {
			throw new XmlRpcException("Failed to locate resource XmlRpcServlet.properties");
		}
		try {
			PropertyHandlerMapping newPropertyHandlerMapping = newPropertyHandlerMapping(url);
			XmlRpcSystemImpl.addSystemHandler(newPropertyHandlerMapping);
			return newPropertyHandlerMapping;
		} catch (IOException e) {
			throw new XmlRpcException("Failed to load resource " + url + ": " + e.getMessage(), e);
		}
	}
}