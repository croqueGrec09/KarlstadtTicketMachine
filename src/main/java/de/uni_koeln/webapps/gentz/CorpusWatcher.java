package de.uni_koeln.webapps.gentz;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

/**
 * This is a watcher class keeping track all changes in the directory where the letter 
 * transcripts are being stored and triggering reindexing upon change. Currently 
 * only working locally.
 * 
 * @author agalffy
 *
 */
public class CorpusWatcher implements Runnable {
	
	private final WatchService watcher;
	private final Path dir;
	private GentzTEICorpus gts;
	private GentzIndexerService gis;
	
	CorpusWatcher(GentzTEICorpus gts, GentzIndexerService gis) throws IOException{
		this.gts = gts;
		this.gis = gis;
		this.watcher = FileSystems.getDefault().newWatchService();
		dir = Paths.get("gentz");
		dir.register(watcher, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);
	}
	
	void watch() {
		while(true) {
			WatchKey key;
			try {
				key = watcher.take();
			}
			catch (InterruptedException e) {
				return;
			}
			for(WatchEvent<?> event: key.pollEvents()) {
				WatchEvent.Kind kind = event.kind();
				if(kind == OVERFLOW)
					continue;
				else if(kind == ENTRY_CREATE) {
					WatchEvent<Path> ev = (WatchEvent<Path>) event;
					Path filename = ev.context();
					try {
						Path child = dir.resolve(filename);
						if(!Files.probeContentType(child).equals("text/xml") && child != null) {
							System.err.format("New file '%s' is not an XML document",filename);
							continue;
						}
						if(filename.toString().matches("LETTER_GENTZ_IDNO_[0-9]{4}[.]xml")) {
							File xmlFile = child.toFile();
							System.out.println("Index modification detected, adding file "+child.getFileName()+" to index");
							gts.addFileToCorpus(xmlFile);
							gis.init();
						}
					}
					catch (IOException e) {
						System.err.println(e);
						continue;
					}
				}
				else if(kind == ENTRY_DELETE) {
					WatchEvent<Path> ev = (WatchEvent<Path>) event;
					Path filename = ev.context();
					if(filename.toString().matches("LETTER_GENTZ_IDNO_[0-9]{4}[.]xml")) {
						System.out.println("File "+filename.toString()+" has been removed from index");
						String xmlId = filename.toString().replace("_IDNO","").split(".xml")[0];
						gts.deleteFileFromCorpus(xmlId);
						try {
							gis.init();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
				else if(kind == ENTRY_MODIFY) {
					WatchEvent<Path> ev = (WatchEvent<Path>) event;
					Path filename = ev.context();
					if(filename.toString().matches("LETTER_GENTZ_IDNO_[0-9]{4}[.]xml")) {
						System.out.println("File "+filename.toString()+" has been modified, recreating index entry");
						String xmlId = filename.toString().replace("_IDNO","").split(".xml")[0];
						gts.deleteFileFromCorpus(xmlId);
						Path child = dir.resolve(filename);
						File xmlFile = child.toFile();
						gts.addFileToCorpus(xmlFile);
						try {
							gis.init();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
			boolean valid = key.reset();
			if(!valid)
				break;
		}
	}

	@Override
	public void run() {
		System.out.println("Thread 2 running");
		watch();
	}
}
