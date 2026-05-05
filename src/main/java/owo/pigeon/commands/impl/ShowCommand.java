package owo.pigeon.commands.impl;

import owo.pigeon.commands.Command;
import owo.pigeon.modules.Module;
import owo.pigeon.utils.CommandUtil;
import owo.pigeon.utils.ModuleUtil;
import owo.pigeon.utils.chat.ChatUtil;

public class ShowCommand extends Command {
    public ShowCommand() {
        super("show");
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

        String moduleName = args[0];

        if (!ModuleUtil.isModuleExist(moduleName)) {
            CommandUtil.sendCommandError(CommandUtil.errorReason.UnknownModule,
                    this.getCommand(),
                    args,
                    0
            );
            return;
        }

        boolean show = true;
        if (args.length > 1) {
            String boolStr = args[1].toLowerCase();
            if (!boolStr.equals("true") && !boolStr.equals("false")) {
                CommandUtil.sendCommandError(CommandUtil.errorReason.InvalidBoolean,
                        this.getCommand(),
                        args,
                        1
                );
                return;
            }
            show = Boolean.parseBoolean(boolStr);
        }

        Module module = ModuleUtil.getModule(moduleName);
        module.setHide(!show);

        ChatUtil.sendMessage(module.name + " is now " + (show ? "&avisible" : "&chidden"));
    }

    @Override
    public String getUsage() {
        return CommandUtil.getCommandPrefix() + "show <module> [(true|false)]";
    }
}
