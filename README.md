# Command Array

A library of generating a command array compatible with **CommandRunner** from the module **xyz.ronella.casual.trivial**.

## Requires

* Java 11

## Usage

1. Add the following **maven** dependency to your project:

   | Property    | Value              |
   | ----------- | ------------------ |
   | Group ID    | xyz.ronella.casual |
   | Artifact ID | command-arrays     |
   | Version     | 1.0.0              |

   > Using gradle, this can be added as a dependency entry like the following:
   >
   > ```groovy
   > implementation 'xyz.ronella.casual:command-arrays:1.0.0'
   > ```
   >
   
2. Include the following to your **module-info.java**:

   ```java
   requires xyz.ronella.casual.command.arrays;
   ```

## [User Guide](docs/USER_GUIDE_TOC.md)

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details

## [Build](BUILD.md)

## [Changelog](CHANGELOG.md)

## Author

* Ronaldo Webb