package de.uni_koeln.webapps.gentz;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Processes the given documents for storing in the Lucene index. This class builds thus the 
 * index so that the letters are queryable in an easy way.
 * 
 * @author agalffy
 *
 * @see GentzLetter
 * @see GentzSearcherService
 */
@Service
public class GentzIndexerService {

	@Autowired
	private GentzTEICorpus gts;
	
	/**
	 * Builds up the letter index.
	 *
	 */
	@PostConstruct
	public void init() throws IOException{
		String indexDir = "index";
		Directory dir = new NIOFSDirectory(new File(indexDir).toPath());
		IndexWriterConfig writerConfig = new IndexWriterConfig(new StandardAnalyzer());
		IndexWriter writer = new IndexWriter(dir, writerConfig);
		writer.deleteAll();
		System.out.println("building Gentz index ...");
		List<GentzLetter> letters = gts.getLetters();
		for(GentzLetter letter: letters){
			Document doc = convertToLuceneDoc(letter);
			writer.addDocument(doc);
		}
		System.out.println("index size: "+letters.size()+". Not enough.");
		writer.close();
	}

	/**
	 * Converts a given letter into a Lucene {@link Document}.
	 * 
	 * @param letter - the given letter
	 * @return its conversion into a Lucene {@link Document}.
	 */
	private Document convertToLuceneDoc(GentzLetter letter) {
		Document doc = new Document();
		doc.add(new TextField("id",letter.getId(),Store.YES));
		doc.add(new TextField("title",letter.getTitle(),Store.YES));
		for(GentzEntity ge: letter.getListPerson().get()){
			if(ge instanceof GentzPerson) {
				GentzPerson person = (GentzPerson) ge;
				String role = person.getRole();
				if(role.equals("named"))
					role = "person";
				doc.add(new TextField(role,person.getFullName(true).toLowerCase(),Store.YES));
				doc.add(new TextField(role,person.getForename().toLowerCase(),Store.YES));
				doc.add(new TextField(role,person.getSurname().toLowerCase(),Store.YES));
			}
		}
		for(GentzEntity ge: letter.getListPlace().get()){
			if(ge instanceof GentzPlace) {
				GentzPlace place = (GentzPlace) ge;
				switch(place.getRole()) {
				case "sender": doc.add(new TextField("sender",place.getPlaceName().toLowerCase(),Store.YES));
				break;
				case "receiver": doc.add(new TextField("receiver",place.getPlaceName().toLowerCase(),Store.YES));
				break;
				default: doc.add(new TextField("place",place.getPlaceName(),Store.YES));
				break;
				}
			}
		}
		doc.add(new TextField("content",letter.getSearchableContent(),Store.YES));
		for(String keyword: letter.getKeywords()){
			doc.add(new TextField("keyword",keyword,Store.YES));
		}
		for(String category: letter.getCategories().values()) {
			if(gts.getCatEnum().getKey(category) != null)
				doc.add(new TextField("category",gts.getCatEnum().getKey(category),Store.YES));
			else System.out.println(letter.getId()+": "+category);
		}
		return doc;
	}
	
}
