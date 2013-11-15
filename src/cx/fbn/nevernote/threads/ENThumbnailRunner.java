/*
 * This file is part of NeighborNote
 * Copyright 2013 Yuki Takahashi
 * 
 * This file may be licensed under the terms of of the
 * GNU General Public License Version 2 (the ``GPL'').
 *
 * Software distributed under the License is distributed
 * on an ``AS IS'' basis, WITHOUT WARRANTY OF ANY KIND, either
 * express or implied. See the GPL for the specific language
 * governing rights and limitations.
 *
 * You should have received a copy of the GPL along with this
 * program. If not, go to http://www.gnu.org/licenses/gpl.html
 * or write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
*/

package cx.fbn.nevernote.threads;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

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
import com.trolltech.qt.core.QByteArray;
import com.trolltech.qt.core.QMutex;
import com.trolltech.qt.core.QObject;
import com.trolltech.qt.gui.QPixmap;

import cx.fbn.nevernote.Global;
import cx.fbn.nevernote.oauth.OAuthTokenizer;
import cx.fbn.nevernote.signals.ENThumbnailSignal;
import cx.fbn.nevernote.signals.LimitSignal;
import cx.fbn.nevernote.sql.DatabaseConnection;
import cx.fbn.nevernote.utilities.AESEncrypter;
import cx.fbn.nevernote.utilities.ApplicationLogger;

public class ENThumbnailRunner extends QObject implements Runnable{
	
	private final ApplicationLogger					logger;
	private final DatabaseConnection				conn;
	public volatile ENThumbnailSignal				enThumbnailSignal;
	public QMutex									mutex;
	private volatile boolean						keepRunning;
	private volatile LinkedBlockingQueue<String>	workQueue;
	public volatile LimitSignal 					limitSignal;
	private volatile User							user;
	private volatile String							serverUrl;
	
	public ENThumbnailRunner(String logname, int t, String u, String i, String r, String b, String uid, String pswd, String cpswd) {
		this.logger = new ApplicationLogger(logname);
		this.conn = new DatabaseConnection(logger, u, i, r, b, uid, pswd, cpswd, 0);
		this.enThumbnailSignal = new ENThumbnailSignal();
		this.mutex = new QMutex();
		this.keepRunning = true;
		this.workQueue = new LinkedBlockingQueue<String>();
		this.limitSignal = new LimitSignal();
		this.user = new User();
		this.serverUrl = "";
	}

	@Override
	public void run() {
		thread().setPriority(Thread.MIN_PRIORITY);
		
		logger.log(logger.MEDIUM, "ENThumbnailスレッド開始");
		while (keepRunning) {
			try {
				String work = workQueue.take();
				mutex.lock();
				if (work.startsWith("GET")) {
					String guid = work.replace("GET ", "");
					logger.log(logger.EXTREME, "Evernoteサムネイル取得開始 guid = " + guid);
					
					QByteArray thumbnailData = getENThumbnailData(guid);
					if (thumbnailData == null) {				// 取得に失敗
						logger.log(logger.EXTREME, "Evernoteサムネイルの取得に失敗");
					} else {
						QPixmap thumbnail_p = new QPixmap();
						thumbnail_p.loadFromData(thumbnailData);
						logger.log(logger.EXTREME, "Evernoteサムネイルの取得に成功");
						saveImage(thumbnail_p, guid);
						registImage(thumbnailData, guid);
					}
					
					enThumbnailSignal.getENThumbnailFinished.emit(guid);
					logger.log(logger.EXTREME, "Evernoteサムネイル取得完了 guid = " + guid);
				} else if (work.startsWith("STOP")) {
					logger.log(logger.MEDIUM, "ENThumbnailスレッド停止");
					keepRunning = false;
				}
				mutex.unlock();
			} catch (InterruptedException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}
		}
	}
	
	// Evernoteサーバからサムネイルを取得
	private synchronized QByteArray getENThumbnailData(String guid) {
		// サムネイルをEvernoteサーバから取得
		String shardId = user.getShardId();
		if (shardId == null || shardId.equals("")) {
			return null;
		}
		
		OAuthTokenizer tokenizer = new OAuthTokenizer();
    	AESEncrypter aes = new AESEncrypter();
    	try {
			aes.decrypt(new FileInputStream(Global.getFileManager().getHomeDirFile("oauthkey.txt")));
		} catch (FileNotFoundException e) {
			logger.log(logger.HIGH, "Evernoteサムネイル取得中に例外発生：FileNotFoundException");
			e.printStackTrace();
			return null;
		}
		String authString = aes.getString();
		String oauthToken = new String();
		if (!authString.equals("")) {
			tokenizer.tokenize(authString);
			oauthToken = tokenizer.oauth_token;
		}
		
		HttpClient httpClient = new DefaultHttpClient();

		HttpPost httpPost = new HttpPost("https://" + serverUrl + "/shard/" + user.getShardId() + "/thm/note/" + guid + ".png");
		httpPost.setHeader("Content-type", "application/x-www-form-urlencoded");
		httpPost.setHeader("Host", getServerUrl());

		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
		nameValuePairs.add(new BasicNameValuePair("auth", oauthToken));
		nameValuePairs.add(new BasicNameValuePair("size", "80"));
		
		QByteArray data = new QByteArray();
		try {
			httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			// Webサーバからのレスポンスを処理
			HttpResponse response = null;
			response = httpClient.execute(httpPost);
			byte[] bytes = EntityUtils.toByteArray(response.getEntity());
			data = new QByteArray(bytes);
		} catch (UnsupportedEncodingException e) {
			logger.log(logger.HIGH, "Evernoteサムネイル取得中に例外発生：UnsupportedEncodingException");
			e.printStackTrace();
			return null;
		} catch (ClientProtocolException e) {
			logger.log(logger.HIGH, "Evernoteサムネイル取得中に例外発生：ClientProtocolException");
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			logger.log(logger.HIGH, "Evernoteサムネイル取得中に例外発生：IOException");
			e.printStackTrace();
			return null;
		} finally {
			httpClient.getConnectionManager().shutdown();
		}

		return data;
	}
	
	// サムネイルをpng形式のファイルとしてresディレクトリに保存
	private synchronized void saveImage(QPixmap thumbnail, String guid) {
		String thumbnailName = Global.getFileManager().getResDirPath("enThumbnail-" + guid + ".png");
		thumbnail.save(thumbnailName, "PNG");
	}
	
	// サムネイルのバイナリデータをデータベースに登録
	private synchronized void registImage(QByteArray data, String guid) {
		conn.getNoteTable().setENThumbnail(guid, data);
	}

	public boolean isKeepRunning() {
		return keepRunning;
	}

	public void setKeepRunning(boolean keepRunning) {
		this.keepRunning = keepRunning;
	}
	
	public boolean addGuid(String guid) {
		if (workQueue.offer("GET " + guid)) {
			return true;
		}
		
		return false;
	}
	
	public boolean addStop() {
		if (workQueue.offer("STOP")) {
			return true;
		}
		return false;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getServerUrl() {
		return serverUrl;
	}

	public void setServerUrl(String serverUrl) {
		this.serverUrl = serverUrl;
	}
}
