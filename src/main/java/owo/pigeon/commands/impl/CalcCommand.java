package owo.pigeon.commands.impl;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import owo.pigeon.commands.Command;
import owo.pigeon.utils.CommandUtil;
import owo.pigeon.utils.chat.ChatUtil;


public class CalcCommand extends Command {
    public CalcCommand() {
        super("calc");
    }

    @Override
    public void execute(String[] args) {
        if (args.length == 0) {
            CommandUtil.sendCommandError(CommandUtil.errorReason.UnknownOrIncompleteCommand,
                    this.getCommand(),
                    args,
                    args.length
            );
            return;
        }
        String expr = String.join(" ", args).replaceAll(" ", "").replaceAll("x", "*");

        double answer = 0;

        try {
            Expression expression = new ExpressionBuilder(expr).build();
            answer = expression.evaluate();
        } catch (Exception e) {
            CommandUtil.sendCommandError(CommandUtil.errorReason.IncorrectArgument,
                    this.getCommand(),
                    args,
                    args.length
            );
            return;
        }

        String answerStr;
        if (Math.floor(answer) == answer) {
            answerStr = String.format("%.0f", answer);
        } else {
            answerStr = String.valueOf(answer);
        }

        String message = expr + " = " + answerStr;

        if (answer > 64 && Math.floor(answer) == answer) {
            long answerLong = (long) answer;

            long x = answerLong / 64;
            message += " = " + x + "*64";

            long y = answerLong % 64;
            if (y != 0) message += "+" + y;
        }

        ChatUtil.sendMessage(message);
    }

    @Override
    public String getUsage() {
        return CommandUtil.getCommandPrefix() + "calc <expression>";
    }
}
