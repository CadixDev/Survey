Survey
======

Survey is a minimalistic jar remapper, built on [ASM] and [Lorenz]. It is designed in such
a way that the ClassRemapper component could easily be integrated into another workflow.

## CLI

Survey comes with a CLI for remapping jar files, with SRG mappings (in the future, more
formats will be available).

> java -jar survey.jar --jarIn obf.jar --mappings mappings.srg --jarOut deobf.jar


## Usage

Survey is available through my Maven repository (repo.jamiemansfield.me).

```gradle
repositories {
    mavenCentral()
    maven {
        name = 'jamiemansfield'
        url = 'https://repo.jamiemansfield.me/'
    }
}

dependencies {
    compile 'me.jamiemansfield:survey:0.0.1-SNAPSHOT'
}
```

## License

Survey is BSD 3-Clause licensed.

[ASM]: http://asm.ow2.org/
[Lorenz]: https://github.com/jamiemansfield/Lorenz
