package ysh.ScannerTest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ysh.Scanner;
import ysh.Type;
import ysharp.treewalk.YsharpException;

import static org.junit.jupiter.api.Assertions.*;

public class BacktickTest {

    @Test
    void simpleBackTick() {
        Scanner scanner = new Scanner("echo $`cmd 1` sudenaz yetkin");
        scanner.scanAll();

        Assertions.assertEquals(scanner.tokens.get(0).type, Type.TokenType.TEXT);
        assertEquals(scanner.tokens.get(0).lexeme, "echo");

        assertEquals(scanner.tokens.get(1).type, Type.TokenType.WORD_BREAK);

        assertEquals(scanner.tokens.get(2).lexeme, "$");
        assertEquals(scanner.tokens.get(2).type, Type.TokenType.DOLLAR);

        assertEquals(scanner.tokens.get(3).lexeme, "`");
        assertEquals(scanner.tokens.get(3).type, Type.TokenType.BACKTICK);

        assertEquals(scanner.tokens.get(4).lexeme, "cmd 1");
        assertEquals(scanner.tokens.get(4).type, Type.TokenType.TEXT);

        assertEquals(scanner.tokens.get(5).lexeme, "`");
        assertEquals(scanner.tokens.get(5).type, Type.TokenType.BACKTICK);

        assertEquals(scanner.tokens.get(6).type, Type.TokenType.WORD_BREAK);

        assertEquals(scanner.tokens.get(7).lexeme, "sudenaz");
        assertEquals(scanner.tokens.get(7).type, Type.TokenType.TEXT);

        assertEquals(scanner.tokens.get(8).type, Type.TokenType.WORD_BREAK);

        assertEquals(scanner.tokens.get(9).lexeme, "yetkin");
        assertEquals(scanner.tokens.get(9).type, Type.TokenType.TEXT);

        assertEquals(scanner.tokens.get(10).type, Type.TokenType.EOF);
    }

    @Test
    void emptyBacktick() {
        Scanner scanner = new Scanner("echo $``");
        scanner.scanAll();

        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(0).type);
        assertEquals("echo", scanner.tokens.get(0).lexeme);

        assertEquals(Type.TokenType.WORD_BREAK, scanner.tokens.get(1).type);

        assertEquals(Type.TokenType.DOLLAR, scanner.tokens.get(2).type);
        assertEquals(Type.TokenType.BACKTICK, scanner.tokens.get(3).type);

        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(4).type);
        assertEquals("", scanner.tokens.get(4).lexeme);

        assertEquals(Type.TokenType.BACKTICK, scanner.tokens.get(5).type);
        assertEquals(Type.TokenType.EOF, scanner.tokens.get(6).type);
    }

    @Test
    void backtickWithoutSpaceAfter() {
        Scanner scanner = new Scanner("echo $`cmd`abc");
        scanner.scanAll();

        assertEquals("echo", scanner.tokens.get(0).lexeme);
        assertEquals(Type.TokenType.WORD_BREAK, scanner.tokens.get(1).type);

        assertEquals(Type.TokenType.DOLLAR, scanner.tokens.get(2).type);
        assertEquals(Type.TokenType.BACKTICK, scanner.tokens.get(3).type);
        assertEquals("cmd", scanner.tokens.get(4).lexeme);
        assertEquals(Type.TokenType.BACKTICK, scanner.tokens.get(5).type);

        assertEquals("abc", scanner.tokens.get(6).lexeme);
        assertEquals(Type.TokenType.EOF, scanner.tokens.get(7).type);
    }

    @Test
    void backtickWithoutSpaceBefore() {
        Scanner scanner = new Scanner("echo a$`cmd`");
        scanner.scanAll();

        assertEquals("echo", scanner.tokens.get(0).lexeme);
        assertEquals(Type.TokenType.WORD_BREAK, scanner.tokens.get(1).type);

        assertEquals("a", scanner.tokens.get(2).lexeme);
        assertEquals(Type.TokenType.DOLLAR, scanner.tokens.get(3).type);
        assertEquals(Type.TokenType.BACKTICK, scanner.tokens.get(4).type);
        assertEquals("cmd", scanner.tokens.get(5).lexeme);
        assertEquals(Type.TokenType.BACKTICK, scanner.tokens.get(6).type);

        assertEquals(Type.TokenType.EOF, scanner.tokens.get(7).type);
    }

    @Test
    void backtickWithMultipleSpacesInside() {
        Scanner scanner = new Scanner("echo $`cmd    1     2`");
        scanner.scanAll();

        assertEquals("cmd    1     2", scanner.tokens.get(4).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(4).type);
    }

    @Test
    void backtickWithNewlineInside() {
        Scanner scanner = new Scanner("echo $`echo a\necho b`");
        scanner.scanAll();

        assertEquals("echo a\necho b", scanner.tokens.get(4).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(4).type);
    }

    @Test
    void backtickWithSemicolonInside() {
        Scanner scanner = new Scanner("echo $`echo a; echo b`");
        scanner.scanAll();

        assertEquals("echo a; echo b", scanner.tokens.get(4).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(4).type);
    }

    @Test
    void backtickWithPipeInside() {
        Scanner scanner = new Scanner("echo $`cat file.txt | grep test`");
        scanner.scanAll();

        assertEquals("cat file.txt | grep test", scanner.tokens.get(4).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(4).type);
    }

    @Test
    void backtickWithQuotesInside() {
        Scanner scanner = new Scanner("echo $`echo \"hello world\"`");
        scanner.scanAll();

        assertEquals("echo \"hello world\"", scanner.tokens.get(4).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(4).type);
    }

    @Test
    void backtickWithSingleQuotesInside() {
        Scanner scanner = new Scanner("echo $`echo 'hello world'`");
        scanner.scanAll();

        assertEquals("echo 'hello world'", scanner.tokens.get(4).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(4).type);
    }

    @Test
    void escapedBacktickInsideBacktick() {
        Scanner scanner = new Scanner("echo $`echo \\^`test\\^``");
        scanner.scanAll();

        assertEquals("echo \\`test\\`", scanner.tokens.get(4).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(4).type);
    }

    @Test
    void nestedBacktickRawText() {
        Scanner scanner = new Scanner("echo $`echo $`inner``");
        scanner.scanAll();

        assertEquals(Type.TokenType.DOLLAR, scanner.tokens.get(2).type);
        assertEquals(Type.TokenType.BACKTICK, scanner.tokens.get(3).type);
        assertEquals("echo $`inner`", scanner.tokens.get(4).lexeme);
        assertEquals(Type.TokenType.BACKTICK, scanner.tokens.get(5).type);
        assertEquals(Type.TokenType.EOF, scanner.tokens.get(6).type);
    }

    @Test
    void twoBackticksInSameLine() {
        Scanner scanner = new Scanner("echo $`one` $`two`");
        scanner.scanAll();

        assertEquals("echo", scanner.tokens.get(0).lexeme);
        assertEquals(Type.TokenType.WORD_BREAK, scanner.tokens.get(1).type);

        assertEquals(Type.TokenType.DOLLAR, scanner.tokens.get(2).type);
        assertEquals(Type.TokenType.BACKTICK, scanner.tokens.get(3).type);
        assertEquals("one", scanner.tokens.get(4).lexeme);
        assertEquals(Type.TokenType.BACKTICK, scanner.tokens.get(5).type);

        assertEquals(Type.TokenType.WORD_BREAK, scanner.tokens.get(6).type);

        assertEquals(Type.TokenType.DOLLAR, scanner.tokens.get(7).type);
        assertEquals(Type.TokenType.BACKTICK, scanner.tokens.get(8).type);
        assertEquals("two", scanner.tokens.get(9).lexeme);
        assertEquals(Type.TokenType.BACKTICK, scanner.tokens.get(10).type);

        assertEquals(Type.TokenType.EOF, scanner.tokens.get(11).type);
    }

    @Test
    void unclosedBacktickShouldThrow() {
        assertThrows(RuntimeException.class, () -> {
            Scanner scanner = new Scanner("echo $`cmd 1");
            scanner.scanAll();
        });
    }

    @Test
    void dollarWithoutBacktickShouldNotStartSubstitution() {
        Scanner scanner = new Scanner("echo $abc");
        scanner.scanAll();

        assertEquals("echo", scanner.tokens.get(0).lexeme);
        assertEquals(Type.TokenType.WORD_BREAK, scanner.tokens.get(1).type);

        assertEquals(Type.TokenType.DOLLAR, scanner.tokens.get(2).type);
        assertEquals("abc", scanner.tokens.get(3).lexeme);

        assertEquals(Type.TokenType.EOF, scanner.tokens.get(4).type);
    }

    @Test
    void nestedBacktickWithTextAfterInner() {
        Scanner scanner = new Scanner("echo $`a $`b` c`");
        scanner.scanAll();

        assertEquals("a $`b` c", scanner.tokens.get(4).lexeme);
        assertEquals(Type.TokenType.EOF, scanner.tokens.get(6).type);
    }

    @Test
    void doubleNestedBacktick() {
        Scanner scanner = new Scanner("echo $`a $`b $`c` d` e`");
        scanner.scanAll();

        assertEquals("a $`b $`c` d` e", scanner.tokens.get(4).lexeme);
    }

    @Test
    void unclosedNestedBacktickShouldThrow() {
        assertThrows(RuntimeException.class, () -> {
            Scanner scanner = new Scanner("echo $`a $`b`");
            scanner.scanAll();
        });
    }

    @Test
    void escapedDollarBeforeNestedBacktickShouldNotNest() {
        Scanner scanner = new Scanner("echo $`echo ^$`inner``");
        scanner.scanAll();

        assertEquals("echo $", scanner.tokens.get(4).lexeme);
        assertEquals(Type.TokenType.BACKTICK, scanner.tokens.get(5).type);
        assertEquals("inner", scanner.tokens.get(6).lexeme);
    }

    @Test
    void escapedBacktickDoesNotCloseOuter() {
        Scanner scanner = new Scanner("echo $`a ^` b`");
        scanner.scanAll();

        assertEquals("a ` b", scanner.tokens.get(4).lexeme);
    }

    @Test
    void danglingEscapeAtEndInsideBacktick() {
       assertThrows(YsharpException.class, () -> {
           Scanner scanner = new Scanner("echo $`abc^`");
           scanner.scanAll();

           assertEquals("abc", scanner.tokens.get(4).lexeme);
       });
    }

    @Test
    void onlyNestedCommandInsideBacktick() {
        Scanner scanner = new Scanner("echo $`$`inner``");
        scanner.scanAll();

        assertEquals("$`inner`", scanner.tokens.get(4).lexeme);
    }

    @Test
    void backtickInsideSingleQuotesShouldNotStartCommandSubstitution() {
        Scanner scanner = new Scanner("echo '$`cmd`'");
        scanner.scanAll();

        assertEquals("echo", scanner.tokens.get(0).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(0).type);

        assertEquals(Type.TokenType.WORD_BREAK, scanner.tokens.get(1).type);

        assertEquals("'", scanner.tokens.get(2).lexeme);
        assertEquals(Type.TokenType.SINGLE_QUOTE, scanner.tokens.get(2).type);

        assertEquals("$`cmd`", scanner.tokens.get(3).lexeme);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(3).type);

        assertEquals("'", scanner.tokens.get(4).lexeme);
        assertEquals(Type.TokenType.SINGLE_QUOTE, scanner.tokens.get(4).type);

        assertEquals(Type.TokenType.EOF, scanner.tokens.get(5).type);
    }

    @Test
    void backtickInsideDoubleQuotesDependsOnYourDesign() {
        Scanner scanner = new Scanner("echo \"$`cmd`\"");
        scanner.scanAll();
        assertEquals(Type.TokenType.WORD_BREAK, scanner.tokens.get(1).type);
        assertEquals(Type.TokenType.DOUBLE_QUOTE, scanner.tokens.get(2).type);
        assertEquals(Type.TokenType.DOLLAR, scanner.tokens.get(3).type);
        assertEquals(Type.TokenType.BACKTICK, scanner.tokens.get(4).type);
        assertEquals(Type.TokenType.TEXT, scanner.tokens.get(5).type);
        assertEquals(Type.TokenType.BACKTICK, scanner.tokens.get(6).type);
        assertEquals(Type.TokenType.DOUBLE_QUOTE, scanner.tokens.get(7).type);
        assertEquals(Type.TokenType.EOF, scanner.tokens.get(8).type);
    }

    @Test
    void tripleNestedBacktickRawText() {
        Scanner scanner = new Scanner("echo $`a $`b $`c $`d` e` f` g`");
        scanner.scanAll();

        assertEquals(Type.TokenType.DOLLAR, scanner.tokens.get(2).type);
        assertEquals(Type.TokenType.BACKTICK, scanner.tokens.get(3).type);
        assertEquals("a $`b $`c $`d` e` f` g", scanner.tokens.get(4).lexeme);
        assertEquals(Type.TokenType.BACKTICK, scanner.tokens.get(5).type);
        assertEquals(Type.TokenType.EOF, scanner.tokens.get(6).type);
    }

    @Test
    void quadrupleNestedBacktickRawText() {
        Scanner scanner = new Scanner("echo $`a $`b $`c $`d $`e` f` g` h` i`");
        scanner.scanAll();

        assertEquals(Type.TokenType.DOLLAR, scanner.tokens.get(2).type);
        assertEquals(Type.TokenType.BACKTICK, scanner.tokens.get(3).type);
        assertEquals("a $`b $`c $`d $`e` f` g` h` i", scanner.tokens.get(4).lexeme);
        assertEquals(Type.TokenType.BACKTICK, scanner.tokens.get(5).type);
        assertEquals(Type.TokenType.EOF, scanner.tokens.get(6).type);
    }

    @Test
    void tripleNestedOnlyCommands() {
        Scanner scanner = new Scanner("echo $`$`$`$`inner````");
        scanner.scanAll();

        assertEquals("$`$`$`inner```", scanner.tokens.get(4).lexeme);
        assertEquals(Type.TokenType.EOF, scanner.tokens.get(6).type);
    }

    @Test
    void quadrupleNestedOnlyCommands() {
        Scanner scanner = new Scanner("echo $`$`$`$`$`inner`````");
        scanner.scanAll();

        assertEquals("$`$`$`$`inner````", scanner.tokens.get(4).lexeme);
        assertEquals(Type.TokenType.EOF, scanner.tokens.get(6).type);
    }

    @Test
    void tripleNestedWithTextAfterEveryLevel() {
        Scanner scanner = new Scanner("echo $`L1 $`L2 $`L3 $`L4` after4` after3` after2` after1");
        scanner.scanAll();

        assertEquals("L1 $`L2 $`L3 $`L4` after4` after3` after2", scanner.tokens.get(4).lexeme);
        assertEquals("after1", scanner.tokens.get(7).lexeme);
        assertEquals(Type.TokenType.EOF, scanner.tokens.get(8).type);
    }

    @Test
    void quadrupleNestedWithTextAfterEveryLevel() {
        Scanner scanner = new Scanner("echo $`A $`B $`C $`D $`E` d` c` b` a` tail");
        scanner.scanAll();

        assertEquals("A $`B $`C $`D $`E` d` c` b` a", scanner.tokens.get(4).lexeme);
        assertEquals("tail", scanner.tokens.get(7).lexeme);
        assertEquals(Type.TokenType.EOF, scanner.tokens.get(8).type);
    }

    @Test
    void unclosedTripleNestedBacktickShouldThrow() {
        assertThrows(RuntimeException.class, () -> {
            Scanner scanner = new Scanner("echo $`a $`b $`c` d`");
            scanner.scanAll();
        });
    }

    @Test
    void unclosedQuadrupleNestedBacktickShouldThrow() {
        assertThrows(RuntimeException.class, () -> {
            Scanner scanner = new Scanner("echo $`a $`b $`c $`d` e` f`");
            scanner.scanAll();
        });
    }
}
