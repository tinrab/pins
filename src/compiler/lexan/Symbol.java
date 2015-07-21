package compiler.lexan;

import compiler.*;

public class Symbol {

  public final int token;

  public final String lexeme;

  public final Position position;

  public Symbol(int token, String lexeme, int begLine, int begColumn,
                int endLine, int endColumn) {
    this.token = token;
    this.lexeme = lexeme;
    this.position = new Position(begLine, begColumn, endLine, endColumn);
  }

  public Symbol(int token, String lexeme, Position position) {
    this.token = token;
    this.lexeme = lexeme;
    this.position = position;
  }

  public boolean equals(Object obj) {
    if (obj == null)
      return false;
    return token == ((Symbol)obj).token;
  }

  @Override
  public String toString() {
    return Token.getTokenName(token) + ":" + lexeme;
  }
}
