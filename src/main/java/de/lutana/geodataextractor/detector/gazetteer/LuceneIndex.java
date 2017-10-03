package de.lutana.geodataextractor.detector.gazetteer;

import de.lutana.geodataextractor.entity.Location;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.Term;
import org.apache.lucene.queries.CustomScoreProvider;
import org.apache.lucene.queries.CustomScoreQuery;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopFieldDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.automaton.TooComplexToDeterminizeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LuceneIndex {

	private LuceneAnalyzer analyzer;
	private File indexPath;
	private DirectoryReader indexReader;
	private IndexSearcher indexSearcher;
	private File countryCodeMappingPath;
	private Map<String,String> countryCodeMapping;

	/**
	 * Custom Lucene sorting based on Lucene match score and the importance of
	 * the OsmNames gazetteer entry represented by the matched index document.
	 */
	private static final Sort SORT = new Sort(SortField.FIELD_SCORE);

	public LuceneIndex() {
		this(new File("./OsmNamesIndex"));
	}

	public LuceneIndex(File indexFolder) {
		this.indexPath = indexFolder;
		this.countryCodeMappingPath = new File(indexFolder, "__countryCodeMapping.ser");
		this.analyzer = new LuceneAnalyzer();
		this.countryCodeMapping = null;
	}

	public boolean load() {
		if (indexSearcher != null) {
			return true;
		}
		try {
			if (!this.exists()) {
				this.create();
			}
			else {
				this.readCountryCodeMapping();
			}
			Directory index = FSDirectory.open(indexPath.toPath());
			indexReader = DirectoryReader.open(index);
			indexSearcher = new IndexSearcher(indexReader);
			return true;
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return false;
	}

	public boolean exists() {
		return indexPath.exists();
	}

	protected final void create() throws IOException {
		Logger l = LoggerFactory.getLogger(getClass());

		Directory index = FSDirectory.open(indexPath.toPath());
		IndexWriterConfig config = new IndexWriterConfig(analyzer);
		try (IndexWriter writer = new IndexWriter(index, config)) {
			l.debug("Indexing custom geonames...");
			InputStream stream = LuceneIndex.class.getClassLoader().getResourceAsStream("clavin/additional_data.tsv");
			OsmNamesReader readerCustom = new OsmNamesReader(stream);
			while(readerCustom.hasNext()) {
				writer.addDocument(this.makeDocument(readerCustom.next()));
			}
			readerCustom.close();

			OsmNamesReader reader = new OsmNamesReader();
			for (int i = 0; reader.hasNext(); i++) {
				if ((i % 100000) == 0) {
					l.debug("Indexed " + i + " geonames...");
				}

				writer.addDocument(this.makeDocument(reader.next()));
			}
			reader.close();

			l.debug("Merging Index...");
			writer.forceMerge(1);

			l.debug("Storing country code mapping...");
			this.writeCountryCodeMapping(reader);
		}
		l.debug("Indexing completed.");
	}
	
	protected Document makeDocument(GeoName gn) {
		Document doc = new Document();
		doc.add(new TextField("name", gn.getName(), Field.Store.YES));
		doc.add(new TextField("displayName", gn.getDisplayName(), Field.Store.YES));
		doc.add(new TextField("alternativeNames", String.join(",", gn.getAlternativeNames()), Field.Store.YES));
		doc.add(new StringField("osmId", gn.getOsmId(), Field.Store.YES));
		doc.add(new StoredField("featureClass", gn.getFeatureClass()));
		doc.add(new StoredField("type", gn.getType()));
		doc.add(new StoredField("placeRank", gn.getPlaceRank()));
		doc.add(new StoredField("importance", gn.getImportance()));
		doc.add(new TextField("city", gn.getCity(), Field.Store.YES));
		doc.add(new TextField("county", gn.getCounty(), Field.Store.YES));
		doc.add(new TextField("state", gn.getState(), Field.Store.YES));
		doc.add(new TextField("country", gn.getCountry(), Field.Store.YES));
		doc.add(new StringField("countryCode", gn.getCountryCode(), Field.Store.YES));
		Location location = gn.getLocation();
		doc.add(new StoredField("south", location.getMinY()));
		doc.add(new StoredField("north", location.getMaxY()));
		doc.add(new StoredField("west", location.getMinX()));
		doc.add(new StoredField("east", location.getMaxX()));
		return doc;
	}

	protected void writeCountryCodeMapping(OsmNamesReader reader) {
		this.countryCodeMapping = reader.getCountryCodeMapping();
		try (FileOutputStream fos = new FileOutputStream(this.countryCodeMappingPath); ObjectOutputStream oos = new ObjectOutputStream(fos)) {
			oos.writeObject(this.countryCodeMapping);
		} catch (IOException ex) {
			LoggerFactory.getLogger(getClass()).warn(ex.getMessage());
		}
	}

	protected void readCountryCodeMapping() {
		try (FileInputStream fis = new FileInputStream(this.countryCodeMappingPath); ObjectInputStream ois = new ObjectInputStream(fis)) {
			this.countryCodeMapping = (HashMap) ois.readObject();
		} catch (IOException ex) {
			LoggerFactory.getLogger(getClass()).warn(ex.getMessage());
		} catch (ClassNotFoundException ex) {}
	}

	public String getOsmIdForCountryCode(String iso2countryCode) {
		if(this.countryCodeMapping != null) {
			return this.countryCodeMapping.get(iso2countryCode.toUpperCase());
		}
		return null;
	}

	public int count() {
		if (indexReader == null) {
			LoggerFactory.getLogger(getClass()).warn("Index not initialized. Call LuceneIndex::load() and LuceneIndex::close() manually.");
			return -1;
		}
		return indexReader.numDocs();
	}

	public GeoName get(int id) {
		if (indexSearcher == null) {
			LoggerFactory.getLogger(getClass()).warn("Index not initialized. Call LuceneIndex::load() and LuceneIndex::close() manually.");
			return null;
		}

		try {
			Document doc = indexSearcher.doc(id);
			return this.toGeoName(doc);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	protected Query getLocationQuery(String locationName, String field, boolean fuzzy) {
		List<String> names = this.analyzer.parse(locationName);
		Query query;
		if (names.size() == 1) {
			locationName = names.get(0);
			Term nameTerm = new Term(field, locationName);
			if (fuzzy) {
				query = new FuzzyQuery(nameTerm, locationName.length() >= 10 ? 2 : 1);
			} else {
				query = new TermQuery(nameTerm);
			}
		} else {
			query = new PhraseQuery(field, names.toArray(new String[0]));
		}
		return query;
	}

	public List<GeoName> find(String locationName) {
		return this.find(locationName, false, 10);
	}

	public List<GeoName> find(String locationName, boolean fuzzyIfNoResults, int limitResults) {
		return this.find(locationName, fuzzyIfNoResults, limitResults, false);
	}

	private List<GeoName> find(String locationName, boolean fuzzyIfNoResults, int limitResults, boolean fuzzy) {
		List<GeoName> collection = new ArrayList<>();
		if (indexSearcher == null) {
			LoggerFactory.getLogger(getClass()).warn("Index not initialized. Call LuceneIndex::load() and LuceneIndex::close() manually.");
			return collection;
		}

		Query query = null;

		// Handle possible (uppercase) country codes specially
		if (locationName.length() == 2 && locationName.toUpperCase().equals(locationName)) {
			String osmId = this.getOsmIdForCountryCode(locationName);
			if (osmId != null) {
				Term term = new Term("osmId", osmId);
				query = new TermQuery(term);
			}
		}

		// If no specialized query kicks in...
		if (query == null) {
			Query nameQuery = this.getLocationQuery(locationName, "name", fuzzy);
			Query altNamesQuery = this.getLocationQuery(locationName, "alternativeNames", fuzzy);
			BooleanQuery.Builder bqBuilder = new BooleanQuery.Builder();
			bqBuilder.add(nameQuery, BooleanClause.Occur.SHOULD);
			bqBuilder.add(altNamesQuery, BooleanClause.Occur.SHOULD);
			BooleanQuery booleanQuery = bqBuilder.build();
			query = new ImportanceScoreQuery(booleanQuery);
		}

		try {
			TopFieldDocs docs = indexSearcher.search(query, limitResults, SORT, true, false);
			for (ScoreDoc entry : docs.scoreDocs) {
				GeoName geoname = this.get(entry.doc);
				if (geoname != null) {
					geoname.setResultScore(entry.score);
					collection.add(geoname);
				}
			}
		} catch (IOException | TooComplexToDeterminizeException ex) {
			ex.printStackTrace();
		}

		if (fuzzyIfNoResults && collection.isEmpty() && !fuzzy) {
			collection = this.find(locationName, fuzzyIfNoResults, limitResults, true);
		}

		return collection;
	}

	protected GeoName toGeoName(Document doc) {
		if (doc == null) {
			return null;
		}
		GeoName gn = new GeoName();
		gn.setName(doc.get("name"));
		gn.setDisplayName(doc.get("displayName"));
		String altNames = doc.get("alternativeNames");
		String[] altNamesArr = altNames.split(",");
		if (altNamesArr.length > 0 && !altNamesArr[0].isEmpty()) {
			gn.setAlternativeNames(altNamesArr);
		}
		gn.setOsmId(doc.get("osmId"));
		gn.setFeatureClass(doc.get("featureClass"));
		gn.setType(doc.get("type"));
		try {
			String placeRank = doc.get("placeRank");
			if (placeRank != null) {
				gn.setPlaceRank(Integer.parseInt(placeRank));
			}
		} catch (NumberFormatException e) {
		}
		try {
			String importance = doc.get("importance");
			if (importance != null) {
				gn.setImportance(Float.parseFloat(importance));
			}
		} catch (NumberFormatException e) {
		}
		gn.setCity(doc.get("city"));
		gn.setCounty(doc.get("county"));
		gn.setState(doc.get("state"));
		gn.setCountry(doc.get("country"));
		gn.setCountryCode(doc.get("countryCode"));
		try {
			String minLon = doc.get("west");
			String maxLon = doc.get("east");
			String minLat = doc.get("south");
			String maxLat = doc.get("north");
			if (minLon != null && maxLon != null && minLat != null && maxLat != null) {
				Location l = new Location(Double.parseDouble(minLon), Double.parseDouble(maxLon), Double.parseDouble(minLat), Double.parseDouble(maxLat));
				gn.setLocation(l);
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		return gn;
	}

	public void close() {
		try {
			if (indexReader != null) {
				indexReader.close();
				indexSearcher = null;
				indexReader = null;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private class ImportanceScoreProvider extends CustomScoreProvider {

		public ImportanceScoreProvider(LeafReaderContext context) {
			super(context);
		}

		@Override
		public float customScore(int doc, float subQueryScore, float valSrcScores[]) throws IOException {
			try {
				// subQueryScore is the default score you get from  the original Query
				Document currentDocument = context.reader().document(doc);

				// get the value of importtance field
				// make sure the two fields are stored since you have to retrieve the value
				float importance = Float.parseFloat(currentDocument.get("importance"));

				// ignore the valSrcScores here, the original calculation      
				// is modifiedScore = subQueryScore*valSrcScores[0]*..
				return importance;
			} catch (NumberFormatException e) {
				return subQueryScore;
			}
		}
	}

	/**
	 * Create a CustomScoreQuery over input subQuery.
	 *
	 * @param subQuery the sub query whose scored is being customized. Must not
	 * be null.
	 */
	public class ImportanceScoreQuery extends CustomScoreQuery {

		public ImportanceScoreQuery(Query subQuery) {
			super(subQuery);
		}

		@Override
		protected CustomScoreProvider getCustomScoreProvider(LeafReaderContext context) throws IOException {
			return new ImportanceScoreProvider(context);
		}
	}

}
