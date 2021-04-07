package jp.fastkensaku;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Main {

    private JTabbedPane tab;
    JPanel panel;

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
                main.initGui();
            }
        });
    }

}