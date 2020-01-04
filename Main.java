
/**
 *  Sequence in which the classes shall be coded and tested:
 *
 *    Phase 1:   Complete classes Main, ColorServer, Tetrad,  GridBoard
 *               Finish part of class Gameboard and Tetris to allow the test of
 *               tetrad shown on the game board and dropping periodically.
 *               This phase focuses on
 *                    display gamebord and tetrad
 *                    the movedDown method of the Tetris
 *
 *    Phase 2:   Continue work on Gameboard and Tetris.
 *               Implement the logic in responset to player instruction:
 *                    shift, rotate, start/pause/resume keys
 *               Full line cleared and info area display (scores, levels, time ployed) updated
 *
 *    Phase 3:   Implement the logic for fast drop down, score levels change droping speed;
 */

// --------------------------------------------------------

/**
 *  Evidence of Progress
 *
 *    Version 1: May 30, 2018
 *               Display the game board, active tetrad;
 *               start/pause/resume game;
 *               shift & rotate Tetrad.
 *               Info display partially.
 *
 *    Version 2: June 3, 2018
 *               Fast drop,  clear full rows, scores rewarded; info area display finished;
 *
 *    Version 3: June 6, 2018
 *               Fast drop in a smooth way.
 *               Three speed levels: 0.7s, 0.6s and 0.5s drop one row
 *               let part of the last tetard enter the board (this tetrad made the game over)
 *
 */

// --------------------------------------------------------

/**
 * @auther Ryan Liu
 *
 * @version 3.0
 *
 * There are 7 types tetrads: I, O, T, L, J, S and Z.
 *   Each type have 4 orentations, rotated clockwise.
 *   They are defined in class Tetrad
 *
 * The tetris defned by Class Tetris is the controller of the game.
 * A tetris extends the Panel and implements the KeyListener.
 *
 *   On the panel, the tetris displays the tetrad moving on the GameBoard
 *   and some game information such as scores, time used, and the stand-by
 *   tetrad
 *
 *   The tetris listen to the instructions of the player and ask the Tetrad
 *   to take action to meet the request if it is feasible.
 *   The feasible check utlize the method defined in class GameBoard
 *
 *   The tetris is responsible for counting the scores and adjust the speed of
 *   the game (i.e., the droping speed of the tetrad) based on the scores.
 *
 * The tetrads come in diffrent colors. All the colors used for tetrads and
 * for displaying game infomation are defined in class ColorServer.
 *
 */

import java.awt.Frame;

/**
 * The Main class creates a not resizable Frame and
 * adds the Tetris instance to the Frame
 */
public class Main
{
   /**
    *  The main method, create the Fram and Tetris
    */
   public static void main(String[] args)
   {
      // the real cell size will be caclulated by Tetris
      int cellSize = 22;
      int colNr = 16;
      int rowNr = 23;
      int infoRowNr = 5;

      Frame frame = new Frame("Tetris");
      // plus 1 to add boundary for the game board
      frame.setSize( cellSize*(colNr + 1), cellSize*(rowNr + infoRowNr + 1) );
      frame.setLocation( 166, 6 );
      // not allow to resize for now
      frame.setResizable( false );
      frame.add( new Tetris(rowNr, colNr, infoRowNr) );
      frame.setVisible( true );
   }

} //end of class Main