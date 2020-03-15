package io.github.wysohn.rapidframework2.bukkit.main.objects;

import io.github.wysohn.rapidframework2.core.interfaces.entity.ICommandSender;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.Locale;
import java.util.UUID;

public class BukkitCommandSender implements ICommandSender {
    private CommandSender sender;

    public ICommandSender setSender(CommandSender sender) {
        this.sender = sender;
        return this;
    }

    @Override
    public void sendMessageRaw(String... msg) {
        sender.sendMessage(msg);
    }

    @Override
    public Locale getLocale() {
        return Locale.ENGLISH;
    }

    @Override
    public boolean hasPermission(String... permissions) {
        return Arrays.stream(permissions).anyMatch(sender::hasPermission);
    }

    @Override
    public String getDisplayName() {
        return sender.getName();
    }

    @Override
    public UUID getUuid() {
        return null;
    }
}