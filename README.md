The Axiom Java SDK
==================================
Axiom-web3j is a comprehensive and easy-to-use Axiom blockchain interaction tool developed forked from [web3j](https://github.com/web3j/web3j).

**Features**

- Fully compatible with web3j, seamless switching

Developer
------
This section is intended for Axiom-web3j developers.

**Environment**
```text
java version: JDK-17
```

**Upgrade**

After completing the new development content, use the following command to build:
```shell
./gradlew build
```

After completing self-testing, use the following command to format the code:
```shell
./gradlew :spotlessJavaCheck
./gradlew :spotlessApply
```

Once everything is ready, push to the project repository so it can be referenced in the project:
```shell
./gradlew :publish
```
When using Axiom-web3j in other Java projects, be mindful of whether the GitHubToken is valid. If using a SNAPSHOT package, use `mvn -U` to force an update.

License
------
Apache 2.0
