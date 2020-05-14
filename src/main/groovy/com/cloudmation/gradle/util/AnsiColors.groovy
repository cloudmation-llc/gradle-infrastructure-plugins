package com.cloudmation.gradle.util

import groovy.transform.CompileStatic

/**
 * ANSI escape codes for colorizing log messages
 * @link https://www.lihaoyi.com/post/BuildyourownCommandLinewithANSIescapecodes.html
 */
@CompileStatic
class AnsiColors {

    private static final String COLOR_GREEN = "\u001b[32;1m";
    private static final String COLOR_ORANGE = "\u001b[38;5;208m";
    private static final String COLOR_YELLOW = "\u001b[33;1m";
    private static final String UTIL_RESET = "\u001b[0m";

    static String green(String input) {
        return COLOR_GREEN + input + UTIL_RESET
    }

    static String yellow(String input) {
        return COLOR_YELLOW + input + UTIL_RESET
    }

}
