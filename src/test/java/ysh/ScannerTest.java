package ysh;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ScannerTest {

    @Test
    void scansSimpleCommand() {
        Scanner scanner = new Scanner("echo hello");
        scanner.scanAll();

        assertNotNull(scanner);
        assertEquals(scanner.tokens.size(), 3);
        assertEquals(scanner.tokens.get(0).type, Type.TokenType.TEXT);
        assertEquals(scanner.tokens.get(0).lexeme, "echo");

        assertEquals(scanner.tokens.get(1).lexeme, "hello");
        assertEquals(scanner.tokens.get(1).type, Type.TokenType.TEXT);

        assertEquals(scanner.tokens.get(2).type, Type.TokenType.EOF);
    }

    @Test
    void scansRedirectionOut() {
        Scanner scanner = new Scanner("echo hello > out.txt");
        scanner.scanAll();

        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(0).type);
        assertEquals("echo", scanner.tokens.get(0).lexeme);

        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(1).type);
        assertEquals("hello", scanner.tokens.get(1).lexeme);

        assertEquals(Type.TokenType.REDIRECT_OUT, scanner.tokens.get(2).type);

        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(3).type);
        assertEquals("out.txt", scanner.tokens.get(3).lexeme);

        assertEquals(Type.TokenType.EOF, scanner.tokens.get(4).type);
    }


    @Test
    void scansPipe() {
        Scanner scanner = new Scanner("echo hello | findstr h");
        scanner.scanAll();

        assertEquals("echo", scanner.tokens.get(0).lexeme);
        assertEquals("hello", scanner.tokens.get(1).lexeme);
        assertEquals(Type.TokenType.PIPE, scanner.tokens.get(2).type);
        assertEquals("findstr", scanner.tokens.get(3).lexeme);
        assertEquals("h", scanner.tokens.get(4).lexeme);
        assertEquals(Type.TokenType.EOF, scanner.tokens.get(5).type);
    }

    @Test
    void scansConditionalAnd() {
        Scanner scanner = new Scanner("echo hello && echo found");
        scanner.scanAll();

        assertEquals("echo", scanner.tokens.get(0).lexeme);
        assertEquals("hello", scanner.tokens.get(1).lexeme);
        assertEquals(Type.TokenType.AND_CONDITIONAL, scanner.tokens.get(2).type);
        assertEquals("echo", scanner.tokens.get(3).lexeme);
        assertEquals("found", scanner.tokens.get(4).lexeme);
        assertEquals(Type.TokenType.EOF, scanner.tokens.get(5).type);
    }


    @Test
    void scansConditionalOr() {
        Scanner scanner = new Scanner("badcommand || echo failed");
        scanner.scanAll();

        assertEquals("badcommand", scanner.tokens.get(0).lexeme);
        assertEquals(Type.TokenType.OR_CONDITIONAL, scanner.tokens.get(1).type);
        assertEquals("echo", scanner.tokens.get(2).lexeme);
        assertEquals("failed", scanner.tokens.get(3).lexeme);
        assertEquals(Type.TokenType.EOF, scanner.tokens.get(4).type);
    }


    @Test
    void scansAppendRedirection() {
        Scanner scanner = new Scanner("echo hello >> out.txt");
        scanner.scanAll();

        assertEquals("echo", scanner.tokens.get(0).lexeme);
        assertEquals("hello", scanner.tokens.get(1).lexeme);
        assertEquals(Type.TokenType.REDIRECT_OUT_APPEND, scanner.tokens.get(2).type);
        assertEquals("out.txt", scanner.tokens.get(3).lexeme);
        assertEquals(Type.TokenType.EOF, scanner.tokens.get(4).type);
    }

    @Test
    void scansStdoutRedirection() {
        Scanner scanner = new Scanner("echo hello 1> out.txt");
        scanner.scanAll();

        assertEquals("echo", scanner.tokens.get(0).lexeme);
        assertEquals("hello", scanner.tokens.get(1).lexeme);
        assertEquals(Type.TokenType.REDIRECT_STDOUT, scanner.tokens.get(2).type);
        assertEquals("out.txt", scanner.tokens.get(3).lexeme);
        assertEquals(Type.TokenType.EOF, scanner.tokens.get(4).type);
    }

    @Test
    void scansStderrRedirection() {
        Scanner scanner = new Scanner("cmd 2> err.txt");
        scanner.scanAll();

        assertEquals("cmd", scanner.tokens.get(0).lexeme);
        assertEquals(Type.TokenType.REDIRECT_STDERR, scanner.tokens.get(1).type);
        assertEquals("err.txt", scanner.tokens.get(2).lexeme);
        assertEquals(Type.TokenType.EOF, scanner.tokens.get(3).type);
    }

    @Test
    void scansStderrToStdout() {
        Scanner scanner = new Scanner("cmd 2>&1");
        scanner.scanAll();

        assertEquals("cmd", scanner.tokens.get(0).lexeme);
        assertEquals(Type.TokenType.REDIRECT_STDERR_TO_STDOUT, scanner.tokens.get(1).type);
        assertEquals(Type.TokenType.EOF, scanner.tokens.get(2).type);
    }

    @Test
    void scansStdoutToStderr() {
        Scanner scanner = new Scanner("cmd 1>&2");
        scanner.scanAll();

        assertEquals("cmd", scanner.tokens.get(0).lexeme);
        assertEquals(Type.TokenType.REDIRECT_STDOUT_TO_STDERR, scanner.tokens.get(1).type);
        assertEquals(Type.TokenType.EOF, scanner.tokens.get(2).type);
    }


    @Test
    void scansNewline() {
        Scanner scanner = new Scanner("echo a\necho b");
        scanner.scanAll();

        assertEquals("echo", scanner.tokens.get(0).lexeme);
        assertEquals("a", scanner.tokens.get(1).lexeme);
        assertEquals(Type.TokenType.NEWLINE, scanner.tokens.get(2).type);
        assertEquals("echo", scanner.tokens.get(3).lexeme);
        assertEquals("b", scanner.tokens.get(4).lexeme);
        assertEquals(Type.TokenType.EOF, scanner.tokens.get(5).type);
    }

    @Test
    void scansSemicolonSeparator() {
        Scanner scanner = new Scanner("echo a; echo b");
        scanner.scanAll();

        assertEquals("echo", scanner.tokens.get(0).lexeme);
        assertEquals("a", scanner.tokens.get(1).lexeme);
        assertEquals(Type.TokenType.SEMI_COLON, scanner.tokens.get(2).type);
        assertEquals("echo", scanner.tokens.get(3).lexeme);
        assertEquals("b", scanner.tokens.get(4).lexeme);
        assertEquals(Type.TokenType.EOF, scanner.tokens.get(5).type);
    }

    @Test
    void scansAmpersandSeparator() {
        Scanner scanner = new Scanner("echo a & echo b");
        scanner.scanAll();

        assertEquals("echo", scanner.tokens.get(0).lexeme);
        assertEquals("a", scanner.tokens.get(1).lexeme);
        assertEquals(Type.TokenType.AND_SEPARATOR, scanner.tokens.get(2).type);
        assertEquals("echo", scanner.tokens.get(3).lexeme);
        assertEquals("b", scanner.tokens.get(4).lexeme);
        assertEquals(Type.TokenType.EOF, scanner.tokens.get(5).type);
    }

    @Test
    void scansDoubleQuotes() {
        Scanner scanner = new Scanner("echo \"hello world\"");
        scanner.scanAll();

        assertEquals("echo", scanner.tokens.get(0).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(0).type);

        assertEquals("\"", scanner.tokens.get(1).lexeme);
        assertEquals(Type.TokenType.DOUBLE_QUOTE, scanner.tokens.get(1).type);

        assertEquals("hello", scanner.tokens.get(2).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(2).type);

        assertEquals(" ", scanner.tokens.get(3).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(3).type);

        assertEquals("world", scanner.tokens.get(4).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(4).type);

        assertEquals("\"", scanner.tokens.get(5).lexeme);
        assertEquals(Type.TokenType.DOUBLE_QUOTE, scanner.tokens.get(5).type);

        assertEquals(Type.TokenType.EOF, scanner.tokens.get(6).type);
    }

    @Test
    void mixedQuotes() {
        Scanner scanner = new Scanner("echo \"hello 'world' \"");
        scanner.scanAll();

        assertEquals("echo", scanner.tokens.get(0).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(0).type);

        assertEquals("\"", scanner.tokens.get(1).lexeme);
        assertEquals(Type.TokenType.DOUBLE_QUOTE, scanner.tokens.get(1).type);

        assertEquals("hello", scanner.tokens.get(2).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(2).type);

        assertEquals(" ", scanner.tokens.get(3).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(3).type);

        assertEquals("'world'", scanner.tokens.get(4).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(4).type);

        assertEquals(" ", scanner.tokens.get(5).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(5).type);

        assertEquals("\"", scanner.tokens.get(6).lexeme);
        assertEquals(Type.TokenType.DOUBLE_QUOTE, scanner.tokens.get(6).type);

        assertEquals(Type.TokenType.EOF, scanner.tokens.get(7).type);
    }

    @Test
    void mixedQuotes2() {
        Scanner scanner = new Scanner("echo \"hello 'world \"");
        scanner.scanAll();

        assertEquals("echo", scanner.tokens.get(0).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(0).type);

        assertEquals("\"", scanner.tokens.get(1).lexeme);
        assertEquals(Type.TokenType.DOUBLE_QUOTE, scanner.tokens.get(1).type);

        assertEquals("hello", scanner.tokens.get(2).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(2).type);

        assertEquals(" ", scanner.tokens.get(3).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(3).type);

        assertEquals("'world", scanner.tokens.get(4).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(4).type);

        assertEquals(" ", scanner.tokens.get(5).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(5).type);

        assertEquals("\"", scanner.tokens.get(6).lexeme);
        assertEquals(Type.TokenType.DOUBLE_QUOTE, scanner.tokens.get(6).type);

        assertEquals(Type.TokenType.EOF, scanner.tokens.get(7).type);
    }

    @Test
    void scanSingleQuote() {
        Scanner scanner = new Scanner("echo 'hello ^'world' ");
        scanner.scanAll();

        assertEquals("echo", scanner.tokens.get(0).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(0).type);

        assertEquals("\'", scanner.tokens.get(1).lexeme);
        assertEquals(Type.TokenType.SINGLE_QUOTE, scanner.tokens.get(1).type);

        assertEquals("hello 'world", scanner.tokens.get(2).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(2).type);

        assertEquals("'", scanner.tokens.get(3).lexeme);
        assertEquals(Type.TokenType.SINGLE_QUOTE, scanner.tokens.get(3).type);

        assertEquals(Type.TokenType.EOF, scanner.tokens.get(4).type);
    }

    @Test
    void scanSingleQuote2() {
        Scanner scanner = new Scanner("echo 'hello >> 2>&1 && ^^^'world' ");
        scanner.scanAll();

        assertEquals("echo", scanner.tokens.get(0).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(0).type);

        assertEquals("\'", scanner.tokens.get(1).lexeme);
        assertEquals(Type.TokenType.SINGLE_QUOTE, scanner.tokens.get(1).type);

        assertEquals("hello >> 2>&1 && ^'world", scanner.tokens.get(2).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(2).type);

        assertEquals("'", scanner.tokens.get(3).lexeme);
        assertEquals(Type.TokenType.SINGLE_QUOTE, scanner.tokens.get(3).type);

        assertEquals(Type.TokenType.EOF, scanner.tokens.get(4).type);
    }

    @Test
    void operatorInsideDoubleQuoteIsText() {
        Scanner scanner = new Scanner("echo \"a >> b && c | d\"");
        scanner.scanAll();

        assertEquals("echo", scanner.tokens.get(0).lexeme);
        assertEquals(Type.TokenType.DOUBLE_QUOTE, scanner.tokens.get(1).type);
        assertEquals("a", scanner.tokens.get(2).lexeme);
        assertEquals(" ", scanner.tokens.get(3).lexeme);
        assertEquals(">>", scanner.tokens.get(4).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(4).type);
        assertEquals(" ", scanner.tokens.get(5).lexeme);
        assertEquals("b", scanner.tokens.get(6).lexeme);
        assertEquals(" ", scanner.tokens.get(7).lexeme);
        assertEquals("&&", scanner.tokens.get(8).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(8).type);
        assertEquals(" ", scanner.tokens.get(9).lexeme);
        assertEquals("c", scanner.tokens.get(10).lexeme);
        assertEquals(" ", scanner.tokens.get(11).lexeme);
        assertEquals("|", scanner.tokens.get(12).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(12).type);
        assertEquals(" ", scanner.tokens.get(13).lexeme);
        assertEquals("d", scanner.tokens.get(14).lexeme);
        assertEquals(Type.TokenType.DOUBLE_QUOTE, scanner.tokens.get(15).type);
        assertEquals(Type.TokenType.EOF, scanner.tokens.get(16).type);
    }

    @Test
    void expansionInsideDoubleQuoteIsStillTokenized() {
        Scanner scanner = new Scanner("echo \"hello $USER %PATH%\"");
        scanner.scanAll();

        assertEquals("echo", scanner.tokens.get(0).lexeme);
        assertEquals(Type.TokenType.DOUBLE_QUOTE, scanner.tokens.get(1).type);

        assertEquals("hello", scanner.tokens.get(2).lexeme);
        assertEquals(" ", scanner.tokens.get(3).lexeme);

        assertEquals(Type.TokenType.DOLLAR, scanner.tokens.get(4).type);
        assertEquals("USER", scanner.tokens.get(5).lexeme);

        assertEquals(" ", scanner.tokens.get(6).lexeme);

        assertEquals(Type.TokenType.PERCENT, scanner.tokens.get(7).type);
        assertEquals("PATH", scanner.tokens.get(8).lexeme);
        assertEquals(Type.TokenType.PERCENT, scanner.tokens.get(9).type);

        assertEquals(Type.TokenType.DOUBLE_QUOTE, scanner.tokens.get(10).type);
        assertEquals(Type.TokenType.EOF, scanner.tokens.get(11).type);
    }

    @Test
    void escapedOperatorOutsideQuoteBecomesText() {
        Scanner scanner = new Scanner("echo ^& ^| ^> ^<");
        scanner.scanAll();

        assertEquals("echo", scanner.tokens.get(0).lexeme);

        assertEquals("&", scanner.tokens.get(1).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(1).type);

        assertEquals("|", scanner.tokens.get(2).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(2).type);

        assertEquals(">", scanner.tokens.get(3).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(3).type);

        assertEquals("<", scanner.tokens.get(4).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(4).type);

        assertEquals(Type.TokenType.EOF, scanner.tokens.get(5).type);
    }

    @Test
    void escapedDollarDoesNotStartExpansion() {
        Scanner scanner = new Scanner("echo ^$USER");
        scanner.scanAll();

        assertEquals("echo", scanner.tokens.get(0).lexeme);
        assertEquals("$USER", scanner.tokens.get(1).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(1).type);
        assertEquals(Type.TokenType.EOF, scanner.tokens.get(2).type);
    }

    @Test
    void dollarExpansionOutsideQuote() {
        Scanner scanner = new Scanner("echo $USER");
        scanner.scanAll();

        assertEquals("echo", scanner.tokens.get(0).lexeme);
        assertEquals(Type.TokenType.DOLLAR, scanner.tokens.get(1).type);
        assertEquals("USER", scanner.tokens.get(2).lexeme);
        assertEquals(Type.TokenType.EOF, scanner.tokens.get(3).type);
    }

    @Test
    void braceExpansionOutsideQuote() {
        Scanner scanner = new Scanner("echo ${USER}");
        scanner.scanAll();

        assertEquals("echo", scanner.tokens.get(0).lexeme);
        assertEquals(Type.TokenType.DOLLAR, scanner.tokens.get(1).type);
        assertEquals(Type.TokenType.LEFT_CURLY_BRACE, scanner.tokens.get(2).type);
        assertEquals("USER", scanner.tokens.get(3).lexeme);
        assertEquals(Type.TokenType.RIGHT_CURLY_BRACE, scanner.tokens.get(4).type);
        assertEquals(Type.TokenType.EOF, scanner.tokens.get(5).type);
    }

    @Test
    void percentExpansionOutsideQuote() {
        Scanner scanner = new Scanner("echo %PATH%");
        scanner.scanAll();

        assertEquals("echo", scanner.tokens.get(0).lexeme);
        assertEquals(Type.TokenType.PERCENT, scanner.tokens.get(1).type);
        assertEquals("PATH", scanner.tokens.get(2).lexeme);
        assertEquals(Type.TokenType.PERCENT, scanner.tokens.get(3).type);
        assertEquals(Type.TokenType.EOF, scanner.tokens.get(4).type);
    }

    @Test
    void scansGroupedCommand() {
        Scanner scanner = new Scanner("(echo a & echo b) > out.txt");
        scanner.scanAll();

        assertEquals(Type.TokenType.LEFT_PAREN, scanner.tokens.get(0).type);
        assertEquals("echo", scanner.tokens.get(1).lexeme);
        assertEquals("a", scanner.tokens.get(2).lexeme);
        assertEquals(Type.TokenType.AND_SEPARATOR, scanner.tokens.get(3).type);
        assertEquals("echo", scanner.tokens.get(4).lexeme);
        assertEquals("b", scanner.tokens.get(5).lexeme);
        assertEquals(Type.TokenType.RIGHT_PAREN, scanner.tokens.get(6).type);
        assertEquals(Type.TokenType.REDIRECT_OUT, scanner.tokens.get(7).type);
        assertEquals("out.txt", scanner.tokens.get(8).lexeme);
        assertEquals(Type.TokenType.EOF, scanner.tokens.get(9).type);
    }

    @Test
    void multipleNewlinesAreTokens() {
        Scanner scanner = new Scanner("echo a\n\n\necho b");
        scanner.scanAll();

        assertEquals("echo", scanner.tokens.get(0).lexeme);
        assertEquals("a", scanner.tokens.get(1).lexeme);
        assertEquals(Type.TokenType.NEWLINE, scanner.tokens.get(2).type);
        assertEquals(Type.TokenType.NEWLINE, scanner.tokens.get(3).type);
        assertEquals(Type.TokenType.NEWLINE, scanner.tokens.get(4).type);
        assertEquals("echo", scanner.tokens.get(5).lexeme);
        assertEquals("b", scanner.tokens.get(6).lexeme);
        assertEquals(Type.TokenType.EOF, scanner.tokens.get(7).type);
    }

    @Test
    void Backticks() {
        Scanner scanner = new Scanner("echo $`cat test.txt`");
        scanner.scanAll();

        assertEquals("echo", scanner.tokens.get(0).lexeme);

        assertEquals("$", scanner.tokens.get(1).lexeme);
        assertEquals(Type.TokenType.DOLLAR, scanner.tokens.get(1).type);

        assertEquals("`", scanner.tokens.get(2).lexeme);
        assertEquals(Type.TokenType.BACKTICK, scanner.tokens.get(2).type);

        assertEquals("cat test.txt", scanner.tokens.get(3).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(3).type);

        assertEquals("`", scanner.tokens.get(4).lexeme);
        assertEquals(Type.TokenType.BACKTICK, scanner.tokens.get(4).type);

        assertEquals(Type.TokenType.EOF, scanner.tokens.get(5).type);
    }

    @Test
    void doubleQuoteWithBackticks2() {
        Scanner scanner = new Scanner("echo \"$`cat test.txt` `cat test2.txt` \t >> \n\n $`cat text.txt3`\"");
        scanner.scanAll();

        assertEquals("echo", scanner.tokens.get(0).lexeme);

        assertEquals("\"", scanner.tokens.get(1).lexeme);
        assertEquals(Type.TokenType.DOUBLE_QUOTE, scanner.tokens.get(1).type);

        assertEquals("$", scanner.tokens.get(2).lexeme);
        assertEquals(Type.TokenType.DOLLAR, scanner.tokens.get(2).type);

        assertEquals("`", scanner.tokens.get(3).lexeme);
        assertEquals(Type.TokenType.BACKTICK, scanner.tokens.get(3).type);

        assertEquals("cat test.txt", scanner.tokens.get(4).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(4).type);

        assertEquals("`", scanner.tokens.get(5).lexeme);
        assertEquals(Type.TokenType.BACKTICK, scanner.tokens.get(5).type);

        assertEquals(" ", scanner.tokens.get(6).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(6).type);

        assertEquals("`", scanner.tokens.get(7).lexeme);
        assertEquals(Type.TokenType.BACKTICK, scanner.tokens.get(7).type);

        assertEquals("cat test2.txt", scanner.tokens.get(8).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(8).type);

        assertEquals("`", scanner.tokens.get(9).lexeme);
        assertEquals(Type.TokenType.BACKTICK, scanner.tokens.get(9).type);

        assertEquals(" \t ", scanner.tokens.get(10).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(10).type);

        assertEquals(">>", scanner.tokens.get(11).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(11).type);

        assertEquals(" \n\n ", scanner.tokens.get(12).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(12).type);

        assertEquals("$", scanner.tokens.get(13).lexeme);
        assertEquals(Type.TokenType.DOLLAR, scanner.tokens.get(13).type);

        assertEquals("`", scanner.tokens.get(14).lexeme);
        assertEquals(Type.TokenType.BACKTICK, scanner.tokens.get(14).type);

        assertEquals("cat text.txt3", scanner.tokens.get(15).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(15).type);

        assertEquals("`", scanner.tokens.get(16).lexeme);
        assertEquals(Type.TokenType.BACKTICK, scanner.tokens.get(16).type);

        assertEquals("\"", scanner.tokens.get(17).lexeme);
        assertEquals(Type.TokenType.DOUBLE_QUOTE, scanner.tokens.get(17).type);

        assertEquals(Type.TokenType.EOF, scanner.tokens.get(18).type);
    }
}