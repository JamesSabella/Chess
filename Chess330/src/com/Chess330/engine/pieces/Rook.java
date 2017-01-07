package com.Chess330.engine.pieces;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.Chess330.engine.Alliance;
import com.Chess330.engine.board.Board;
import com.Chess330.engine.board.BoardUtils;
import com.Chess330.engine.board.Move;
import com.Chess330.engine.board.Tile;
import com.Chess330.engine.board.Move.MajorAttackMove;
import com.Chess330.engine.board.Move.MajorMove;
import com.Chess330.engine.pieces.Piece.PieceType;
import com.google.common.collect.ImmutableList;

public class Rook extends Piece{
	
	private final static int[] CANDIDATE_VECTOR_COORDINATES = {-8, -1, 1, 8};

	public Rook(final Alliance pieceAlliance, final int piecePosition) {
		super(PieceType.ROOK, piecePosition, pieceAlliance, true);
	}
	
	public Rook(final Alliance pieceAlliance, final int piecePosition, final boolean isFirstMove){
		super(PieceType.ROOK, piecePosition, pieceAlliance, isFirstMove);
	}

	@Override
	public Collection<Move> calculateLegalMoves(final Board board) {
		final List<Move> legalMoves = new ArrayList<>();
		
		for(final int candidateCoordinateOffset : CANDIDATE_VECTOR_COORDINATES){
			int candidateDestinationCoordinate = this.piecePosition;
			while(BoardUtils.isValidTileCoordinate(candidateDestinationCoordinate)){
				
				if(isFirstColumnExculsion(candidateDestinationCoordinate, candidateCoordinateOffset) ||
						isEighthColumnExculsion(candidateDestinationCoordinate, candidateCoordinateOffset)){
					break;
				}
				
				candidateDestinationCoordinate += candidateCoordinateOffset;
				if(BoardUtils.isValidTileCoordinate(candidateDestinationCoordinate)){
					final Tile candidateDestinationTile = board.getTile(candidateDestinationCoordinate);
					if(!candidateDestinationTile.isTileOccupied()){
						legalMoves.add(new MajorMove(board, this, candidateDestinationCoordinate));
					}
					else{
						final Piece pieceAtDestination = candidateDestinationTile.getPiece();
						final Alliance pieceAlliance = pieceAtDestination.getPieceAlliance();
						if(this.pieceAlliance != pieceAlliance){
							legalMoves.add(new MajorAttackMove(board, this, candidateDestinationCoordinate, pieceAtDestination));
						}
						break;
					}
				}
			}
		}
		return ImmutableList.copyOf(legalMoves);
	}
	
	@Override
	public Rook movePiece(final Move move) {
		return new Rook(move.getMovedPiece().getPieceAlliance(), move.getDestinationCoordinate());
	}
	
	@Override 
	public String toString(){
		return PieceType.ROOK.toString();
	}
	
	private static boolean isFirstColumnExculsion(final int currentPosition, final int candidateOffset){
		return BoardUtils.FIRST_COLUMN[currentPosition] && (candidateOffset == -1);
	}
	
	private static boolean isEighthColumnExculsion(final int currentPosition, final int candidateOffset){
		return BoardUtils.EIGHTH_COLUMN[currentPosition] && (candidateOffset == 1);
	}
	
}
