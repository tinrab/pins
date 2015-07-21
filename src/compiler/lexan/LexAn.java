package compiler.lexan;

import compiler.Report;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

public class LexAn {

  private static final String[] keywords =
      new String[] {"arr",  "else", "for", "fun",   "if",
                    "then", "typ",  "var", "where", "while"};
  private static final String[] atomic =
      new String[] {"logical", "integer", "string"};
  private static final String[] other = new String[] {
      "&", "|", "!", "==", "!=", "<", ">", "<=", ">=", "*", "/", "%", "+",
      "-", "(", ")", "[",  "]",  "{", "}", ".",  ":",  ";", ",", "="};

  private boolean dump;
  private BufferedInputStream stream;
  private int lineNumber = 1, columnNumber;

  public LexAn(String sourceFileName, boolean dump) {
    try {
      stream = new BufferedInputStream(new FileInputStream(sourceFileName));
    } catch (FileNotFoundException e) {
      Report.error("Source file " + sourceFileName + " not found\n");
      System.exit(1);
    }

    this.dump = dump;
  }

  public Symbol lexAn() {
    Symbol symbol = null;

    try {
      int ch;

      while ((ch = stream.read()) != -1) {
        columnNumber++;

        if (ch == '#') {
          while (ch != '\n' && ch != '\r') {
            ch = stream.read();
          }

          stream.mark(1);
          int next = stream.read();

          if (next != 10 && next != 13) {
            stream.reset();
          }

          lineNumber++;
          columnNumber = 0;
        } else if (Character.isDigit(ch)) {
          StringBuffer sb = new StringBuffer();

          do {
            sb.append((char)ch);
            stream.mark(1);
            ch = stream.read();
          } while (Character.isDigit(ch));

          stream.reset();

          String lexeme = sb.toString();
          int endColumn = columnNumber + lexeme.length();

          symbol = new Symbol(Token.INT_CONST, lexeme, lineNumber, columnNumber,
                              lineNumber, endColumn);
          columnNumber = endColumn - 1;

          break;
        } else if (Character.isLetter(ch) || ch == '_') {
          StringBuffer sb = new StringBuffer();

          do {
            sb.append((char)ch);
            stream.mark(1);
            ch = stream.read();
          } while (Character.isLetterOrDigit(ch) || ch == '_');

          stream.reset();
          String lexeme = sb.toString();
          int token = Arrays.asList(keywords).indexOf(lexeme);

          if (token != -1) {
            token += Token.KW_ARR;
          } else {
            token = Arrays.asList(atomic).indexOf(lexeme);

            if (token != -1) {
              token += Token.LOGICAL;
            } else if (lexeme.equals("true") || lexeme.equals("false")) {
              token = Token.LOG_CONST;
            } else {
              token = Token.IDENTIFIER;
            }
          }

          int endColumn = columnNumber + lexeme.length();
          symbol = new Symbol(token, lexeme, lineNumber, columnNumber,
                              lineNumber, endColumn);
          columnNumber = endColumn - 1;

          break;
        } else if (ch == '\'') {
          StringBuffer sb = new StringBuffer();
          int endColumn = columnNumber + 1;
          sb.append((char)ch);

          do {
            ch = stream.read();
            endColumn++;

            if (ch < 32 || ch > 126) {
              Report.error(lineNumber, endColumn, "Illegal string character");
            }

            sb.append((char)ch);

            if (ch == '\'') {
              stream.mark(1);
              int next = stream.read();

              if (next == '\'') {
                sb.append('\'');
                endColumn++;
              } else {
                stream.reset();
                break;
              }
            } else if (ch == '\n' || ch == '\r' || ch == -1) {
              Report.error(
                  lineNumber, columnNumber,
                  "String literal is not properly closed by a apostrophe");
            }
          } while (true);

          String lexeme = sb.toString();

          symbol = new Symbol(Token.STR_CONST, lexeme, lineNumber, columnNumber,
                              lineNumber, endColumn);
          columnNumber = endColumn - 1;

          break;
        } else if (!Character.isWhitespace(ch)) {
          String lexeme = Character.toString((char)ch);

          if (ch == '>' || ch == '=' || ch == '!' || ch == '<') {
            stream.mark(1);

            if ((ch = stream.read()) == '=') {
              lexeme += '=';
            } else {
              stream.reset();
            }
          }

          int o = Arrays.asList(other).indexOf(lexeme);

          if (o == -1) {
            Report.error(lineNumber, columnNumber, "Illegal character");
          }

          int endColumn = columnNumber + lexeme.length();

          symbol = new Symbol(Token.AND + o, lexeme, lineNumber, columnNumber,
                              lineNumber, endColumn);
          columnNumber = endColumn - 1;

          break;
        } else if (ch == 10 || ch == 13) {
          lineNumber++;
          columnNumber = 0;

          stream.mark(1);
          int next = stream.read();

          if (next != 10 && next != 13) {
            stream.reset();
          }
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    if (symbol == null) {
      symbol = new Symbol(Token.EOF, "", lineNumber, columnNumber, lineNumber,
                          columnNumber);
    }

    dump(symbol);

    return symbol;
  }

  private void dump(Symbol symb) {
    if (!dump)
      return;
    if (Report.dumpFile() == null)
      return;
    if (symb.token == Token.EOF)
      Report.dumpFile().println(symb.toString());
    else
      Report.dumpFile().println("[" + symb.position.toString() + "] " +
                                symb.toString());
  }
}
