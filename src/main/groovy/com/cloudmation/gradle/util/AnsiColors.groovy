/**
 * Copyright 2020 Cloudmation LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
