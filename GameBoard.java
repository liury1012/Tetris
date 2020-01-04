/**
 * A game board is a grid board. So class GameBoard extends class GridBoard
 *
 * On the show box, the grid cells don't change colors and the stand-by tetrad do not move.
 * On the gameboard, however, tetrads can move and will be fixed on the grid when touched
 * the ground and thus change the color of the cells.
 *
 * The GameBoard has a reference to its owner Tetris such that the game board can
 * access the game status in determing the Color style (light or normal) for the
 * grid board and the tetrads
 *
 * GameBoard provides some additional utility methods to allow the tetris to
 *  - check a tetrad movement action (shift, rotate, drop down) is feasible
 *  - fix the active tetrad on the board after it touch the ground
 *  - find out how many full rows are there
 *  - clear the full rows
 *  - display the gameboard with the active tetrad shown in-motion style
 *
 * GameBoard define an extra private attirbute emptyRowStart to track the first
 * row index at which and above all the cells are empty. This helps up speed up,
 * for eample, in checking how many full rows are there in the game board
 */

import java.util.*;
import java.awt.*;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.BasicStroke;
import java.awt.RenderingHints;

public class GameBoard extends GridBoard
{
   // the owner  ...
   private Tetris tetris = null;
   /*
    * to speed-up: cells at and above this row are all empty
    */
   private int emptyRowStart;

   /**
    * The constructor
    * @param Tetris - the owner
    * @param int r - row number of the grid (r > 0)
    * @param int c - colum number of the grid (c > 0)
    * @param int x - the left position of the grid in the tetris panel
    * @param int t - the top position of the grid in the tetris panel
    * @param int s - the side length of the square grid cells
    * @param BasicStroke - the thickness of the gridline of the gameboard
    */
   public GameBoard(Tetris t, int r, int c, int x, int y, int s, BasicStroke k)
   {
      super(r, c, x, y, s, k);
      tetris = t;
      emptyRowStart = r - 1;
   }

   /**
    * set the gameboard to its initial state - all cells are empty
    * called to start a new game
    */
   public void reset()
   {
      for ( int iy = 0; iy < rowNr; iy++ ) {
         for ( int ix = 0; ix < colNr; ix++ ) {
            cellArray[iy][ix] = empty;
         }
      }
      emptyRowStart = rowNr - 1;
   }

   /**
    * called by Tetris to
    *  - show the states (Color) of each gameboard cells and
    *  - show the active tetrad on its current position
    *  - show the destination position if the active tetrad touch the ground from
    *    its current position
    *
    *  @param Graphics: where to paint
    *  @param Tetrad: the tetrad currently moving on the game board
    *  @param boolean lighColor: true - light colors for the game board and tetrad block
    *  @param boolean inMotion: true - show the activeTetrad as in-motion
    */
   public void display(Graphics gr, Tetrad activeTetrad)
   {
      Graphics2D g = (Graphics2D)gr;
      /*
       * the Color of each cells on the game board - call the inherited method
       */
      boolean lightColor = false;
	  boolean inMotion = true;
      if ( !tetris.gameStarted() || tetris.gamePaused() || tetris.gameOver() ) {
         inMotion = false;
         lightColor = true;
      }
      displayBoard(g, lightColor);

      if ( activeTetrad != null ) {
         /*
          * the current tetrad position on the board - call the inherited method
          */
         if ( tetris.gameOver() ) {
            /*
             * this tetrad made the game over
             * display some blocks that are valid on the board
             */
            ArrayList<Point> blkArray = getFeasibleBlockPosByLifting( activeTetrad.getBlockPos(0) );
            if ( blkArray != null ) {
               Color cellColor = ColorServer.getTetradColor(activeTetrad, lightColor);
               Color lineColor = ColorServer.getGridLineColor(lightColor);
               g.setStroke(lineStroke);
               for ( Point blk : blkArray ) {
                  g.setColor(cellColor);
                  g.fillRect(xPos + cellSize*blk.x, yPos + cellSize*blk.y, cellSize, cellSize);
                  g.setColor(lineColor);
                  g.drawRect(xPos + cellSize*blk.x, yPos + cellSize*blk.y, cellSize, cellSize);
               }
            }
            return;
         }

         // dsiplay the active tetrad
         Point[] blkArray = displayTetrad(g, activeTetrad, lightColor, inMotion);
         /*
          * the end position if the tetrad touches the ground - only draw lines
          */
         if ( blkArray != null ) {
            blkArray = getDestPos(blkArray);
            if ( blkArray != null ) {
               g.setStroke(lineStroke);
               g.setColor( ColorServer.getTetradLineColor(activeTetrad) );
               for ( Point blk : blkArray ) {
                  g.drawRect(xPos + cellSize*blk.x, yPos + cellSize*blk.y, cellSize, cellSize);
               }
            }
         }
      }
   }

   /**
    * @param Point[]: the position array of a tetrad blocks in the gameboard
    * @return boolean: false if some element of blockPos is out of
    *                  the board, or in a cell already occupied
    */
   public boolean isValidAndEmpty(Point[] blockPos)
   {
      for (int ix = blockPos.length - 1; ix >= 0; --ix ) {
         Point blkPos = blockPos[ix];
         if ( blkPos.x < 0 || blkPos.x >= colNr ||
              blkPos.y < 0 || blkPos.y >= rowNr )
            return false;

         if ( cellArray[blkPos.y][blkPos.x] != empty )
            return false;
      }
      return true;
   }

   /**
    * called by the owner tetris to check if a shift a tetrad right/left request is feasible
    * pre-condition: dir = 1 or -1
    *
    * @param Point[]: the position array of a tetrad blocks in the gameboard
    * @param int:     -1 - shift left one colum; 1 - shift right one colum
    * @return boolean: false if the required shift will hit the boundary or
    *                  hit an occupied cell
    */
   public boolean isValidShift(Point[] blockPos, int dir)
   {
      for (int ix = 0; ix < blockPos.length; ++ix ) {
         Point blkPos = blockPos[ix];
         if ( blkPos.x + dir < 0 || blkPos.x + dir >= colNr ||
              cellArray[blkPos.y][blkPos.x + dir] != empty ) {
            return false;
         }
      }
      return true;
   }

   /**
    * check if it is feasible to drop a tetrad one row down:
    *       - not hit a fixed tetrad block
    *       - not get out of the grid
    * @param Point[]: the tetrad block's current positions
    *
    * @return boolean: true - if feasible; false - not feasible
    */
   public boolean isValidMoveDown(Point[] blockPos)
   {
      for (int ix = blockPos.length - 1; ix >= 0; --ix ) {
         Point blkPos = blockPos[ix];
         if ( blkPos.y + 1 >= rowNr )
            return false;

         if ( cellArray[blkPos.y + 1][blkPos.x] != empty )
            return false;
      }
      return true;
   }

   /**
    * Called by the owner tetris to dispaly the destination positions of
    * the active tetrad
    *
    * @param Point[]: the positions of the blocks form the active tetrad
    * @return Point[]: the positions the param blocks touch the ground
    *                  (null if the param blocks already touched the ground)
    */
   public Point[] getDestPos(Point[] currBlockPos)
   {
      int dropDistance = -1;

      for (Point blkPos : currBlockPos ) {
         int dist = -1;
         for ( int iy = blkPos.y + 1; iy < rowNr; iy++ ) {
            if ( cellArray[iy][blkPos.x] != empty ) {
               dist = (iy - 1) - blkPos.y;
               break;
            }
         }
         if ( dist < 0 ) {
            dist = (rowNr - 1) - blkPos.y;
         }

         if ( dist <= 0 ) return null;
         if ( dropDistance < 0 || dist < dropDistance )
            dropDistance = dist;
      }

      if ( dropDistance <= 0 )
         return null;

      Point[] blockDestArray = new Point[ currBlockPos.length ];
      for ( int bx = 0; bx < currBlockPos.length; ++bx ) {
         blockDestArray[bx] = new Point( currBlockPos[bx].x,
                                         currBlockPos[bx].y + dropDistance );
      }
      return blockDestArray;
   }

   /**
    * Called by the owner tetris to make the game board cells
    * be occupied by the blocks in blockPos array (generated from
    * the active currTetrad that cannot drop further)
    * The cell int value changed to the color index of the tetrad
    *
    * @param Point[]: the tetrad blocks
    * @param int: the color index of the tetrad
    */
   public void placeBlocks(Point[] blockPos, int blkColorIx)
   {
      for ( Point pos : blockPos ) {
         if ( pos.y <= emptyRowStart ) {
            emptyRowStart = pos.y - 1;
         }
         cellArray[pos.y][pos.x] = blkColorIx;
      }
   }

   /**
    *  Called by the owner tetris after a tetrad is fixed on the game board.
    *  Remove rows without empty cells and drop the blocks above the removed rows
    *
    *  @return int: number of rows removed - used by the tetris to update the scores
    */
   public int clearFullRows()
   {
      int totalRemoved = 0;

      for ( int r = rowNr - 1; r > emptyRowStart; --r) {
         boolean full = true;
         for ( int c = 0; c < colNr; c++ ) {
            if ( cellArray[r][c] == empty ) {
               full = false;
               break;
            }
         }
         if ( full ) {
            deleteRow(r);
            // stay at this row
            r++;
            totalRemoved++;
         }
      }

      emptyRowStart += totalRemoved;
      return totalRemoved;
   }

   /*
    * Called by clearFullRow to drop one row the tetrad blocks that are above
    * the specified row index
    *
    * @param int - the row index to be cleared
    */
   private void deleteRow(int row)
   {
      // drop the blocks above the line
      if ( row == 0 ) {
         for ( int c = 0; c < colNr; ++c ) cellArray[0][c] = empty;
         return;
      }

      if ( row > 0 ) {
         int top = emptyRowStart;
         if ( top < 0 ) top = 0;
         for ( int r = row - 1; r >= top; --r ) {
            for (int c = 0; c <  colNr; ++c )
               cellArray[r + 1][c] = cellArray[r][c];
         }
      }
      if ( row == 0 || emptyRowStart < 0 ) {
         for ( int c = 0; c < colNr; ++c )
            cellArray[0][c] = empty;
      }
   }

   /*
    * blocks (from a tetrad) cannot be placed on the gameboard
    * because some block's positions are not valid
    * lift the block one or two rows to get some valid rows of block
    *
    * @param Point[] - ordered in ascending order of (x, y) of the Point
    *                   top-left the first, bottum-righ the last
    * @return ArrayList<Point> - blocks that can be placed on the gameboard
    *                            null if none of them
    */
   private ArrayList<Point> getFeasibleBlockPosByLifting(Point[] blkArray)
   {
      boolean tryMore = true;
      int lastIx = blkArray.length - 1;
      int liftRowNr = 1;
      ArrayList<Point> feasibleBlocks = new ArrayList<Point>();
      while ( tryMore ) {
         int rowIx = -1;
         for ( int ix =  lastIx; ix >= 0; --ix ) {
            Point blk = blkArray[ix];
            if ( rowIx < 0 ) {
               rowIx = blk.y;
               if ( rowIx - liftRowNr < 0 ) {
                  tryMore = false;
                  break;
               }
            }

            if ( rowIx == blk.y ) {
               if ( cellArray[rowIx - liftRowNr][blk.x] != empty ) {
                  // lift one more row
                  liftRowNr++;
                  break;
               }
            } else {

               // the buttom row blocks are feasible by lifting
               for ( int bx = lastIx; bx > ix; --bx ) {
                  blk = blkArray[bx];
                  blk.y -= liftRowNr;
                  feasibleBlocks.add(blk);
               }
               for ( int rx = ix; rx >= 0; --rx ) {
                  blk = blkArray[rx];
                  if ( blk.y - liftRowNr < 0 ) {
                     break;
                  }
                  blk.y -= liftRowNr;
                  feasibleBlocks.add(blk);
               }
               tryMore = false;
               break;
            }
         }
      }

      if ( feasibleBlocks.size() <= 0 ) return null;
      return feasibleBlocks;
   }

} // end of class GameBoard