package RedBox;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.UIManager;
import javax.swing.SwingConstants;
import java.awt.Font;


public class RedBox extends JFrame implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3356889636530658771L;
	
	JPanel panel = null;
	JFileChooser chooser = null;
	JFileChooser passwordFileChooser = null;
	JButton  choosePasswordFile = null;
	JPasswordField password = null;
	JPasswordField password_verify = null;
	JRadioButton encrypt = null;
	JRadioButton decrypt = null;
	
	final String decrypt_label = "decrypt";
	final String encrypt_label = "encrypt";
	final String password_label = "Password:";
	final String password_verify_label = "  Verify:";
	final String password_file_label = "Select file...";
	final String action_encrypt = "Encrypt";
	final String action_decrypt = "Decrypt";
	final String default_decrypt_extension = ".decrypt";
	final String default_encrypt_extension = ".bin";
	final String charset = "UTF-8";
	final String RELEASEDATE = "29 Dec 2020";

	
	public static void main(String[] args) {
		new RedBox(args).setVisible(true);
		
	}
	
	/**
	 * main for program
	 * 
	 * @param args
	 */
	RedBox(String[] args) {
		
		super("RedBox");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		try {
			UIManager.setLookAndFeel(
			        UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
		//setLocationRelativeTo(null);
		panel = new JPanel();
		panel.setLayout(new BorderLayout());
		this.add(panel);
		this.setSize(800, 600);
		
		chooser = new JFileChooser();
		chooser.setMultiSelectionEnabled(true);
		/*
		 * this is coded to take files and directories, however in that mode
		 * it will encrypt an entire filesystem, which could be a mistake. 
		 * if the argument "dangerous" is given it will allow this.
		 */
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		
		if (args.length > 0 && args[0].equals("dangerous")) {
			chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		}
		
		chooser.addActionListener(this);
		chooser.setApproveButtonText(action_encrypt);

		JPanel p = new JPanel();
		p.setLayout(new FlowLayout(FlowLayout.CENTER));
		p.add(createPassword());

		
		panel.add(p,BorderLayout.NORTH);
		panel.add(chooser, BorderLayout.CENTER);

		JLabel version = new JLabel(RELEASEDATE, SwingConstants.CENTER);
		version.setFont(new Font(version.getFont().getName(), Font.PLAIN, 7));
		panel.add(version, BorderLayout.SOUTH);

		panel.doLayout();
		
		// preset th epassword on starting.
		
		if (args.length > 1) {
			password.setText(args[1]);
		}
	}
	
	
	/**
	 * craete the password panel
	 * @return panel with password fields
	 * 
	 */
	JPanel createPassword() {
		
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0; c.gridy = 0;
	
		JLabel label1 = new JLabel(password_label);
		panel.add(label1, c);
		password = new JPasswordField(32);
		choosePasswordFile = new JButton(password_file_label); 
		choosePasswordFile.addActionListener(this);
		
		c.gridx = 1; c.gridy = 0;
		panel.add(password, c);
		c.gridx = 2; c.gridy = 0;
		panel.add(choosePasswordFile);
		
		JLabel label2 = new JLabel(password_verify_label);
		c.gridx = 0;  c.gridy = 1;
		panel.add(label2, c);
		
		label1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
		label2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
		
		password_verify = new JPasswordField(32);
		c.gridx = 1; c.gridy = 1;
		panel.add(password_verify, c);
		c.gridx = 1; c.gridy = 2;
		panel.add(createOptions(), c);
		
		return panel;
	}
	
	
	/**
	 * create options panel
	 * 
	 * @return
	 */
	JPanel createOptions() {
		
		JPanel panel = new JPanel();
		ButtonGroup group = new ButtonGroup();
		encrypt = new JRadioButton(encrypt_label, true);
		decrypt = new JRadioButton(decrypt_label, false);
		group.add(encrypt);
		group.add(decrypt);
		panel.add(encrypt);
		panel.add(decrypt);
		encrypt.addActionListener(this);
		decrypt.addActionListener(this);
		return panel;
	}

	
	/**
	 * action listener for all buttons and controls
	 * 
	 */
	public void actionPerformed(ActionEvent action) {
		
		if (action.getActionCommand().equals(decrypt_label)) {
			chooser.setApproveButtonText(action_decrypt);
		} else if (action.getActionCommand().equals(encrypt_label)) {
			chooser.setApproveButtonText(action_encrypt);
		} else if (action.getActionCommand().equals("ApproveSelection")) {
			
			// check that the passwords match
			if (!checkPasswords()) {
				return;
			}
			
			setEnabled(false);
			
			final File[] fileset = chooser.getSelectedFiles();
			if (fileset.length == 0) {
				setEnabled(true);
				return;
			}
			
			final int numFiles = fileset.length;
			final progressBar pb = new progressBar(numFiles + 1);
			System.out.println("processing " + numFiles + " files");
			
			
			final Thread t = new Thread("file process") { 
				int count = 1;
				public void run() {
				
				encryptfiles(fileset);
				
				pb.end();
				setEnabled(true);
				chooser.setSelectedFiles(new File[]{new File("")});
				chooser.rescanCurrentDirectory();
			} 
			
			/**
			 * encrypt the files or recursively encrypt directories.
			 * @param fileset
			 */
			void encryptfiles(final File[] fileset) {
				
				for (File file : fileset) {
					if (file.isDirectory()) {
						final File[] sub = file.listFiles();
						pb.incMax(sub.length);
						encryptfiles(sub);
					} else if(file.isFile()) {
						encryptfile(file);
					}
				}
			}

			/**
			 * encrypt a single file
			 * @param file
			 */
			void encryptfile(final File file) {

				System.out.println(count + "\t" + file);
				pb.setProgress(count);
				count++;
				
				try {

					RedBoxEngine.mode command;
					File cfile;

					final char[] pass = password.getPassword();

					final byte[] okey = Charset.forName(charset)
							.encode(CharBuffer.wrap(pass)).array();

					EncryptionKey key = new EncryptionKey(okey);

					if (encrypt.isSelected()) {
						command = RedBoxEngine.mode.ENCRYPT;
						cfile = new File(file.getCanonicalPath() + default_encrypt_extension);

						int fileUniquer = 1;
						while (cfile.exists()) {
							cfile = new File(file.getCanonicalPath() + fileUniquer++ + default_encrypt_extension);
						}

					} else {

						command = RedBoxEngine.mode.DECRYPT;
						String fname = file.getCanonicalPath();
						int fileUniquer = 1;

						if (fname.lastIndexOf(".") > -1 ) {
							cfile = new File(fname.substring(0, fname.lastIndexOf(".")));
							while (cfile.exists()) {
								cfile = new File(fname.substring(0, fname.lastIndexOf(".") + fileUniquer++));
							}
						} else {
							cfile = new File(fname + default_decrypt_extension);
							while (cfile.exists()) {
								cfile = new File(fname + fileUniquer++ + default_decrypt_extension);
							}

						}
					}

					final FileInputStream  in  = new FileInputStream(file);
					final FileOutputStream out = new FileOutputStream(cfile);
					System.out.println("creating file:\n" + cfile);

					new RedBoxEngine().go(command, key, in, out);

					System.gc();

					if (file.delete()) {
						System.out.println("\tdeleted " + file);
					} else {
						System.out.println("\tfailed to delete " + file);
					}
					
				} catch (Exception e) {
					
					JOptionPane.showMessageDialog(panel,
						    e.toString(),
						    "Encryption error",
						    JOptionPane.ERROR_MESSAGE);
					e.printStackTrace();
				}
				
			}
			};
			
			t.setName("progress");
			t.start();
			
		} else  if (action.getActionCommand().equals(password_file_label)) {
			
				final JFileChooser fc = new JFileChooser();
				
				if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION ) {
					
					final File ekey = fc.getSelectedFile();
					try {
						final FileReader fr = new FileReader(ekey);
						final CharBuffer cb = CharBuffer.allocate(
								(int)Math.min(Integer.MAX_VALUE/2, ekey.length()));
						fr.read(cb);
						fr.close();
						cb.flip();
						password.setText(cb.toString());
						password_verify.setText(cb.toString());
						
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
								
		} else 
			System.exit(0);
		}
	
	
	/**
	 * check that passwords fields have same value.
	 * 
	 * @return true if they do, display error message and false if not.
	 */
	boolean checkPasswords() {

		boolean result = Arrays.equals(password.getPassword(), password_verify.getPassword());
		
		if (!result) {
			JOptionPane.showMessageDialog(this, 
					"Encryption passwords do not match",
					"Passwords do not Match", JOptionPane.ERROR_MESSAGE);
		}
		
		if (password.getPassword().length == 0) {
			JOptionPane.showMessageDialog(this, 
					"Encryption password is empty",
					"Encryption password is empty", JOptionPane.ERROR_MESSAGE);
			result = false;
		}
		return result;
	}
	
}
