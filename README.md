Survey
======

Survey is a minimalistic jar remapper, built on [ASM] and [Lorenz]. It is designed in such
a way that the ClassRemapper component could easily be integrated into another workflow.

## CLI

Survey comes with a CLI for remapping jar files, with SRG mappings (in the future, more
formats will be available).

> java -jar survey.jar --remap --jar-in obf.jar --mappings mappings.srg --jar-out deobf.jar

## License

Survey is BSD 3-Clause licensed.

[ASM]: http://asm.ow2.org/
[Lorenz]: https://github.com/jamiemansfield/Lorenz
