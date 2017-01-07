package com.Chess330.gui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.*;

import com.Chess330.engine.board.Board;
import com.Chess330.engine.board.BoardUtils;
import com.Chess330.engine.board.Move;
import com.Chess330.engine.board.Tile;
import com.Chess330.engine.pieces.Piece;
import com.Chess330.engine.player.MoveTransition;
import com.google.common.collect.Lists;

import static javax.swing.SwingUtilities.isLeftMouseButton;
import static javax.swing.SwingUtilities.isRightMouseButton;

import java.awt.*;

public class Table {
	
	private final JFrame gameFrame;
	private final GameHistoryPanel gameHistoryPanel;
	private final TakenPiecesPanel takenPiecesPanel;
    private final ConsolePanel consolePanel;
	private final BoardPanel boardPanel;
	private final MoveLog moveLog;
	private Board chessBoard;
	
	private Tile sourceTile;
	private Tile destinationTile;
	private Piece humanMovedPiece;
	private BoardDirection boardDirection;
	
	private boolean highlightLegalMoves;
	
	private final static Dimension OUTER_FRAME_DIMENSION = new Dimension(600, 600);
	private final static Dimension BOARD_PANEL_DIMENSION = new Dimension(400, 350);
	private final static Dimension TILE_PANEL_DIMENSION = new Dimension(10, 10);
	private static String defaultPieceImagesPath = "art/pieces/plain/";
	
    private final Color lightTileColor = Color.decode("#FFFACD");
    private final Color darkTileColor = Color.decode("#593E1A");
    
    private static final Table INSTANCE = new Table();
	//creates a new instance of the table﻿
	
	public Table(){
		this.gameFrame = new JFrame("Chess330");
		this.gameFrame.setLayout(new BorderLayout());
		final JMenuBar tableMenuBar = creatTableMenuBar();
		this.gameFrame.setJMenuBar(tableMenuBar);
		this.gameFrame.setSize(OUTER_FRAME_DIMENSION);
		this.chessBoard = Board.createStandardBoard();
		this.gameHistoryPanel = new GameHistoryPanel();
		this.takenPiecesPanel = new TakenPiecesPanel();
		this.consolePanel = new ConsolePanel();
		this.boardPanel = new BoardPanel();
		this.moveLog = new MoveLog();
		this.boardDirection = BoardDirection.NORMAL;
		this.highlightLegalMoves = false;
		this.gameFrame.add(takenPiecesPanel, BorderLayout.WEST);
		this.gameFrame.add(this.boardPanel, BorderLayout.CENTER);
		this.gameFrame.add(this.gameHistoryPanel, BorderLayout.EAST);
		this.gameFrame.add(consolePanel, BorderLayout.SOUTH);
		this.gameFrame.setVisible(true);
	}
	
    public static Table get() {
        return INSTANCE;
    }
    
    MoveLog getMoveLog() {
        return this.moveLog;
    }
    
    GameHistoryPanel getGameHistoryPanel() {
        return this.gameHistoryPanel;
    }
    
    TakenPiecesPanel getTakenPiecesPanel() {
        return this.takenPiecesPanel;
    }
    
    ConsolePanel getConsolePanel() {
       return this.consolePanel;
    }
    
    BoardPanel getBoardPanel() {
        return this.boardPanel;
    }
	
	private JMenuBar creatTableMenuBar(){
		final JMenuBar tableMenuBar = new JMenuBar();
		tableMenuBar.add(createFileMenu());
		tableMenuBar.add(createPreferencesMenu());
		tableMenuBar.add(createOptionsMenu());
		return tableMenuBar;
	}
	
	private JMenu createFileMenu(){
		final JMenu fileMenu = new JMenu("File");
		final JMenuItem openPGN = new JMenuItem("Load PGN File");
		openPGN.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e){
				System.out.println("open up that pgn file!");
			}
		});
		fileMenu.add(openPGN);
		final JMenuItem exitMenuItem = new JMenuItem("Exit");
		exitMenuItem.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e){
				System.exit(0);
			}
		});
		fileMenu.add(exitMenuItem);
		return fileMenu;
	}
	
	JMenu createOptionsMenu() {
        final JMenu optionsMenu = new JMenu("Options");
        final JMenuItem resetMenuItem = new JMenuItem("New Game");
        resetMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                undoAllMoves();
            }
        });
        optionsMenu.add(resetMenuItem);
        return optionsMenu;
	}

	void undoAllMoves() {
        for(int i = Table.get().getMoveLog().size() - 1; i >= 0; i--) {
            final Move lastMove = Table.get().getMoveLog().removeMove(Table.get().getMoveLog().size() - 1);
            this.chessBoard = this.chessBoard.currentPlayer().unMakeMove(lastMove).getTransitionBoard();
        }
        Table.get().getMoveLog().clear();
        Table.get().getGameHistoryPanel().redo(chessBoard, Table.get().getMoveLog());
        Table.get().getTakenPiecesPanel().redo(Table.get().getMoveLog());
        Table.get().getBoardPanel().drawBoard(chessBoard);
        Table.get().getConsolePanel().redo();
    }
	
	private JMenu createPreferencesMenu(){
		final JMenu preferencesMenu = new JMenu("Preferences");
		final JMenuItem flipBoardMenuItem = new JMenuItem("Flip Board");
		flipBoardMenuItem.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(final ActionEvent e){
				boardDirection = boardDirection.opposite();
				boardPanel.drawBoard(chessBoard);
			}
		});
		preferencesMenu.add(flipBoardMenuItem);
		preferencesMenu.addSeparator();
		final JCheckBoxMenuItem legalMoveHighlighterCheckbox = new JCheckBoxMenuItem("Highlight Legal Moves", false);
		legalMoveHighlighterCheckbox.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e){
				highlightLegalMoves = legalMoveHighlighterCheckbox.isSelected();
			}
		});
		preferencesMenu.add(legalMoveHighlighterCheckbox);
		return preferencesMenu;
	}
	
	public enum BoardDirection{
		
		NORMAL{
			@Override
			List<TilePanel> traverse(final List<TilePanel> boardTiles){
				return boardTiles;
			}
			
			@Override
			BoardDirection opposite(){
				return FLIPPED;
			}
		},
		FLIPPED{
			@Override
			List<TilePanel> traverse(final List<TilePanel> boardTiles){
				return Lists.reverse(boardTiles);
			}
			
			@Override
			BoardDirection opposite(){
				return NORMAL;
			}
		};
		
		abstract List<TilePanel> traverse(final List<TilePanel> boardTiles);
		abstract BoardDirection opposite();
	}
	
	private class BoardPanel extends JPanel{
		final List<TilePanel> boardTiles;
		
		BoardPanel(){
			super(new GridLayout(8, 8));
			this.boardTiles = new ArrayList<>();
			for(int i = 0; i < BoardUtils.NUM_TILES; i++){
				final TilePanel tilePanel = new TilePanel(this, i);
				this.boardTiles.add(tilePanel);
				add(tilePanel);
			}
			setPreferredSize(BOARD_PANEL_DIMENSION);
			validate();
		}
		
		public void drawBoard(final Board board){
			removeAll();
			for(final TilePanel tilePanel : boardDirection.traverse(boardTiles)){
				tilePanel.drawTile(board);
				add(tilePanel);
			}
			validate();
			repaint();
		}
	}
	
	public static class MoveLog{
		private final List<Move> moves;
		
		MoveLog(){
			this.moves = new ArrayList<>();
		}
		
		public List<Move> getMoves(){
			return this.moves;
		}
		
		public void addMove(final Move move){
			this.moves.add(move);
		}
		
		public int size(){
			return this.moves.size();
		}
		
		public void clear(){
			this.moves.clear();
		}
		
		public Move removeMove(int index){
			return this.moves.remove(index);
		}
		
		public boolean removeMove(final Move move){
			return this.moves.remove(move);
		}
	}
	
	private class TilePanel extends JPanel{
		private final int tileId;
		TilePanel(BoardPanel boardPanel, final int tileId){
			super(new GridBagLayout());
			this.tileId = tileId;
			setPreferredSize(TILE_PANEL_DIMENSION);
			assignTileColor();
			assignTilePieceIcon(chessBoard);
			highlightTileBorder(chessBoard);
			addMouseListener(new MouseListener(){

				@Override
				public void mouseClicked(final MouseEvent e) {
					if(isRightMouseButton(e)){
						sourceTile = null;
						destinationTile = null;
						humanMovedPiece = null;
					}else if (isLeftMouseButton(e)){
						if(sourceTile == null){
							sourceTile = chessBoard.getTile(tileId);
							humanMovedPiece = sourceTile.getPiece();
							if(humanMovedPiece == null){
								sourceTile = null;
							}
						}else{
							destinationTile = chessBoard.getTile(tileId);
							final Move move = Move.MoveFactory.createMove(chessBoard, sourceTile.getTileCoordinate(), destinationTile.getTileCoordinate());
							final MoveTransition transition = chessBoard.currentPlayer().makeMove(move);
							if(transition.getMoveStatus().isDone()){
								chessBoard = transition.getTransitionBoard();
								moveLog.addMove(move);
							}
							sourceTile = null;
							destinationTile = null;
							humanMovedPiece = null;
						}
						SwingUtilities.invokeLater(new Runnable(){
							@Override
							public void run() {
								gameHistoryPanel.redo(chessBoard, moveLog);
								takenPiecesPanel.redo(moveLog);
								boardPanel.drawBoard(chessBoard);
							}
							
						});
					}	
				}

				@Override
				public void mouseEntered(final MouseEvent e) {
					
				}

				@Override
				public void mouseExited(final MouseEvent e) {
					
				}

				@Override
				public void mousePressed(final MouseEvent e) {
					
				}

				@Override
				public void mouseReleased(final MouseEvent e) {
					
				}
				
			});
			validate();
		}
		
		public void drawTile(final Board board){
			assignTileColor();
			assignTilePieceIcon(board);
			highlightTileBorder(board);
			highlightLegals(board);
			validate();
			repaint();
		}
		
		private void highlightTileBorder(final Board board) {
            if(humanMovedPiece != null &&
               humanMovedPiece.getPieceAlliance() == board.currentPlayer().getAlliance() &&
               humanMovedPiece.getPiecePosition() == this.tileId) {
                setBorder(BorderFactory.createLineBorder(Color.cyan));
            } else {
                setBorder(BorderFactory.createLineBorder(Color.GRAY));
            }
        }
		
		private void assignTilePieceIcon(final Board board){
			this.removeAll();
			if(board.getTile(this.tileId).isTileOccupied()){
				try{
				final BufferedImage image = 
						ImageIO.read(new File(defaultPieceImagesPath  + board.getTile(this.tileId).getPiece().getPieceAlliance().toString().substring(0,  1) +
								board.getTile(this.tileId).getPiece().toString() + ".gif"));
				add(new JLabel(new ImageIcon(image)));
			}catch(IOException e){
				e.printStackTrace();
			}
			}
		}
		
		private void highlightLegals(final Board board){
			if(highlightLegalMoves){
				for(final Move move : pieceLegalMoves(board)){
					if(move.getDestinationCoordinate() == this.tileId){
						try{
							add(new JLabel(new ImageIcon(ImageIO.read(new File("art/misc/green_dot.png")))));
						}catch(Exception e){
							e.printStackTrace();
						}
					}
				}
			}
		}
		
		private Collection<Move> pieceLegalMoves(final Board board){
			if(humanMovedPiece != null && humanMovedPiece.getPieceAlliance() == board.currentPlayer().getAlliance()){
				return humanMovedPiece.calculateLegalMoves(board);
			}
			return Collections.emptyList();
		}
		
		private void assignTileColor() {
			if(BoardUtils.EIGHTH_RANK[this.tileId] ||
					BoardUtils.SIXTH_RANK[this.tileId] ||
					BoardUtils.FOURTH_RANK[this.tileId] ||
					BoardUtils.SECOND_RANK[this.tileId]){
				setBackground(this.tileId % 2 == 0 ? lightTileColor : darkTileColor);
			}else if(BoardUtils.SEVENTH_RANK[this.tileId] ||
					BoardUtils.FIFTH_RANK[this.tileId] ||
					BoardUtils.THRID_RANK[this.tileId] ||
					BoardUtils.FIRST_RANK[this.tileId]){
				setBackground(this.tileId % 2 != 0 ? lightTileColor : darkTileColor);
			}
		}
	}

}
