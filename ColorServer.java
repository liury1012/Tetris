
/**
 * Class ColorServer provides colors for the board and tetrads.
 * It also define the color of the background and the colors for messages
 * displayed on the foreground game board or on the info areas.
 *
 * Put them all in the same class to easy the maitenance (only need to change this class)
 *
 * All methods and fileds are static - no need of constructor
 *
 * The colors for the grid board cells and tetards blocks are stored in two Color[]
 * arrays of the same length: colors, lightColors.
 *   The colors in these arrays represent two different lightness of
 *   the same color. For example, red, light red at index 1
 *
 *   The Colors GRAY, LIGHT_GRAY at index 0 are reserved for the empty cells;
 *   Tetrads has color index > 0
 *
 *   We want the game board and tetrad have light colors when the game is
 *   over or the game is paused.
 *    - when the game is in-progress, the game board cells and tetrad blocks use the
 *      colors in colors array
 *    - when the game is paused or the game is over, the game board and
 *      tetrad blocks use the color in lightColors array
 *    - all colors are acessed by the index stored in each cell of the game board
 *      or color index attribute of tetrad objects
 */

import java.util.*;
import java.awt.*;

class ColorServer
{
   private static Random rand = new Random();

   /*
    * - use colors or light colors for different scenarios
    * - index 0 is for empty cells, index > 1 for tetrad
    */
   private static final Color[] colors = {
                                   Color.GRAY,
                                   Color.RED,
                                   Color.BLUE,
                                   Color.GREEN,
                                   new Color(255, 20, 147),  // deep pink
                                   Color.YELLOW,
                                   new Color(255, 102, 0),   // orange
                                   Color.MAGENTA,
                                   Color.CYAN,
                                   new Color(102, 0, 153),    // purple
                                   new Color(160, 82, 45),    // sienna
                                   new Color(0, 191, 255)  }; // deep sky blue

   private static final Color[] lightColors = {
               Color.LIGHT_GRAY,
               new Color(255,  51,  51),     // light red
               new Color(51,  153, 255),     // light blue
               new Color(0,   255,  51),     // light green
               new Color(255, 105, 180),     // hot pink
               new Color(255, 255, 153),     // light yellow
               new Color(255, 156,  90),     // light orange
               new Color(255, 102, 255),     // light magenta
               new Color(102, 255, 255),     // light cyan
               new Color(178, 102, 255),     // light purple
               new Color(210, 133,  30),     // chocolate
               new Color(135, 206, 235)  };  // sky blue

   /*
    * Colors for the grid lines
    */
   private static Color gridLineColor = Color.LIGHT_GRAY;
   private static Color lightGridLineColor = new Color(245, 245, 245);     // white smoke

   /*
    * Color for messages shown on the info area
    */

   private static Color infoAreaMsgColor = new Color(128, 192, 128);

   /*
    * Color for messages with the game board as background
    */
   private static Color gameBoardAreaMsgColor = Color.DARK_GRAY;

   /**
    * The color for gameboard cells (empty or fixed tetrad blocks
    *
    * @param int: a valid index in the three color arrays
    * @param boolean: false - use colors array; ture - use lightColors array
    * @return Color: the color required
    */
   public static Color getCellColor(int ix, boolean lightColor)
   {
      if ( lightColor ) return lightColors[ix];
      return colors[ix];
   }

   /**
    * @param boolean: ture -  lightGridLineColor; fasle - gridLineColor
    * @return Color: the color of the grid lines
    */
   public static Color getGridLineColor(boolean lightColor)
   {
      if ( lightColor ) return lightGridLineColor;
      return gridLineColor;
   }

   /**
    * The color for edge lines of the tetrad block moving in the game board
    * @return Color - a color different from any color in the three color arrays
    */
   public static Color getMotionLineColor()
   {
       return Color.WHITE;
   }

   /**
    * @return int > 0: randomly generate a valid color index for a tetard
    */
   public static int colorIxForTetrad()
   {
      // index 0 is for empty cell
      return 1 + rand.nextInt(colors.length - 1);
   }

   /**
    * @param Tatras: the tetard asking its Color
    * @param boolean: true from lightColors; false - from the colors array
    * @return Color: return the Color according to the color index of the
    *                tetard on required lightness of the color
    */
   public static Color getTetradColor(Tetrad tetrad, boolean lighColor)
   {
      if ( lighColor  ) {
         return lightColors[tetrad.getColorIx()];
      }
      return colors[tetrad.getColorIx()];
   }

   /**
    * The color of the four edges of the destination positions for
    * the current tetrad blocks if touch the ground in their current positions
    *
    * @param Tatras: the tetard asking its Color
    * @return Color: the Color in the lightColors array
    */
   public static Color getTetradLineColor(Tetrad tetrad)
   {
      return lightColors[tetrad.getColorIx()];
   }

   /**
    * background of the panel - taken as the boundary sides of the gameboard
    */
   public static Color getBackColor()
   {
      return Color.BLACK;
   }

   /**
    * @return Color: color for message shown on the info area
    */
   public static Color getInfoAreaMsgColor()
   {
      return infoAreaMsgColor;
   }

   /**
    * @return Color: color for message with the game board as backgrond
    */
   public static Color getGameBoardAreaMsgColor()
   {
      return gameBoardAreaMsgColor;
   }

} // end of class ColorServer