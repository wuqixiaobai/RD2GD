package datamodel;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.Semaphore;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import connection.DbConnect;

public class TableMetadata extends Thread
{
	
	Table table;
	Semaphore cores;
	
	public TableMetadata(Table table, Semaphore cores)
	{
		this.table = table;
		this.cores = cores;
		setName(table.toString());
		this.start();
	}
	
	@Override
	public void run()
	{
		System.out.println("Processing table:" + getName());
		cores.acquireUninterruptibly();
		getMetadata();
		cores.release();
		System.out.println("Finished processing table:" + getName());
	}

	public void getMetadata()
	{
		String query = "SELECT count(*) FROM " + table.toString();
		
		try
		(
				Connection connection = DbConnect.getConnection();
				Statement statement = connection.createStatement();
				ResultSet resultset = statement.executeQuery(query);
		)
		{
			if(resultset.next())
			{
				table.setRowCount(resultset.getInt(1));
			}
		}
		catch(SQLException e)
		{
			JOptionPane.showMessageDialog(null, e.getMessage(), "SQL Error", JOptionPane.ERROR_MESSAGE,
					new ImageIcon(getClass().getResource("/icon/db_error.gif")));
		}
	}
}
