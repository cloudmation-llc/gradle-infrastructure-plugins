package com.cloudmation.gradle.util

import groovy.transform.CompileStatic

/**
 * ANSI escape codes for colorizing log messages
 * @link https://www.lihaoyi.com/post/BuildyourownCommandLinewithANSIescapecodes.html
 */
@CompileStatic
class AnsiColors {

    private static final String COLOR_BLUE = "\u001b[34;1m";
    private static final String COLOR_GRAY = "\u001b[38;5;247m";
    private static final String COLOR_GREEN = "\u001b[32;1m";
    private static final String COLOR_ORANGE = "\u001b[38;5;208m";
    private static final String COLOR_RED = "\u001b[31;1m";
    private static final String COLOR_YELLOW = "\u001b[33;1m";
    private static final String UTIL_RESET = "\u001b[0m";

    static String blue(String input) {
        return COLOR_BLUE + input + UTIL_RESET
    }

    static String gray(String input) {
        return COLOR_GRAY + input + UTIL_RESET
    }

    static String green(String input) {
        return COLOR_GREEN + input + UTIL_RESET
    }

    static String red(String input) {
        return COLOR_RED + input + UTIL_RESET
    }

    static String yellow(String input) {
        return COLOR_YELLOW + input + UTIL_RESET
    }

}
