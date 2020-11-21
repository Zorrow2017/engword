/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chessblood;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.plaf.FontUIResource;

/**
 * @author zorrow2017
 * @create at 2020-11-19 17:40:20
 * @description MainChessItem
 * @Chinese poem:<p>
 * 战国烽火兵戈，君王封侯拜帅。点齐五万大军，誓破千里长风。武将运筹帷幄，精兵三千出奇。谁胜谁负未可知，不要拘泥于传统的中国象棋套路。
 * </p>
 */
public class ChessItem extends JPanel {

    /**
     * black:   0-15    {"車", "馬", "象", "仕", "將", "仕", "象", "馬", "車", "砲", "砲", "卒", "卒", "卒", "卒", "卒",}
     * red:     16-31   {"車", "馬", "相", "仕", "帥", "仕", "相", "馬", "車", "砲", "砲", "兵", "兵", "兵", "兵", "兵",}
     * blank:   -1      null
     */
    private final String[][] CHESS_NAME = new String[][]{{"車", "馬", "象", "仕", "將", "仕", "象", "馬", "車", "砲", "砲", "卒", "卒", "卒", "卒", "卒",},
    {"車", "馬", "相", "仕", "帥", "仕", "相", "馬", "車", "砲", "砲", "兵", "兵", "兵", "兵", "兵",}};
    
    
    //the blood attribute of every item, {max_blood, current_blood, blood_recover_rate, attack_blood_rate, current_blood_of_last_round}
    private int[][] chess_blood_max = new int[][]{{100, 100, 1000, 200, 100, 200, 1000, 100, 100, 100, 100, 10000, 10000, 10000, 10000, 10000},
    {100, 100, 1000, 200, 100, 200, 1000, 100, 100, 100, 100, 10000, 10000, 10000, 10000, 10000}};
    private int[][] chess_blood = new int[][]{{100, 100, 1000, 200, 100, 200, 1000, 100, 100, 100, 100, 10000, 10000, 10000, 10000, 10000},
    {100, 100, 1000, 200, 100, 200, 1000, 100, 100, 100, 100, 10000, 10000, 10000, 10000, 10000}}, cb2;
    private final int[][] CHESS_BLOOD_RECOVER = new int[][]{{100, 100, 2000, 1000, 100, 1000, 2000, 100, 100, 100, 100, 10000, 10000, 10000, 10000, 10000},
    {100, 100, 2000, 1000, 100, 1000, 2000, 100, 100, 100, 100, 10000, 10000, 10000, 10000, 10000}};
    private final int[][] CHESS_ATTACK = new int[][]{{30, 20, 20, 50, 20, 50, 20, 20, 30, 20, 20, 10, 10, 10, 10, 10},
    {30, 20, 20, 50, 20, 50, 20, 20, 30, 20, 20, 10, 10, 10, 10, 10}};
    
    
    //the map[10][9] of game, and map of last round
    private volatile int[][] map, map2;
    
    
    /**
     * undoMode int
     * ={0:nothing, 1:move, 2:blood, 3:kill(blood+move), 4:,,}
     */
    private volatile int undoMode = 0;  
    
    //the current country, active position at (movex,movey)
    public volatile int curcountry, movex, movey;
    
    //the item circle radius, each cell edge size
    private int cr, csize;
    //the varables to control detailed display style
    private final int SEP = 16;
    public static int CIRCLE_THICK = 2, MIN_SPACE = 5;
    public static Color[] COUNTRY = new Color[]{Color.BLACK, Color.RED};

    
    public ChessItem(int cell_size, int[][] map) {
        this.map = map;
        this.curcountry = 1;
        this.movex = -1;
        this.movey = -1;
        this.csize = cell_size;
        this.cr = getRadiusFromSize(cell_size);
        super.setBounds(0, 0, cell_size * (map[0].length + 1), cell_size * (map.length + 1));
        super.setOpaque(false);
        
        //@Important
        //to play the game, this is core code 核心代码
        super.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent ev) {
                //to prepare calculated varables
                int[][] map = ChessItem.this.map;
                int csize = ChessItem.this.csize;
                int px = ev.getX(), py = ev.getY();
                int indexx = (py * 2 - csize) / 2 / csize, indexy = (px * 2 - csize) / 2 / csize;
                
                //towho   item, the destination position to move or attack
                //from  item, the current active item at position(movex,movey)
                int towho;
                int from;
                if (!(indexx>=0 && indexx<map.length && indexy>=0 && indexy<map[0].length)){
                    return ;
                }
                towho = map[indexx][indexy];
                if (movex >= 0 && movey >= 0) {
                    from = map[movex][movey];
                } else {
                    from = -1;
                }
                
                if (isChessItem(towho)) {
                //if you click a chess item
                    if (towho / SEP == curcountry) {
                        
                    //if the aimed item is a item of our country, then set aimed item active
                        movex = indexx;
                        movey = indexy;
                        ChessItem.this.repaint();
                        
                    } else if (from >= 0 && from / SEP == curcountry) {
                        
                    //if the aimed item is an enemy, then attack or kill him
                        cb2 = deepCopy(chess_blood);
                        undoMode = 2;
                        int blood_rest = chess_blood[towho / SEP][towho % SEP] - CHESS_ATTACK[curcountry][from % SEP];
                        chess_blood[towho / SEP][towho % SEP] = blood_rest;
                        if (blood_rest <= 0) {
                            undoMode = 3;
                            map2 = deepCopy(map);
                            map[indexx][indexy] = from;
                            map[movex][movey] = -1;
                            movex = indexx;
                            movey = indexy;
                        }
                        curcountry = (curcountry + 1) % (CHESS_NAME.length);
                        ChessItem.this.repaint();
                        
                    }
                } else if (from >= 0 && from / SEP == curcountry) {
                //if you click a space place
                    if (ChessItem.this.canMove(from, indexx, indexy)) {
                        
                    //to move active item to the aimed place
                        undoMode = 1;
                        map2 = deepCopy(map);
                        map[indexx][indexy] = from;
                        map[movex][movey] = -1;
                        movex = indexx;
                        movey = indexy;
                        curcountry = (curcountry + 1) % (CHESS_NAME.length);
                        ChessItem.this.repaint();
                        
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent paramMouseEvent) {
            }

            @Override
            public void mouseReleased(MouseEvent paramMouseEvent) {
            }

            @Override
            public void mouseEntered(MouseEvent paramMouseEvent) {
            }

            @Override
            public void mouseExited(MouseEvent paramMouseEvent) {
            }
        });
    }
    
    /**
    * to judge if the active item can move to (indexX,indexY)
    * according to traditional Chinese chess rules
    * such as 馬走日，象飞田，車横冲直撞
    * <br>
    * but unfortunately, not yet available
    */
    private boolean canMove(int itemId, int indexX, int indexY) {
        boolean aim = true;
        return aim;
    }

    public static int getRadiusFromSize(int csize) {
        int radius;
        radius = csize / 2 - 5;
        return radius;
    }

    /**
     * to judge if itemId is a legal item according to this.CHESS_NAME
     * @param itemId 0-15,16-31,else
     * @return true if itemId=(0-15,16-31), else false
     */
    public boolean isChessItem(int itemId) {
        boolean aim = true;
        if (itemId < 0) {
            aim = false;
        }
        int sumid = 0;
        for (int i = 0; i < CHESS_NAME.length; i++) {
            for (int j = 0; j < CHESS_NAME[i].length; j++) {
                if (chess_blood[i][j] <= 0 && itemId == sumid) {
                    //raise aim=false;
                    return false;
                }
                sumid++;
            }
        }
        return aim;
    }

    public int[][] deepCopy(int[][] arr) {
        if (arr == null || arr.length == 0) {
            return arr;
        }
        int len = arr.length, width = arr[0].length;
        int[][] res = new int[len][width];
        for (int i = 0; i < len; i++) {
            System.arraycopy(arr[i], 0, res[i], 0, width);
        }
        return res;
    }

    
    /**
     * to roll back
     */
    public void undoChess() {
        int[][] last, lastcb;
        switch (undoMode) {
            case 0:
                //to do nothing
                break;
            case 1:
                //to roll back move
                last = map2;
                map2 = map;
                map = last;
                curcountry = (curcountry - 1+CHESS_NAME.length) % (CHESS_NAME.length);
                ChessItem.this.repaint();
                break;
            case 2:
                //to roll back attack
                lastcb = cb2;
                cb2 = chess_blood;
                chess_blood = lastcb;
                curcountry = (curcountry - 1+CHESS_NAME.length) % (CHESS_NAME.length);
                ChessItem.this.repaint();
                break;
            case 3:
                //to roll back kill, (attack+move)
                last = map2;
                lastcb = cb2;
                map2 = map;
                map = last;
                cb2 = chess_blood;
                chess_blood = lastcb;
                curcountry = (curcountry - 1+CHESS_NAME.length) % (CHESS_NAME.length);
                ChessItem.this.repaint();
                break;
            default:
                break;
        }
    }
    
    /**
     * to finish current country's turn
     * not yet available
     */
    void endsChessTurn() {
    }
    

    
    @Override
    public void paint(Graphics gr) {
        super.paint(gr);
        
        //to paint every chess item in this.map
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[i].length; j++) {
                //to paint null space
                if (!isChessItem(map[i][j])) {
                    continue;
                }
                //to paint every item
                int country = map[i][j] / SEP;
                int itemId = map[i][j] % SEP;
                paintOne(gr, itemId, country, csize * (1 + j), csize * (1 + i), chess_blood[country][itemId]);
            }
        }
        
        //to paint focus rect of active item
        if (movex >= 0 && movey >= 0) {
            paintFocus(gr, movex, movey);
        }
    }

    /**
     * to draw one chess item
     * @param gr  Graphics
     * @param itemId  0-15,16-31,-1
     * @param country  black or red
     * @param blood  chess_blood rest of this itemId
     * @param posX  the index is based on this.map[10][9], 
     *      and has been reversed and weighted to fit screeen tuple 
     *      that means 馬(0,1) => 馬(2*csize,csize)
     * @param posY
     */
    public void paintOne(Graphics gr, int itemId, int country, int posX, int posY, int blood) {
//        *-   --
//      *-       --
//    *-           --
//  *-       馬      --
//  *-               --
//    *-           --
//      --        --
//        --    --
// itemId, {name="馬", itemId=17, country=1(red), blood=40(40%rest), position=(2*csize,10*csize)}

        if (!isChessItem(itemId + country * SEP)) {
            return ;
        }
        
        //to calculate relative arguments
        int itemIndex = itemId % CHESS_NAME[country].length;
        int cr2 = cr + CIRCLE_THICK;
        int arcRange = (int) (360 * blood / chess_blood_max[country][itemIndex]);
        
        //to draw outter circle and blood
        gr.setColor(COUNTRY[country]);
        gr.drawOval(posX - cr2, posY - cr2, cr2 * 2, cr2 * 2);
        gr.fillArc(posX - cr2, posY - cr2, cr2 * 2, cr2 * 2, 90, arcRange);
        
        //to draw inner circle
        gr.setColor(Color.LIGHT_GRAY);
        gr.fillOval(posX - cr, posY - cr, cr * 2, cr * 2);
        
        //to draw the Chinese character
        gr.setColor(COUNTRY[country]);
        int strSize = (int) (cr * Math.sqrt(2) / 2);
        Font fonts = new FontUIResource("", Font.PLAIN, strSize * 3 / 2);
        gr.setFont(fonts);
        gr.drawString(CHESS_NAME[country][itemIndex], posX - strSize / 2, posY + strSize / 2);
    }

    private void paintFocus(Graphics gr, int movex, int movey) {
//        |
//        |
//     _  |   _
//    | __|__  |
//______| 馬 |____________
//      |____|
//    |_  |   _|
//        |
//        |
//the focus effect, also the active item
        gr.setColor(COUNTRY[curcountry]);
        int posX = csize * (movey + 1), posY = csize * (movex + 1);
        int size = csize / 2;
        gr.drawLine(posX - size, posY - size + 3, posX - size, posY - size);
        gr.drawLine(posX - size, posY - size, posX - size + 3, posY - size);
        gr.drawLine(posX + size - 3, posY - size, posX + size, posY - size);
        gr.drawLine(posX + size, posY - size, posX + size, posY - size + 3);
        gr.drawLine(posX + size, posY + size - 3, posX + size, posY + size);
        gr.drawLine(posX + size, posY + size, posX + size - 3, posY + size);
        gr.drawLine(posX - size + 3, posY + size, posX - size, posY + size);
        gr.drawLine(posX - size, posY + size, posX - size, posY + size - 3);
    }


}
