/*
 * Copyright (c) CovertJaguar, 2015 http://railcraft.info
 *
 * This code is the property of CovertJaguar
 * and may only be used with explicit written
 * permission unless otherwise specified on the
 * license page at http://railcraft.info/wiki/info:license.
 */
package mods.railcraft.common.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * This is the command that will be registered with Minecraft, it will delegate execution to our sub-commands.
 *
 * Created by CovertJaguar on 3/12/2015.
 */
public class RootCommand extends CommandBase implements IModCommand {
    public static final String ROOT_COMMAND_NAME = "railcraft";
    private final SortedSet<SubCommand> children = new TreeSet<>(SubCommand::compareTo);

    public void addChildCommand(SubCommand child) {
        child.setParent(this);
        children.add(child);
    }

    @Override
    public SortedSet<SubCommand> getChildren() {
        return children;
    }

    @Override
    public String getCommandName() {
        return ROOT_COMMAND_NAME;
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public List<String> getCommandAliases() {
        List<String> aliases = new ArrayList<>();
        aliases.add("rc");
        aliases.add("rail");
        return aliases;
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/" + getCommandName() + " help";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (!CommandHelpers.executeStandardCommands(server, sender, this, args))
            CommandHelpers.throwWrongUsage(sender, this);
    }

    @Override
    public String getFullCommandString() {
        return getCommandName();
    }

    @Override
    public void printHelp(ICommandSender sender) {
        CommandHelpers.printHelp(sender, this);
    }
}