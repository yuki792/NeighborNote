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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import com.evernote.edam.error.EDAMNotFoundException;
import com.evernote.edam.error.EDAMSystemException;
import com.evernote.edam.error.EDAMUserException;
import com.evernote.edam.limits.Constants;
import com.evernote.edam.notestore.RelatedQuery;
import com.evernote.edam.notestore.RelatedResult;
import com.evernote.edam.notestore.RelatedResultSpec;
import com.evernote.edam.type.Note;
import com.evernote.thrift.TException;
import com.trolltech.qt.core.QMutex;
import com.trolltech.qt.core.QObject;

import cx.fbn.nevernote.Global;
import cx.fbn.nevernote.signals.ENRelatedNotesSignal;
import cx.fbn.nevernote.utilities.Pair;

public class ENRelatedNotesRunner extends QObject implements Runnable{
	private final SyncRunner syncRunner;
	public volatile ENRelatedNotesSignal enRelatedNotesSignal;
	public QMutex mutex;
	private volatile boolean keepRunning;
	private volatile LinkedBlockingQueue<String> workQueue;
	private volatile LinkedBlockingQueue<Pair<String, List<String>>> resultQueue;	// ペア<元ノートguid, 関連ノートguidリスト>を溜めておくキュー
	
	public ENRelatedNotesRunner(SyncRunner syncRunner) {
		this.syncRunner = syncRunner;
		this.enRelatedNotesSignal = new ENRelatedNotesSignal();
		this.mutex = new QMutex();
		this.keepRunning = true;
		this.workQueue = new LinkedBlockingQueue<String>();
		this.resultQueue = new LinkedBlockingQueue<Pair<String, List<String>>>();
	}

	@Override
	public void run() {
		thread().setPriority(Thread.MIN_PRIORITY);
		
		while (keepRunning) {
			try {
				String work = workQueue.take();
				mutex.lock();
				if (work.startsWith("GET")) {
					String guid = work.replace("GET ", "");
					
					List<Note> relatedNotes = getENRelatedNotes(guid);
					
					Pair<String, List<String>> resultPair = new Pair<String, List<String>>();
					resultPair.setFirst(guid);
					if (relatedNotes == null) {				// 取得に失敗
					} else if (relatedNotes.isEmpty()) {	// このノートにEvernote関連ノートは存在しない
						resultPair.setSecond(new ArrayList<String>());
					} else {								// Evernote関連ノートが存在する
						List<String> relatedNoteGuids = new ArrayList<String>();
						for (Note relatedNote : relatedNotes) {
							relatedNoteGuids.add(relatedNote.getGuid());
						}
						resultPair.setSecond(relatedNoteGuids);
					}
					
					resultQueue.offer(resultPair);
					enRelatedNotesSignal.getENRelatedNotesFinished.emit();
				}
				if (work.startsWith("STOP")) {
					keepRunning = false;
				}
				mutex.unlock();
			} catch (InterruptedException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}
		}
	}
	
	private List<Note> getENRelatedNotes(String guid) {
		RelatedResult result = getENRelatedResult(guid);
		List<Note> relatedNotes = new ArrayList<Note>();
		
		if (result != null) {
			relatedNotes = result.getNotes();
			return relatedNotes;
		}
		
		return null;
	}
	
	private RelatedResult getENRelatedResult(String guid) {
		if (!Global.isConnected) {
			return null;
		}
		
		RelatedQuery rquery = new RelatedQuery();
		rquery.setNoteGuid(guid);
		RelatedResultSpec resultSpec = new RelatedResultSpec();
		resultSpec.setMaxNotes(Constants.EDAM_RELATED_MAX_NOTES);
		if (syncRunner != null && syncRunner.localNoteStore != null) {
			try {
				RelatedResult result = syncRunner.localNoteStore.findRelated(syncRunner.authToken, rquery, resultSpec);
				return result;
			} catch (EDAMUserException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			} catch (EDAMSystemException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			} catch (EDAMNotFoundException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			} catch (TException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}
		}
		return null;
	}

	public boolean isKeepRunning() {
		return keepRunning;
	}

	public void setKeepRunning(boolean keepRunning) {
		this.keepRunning = keepRunning;
	}
	
	public synchronized boolean addGuid(String guid) {
		if (workQueue.offer("GET " + guid)) {
			return true;
		}
		
		return false;
	}
	
	public synchronized boolean addStop() {
		if (workQueue.offer("STOP")) {
			return true;
		}
		return false;
	}
	
	public synchronized Pair<String, List<String>> getENRelatedNoteGuids() {
		try {
			return resultQueue.take();
		} catch (InterruptedException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		
		return null;
	}
}
