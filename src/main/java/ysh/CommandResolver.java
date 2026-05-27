package ysh;

import ysharp.treewalk.YsharpException;

import java.util.ArrayList;
import java.util.List;

public class CommandResolver {

    private final static List<String> builtinRegistry = new ArrayList<>();

    static {
        builtinRegistry.add("cd");
        builtinRegistry.add("chdir");
        builtinRegistry.add("echo");
        builtinRegistry.add("exit");
        builtinRegistry.add("dir");
        builtinRegistry.add("ls");
        builtinRegistry.add("set");
        builtinRegistry.add("mkdir");
    }

    public void ResolveArgs(Type.Command command)   {
        class NormalizeWordsVisitor implements Visitor<Void> {

            public List<String> args = new ArrayList<>();

            private StringBuilder builder = new StringBuilder();

            private boolean isSpace(char c) {
                return  c == ' ' || c == '\t' || c == '\n';
            }

            private boolean isQuoted = false;

            @Override
            public Void visitConditionalNode(Type.ConditionalNode node) {
                throw new YsharpException(YsharpException.YsharpErrorType.SEMANTIC, -1, "Normalize " +
                        "words only works in word type nodes");
            }

            @Override
            public Void visitPipelineNode(Type.PipelineNode node) {
                throw new YsharpException(YsharpException.YsharpErrorType.SEMANTIC, -1, "Normalize " +
                        "words only works in word type nodes");
            }

            @Override
            public Void visitCommandNode(Type.CommandNode node) {
                throw new YsharpException(YsharpException.YsharpErrorType.SEMANTIC, -1, "Normalize " +
                        "words only works in word type nodes");
            }

            @Override
            public Void visitGroupedCommandNode(Type.GroupedCommandNode node) {
                throw new YsharpException(YsharpException.YsharpErrorType.SEMANTIC, -1, "Normalize " +
                        "words only works in word type nodes");
            }

            @Override
            public Void visitWord(Type.Word node) {

                if(node.hasGlobExpansion) {
                    node.globExpansionResult.forEach(path -> {
                        args.add(path);
                    });
                    return null;
                }

                isQuoted = false;
                for(Type.WordPart part : node.parts) {
                    part.accept(this);
                    isQuoted = false;
                }
                if(!builder.isEmpty()) {
                    args.add(builder.toString());
                }
                return null;
            }

            @Override
            public Void visitRedirection(Type.Redirection node) {
                throw new YsharpException(YsharpException.YsharpErrorType.SEMANTIC, -1, "Command expansion " +
                        "only works in word type nodes");

            }

            @Override
            public Void visitUnquotedWord(Type.UnquotedWord node) {
                for(int i = 0; i < node.word.lexeme.length(); i++) {
                    if(isSpace(node.word.lexeme.charAt(i)) && !isQuoted) {
                        args.add(builder.toString());
                        builder.setLength(0);
                    }
                    else {
                        builder.append(node.word.lexeme.charAt(i));
                    }
                }
                return null;
            }

            @Override
            public Void visitSinglequotedWord(Type.SinglequotedWord node) {
                isQuoted = true;
                for(int i = 0; i < node.word.lexeme.length(); i++) {
                    builder.append(node.word.lexeme.charAt(i));
                }
                isQuoted = false;
                return null;
            }

            @Override
            public Void visitDoublequotedWord(Type.DoublequotedWord node) {
                isQuoted = true;
                for(Type.WordPart part : node.wordParts) {
                    part.accept(this);
                }
                isQuoted = false;
                return null;
            }

            @Override
            public Void visitShellCommandWord(Type.ShellCommandWord node) {
                isQuoted = true;
                for(int i = 0; i < node.word.lexeme.length(); i++) {
                    builder.append(node.word.lexeme.charAt(i));
                }
                isQuoted = false;
                return null;
            }

            @Override
            public Void visitVariableWord(Type.VariableWord node) {
                for(int i = 0; i < node.word.lexeme.length(); i++) {
                    if(isSpace(node.word.lexeme.charAt(i)) && !isQuoted) {
                        args.add(builder.toString());
                        builder.setLength(0);
                    }
                    else {
                        builder.append(node.word.lexeme.charAt(i));
                    }
                }
                return null;
            }
        }


        for(int i = 0; i < command.rawArgs.size(); i++) {
            Type.Word word = command.rawArgs.get(i);
            NormalizeWordsVisitor normalize = new NormalizeWordsVisitor();
            normalize.visitWord(word);
            List<String> args = normalize.args;
            command.args.addAll(args);
        }
    }

    public void ResolveIsBuiltIn(Type.Command command) {
        if(!command.args.isEmpty()) {
            if(CommandResolver.builtinRegistry.contains(command.args.getFirst())) {
                command.isBuiltIn = true;
            }
        }
    }

    public void Resolve(Type.Command command) {
        this.ResolveArgs(command);
        this.ResolveIsBuiltIn(command);
    }


}
