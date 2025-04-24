# Logging Utils

Logging Utils uses a single, static logger to log messages to be used primarily by top-level CLIs/programs.

**This is designed to be used as a replacement for `System.out` logging.** If you need a robust logging framework to be
used in complex environments, consider SLF4J or Java's own Logging implementation.

## Features

- A single, static logger wrapped around `System.out` and `System.err`.
- The ability to add indentations using `#push` and `#pop`.
- A simple level system using `Log.Level`.
- The ability to capture log messages and prevent them from printing on the screen.
  - These captures can then either be released and all printed at once (in order), or dropped and never printed.
- Delegate to a `PrintStream` object (for things like `Throwable#printStackTrace`) using `#getLog` or similar.
  - If a level is not being logged (i.e. lower than `Log.enabled`), an empty print stream is returned that will ignore all method calls.
  - Each level's `PrintStream` also exists as a public static final field, such as `Log.WARN`.
- No config files, no managers, no bullshit.
