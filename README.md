# saker.java.compiler

[![Build status](https://img.shields.io/azure-devops/build/sakerbuild/f1251dbc-3ee1-4e43-a095-0231770283de/14/master)](https://dev.azure.com/sakerbuild/saker.java.compiler/_build) [![Latest version](https://mirror.nest.saker.build/badges/saker.java.compiler/version.svg)](https://nest.saker.build/package/saker.java.compiler "saker.java.compiler | saker.nest")

Incremental Java compiler support for the [saker.build system](https://saker.build). The project contains the implementation of performing incremental Java compilation during builds with the saker.build system. The implementation performs deep analysis of the compiled source files and can perfectly determine the dependencies of each class, therefore providing minimal rebuilds every time.

The build task also support incremental annotation processing, which can also performed concurrently, further reducing build times.

See the [documentation](https://saker.build/saker.java.compiler/doc/) for more information.

## Build instructions

The project uses the [saker.build system](https://saker.build) for building. It requires multiple JDKs to be installed to successfully build the complete project. This ranges from JDK 8-14, but you can also selectively build for a given JDK using the other build tasks in the `saker.build` build script.

Use the following command to build the project:

```
java -jar path/to/saker.build.jar -bd build -EUsaker.java.jre.install.locations=path/to/jdk8;path/to/jdk9;... compile saker.build
```

## License

The source code for the project is licensed under *GNU General Public License v3.0 only*.

Short identifier: [`GPL-3.0-only`](https://spdx.org/licenses/GPL-3.0-only.html).

Official releases of the project (and parts of it) may be licensed under different terms. See the particular releases for more information.
