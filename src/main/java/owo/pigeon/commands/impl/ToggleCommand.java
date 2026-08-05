package owo.pigeon.commands.impl;

import owo.pigeon.commands.Command;
import owo.pigeon.utils.CommandUtil;
import owo.pigeon.utils.ModuleUtil;

public class ToggleCommand extends Command {
    public ToggleCommand() {
        super("toggle");
    }

    @Override
    public void execute(String[] args) {
        if (args.length < 1) {
            CommandUtil.sendCommandError(CommandUtil.ErrorReason.UnknownOrIncompleteCommand,
                    this.getCommand(),
                    args,
                    args.length
            );
            return;
        }

        String module = args[0];

        if (ModuleUtil.isModuleExist(module)) {

            String action = args.length > 1 ? args[1].toLowerCase() : "";

            switch (action) {
                case "enable":
                    ModuleUtil.enableModule(module);
                    break;
                case "disable":
                    ModuleUtil.disableModule(module);
                    break;
                default:
                    ModuleUtil.toggleModule(module);
                    break;
            }

        } else {
            CommandUtil.sendCommandError(CommandUtil.ErrorReason.UnknownModule,
                    this.getCommand(),
                    args,
                    0
            );
        }
    }

    @Override
    public String getUsage() {
        return CommandUtil.getCommandPrefix() + "toggle <module> [(enable|disable)]";
    }
}
