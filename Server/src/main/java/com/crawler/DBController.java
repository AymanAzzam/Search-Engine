package com.crawler;

//import java.io.ObjectInputStream.GetField;
import java.sql.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map.Entry;

import com.crawler.Indexer.WordRecord;

public class DBController {

	
	final String DBName = "SE";
	final String username = "root";
	final String password = "";
	
	// URL Table
	final String URL_table = "URL_table";
	final String URLID_col = "ID";				// PRIMARY
	final String URLName_col = "URL";
	final String countWords_col = "words_count";
	final String URLFilePath_col = "file_path";
	final String URLTitle_col = "title";
	final String URLContent_col = "content";
	final String isIndexed_col = "is_indexed";
	final String popularity_col = "popularity";
	
	// Image Table
	final String image_table = "image_table";
	final String imageID_col = "image_ID";		//PRIMARY
	final String imageURLID_col = "URL_ID";
	final String imageURL_col = "image_URL";
	
	// Inverted File Table
	final String word_table = "word_table";
	final String word_col = "word";				// PRIMARY
	final String wordURLID_col = "URL_ID";		// PRIMARY
	final String countPlaintxt_col = "plaintext_count";
	final String countHeader_col = "header_count";
	final String countTotal_col = "total_count";
	
	// Crawling Table
	final String crawl_table = "crawling_table";
//	final String URLID_col = "ID";				// PRIMARY
//	final String URLName_col = "URL";
	final String isCrawled_col = "is_crawled";
	
	
	
	public DBController() throws ClassNotFoundException 
	{
		Class.forName("com.mysql.cj.jdbc.Driver");
	}
	
	// Establish a database connection
	public Connection connect() throws SQLException
	{
//		System.out.println("Establishing DB Connection...");
		return DriverManager.getConnection(String.format("jdbc:mysql://localhost:3306/%s?useLegacyDatetimeCode=false&serverTimezone=Africa/Cairo", DBName),username,password);
	}
	
	public void drop(Connection conn) throws SQLException {
		
		Statement stmt = conn.createStatement();
		stmt.executeUpdate(String.format("DROP TABLE URL_REF,%s, %s, %s, %s;",
				image_table, word_table, URL_table, crawl_table));
		
		stmt.close();
	}
	
	// Create the tables
	public void build(Connection conn) throws SQLException
	{
		try {
			// CREATE URL TABLE
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(String.format("CREATE TABLE %s ("
					+ "%s INT PRIMARY KEY AUTO_INCREMENT,"
					+ "%s VARCHAR(300) UNIQUE NOT NULL,"
					+ "%s INT DEFAULT 0 NOT NULL,"
					+ "%s TINYTEXT,"
					+ "%s TINYTEXT,"
					+ "%s MEDIUMTEXT,"
					+ "%s BOOLEAN DEFAULT FALSE NOT NULL,"
					+ "%s DOUBLE DEFAULT 0);", 
					URL_table, URLID_col, URLName_col, countWords_col, 
					URLFilePath_col, URLTitle_col, URLContent_col, isIndexed_col, popularity_col));
			
			
			
			// CREATE IMAGE TABLE
			stmt.executeUpdate(String.format("CREATE TABLE %s ("
					+ " %s INT NOT NULL,"
					+ " %s VARCHAR(500) NOT NULL,"
					+ "PRIMARY KEY(%s,%s),"
					+ " FOREIGN KEY(%s) REFERENCES %s(%s) ON DELETE CASCADE"
					+ ");", 
					image_table, imageURLID_col, imageURL_col,
					imageURLID_col, imageURL_col,
					imageURLID_col, URL_table, URLID_col));
			
			
			// CREATE INVERTED FILE TABLE
			stmt.executeUpdate(String.format("CREATE TABLE %s ("
					+ "%s VARCHAR(200) NOT NULL,"
					+ "%s INT NOT NULL,"
					+ "%s INT NOT NULL,"
					+ "%s INT NOT NULL,"
					+ "%s INT NOT NULL,"
					+ "PRIMARY KEY(%s,%s),"
					+ "FOREIGN KEY(%s) REFERENCES %s(%s) ON DELETE CASCADE"
					+ ");", word_table, word_col, wordURLID_col, countPlaintxt_col, countHeader_col, countTotal_col, 
					word_col, wordURLID_col,
					wordURLID_col, URL_table, URLID_col));
			
			// CREATE CRAWLING TABLE
			stmt.executeUpdate(String.format("CREATE TABLE %s ("
					+ "%s INT PRIMARY KEY AUTO_INCREMENT,"
					+ "%s VARCHAR(300) UNIQUE NOT NULL,"
					+ "%s BOOLEAN DEFAULT FALSE);",
					crawl_table, URLID_col, URLName_col, isCrawled_col));

			stmt.executeUpdate("CREATE TABLE URL_REF ("
				+ " Pointer VARCHAR(300),"
				+ " Pointed VARCHAR(300),"
				+ " PRIMARY KEY(Pointer,Pointed));");
				// + " PRIMARY KEY(Pointer,Pointed),"
				// + " FOREIGN KEY(Pointer) REFERENCES CRAWLING_TABLE(URL) ON DELETE CASCADE,"
				// + " FOREIGN KEY(Pointed) REFERENCES CRAWLING_TABLE(URL) ON DELETE CASCADE);");
			
			stmt.close();
			System.out.println("Database Tables Created Successfully!");
			
			
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Database Tables already exists!");
		}
	}

	public void insertRefs(Connection conn, String pointer, ArrayList<String> pointeds) {


		String query = new String("INSERT IGNORE INTO URL_REF VALUES ");
		
		String values = new String();
		
		for(String url:pointeds) {

			if(values.length()>0)	values +=",";
			values+=String.format("('%s','%s')", pointer, url);
		}

		query+=values+";";

		try {
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(query);
		} catch (SQLException e) {
			//TODO: handle exception
			// e.printStackTrace();
		}
	}

	public int getPointingToCount(Connection conn, String pointer) {
		int cnt = 0;
		try {
			Statement stmt = conn.createStatement();
			ResultSet res = stmt.executeQuery(String.format("SELECT COUNT(*) FROM URL_REF WHERE POINTER = '%s';",pointer));
			res.next();
			cnt = res.getInt(1);
		} catch (SQLException e) {
			//TODO: handle exception
		}
		return cnt;
	}

	public ArrayList<String> getPointedFromURLs(Connection conn, String pointed) {
		ArrayList<String> ret = new ArrayList<String>();
		try {
			Statement stmt = conn.createStatement();
			ResultSet res = stmt.executeQuery(String.format("SELECT POINTER FROM URL_REF WHERE POINTED = '%s';",pointed));
			
			while(res.next()) {
				ret.add(res.getString(1));
			}

		} catch (SQLException e) {
			//TODO: handle exception
		}
		return ret;
	}
	
	// Get non-indexed yet rows
	public ResultSet getNonIndexedRows(Connection conn, int LIMIT) throws SQLException
	{
		Statement stmt = conn.createStatement();
		return stmt.executeQuery(String.format("SELECT * FROM %s"
				+ " WHERE %s = FALSE ORDER BY %s LIMIT %d;", URL_table, isIndexed_col, URLID_col,LIMIT));
	}
	
	// Mark returned non-indexed rows
	public int markNonIndexedRows(Connection conn, int LIMIT) throws SQLException {

		int ret = 0;

		Statement stmt = conn.createStatement();
		
		ret = stmt.executeUpdate(String.format("UPDATE %s "
				+ "SET %s = TRUE "
				+ "WHERE %s = FALSE ORDER BY %s LIMIT %d;",
				URL_table, isIndexed_col, isIndexed_col, URLID_col,LIMIT));
		
		stmt.close();

		return ret;
	}
	
	// Get the minimum word counts in url_table
	public int checkNonIndexed(Connection conn) throws SQLException {
		
		Statement stmt = conn.createStatement();
		ResultSet res = stmt.executeQuery(String.format("SELECT COUNT(*)"
				+ " FROM %s WHERE %s = FALSE;", URL_table, isIndexed_col));
		
		res.next();
		int ret = res.getInt(1);
		res.close();
		stmt.close();
		return ret;
	}

	// Get non-crawled yet rows
	public ResultSet getNonCrawledRows(Connection conn, int LIMIT) throws SQLException
	{
		Statement stmt = conn.createStatement();
		return stmt.executeQuery(String.format("SELECT * FROM %s"
				+ " WHERE %s = FALSE ORDER BY %s LIMIT %d;", crawl_table, isCrawled_col, URLID_col, LIMIT));
	}
	
	// Mark returned non-crawled rows
	public int markNonCrawledRows(Connection conn, int LIMIT) throws SQLException {

		int ret = 0;

		Statement stmt = conn.createStatement();
		
		ret = stmt.executeUpdate(String.format("UPDATE %s "
				+ "SET %s = TRUE "
				+ "WHERE %s = FALSE ORDER BY %s LIMIT %d;",
				crawl_table, isCrawled_col, isCrawled_col, URLID_col, LIMIT));
		
		stmt.close();

		return ret;
	}
	
	// Get the minimum word counts in crawling_table
	public int checkNonCrawled(Connection conn) throws SQLException {

		Statement stmt = conn.createStatement();
		ResultSet res = stmt.executeQuery(String.format("SELECT COUNT(*)"
				+ " FROM %s WHERE %s = FALSE;", crawl_table, isCrawled_col));
		
		res.next();
		int ret = res.getInt(1);
		res.close();
		stmt.close();
		return ret;
	}
	
	public int getCrawlingSize(Connection conn) throws SQLException {

		Statement stmt = conn.createStatement();
		ResultSet res = stmt.executeQuery(String.format("SELECT COUNT(*)"
				+ " FROM %s;", crawl_table));
		
		res.next();
		int ret = res.getInt(1);
		res.close();
		stmt.close();
		return ret;
	}
	
	public int insertCrawlingURLs(Connection conn, ArrayList<String> URLs) {
		
		int ret = 0;

		String query = new String(String.format("INSERT IGNORE INTO %s(%s) VALUES",
			crawl_table, URLName_col));
		
		String values = new String();
		
		for(String url:URLs) {

			if(values.length()>0)	values +=",";
			values+=String.format("('%s')", url);
		}

		query+=values+";";
		
		try {
			Statement stmt = conn.createStatement();
			
			try {
				
				ret = stmt.executeUpdate(query);
			} catch (SQLException e) {

			}
			
			stmt.close();
			
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		
		return ret;
	}
	
	
	// Insert an image related with a URL
	public boolean insertImages(Connection conn, int URLID, ArrayList<String> imageURLs) {
		
		String query = new String(String.format("INSERT IGNORE INTO %s(%s,%s) VALUES",
			image_table, imageURLID_col, imageURL_col));
		
		String values = new String();
		
		for(String img:imageURLs) {

			if(values.length()>0)	values +=",";
			values+=String.format("(%d,'%s')",URLID, img);
		}

		query+=values+";";

		try {

			Statement stmt = conn.createStatement();
			stmt.executeUpdate(query);
			stmt.close();
		} catch (SQLException e) {
			return false;
		}
		return true;
	}
	
	public ArrayList<String> getImagesURLs(Connection conn, String url) {
		
		
		ArrayList<String> images = new ArrayList<String>();
		try {
			
			Statement stmt = conn.createStatement();
			
			ResultSet res = stmt.executeQuery(String.format("SELECT %s FROM %s"
					+ " INNER JOIN %s ON %s = %s"
					+ " WHERE %s = '%s' LIMIT 3;",
					imageURL_col, image_table, URL_table, URLID_col, imageURLID_col, URLName_col, url));
			
			while(res.next()) {
				images.add(res.getString(1));
			}
			
			res.close();
			stmt.close();
			
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return images;
	}
	
	// Insert a URL with its extracted document path
	public boolean insertURL(Connection conn, String URL, String filePath) {
		
		try {

			Statement stmt = conn.createStatement();
			stmt.executeUpdate(String.format("INSERT INTO %s(%s,%s) "
					+ "VALUES('%s','%s');",
					URL_table, URLName_col, URLFilePath_col, URL, filePath));
			stmt.close();
		} catch (SQLException e) {
			return false;
		}
		return true;
	}
	
	// Update the remaining attributes of the url_table record
	public boolean updateURL(Connection conn, int URLID, int count, String title, String content) {
		
		try {

			Statement stmt = conn.createStatement();
			stmt.executeUpdate(String.format("UPDATE %s "
					+ "SET %s=%d, %s='%s', %s='%s', %s = TRUE "
					+ "WHERE %s=%d;", 
					URL_table, countWords_col, count, URLTitle_col, title, URLContent_col, content, isIndexed_col,
					URLID_col, URLID));
			stmt.close();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	// Insert a record to word_table
	public boolean insertWords(Connection conn, int URLID, ArrayList<WordRecord> wordStat) {
		
		if(wordStat.isEmpty())	return false;

		String query = new String(String.format("INSERT INTO %s(%s,%s,%s,%s,%s) VALUES ",
			word_table, word_col, wordURLID_col, countPlaintxt_col, countHeader_col, countTotal_col));
		String values = new String();
		for(WordRecord w:wordStat)
		{
			if(values.length()>0)	values +=",";
			values+=String.format("('%s',%d,%d,%d,%d)",w.word, URLID, w.plainCount, w.headerCount, w.wordCount);
		}
		query+=values+";";

		try {

			Statement stmt = conn.createStatement();
			stmt.executeUpdate(query);
			stmt.close();
		} catch(SQLException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	// get URLID of specific URL String
	public int getURLID(Connection conn, String URL)
	{
		int ID;
		try {

			Statement stmt = conn.createStatement();
			ResultSet res = stmt.executeQuery(String.format("SELECT %s FROM %s WHERE %s='%s';",
					URLID_col, URL_table, URLName_col, URL));
			
			res.next();
			
			ID = res.getInt(1);
			stmt.close();
			res.close();
		} catch (SQLException e) {
			return -1;
		}
		return ID;
	}
	
	public int getURLsSize(Connection conn) {
		int size = 0;
		
		try {
			
			Statement stmt = conn.createStatement();
			
			ResultSet rs = stmt.executeQuery(String.format("SELECT COUNT(*) FROM %s;"
					, URL_table));
			
			rs.next();
			size = rs.getInt(1);

			stmt.close();
			rs.close();
			
		} catch (SQLException e) {
			// TODO: handle exception
		}
		return size;
	}

	public ArrayList<String> getAllURLs(Connection conn) throws SQLException {
		ArrayList<String> ret = new ArrayList<String>();

		Statement stmt = conn.createStatement();
		ResultSet res = stmt.executeQuery("SELECT URL FROM CRAWLING_TABLE;");
		
		while(res.next()) {
			ret.add(res.getString(1));
		}

		return ret;
	}
	
	// get Inverted File Content for specific word
	public ArrayList<String> getInvertedFile(Connection conn, String word)
	{
		ArrayList<String> out = new ArrayList<String>();	ResultSet rs;
	
		Statement stmt;
		try {
				stmt = conn.createStatement();

				rs = stmt.executeQuery(String.format("SELECT * FROM %s"
						+ " WHERE %s = '%s';", word_table, word_col, word));
				
				while(rs.next())
				{
					out.add(rs.getString(wordURLID_col));
					out.add(rs.getString(countPlaintxt_col));
					out.add(rs.getString(countHeader_col));
					out.add(rs.getString(countTotal_col));
				}
				
				stmt.close();
				rs.close();
		} catch (SQLException e) {	e.printStackTrace();	}
		
		return out;
	}

	// get Content for specific URL_ID
	public ArrayList<String> getUrlFile(Connection conn, int URL_ID)
	{
		ArrayList<String> out = new ArrayList<String>();	ResultSet rs;
	
		Statement stmt;
		try {
				stmt = conn.createStatement();
				rs = stmt.executeQuery(String.format("SELECT * FROM %s"
						+ " WHERE %s = %s;", URL_table, URLID_col, URL_ID));
				
				while(rs.next())
				{
					out.add(rs.getString(URLTitle_col));
					out.add(rs.getString(URLName_col));
					out.add(rs.getString(URLContent_col));	
					out.add(rs.getString(countWords_col));
				}

				stmt.close();
				rs.close();
				
		} catch (SQLException e) {	e.printStackTrace();	}
		
		return out;
	}

	public ArrayList<OutputValue> getMatchingURLs(Connection conn, String word) throws SQLException{
		ArrayList<OutputValue> ret = new ArrayList<OutputValue>();

		Statement stmt = conn.createStatement();
		word = "%" + word + "%";
		ResultSet res = stmt.executeQuery(String.format("SELECT * FROM %s"
				+ " WHERE %s like '%s';", URL_table, URLName_col, word));


		while(res.next())
		{
			String content = res.getString(URLContent_col);
			ret.add(new OutputValue(res.getString(URLName_col), res.getString(URLTitle_col), content.substring(0,Math.min(500,content.length()))));
		}
		stmt.close();
		res.close();
		return ret;
	}

	public void removeDefected(Connection conn) throws SQLException {
		
		Statement stmt = conn.createStatement();

		stmt.executeUpdate(String.format("DELETE FROM %s WHERE NOT EXISTS "
			+ "(SELECT * FROM %s WHERE %s.%s = %s.%s);"
			,crawl_table, URL_table, crawl_table, URLName_col, URL_table, URLName_col));

		stmt.executeUpdate(String.format("DELETE FROM URL_REF WHERE NOT EXISTS "
			+ "(SELECT * FROM %s WHERE URL_REF.POINTER = %s.%s AND URL_REF.POINTED = %s.%s);"
			,URL_table, URL_table, URLName_col, URL_table, URLName_col));
		stmt.close();
	}

	public void insertPopularity(Connection conn, Hashtable<String, Double> pop) throws SQLException {

		String query = String.format("INSERT INTO %s(%s,%s) VALUES "
			,URL_table, URLName_col, popularity_col);
		
		String values = new String();
		for(Entry<String,Double> en:pop.entrySet()) {

			if(values.length()>0)	values +=",";
			values += String.format("('%s',%f)",en.getKey(),en.getValue());
		}

		query += values + String.format(" ON DUPLICATE KEY UPDATE %s= VALUES(%s);"
			, popularity_col, popularity_col);


		Statement stmt = conn.createStatement();
		stmt.executeUpdate(query);
		stmt.close();
	}

	public Hashtable<String, Double> getPopularity(Connection conn) throws SQLException {

		Statement stmt = conn.createStatement();

		ResultSet res = stmt.executeQuery(String.format("SELECT %s, %s FROM %s;"
			, URLName_col, popularity_col, URL_table));
		
		Hashtable<String, Double> ret = new Hashtable<String, Double>();

		while(res.next()) {
			ret.put(res.getString(1), res.getDouble(2));
		}

		res.close();
		stmt.close();

		return ret;
	}

	// Close a database connection
	public void close(Connection conn) throws SQLException
	{
		System.out.println("Closing DB Connection...");
		conn.close();
	}
	

	public static int test(Connection conn) throws SQLException {

		Statement stmt = conn.createStatement();
		return stmt.executeUpdate("update test2 set id = 2 where id < 10 limit 8;");
		
	}

	// Main method
	public static void main(String []args) throws ClassNotFoundException, SQLException {

		DBController controller = new DBController();
		Connection conn;
		conn = controller.connect();
		// controller.build(conn);		
		System.out.println(test(conn));
		controller.close(conn);
	}
	
	// Preparing a test for the indexer
	public void indexerTest(Connection conn) throws SQLException {
		Statement stmt = conn.createStatement();
		
		stmt.executeUpdate("delete from url_table;");
		
		for(int i=0 ; i<10 ; ++i) {
			stmt.executeUpdate("INSERT INTO url_table(URL, file_path) values('www."+ i +".com','crawler_format.txt');");
		}
	}
	
	
}
