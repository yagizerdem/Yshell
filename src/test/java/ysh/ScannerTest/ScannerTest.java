package ysh.ScannerTest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ysh.Preprocess;
import ysh.Scanner;
import ysh.Type;

import static org.junit.jupiter.api.Assertions.*;

class ScannerTest {

    @Test
    void scansSimpleCommand() {
        Scanner scanner = new Scanner(Preprocess.preprocess("echo hello"));
        scanner.scanAll();

        assertNotNull(scanner);
        assertEquals(scanner.tokens.size(), 4);
        Assertions.assertEquals(scanner.tokens.get(0).type, Type.TokenType.TEXT);
        assertEquals(scanner.tokens.get(0).lexeme, "echo");

        assertEquals(scanner.tokens.get(1).type, Type.TokenType.WORD_BREAK);

        assertEquals(scanner.tokens.get(2).lexeme, "hello");
        assertEquals(scanner.tokens.get(2).type, Type.TokenType.TEXT);

        assertEquals(scanner.tokens.get(3).type, Type.TokenType.EOF);
    }

    @Test
    void scansRedirectionOut() {
        Scanner scanner = new Scanner(Preprocess.preprocess("echo hello > out.txt"));
        scanner.scanAll();

        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(0).type);
        assertEquals("echo", scanner.tokens.get(0).lexeme);

        assertEquals(Type.TokenType.WORD_BREAK, scanner.tokens.get(1).type);

        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(2).type);
        assertEquals("hello", scanner.tokens.get(2).lexeme);

        assertEquals(Type.TokenType.WORD_BREAK, scanner.tokens.get(3).type);

        assertEquals(Type.TokenType.REDIRECT_OUT, scanner.tokens.get(4).type);

        assertEquals(Type.TokenType.WORD_BREAK, scanner.tokens.get(5).type);

        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(6).type);
        assertEquals("out.txt", scanner.tokens.get(6).lexeme);

        assertEquals(Type.TokenType.EOF, scanner.tokens.get(7).type);
    }

    @Test
    void scansPipe() {
        Scanner scanner = new Scanner(Preprocess.preprocess("echo hello | findstr h"));
        scanner.scanAll();

        assertEquals("echo", scanner.tokens.get(0).lexeme);

        assertEquals(Type.TokenType.WORD_BREAK, scanner.tokens.get(1).type);

        assertEquals("hello", scanner.tokens.get(2).lexeme);

        assertEquals(Type.TokenType.WORD_BREAK, scanner.tokens.get(3).type);

        assertEquals(Type.TokenType.PIPE, scanner.tokens.get(4).type);

        assertEquals(Type.TokenType.WORD_BREAK, scanner.tokens.get(5).type);

        assertEquals("findstr", scanner.tokens.get(6).lexeme);

        assertEquals(Type.TokenType.WORD_BREAK, scanner.tokens.get(7).type);

        assertEquals("h", scanner.tokens.get(8).lexeme);

        assertEquals(Type.TokenType.EOF, scanner.tokens.get(9).type);
    }

    @Test
    void scansConditionalAnd() {
        Scanner scanner = new Scanner(Preprocess.preprocess("echo hello && echo found"));
        scanner.scanAll();

        assertEquals("echo", scanner.tokens.get(0).lexeme);
        assertEquals(Type.TokenType.WORD_BREAK, scanner.tokens.get(1).type);
        assertEquals("hello", scanner.tokens.get(2).lexeme);
        assertEquals(Type.TokenType.WORD_BREAK, scanner.tokens.get(3).type);
        assertEquals(Type.TokenType.AND_CONDITIONAL, scanner.tokens.get(4).type);
        assertEquals(Type.TokenType.WORD_BREAK, scanner.tokens.get(5).type);
        assertEquals("echo", scanner.tokens.get(6).lexeme);
        assertEquals(Type.TokenType.WORD_BREAK, scanner.tokens.get(7).type);
        assertEquals("found", scanner.tokens.get(8).lexeme);
        assertEquals(Type.TokenType.EOF, scanner.tokens.get(9).type);
    }

    @Test
    void scansConditionalOr() {
        Scanner scanner = new Scanner(Preprocess.preprocess("badcommand || echo failed"));
        scanner.scanAll();

        assertEquals("badcommand", scanner.tokens.get(0).lexeme);
        assertEquals(Type.TokenType.WORD_BREAK, scanner.tokens.get(1).type);
        assertEquals(Type.TokenType.OR_CONDITIONAL, scanner.tokens.get(2).type);
        assertEquals(Type.TokenType.WORD_BREAK, scanner.tokens.get(3).type);
        assertEquals("echo", scanner.tokens.get(4).lexeme);
        assertEquals(Type.TokenType.WORD_BREAK, scanner.tokens.get(5).type);
        assertEquals("failed", scanner.tokens.get(6).lexeme);
        assertEquals(Type.TokenType.EOF, scanner.tokens.get(7).type);
    }


    @Test
    void scansAppendRedirection() {
        Scanner scanner = new Scanner(Preprocess.preprocess("echo hello >> out.txt"));
        scanner.scanAll();

        assertEquals("echo", scanner.tokens.get(0).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(0).type);

        assertEquals(Type.TokenType.WORD_BREAK, scanner.tokens.get(1).type);

        assertEquals("hello", scanner.tokens.get(2).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(2).type);

        assertEquals(Type.TokenType.WORD_BREAK, scanner.tokens.get(3).type);

        assertEquals(Type.TokenType.REDIRECT_OUT_APPEND, scanner.tokens.get(4).type);
        assertEquals(Type.TokenType.WORD_BREAK, scanner.tokens.get(5).type);

        assertEquals("out.txt", scanner.tokens.get(6).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(6).type);

        assertEquals(Type.TokenType.EOF, scanner.tokens.get(7).type);
    }

    @Test
    void scansStdoutRedirection() {
        Scanner scanner = new Scanner(Preprocess.preprocess("echo hello 1> out.txt"));
        scanner.scanAll();

        assertEquals("echo", scanner.tokens.get(0).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(0).type);

        assertEquals(Type.TokenType.WORD_BREAK, scanner.tokens.get(1).type);

        assertEquals("hello", scanner.tokens.get(2).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(2).type);

        assertEquals(Type.TokenType.WORD_BREAK, scanner.tokens.get(3).type);

        assertEquals(Type.TokenType.REDIRECT_STDOUT, scanner.tokens.get(4).type);

        assertEquals(Type.TokenType.WORD_BREAK, scanner.tokens.get(5).type);

        assertEquals("out.txt", scanner.tokens.get(6).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(6).type);

        assertEquals(Type.TokenType.EOF, scanner.tokens.get(7).type);
    }

    @Test
    void scansStderrRedirection() {
        Scanner scanner = new Scanner(Preprocess.preprocess("cmd 2> err.txt"));
        scanner.scanAll();

        assertEquals("cmd", scanner.tokens.get(0).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(0).type);

        assertEquals(Type.TokenType.WORD_BREAK, scanner.tokens.get(1).type);

        assertEquals(Type.TokenType.REDIRECT_STDERR, scanner.tokens.get(2).type);

        assertEquals(Type.TokenType.WORD_BREAK, scanner.tokens.get(3).type);

        assertEquals("err.txt", scanner.tokens.get(4).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(4).type);

        assertEquals(Type.TokenType.EOF, scanner.tokens.get(5).type);
    }

    @Test
    void scansStderrToStdout() {
        Scanner scanner = new Scanner(Preprocess.preprocess("cmd 2>&1"));
        scanner.scanAll();

        assertEquals("cmd", scanner.tokens.get(0).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(0).type);

        assertEquals(Type.TokenType.WORD_BREAK, scanner.tokens.get(1).type);

        assertEquals(Type.TokenType.REDIRECT_STDERR_TO_STDOUT, scanner.tokens.get(2).type);

        assertEquals(Type.TokenType.EOF, scanner.tokens.get(3).type);
    }

    @Test
    void scansStdoutToStderr() {
        Scanner scanner = new Scanner(Preprocess.preprocess("cmd 1>&2"));
        scanner.scanAll();

        assertEquals("cmd", scanner.tokens.get(0).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(0).type);

        assertEquals(Type.TokenType.WORD_BREAK, scanner.tokens.get(1).type);

        assertEquals(Type.TokenType.REDIRECT_STDOUT_TO_STDERR, scanner.tokens.get(2).type);

        assertEquals(Type.TokenType.EOF, scanner.tokens.get(3).type);
    }


    @Test
    void scansNewline() {
        Scanner scanner = new Scanner(Preprocess.preprocess("echo a\necho b"));
        scanner.scanAll();

        assertEquals("echo", scanner.tokens.get(0).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(0).type);

        assertEquals(Type.TokenType.WORD_BREAK, scanner.tokens.get(1).type);

        assertEquals("a", scanner.tokens.get(2).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(2).type);

        assertEquals(Type.TokenType.NEWLINE, scanner.tokens.get(3).type);

        assertEquals("echo", scanner.tokens.get(4).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(4).type);

        assertEquals(Type.TokenType.WORD_BREAK, scanner.tokens.get(5).type);

        assertEquals("b", scanner.tokens.get(6).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(6).type);

        assertEquals(Type.TokenType.EOF, scanner.tokens.get(7).type);
    }

    @Test
    void scansSemicolonSeparator() {
        Scanner scanner = new Scanner(Preprocess.preprocess("echo a; echo b"));
        scanner.scanAll();

        assertEquals("echo", scanner.tokens.get(0).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(0).type);

        assertEquals(Type.TokenType.WORD_BREAK, scanner.tokens.get(1).type);

        assertEquals("a", scanner.tokens.get(2).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(2).type);

        assertEquals(Type.TokenType.SEMI_COLON, scanner.tokens.get(3).type);

        assertEquals(Type.TokenType.WORD_BREAK, scanner.tokens.get(4).type);

        assertEquals("echo", scanner.tokens.get(5).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(5).type);

        assertEquals(Type.TokenType.WORD_BREAK, scanner.tokens.get(6).type);

        assertEquals("b", scanner.tokens.get(7).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(7).type);

        assertEquals(Type.TokenType.EOF, scanner.tokens.get(8).type);
    }

    @Test
    void scansAmpersandSeparator() {
        Scanner scanner = new Scanner(Preprocess.preprocess("echo a & echo b"));
        scanner.scanAll();

        assertEquals("echo", scanner.tokens.get(0).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(0).type);

        assertEquals(Type.TokenType.WORD_BREAK, scanner.tokens.get(1).type);

        assertEquals("a", scanner.tokens.get(2).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(2).type);

        assertEquals(Type.TokenType.WORD_BREAK, scanner.tokens.get(3).type);

        assertEquals(Type.TokenType.AND_SEPARATOR, scanner.tokens.get(4).type);

        assertEquals(Type.TokenType.WORD_BREAK, scanner.tokens.get(5).type);

        assertEquals("echo", scanner.tokens.get(6).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(6).type);

        assertEquals(Type.TokenType.WORD_BREAK, scanner.tokens.get(7).type);

        assertEquals("b", scanner.tokens.get(8).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(8).type);

        assertEquals(Type.TokenType.EOF, scanner.tokens.get(9).type);
    }

    @Test
    void scansDoubleQuotes() {
        Scanner scanner = new Scanner(Preprocess.preprocess("echo \"hello world\""));
        scanner.scanAll();

        assertEquals("echo", scanner.tokens.get(0).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(0).type);

        assertEquals(Type.TokenType.WORD_BREAK, scanner.tokens.get(1).type);

        assertEquals("\"", scanner.tokens.get(2).lexeme);
        assertEquals(Type.TokenType.DOUBLE_QUOTE, scanner.tokens.get(2).type);

        assertEquals("hello", scanner.tokens.get(3).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(3).type);

        assertEquals(" ", scanner.tokens.get(4).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(4).type);

        assertEquals("world", scanner.tokens.get(5).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(5).type);

        assertEquals("\"", scanner.tokens.get(6).lexeme);
        assertEquals(Type.TokenType.DOUBLE_QUOTE, scanner.tokens.get(6).type);

        assertEquals(Type.TokenType.EOF, scanner.tokens.get(7).type);
    }

    @Test
    void mixedQuotes() {
        Scanner scanner = new Scanner(Preprocess.preprocess("echo \"hello 'world' \""));
        scanner.scanAll();

        assertEquals("echo", scanner.tokens.get(0).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(0).type);

        assertEquals(Type.TokenType.WORD_BREAK, scanner.tokens.get(1).type);

        assertEquals("\"", scanner.tokens.get(2).lexeme);
        assertEquals(Type.TokenType.DOUBLE_QUOTE, scanner.tokens.get(2).type);

        assertEquals("hello", scanner.tokens.get(3).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(3).type);

        assertEquals(" ", scanner.tokens.get(4).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(4).type);

        assertEquals("'world'", scanner.tokens.get(5).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(5).type);

        assertEquals(" ", scanner.tokens.get(6).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(6).type);

        assertEquals("\"", scanner.tokens.get(7).lexeme);
        assertEquals(Type.TokenType.DOUBLE_QUOTE, scanner.tokens.get(7).type);

        assertEquals(Type.TokenType.EOF, scanner.tokens.get(8).type);
    }

    @Test
    void mixedQuotes2() {
        Scanner scanner = new Scanner(Preprocess.preprocess("echo \"hello 'world \""));
        scanner.scanAll();

        assertEquals("echo", scanner.tokens.get(0).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(0).type);

        assertEquals(Type.TokenType.WORD_BREAK, scanner.tokens.get(1).type);

        assertEquals("\"", scanner.tokens.get(2).lexeme);
        assertEquals(Type.TokenType.DOUBLE_QUOTE, scanner.tokens.get(2).type);

        assertEquals("hello", scanner.tokens.get(3).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(3).type);

        assertEquals(" ", scanner.tokens.get(4).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(4).type);

        assertEquals("'world", scanner.tokens.get(5).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(5).type);

        assertEquals(" ", scanner.tokens.get(6).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(6).type);

        assertEquals("\"", scanner.tokens.get(7).lexeme);
        assertEquals(Type.TokenType.DOUBLE_QUOTE, scanner.tokens.get(7).type);

        assertEquals(Type.TokenType.EOF, scanner.tokens.get(8).type);
    }

    @Test
    void scanSingleQuote() {
        Scanner scanner = new Scanner(Preprocess.preprocess("echo 'hello ^'world' "));
        scanner.scanAll();

        assertEquals("echo", scanner.tokens.get(0).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(0).type);

        assertEquals(Type.TokenType.WORD_BREAK, scanner.tokens.get(1).type);

        assertEquals("\'", scanner.tokens.get(2).lexeme);
        assertEquals(Type.TokenType.SINGLE_QUOTE, scanner.tokens.get(2).type);

        assertEquals("hello 'world", scanner.tokens.get(3).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(3).type);

        assertEquals("'", scanner.tokens.get(4).lexeme);
        assertEquals(Type.TokenType.SINGLE_QUOTE, scanner.tokens.get(4).type);

        assertEquals(Type.TokenType.WORD_BREAK, scanner.tokens.get(5).type);

        assertEquals(Type.TokenType.EOF, scanner.tokens.get(6).type);
    }

    @Test
    void scanSingleQuote2() {
        Scanner scanner = new Scanner(Preprocess.preprocess("echo 'hello >> 2>&1 && ^^^'world' "));
        scanner.scanAll();

        assertEquals("echo", scanner.tokens.get(0).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(0).type);

        assertEquals(Type.TokenType.WORD_BREAK, scanner.tokens.get(1).type);

        assertEquals("\'", scanner.tokens.get(2).lexeme);
        assertEquals(Type.TokenType.SINGLE_QUOTE, scanner.tokens.get(2).type);

        assertEquals("hello >> 2>&1 && ^'world", scanner.tokens.get(3).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(3).type);

        assertEquals("'", scanner.tokens.get(4).lexeme);
        assertEquals(Type.TokenType.SINGLE_QUOTE, scanner.tokens.get(4).type);

        assertEquals(Type.TokenType.WORD_BREAK, scanner.tokens.get(5).type);

        assertEquals(Type.TokenType.EOF, scanner.tokens.get(6).type);
    }

    @Test
    void operatorInsideDoubleQuoteIsText() {
        Scanner scanner = new Scanner(Preprocess.preprocess("echo \"a >> b && c | d\""));
        scanner.scanAll();

        assertEquals("echo", scanner.tokens.get(0).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(0).type);

        assertEquals(Type.TokenType.WORD_BREAK, scanner.tokens.get(1).type);

        assertEquals(Type.TokenType.DOUBLE_QUOTE, scanner.tokens.get(2).type);
        assertEquals("a", scanner.tokens.get(3).lexeme);
        assertEquals(" ", scanner.tokens.get(4).lexeme);
        assertEquals(">>", scanner.tokens.get(5).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(5).type);
        assertEquals(" ", scanner.tokens.get(6).lexeme);
        assertEquals("b", scanner.tokens.get(7).lexeme);
        assertEquals(" ", scanner.tokens.get(8).lexeme);
        assertEquals("&&", scanner.tokens.get(9).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(9).type);
        assertEquals(" ", scanner.tokens.get(10).lexeme);
        assertEquals("c", scanner.tokens.get(11).lexeme);
        assertEquals(" ", scanner.tokens.get(12).lexeme);
        assertEquals("|", scanner.tokens.get(13).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(13).type);
        assertEquals(" ", scanner.tokens.get(14).lexeme);
        assertEquals("d", scanner.tokens.get(15).lexeme);
        assertEquals(Type.TokenType.DOUBLE_QUOTE, scanner.tokens.get(16).type);
        assertEquals(Type.TokenType.EOF, scanner.tokens.get(17).type);
    }

    @Test
    void expansionInsideDoubleQuoteIsStillTokenized() {
        Scanner scanner = new Scanner(Preprocess.preprocess("echo \"hello $USER %PATH%\""));
        scanner.scanAll();

        assertEquals("echo", scanner.tokens.get(0).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(0).type);

        assertEquals(Type.TokenType.WORD_BREAK, scanner.tokens.get(1).type);

        assertEquals(Type.TokenType.DOUBLE_QUOTE, scanner.tokens.get(2).type);

        assertEquals("hello", scanner.tokens.get(3).lexeme);
        assertEquals(" ", scanner.tokens.get(4).lexeme);

        assertEquals(Type.TokenType.DOLLAR, scanner.tokens.get(5).type);
        assertEquals("USER", scanner.tokens.get(6).lexeme);

        assertEquals(" ", scanner.tokens.get(7).lexeme);

        assertEquals(Type.TokenType.PERCENT, scanner.tokens.get(8).type);
        assertEquals("PATH", scanner.tokens.get(9).lexeme);
        assertEquals(Type.TokenType.PERCENT, scanner.tokens.get(10).type);

        assertEquals(Type.TokenType.DOUBLE_QUOTE, scanner.tokens.get(11).type);
        assertEquals(Type.TokenType.EOF, scanner.tokens.get(12).type);
    }

    @Test
    void escapedOperatorOutsideQuoteBecomesText() {
        Scanner scanner = new Scanner(Preprocess.preprocess("echo ^& ^| ^> ^<"));
        scanner.scanAll();

        assertEquals("echo", scanner.tokens.get(0).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(0).type);

        assertEquals(Type.TokenType.WORD_BREAK, scanner.tokens.get(1).type);

        assertEquals("&", scanner.tokens.get(2).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(2).type);

        assertEquals(Type.TokenType.WORD_BREAK, scanner.tokens.get(3).type);

        assertEquals("|", scanner.tokens.get(4).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(4).type);

        assertEquals(Type.TokenType.WORD_BREAK, scanner.tokens.get(5).type);

        assertEquals(">", scanner.tokens.get(6).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(6).type);

        assertEquals(Type.TokenType.WORD_BREAK, scanner.tokens.get(7).type);

        assertEquals("<", scanner.tokens.get(8).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(8).type);

        assertEquals(Type.TokenType.EOF, scanner.tokens.get(9).type);
    }

    @Test
    void escapedDollarDoesNotStartExpansion() {
        Scanner scanner = new Scanner(Preprocess.preprocess("echo ^$USER"));
        scanner.scanAll();

        assertEquals("echo", scanner.tokens.get(0).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(0).type);

        assertEquals(Type.TokenType.WORD_BREAK, scanner.tokens.get(1).type);

        assertEquals("$USER", scanner.tokens.get(2).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(2).type);

        assertEquals(Type.TokenType.EOF, scanner.tokens.get(3).type);
    }

    @Test
    void dollarExpansionOutsideQuote() {
        Scanner scanner = new Scanner(Preprocess.preprocess("echo $USER"));
        scanner.scanAll();

        assertEquals("echo", scanner.tokens.get(0).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(0).type);

        assertEquals(Type.TokenType.WORD_BREAK, scanner.tokens.get(1).type);

        assertEquals(Type.TokenType.DOLLAR, scanner.tokens.get(2).type);

        assertEquals("USER", scanner.tokens.get(3).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(3).type);

        assertEquals(Type.TokenType.EOF, scanner.tokens.get(4).type);
    }

    @Test
    void braceExpansionOutsideQuote() {
        Scanner scanner = new Scanner(Preprocess.preprocess("echo ${USER}"));
        scanner.scanAll();

        assertEquals("echo", scanner.tokens.get(0).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(0).type);

        assertEquals(Type.TokenType.WORD_BREAK, scanner.tokens.get(1).type);

        assertEquals(Type.TokenType.DOLLAR, scanner.tokens.get(2).type);
        assertEquals(Type.TokenType.LEFT_CURLY_BRACE, scanner.tokens.get(3).type);

        assertEquals("USER", scanner.tokens.get(4).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(4).type);

        assertEquals(Type.TokenType.RIGHT_CURLY_BRACE, scanner.tokens.get(5).type);
        assertEquals(Type.TokenType.EOF, scanner.tokens.get(6).type);
    }

    @Test
    void percentExpansionOutsideQuote() {
        Scanner scanner = new Scanner(Preprocess.preprocess("echo %PATH%"));
        scanner.scanAll();

        assertEquals("echo", scanner.tokens.get(0).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(0).type);

        assertEquals(Type.TokenType.WORD_BREAK, scanner.tokens.get(1).type);

        assertEquals(Type.TokenType.PERCENT, scanner.tokens.get(2).type);

        assertEquals("PATH", scanner.tokens.get(3).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(3).type);

        assertEquals(Type.TokenType.PERCENT, scanner.tokens.get(4).type);
        assertEquals(Type.TokenType.EOF, scanner.tokens.get(5).type);
    }

    @Test
    void scansGroupedCommand() {
        Scanner scanner = new Scanner(Preprocess.preprocess("(echo a & echo b) > out.txt"));
        scanner.scanAll();

        assertEquals(Type.TokenType.LEFT_PAREN, scanner.tokens.get(0).type);

        assertEquals("echo", scanner.tokens.get(1).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(1).type);

        assertEquals(Type.TokenType.WORD_BREAK, scanner.tokens.get(2).type);

        assertEquals("a", scanner.tokens.get(3).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(3).type);

        assertEquals(Type.TokenType.WORD_BREAK, scanner.tokens.get(4).type);

        assertEquals(Type.TokenType.AND_SEPARATOR, scanner.tokens.get(5).type);

        assertEquals(Type.TokenType.WORD_BREAK, scanner.tokens.get(6).type);

        assertEquals("echo", scanner.tokens.get(7).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(7).type);

        assertEquals(Type.TokenType.WORD_BREAK, scanner.tokens.get(8).type);

        assertEquals("b", scanner.tokens.get(9).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(9).type);

        assertEquals(Type.TokenType.RIGHT_PAREN, scanner.tokens.get(10).type);

        assertEquals(Type.TokenType.WORD_BREAK, scanner.tokens.get(11).type);

        assertEquals(Type.TokenType.REDIRECT_OUT, scanner.tokens.get(12).type);

        assertEquals(Type.TokenType.WORD_BREAK, scanner.tokens.get(13).type);

        assertEquals("out.txt", scanner.tokens.get(14).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(14).type);

        assertEquals(Type.TokenType.EOF, scanner.tokens.get(15).type);
    }

    @Test
    void multipleNewlinesAreTokens() {
        Scanner scanner = new Scanner(Preprocess.preprocess("echo a\n\n\necho b"));
        scanner.scanAll();

        assertEquals("echo", scanner.tokens.get(0).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(0).type);

        assertEquals(Type.TokenType.WORD_BREAK, scanner.tokens.get(1).type);

        assertEquals("a", scanner.tokens.get(2).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(2).type);

        assertEquals(Type.TokenType.NEWLINE, scanner.tokens.get(3).type);
        assertEquals(Type.TokenType.NEWLINE, scanner.tokens.get(4).type);
        assertEquals(Type.TokenType.NEWLINE, scanner.tokens.get(5).type);

        assertEquals("echo", scanner.tokens.get(6).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(6).type);

        assertEquals(Type.TokenType.WORD_BREAK, scanner.tokens.get(7).type);

        assertEquals("b", scanner.tokens.get(8).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(8).type);

        assertEquals(Type.TokenType.EOF, scanner.tokens.get(9).type);
    }

    @Test
    void Backticks() {
        Scanner scanner = new Scanner(Preprocess.preprocess("echo $`cat test.txt`"));
        scanner.scanAll();

        assertEquals("echo", scanner.tokens.get(0).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(0).type);

        assertEquals(Type.TokenType.WORD_BREAK, scanner.tokens.get(1).type);

        assertEquals("$", scanner.tokens.get(2).lexeme);
        assertEquals(Type.TokenType.DOLLAR, scanner.tokens.get(2).type);

        assertEquals("`", scanner.tokens.get(3).lexeme);
        assertEquals(Type.TokenType.BACKTICK, scanner.tokens.get(3).type);

        assertEquals("cat test.txt", scanner.tokens.get(4).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(4).type);

        assertEquals("`", scanner.tokens.get(5).lexeme);
        assertEquals(Type.TokenType.BACKTICK, scanner.tokens.get(5).type);

        assertEquals(Type.TokenType.EOF, scanner.tokens.get(6).type);
    }

    @Test
    void doubleQuoteWithBackticks2() {
        Scanner scanner = new Scanner(Preprocess.preprocess("echo \"$`cat test.txt` `cat test2.txt` \t >> \n\n $`cat text.txt3`\""));
        scanner.scanAll();

        assertEquals("echo", scanner.tokens.get(0).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(0).type);

        assertEquals(Type.TokenType.WORD_BREAK, scanner.tokens.get(1).type);

        assertEquals("\"", scanner.tokens.get(2).lexeme);
        assertEquals(Type.TokenType.DOUBLE_QUOTE, scanner.tokens.get(2).type);

        assertEquals("$", scanner.tokens.get(3).lexeme);
        assertEquals(Type.TokenType.DOLLAR, scanner.tokens.get(3).type);

        assertEquals("`", scanner.tokens.get(4).lexeme);
        assertEquals(Type.TokenType.BACKTICK, scanner.tokens.get(4).type);

        assertEquals("cat test.txt", scanner.tokens.get(5).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(5).type);

        assertEquals("`", scanner.tokens.get(6).lexeme);
        assertEquals(Type.TokenType.BACKTICK, scanner.tokens.get(6).type);

        assertEquals(" ", scanner.tokens.get(7).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(7).type);

        assertEquals("`cat", scanner.tokens.get(8).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(8).type);

        assertEquals(" ", scanner.tokens.get(9).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(9).type);

        assertEquals("test2.txt`", scanner.tokens.get(10).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(10).type);

        assertEquals(" \t ", scanner.tokens.get(11).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(11).type);

        assertEquals(">>", scanner.tokens.get(12).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(12).type);

        assertEquals(" \n\n ", scanner.tokens.get(13).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(13).type);

        assertEquals("$", scanner.tokens.get(14).lexeme);
        assertEquals(Type.TokenType.DOLLAR, scanner.tokens.get(14).type);

        assertEquals("`", scanner.tokens.get(15).lexeme);
        assertEquals(Type.TokenType.BACKTICK, scanner.tokens.get(15).type);

        assertEquals("cat text.txt3", scanner.tokens.get(16).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(16).type);

        assertEquals("`", scanner.tokens.get(17).lexeme);
        assertEquals(Type.TokenType.BACKTICK, scanner.tokens.get(17).type);

        assertEquals("\"", scanner.tokens.get(18).lexeme);
        assertEquals(Type.TokenType.DOUBLE_QUOTE, scanner.tokens.get(18).type);

        assertEquals(Type.TokenType.EOF, scanner.tokens.get(19).type);
    }

    @Test
    void wordBoundary() {
        Scanner scanner = new Scanner(Preprocess.preprocess("echo yagiz\"erdem\""));
        scanner.scanAll();

        assertEquals("echo", scanner.tokens.get(0).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(0).type);

        assertEquals(Type.TokenType.WORD_BREAK, scanner.tokens.get(1).type);

        assertEquals("yagiz", scanner.tokens.get(2).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(2).type);

        assertEquals("\"", scanner.tokens.get(3).lexeme);
        assertEquals(Type.TokenType.DOUBLE_QUOTE, scanner.tokens.get(3).type);

        assertEquals("erdem", scanner.tokens.get(4).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(4).type);

        assertEquals("\"", scanner.tokens.get(5).lexeme);
        assertEquals(Type.TokenType.DOUBLE_QUOTE, scanner.tokens.get(5).type);

        assertEquals(Type.TokenType.EOF, scanner.tokens.get(6).type);


        scanner = new Scanner(Preprocess.preprocess("echo yagiz^\"erdem^\""));
        scanner.scanAll();

        assertEquals("echo", scanner.tokens.get(0).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(0).type);

        assertEquals(Type.TokenType.WORD_BREAK, scanner.tokens.get(1).type);

        assertEquals("yagiz\"erdem\"", scanner.tokens.get(2).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(2).type);

        assertEquals(Type.TokenType.EOF, scanner.tokens.get(3).type);
    }

    @Test
    void wordBreakOnEmpty() {
        Scanner scanner = new Scanner(Preprocess.preprocess(" "));
        scanner.scanAll();

        assertEquals(Type.TokenType.EOF, scanner.tokens.get(0).type);
    }

    @Test
    void wordBreakUnquotedSingle() {
        Scanner scanner = new Scanner(Preprocess.preprocess(" a "));
        scanner.scanAll();

        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(0).type);
        assertEquals(Type.TokenType.WORD_BREAK, scanner.tokens.get(1).type);
        assertEquals(Type.TokenType.EOF, scanner.tokens.get(2).type);
    }

    @Test
    void wordBreakUnquotedDouble() {
        Scanner scanner = new Scanner(Preprocess.preprocess(" a b "));
        scanner.scanAll();

        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(0).type);
        assertEquals(Type.TokenType.WORD_BREAK, scanner.tokens.get(1).type);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(2).type);
        assertEquals(Type.TokenType.WORD_BREAK, scanner.tokens.get(3).type);
        assertEquals(Type.TokenType.EOF, scanner.tokens.get(4).type);

        scanner = new Scanner(Preprocess.preprocess(" a b"));
        scanner.scanAll();

        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(0).type);
        assertEquals(Type.TokenType.WORD_BREAK, scanner.tokens.get(1).type);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(2).type);
        assertEquals(Type.TokenType.EOF, scanner.tokens.get(3).type);
    }
}