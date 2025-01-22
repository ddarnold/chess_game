package piece;

import main.GamePanel;
import main.Type;

public class King extends Piece {
    public King(int color, int col, int row) {
        super(color, col, row);

        type = Type.KING;

        if (color == GamePanel.WHITE) {
            image = getImage("/piece/UI_Project_Chess_W_King");
        } else {
            image = getImage("/piece/UI_Project_Chess_B_King");
        }
    }

    @Override
    public boolean canMove(int targetCol, int targetRow) {
        if (isWithinBoard(targetCol, targetRow)) {
            // Standard King Movement: One square in any direction
            if (Math.abs(targetCol - preCol) <= 1 && Math.abs(targetRow - preRow) <= 1) {
                return isValidSquare(targetCol, targetRow); // Ensure the target square is valid
            }

            // Castling Logic
            if (!moved && targetRow == preRow) { // Ensure the king hasn't moved and stays in the same row
                // Right Castling
                if (targetCol == preCol + 2 && isValidCastling(preCol + 3, preCol + 1, preCol + 2)) {
                    return true;
                }

                // Left Castling
                if (targetCol == preCol - 2 && isValidCastling(preCol - 4, preCol - 1, preCol - 2)) {
                    return true;
                }
            }
        }

        return false; // If none of the conditions are met, the move is invalid
    }

    // Helper Method to Validate Castling
    private boolean isValidCastling(int rookCol, int pathCol1, int pathCol2) {
        // Find the rook at the specified column
        Piece rook = GamePanel.simPieces.stream()
                .filter(p -> p.col == rookCol && p.row == preRow && p.type == Type.ROOK && !p.moved)
                .findFirst()
                .orElse(null);

        if (rook == null) return false; // No rook found or rook has moved

        // Check that the squares between the king and rook are empty
        if (GamePanel.simPieces.stream().anyMatch(p -> (p.col == pathCol1 || p.col == pathCol2) && p.row == preRow)) {
            return false;
        }

        // Ensure the king does not move through or into check
        if (GamePanel.simPieces.stream().anyMatch(p -> p.canMove(pathCol1, preRow) || p.canMove(pathCol2, preRow))) {
            return false;
        }

        // Set the rook for castling
        GamePanel.castlingPiece = rook;
        return true;
    }
}
