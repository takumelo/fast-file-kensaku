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
    JComboBox dirComboBox;
    DefaultComboBoxModel cmbModel;

    private DBHandler dbHandler;

    /**
     * 設定のメニュー作成
     *
     * @return 設定メニュー
     */
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

    /**
     * ヘルプメニューの作成
     *
     * @return ヘルプメニュー
     */
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

    /**
     * メニューバー作成(JMenuを子に持たせる)
     *
     * @return メニューバー
     */
    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(createSettingMenu());
        menuBar.add(createHelpMenu());
        return menuBar;
    }

    /**
     * メイン画面のタブ作成
     *
     * @return タブ
     */
    private JTabbedPane createTab(){
        tab = new JTabbedPane();
        panel = new JPanel();
        JPanel childTextPanel = new JPanel();
        JPanel childBtnPanel = new JPanel();

        GridBagConstraints gbc = new GridBagConstraints();
        panel.setLayout(new GridBagLayout());
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 9.0;
        gbc.fill = GridBagConstraints.BOTH;
        childTextPanel.add(createTextField());

        dirComboBox = new JComboBox();
        Object[] combodata = dbHandler.getAllDirForCmb();
        cmbModel = new DefaultComboBoxModel(combodata);
        dirComboBox.setModel(cmbModel);
        childTextPanel.add(dirComboBox);

        panel.add(childTextPanel, gbc);
        gbc.gridy = 1;
        gbc.weighty = 1.0;
        childBtnPanel.add(createSearchButton());
        panel.add(childBtnPanel, gbc);

        // TODO: プログレスバーを下部に設置

        tab.add("検索", panel);
        return tab;
    }

    /**
     * テキストフィールド作成
     *
     * @return テキストフィールド
     */
    private JTextField createTextField(){
        JTextField field = new JTextField(10);
        return field;
    }

    /**
     * 検索ボタンの作成
     *
     * @return 検索ボタン
     */
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

    /**
     * 検索結果用のパネル作成
     *
     * @return パネル
     */
    private JPanel createResultPanel(){
        JPanel panel = new JPanel();
        return panel;
    }

    /**
     * ディレクトリ選択画面を開き、JTableに登録
     *
     * @return なし
     */
    private void openFileChooser(){
        JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
        jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnValue = jfc.showOpenDialog(null);
        // int returnValue = jfc.showSaveDialog(null);

        String newPath;
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = jfc.getSelectedFile();
            newPath = selectedFile.getAbsolutePath();
            String[] cols = {"ディレクトリ"};
            Object[] row = {newPath};
            tblModel.addRow(row);
            tbl.setModel(tblModel);
            tblModel.fireTableDataChanged();
        }
    }

    /**
     * ディレクトリ設定のDBとJテーブルを削除
     *
     */
    private void deleteAllDirSetting(){
        tblModel.setRowCount(0);
        tblModel.fireTableDataChanged();
    }

    /**
     * テーブルモデルにあるディレクトリをDBに登録
     *
     */
    private void applySetting(){
        dbHandler.deleteAllDir();
        int rowCnt = tblModel.getRowCount();
        for(int cnt = 0; cnt < rowCnt; cnt++){
            Object data = tblModel.getValueAt(cnt, 0);
            String newPath = data.toString();
            dbHandler.addNewDir(newPath);
            dbHandler.createDirTbl(newPath);
            System.out.println(dbHandler.getFilesCntRecur(newPath));
            //TODO: 時間がかかるので、swingworkerとプログレスバーでごまかす
            dbHandler.insertFilesRecur(newPath);
        }
        Object[] combodata = dbHandler.getAllDirForCmb();
        cmbModel = new DefaultComboBoxModel(combodata);
        dirComboBox.setModel(cmbModel);
    }

    /**
     * 設定画面作成、表示
     *
     */
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
            //tbl.setModel(tblModel);
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

            JButton cancelBtn = new JButton("キャンセル");
            cancelBtn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    settingFrame.setVisible(false);
                }
            });
            btnPanel.add(cancelBtn);
            JButton applyBtn = new JButton("設定反映");
            applyBtn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    applySetting();
                    settingFrame.setVisible(false);
                }
            });
            btnPanel.add(applyBtn);


            gbc.gridx = 0;
            gbc.gridy = 1;
            gbc.weightx = 1.0;
            gbc.weighty = 1.0;
            gbc.fill = GridBagConstraints.BOTH;
            settingFrame.add(btnPanel, gbc);

            //settingFrame.setContentPane(panel);
            settingFrame.setVisible(true);
        }else{
            // table作成
            tblModel.setRowCount(0);
            Object data[][] = dbHandler.getAllDir();
            for (int i = 0; i < data.length; i++) {
                tblModel.addRow(data[i]);
            }
            tblModel.fireTableDataChanged();

            settingFrame.setVisible(true);
        }
    }

    /**
     * 使い方画面作成、表示
     *
     */
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

    /**
     * アプリについての画面作成、表示
     *
     */
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

    /**
     * DBHandlerインスタンス作成
     *
     */
    private void prepareDB(){
        dbHandler = new DBHandler();
    }

    private JPanel createProgBar(){
        JProgressBar pb = new JProgressBar();
        pb.setIndeterminate(false);
        int max = 1000;
        pb.setMaximum(max);
        JPanel p = new JPanel();
        p.add(pb);
        return p;
    }

    /**
     * アプリ立ち上がり画面の表示
     *
     */
    private void initGui() {
        JFrame frame1 = new JFrame();
        // TODO: gridbaglayoutを使って、タブの下にいい感じにプログレスバーをやる
        frame1.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame1.setTitle("高速ファイル検索君");
        frame1.setJMenuBar(createMenuBar());
        frame1.add(createTab());
        frame1.add(createProgBar());
        frame1.setSize(400, 350);
        frame1.setLocationByPlatform(true);
        frame1.setVisible(true);
    }

    /**
     * メイン文
     */
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