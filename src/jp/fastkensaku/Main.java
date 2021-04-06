package jp.fastkensaku;

import javax.swing.*;

public class Main {
    private JFrame frame1;
    private JTabbedPane tabbedPane1;
    private JMenuBar menuBar;
    private JMenu menu, submenu;
    private JMenuItem menuItem;

    private JMenu createSettingMenu() {
        JMenu fileMenu = new JMenu("設定");
        JMenuItem newItem = new JMenuItem("ディレクトリ設定");
        fileMenu.add(newItem);
        return fileMenu;
    }

    private JMenu createHelpMenu(){
        JMenu fileMenu = new JMenu("ヘルプ");
        JMenuItem newItem = new JMenuItem("使い方");
        fileMenu.add(newItem);
        newItem = new JMenuItem("アプリについて");
        fileMenu.add(newItem);
        return fileMenu;
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(createSettingMenu());
        menuBar.add(createHelpMenu());
        return menuBar;
    }

    private void initGui() {
        frame1 = new JFrame();
        frame1.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame1.setTitle("高速ファイル検索君");
        frame1.setJMenuBar(createMenuBar());
        frame1.setSize(400, 350);
        frame1.setLocationByPlatform(true);
        frame1.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Main main = new Main();
                main.initGui();
            }
        });
    }

}