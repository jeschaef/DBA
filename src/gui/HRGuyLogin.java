package gui;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.security.sasl.AuthenticationException;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import dbc.DatabaseController;
import dbc.PasswordHash;

import javax.swing.JLabel;

import java.awt.Font;

import javax.swing.JTextField;
import javax.swing.JPasswordField;
import javax.swing.JButton;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class HRGuyLogin extends JFrame {

	private JPanel contentPane;
	
	
	//Variables for database access
	DatabaseController dbc = null;
	private static final String PATH_TO_DB = "C:/SQLite/db/dba/HRD.db";
	private JTextField textField;
	private JPasswordField passwordField;
	
	

	/**
	 * Test Unit
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					HRGuyLogin frame = new HRGuyLogin(PATH_TO_DB);
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame to login to the HRGuyGUI.
	 * @param path The path to the database
	 */
	public HRGuyLogin(String path) {
		setTitle("Login to HRGuyGUI");
		
		//Database options
		try {
			dbc = new DatabaseController(path);
		} catch (SQLException e) {
			handleSQLException(e, true);
		}
		
		
		
		
		
		
		
		//Frame options		
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		//	Handle closing event
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				exit();
			}
		});
		
		
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		
		//Login form options
		JPanel panel = new JPanel();
		contentPane.add(panel, BorderLayout.CENTER);
		panel.setLayout(null);
		
		
		//Username
		JLabel lblUsername = new JLabel("Username:");
		lblUsername.setFont(new Font("Tahoma", Font.PLAIN, 15));
		lblUsername.setBounds(12, 29, 79, 26);
		panel.add(lblUsername);
		
		textField = new JTextField();
		textField.setFont(new Font("Tahoma", Font.PLAIN, 15));
		textField.setBounds(92, 28, 160, 30);
		panel.add(textField);
		textField.setColumns(10);
		
		
		//Password
		JLabel lblPassword = new JLabel("Password:");
		lblPassword.setFont(new Font("Tahoma", Font.PLAIN, 15));
		lblPassword.setBounds(12, 87, 79, 31);
		panel.add(lblPassword);
		
		passwordField = new JPasswordField();
		passwordField.setBounds(92, 88, 160, 30);
		panel.add(passwordField);
		
		
		
		//Buttons
		JButton btnLogin = new JButton("Login");
		btnLogin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				String user = textField.getText();
				String password = new String(passwordField.getPassword());
				try {
					//Try to login
					login(user, password);
					
					//Successful login
					HRGuyGUI gui = new HRGuyGUI(dbc);
					gui.setVisible(true);
					dispose();
					
					
				} catch (IllegalArgumentException e) {
					handleLoginArgumentError(e);
				} catch (SQLException e) {
					handleSQLException(e, true);
				} catch (AuthenticationException e) {
					//handleAuthExc
				}
			}
		});
		btnLogin.setFont(new Font("Tahoma", Font.PLAIN, 15));
		btnLogin.setBounds(80, 160, 100, 30);
		panel.add(btnLogin);
		
		JButton btnExit = new JButton("Exit");
		btnExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				exit();
			}
		});
		btnExit.setFont(new Font("Tahoma", Font.PLAIN, 15));
		btnExit.setBounds(220, 160, 100, 30);
		panel.add(btnExit);
	}

	
	
	

	/**
	 * Perform a login with the entered information.
	 * @throws SQLException
	 * @throws IllegalArgumentException
	 */
	private void login(String name, String password) 
			throws SQLException, IllegalArgumentException, AuthenticationException {
		
		
		//Empty Strings
		if (name.equals(""))
			throw new IllegalArgumentException("The argument name must not be empty!");
		
		if (password.equals(""))
			throw new IllegalArgumentException("The argument password must not be empty!");
		
		
		//Hash the password
		String hashedPassword = PasswordHash.hashPassword(password);

		//Query the password from the db
		String query = "SELECT password FROM hrguys WHERE id = '" + name + "';";
		ResultSet res = dbc.execute(query);

		if (!res.next()) 		//User not registered (empty result)
			throw new AuthenticationException("Wrong user name!");
						
		//Check password
		String dbPassword = res.getString("password");
		if (! dbPassword.equals(hashedPassword)) 
				throw new AuthenticationException("Wrong password!");
			
	}
	
	
	
	/**
	 * Handles errors upon login due to illegal arguments (e.g. empty textfields)
	 * by showing an error message to the user.
	 * @param e The IllegalArgumentException that occured
	 */
	private void handleLoginArgumentError(IllegalArgumentException e) {
		//display an error message
		JOptionPane.showMessageDialog(this, "An error occured!\n"+e.getMessage());
	}
	
	
	

	/**
	 * Handles any SQLException with some error output and also a message dialog for the user
	 * @param e The exception to handle
	 * @param showDialog Determines if a dialog is shown or not
	 */
	private void handleSQLException(SQLException e, boolean showDialog) {
		System.err.println(e.getErrorCode());
		System.err.println(e.getSQLState());
		System.err.println(e.getMessage());
		
		//show an error message
		if (showDialog)
			JOptionPane.showMessageDialog(this, "An error occured!\n"+e.getMessage());
	}
	

	
	/**
	 * Exits the HRGuy-GUI in a safe way after confirmed by the user via
	 * a closing dialog
	 */
	private void exit() {

		//Exit dialog
		int confirm = JOptionPane.showOptionDialog(null,
				"Are you sure to close the application?", "Exit Confirmation",
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, 
	             null, null, null);
		
		//Do not close yet
		if (confirm == JOptionPane.NO_OPTION)
			return;
		
		
		//Close database connection
		try {
			dbc.close();
		} catch (SQLException e) {
			handleSQLException(e, false);
			System.exit(-1);
		}
		
		//Exit frame
		this.dispose();
	}
	
	
}
