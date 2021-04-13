package jp.fastkensaku;

import java.io.File;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileSystemView;
import javax.swing.table.DefaultTableModel;


public class Main {

    private JTabbedPane tab;
    private JPanel panel;
    private JFrame settingFrame;
    private JFrame usageFrame;
    private JFrame aboutFrame;
    // ディレクトリ設定用
    private JTable tbl;
    private DefaultTableModel tblModel;

    private DBHandler dbHandler;

    private JMenu createSettingMenu() {
        JMenu fileMenu = new JMenu("設定");
        JMenuItem newItem = new JMenuItem("ディレクトリ設定");
        newItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                createSettingFrame();
            }
        });
        fileMenu.add(newItem);
        return fileMenu;
    }

    private JMenu createHelpMenu(){
        JMenu fileMenu = new JMenu("ヘルプ");
        JMenuItem newItem = new JMenuItem("使い方");
        newItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                createUsageFrame();
            }
        });
        fileMenu.add(newItem);
        newItem = new JMenuItem("アプリについて");
        newItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                createAboutFrame();
            }
        });
        fileMenu.add(newItem);
        return fileMenu;
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(createSettingMenu());
        menuBar.add(createHelpMenu());
        return menuBar;
    }

    private JTabbedPane createTab(){
        tab = new JTabbedPane();
        panel = new JPanel();
        panel.add(createTextField());
        panel.add(createSearchButton());
        tab.add("検索", panel);
        return tab;
    }

    private JTextField createTextField(){
        JTextField field = new JTextField(10);
        return field;
    }

    private JButton createSearchButton(){
        JButton button = new JButton("検索");
        button.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                tab.add("結果", createResultPanel());
                int ind = tab.getTabCount() - 1;
                tab.setTabComponentAt(ind, new ButtonTabComponent(tab));
            }
        });
        return button;
    }

    private JPanel createResultPanel(){
        JPanel panel = new JPanel();
        return panel;
    }

    private void openFileChooser(){
        JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
        jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnValue = jfc.showOpenDialog(null);
        // int returnValue = jfc.showSaveDialog(null);

        String newPath;
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = jfc.getSelectedFile();
            newPath = selectedFile.getAbsolutePath();
            dbHandler.addNewDir(newPath);
        }

        Object data[][] = dbHandler.getAllDir();
        String[] cols = {"ディレクトリ"};
        tblModel = new DefaultTableModel(data, cols);
        tbl.setModel(tblModel);
        tblModel.fireTableDataChanged();
    }

    private void deleteAllDirSetting(){
        dbHandler.deleteAllDir();
        tblModel.setRowCount(0);
        tblModel.fireTableDataChanged();
    }

    private void createSettingFrame(){
        if (settingFrame == null) {
            settingFrame = new JFrame();
            settingFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            settingFrame.setTitle("ディレクトリ設定");
            settingFrame.setSize(400, 350);
            settingFrame.setLocationByPlatform(true);
            settingFrame.setLayout(new GridBagLayout());

            GridBagConstraints gbc = new GridBagConstraints();

            // table作成
            String[] cols = {"ディレクトリ"};
            Object data[][] = dbHandler.getAllDir();
            tblModel = new DefaultTableModel(data, cols);
            tbl = new JTable(tblModel);
            tbl.setModel(tblModel);
            tblModel.fireTableDataChanged();

            // セルの幅を変更したい場合
            //tbl.getColumnModel().getColumn(0).setPreferredWidth(20);
            //tbl.getColumnModel().getColumn(1).setPreferredWidth(2000);

            // セル編集不可に
            tbl.setDefaultEditor(Object.class, null);

            JScrollPane sp = new JScrollPane(tbl);
            //sp.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weightx = 1.0;
            gbc.weighty = 9.0;
            gbc.fill = GridBagConstraints.BOTH;
            settingFrame.add(sp, gbc);

            // add new dir
            JPanel btnPanel = new JPanel();
            JButton newBtn = new JButton("新規追加");
            newBtn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    openFileChooser();
                }
            });
            btnPanel.add(newBtn);

            JButton deleteBtn = new JButton("一括削除");
            deleteBtn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    deleteAllDirSetting();
                }
            });
            btnPanel.add(deleteBtn);

            JButton okBtn = new JButton("設定を閉じる");
            okBtn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    settingFrame.setVisible(false);
                }
            });
            btnPanel.add(okBtn);


            gbc.gridx = 0;
            gbc.gridy = 1;
            gbc.weightx = 1.0;
            gbc.weighty = 1.0;
            gbc.fill = GridBagConstraints.BOTH;
            settingFrame.add(btnPanel, gbc);

            //settingFrame.setContentPane(panel);
            settingFrame.setVisible(true);
        }else{
            settingFrame.setVisible(true);
        }
    }

    private void createUsageFrame(){
        if (usageFrame == null) {
            usageFrame = new JFrame();
            usageFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            usageFrame.setTitle("使い方");
            usageFrame.setSize(400, 350);
            usageFrame.setLocationByPlatform(true);
            usageFrame.setVisible(true);
        }else{
            usageFrame.setVisible(true);
        }
    }

    private void createAboutFrame(){
        if (aboutFrame == null) {
            aboutFrame = new JFrame();
            aboutFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            aboutFrame.setTitle("アプリについて");
            aboutFrame.setSize(400, 350);
            aboutFrame.setLocationByPlatform(true);
            aboutFrame.setVisible(true);
        }else{
            aboutFrame.setVisible(true);
        }
    }

    private void prepareDB(){
        dbHandler = new DBHandler();
    }

    private void initGui() {
        JFrame frame1 = new JFrame();
        frame1.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame1.setTitle("高速ファイル検索君");
        frame1.setJMenuBar(createMenuBar());
        frame1.add(createTab());
        frame1.setSize(400, 350);
        frame1.setLocationByPlatform(true);
        frame1.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Main main = new Main();
                main.prepareDB();
                main.initGui();
            }
        });
    }

}