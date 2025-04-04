# Logging Utils

Logging Utils uses a single, static logger to log messages to be used primarily by top-level CLIs/programs.

## Features

- A single, static logger wrapped around `System.out` and `System.err`.
- The ability to add indentations using `#push` and `#pop`.
- A simple level system using `Log.Level`.
- Delegate to a `PrintStream` object (for things like `Throwable#printStackTrace`) using `#getLog` or similar.
  - If a level is not being logged (i.e. lower than `Log.enabled`), an empty print stream is returned that will ignore all method calls.
  - Each level's `PrintStream` also exists as a public static final field, such as `Log.WARN`.
- Java 5.
- No config files, no managers, no bullshit.
