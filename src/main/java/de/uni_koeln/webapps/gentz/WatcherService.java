package de.uni_koeln.webapps.gentz;

import java.io.IOException;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

/**
 * This is the service for triggering the CorpusWatcher, so that the second thread gets 
 * running when the Spring webapp is being set up.
 * 
 * @author agalffy
 *
 */
@Service
public class WatcherService {

	@Autowired
	private TaskExecutor taskExecutor;
	@Autowired
	private GentzTEICorpus gts;
	@Autowired
	private GentzIndexerService gis;
	
	@PostConstruct
	public void execute() {
		try {
			CorpusWatcher w = new CorpusWatcher(gts,gis);
			if(gts != null && gis != null)
				taskExecutor.execute(w);
			else {
				System.err.println("No TEI-Corpus object defined!");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
