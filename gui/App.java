package gui;
//Tenkaichi Skill List Remover by ViveTheModder
import java.awt.Desktop;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;
import cmd.Main;

public class App 
{
	public static JLabel fileLabel, fileCntLabel;
	private static File src;
	private static double seconds=0;
	private static int chkBoxCnt=Main.BT3_SKL_LST_LANGS.length;
	private static final Font BOLD = new Font("Tahoma", 1, 24);
	private static final Font BOLD_S = new Font("Tahoma", 1, 12);
	private static final Font MED = new Font("Tahoma", 0, 18);
	private static final String HTML_A_START = "<html><a href=''>";
	private static final String HTML_A_END = "</a></html>";
	private static final String WINDOW_TITLE = "Tenkaichi Skill List Remover";
	private static final Toolkit DEF_TOOLKIT = Toolkit.getDefaultToolkit();

	private static File getFolderFromChooser()
	{
		File folder=null;
		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setDialogTitle("Open Folder with Character Costume Files...");
		while (folder==null)
		{
			int result = chooser.showOpenDialog(chooser);
			if (result==0)
			{
				File temp = chooser.getSelectedFile(); //actually gets the selected folder
				File[] tempPaks = temp.listFiles((dir, name) -> 
				{
					String nameLower = name.toLowerCase();
					//exclude ANM, EFF and Voice PAKs from the PAK filter
					return nameLower.endsWith(".pak") && !(nameLower.contains("anm") || nameLower.contains("eff") || nameLower.contains("voice"));
				});
				if (!(tempPaks==null || tempPaks.length==0)) folder = temp;
				else 
				{
					errorBeep();
					JOptionPane.showMessageDialog(chooser, "This folder does NOT have Character Costume Files! Try again!", WINDOW_TITLE, 0);
				}
			}
			else break;
		}
		return folder;
	}
	private static void errorBeep()
	{
		Runnable runWinErrorSnd = (Runnable) DEF_TOOLKIT.getDesktopProperty("win.sound.exclamation");
		if (runWinErrorSnd!=null) runWinErrorSnd.run();
	}
	private static void setApplication()
	{
		//initialize components
		Box btBox = Box.createHorizontalBox();
		Box vertBox = Box.createVerticalBox();
		ButtonGroup btnGrp = new ButtonGroup();
		GridBagConstraints gbc = new GridBagConstraints();
		JButton applyBtn = new JButton("Remove Selected");
		JCheckBox[] checkboxes = new JCheckBox[chkBoxCnt];
		JFrame frame = new JFrame(WINDOW_TITLE);
		JLabel btLabel = new JLabel("Specify Game Version:");
		JLabel chkBoxLabel = new JLabel("Skill Lists to Remove:");
		JMenuBar menuBar = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		JMenu helpMenu = new JMenu("Help");
		JMenuItem about = new JMenuItem("About");
		JMenuItem open = new JMenuItem("Open Folder...");
		JPanel panel = new JPanel();
		JRadioButton bt2Btn = new JRadioButton("Budokai Tenkaichi 2");
		JRadioButton bt3Btn = new JRadioButton("Budokai Tenkaichi 3");
		//set component properties and add components
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		panel.setLayout(new GridBagLayout());
		btLabel.setFont(BOLD);
		btLabel.setHorizontalAlignment(SwingConstants.CENTER);
		chkBoxLabel.setFont(BOLD);
		chkBoxLabel.setHorizontalAlignment(SwingConstants.CENTER);
		applyBtn.setFont(MED);
		bt2Btn.setFont(BOLD_S);
		bt3Btn.setFont(BOLD_S);
		open.setToolTipText("The folder (and its subfolders) must have Character Costume Files.");
		btnGrp.add(bt2Btn);
		btnGrp.add(bt3Btn);
		btBox.add(bt2Btn);
		btBox.add(Box.createHorizontalStrut(10));
		btBox.add(bt3Btn);
		panel.add(btLabel,gbc);
		panel.add(new JLabel(" "),gbc);
		panel.add(btBox);
		panel.add(new JLabel(" "),gbc);
		panel.add(new JLabel(" "),gbc);
		panel.add(chkBoxLabel);
		panel.add(new JLabel(" "),gbc);
		for (int i=0; i<checkboxes.length; i++)
		{
			checkboxes[i] = new JCheckBox("Skill List "+(i+1));
			checkboxes[i].setFont(MED);
			vertBox.add(new JLabel(" "),gbc);
			vertBox.add(checkboxes[i],gbc);
		}
		panel.add(vertBox,gbc);
		panel.add(new JLabel(" "),gbc);
		panel.add(applyBtn);
		fileMenu.add(open);
		helpMenu.add(about);
		menuBar.add(fileMenu);
		menuBar.add(helpMenu);
		//add action listeners
		about.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				Box horBox = Box.createHorizontalBox();
				JLabel label = new JLabel("Made by: ");
				JLabel author = new JLabel(HTML_A_START+"ViveTheModder"+HTML_A_END);
				author.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						try {
							Desktop.getDesktop().browse(new URI("https://github.com/ViveTheModder"));
						} catch (IOException | URISyntaxException e1) {
							errorBeep();
							JOptionPane.showMessageDialog(frame, e1.getClass().getSimpleName()+": "+e1.getMessage(), "Exception", 0);
						}
					}});
				horBox.add(label);
				horBox.add(author);
				JOptionPane.showMessageDialog(null, horBox, WINDOW_TITLE, 1);
			}
		});
		applyBtn.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				for (int i=0; i<chkBoxCnt; i++)
				{
					if (checkboxes[i].isSelected()) Main.sklLstIds[i]=i;
					else Main.sklLstIds[i]=-1;
				}
				if (src!=null)
				{
					frame.setEnabled(false);
					setProgress(frame);
				}
				else
				{
					errorBeep();
					JOptionPane.showMessageDialog(frame, "No folder has been provided!", WINDOW_TITLE, 0);
				}
			}
		});
		bt2Btn.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				for (int i=0; i<checkboxes.length; i++)
				{
					if (i<Main.BT2_SKL_LST_LANGS.length) checkboxes[i].setText(Main.BT2_SKL_LST_LANGS[i]);
					else
					{
						vertBox.remove(checkboxes[i]);
						vertBox.updateUI();
						chkBoxCnt=8;
					}
				}
			}
		});
		bt3Btn.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				for (int i=0; i<checkboxes.length; i++) checkboxes[i].setText(Main.BT3_SKL_LST_LANGS[i]);
				if (chkBoxCnt==8)
				{
					checkboxes[8].setSelected(false);
					vertBox.add(checkboxes[8],gbc);
					vertBox.updateUI();
					chkBoxCnt++;
				}
			}
		});
		open.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				try {
					src = getFolderFromChooser();
					if (src!=null) frame.setTitle(WINDOW_TITLE+" - "+src.getCanonicalPath());
				} catch (Exception e1) {
					errorBeep();
					JOptionPane.showMessageDialog(frame, e1.getClass().getSimpleName()+": "+e1.getMessage(), "Exception", 0);
				}
			}
		});
		//set frame properties
		frame.add(panel);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setJMenuBar(menuBar);
		frame.setLocationRelativeTo(null);
		frame.setSize(512,768);
		frame.setVisible(true);
	}
	private static void setProgress(JFrame frame)
	{
		Main.fileCnt=0;
		seconds=0;
		//initialize components
		JDialog loading = new JDialog();
		JPanel panel = new JPanel();
		JLabel label1 = new JLabel("Working on:");
		JLabel label2 = new JLabel("Time elapsed:");
		fileLabel = new JLabel(" "); fileCntLabel = new JLabel("Overwritten Costumes: 0");
		JLabel timeLabel = new JLabel();
		GridBagConstraints gbc = new GridBagConstraints();
		Timer timer = new Timer(100, e -> 
		{
			seconds+=0.1;
			timeLabel.setText((int)(seconds/3600)+"h"+(int)(seconds/60)%60+"m"+String.format("%.1f",seconds%60)+"s");
		});
		timer.start();
		//set component properties
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		fileLabel.setHorizontalAlignment(SwingConstants.CENTER);
		fileCntLabel.setHorizontalAlignment(SwingConstants.CENTER);
		label1.setHorizontalAlignment(SwingConstants.CENTER);
		label2.setHorizontalAlignment(SwingConstants.CENTER);
		timeLabel.setHorizontalAlignment(SwingConstants.CENTER);
		fileLabel.setFont(MED);
		fileCntLabel.setFont(MED);
		label1.setFont(BOLD);
		label2.setFont(BOLD);
		timeLabel.setFont(MED);
		panel.setLayout(new GridBagLayout());
		//add components
		panel.add(label1,gbc);
		panel.add(fileLabel,gbc);
		panel.add(new JLabel(" "),gbc);
		panel.add(label2,gbc);
		panel.add(timeLabel,gbc);
		panel.add(new JLabel(" "),gbc);
		panel.add(fileCntLabel,gbc);
		//add window listener
		loading.addWindowListener(new WindowAdapter()
		{
			@Override
            public void windowClosed(WindowEvent e) 
			{
				frame.setEnabled(true);
				timer.stop();
            }
		});
		//set dialog & frame properties
		loading.add(panel);
		loading.setTitle(WINDOW_TITLE+" - Progress Report");
		loading.setSize(1024,256);
		loading.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		loading.setLocationRelativeTo(null);
		loading.setVisible(true);
		
		SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>()
		{
			@Override
			protected Void doInBackground() throws Exception 
			{
				long start = System.currentTimeMillis();
				Main.traverse(src);
				long finish = System.currentTimeMillis();
				double time = (finish-start)/1000.0;
				loading.setVisible(false); 
				loading.dispose();
				JOptionPane.showMessageDialog(null, Main.fileCnt+" character costume files overwritten successfully in "+time+" seconds!", WINDOW_TITLE, 1);
				frame.setEnabled(true);
				timer.stop();
				return null;
			}
		};
		worker.execute();
	}
	public static void main(String[] args) 
	{
		try 
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			setApplication();
		} 
		catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) 
		{
			e.printStackTrace();
		}
	}
}
