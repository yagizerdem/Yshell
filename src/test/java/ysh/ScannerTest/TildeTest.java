package ysh.ScannerTest;

import org.junit.jupiter.api.Test;
import ysh.Scanner;
import ysh.Type;

import static org.junit.jupiter.api.Assertions.*;

public class TildeTest {

    @Test
    void simpleTilde() {
        Scanner scanner = new Scanner("cd ~");
        scanner.scanAll();

        assertEquals("cd", scanner.tokens.get(0).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(0).type);
        assertEquals(Type.TokenType.WORD_BREAK, scanner.tokens.get(1).type);
        assertEquals("~", scanner.tokens.get(2).lexeme);
        assertEquals(Type.TokenType.TILDE, scanner.tokens.get(2).type);
        assertEquals(Type.TokenType.EOF, scanner.tokens.get(3).type);
    }

    @Test
    void tildeWithPath() {
        Scanner scanner = new Scanner("cd ~/Desktop");
        scanner.scanAll();

        assertEquals("cd", scanner.tokens.get(0).lexeme);
        assertEquals(Type.TokenType.WORD_BREAK, scanner.tokens.get(1).type);
        assertEquals("~", scanner.tokens.get(2).lexeme);
        assertEquals(Type.TokenType.TILDE, scanner.tokens.get(2).type);
        assertEquals("/Desktop", scanner.tokens.get(3).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(3).type);
        assertEquals(Type.TokenType.EOF, scanner.tokens.get(4).type);
    }

    @Test
    void tildeAfterTextShouldNotBeSpecial() {
        Scanner scanner = new Scanner("echo abc~");
        scanner.scanAll();

        assertEquals("echo", scanner.tokens.get(0).lexeme);
        assertEquals(Type.TokenType.WORD_BREAK, scanner.tokens.get(1).type);
        assertEquals("abc~", scanner.tokens.get(2).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(2).type);
        assertEquals(Type.TokenType.EOF, scanner.tokens.get(3).type);
    }

    @Test
    void tildeInMiddleOfPathShouldNotBeSpecial() {
        Scanner scanner = new Scanner("echo /tmp/~user/file");
        scanner.scanAll();

        assertEquals("echo", scanner.tokens.get(0).lexeme);
        assertEquals(Type.TokenType.WORD_BREAK, scanner.tokens.get(1).type);
        assertEquals("/tmp/~user/file", scanner.tokens.get(2).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(2).type);
        assertEquals(Type.TokenType.EOF, scanner.tokens.get(3).type);
    }

    @Test
    void tildeInsideDoubleQuotesShouldNotBeSpecial() {
        Scanner scanner = new Scanner("echo \"~\"");
        scanner.scanAll();

        assertEquals("echo", scanner.tokens.get(0).lexeme);
        assertEquals(Type.TokenType.WORD_BREAK, scanner.tokens.get(1).type);
        assertEquals(Type.TokenType.DOUBLE_QUOTE, scanner.tokens.get(2).type);
        assertEquals(Type.TokenType.TILDE, scanner.tokens.get(3).type);
        assertEquals(Type.TokenType.DOUBLE_QUOTE, scanner.tokens.get(4).type);
        assertEquals(Type.TokenType.EOF, scanner.tokens.get(5).type);
    }

    @Test
    void tildeInsideSingleQuotesShouldNotBeSpecial() {
        Scanner scanner = new Scanner("echo '~'");
        scanner.scanAll();

        assertEquals("echo", scanner.tokens.get(0).lexeme);
        assertEquals(Type.TokenType.WORD_BREAK, scanner.tokens.get(1).type);
        assertEquals(Type.TokenType.SINGLE_QUOTE, scanner.tokens.get(2).type);
        assertEquals("~", scanner.tokens.get(3).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(3).type);
        assertEquals(Type.TokenType.SINGLE_QUOTE, scanner.tokens.get(4).type);
        assertEquals(Type.TokenType.EOF, scanner.tokens.get(5).type);
    }

    @Test
    void escapedTildeShouldBeText() {
        Scanner scanner = new Scanner("echo ^~");
        scanner.scanAll();

        assertEquals("echo", scanner.tokens.get(0).lexeme);
        assertEquals(Type.TokenType.WORD_BREAK, scanner.tokens.get(1).type);
        assertEquals("~", scanner.tokens.get(2).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(2).type);
        assertEquals(Type.TokenType.EOF, scanner.tokens.get(3).type);
    }

    @Test
    void assignmentLikeWordTildeShouldNotBeSpecialInScanner() {
        Scanner scanner = new Scanner("HOME=~");
        scanner.scanAll();

        assertEquals("HOME=~", scanner.tokens.get(0).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(0).type);
        assertEquals(Type.TokenType.EOF, scanner.tokens.get(1).type);
    }

    @Test
    void tildeBeforeBacktickAtWordStartShouldBeSpecial() {
        Scanner scanner = new Scanner("echo ~$`cmd`");
        scanner.scanAll();

        assertEquals("echo", scanner.tokens.get(0).lexeme);
        assertEquals(Type.TokenType.WORD_BREAK, scanner.tokens.get(1).type);
        assertEquals(Type.TokenType.TILDE, scanner.tokens.get(2).type);
        assertEquals(Type.TokenType.DOLLAR, scanner.tokens.get(3).type);
        assertEquals(Type.TokenType.BACKTICK, scanner.tokens.get(4).type);
        assertEquals("cmd", scanner.tokens.get(5).lexeme);
        assertEquals(Type.TokenType.BACKTICK, scanner.tokens.get(6).type);
        assertEquals(Type.TokenType.EOF, scanner.tokens.get(7).type);
    }

    @Test
    void backtickTextContainsTildeRaw() {
        Scanner scanner = new Scanner("echo $`cd ~/Desktop`");
        scanner.scanAll();

        assertEquals("echo", scanner.tokens.get(0).lexeme);
        assertEquals(Type.TokenType.WORD_BREAK, scanner.tokens.get(1).type);
        assertEquals(Type.TokenType.DOLLAR, scanner.tokens.get(2).type);
        assertEquals(Type.TokenType.BACKTICK, scanner.tokens.get(3).type);
        assertEquals("cd ~/Desktop", scanner.tokens.get(4).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(4).type);
        assertEquals(Type.TokenType.BACKTICK, scanner.tokens.get(5).type);
        assertEquals(Type.TokenType.EOF, scanner.tokens.get(6).type);
    }


    @Test
    void escapedTildeAtWordStartShouldNotBeSpecial() {
        Scanner scanner = new Scanner("^~/Desktop");
        scanner.scanAll();

        assertEquals("~/Desktop", scanner.tokens.get(0).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(0).type);

        assertEquals(Type.TokenType.EOF, scanner.tokens.get(1).type);
    }

    @Test
    void escapedTildeAfterCommandShouldNotBeSpecial() {
        Scanner scanner = new Scanner("cd ^~/Desktop");
        scanner.scanAll();

        assertEquals("cd", scanner.tokens.get(0).lexeme);
        assertEquals(Type.TokenType.WORD_BREAK, scanner.tokens.get(1).type);

        assertEquals("~/Desktop", scanner.tokens.get(2).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(2).type);

        assertEquals(Type.TokenType.EOF, scanner.tokens.get(3).type);
    }

    @Test
    void escapedTildeInMiddleShouldStayText() {
        Scanner scanner = new Scanner("echo abc^~def");
        scanner.scanAll();

        assertEquals("echo", scanner.tokens.get(0).lexeme);
        assertEquals(Type.TokenType.WORD_BREAK, scanner.tokens.get(1).type);

        assertEquals("abc~def", scanner.tokens.get(2).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(2).type);

        assertEquals(Type.TokenType.EOF, scanner.tokens.get(3).type);
    }
}