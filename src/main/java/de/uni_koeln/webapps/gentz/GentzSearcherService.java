package de.uni_koeln.webapps.gentz;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Searcher service for the search within the document corpus. It processes a so-called 
 * "grocery list" which consists of all fields to be searched with their respective 
 * parameters.
 * @author agalffy
 *
 */
@Service
public class GentzSearcherService {

	@Autowired
	GentzTEICorpus gts;
	
	/**
	 * Retrieve a subset of the corpus which fulfills the given search criteria.
	 * 
	 * @param searchParams - the map of all parameters and fields to be queried
	 * @param andConnected - set all of the fields or ony one of them must match
	 * @return a set of letters matching the search criteria
	 */
	public Set<GentzLetter> search(Map<String,String> searchParams, boolean andConnected) throws IOException, ParseException{
		String indexDir = "index";
		Directory dir = new NIOFSDirectory(new File(indexDir).toPath());
		DirectoryReader dirReader = DirectoryReader.open(dir);
		/*
		 * We fight against him, by conveying educational content for 
		 * orientation-less young people. But here, it is not meant, the
		 * Islamic State, but the IndexSearcher.
		 */
		IndexSearcher is = new IndexSearcher(dirReader);
		Set<GentzLetter> result = new HashSet<>();
		StandardAnalyzer analyser = new StandardAnalyzer();
		List<Query> groceryList = new ArrayList<>();
		//for each field its own query with attached parser
		for(String searchParam: searchParams.keySet()) {
			if(searchParams.get(searchParam) != null && !searchParams.get(searchParam).isEmpty()
					&& !searchParam.equals("dateFrom") && !searchParam.equals("dateTo")) {
				QueryParser parser = new QueryParser(searchParam, analyser);
				groceryList.add(parser.parse(searchParams.get(searchParam)));
			}
		}
		BooleanQuery.Builder qb = null;
		for(Query q: groceryList) {
			if(andConnected)
				qb = new BooleanQuery.Builder().add(q, BooleanClause.Occur.MUST);
			else
				qb = new BooleanQuery.Builder().add(q, BooleanClause.Occur.SHOULD);
		}
		if(!andConnected && qb != null)
			qb.setMinimumNumberShouldMatch(1);
		if(!groceryList.isEmpty() && qb != null) {
			BooleanQuery query = qb.build();
			System.out.println("Gesucht wird: "+query);
			TopDocs hits = is.search(query, dirReader.numDocs());
			for(int r = 0; r < hits.scoreDocs.length; r++) {
				ScoreDoc scoreDoc = hits.scoreDocs[r];
				Document doc = is.doc(scoreDoc.doc);
				LocalDate dateFrom = LocalDate.parse("1784-10-08", DateTimeFormatter.ISO_DATE);
				LocalDate dateTo = LocalDate.parse("1802-12-31", DateTimeFormatter.ISO_DATE);
				if(searchParams.get("dateFrom") != null) {
					dateFrom = LocalDate.parse(searchParams.get("dateFrom"),DateTimeFormatter.ISO_DATE);
				}
				if(searchParams.get("dateTo") != null) {
					dateTo = LocalDate.parse(searchParams.get("dateTo"),DateTimeFormatter.ISO_DATE);
				}
				GentzLetter candidateLetter = gts.getCorpus().get(doc.get("id"));
				if(candidateLetter.getDateObj().isAfter(dateFrom) && 
					candidateLetter.getDateObj().isBefore(dateTo))
					result.add(candidateLetter);
			}
		}
		else {
			//search only for a limited date range
			LocalDate dateFrom = LocalDate.parse("1784-10-07", DateTimeFormatter.ISO_DATE);
			LocalDate dateTo = LocalDate.parse("1803-01-01", DateTimeFormatter.ISO_DATE);
			if(searchParams.get("dateFrom") != null) {
				dateFrom = LocalDate.parse(searchParams.get("dateFrom"),DateTimeFormatter.ISO_DATE);
			}
			if(searchParams.get("dateTo") != null) {
				dateTo = LocalDate.parse(searchParams.get("dateTo"),DateTimeFormatter.ISO_DATE);
			}
			for(GentzLetter candidateLetter: gts.getLetters()) {
				if(candidateLetter.getDateObj().isAfter(dateFrom) && 
					candidateLetter.getDateObj().isBefore(dateTo))
					result.add(candidateLetter);
			}
		}
		dirReader.close();
		return result;
	}
	
}
