
/**
 * The Tetrad class
 *
 *  This class define the tetrads presented in the game and
 *  provides utilitis to access and move the blocks of a tetrad
 *    A tetrad is represeted in a 4*4 int array with 1 for
 *    tetard block and 0 for empty.
 *
 *    We have 7 tetrad types:  I, O, T, L, S and Z.
 *    Each type has 4 orientations:
 *      - original block position configuration
 *      - block configurations obtained by rotate the original
 *        configuration 90, 180 and 270 degrees
 *
 *    We can
 *      - shift a tetrad left/right 1 colum,
 *      - rotate a tetrad clockwise 90 degree
 *      - drop down a tetard one row
 *
 *  Important static vaiables/methods:
 *     - a 4-D int array encode possible tetrad configurations
 *         1st dimension: 7 types (I, O, T, L, J, S and Z)
 *         2nd dimension: each type have four orientations
 *                        (in the order of clockwise rotation)
 *             3rd & 4th: the positions of blocks of a tetrad
 *                        in a 4*4 int array
 *
 *     - tetrads can only be created by the static method next(int, int)
 *             (constructor made private)
 *
 *  Important instance variables/methods
 *    - typeIx, rotation, colorIx: identify the tetrad and color
 *    - (colIx, rowIx): top left postion of the 4*4 tetrad grid
 *                      relative to the grid cellArray.
 *                      Notice that they are NOT physical positions!
 *
 *    - shift/rotate/dropDown - move the tetrad
 *
 *    - Point[] getBlockPos(int rta):
 *         with the curent configuration (rta = 0) or with the
 *           configuration after one rotation (rta = 1 for clockwise
 *           or -1 for counter clockwise),
 *         returns the index positions of the blocks in the show box
 *           or game board grid, i.e, (colIx, rowIx) + the block's
 *           column and row index within the 4x4 grid.
 *
 *         Used by the game board, for example, to determined
 *         if the tetrad configurtaion can be moved or rotated
 */

import java.util.*;
import java.awt.*;

public class Tetrad
{
   /*
    * used by next() in generate a tetrad (randomly pick the type and color)
    */
   private static Random rand = new Random();
   /*
    * tetrads of all types in all possible roations.
    * represented in a 4*4 grid: 1 - a tetrad block
    */
   private static final int[][][][] types =
         {  // I-type
            {  {  {0, 0, 0, 0},           // original configuration
                  {1, 1, 1, 1},
                  {0, 0, 0, 0},
                  {0, 0, 0, 0}  },

               {  {0, 1, 0, 0},           // rotated clockwise 90 degree
                  {0, 1, 0, 0},
                  {0, 1, 0, 0},
                  {0, 1, 0, 0}  },

               {  {0, 0, 0, 0},
                  {1, 1, 1, 1},
                  {0, 0, 0, 0},
                  {0, 0, 0, 0}  },

               {  {0, 1, 0, 0},
                  {0, 1, 0, 0},
                  {0, 1, 0, 0},
                  {0, 1, 0, 0}  }  },

            // O-type
            {  {  {0, 1, 1, 0},
                  {0, 1, 1, 0},
                  {0, 0, 0, 0},
                  {0, 0, 0, 0}  },

               {  {0, 1, 1, 0},
                  {0, 1, 1, 0},
                  {0, 0, 0, 0},
                  {0, 0, 0, 0}  },

               {  {0, 1, 1, 0},
                  {0, 1, 1, 0},
                  {0, 0, 0, 0},
                  {0, 0, 0, 0}  },

               {  {0, 1, 1, 0},
                  {0, 1, 1, 0},
                  {0, 0, 0, 0},
                  {0, 0, 0, 0}  }  },

            // T-type
            {  {  {0, 1, 0, 0},
                  {1, 1, 1, 0},
                  {0, 0, 0, 0},
                  {0, 0, 0, 0}  },

               {  {0, 1, 0, 0},
                  {0, 1, 1, 0},
                  {0, 1, 0, 0},
                  {0, 0, 0, 0}  },

               {  {0, 0, 0, 0},
                  {1, 1, 1, 0},
                  {0, 1, 0, 0},
                  {0, 0, 0, 0}  },

               {  {0, 1, 0, 0},
                  {1, 1, 0, 0},
                  {0, 1, 0, 0},
                  {0, 0, 0, 0}  }  },

            // L-type
            {  {  {0, 0, 1, 0},
                  {1, 1, 1, 0},
                  {0, 0, 0, 0},
                  {0, 0, 0, 0}  },

               {  {0, 1, 0, 0},
                  {0, 1, 0, 0},
                  {0, 1, 1, 0},
                  {0, 0, 0, 0}  },

               {  {0, 0, 0, 0},
                  {1, 1, 1, 0},
                  {1, 0, 0, 0},
                  {0, 0, 0, 0}  },

               {  {1, 1, 0, 0},
                  {0, 1, 0, 0},
                  {0, 1, 0, 0},
                  {0, 0, 0, 0}  }  },

            // J-type
            {  {  {1, 0, 0, 0},
                  {1, 1, 1, 0},
                  {0, 0, 0, 0},
                  {0, 0, 0, 0}  },

               {  {0, 1, 1, 0},
                  {0, 1, 0, 0},
                  {0, 1, 0, 0},
                  {0, 0, 0, 0}  },

               {  {0, 0, 0, 0},
                  {1, 1, 1, 0},
                  {0, 0, 1, 0},
                  {0, 0, 0, 0}  },

               {  {0, 1, 0, 0},
                  {0, 1, 0, 0},
                  {1, 1, 0, 0},
                  {0, 0, 0, 0}  }  },

            // S-type
            {  {  {0, 1, 1, 0},
                  {1, 1, 0, 0},
                  {0, 0, 0, 0},
                  {0, 0, 0, 0}  },

               {  {0, 1, 0, 0},
                  {0, 1, 1, 0},
                  {0, 0, 1, 0},
                  {0, 0, 0, 0}  },

               {  {0, 0, 0, 0},
                  {0, 1, 1, 0},
                  {1, 1, 0, 0},
                  {0, 0, 0, 0}  },

               {  {1, 0, 0, 0},
                  {1, 1, 0, 0},
                  {0, 1, 0, 0},
                  {0, 0, 0, 0}  }  },

            // Z-type
            {  {  {1, 1, 0, 0},
                  {0, 1, 1, 0},
                  {0, 0, 0, 0},
                  {0, 0, 0, 0}  },

               {  {0, 0, 1, 0},
                  {0, 1, 1, 0},
                  {0, 1, 0, 0},
                  {0, 0, 0, 0}  },

               {  {0, 0, 0, 0},
                  {1, 1, 0, 0},
                  {0, 1, 1, 0},
                  {0, 0, 0, 0}  },

               {  {0, 1, 0, 0},
                  {1, 1, 0, 0},
                  {1, 0, 0, 0},
                  {0, 0, 0, 0}  }  }
         };

   /*
    * The real size of the tetrads: 1st dimension - tetrad type, 2nd dimension - orientation
    * Used by the tetris to determine the show box grid size - we want the stand-by
    * tetrad be set in the middle of the show box
    */
   private static final Point[][] sizes =
         {   // I-type
            { new Point(4, 1), new Point(1, 4), new Point(4, 1),  new Point(1, 4) },
            // O-type
            { new Point(2, 2), new Point(2, 2), new Point(2, 2),  new Point(2, 2) },
            // T-type
            { new Point(3, 2), new Point(2, 3), new Point(3, 2),  new Point(2, 3) },
            // L-type
            { new Point(3, 2), new Point(2, 3), new Point(3, 2),  new Point(2, 3) },
            // J-type
            { new Point(3, 2), new Point(2, 3), new Point(3, 2),  new Point(2, 3) },
            // S-type
            { new Point(3, 2), new Point(2, 3), new Point(3, 2),  new Point(2, 3) },
            // Z-type
            { new Point(3, 2), new Point(2, 3), new Point(3, 2),  new Point(2, 3) }
         };

   /*
    * The (colIx, rowIx) postion that will make a tetrad in the center in the show box
    * The show box grid each side added 1 row or col to the tetrad real dimension
    *
    * Right now, tetrad is shown in its original orintation. We have the positions for
    * all four orientations of every tetard types such that we can easily have an extension
    * to allow the tetrid start in any orientation.
    */
   private static final Point[][] bestShowPos =
         {  // I-type
            { new Point(1, 0), new Point(0, 1), new Point(1, 0),  new Point(0, 1) },
            // O-type
            { new Point(0, 1), new Point(0, 1), new Point(0, 1),  new Point(0, 1) },
            // T-type
            { new Point(1, 1), new Point(0, 1), new Point(1, 0),  new Point(1, 1) },
            // L-type
            { new Point(1, 1), new Point(0, 1), new Point(1, 0),  new Point(1, 1) },
            // J-type
            { new Point(1, 1), new Point(0, 1), new Point(1, 0),  new Point(1, 1) },
            // S-type
            { new Point(1, 1), new Point(0, 1), new Point(1, 0),  new Point(1, 1) },
            // Z-type
            { new Point(1, 1), new Point(0, 1), new Point(1, 0),  new Point(1, 1) }
         };

  /**
   * @retrun Tetard - a tetrad in the orignal orientation of
   *                  a rondom type and in a random color
   */
   public static Tetrad next()
   {
      return new Tetrad( rand.nextInt(types.length), ColorServer.colorIxForTetrad() );
   }

   /**
    * @param Tetrad - the tetrad in the question
    * @return Point - (colum, row) numbers the param tetard blocks occupied
    */
   public static Point getSize(Tetrad tetrad)
   {
      return sizes[tetrad.typeIx][tetrad.rotation];
   }

   /**
    * @param Tetrad - the tetrad to be presented in the show box
    * @return Point - the (colum, row) of the param tetrad in the show box
    *                 that will make it appear in the center of the box
    */
   public static Point getBestShowPos(Tetrad tetrad)
   {
      return bestShowPos[tetrad.typeIx][tetrad.rotation];
   }

   /* -----------------------------
    * instance attributes & methods
    */

   // top-left position of the 4x4 relative to the (0, 0) position of cellArray
   private int rowIx = 0;
   private int colIx = 0;

   // tetrad type
   private int typeIx = 0;
   // orientation
   private int rotation = 0;
   // color
   private int colorIx = 0;

   /*
    * private constrctor, called by Tetrad.next(int, int)
    *  - we make sure the params are valid index
    */
   private Tetrad(int tx, int color)
   {
      rowIx = 0;
      colIx = 0;
      typeIx = tx;
      colorIx = color;
   }

   /**
    * Set the top-left position of this tetrad's 4*4 grid
    * in the gameboard or show box grid - attach this tetrad
    * to the game board or show box grid.
    *
    * pre-confition: the paramters must make sure that the
    *                blocks of the tetrad have valid row and colum
    *                indexes in the target cellArray
    *
    * @param: int c - colum number relative to the top-left position
    *                 of the grid it is attached to
    * @param: int r - row number relative to the top-left position
    *                 of the grid it is attached to
    */
   public void setPos(int c, int r) {  rowIx = r;   colIx = c;  }

   /**
    * @return Point - the postion (row, colum) of this grid in the grid
    *                 it currently attached to
    */
   public Point getPos()  {  return new Point(rowIx, colIx);  }

   /**
    * @returns int - the color index for the blocks of this tetrad
    */
   public int getColorIx()  {   return colorIx;   }

   /**
    * Used by tetris to drop down this tetrad one rows down
    * pre-condition: the trop down must be valid (will not hit
    *                any fixed tetrad blocks
    */
   public void dropDownOneRow()  {  ++rowIx;  }

   /**
    * Used by tetris to shift this tetrad at its current row
    * pre-condition: the shift must be valid (will not hit any fixed tetrad blocks)
    * $param int dir: -1 - move left; 1 - move right
    */
   public void shift(int dir)  {  colIx += dir;  }

   /**
    * Used by tetris to rotate this tetrad clockwise.
    * pre-condition: the rotation must be valid (will not hit any fixed tetrad blocks)
    * $param int dir: 1 - clockwise; -1 - anti-clockwise (for possible extension)
    */
   public void rotate(int dir)
   {
      rotation += dir;
      if ( rotation < 0 ) rotation = 3;
      else if ( rotation >= 4 ) rotation = 0;
   }

   /**
    * Used by the tetris in determing the start rowIx of the tetrad
    * We want the tetrad start at position as high as possible
    * For example, in the I-type's original configuration, there is
    * an empty row at the top, so its start rowIx shall be -1, instead of 0
    *
    * @returns int: how many empty rows from the top in thr 4*4 grid
    */
   public int getTopIndent()
   {
      int[][] activeBlocks = types[typeIx][rotation];
      for ( int r = 0; r < activeBlocks.length; ++r ) {
         for ( int c = 0; c < activeBlocks[r].length; ++c ) {
            if ( activeBlocks[r][c] != 0 ) {
               return r;
            }
         }
      }
      return 0;
   }

   /**
    * Used by the tetris in determining an action (shift, rotate or drop down)
    * on this tetrad is feasible or not
    * pre-condition: the tetrad has been placed on the game board
    *
    * @param rta = 0: block positions in the current configuration
    *             -1: block positions after rotate anti-clockwise
    *              1: block positions after rotate clockwise
    * @return Point[]: a point array, each point record the index values
    *                 (col, row) of a block in the game board
    *                  - the Point is in ascending order of their (colum, row)
    *                    index:the top-left as the first and the bottom-right
    *                    as the last
    */
   public Point[] getBlockPos(int rta)
   {
      rta += rotation;
      if ( rta < 0 ) rta = 3;
      else if ( rta >= 4 ) rta = 0;

      Point[] blkPosArray = new Point[4];

      int[][] activeBlocks = types[typeIx][rta];
      int count = 0;
      for ( int r = 0;  r < activeBlocks.length && count < 4; r++ ) {
         for ( int c = 0; c < activeBlocks[r].length && count < 4; c++  ) {
            if ( activeBlocks[r][c] == 1 ) {
               blkPosArray[count] = new Point(colIx + c, rowIx + r);
               count++;
            }
         }
      }
      return blkPosArray;
   }

   /**
    * @returns String: the tetrad's attribute values in a string (debug)
    */
   public String toString()
   {
      return "[(" + colIx + ", " + rowIx + "), " + typeIx + "," + rotation + ", " + colorIx + "]";
   }

} // end of class Tetrad