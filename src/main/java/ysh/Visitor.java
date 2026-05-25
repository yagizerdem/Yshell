package ysh;

public interface Visitor<R> {
    R visitConditionalNode(Type.ConditionalNode node);
    R visitPipelineNode(Type.PipelineNode node);
    R visitCommandNode(Type.CommandNode node);
    R visitGroupedCommandNode(Type.GroupedCommandNode node);

    R visitWord(Type.Word node);
    // R visitWordBreak(Type.WordBreak node);
    R visitRedirection(Type.Redirection node);

    R visitUnquotedWord(Type.UnquotedWord node);
    R visitSinglequotedWord(Type.SinglequotedWord node);
    R visitDoublequotedWord(Type.DoublequotedWord node);
    R visitShellCommandWord(Type.ShellCommandWord node);
    R visitVariableWord(Type.VariableWord node);
}