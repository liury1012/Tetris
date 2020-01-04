/**
 * class GridBoard builds the grid show box for thes tand-by tetrad
 *
 * All attributes are protected as they will be inherited by class GameBoard
 *
 * A GridBoard instance has
 *   - its top/left position in the Tetris panel.
 *   - the row and colum numbers of the grid
 *   - the side length of the square cells in the grid
 *   - the thickness of the grid lines
 *
 * They grid is represented by a 2D int array with 0 as
 * its element values - represent the color index of the empty cell.
 *
 * It provides two display methods
 *   - print the empty grid board on a Graphics2D with required color lightness
 *   - print a tetrad on top of the grid board with required color lightness
 *     and draw the in-motion edge lines if the tetard is in-motion
 */

import java.util.*;
import java.awt.*;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.BasicStroke;

class GridBoard
{
   /*
    * top-left physic position of the board in the panel
    */
   protected int xPos;
   protected int yPos;
   /*
    * the row and colum numbers of the grid
    */
   protected int rowNr;
   protected int colNr;
   /*
    * the size of the sqaure cells
    */
   protected int cellSize;
   /*
    * the color index of the empty cell
    */
   protected int empty = 0;
   /*
    * the thickness of the grid lines
    */
   protected BasicStroke lineStroke = null;

   /*
    * The grid of size rowN*ColNr
    *  - init with the empty Color.
    *  - cellArray[r][c] != empty means tetrad block placed at the cell
    */
   protected int[][] cellArray = null;

   /**
    * The constructor.
    * @param int r - row number of the grid (r > 0)
    * @param int c - colum number of the grid (c > 0)
    * @param int x - the left position of the grid in the tetris panel
    * @param int t - the top position of the grid in the tetris panel
    * @param int s - the side length of the square grid cells
    * @param BasicStroke - the thickness of the gridline
    */
   public GridBoard(int r, int c, int x, int y, int s, BasicStroke k)
   {
      // top left position
      xPos = x;   yPos  = y;
      rowNr = r;  colNr = c;
      cellSize = s;
      lineStroke = k;

      cellArray = new int[rowNr][colNr];
      for ( int iy = 0; iy < rowNr; iy++ ) {
         for ( int ix = 0; ix < colNr; ix++ )
            cellArray[iy][ix] = empty;
      }
   }

   /**
    * @return int: the left (horizontal) position of the grid in the tetris panel
    */
   public int xPos() {  return xPos;  }
   /**
    * @return int: the top (vertical) position of the grid in the tetris panel
    */
   public int yPos() {  return yPos;  }
   /**
    * @return int: the side length of the sqaure grid cells
    */
   public int cellSize() { return cellSize; }

   /**
    * display the board, use the cellArray elements value as index
    * to find out the Color of the grid cells
    *
    * @param Graphics2D - where to dispaly
    * @param boolean: true - use light colors; false - use normal colors
    */
   public void displayBoard(Graphics2D g, boolean lightColor)
   {  /*
       * The lined grid with the colors of the cells
       */
      g.setStroke(lineStroke);

      int px, py = yPos;
      Color lineColor = ColorServer.getGridLineColor(lightColor);
      for ( int r = 0; r < rowNr; r++ ) {
         px = xPos;
         for ( int c = 0; c < colNr; c++ ) {
            g.setColor( ColorServer.getCellColor(cellArray[r][c], lightColor) );
            g.fillRect( px, py, cellSize, cellSize );
            g.setColor( lineColor );
            g.drawRect( px, py, cellSize, cellSize );
            px += cellSize;
         }
         py += cellSize;
      }
   }

   /**
    * display a tetrad (not fixed on the grid yet) on the grid
    * pre-condition: all blocks of the tetrad are inside the gridboard and on empty cells
    *                don't check for performance reason
    * @param Graphics2D - where to display
    * @param Tetrad - the tetard to be displayed
    * @param boolean lightColor - the tetrad blocks in light or normal color
    * @param boolean inMotion - in case true, the edge of the blocks are shown
    *                           in-motion line color
    * @return Point[] - the block positions of the tetrad
    */
   public Point[] displayTetrad(Graphics2D g, Tetrad tetrad, boolean lightColor, boolean inMotion)
   {
      Point[] tetradBlkPos = tetrad.getBlockPos(0);
      if ( tetradBlkPos == null ) return null;

      g.setStroke(lineStroke);

      Color cellColor = ColorServer.getTetradColor(tetrad, lightColor);
      Color lineColor = ColorServer.getMotionLineColor();
      if ( !inMotion ) lineColor =  ColorServer.getGridLineColor(lightColor);

      for ( Point blk : tetradBlkPos ) {
         int px = xPos + cellSize*blk.x;
         int py = yPos + cellSize*blk.y;
         g.setColor( cellColor );
         g.fillRect( px, py, cellSize, cellSize );

         g.setColor( lineColor );
         g.drawRect( px, py, cellSize, cellSize );
      }

      return tetradBlkPos;
   }

} // end of class GridBoard