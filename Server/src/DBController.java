//import java.io.ObjectInputStream.GetField;
import java.sql.*;

public class DBController {

//	private Connection conn;
	
//	private Statement stmt;

	
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
	final String URLContent_col = "content";
	final String isIndexed_col = "is_indexed";
	
	// Image Database
	final String image_table = "image_table";
	final String imageID_col = "image_ID";		//PRIMARY
	final String imageURLID_col = "URL_ID";
	final String imageURL_col = "image_URL";
	
	// Inverted File Database
	final String word_table = "word_table";
	final String word_col = "word";				// PRIMARY
	final String wordURLID_col = "URL_ID";		// PRIMARY
	final String countPlaintxt_col = "plaintext_count";
	final String countHeader_col = "header_count";
	final String countTotal_col = "total_count";
	
	
	
	public DBController() throws ClassNotFoundException 
	{
		Class.forName("com.mysql.cj.jdbc.Driver");
	}
	
	// Establish a database connection
	public Connection connect() throws SQLException
	{
		System.out.println("Establishing DB Connection...");
		return DriverManager.getConnection(String.format("jdbc:mysql://localhost:3306/%s?useLegacyDatetimeCode=false&serverTimezone=Africa/Cairo", DBName),username,password);
	}
	
	// Create the tables
	public void build(Connection conn) throws SQLException
	{
		try {
			// CREATE URL TABLE
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(String.format("CREATE TABLE %s ("
					+ "%s INT PRIMARY KEY AUTO_INCREMENT,"
					+ "%s TINYTEXT UNIQUE NOT NULL,"
					+ "%s INT DEFAULT 0 NOT NULL,"
					+ "%s TINYTEXT NOT NULL,"
					+ "%s TINYTEXT,"
					+ "%s TEXT,"
					+ "%s BOOLEAN DEFAULT FALSE NOT NULL);", 
					URL_table, URLID_col, URLName_col, countWords_col, 
					URLFilePath_col, URLTitle_col, URLContent_col, isIndexed_col));
			
			
			
			// CREATE IMAGE TABLE
			stmt.executeUpdate(String.format("CREATE TABLE %s ("
					+ " %s INT PRIMARY KEY AUTO_INCREMENT,"
					+ " %s INT NOT NULL,"
					+ " %s TINYTEXT NOT NULL,"
					+ " FOREIGN KEY(%s) REFERENCES %s(%s) ON DELETE CASCADE"
					+ ");", 
					image_table, imageID_col, imageURLID_col, imageURL_col,
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
			
			stmt.close();
			System.out.println("Init...");
			
			
		} catch (Exception e) {
//			e.printStackTrace();
			System.out.println("Init: Tables exists!");
		}
	}
	
	// Get non-indexed yet rows
	public ResultSet getNonIndexedRows(Connection conn) throws SQLException
	{
		Statement stmt = conn.createStatement();
		return stmt.executeQuery(String.format("SELECT * FROM %s"
				+ " WHERE %s = FALSE ORDER BY %s LIMIT 1;", URL_table, isIndexed_col, URLID_col));
	}
	
	// Mark returned non-indexed rows
	public void markNonIndexedRows(Connection conn) throws SQLException {
		Statement stmt = conn.createStatement();
		
		stmt.executeUpdate(String.format("UPDATE %s "
				+ "SET %s = TRUE "
				+ "WHERE %s = FALSE ORDER BY %s LIMIT 1;",
				URL_table, isIndexed_col, isIndexed_col, URLID_col));
		
		stmt.close();
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
	
	// Insert an image related with a URL
	public boolean insertImage(Connection conn, int URLID, String imageURL) {
		
		try {

			Statement stmt = conn.createStatement();
			stmt.executeUpdate(String.format("INSERT INTO %s(%s,%s) "
					+ "VALUES(%d,'%s');",
					image_table, imageURLID_col, imageURL_col, URLID, imageURL));
			stmt.close();
		} catch (SQLException e) {
			return false;
		}
		return true;
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
	public boolean insertWord(Connection conn, String word, int URLID, int plain, int header, int total) {
		
		try {

			Statement stmt = conn.createStatement();
			stmt.executeUpdate(String.format("INSERT INTO %s(%s,%s,%s,%s,%s) "
					+ "VALUES('%s',%d,%d,%d,%d);", 
					word_table, word_col, wordURLID_col, countPlaintxt_col, countHeader_col, countTotal_col,
					word, URLID, plain, header, total));
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

	// Close a database connection
	public void close(Connection conn) throws SQLException
	{
		System.out.println("Closing DB Connection...");
		conn.close();
	}
	
	// Main method
	public static void main(String []args) throws ClassNotFoundException, SQLException {

		DBController controller = new DBController();
		Connection conn;
		conn = controller.connect();
		controller.build(conn);		
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
