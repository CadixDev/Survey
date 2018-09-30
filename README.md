Survey
======

Survey is a binary transformation and -remapping framework for Java, built on [ASM] and
[Lorenz].

## Features

Survey is a powerful, flexible tool that can be used in a variety of workflows - both
programmatic (through Survey's simple API), and through the CLI.

- Apply de-obfuscations mappings to a jar file.
  - Specific focus on producing *valid*, *runnable* output
    - Directory entries will be created for new directories, and obsolete ones will be
      removed (this resolves an issue with running programs using log4j2)
    - The MANIFEST will be remapped, specifically the `Main-Class` attribute
    - Service Provider Configurations will be remapped
    
Survey is also designed to be highly safe, and is backed by unit tests covering vast amounts
of its functionality (the majority of these tests are in the larger upstream projects -
[Lorenz] and [Bombe]).

## License

Survey is made available under the Mozilla Public License 2.0, you can find a copy on this
repository: [LICENSE.txt](LICENSE.txt).

[ASM]: http://asm.ow2.org/
[Lorenz]: https://github.com/CadixDev/Lorenz
[Bombe]: https://github.com/CadixDev/Bombe
