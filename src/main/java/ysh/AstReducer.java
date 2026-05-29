package ysh;

import java.util.ArrayList;
import java.util.List;

public class AstReducer  {

    public static class Response {
        public Type.BaseCommand command = null;
        public Type.Word word = null;
        public Type.Redirection redirection = null;
    }


    public static final class AstReducerVisitor  implements Visitor<Response> {

        public List<Type.BaseCommand> vectorizedCommands;

        public List<Type.AstNode> nodes;

        public AstReducerVisitor() {
            this.vectorizedCommands = new ArrayList<>();
        }

        public AstReducerVisitor(List<Type.AstNode> nodes) {
            this.vectorizedCommands = new ArrayList<>();
            this.nodes = nodes;
        }

        public void vectorizeAll() {
            for(Type.AstNode node : nodes) {
                this.vectorizedCommands.add(node.accept(this).command);
            }
        }

        @Override
        public Response visitConditionalNode(Type.ConditionalNode node) {
            Response result = new Response();

            Response pipeline = node.first.accept(this);

            if(node.rest != null && !node.rest.isEmpty()) {
                Type.ConditionalCommand conditionalCommand = new Type.ConditionalCommand();
                conditionalCommand.command = pipeline.command;
                Type.ConditionalCommand cur = conditionalCommand;

                for(Type.ConditionalNode.ConditionalPart conditionalPart : node.rest) {
                    Type.Token op = conditionalPart.operator;
                    cur.operator = op;

                    Response pipeline_ =  conditionalPart.pipeline.accept(this);

                    Type.ConditionalCommand conditionalCommand_ = new Type.ConditionalCommand();
                    conditionalCommand_.command = pipeline_.command;
                    cur.chainCommand = conditionalCommand_;
                    cur = conditionalCommand_;
                }

                result.command = conditionalCommand;
            } else {
                result.command = pipeline.command;
            }

            return result;
        }

        @Override
        public Response visitPipelineNode(Type.PipelineNode node) {
            Response command = node.first.accept(this);

            Response result = new Response();

            if(node.rest != null && !node.rest.isEmpty()) {
                Type.Pipe pipeCommand = new Type.Pipe();
                List<Type.BaseCommand> commands = new ArrayList<>();
                commands.add(command.command);
                for(Type.PipelineNode.PipelinePart part : node.rest) {
                    commands.add(part.command.accept(this).command);
                }
                pipeCommand.commands = commands;
                result.command = pipeCommand;

            }else {
                result.command = command.command;
            }

            return result;
        }

        @Override
        public Response visitCommandNode(Type.CommandNode node) {
            Type.Command command = new Type.Command();
            Response result = new Response();

            for(Type.AstNode val : node.commandElements) {
                Response response = val.accept(this);
                if(response.word != null) {
                    command.rawCommandElements.add(response.word);
                }
                else if(response.redirection != null) {
                    command.rawCommandElements.add(response.redirection);
                }
            }
            result.command = command;
            return result;
        }

        @Override
        public Response visitGroupedCommandNode(Type.GroupedCommandNode node) {
            Type.GroupedCommand groupedCommand = new Type.GroupedCommand();
            Response result = new Response();
            if(node.redirections != null) {
                for(Type.AstNode redirection : node.redirections) {
                    groupedCommand.redirections.add(redirection.accept(this).redirection);
                }
            }
            if(node.list != null && !node.list.isEmpty()) {
                for(Type.AstNode commands : node.list) {
                    groupedCommand.commands.add(commands.accept(this).command);
                }
            }
            result.command = groupedCommand;
            return result;
        }

        @Override
        public Response visitWord(Type.Word node) {
            Response response = new Response();
            response.word = node;
            return response;
        }

//        @Override
//        public Response visitWordBreak(Type.WordBreak node) {
//            return null;
//        }

        @Override
        public Response visitRedirection(Type.Redirection node) {
            Response response = new Response();
            response.redirection = node;
            return response;
        }

        @Override
        public Response visitUnquotedWord(Type.UnquotedWord node) {
            return null;
        }

        @Override
        public Response visitSinglequotedWord(Type.SinglequotedWord node) {
            return null;
        }

        @Override
        public Response visitDoublequotedWord(Type.DoublequotedWord node) {
            return null;
        }

        @Override
        public Response visitShellCommandWord(Type.ShellCommandWord node) {
            return null;
        }

        @Override
        public Response visitVariableWord(Type.VariableWord node) {
            return null;
        }
    }

}