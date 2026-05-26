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

    public void WordAssembler(Type.Command command)   {
        class VariableSubstitutionVisitor implements Visitor<String> {
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
                return null;
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
        VariableSubstitutionVisitor visitor = new VariableSubstitutionVisitor();

        for(int i = 0; i < command.rawArgs.size(); i++) {
            Type.Word word = command.rawArgs.get(i);
            command.args.add(word.accept(visitor));
        }
    }

    public void Resolve(Type.Command command) {
        this.WordAssembler(command);

        if(!command.args.isEmpty()) {
            if(CommandResolver.builtinRegistry.contains(command.args.getFirst())) {
                command.isBuiltIn = true;
            }
        }
    }
}
