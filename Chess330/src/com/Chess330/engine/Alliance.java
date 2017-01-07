package com.Chess330.engine;

import com.Chess330.engine.board.BoardUtils;
import com.Chess330.engine.player.BlackPlayer;
import com.Chess330.engine.player.Player;
import com.Chess330.engine.player.WhitePlayer;

public enum Alliance {
	WHITE{
		@Override
		public int getDirection() {
			return -1;
		}

		@Override
		public boolean isWhite() {
			return true;
		}

		@Override
		public boolean isBlack() {
			return false;
		}

		@Override
		public Player choosePlayer(final WhitePlayer whitePlayer, final BlackPlayer blackPlayer) {
			return whitePlayer;
		}

		@Override
		public boolean isPawnPromotionSquare(int position) {
			return BoardUtils.FIRST_RANK[position];
		}

		@Override
		public int getOppositeDirection() {
			return DOWN_DIRECTION;
		}
	},	
	BLACK{
		@Override
		public int getDirection() {
			return 1;
		}

		@Override
		public boolean isWhite() {
			return false;
		}

		@Override
		public boolean isBlack() {
			return true;
		}

		@Override
		public Player choosePlayer(final WhitePlayer whitePlayer, final BlackPlayer blackPlayer) {
			return blackPlayer;
		}

		@Override
		public boolean isPawnPromotionSquare(int position) {
			return BoardUtils.EIGHTH_RANK[position];
		}

		@Override
		public int getOppositeDirection() {
			return UP_DIRECTION;
		}
	};	
	
	public abstract int getDirection();
	public abstract boolean isWhite();
	public abstract boolean isBlack();
	public abstract Player choosePlayer(final WhitePlayer whitePlayer, final BlackPlayer blackPlayer);
	public abstract boolean isPawnPromotionSquare(int position);
	public abstract int getOppositeDirection();
	
	 private static final int UP_DIRECTION = -1;
	 private static final int DOWN_DIRECTION = 1;
	
}
