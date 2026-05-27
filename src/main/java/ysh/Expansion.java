package ysh;

import ysharp.treewalk.YsharpException;

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
        for(Type.Word word : command.rawArgs) {
            visitor.visitWord(word);
        }

        for(Type.Redirection redirection : command.redirections) {
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
        for(Type.Word word : command.rawArgs) {
            visitor.visitWord(word);
        }

        for(Type.Redirection redirection : command.redirections) {
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
                Type.CommandExecutionOptions options = Type.CommandExecutionOptions.capture();
//               Core.ExecuteShellProgram(node.word.lexeme, options);
                return null;
            }

            @Override
            public Void visitVariableWord(Type.VariableWord node) {
                return null;
            }
        }


        CommandSubstitutionVisitor visitor = new CommandSubstitutionVisitor();
        for(Type.Word word : command.rawArgs) {
            visitor.visitWord(word);
        }

        for(Type.Redirection redirection : command.redirections) {
            visitor.visitWord(redirection.filename);
        }
    }
}
