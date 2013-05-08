package cx.fbn.nevernote.gui;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.evernote.edam.type.User;
import com.trolltech.qt.core.QBuffer;
import com.trolltech.qt.core.QByteArray;
import com.trolltech.qt.core.QIODevice;
import com.trolltech.qt.core.QMutex;
import com.trolltech.qt.core.QObject;
import com.trolltech.qt.core.QSize;
import com.trolltech.qt.core.Qt.AspectRatioMode;
import com.trolltech.qt.core.Qt.Orientation;
import com.trolltech.qt.core.Qt.ScrollBarPolicy;
import com.trolltech.qt.core.Qt.TransformationMode;
import com.trolltech.qt.gui.QImage;
import com.trolltech.qt.gui.QImage.Format;
import com.trolltech.qt.gui.QPainter;
import com.trolltech.qt.webkit.QWebPage;

import cx.fbn.nevernote.Global;
import cx.fbn.nevernote.oauth.OAuthTokenizer;
import cx.fbn.nevernote.sql.DatabaseConnection;
import cx.fbn.nevernote.threads.ThumbnailRunner;
import cx.fbn.nevernote.utilities.AESEncrypter;
import cx.fbn.nevernote.utilities.ApplicationLogger;
import cx.fbn.nevernote.utilities.ListManager;

public class Thumbnailer extends QObject {
    public QWebPage page = new QWebPage();
    public QImage image;
    public QPainter painter = new QPainter();
    public QMutex mutex;
    public String guid;
    private final QSize size;
    private int zoom;
    private QBuffer buffer;
    private final ApplicationLogger logger;
    private final DatabaseConnection conn;
    
    public Thumbnailer(ApplicationLogger logger, DatabaseConnection conn, ListManager l, ThumbnailRunner r)
    {
    	mutex = new QMutex();
    	zoom = 1;

    	this.logger = logger;
    	page = new QWebPage();
    	this.conn = conn;
        size = new QSize(1024,768);
//        size = new QSize();
//        size.setWidth(Global.largeThumbnailSize.width());
//        size.setHeight(Global.largeThumbnailSize.height());
        image = new QImage(size, Format.Format_ARGB32_Premultiplied);
    	page.setViewportSize(size);
    	page.loadFinished.connect(this, "loadFinished(Boolean)");
    	buffer = new QBuffer();
        mutex.unlock();
    }
    
    public void loadContent(String guid, QByteArray html, int zoom) {
    	this.zoom = zoom;
    	this.guid = guid;
    	page.mainFrame().setScrollBarPolicy(Orientation.Horizontal, ScrollBarPolicy.ScrollBarAlwaysOff);
    	page.mainFrame().setScrollBarPolicy(Orientation.Vertical, ScrollBarPolicy.ScrollBarAlwaysOff);
    	page.mainFrame().setContent(html);
    }

    
	@SuppressWarnings("unused")
	private String loadFinished(Boolean ok) {
		if (!ok) { 
			mutex.unlock();
			return null;
		}
//		page.setViewportSize(page.mainFrame().contentsSize());
//		image = new QImage(size, Format.Format_ARGB32);
		logger.log(logger.EXTREME, "Creating painter");
		painter = new QPainter();
		logger.log(logger.EXTREME, "Beginning painter");
        painter.begin(image);
        page.mainFrame().setZoomFactor(new Double(zoom));
    	if (painter.paintEngine() == null) {
    		logger.log(logger.EXTREME, "Bad paint engine.  Aborting");
    		painter.end();
			mutex.unlock();
			return null;
    	}
		logger.log(logger.EXTREME, "Rendering image");
        page.mainFrame().render(painter);
		logger.log(logger.EXTREME, "Closing painter");
        painter.end();
        if (image.isNull()) {
        	logger.log(logger.EXTREME, "Error rendering thumbnail image.  Aborting");
        	mutex.unlock();
        	return null;
        }
		logger.log(logger.EXTREME, "Saving image isNull=" +image.isNull() +" Size=" +image.size());
		logger.log(logger.EXTREME, "Adding image to runner");
		saveImage();
		logger.log(logger.EXTREME, "Unlocking thumbnailer");
        mutex.unlock();
        return guid;
    }
	
	
	private void saveImage() {
		logger.log(logger.EXTREME, "Image found "+guid);
				
		logger.log(logger.EXTREME, "Opening buffer");
		if (buffer == null)
			buffer = new QBuffer();
        if (!buffer.open(QIODevice.OpenModeFlag.ReadWrite)) {
        	logger.log(logger.EXTREME, "Failure to open buffer.  Aborting.");
        	mutex.unlock();
        	return;
        }
	        
		logger.log(logger.EXTREME, "Filling buffer");
		QImage img = image.scaled(Global.largeThumbnailSize,
				AspectRatioMode.KeepAspectRatio, TransformationMode.SmoothTransformation);
        if (!img.save(buffer, "PNG")) {
        	logger.log(logger.EXTREME, "Failure to write to buffer.  Aborting.");	  
        	mutex.unlock();
        	return;
        }
        buffer.close();
	        
		logger.log(logger.EXTREME, "Updating database");
		QByteArray b = buffer.buffer();
		conn.getNoteTable().setThumbnail(guid, b);
		conn.getNoteTable().setThumbnailNeeded(guid, false);
		
		// サムネイルをEvernoteサーバから取得
		User user = Global.getUserInformation();
		String serverUrl = Global.getServer();
		String shardId = user.getShardId();
		if (shardId == null || shardId.equals("")) {
			return;
		}
		
		OAuthTokenizer tokenizer = new OAuthTokenizer();
    	AESEncrypter aes = new AESEncrypter();
    	try {
			aes.decrypt(new FileInputStream(Global.getFileManager().getHomeDirFile("oauth.txt")));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		String authString = aes.getString();
		String oauthToken = new String();
		if (!authString.equals("")) {
			tokenizer.tokenize(authString);
			oauthToken = tokenizer.oauth_token;
		}
		
		HttpClient httpClient = new DefaultHttpClient();

		HttpPost httpPost = new HttpPost("https://" + serverUrl + "/shard/" + user.getShardId() + "/thm/note/" + guid);
		httpPost.setHeader("Content-type", "application/x-www-form-urlencoded");
		httpPost.setHeader("Host", serverUrl);

		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
		nameValuePairs.add(new BasicNameValuePair("auth", oauthToken));
		nameValuePairs.add(new BasicNameValuePair("size", "80"));
		
		try {
			httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			// Webサーバからのレスポンスを処理
			HttpResponse response = null;
			response = httpClient.execute(httpPost);
			byte[] bytes = EntityUtils.toByteArray(response.getEntity());
			QByteArray data = new QByteArray(bytes);
			// データベースにEvernoteサーバから取得したサムネイルを保存。例↓
			// conn.getNoteTable().setThumbnail(guid, data);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		httpClient.getConnectionManager().shutdown();
	}
}
