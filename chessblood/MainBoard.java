/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
open source: GPLv2+
 */
package chessblood;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.plaf.FontUIResource;

/**
 * @author zorrow2017
 * @create at 2020-11-19 14:28:18
 * @encoding charset utf-8
 * @description MainBoard
 */
public class MainBoard extends JFrame {

    //the global final control varables
    private final int BOARD_ROWS = 10, BOARD_COLS = 9, BOARD_SEP = 16, BOARD_NULL = -1;
    private final int BOARD_WIDTH, BOARD_HEIGHT;
    
    /**
     * int[][] map;
     * chess map; map=new int[10][9];
     * 0-15: {"車", "馬", "象", "仕", "將", "仕", "象", "馬", "車", "砲", "砲", "卒", "卒", "卒", "卒", "卒",}
     * 16-31: {"車", "馬", "相", "仕", "帥", "仕", "相", "馬", "車", "砲", "砲", "兵", "兵", "兵", "兵", "兵",}
     * -1|>=32: null
     */
    private volatile int[][] map;  
    
    //the components of menu
    private javax.swing.JMenuBar menuMainBar;
    private javax.swing.JMenu menuFile;
    private javax.swing.JMenu menuSetting;
    private javax.swing.JMenu menuPlay;
    private javax.swing.JMenu menuHelp;
    private javax.swing.JMenuItem mpNewStart;  //menuPlay.newStart
    private javax.swing.JMenuItem mpPauseResume, mppr2;
    private javax.swing.JMenuItem mpUndo;
    private javax.swing.JMenuItem mfExit;
    
    //the components of board
    private JLayeredPane layerTwo;
    private MainChessBoard board;
    private ChessItem items;
    private JPanel rightMenu;
    private JTextArea rightMenuLabel;
    private JTextArea rightMenuPoem;
    private JButton rmUndo;
    private JButton rmEnds;

    
    public MainBoard() {
        this(1200, 850);
    }

    public MainBoard(int width, int height) {
        this.BOARD_WIDTH = width;
        this.BOARD_HEIGHT = height;
        
        initComponents();
        
        super.setVisible(true);
        super.setTitle("中國象棋v加血版");
        super.setSize(this.BOARD_WIDTH, this.BOARD_HEIGHT);
        super.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    
    /**
     * the main function to draw the chess window
     */
    private void initComponents() {
//-------------------------------------
//|      main menu              - O X |
//-------------------------------------
//|                           |       |
//|  main chess board         | right |
//|                           |       |
//|     and                   | menu  |
//|                           |       |
//|  chess items              |       |
//|                           |       |
//|                           |       |
//-------------------------------------
        
        drawMainMenu();
        
        drawMainBoard();
        
        drawChessItem();
        
        drawRightMenu();
        
    }

    
    private void drawBoardSpecial() {
        initMap();
    }

    /**
     * to initialize this.map, fellowing the classic Chinese Chess locations;
     */
    public void initMap() {
        map = new int[this.BOARD_ROWS][this.BOARD_COLS];
        for (int i = 0; i < this.BOARD_ROWS; i++) {
            if (i == 0) {
                //to fill black item
                for (int j = 0; j < this.BOARD_COLS; j++) {
                    map[i][j] = j;
                }
            } else if (i + 1 == this.BOARD_ROWS) {
                //to fill red item
                for (int j = 0; j < this.BOARD_COLS; j++) {
                    map[i][j] = this.BOARD_SEP + j;
                }
            } else {
                Arrays.fill(map[i], this.BOARD_NULL);
            }
        }
        //to fill rest black item ("砲", "卒")
        map[2][1] = 9;
        map[2][7] = 10;
        for (int j = 0, i = 0; j < this.BOARD_COLS; j += 2, i++) {
            map[3][j] = 11 + i;
        }
        //to fill rest red item ("砲", "兵")
        map[7][1] = this.BOARD_SEP + 9;
        map[7][7] = this.BOARD_SEP + 10;
        for (int j = 0, i = 0; j < this.BOARD_COLS; j += 2, i++) {
            map[6][j] = this.BOARD_SEP + 11 + i;
        }
    }

    public void changeMap(int fromX, int fromY, int toX, int toY) {
    }

    
    /**
     * to draw main menu
     */
    private void drawMainMenu() {
        //to new
        menuMainBar = new javax.swing.JMenuBar();
        menuFile = new javax.swing.JMenu("File");
        menuSetting = new javax.swing.JMenu("Setting");
        menuPlay = new javax.swing.JMenu("play");
        menuHelp = new javax.swing.JMenu("help");
        mfExit = new javax.swing.JMenuItem("exit");
        mpNewStart = new javax.swing.JMenuItem("new start");
        mpUndo = new JMenuItem("undo");
        mpPauseResume = new javax.swing.JMenuItem("paause/resume");
        mppr2 = new JMenuItem("pause/resume(backup)");
        
        //to set accelerator function {exit=Ctrl+X, start=Ctrl+J, pause=Ctrl+SPACE Ctrl+P, undo=Ctrl+Z,,}
        mfExit.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_X, java.awt.event.InputEvent.CTRL_MASK));
        mpNewStart.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_J, java.awt.event.InputEvent.CTRL_MASK));
        mpUndo.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Z, java.awt.event.InputEvent.CTRL_MASK));
        mpPauseResume.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_SPACE, java.awt.event.InputEvent.CTRL_MASK));
        mppr2.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_P, java.awt.event.InputEvent.CTRL_MASK));
        mppr2.setVisible(false);
        
        //to attach listener
        mfExit.addActionListener((ActionEvent al) -> {
            System.exit(0);
        });
        mpNewStart.addActionListener((ActionEvent al) -> {
            //not yet available
            //MainBoard.this.initComponents();
        });
        mpUndo.addActionListener((ActionEvent al) -> {
            //debug code
            System.out.println("csize=%d, %dx%d\n");//,csize,px,py);
            map[5][3] = 4;
            map[0][7] = (int) (Math.random() * 32);
            board.repaint();
        });
        
        //to join hierarchy
        menuMainBar.add(menuFile);
        menuMainBar.add(menuSetting);
        menuMainBar.add(menuPlay);
        menuMainBar.add(menuHelp);
        menuFile.add(mfExit);
        menuPlay.add(mpNewStart);
        menuPlay.add(mpPauseResume);
        menuPlay.add(mppr2);
        menuPlay.add(mpUndo);
        //board.add(menuMainBar,BorderLayout.NORTH);
        this.setJMenuBar(menuMainBar);
    }

    
    /**
     * to draw main chess board
     */
    private void drawMainBoard() {
        //the two layer(mainBoard and chessItem)
        layerTwo = new JLayeredPane();
        layerTwo.setBounds(0, 0, Math.min(this.BOARD_WIDTH,this.BOARD_HEIGHT), Math.min(this.BOARD_WIDTH,this.BOARD_HEIGHT));
        rightMenu = new JPanel();
        this.setLayout(new BorderLayout());
        this.add(layerTwo, BorderLayout.CENTER);
        this.add(rightMenu, BorderLayout.EAST);
        
        //to find out how much px of each small cell;
        int cols = this.BOARD_COLS + 2, rows = this.BOARD_ROWS + 2;
        int csize = Math.min(this.BOARD_HEIGHT * 4 / 5 / (cols - 1), this.BOARD_WIDTH * 4 / 5 / (rows - 1));
        
        //
        board = new MainChessBoard(csize, rows, cols);
        layerTwo.add(board, 100, -1);
    }
    

    /**
     * to draw chess items
     */
    private void drawChessItem() {
        //to initialize this.map
        drawBoardSpecial();
        
        //to find out how much px of each small cell;
        int cols = this.BOARD_COLS + 2, rows = this.BOARD_ROWS + 2;
        int csize = Math.min(this.BOARD_HEIGHT * 4 / 5 / (cols - 1), this.BOARD_WIDTH * 4 / 5 / (rows - 1));
        
        //
        items = new ChessItem(csize, map);
        layerTwo.add(items, 100, 0);
    }

    
    /**
     * to draw right menu
     */
    private void drawRightMenu() {
        //to add boxLayout for rightMenu
        BoxLayout boxLayout = new BoxLayout(rightMenu, BoxLayout.Y_AXIS);
        rightMenu.setLayout(boxLayout);
        
        //to say hello world
        String hello = "<p>\n战国烽火兵戈，\n君王封侯拜帅。\n点齐五万大军，\n誓破千里长风。\n\n武将运筹帷幄，\n精兵三千出奇。\n谁胜谁负未可知，\n不要拘泥于传统的中国象棋套路。\n</p>\n\n";
        rightMenuLabel = new JTextArea();
        rightMenuLabel.setColumns(8);
        rightMenuLabel.setRows(24);
        rightMenuLabel.setLineWrap(true);
        rightMenuLabel.setText(hello);
        
        //to add button-undo
        rmUndo = new JButton("undo");
        rmUndo.addActionListener((ActionEvent al) -> {
            items.undoChess();
        });
        
        //to add button-ends
        rmEnds = new JButton("ends");
        rmEnds.addActionListener((ActionEvent al) -> {
            items.endsChessTurn();
        });
        
        //to add poem text area
        rightMenuPoem = new JTextArea();
        rightMenuPoem.setColumns(8);
        rightMenuPoem.setRows(16);
        rightMenuPoem.setLineWrap(true);
        
        //
        rightMenu.add(rmUndo);
        rightMenu.add(rmEnds);
        rightMenu.add(new JScrollPane(rightMenuLabel));
        rightMenu.add(new JScrollPane(rightMenuPoem));
    }
    
    
    /**
     * @param args the command line arguments
     * @param notAMainClass class MainBoard is not a main class
     */
    public static void main(String[] args, int notAMainClass) {
        try {
            SwingUtilities.invokeAndWait(() -> {
                MainBoard mb = new MainBoard();
            });
        } catch (StackOverflowError | Exception ex) {
            Logger.getLogger(MainBoard.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(1);
        }
    }
}


class MainChessBoard extends JPanel {

    private int csize;
    private int cols, rows;
    public final String sep = "楚河 汉界";

    
    public MainChessBoard(int cell_size, int rows, int cols) {
        this.csize = cell_size;
        this.rows = rows;
        this.cols = cols;
        //
        super.setBounds(0, 0, cell_size * cols, cell_size * rows);
        super.setOpaque(false);
    }

    
    @Override
    public void paint(Graphics gr) {
        //@Important
        super.paint(gr);
        
        //to draw board lines (10行9列，棋盘4个边缘外预留一个单元格大小的空间)
        for (int i = 0; i < rows; i++) {
            gr.drawLine(csize, csize * i, csize * (cols - 2), csize * i);
        }
        for (int i = 0; i < cols; i++) {
            gr.drawLine(csize * i, csize, csize * i, csize * (rows - 2));
        }
        
        //to draw "楚河汉界"
        gr.setColor(new Color(0, 255, 0, 10));
        gr.fillRect(csize, csize * (rows / 2 - 1), csize * (cols - 2 - 1), csize);
        gr.setColor(Color.BLACK);
        Font fonts = new FontUIResource("隶书", Font.PLAIN, csize);
        gr.setFont(fonts);
        gr.drawString(sep, (int) (csize * (cols - sep.length() - 1) / 2.0), csize * (rows / 2));
        
        //to draw special camps in board (炮、卒、兵的初始位置的小标记)
        drawCamp(gr, 1, csize * 2, csize * 3);
        drawCamp(gr, 1, csize * 8, csize * 3);
        for (int i = 0; i < 5; i++) {
            drawCamp(gr, 1, csize * (1 + i * 2), csize * 4);
            drawCamp(gr, 1, csize * (1 + i * 2), csize * 7);
        }
        drawCamp(gr, 1, csize * 2, csize * 8);
        drawCamp(gr, 1, csize * 8, csize * 8);
        
        //to draw palace in board (将、帅所在营帐)
        gr.drawLine(csize * (cols / 2 - 1), csize, csize * (cols / 2 - 1 + 2), csize * 3);
        gr.drawLine(csize * (cols / 2 - 1), csize * 3, csize * (cols / 2 - 1 + 2), csize);
        gr.drawLine(csize * (cols / 2 - 1), csize * (rows - 4), csize * (cols / 2 - 1 + 2), csize * (rows - 2));
        gr.drawLine(csize * (cols / 2 - 1), csize * (rows - 2), csize * (cols / 2 - 1 + 2), csize * (rows - 4));
        gr.setColor(new Color(255, 255, 0, 10));
        gr.fillRect(csize * (cols / 2 - 1), csize, csize * 2, csize * 2);
        gr.fillRect(csize * (cols / 2 - 1), csize * (rows - 4), csize * 2, csize * 2);
    }

    protected void drawCamp(Graphics gr, int minSize, int px, int py) {
//        |
//        |
//        |
//      | | |
//   ___| | |___
//________|____________
//   ___  |  ___
//      | | |
//      | | |
//        |
//        |
//        |
//the campus effect
        gr.drawLine(px - 5, py - 2, px - 2, py - 2);
        gr.drawLine(px - 2, py - 2, px - 2, py - 4);
        gr.drawLine(px + 2, py - 2, px + 5, py - 2);
        gr.drawLine(px + 2, py - 2, px + 2, py - 4);
        gr.drawLine(px - 5, py + 2, px - 2, py + 2);
        gr.drawLine(px - 2, py + 3, px - 2, py + 5);
        gr.drawLine(px + 2, py + 2, px + 5, py + 2);
        gr.drawLine(px + 2, py + 3, px + 2, py + 5);
    }

    public void drawChessItem(int itemId, int posX, int posY, int csize, int blood) {
        //gr.drawString(BOARD_SPECIAL_NAME[itemId/2][itemId%2], csize*posX, csize);
    }
}
