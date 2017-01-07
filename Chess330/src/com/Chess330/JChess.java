package com.Chess330;

import com.Chess330.engine.board.Board;
import com.Chess330.gui.Table;

public class JChess {

	public static void main(String[] args) {
		Board board = Board.createStandardBoard();
		
		System.out.println(board);
		
		Table.get();
	}

}
