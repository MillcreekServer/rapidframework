package io.github.wysohn.rapidframework2.core.manager.command;

import io.github.wysohn.rapidframework2.core.interfaces.entity.ICommandSender;
import io.github.wysohn.rapidframework2.core.main.PluginMain;
import io.github.wysohn.rapidframework2.core.manager.lang.DynamicLang;
import io.github.wysohn.rapidframework2.core.manager.lang.Lang;
import io.github.wysohn.rapidframework2.core.manager.lang.PreParseHandle;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class SubCommand<Sender extends ICommandSender> {
    final PluginMain main;
    final String name;
    final int nArguments;

    String[] aliases = new String[0];
    String permission;
    DynamicLang description;
    List<DynamicLang> usage = new ArrayList<>();
    private CommandAction<Sender> action;
    private List<ArgumentMapper> argumentMappers = new ArrayList<>();

    public SubCommand(PluginMain main, String name, int nArguments) {
        this.main = main;
        this.name = name;
        this.nArguments = nArguments;
    }

    private SubCommand(PluginMain main, String name) {
        this(main, name, -1);
    }

    public boolean execute(Sender sender, String commandLabel, String[] args) {
        if (nArguments != -1 && args.length != nArguments)
            return false;

        Arguments argsObj = new Arguments(sender, args);

        return action.execute(sender, argsObj);
    }

    public static class Builder {
        private SubCommand command;

        private Builder(PluginMain main, String cmd, int numArgs) {
            command = new SubCommand(main, cmd, numArgs);
            command.permission = main.getAdminPermission() + "." + cmd;
        }

        public static Builder forCommand(String cmd, PluginMain main) {
            return new Builder(main, cmd, 0);
        }

        public static Builder forCommand(String cmd, PluginMain base, int numArgs) {
            return new Builder(base, cmd, numArgs);
        }

        public Builder withAlias(String... alias) {
            command.aliases = alias;
            return this;
        }

        public Builder withPermission(String permission) {
            command.permission = permission;
            return this;
        }

        public Builder withDescription(Lang description) {
            return withDescription(description, (managerLanguage) -> {
            });
        }

        public Builder withDescription(Lang description, PreParseHandle handle) {
            command.description = new DynamicLang(description, handle);
            return this;
        }

        /**
         * ${command} is built-in placeholder for 'this command' without slash(/)
         *
         * @param usage
         * @return
         */
        public Builder addUsage(Lang usage) {
            return addUsage(usage, (managerLanguage -> {
            }));
        }

        /**
         * ${command} is built-in placeholder for 'this command' without slash(/)
         *
         * @param lang
         * @return
         */
        public Builder addUsage(Lang lang, PreParseHandle handle) {
            command.usage.add(new DynamicLang(lang, handle));
            return this;
        }

        public Builder action(CommandAction<? extends ICommandSender> action) {
            command.action = action;
            return this;
        }

        public Builder addArgumentMapper(int index, ArgumentMapper mapper) {
            if (mapper == null)
                throw new RuntimeException(
                        "Cannot use null for mapper! Use ArgumentMapper.IDENTITY if mapping is not required.");

            while (command.argumentMappers.size() <= index)
                command.argumentMappers.add(ArgumentMapper.IDENTITY);
            command.argumentMappers.set(index, mapper);

            return this;
        }

        public SubCommand create() {
            return command;
        }
    }

    public class Arguments implements Iterable<String> {
        private Sender sender;
        private String[] args;

        public Arguments(Sender sender, String[] args) {
            super();
            this.sender = sender;
            this.args = args;
        }

        public int size() {
            return args.length;
        }

        /**
         * Try to get argument at the index. If index is out of range, the provided
         * default value will be used. You may add ArgumentMapper when building the
         * command to automatically convert the input to appropriate value (e.g.
         * argument to integer). If the ArgumentMapper cannot convert the argument for
         * some reason (like trying to convert non-number string to an integer), it will
         * automatically show error message to the user.
         *
         * @param index index of argument
         * @param def   the value to be used if index is out of range
         * @return the argument; Empty Optional if conversion fails. If Empty Optional was
         * returned, the error message is already sent to the sender.
         */
        @SuppressWarnings("unchecked")
        public <T> Optional<T> get(int index, T def) {
            try {
                if (index >= args.length)
                    return Optional.of(def);

                if (index < argumentMappers.size())
                    return Optional.of((T) argumentMappers.get(index).apply(args[index]));
                else
                    return Optional.of((T) ArgumentMapper.IDENTITY.apply(args[index]));
            } catch (InvalidArgumentException e) {
                main.lang().addString(args[index]);
                main.lang().sendMessage(sender, e.lang);
            }

            return Optional.empty();
        }

        /**
         * get all arguments starting from 'index' to the end as a string
         *
         * @param index
         * @return null if index is out of bound; string otherwise
         */
        public String getAsString(int index) {
            return getAsString(index, args.length - 1);
        }

        /**
         * get all arguments starting from 'index' to the 'end'
         *
         * @param index inclusive
         * @param end   inclusive
         * @return null if index is out of bound; string otherwise
         */
        public String getAsString(int index, int end) {
            if (index > args.length - 1)
                return null;

            StringBuilder builder = new StringBuilder(args[index]);
            for (int i = index + 1; i <= end; i++) {
                builder.append(" " + args[i]);
            }
            return builder.toString();

        }

        @Override
        public Iterator<String> iterator() {
            return new ArgumentIterator();
        }

        private class ArgumentIterator implements Iterator<String> {
            private int index = -1;

            @Override
            public boolean hasNext() {
                return index + 1 < args.length;
            }

            @Override
            public String next() {
                return args[++index];
            }
        }
    }
}
