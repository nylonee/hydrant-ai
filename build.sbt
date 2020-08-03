scalaVersion := "2.13.1"
name := "hydrant-ai"
version := "0.0.1"

libraryDependencies += "org.typelevel" %% "cats-core" % "2.0.0"
libraryDependencies += "io.monix" %% "monix" % "3.2.2"

// Deep learning libraries from deeplearning4j
libraryDependencies += "org.deeplearning4j" % "deeplearning4j-core" % "1.0.0-beta6"
libraryDependencies += "org.nd4j" % "nd4j-native-platform" % "1.0.0-beta6"
