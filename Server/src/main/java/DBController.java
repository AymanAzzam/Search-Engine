import java.sql.*;

public class DBController {

	private Connection conn;
	
	private Statement stmt;

	
	final String DBName = "Search_Engine";
	final String username = "root";
	final String password = "";
	
	// Inverted File Database
	final String invertedFile_table = "inverted_file";
	final String word_col = "word";				// PRIMARY
	final String wordURLID_col = "URL_ID";		// PRIMARY
	final String countPlaintxt_col = "plaintext_count";
	final String countHeader_col = "header_count";
	final String countTotal_col = "total_count";
	
	
	// URL Database
	final String URL_table = "URL_table";
	final String URLID_col = "ID";				// PRIMARY
	final String URLName_col = "Name";
	final String countWords_col = "words_count";
	
	
	public DBController() throws ClassNotFoundException 
	{
		Class.forName("com.mysql.cj.jdbc.Driver");
		System.out.println("Constructor");
	}
	
	public void connect() throws SQLException
	{
		conn = DriverManager.getConnection(String.format("jdbc:mysql://localhost:3306/%s?useLegacyDatetimeCode=false&serverTimezone=Africa/Cairo", DBName),username,password);
		stmt = conn.createStatement();
		System.out.println("Connect...");
	}
	
	public void init() throws SQLException
	{
		// CREATE URL TABLE
		stmt.executeUpdate(String.format("CREATE TABLE %s ("
				+ "%s INT PRIMARY KEY AUTO_INCREMENT,"
				+ "%s TINYTEXT UNIQUE NOT NULL,"
				+ "%s INT NOT NULL);", 
				URL_table, URLID_col, URLName_col, countWords_col));
		
		// CREATE INVERTED FILE TABLE
		stmt.executeUpdate(String.format("CREATE TABLE %s ("
				+ "%s VARCHAR(200) NOT NULL,"
				+ "%s INT NOT NULL,"
				+ "%s INT NOT NULL,"
				+ "%s INT NOT NULL,"
				+ "%s INT NOT NULL,"
				+ "PRIMARY KEY(%s,%s),"
				+ "FOREIGN KEY(%s) REFERENCES %s(%s)"
				+ ");", invertedFile_table, word_col, wordURLID_col, countPlaintxt_col, countHeader_col, countTotal_col, 
				word_col, wordURLID_col,
				wordURLID_col, URL_table, URLID_col));
		
		System.out.println("Init...");
	}
	
	public boolean insertURL(String URL, int count) {
		
		try {
			
			stmt.executeUpdate(String.format("INSERT INTO %s(%s,%s) "
					+ "VALUES('%s',%d);",
					URL_table, URLName_col, countWords_col, URL, count));
			
		} catch (SQLException e) {
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
	
	public int addURLData(String URL, String word, int plain, int header, int total, int totalWords) {
		
		/*
		 * Errors:
		 * 	URL Exists => -1
		 *  Error in word insertion => 0
		 *  Success => 1
		 */
		
		boolean flag = true;
		flag &= insertURL(URL, totalWords);
		if(!flag)	return -1;
		
		int URLID = getURLID(URL);
		if(URLID==-1)	return -1;
		
		flag &= insertWord(word, URLID, plain, header, total);
		
		return flag?1:0;
	}
	
	public void close() throws SQLException
	{
		conn.close();
		System.out.println("Close!");
	}
	
	public static void main(String []args) throws ClassNotFoundException, SQLException {
		System.out.println("Test");
		
		DBController controller = new DBController();
		controller.connect();
//		controller.init();
		System.out.println(controller.addURLData("www.ex.com", "test", 5, 2, 10, 30));
		controller.close();
	}
}
