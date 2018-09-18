package jtabletest;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class JtableTest3 extends JFrame {

	JTable table;

	public static Connection getConnection() throws Exception {
		try {
			String driver = "com.mysql.jdbc.Driver";
			String url = "jdbc:mysql://localhost:3306/jtable1?autoReconnect=true&useSSL=false";
			String username = "root";
			String password = "1234";
			//Class.forName(driver);  //do not need this?? **** 

			Connection conn = DriverManager.getConnection(url, username, password);
			System.out.println("Connected");
			return conn;
		} catch (Exception e) {
			System.out.println(e);
		}
		return null;

	}
	public void showData() throws Exception{
		
	}
	public static void insertData() throws Exception{
		//ready for DEMO 6/6/2018
		final String var1 = "1005";
		final String var2 = "Who Doe";
		final String var3 = "50006";
		final String var4 = "03/03/2018";
		try{
			Connection con = getConnection();
			PreparedStatement insert = con.prepareStatement("INSERT INTO emp (EmployeeNumber,EmployeeName,AssetID,DateDelivered) VALUES ('"+var1+"','"+var2+"','"+var3+"','"+var4+"')");
			insert.executeUpdate(); // query means we are receiving information
		}catch(Exception e){
			System.out.println(e);
		}
		finally{
			System.out.println("Insert Completed");
		}
	}

	public JtableTest3() {
		setLayout(new FlowLayout());
		DefaultTableModel theTable = new DefaultTableModel();
		Object[] columns = {"Employee Number", "Employee Name", "Asset ID", "Date Delivered"};
		String[][] theData = null;
		JTable table = new JTable(theTable);
		theTable.addColumn("EmployeeNumber");
		theTable.addColumn("EmployeeName");
		theTable.addColumn("AssetID");
		theTable.addColumn("DateDelivered");

		table.setModel(theTable);
		table.setPreferredScrollableViewportSize(new Dimension(470,100));
		table.setFillsViewportHeight(true);
		table.getTableHeader().setReorderingAllowed(true); // Set True if you want the columns to be swappable

		try{
			Connection con = getConnection();
			String sql = "SELECT * FROM jtable1.emp";
			Statement S = con.createStatement();
			ResultSet R = S.executeQuery(sql);
			
			while(R.next())
			{
				theTable.addRow(new Object[]{ R.getString(1),R.getString(2),R.getString(3),R.getString(4)});
				
			}
			table.setModel(theTable);
			
			//table.moveColumn(table.getColumnCount() - 1, 0); // testing the swapping of columns

			JScrollPane scrollPane = new JScrollPane(table);
			add(scrollPane);
		} 
		catch (Exception e){
			System.out.println(e);
		}
	}

	private Object[][] data = { 
			{ "1001", "Jane Doe", "52001", "01/01/2018" },
			{ "1002", "Ryan Hong", "52002", "01/05/2018" }, 
			{ "1003", "John Doe", "50001", "03/01/2018" }, };

	public static void tableGui() {
		JtableTest3 gui = new JtableTest3();
		gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// gui.setSize(600,200); // use gui.pack() for the exact size
		gui.pack();
		gui.setVisible(true);
		gui.setTitle("Demo");
	}

	public static void main(String[] args) throws Exception {
		
		//getConnection();
		//insertData(); // uncomment out for DEMO 6/6/2018
		tableGui();
		
		
	}

}
