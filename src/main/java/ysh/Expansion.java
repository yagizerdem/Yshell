package ysh;

import ysharp.treewalk.YsharpException;

import java.util.Arrays;
import java.util.List;

public class Expansion {

    public void VariableSubstitution(Type.Command command) {
        class VariableSubstitutionVisitor implements Visitor<Void> {
            @Override
            public Void visitConditionalNode(Type.ConditionalNode node) {
                throw new YsharpException(YsharpException.YsharpErrorType.SEMANTIC, -1, "Variable expansion only works in word type nodes");
            }

            @Override
            public Void visitPipelineNode(Type.PipelineNode node) {
                throw new YsharpException(YsharpException.YsharpErrorType.SEMANTIC, -1, "Variable expansion only works in word type nodes");
            }

            @Override
            public Void visitCommandNode(Type.CommandNode node) {
                throw new YsharpException(YsharpException.YsharpErrorType.SEMANTIC, -1, "Variable expansion only works in word type nodes");
            }

            @Override
            public Void visitGroupedCommandNode(Type.GroupedCommandNode node) {
                throw new YsharpException(YsharpException.YsharpErrorType.SEMANTIC, -1, "Variable expansion only works in word type nodes");
            }

            @Override
            public Void visitWord(Type.Word node) {
                for(Type.WordPart part : node.parts) {
                    part.accept(this);
                }
                return null;
            }

            @Override
            public Void visitRedirection(Type.Redirection node) {
                throw new YsharpException(YsharpException.YsharpErrorType.SEMANTIC, -1, "Variable expansion only works in word type nodes");
            }

            @Override
            public Void visitUnquotedWord(Type.UnquotedWord node) {
                return null;
            }

            @Override
            public Void visitSinglequotedWord(Type.SinglequotedWord node) {
                return null;
            }

            @Override
            public Void visitDoublequotedWord(Type.DoublequotedWord node) {
                for(Type.WordPart part : node.wordParts) {
                    part.accept(this);
                }
                return null;
            }

            @Override
            public Void visitShellCommandWord(Type.ShellCommandWord node) {
                // shell command does not expand, it has own expansion system
                return null;
            }

            @Override
            public Void visitVariableWord(Type.VariableWord node) {
                Context context = Context.getContext();
                String variable = context.env.getVariableOrDefault(node.word.lexeme, "");
                // expand
                node.word.lexeme = variable;
                return null;
            }
        }


        VariableSubstitutionVisitor visitor = new VariableSubstitutionVisitor();
        for(Type.Word word : command.getRawArgs()) {
            visitor.visitWord(word);
        }

        for(Type.Redirection redirection : command.getRawRedirections()) {
            visitor.visitWord(redirection.filename);
        }
    }

    public void TildeSubstitution(Type.Command command) {
        class VariableSubstitutionVisitor implements Visitor<Void> {
            @Override
            public Void visitConditionalNode(Type.ConditionalNode node) {
                throw new YsharpException(YsharpException.YsharpErrorType.SEMANTIC, -1, "Tilde expansion only works in word type nodes");
            }

            @Override
            public Void visitPipelineNode(Type.PipelineNode node) {
                throw new YsharpException(YsharpException.YsharpErrorType.SEMANTIC, -1, "Tilde expansion only works in word type nodes");
            }

            @Override
            public Void visitCommandNode(Type.CommandNode node) {
                throw new YsharpException(YsharpException.YsharpErrorType.SEMANTIC, -1, "Tilde expansion only works in word type nodes");
            }

            @Override
            public Void visitGroupedCommandNode(Type.GroupedCommandNode node) {
                throw new YsharpException(YsharpException.YsharpErrorType.SEMANTIC, -1, "Tilde expansion only works in word type nodes");
            }

            @Override
            public Void visitWord(Type.Word node) {
                if(node.hasTildeExpansion) {
                    String homeDir = System.getProperty("user.home");
                    Type.Token token = new Type.Token(homeDir, Type.TokenType.TEXT);
                    Type.UnquotedWord word = new Type.UnquotedWord(token);
                    node.parts.add(0, word);
                }
                return null;
            }

            @Override
            public Void visitRedirection(Type.Redirection node) {
                throw new YsharpException(YsharpException.YsharpErrorType.SEMANTIC, -1, "Tilde expansion only works in word type nodes");
            }

            @Override
            public Void visitUnquotedWord(Type.UnquotedWord node) {
                return null;
            }

            @Override
            public Void visitSinglequotedWord(Type.SinglequotedWord node) {
                return null;
            }

            @Override
            public Void visitDoublequotedWord(Type.DoublequotedWord node) {
                return null;
            }

            @Override
            public Void visitShellCommandWord(Type.ShellCommandWord node) {
                // shell command does not expand, it has own expansion system
                return null;
            }

            @Override
            public Void visitVariableWord(Type.VariableWord node) {
                // since tilde expansion is before var expansion, even variable has tilde char it does not affect
                return null;
            }
        }


        VariableSubstitutionVisitor visitor = new VariableSubstitutionVisitor();
        for(Type.Word word : command.getRawArgs()) {
            visitor.visitWord(word);
        }

        for(Type.Redirection redirection : command.getRawRedirections()) {
            visitor.visitWord(redirection.filename);
        }
    }

    public void  CommandSubstitution(Type.Command command) {
        class CommandSubstitutionVisitor implements Visitor<Void> {
            @Override
            public Void visitConditionalNode(Type.ConditionalNode node) {
                throw new YsharpException(YsharpException.YsharpErrorType.SEMANTIC, -1, "Command expansion only works in word type nodes");
            }

            @Override
            public Void visitPipelineNode(Type.PipelineNode node) {
                throw new YsharpException(YsharpException.YsharpErrorType.SEMANTIC, -1, "Command expansion only works in word type nodes");
            }

            @Override
            public Void visitCommandNode(Type.CommandNode node) {
                throw new YsharpException(YsharpException.YsharpErrorType.SEMANTIC, -1, "Command expansion only works in word type nodes");
            }

            @Override
            public Void visitGroupedCommandNode(Type.GroupedCommandNode node) {
                throw new YsharpException(YsharpException.YsharpErrorType.SEMANTIC, -1, "Command expansion only works in word type nodes");
            }

            @Override
            public Void visitWord(Type.Word node) {
                for(Type.WordPart part : node.parts) {
                    part.accept(this);
                }
                return null;
            }

            @Override
            public Void visitRedirection(Type.Redirection node) {
                throw new YsharpException(YsharpException.YsharpErrorType.SEMANTIC, -1, "Command expansion only works in word type nodes");
            }

            @Override
            public Void visitUnquotedWord(Type.UnquotedWord node) {
                return null;
            }

            @Override
            public Void visitSinglequotedWord(Type.SinglequotedWord node) {
                return null;
            }

            @Override
            public Void visitDoublequotedWord(Type.DoublequotedWord node) {
                return null;
            }

            @Override
            public Void visitShellCommandWord(Type.ShellCommandWord node) {
                Context parent = Context.getContext();
                Context ctx = Context.getScoped();
                ctx.settings.captureStdout = true;
                ctx.settings.captureStderr = true;
                Context.active = ctx;
                Type.ProgramExecutionResponse response = Core.ExecuteShellProgram(node.word.lexeme);
                Context.active = parent;
                if(response.stdOut != null && !response.stdOut.isEmpty()) {
                    node.word.lexeme = response.stdOut;
                }else {
                    node.word.lexeme = "";
                }
                return null;
            }

            @Override
            public Void visitVariableWord(Type.VariableWord node) {
                return null;
            }
        }


        CommandSubstitutionVisitor visitor = new CommandSubstitutionVisitor();
        for(Type.Word word : command.getRawArgs()) {
            visitor.visitWord(word);
        }

        for(Type.Redirection redirection : command.getRawRedirections()) {
            visitor.visitWord(redirection.filename);
        }
    }

    public void GlobSubstitution(Type.Command command) {
        class GlobSubstitutionVisitor implements Visitor<Void> {
            public boolean hasGlobMetaChar = false;

            List<Character> globMetaChars = List.of(
                    '*',
                    '?',
                    '[',
                    '{',
                    '\\'
            );
            @Override
            public Void visitConditionalNode(Type.ConditionalNode node) {
                throw new YsharpException(YsharpException.YsharpErrorType.SEMANTIC, -1, "Glob expansion only works in word type nodes");
            }

            @Override
            public Void visitPipelineNode(Type.PipelineNode node) {
                throw new YsharpException(YsharpException.YsharpErrorType.SEMANTIC, -1, "Glob expansion only works in word type nodes");
            }

            @Override
            public Void visitCommandNode(Type.CommandNode node) {
                throw new YsharpException(YsharpException.YsharpErrorType.SEMANTIC, -1, "Glob expansion only works in word type nodes");
            }

            @Override
            public Void visitGroupedCommandNode(Type.GroupedCommandNode node) {
                throw new YsharpException(YsharpException.YsharpErrorType.SEMANTIC, -1, "Glob expansion only works in word type nodes");
            }

            @Override
            public Void visitWord(Type.Word node) {
                for(Type.WordPart part : node.parts) {
                    part.accept(this);
                }

                if(hasGlobMetaChar) {
                    node.hasGlobExpansion = true;
                    WordAssemblerVisitor resolver = new WordAssemblerVisitor();
                    String globPattern = node.accept(resolver);
                    List<String> paths = Globber.expandGlob(globPattern);
                    node.globExpansionResult = paths;
                }
                return null;
            }

            @Override
            public Void visitRedirection(Type.Redirection node) {
                throw new YsharpException(YsharpException.YsharpErrorType.SEMANTIC, -1, "Glob expansion only works in word type nodes");
            }

            @Override
            public Void visitUnquotedWord(Type.UnquotedWord node) {
                for(Type.Pchar pChar : node.word.rawLexeme) {
                    if(!pChar.isEscaped && globMetaChars.contains(pChar.c)) {
                        hasGlobMetaChar = true;
                        return null;
                    }
                }
                return null;
            }

            @Override
            public Void visitSinglequotedWord(Type.SinglequotedWord node) {
                return null;
            }

            @Override
            public Void visitDoublequotedWord(Type.DoublequotedWord node) {
                return null;
            }

            @Override
            public Void visitShellCommandWord(Type.ShellCommandWord node) { return null; }

            @Override
            public Void visitVariableWord(Type.VariableWord node) { return null; }
        }


        GlobSubstitutionVisitor visitor = new GlobSubstitutionVisitor();
        for(Type.Word word : command.getRawArgs()) {
            visitor.visitWord(word);
            // reset flag
            visitor.hasGlobMetaChar = false;
        }

        for(Type.Redirection redirection : command.getRawRedirections()) {
            visitor.visitWord(redirection.filename);
            // reset flag
            visitor.hasGlobMetaChar = false;
        }
    }

    // util
    public static  final class WordAssemblerVisitor implements Visitor<String> {
        @Override
        public String  visitConditionalNode(Type.ConditionalNode node) {
            throw new YsharpException(YsharpException.YsharpErrorType.SEMANTIC, -1, "WordAssembler expansion only works in word type nodes");
        }

        @Override
        public String visitPipelineNode(Type.PipelineNode node) {
            throw new YsharpException(YsharpException.YsharpErrorType.SEMANTIC, -1, "WordAssembler expansion only works in word type nodes");
        }

        @Override
        public String visitCommandNode(Type.CommandNode node) {
            throw new YsharpException(YsharpException.YsharpErrorType.SEMANTIC, -1, "WordAssembler expansion only works in word type nodes");
        }

        @Override
        public String visitGroupedCommandNode(Type.GroupedCommandNode node) {
            throw new YsharpException(YsharpException.YsharpErrorType.SEMANTIC, -1, "WordAssembler expansion only works in word type nodes");
        }

        @Override
        public String visitWord(Type.Word node) {
            StringBuilder builder = new StringBuilder();
            for(Type.WordPart part : node.parts) {
                builder.append(part.accept(this));
            }
            return builder.toString();
        }

        @Override
        public String visitRedirection(Type.Redirection node) {
            throw new YsharpException(YsharpException.YsharpErrorType.SEMANTIC, -1, "WordAssembler expansion only works in word type nodes");
        }

        @Override
        public String visitUnquotedWord(Type.UnquotedWord node) {
            return node.word.lexeme;
        }

        @Override
        public String visitSinglequotedWord(Type.SinglequotedWord node) {
            return node.word.lexeme;
        }

        @Override
        public String visitDoublequotedWord(Type.DoublequotedWord node) {
            StringBuilder joinedParts = new StringBuilder();
            for(Type.WordPart part : node.wordParts) {
                joinedParts.append(part.accept(this));
            }
            return joinedParts.toString();
        }

        @Override
        public String  visitShellCommandWord(Type.ShellCommandWord node) {
            return node.word.lexeme;
        }

        @Override
        public String visitVariableWord(Type.VariableWord node) {
            return node.word.lexeme;
        }
    }
}
