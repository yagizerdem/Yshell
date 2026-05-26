package ysh.ScannerTest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ysh.Preprocess;
import ysh.Scanner;
import ysh.Type;

import static org.junit.jupiter.api.Assertions.*;

public class ScannerWordBreakTest {

    @Test
    void wordBreakMultipleSpacesCollapseToOne() {
        Scanner scanner = new Scanner(Preprocess.preprocess("echo     hello"));
        scanner.scanAll();

        assertEquals("echo", scanner.tokens.get(0).lexeme);
        Assertions.assertEquals(Type.TokenType.TEXT, scanner.tokens.get(0).type);

        assertEquals(Type.TokenType.WORD_BREAK, scanner.tokens.get(1).type);

        assertEquals("hello", scanner.tokens.get(2).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(2).type);

        assertEquals(Type.TokenType.EOF, scanner.tokens.get(3).type);
    }

    @Test
    void wordBreakTabsCollapseToOne() {
        Scanner scanner = new Scanner(Preprocess.preprocess("echo\t\tname"));
        scanner.scanAll();

        assertEquals("echo", scanner.tokens.get(0).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(0).type);

        assertEquals(Type.TokenType.WORD_BREAK, scanner.tokens.get(1).type);

        assertEquals("name", scanner.tokens.get(2).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(2).type);

        assertEquals(Type.TokenType.EOF, scanner.tokens.get(3).type);
    }

    @Test
    void leadingSpacesDoNotCreateWordBreak() {
        Scanner scanner = new Scanner(Preprocess.preprocess("   echo"));
        scanner.scanAll();

        assertEquals("echo", scanner.tokens.get(0).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(0).type);

        assertEquals(Type.TokenType.EOF, scanner.tokens.get(1).type);
    }

    @Test
    void trailingSpacesCreateWordBreakAfterText() {
        Scanner scanner = new Scanner(Preprocess.preprocess("echo   "));
        scanner.scanAll();

        assertEquals("echo", scanner.tokens.get(0).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(0).type);

        assertEquals(Type.TokenType.WORD_BREAK, scanner.tokens.get(1).type);

        assertEquals(Type.TokenType.EOF, scanner.tokens.get(2).type);
    }

    @Test
    void noWordBreakAroundAdjacentQuote() {
        Scanner scanner = new Scanner(Preprocess.preprocess("a\"b\""));
        scanner.scanAll();

        assertEquals("a", scanner.tokens.get(0).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(0).type);

        assertEquals("\"", scanner.tokens.get(1).lexeme);
        assertEquals(Type.TokenType.DOUBLE_QUOTE, scanner.tokens.get(1).type);

        assertEquals("b", scanner.tokens.get(2).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(2).type);

        assertEquals("\"", scanner.tokens.get(3).lexeme);
        assertEquals(Type.TokenType.DOUBLE_QUOTE, scanner.tokens.get(3).type);

        assertEquals(Type.TokenType.EOF, scanner.tokens.get(4).type);
    }

    @Test
    void wordBreakBeforeSeparatedQuote() {
        Scanner scanner = new Scanner(Preprocess.preprocess("a \"b\""));
        scanner.scanAll();

        assertEquals("a", scanner.tokens.get(0).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(0).type);

        assertEquals(Type.TokenType.WORD_BREAK, scanner.tokens.get(1).type);

        assertEquals("\"", scanner.tokens.get(2).lexeme);
        assertEquals(Type.TokenType.DOUBLE_QUOTE, scanner.tokens.get(2).type);

        assertEquals("b", scanner.tokens.get(3).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(3).type);

        assertEquals("\"", scanner.tokens.get(4).lexeme);
        assertEquals(Type.TokenType.DOUBLE_QUOTE, scanner.tokens.get(4).type);

        assertEquals(Type.TokenType.EOF, scanner.tokens.get(5).type);
    }

    @Test
    void wordBreakAroundOperator() {
        Scanner scanner = new Scanner(Preprocess.preprocess("echo  >  out.txt"));
        scanner.scanAll();

        assertEquals("echo", scanner.tokens.get(0).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(0).type);

        assertEquals(Type.TokenType.WORD_BREAK, scanner.tokens.get(1).type);

        assertEquals(Type.TokenType.REDIRECT_OUT, scanner.tokens.get(2).type);

        assertEquals(Type.TokenType.WORD_BREAK, scanner.tokens.get(3).type);

        assertEquals("out.txt", scanner.tokens.get(4).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(4).type);

        assertEquals(Type.TokenType.EOF, scanner.tokens.get(5).type);
    }

    @Test
    void newlineIsNotWordBreak() {
        Scanner scanner = new Scanner(Preprocess.preprocess("echo\nhello"));
        scanner.scanAll();

        assertEquals("echo", scanner.tokens.get(0).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(0).type);

        assertEquals(Type.TokenType.NEWLINE, scanner.tokens.get(1).type);

        assertEquals("hello", scanner.tokens.get(2).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(2).type);

        assertEquals(Type.TokenType.EOF, scanner.tokens.get(3).type);
    }

}
