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

import cx.fbn.nevernote.signals.ENRelatedNotesSignal;

public class ENRelatedNotesRunner extends QObject implements Runnable{
	private final SyncRunner syncRunner;
//	public volatile Signal0 getENRelatedNotesFinished;
	public volatile ENRelatedNotesSignal ENRelatedNotesSignal;
//	private List<String> relatedNoteGuids;
	public QMutex mutex;
	private volatile boolean keepRunning;
	private volatile LinkedBlockingQueue<String> guidQueue;
	private volatile LinkedBlockingQueue<List<String>> resultQueue;
	
	public ENRelatedNotesRunner(SyncRunner syncRunner) {
		this.syncRunner = syncRunner;
//		this.relatedNoteGuids = new ArrayList<String>();
//		this.getENRelatedNotesFinished = new Signal0();
		this.ENRelatedNotesSignal = new ENRelatedNotesSignal();
		this.mutex = new QMutex();
		this.keepRunning = true;
		this.guidQueue = new LinkedBlockingQueue<String>();
		this.resultQueue = new LinkedBlockingQueue<List<String>>();
	}

	@Override
	public void run() {
		thread().setPriority(Thread.MIN_PRIORITY);
		
		while (keepRunning) {
			try {
				String guid = guidQueue.take();
				List<String> relatedNoteGuids = new ArrayList<String>();
				mutex.lock();
			
				List<Note> relatedNotes = getENRelatedNotes(guid);
				if (relatedNotes != null && !relatedNotes.isEmpty()) {
					for (Note relatedNote : relatedNotes) {
						relatedNoteGuids.add(relatedNote.getGuid());
					}
				}
				
				resultQueue.offer(relatedNoteGuids);
				ENRelatedNotesSignal.getENRelatedNotesFinished.emit();
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
		}
		
		return relatedNotes;
	}
	
	private RelatedResult getENRelatedResult(String guid) {
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
		if (guidQueue.offer(guid)) {
			return true;
		}
		
		return false;
	}
	
	public synchronized List<String> getENRelatedNoteGuids() {
		try {
			return resultQueue.take();
		} catch (InterruptedException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		
		return null;
	}
}
