import java.io.ObjectInputStream.GetField;
import java.sql.*;

public class DBController {

	private Connection conn;
	
	private Statement stmt;

	
	final String DBName = "Search_Engine";
	final String username = "root";
	final String password = "";
	
	// URL Database
	final String URL_table = "URL_table";
	final String URLID_col = "ID";				// PRIMARY
	final String URLName_col = "URL";
	final String countWords_col = "words_count";
	final String URLFilePath_col = "file_path";
	final String URLTitle_col = "title";
	final String URLSummary_col = "summary";
	
	// Image Database
	final String image_table = "image_table";
	final String imageID_col = "image_ID";		//PRIMARY
	final String imageURLID_col = "URL_ID";
	final String imageURL_col = "image_URL";
	
	// Inverted File Database
	final String invertedFile_table = "inverted_file_table";
	final String word_col = "word";				// PRIMARY
	final String wordURLID_col = "URL_ID";		// PRIMARY
	final String countPlaintxt_col = "plaintext_count";
	final String countHeader_col = "header_count";
	final String countTotal_col = "total_count";
	
	
	
	public DBController() throws ClassNotFoundException 
	{
		Class.forName("com.mysql.cj.jdbc.Driver");
	}
	
	public void connect() throws SQLException
	{
		conn = DriverManager.getConnection(String.format("jdbc:mysql://localhost:3306/%s?useLegacyDatetimeCode=false&serverTimezone=Africa/Cairo", DBName),username,password);
		stmt = conn.createStatement();
		System.out.println("Connect...");
	}
	
	public void init() throws SQLException

	{
		try {
			// CREATE URL TABLE
			stmt.executeUpdate(String.format("CREATE TABLE %s ("
					+ "%s INT PRIMARY KEY AUTO_INCREMENT,"
					+ "%s TINYTEXT UNIQUE NOT NULL,"
					+ "%s INT DEFAULT -1,"
					+ "%s TINYTEXT NOT NULL,"
					+ "%s TINYTEXT NOT NULL,"
					+ "%s VARCHAR(500) NOT NULL;", 
					URL_table, URLID_col, URLName_col, countWords_col, 
					URLFilePath_col, URLTitle_col, URLSummary_col));
			
			
			// CREATE IMAGE TABLE
			stmt.executeUpdate(String.format("CREATE TABLE %s ("
					+ " %s INT PRIMARY KEY AUTO_INCREMENT,"
					+ " %s INT NOT NULL,"
					+ " %s TINYTEXT NOT NULL);", 
					image_table, imageID_col, imageURLID_col, imageURL_col));
			
			
			// CREATE INVERTED FILE TABLE
			stmt.executeUpdate(String.format("CREATE TABLE %s ("
					+ "%s VARCHAR(200) NOT NULL,"
					+ "%s INT NOT NULL,"
					+ "%s INT NOT NULL,"
					+ "%s INT NOT NULL,"
					+ "%s INT NOT NULL,"
					+ "PRIMARY KEY(%s,%s),"
					+ "FOREIGN KEY(%s) REFERENCES %s(%s) ON DELETE CASCADE"
					+ ");", invertedFile_table, word_col, wordURLID_col, countPlaintxt_col, countHeader_col, countTotal_col, 
					word_col, wordURLID_col,
					wordURLID_col, URL_table, URLID_col));
			
			System.out.println("Init...");
			
		} catch (Exception e) {
			System.out.println("Init: Tables exists!");
		}
	}
	
	public ResultSet getRows(int lowerbound_ID) throws SQLException
	{
		ResultSet res;
		res = stmt.executeQuery(String.format("SELECT * FROM %s"
				+ " WHERE %s>%d;", URL_table, URLID_col, lowerbound_ID));
		
		return res;
	}
	
	public int getMaxURLID() throws SQLException {
		
		ResultSet res = stmt.executeQuery(String.format("SELECT MAX(%s)"
				+ " FROM %s;", URLID_col, URL_table));
		
		res.next();
		return res.getInt(1);
	}
	
	public boolean insertImage(int URLID, String imageURL) {
		
		try {
			
			stmt.executeUpdate(String.format("INSERT INTO %s(%s,%s) "
					+ "VALUES(%d,'%s');",
					image_table, imageURLID_col, imageURL_col, URLID, imageURL));
			
		} catch (SQLException e) {
			return false;
		}
		return true;
	}
	
	public boolean insertURL(String URL, String filePath) {
		
		try {
			
			stmt.executeUpdate(String.format("INSERT INTO %s(%s,%s) "
					+ "VALUES('%s','%s');",
					URL_table, URLName_col, URLFilePath_col, URL, filePath));
			
		} catch (SQLException e) {
			return false;
		}
		return true;
	}
	
	public boolean updateURL(int URLID, int count) {
		
		try {
			
			stmt.executeUpdate(String.format("UPDATE %s "
					+ "SET %s=%d "
					+ "WHERE %s=%d;", 
					URL_table, countWords_col, count, URLID_col, URLID));
			
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	
	public boolean insertWord(String word, int URLID, int plain, int header, int total) {
		
		try {
			
			stmt.executeUpdate(String.format("INSERT INTO %s(%s,%s,%s,%s,%s) "
					+ "VALUES('%s',%d,%d,%d,%d);", 
					invertedFile_table, word_col, wordURLID_col, countPlaintxt_col, countHeader_col, countTotal_col,
					word, URLID, plain, header, total));
			
		} catch(SQLException e) {
			return false;
		}
		return true;
	}
	
	public int getURLID(String URL)
	{
		int ID;
		try {
			
			ResultSet res = stmt.executeQuery(String.format("SELECT %s FROM %s WHERE %s='%s';",
					URLID_col, URL_table, URLName_col, URL));
			
			res.next();
			
			ID = res.getInt(1);
			
		} catch (SQLException e) {
			return -1;
		}
		return ID;
	}
	
//	public int addURLData(int URLID, String word, int plain, int header, int total, int totalWords) {
//		
//		/*
//		 * Errors:
//		 * 	URL Exists => -1
//		 *  Error in word insertion => 0
//		 *  Success => 1
//		 */
//		
//		boolean flag = true;
//		flag &= updateURL(URLID, totalWords);
//		if(!flag)	return -1;
//		
//		flag &= insertWord(word, URLID, plain, header, total);
//		
//		return flag?1:0;
//	}
	
	public void close() throws SQLException
	{
		conn.close();
		System.out.println("Close!");
	}
	
	public static void main(String []args) throws ClassNotFoundException, SQLException {

		
		
		DBController controller = new DBController();
		controller.connect();
		controller.init();
		
//		String link = "www.sdasasdsa.com";
//		System.out.println(controller.insertURL(link, "aaa.txt"));
//		System.out.println(controller.addURLData(controller.getURLID(link), "7moda", 5, 2, 10, 30));
		
		System.out.println(controller.getMaxURLID());
		
		ResultSet res = controller.getRows(1);
		try {
			
			while(res.next()) {
				System.out.println(res.getInt(1) + " " + res.getString(2) + " " + res.getInt(3) + " " + res.getString(4));
			}
		} catch (Exception e) {
			System.out.println("Empty Results!!!");
		}
		
		
		controller.close();
	}
	
}
