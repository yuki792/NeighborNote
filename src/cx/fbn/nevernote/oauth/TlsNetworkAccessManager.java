package cx.fbn.nevernote.oauth;

import com.trolltech.qt.core.QIODevice;
import com.trolltech.qt.core.QObject;
import com.trolltech.qt.network.QNetworkAccessManager;
import com.trolltech.qt.network.QNetworkReply;
import com.trolltech.qt.network.QNetworkRequest;
import com.trolltech.qt.network.QSsl.SslProtocol;
import com.trolltech.qt.network.QSslConfiguration;

import cx.fbn.nevernote.utilities.ApplicationLogger;

public class TlsNetworkAccessManager extends QNetworkAccessManager {
	private final ApplicationLogger logger;

	public TlsNetworkAccessManager(ApplicationLogger logger) {
		super();
		this.logger = logger;
	}

	public TlsNetworkAccessManager(QObject parent, ApplicationLogger logger) {
		super(parent);
		this.logger = logger;
	}
	
	@Override
	protected QNetworkReply createRequest(Operation op, QNetworkRequest request, QIODevice outgoingData) {
		logger.log(logger.EXTREME, "TlsNetworkAccessManager URL request scheme: " + request.url().scheme() + " " + request.url().toString());
		
		// Force to use TLSv1
		QSslConfiguration sslConfig = request.sslConfiguration();
		sslConfig.setProtocol(SslProtocol.TlsV1);
		request.setSslConfiguration(sslConfig);
		QNetworkReply reply = super.createRequest(op, request, outgoingData);
		reply.sslErrors.connect(reply, "ignoreSslErrors()");
		
		return reply;
	}
}
