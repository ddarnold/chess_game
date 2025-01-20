package piece;

import main.Board;
import main.GamePanel;
import main.Type;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

import static main.Constants.*;

public class Piece {

    public Type type;
    public BufferedImage image;
    public int x, y;
    public int col, row, preCol, preRow;
    public int color;
    public Piece hittingPiece;
    public boolean moved, twoStepped;

    public Piece(int color, int col, int row) {
        this.color = color;
        this.col = col;
        this.row = row;
        x = getX(col);
        y = getY(row);
        preCol = col;
        preRow = row;
    }

    public BufferedImage getImage(String imagePath) {
        BufferedImage image = null;

        try {
            image = ImageIO.read(getClass().getResourceAsStream("/res/" + imagePath + ".png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    public int getX(int col) {
        return col * Board.SQUARE_SIZE;
    }

    public int getY(int row) {
        return row * Board.SQUARE_SIZE;
    }

    public int getCol(int x) {
        return (x + Board.HALF_SQUARE_SIZE) / Board.SQUARE_SIZE;
    }

    public int getRow(int y) {
        return (y + Board.HALF_SQUARE_SIZE) / Board.SQUARE_SIZE;
    }

    public int getIndex() {
        for (int index = 0; index < GamePanel.simPieces.size(); index++) {
            if (GamePanel.simPieces.get(index) == this) {
                return index;
            }
        }

        return 0;
    }

    public void updatePosition() {
        if (type == Type.PAWN) {
            if (Math.abs(row - preRow) == 2) {
                twoStepped = true;
            }
        }

        x = getX(col);
        y = getY(row);
        preCol = col; // Direct assignment
        preRow = row; // Direct assignment
        moved = true;
    }

    private int[] getMove() {
        int[] move = new int[4];
        move[0] = preCol;
        move[1] = preRow;
        move[2] = col;
        move[3] = row;

        return move;
    }

    public String getMoveString() {
        return Arrays.toString(getMove());
    }

    public void resetPosition() {
        col = preCol;
        row = preRow;
        x = getX(preCol); // Use preCol
        y = getY(preRow); // Use preRow
    }

    public boolean canMove(int targetCol, int targetRow) {
        return false;
    }

    public boolean isWithinBoard(int targetCol, int targetRow) {
        return targetCol >= 0 && targetCol < 8 && targetRow >= 0 && targetRow < 8;
    }

    public boolean isSameSquare(int targetCol, int targetRow) {
        return targetCol == preCol && targetRow == preRow;
    }

    public boolean pieceIsOnStraightLine(int targetCol, int targetRow) {
        for (int c = preCol - 1; c > targetCol; c--) {
            for (Piece piece : GamePanel.simPieces) {
                if (piece.col == c && piece.row == targetRow) {
                    hittingPiece = piece;
                    return true;
                }
            }
        }

        for (int c = preCol + 1; c < targetCol; c++) {
            for (Piece piece : GamePanel.simPieces) {
                if (piece.col == c && piece.row == targetRow) {
                    hittingPiece = piece;
                    return true;
                }
            }
        }

        for (int r = preRow - 1; r > targetRow; r--) {
            for (Piece piece : GamePanel.simPieces) {
                if (piece.col == targetCol && piece.row == r) {
                    hittingPiece = piece;
                    return true;
                }
            }
        }

        for (int r = preRow + 1; r < targetRow; r++) {
            for (Piece piece : GamePanel.simPieces) {
                if (piece.col == targetCol && piece.row == r) {
                    hittingPiece = piece;
                    return true;
                }
            }
        }

        return false;
    }

    public boolean pieceIsOnDiagonalLine(int targetCol, int targetRow) {
        if (targetRow < preRow) {
            for (int c = preCol - 1; c > targetCol; c--) {
                int diff = Math.abs(c - preCol);
                for (Piece piece : GamePanel.simPieces) {
                    if (piece.col == c && piece.row == preRow - diff) {
                        hittingPiece = piece;
                        return true;
                    }
                }
            }

            for (int c = preCol + 1; c < targetCol; c++) {
                int diff = Math.abs(c - preCol);
                for (Piece piece : GamePanel.simPieces) {
                    if (piece.col == c && piece.row == preRow - diff) {
                        hittingPiece = piece;
                        return true;
                    }
                }
            }
        }

        if (targetRow > preRow) {
            for (int c = preCol - 1; c > targetCol; c--) {
                int diff = Math.abs(c - preCol);
                for (Piece piece : GamePanel.simPieces) {
                    if (piece.col == c && piece.row == preRow + diff) {
                        hittingPiece = piece;
                        return true;
                    }
                }
            }

            for (int c = preCol + 1; c < targetCol; c++) {
                int diff = Math.abs(c - preCol);
                for (Piece piece : GamePanel.simPieces) {
                    if (piece.col == c && piece.row == preRow + diff) {
                        hittingPiece = piece;
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public Piece getHittingPiece(int targetCol, int targetRow) {
        for (Piece piece : GamePanel.simPieces) {
            if (piece.col == targetCol && piece.row == targetRow && piece != this) {
                return piece;
            }
        }
        return null;
    }

    public boolean isValidSquare(int targetCol, int targetRow) {
        hittingPiece = getHittingPiece(targetCol, targetRow);

        if (hittingPiece == null) {
            return true;
        } else {
            if (hittingPiece.color != this.color) {
                return true;
            } else hittingPiece = null;
        }

        return false;
    }

    public List<int[]> getLegalMoves() {
        List<int[]> legalMoves = new ArrayList<>();
        for (int col = 0; col < 8; col++) {
            for (int row = 0; row < 8; row++) {
                if (canMove(col, row)) {
                    legalMoves.add(new int[]{col, row});
                }
            }
        }
        return legalMoves;
    }

    public boolean canMoveAnywhere() {
        return !getLegalMoves().isEmpty();
    }

    public int mobilityBonus() {
        int mobility = 0;
        for (int col = 0; col < 8; col++) {
            for (int row = 0; row < 8; row++) {
                if (canMove(col, row)) {
                    mobility++;
                }
            }
        }
        switch(this.type) {
            case PAWN: return mobility * 1; // Pawns benefit less from mobility
            case KNIGHT:
            case BISHOP: return mobility * 2; // Knights and Bishops benefit from mobility
            case ROOK: return mobility * 3; // Rooks like to have open files and ranks
            case QUEEN: return mobility * 4; // Queens benefit greatly from mobility
            case KING: return mobility * 2; // King's mobility is crucial in the endgame
            default: return 0;
        }
    }

    public void draw(Graphics2D g2) {
        g2.drawImage(
                image, x + MARGIN_X + SCALED_DOWN_VALUE,
                y + MARGIN_Y + SCALED_DOWN_VALUE,
                Board.SQUARE_SIZE - SCALED_DOWN_VALUE * 2,
                Board.SQUARE_SIZE - SCALED_DOWN_VALUE * 2,
                null
        );
    }
}
