/**
 * The tetris is the controller of the game.
 * It extends the Panel and implements the KeyListener.
 *
 * The pannel is divided logically into two portions:
 *   -  The top portion displays some game information, such as
 *      the instructions, the scores, time played, and the next
 *      tetrad in a show box of class GridBoard.
 *
 *   -  The remaining part of the panel show the game board,
 *      on which the tetrads are shifted, rotated and dropped down.
 *      Once a tetrad touched the ground, it will be fixed on the game board.
 *
 * A tetris has an instance of class GameBoard.
 *
 *   A tetris has a TimerTask scheduled every 50 millisceconds
 *   (defined by the final attirbute period). It calls the movedDown
 *   method determines the tetrad's droping down on the game board
 *   The speed of game (droping down speed) is determined by how often
 *   to call the movedDown method. Hence, the droping down time is always
 *   in multiples of this 50 millisecond
 *
 *   When a row of the game board are fully ocuppied by fixed tetrads,
 *     -  the tetrad blocks fixed on that row are removed
 *     -  the blocks above them will drop down to their position.
 *
 *   Scores are rewarded when there are full rows are removed.
 *   Multiple full rows shall get bonus points. The bonus increses
 *   follow the numbers of additinal full rows.
 *
 *   According to the scores achieved, the player is ranked by levels.
 *   Scores 0 is at the first level. Every additinal 500 points add one score level.
 *
 *   The tetrad droping speed depends on the score levels achieved.
 *   The game has 3 different speeds.
 *     - entry (level 1 and 2, scores < 1000) - slowest
 *     - intermediate (level 3 - 6, 1000 <= scores < 3000) - medium
 *     - advanced (level 7 and up, scores >= 3000) - fastest
 *
 *   The tetris listen to the instruction typed by the player:
 *     - start a teris game.
 *     - shift the active tetrad to left or right if feasible;
 *     - rotate the active tetrad clockwise.
 *     - fast drop the active tetrad
 *     - pause or resume the game
 *
 *   The tetris calls the methods of its gameboard to check the feasiblity
 *   of tetrad movement request (shift, roate or drop down) on the game board;
 *   if the move is feasible, then call the tetard do the move action
 *
 *   The game has 4 status:
 *      - Not started: the empty game board shall be displayed and
 *                     the instructions dispalyed on the info area
 *
 *      - Game Paused: User pressed the pause key. The tetrad shall stop moving
 *                     and the game timer shall stop couting.
 *
 *      - Game Over:   The next tetrad cannot be dropped into the game board.
 *                     The final score shall be displayed on the playing area
 *                     with the game board as background.
 *                     Game instruction shows on the info area.
 *
 *      - In progress: The game board display
 *                      - The tetrad blocks previously fixed on the board.
 *                      - The tetrad currently in play
 *                      - The destination position of the current tetrad
 *                        if touch the ground at its current droping position
 *                    The info area display
 *                      - The scores and level achieved
 *                      - For how long the current game has been played
 *                      - The stand-by tetrad that will come into play when the current
 *                        tetrad touch ground.
 */

import java.util.*;
import java.awt.*;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.*;

public class Tetris extends Panel implements KeyListener
{
   // fonts for messages
   private static Font LARGE_FONT = new Font("Tahoma", Font.BOLD, 18);
   private static Font MEDIUM_FONT = new Font("Tahoma", Font.BOLD, 12);
   private static Font SMALL_FONT = new Font("Tahoma", Font.BOLD, 11);

   // dimension of the panel
   private Dimension dim = null;

   // row and col numbers of the game board
   private int rowNr = 0;
   private int colNr = 0;

   // the height of the info display area (upper part of the panel)
   private int infoHeight = 0;
   /*
    * A game board is a rowNr x colNr cell grid
    * the tetrad move (left/right/drop) along the cell grid
    */
   GameBoard gameBoard = null;

   // background color (as the color of game board boundary)
   private Color backColor = ColorServer.getBackColor();

   // the active tetrad, the tetrad in-play
   Tetrad currTetrad = null;

   // the stand-by tetrad, the tetrad that will be in-play next
   Tetrad nextTetrad = null;

   /*
    * how many rows to drop when the fast drop key is pressed
    * used as a factor to shorten the droping cycle for one row
    */
   private double   fastDropRowNr = 2.5;
   // normal drop: 1 row in one drop cycle
   private double   nextDropRowNr = 1.0;

   // delay before the tetrad starts moving, in milliseconds
   private final int  delay = 1000;
   /*
    * frequency of the call to Timertask run, in milliseconds
    */
   private final int  period = 50;

   /* drop down every (period*speedLevel) milliseconds
    *   14 the slowest - 50*14 = 0.7 seconds drop one row
    *   12 medium speed - 50*12 = 0.6 seconds drop one row
    *   10 the fastest - 50*10 = 0.5 second drop one row
    */
   private int slowestSpeedLevel = 14;
   private int speedLevelIncrement = 2;
   private int fastestSpeedLevel = 10;
   private int speedLevel = slowestSpeedLevel;

   /* count how many TimerTask cycles passed
    * implements the speed-up feature:
    *  - do the drop-down only if the cycleCount reached the speedLevel
    *  - reset to 0 after each drop-down
    */
   private int cycleCount = 0;

   // The scores and level reached
   private long scores = 0;
   private int  level = 1;
   // how long (in millisecond) the game is in-progess
   private long timePlayed  = 0;

   /*
    * status variabes of the game
    */
   private boolean gameStarted = false;
   private boolean gamePaused = false;
   private boolean gameOver = false;

   // buffered paiting
   private Image mImage = null;
   private Graphics offG = null;

   /**
    * Class Constructor specifying the size of the game board and
    * the size of the information diplay area.
    *  @param row  the row number of the game board
    *  @param col  the column number of the game board
    *  @param info how many rows the info area has
    */
   public Tetris(int row, int col, int info)
   {
      setBackground(backColor);

      rowNr = row;
      colNr = col;
      infoHeight = info;
      addKeyListener( this );

      /*
       * The TimerTask  run every 50 millisconds
       * It calls the movedDown method to drop down the tetard one row
       * Use cycleCount to control the drop down speed
       *  (i.e., in how many 50 milliseconds it will call movedDown
       */
      Timer tm = new Timer() ;
      TimerTask motion = new TimerTask()
      {
         public void run()
         {
            if ( dim != null && gameStarted && !gamePaused && !gameOver ) {
               timePlayed += period;
               ++cycleCount;
               if ( cycleCount >= (int)(speedLevel/nextDropRowNr) ) {
                  cycleCount = 0;
                  if ( movedDown() ) repaint();
               }
            }
         }
      };
      tm.scheduleAtFixedRate( motion, delay, period ) ;
   }

   /**
    * Reset the status and control variables to default values
    * Called when a new game is started (Enetr key after game over)
    */
   public void reset()
   {
      gameOver = false;
      gamePaused = false;
      scores = 0;
      level = 1;
      timePlayed  = 0;
      nextDropRowNr = 1;
      speedLevel = slowestSpeedLevel;

      gameBoard.reset();
   }

   /**
    *  Tells if the first game has been started
    *  @return boolean
    */
   public boolean gameStarted() {  return gameStarted;  }

   /**
    * Tells if the game is paused
    * @return boolean
    */
   public boolean gamePaused() {  return gamePaused;  }

   /**
    *  Tells if the game is over (no tetrad can enter the game board)
    *  @return boolean
    */
   public boolean gameOver() {  return gameOver;  }

   /**
    * Tells if the game is in-progress
    * @returns boolean
    */
   public boolean gameInProgress()
   {
      if ( gameStarted && !gameOver && !gamePaused ) return true;
      else return false;
   }

   /**
    * @return Tetrad: the active tetrad currently in-play
    */
   public Tetrad getCurrTetrad() {  return currTetrad;  }

   /**
    * @return Tetrad the stand-by tetrad that will be in-play next
    */
   public Tetrad getNextTetrad() {  return nextTetrad;  }

   /**
    * Do the drop down of the current tetrad if feasible
    * If the tetrad has touched the ground
    *  - fix the tetard blocks in its current positions
    *  - If the drop-down results in full rows
    *     - clear those full rows
    *     - drow down tetrad blocks in the rows above
    *     - update the scores and level of the game
    *     - update the speed if the scores reached certain levels
    * Turn the next tetrad to the current tetrad and
    * try to drop it into the game board,
    *    - if not possible, i.e., hit fixed tetard blocks in
    *      its drop position, turn the status to game over
    *    - if possible, get the next stand-by tetrad
    *
    * @return boolean: false if the game is not in-play (e.g, game pause, game over)
    *                  true otherwise (repaint required)
    */
   public boolean movedDown()
   {
      if ( !gameStarted || gamePaused || gameOver || currTetrad == null )
         return false;

      // the postions (col, row) of the blocks in the cellArray
      Point[] blkPos = currTetrad.getBlockPos(0);

      /*
       * check if the drop down feasible
       */
      if ( gameBoard.isValidMoveDown(blkPos) ) {
         // drop down one row
         currTetrad.dropDownOneRow();
         nextDropRowNr = 1;  // back to normal in case of fast drop
         return true;
      }

      /*
       * touched the ground - fixed the on the board
       */
      gameBoard.placeBlocks(blkPos, currTetrad.getColorIx());

      /*
       * update the score and the levels
       */
      int fullNr = gameBoard.clearFullRows();
      if ( fullNr > 0 ) {
         int rowScore = 100;
         int rowBonus = 50;
         /* 2nd full row get base bonus
          * 3nd full row bonus doubled
          * 4rd full row bonus trippled
          */
         for ( int ix = 0; ix < fullNr; ++ix ) {
            scores += (rowScore + ix*rowBonus);
         }

         // increase the level and speed
         int levelMark = 500;
         int delta = (int)(scores - levelMark * level);
         if ( delta > 0 ) {
            // increase the levels
            delta = 1 + (int)(delta/levelMark);
            level += delta;
            if ( speedLevel > fastestSpeedLevel ) {
               /* only 3 levels
                *   - entry (level 1, 2): slowest one drop down
                *   - intermediate (level 3 - 6): in between
                *   - advanced (level 7 and up): fastest
                */
               if ( level >= 7 ) speedLevel = fastestSpeedLevel;
               else if ( level >= 3 ) speedLevel = fastestSpeedLevel + speedLevelIncrement;
            }
         }
      }

      /*
       * activate the stand-by tetrad
       */
      setAsCurrTetrad(nextTetrad);
      if ( !gameBoard.isValidAndEmpty( currTetrad.getBlockPos(0) ) ) {
         // cannot enter the board - game over
         gameOver = true;
         nextTetrad = null;
         return true;
      }

      // new stand-by tetrad
      setAsNextTetrad(Tetrad.next());

      return true;
   }

   /**
    * implement keyListener
    * @param KeyEvent tells which key is pressed
    */
   public void keyPressed( KeyEvent ke)
   {
      if ( gameBoard == null ) return;

      boolean repaintFlag = false;

      switch ( ke.getKeyCode() ) {

         case KeyEvent.VK_LEFT:         // shift left
            if ( currTetrad != null && !gameOver && !gamePaused ) {
               if ( gameBoard.isValidShift(currTetrad.getBlockPos(0), -1) ) {
                  currTetrad.shift( -1 );
                  repaintFlag = true;
               }
            }
            break ;

         case KeyEvent.VK_RIGHT:        // shift right
            if ( currTetrad != null && !gameOver && !gamePaused ) {
               if ( gameBoard.isValidShift( currTetrad.getBlockPos(0), 1 ) ) {
                  currTetrad.shift( 1 );
                  repaintFlag = true;
               }
            }
            break ;

         case KeyEvent.VK_UP:
         case KeyEvent.VK_PAGE_UP:      // rotate clockwise
            if ( currTetrad != null && !gameOver && !gamePaused ) {
              if ( gameBoard.isValidAndEmpty( currTetrad.getBlockPos(1) ) ) {
                  currTetrad.rotate( 1 );
                  repaintFlag = true;
               }
            }
            break;

         case KeyEvent.VK_DOWN:
         case KeyEvent.VK_PAGE_DOWN:    // fast drop
            if ( currTetrad != null && !gameOver && !gamePaused ) {
               nextDropRowNr = fastDropRowNr;
            }
            break;

         case KeyEvent.VK_ESCAPE:        // pause
            if ( gameStarted && !gameOver && !gamePaused ) {
               gamePaused = true;
               repaintFlag = true;
            }
            break;

         case KeyEvent.VK_ENTER:     // start or resume the game
            if ( !gameStarted ) {
               gameStarted = true;
               start();
               repaintFlag = true;
            } else if ( gameOver ) {
               reset();
               start();
               repaintFlag = true;
            } else if ( gamePaused ) {
               gamePaused = false;
               repaintFlag = true;
            }
            break;
      }

      if ( repaintFlag == true ) repaint();
   }

   public void keyReleased( KeyEvent ke ) { /* no action */ }

   public void keyTyped( KeyEvent ke ) { /* no action */ }

   /**
    * Show the game on the screen.
    *  - call game board to display what's going on
    *  - call private method displayInfo to display required message
    *     (depends on the status of the game)
    *
    * Use buffered paint: first paint on an image and then on the screen
    *
    * When called first time, it creates the gameboard.
    * In the contructor as getSize() returns a 0 by 0 dimension
    *
    * @param Graphics - where to paint
    */
   public void paint(Graphics gr)
   {
      if ( dim == null ) {
         dim = getSize();
         int cellSize = calculateCellSize();
         // top-left position of the game board
         double xPos = (dim.width - cellSize*colNr) / 2;
         double yPos = dim.height - xPos - cellSize*rowNr;

         gameBoard = new GameBoard( this, rowNr, colNr,
                                   (int)xPos, (int)yPos,
                                    cellSize, new BasicStroke(2.0f) );
         // turn the infoHeight to a physic height (not row number anymore)
         infoHeight = (int)yPos;
      } else {
         dim = getSize();
      }

      if ( mImage == null || mImage.getWidth(null) != dim.height
                          || mImage.getHeight(null) != dim.height ) {
         mImage = createImage(dim.width, dim.height);
      }

      // buffered paint
      offG = mImage.getGraphics();
      offG.setColor(getBackground());
      offG.fillRect(0, 0, dim.width, dim.height);

      gameBoard.display(offG, currTetrad);
      infoDisplay(offG, nextTetrad);

      // put the offscreen image on the screen.
      gr.drawImage(mImage, 0, 0, null);
   }

   /*
    * dislay some message according to the status of the game
    *  - !gameStarted: instruction on the info area
    *  - gameOver: instruction on the info info area;
    *              game result (scores) on the gameboard with
    *              the final states of the game board as background
    *              (in light colors)
    *  - othewise (game in-progress or paused):
    *        on the info area of the panel,
    *          - display some statistic game information (scores and score levels
    *            at the left side, clocks and time palyed at the right side)
    *          - in the middle of the info area, display the stand-by tetrad
    *            in a grid box; the tetrad is placed in the center of the box
    *            (with one empty row/colum on each side of the tetrad blocks)
    *         Use light colors if the game is paused, nromal colors otherwise
    *
    *         if game paused: message "Press ENTER to Resume" on the
    *                         foreground of the game board
    *
    * @param Graphics: where to display
    * @param Tetrad: the stand-by tetrad
    */
   private void infoDisplay(Graphics gr, Tetrad nextTetrad)
   {
      Graphics2D g = (Graphics2D)gr;
      int edgeWidth = gameBoard.xPos();
      int centerX = (int)(dim.width/2);

      if ( !gameStarted || gameOver ) {
         /*
          * Control Instruction on the info area
          */
         g.setColor( ColorServer.getInfoAreaMsgColor() );
         g.setFont(MEDIUM_FONT);
         String msg = "Press ENTER to Play";
         g.drawString( msg, centerX - g.getFontMetrics().stringWidth(msg)/2, (int)(infoHeight/4) );

         g.setFont(SMALL_FONT);
         g.drawString( "VK_LEFT/RIGHT - Shift",  edgeWidth, (int)(infoHeight/2) );
         g.drawString( "VK_UP", edgeWidth, (int)(infoHeight*3/4) );
         int nxtPos = edgeWidth + g.getFontMetrics().stringWidth("VK_LEFT/RIGHT - Shift")
                                - g.getFontMetrics().stringWidth("- Rotate");
         g.drawString( "- Rotate", nxtPos,  (int)(infoHeight*3/4) );


         msg = "ESC/ENTER - Pause/Resume";
         int xPos = dim.width - edgeWidth - g.getFontMetrics().stringWidth(msg);
         g.drawString( msg,  xPos, (int)(infoHeight/2) );
         g.drawString( "VK_DOWN   - Fast Drop",    xPos, (int)(infoHeight*3/4) );

         if ( !gameStarted ) return;
         /*
          * game over! show the scores and star number
          */
         g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
         g.setColor( ColorServer.getGameBoardAreaMsgColor() );
         g.setFont(LARGE_FONT);
         msg = "SCORED  " + scores + "  POINTS";
         xPos = centerX - g.getFontMetrics().stringWidth(msg)/2;
         g.drawString( msg, xPos, (int)(dim.height*0.4) );

         return;
      }

      /*
       *  in-progress or paused
       *   left:   scores and levels
       *   midlle: standy-by tetrad in a grid box
       *   right: time played and a clock
       */

      // the scores/level
      g.setFont(SMALL_FONT);
      g.setColor( ColorServer.getInfoAreaMsgColor() );

      String msg = "Scores: " + scores;
      int leftEnd = edgeWidth + g.getFontMetrics().stringWidth(msg);
      g.drawString( msg, edgeWidth, (int)(infoHeight/3) );
      g.drawString( " Level: " + level,  edgeWidth, (int)(infoHeight*2/3) );

      // the clock/timer
      msg = "Timer: ";
      long seconds = (long)(timePlayed / 1000);   // translate to seconds
      int hr = (int)(seconds/3600);
      if ( hr < 10) msg += "0";
      msg += hr + ":";
      seconds = seconds%3600;
      int min = (int)(seconds/60);
      if ( min < 10 ) msg += "0";
      msg += min + ":";
      int sec = (int)(seconds % 60);
      if ( sec < 10 ) msg += "0";
      msg += sec;

      int rightStart = dim.width - edgeWidth - g.getFontMetrics().stringWidth(msg);
      g.drawString( msg, rightStart, (int)(infoHeight/3) );

      Calendar cal = Calendar.getInstance( TimeZone.getTimeZone("Canada/Eastern") );
      msg = "Clock: ";
      hr = cal.get(Calendar.HOUR_OF_DAY);
      if ( hr < 10) msg += "0";
      msg += hr + ":";
      min = cal.get(Calendar.MINUTE);
      if ( min < 10 ) msg += "0";
      msg += min + ":";
      sec = cal.get(Calendar.SECOND);
      if ( sec < 10 ) msg += "0";
      msg += sec;
      g.drawString( msg, rightStart, (int)(infoHeight*2/3) );

      /*
       * the stand-by tetrad in the middle area
       * show the tetrad in the middle between the score msg and the timer msg
       *  - the box grid numbers depends on the type of the tetrad
       */
      if ( nextTetrad != null ) {
         Point tetradSize = Tetrad.getSize(nextTetrad);
         int nrRow = tetradSize.y + 2;
         int nrCol = tetradSize.x + 2;
         int maxWidth = 2*Math.min(centerX - leftEnd, rightStart - centerX);
         int maxHeight = infoHeight - 2*gameBoard.cellSize();

         int cellSize = (int)(Math.min( maxWidth/(nrCol + 3), maxHeight/nrRow ));
         if (cellSize > 0.8*gameBoard.cellSize() )
            cellSize = (int)(0.8*gameBoard.cellSize());

         int xPos = centerX - (int)(nrCol*cellSize)/2;
         int yPos = gameBoard.cellSize() + (maxHeight - nrRow*cellSize)/2;

         Point pos = nextTetrad.getPos();
         Point showPos = Tetrad.getBestShowPos(nextTetrad);
         nextTetrad.setPos(showPos.x, showPos.y);

         GridBoard showBox = new GridBoard(nrRow, nrCol, xPos, yPos,
                                           cellSize, new BasicStroke(1.2f));
         boolean lightColor = false;
         if ( gamePaused ) lightColor = true;
         showBox.displayBoard(g, lightColor);                     // the grid
         showBox.displayTetrad(g, nextTetrad, lightColor, false); // the tetrad

         nextTetrad.setPos(pos.x, pos.y);
      }

      if ( gamePaused ) {
         // game paused - resume instruction on the game board
         g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
         g.setColor( ColorServer.getGameBoardAreaMsgColor() );
         g.setFont(LARGE_FONT);
         msg = "Press ENTER to Resume";
         int xPos = centerX - g.getFontMetrics().stringWidth(msg)/2;
         g.drawString( msg, xPos, (int)(dim.height*0.4) );
      }
   }

   /*
    * set the droping position such that the tetrad
    * will not skip the first row
    */
   private void setAsCurrTetrad(Tetrad tetrad )
   {
      currTetrad = tetrad;
      currTetrad.setPos(6, 0 - tetrad.getTopIndent());
      nextDropRowNr = 1;
   }

   /*
    * we determine the size of the show box based on the real size
    * of the tetard (add one exta row/column on each side)
    * Set the stand-by tetrad position to (0, 0) will display it
    * in the center of the show box
    */
   private void setAsNextTetrad(Tetrad tetrad)
   {
      nextTetrad = tetrad;
      // postion in the show box
      nextTetrad.setPos(0, 0);
   }

   /*
    * start a game
    */
   private void start()
   {
      setAsCurrTetrad( Tetrad.next() );
      setAsNextTetrad( Tetrad.next() );
   }

   /**
    * caculate the cell size on the game board; called when we create the gameboard
    *  dim != null must hold
    *  add 2 on both dimensions to get have one cell size edge for game board
    *
    * @return int: the side length of the grid cell
    */
   private int calculateCellSize()
   {
      int vertical = dim.height / (rowNr + infoHeight + 2);
      int horizontal = dim.width / (colNr + 2);
      if ( vertical > horizontal ) {
         return horizontal;
      }
      return vertical;
   }

} // end of class Tetris