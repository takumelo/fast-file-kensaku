package jp.fastkensaku;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Main {

    private JTabbedPane tab;
    private JPanel panel;
    private JFrame settingFrame;
    private JFrame usageFrame;
    private JFrame aboutFrame;

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

    private void createSettingFrame(){
        if (settingFrame == null) {
            settingFrame = new JFrame();
            settingFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            settingFrame.setTitle("ディレクトリ設定");
            settingFrame.setSize(400, 350);
            settingFrame.setLocationByPlatform(true);
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