package io.github.wysohn.rapidframework3.interfaces.language;

import java.util.Locale;
import java.util.Set;

public interface ILangSessionFactory {
    Set<Locale> getLocales();

    ILangSession create(Locale locale);
}
