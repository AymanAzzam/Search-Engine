//import java.io.ObjectInputStream.GetField;
import java.sql.*;

public class DBController {

	private Connection conn;
	
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
	final String URLSummary_col = "summary";
	
	// Image Database
	final String image_table = "image_table";
	final String imageID_col = "image_ID";		//PRIMARY
	final String imageURLID_col = "URL_ID";
	final String imageURL_col = "image_URL";
	
	// Inverted File Database
	final String invertedFile_table = "word_table";
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
		System.out.println("Connect...");
	}
	
	public void build() throws SQLException

	{
		try {
			// CREATE URL TABLE
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(String.format("CREATE TABLE %s ("
					+ "%s INT PRIMARY KEY AUTO_INCREMENT,"
					+ "%s TINYTEXT UNIQUE NOT NULL,"
					+ "%s INT DEFAULT -1,"
					+ "%s TINYTEXT NOT NULL,"
					+ "%s TINYTEXT,"
					+ "%s VARCHAR(500));", 
					URL_table, URLID_col, URLName_col, countWords_col, 
					URLFilePath_col, URLTitle_col, URLSummary_col));
			
			
			
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
					+ ");", invertedFile_table, word_col, wordURLID_col, countPlaintxt_col, countHeader_col, countTotal_col, 
					word_col, wordURLID_col,
					wordURLID_col, URL_table, URLID_col));
			
			stmt.close();
			System.out.println("Init...");
			
			
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Init: Tables exists!");
		}
	}
	
	public ResultSet getRows(int lowerbound_ID) throws SQLException
	{
		Statement stmt = conn.createStatement();
		return stmt.executeQuery(String.format("SELECT * FROM %s"
				+ " WHERE %s>%d;", URL_table, URLID_col, lowerbound_ID));
	}
	
	public int getMaxURLID() throws SQLException {
		
		Statement stmt = conn.createStatement();
		ResultSet res = stmt.executeQuery(String.format("SELECT MAX(%s)"
				+ " FROM %s;", URLID_col, URL_table));
		
		res.next();
		int ret = res.getInt(1);
		res.close();
		stmt.close();
		return ret;
	}
	
	public boolean insertImage(int URLID, String imageURL) {
		
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
	
	public boolean insertURL(String URL, String filePath) {
		
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
	
	public boolean updateURL(int URLID, int count, String title, String summary) {
		
		try {

			Statement stmt = conn.createStatement();
			stmt.executeUpdate(String.format("UPDATE %s "
					+ "SET %s=%d, %s='%s', %s='%s' "
					+ "WHERE %s=%d;", 
					URL_table, countWords_col, count, URLTitle_col, title, URLSummary_col, summary, 
					URLID_col, URLID));
			stmt.close();
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	
	public boolean insertWord(String word, int URLID, int plain, int header, int total) {
		
		try {

			Statement stmt = conn.createStatement();
			stmt.executeUpdate(String.format("INSERT INTO %s(%s,%s,%s,%s,%s) "
					+ "VALUES('%s',%d,%d,%d,%d);", 
					invertedFile_table, word_col, wordURLID_col, countPlaintxt_col, countHeader_col, countTotal_col,
					word, URLID, plain, header, total));
			stmt.close();
		} catch(SQLException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public int getURLID(String URL)
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

	public void close() throws SQLException
	{
		conn.close();
		System.out.println("Close!");
	}
	
	public static void main(String []args) throws ClassNotFoundException, SQLException {

		DBController controller = new DBController();
		controller.connect();
		controller.build();
		
//		String link = "www.sdasasdsa.com";
//		System.out.println(controller.insertURL(link, "aaa.txt"));
//		System.out.println(controller.addURLData(controller.getURLID(link), "7moda", 5, 2, 10, 30));
		
//		System.out.println(controller.getMaxURLID());
//		
		ResultSet res = controller.getRows(0);
		try {
			
			while(res.next()) {
				System.out.println(res.getInt(1) + " " + res.getString(2) + " " + res.getInt(3) + " " + res.getString(4));
			}
		} catch (Exception e) {
			System.out.println("Empty Results!!!");
			e.printStackTrace();
		}
		
		
		controller.close();
	}
	
}
