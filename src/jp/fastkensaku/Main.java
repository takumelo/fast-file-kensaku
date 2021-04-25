package jp.fastkensaku;

import org.apache.lucene.queryparser.classic.ParseException;

import java.awt.desktop.SystemEventListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.stream.Stream;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileSystemView;
import javax.swing.table.DefaultTableModel;


public class Main {

    private JTabbedPane tab;
    private JPanel panel;
    private JFrame settingFrame;
    private JFrame usageFrame;
    private JFrame aboutFrame;
    private JPanel progPanel;
    // ディレクトリ設定用
    private JTable tbl;
    private DefaultTableModel tblModel;
    JComboBox dirComboBox;
    DefaultComboBoxModel cmbModel;

    private DBHandler dbHandler;

    private JTextField searchBox;

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

        tab.add("検索", panel);
        return tab;
    }

    /**
     * テキストフィールド作成
     *
     * @return テキストフィールド
     */
    private JTextField createTextField(){
        searchBox = new JTextField(10);
        return searchBox;
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

                LuceneHandler luceneHandler = new LuceneHandler();
                String searchTxt = searchBox.getText();
                try {
                    luceneHandler.search(searchTxt);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                } catch (ParseException parseException) {
                    parseException.printStackTrace();
                }

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
     * applySetting用のSW
     *
     * 参考: https://stackoverflow.com/questions/20260372/swingworker-progressbar
     *      https://docs.oracle.com/javase/6/docs/api/javax/swing/SwingWorker.html
     */
    static class InsertRecurWorker extends SwingWorker<Integer, Integer>{
        private String path;
        private DBHandler dbHandler;
        private long maxFileNum;
        public InsertRecurWorker(DBHandler dbHandler, String path) {
            this.dbHandler = dbHandler;
            this.path = path;
        }
        @Override
        protected Integer doInBackground() throws Exception {
            this.maxFileNum = dbHandler.getFilesCntRecur(this.path);
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            String formatDateTime = now.format(formatter);
            int intFormatDataTime = Integer.parseInt(formatDateTime);
            long cnt = 0;
            try(Stream<Path> stream = Files.walk(Paths.get(path))){
                Stream<Path> ps = stream.filter(Files::isRegularFile);
                Object[] psArray = ps.toArray();
                for(Object p: psArray){
                    dbHandler.insertFiles(path, (Path)p, intFormatDataTime);
                    TikaHandler tikaHandler = new TikaHandler();
                    Path pp = (Path)p;
                    File f = pp.toFile();
                    tikaHandler.parse(f);
                    // luceneの更新
                    String fn = tikaHandler.getFileName();
                    String meta = tikaHandler.getMeta();
                    String ext = tikaHandler.getExtention();
                    String content = tikaHandler.getContent();
                    LuceneHandler luceneHandler = new LuceneHandler();
                    luceneHandler.index(fn, meta, ext, content);

                    cnt += 1;
                    int percentage = (int)(((double)cnt / (double)maxFileNum) * 100);
                    setProgress(percentage);
                }
            }catch(IOException e) {
                e.printStackTrace();
            }
            return 0;
        }
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
            JProgressBar pb = createProgBar();
            pb.setStringPainted(true);
            pb.setString("追加準備中: " + newPath);
            InsertRecurWorker insertRecurWorker = new InsertRecurWorker(dbHandler, newPath);
            insertRecurWorker.addPropertyChangeListener(
                    new PropertyChangeListener() {
                        public  void propertyChange(PropertyChangeEvent evt) {
                            if ("progress".equals(evt.getPropertyName())) {
                                // 進んでるとき
                                pb.setString("追加中: " + newPath);
                                pb.setValue((Integer)evt.getNewValue());
                            }else if("state".equals(evt.getPropertyName())
                                    && (SwingWorker.StateValue.DONE.equals(evt.getNewValue()))){
                                // 終了時
                                progPanel.remove(pb);
                                progPanel.revalidate();
                                progPanel.repaint();
                                Object[] combodata = dbHandler.getAllDirForCmb();
                                cmbModel = new DefaultComboBoxModel(combodata);
                                dirComboBox.setModel(cmbModel);
                            }
                        }
                    });
            progPanel.add(pb);
            progPanel.revalidate();
            progPanel.repaint();
            insertRecurWorker.execute();
        }
        if(rowCnt == 0){
            Object[] combodata = dbHandler.getAllDirForCmb();
            cmbModel = new DefaultComboBoxModel(combodata);
            dirComboBox.setModel(cmbModel);
        }
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

    private JProgressBar createProgBar(){
        JProgressBar pb = new JProgressBar(0, 100);
        //pb.setIndeterminate(false);
        return pb;
    }

    /**
     * アプリ立ち上がり画面の表示
     *
     */
    private void initGui() {
        JFrame frame1 = new JFrame();
        frame1.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 9.0;
        gbc.fill = GridBagConstraints.BOTH;
        frame1.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame1.setTitle("高速ファイル検索君");
        frame1.setJMenuBar(createMenuBar());
        frame1.add(createTab(), gbc);
        gbc.gridy = 1;
        gbc.weighty = 1.0;
        progPanel = new JPanel();
        GridLayout gl = new GridLayout(0,1);
        progPanel.setLayout(gl);
        frame1.add(progPanel, gbc);
        frame1.setSize(600, 400);
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