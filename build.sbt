import ByteConversions._

organization in ThisBuild := "org.ecommerce"

// the Scala version that will be used for cross-compiled libraries
scalaVersion in ThisBuild := "2.11.8"
//*************************************************
scalaVersion in ThisBuild := "2.11.7"

val immutables = "org.immutables" % "value" % "2.1.14"
val mockito = "org.mockito" % "mockito-core" % "1.10.19"

//****************************************************

//*****************************************************
lazy val itemApi = project("item-api")
  .settings(
    version := "1.0-SNAPSHOT",
    libraryDependencies ++= Seq(lagomJavadslApi, immutables,
      lagomJavadslImmutables, lagomJavadslJackson)
  )

  lazy val itemImpl = project("item-impl")
  .enablePlugins(LagomJava)
  .settings(
    version := "1.0-SNAPSHOT",
    libraryDependencies ++= Seq(lagomJavadslPersistence, immutables,
      lagomJavadslImmutables, lagomJavadslTestKit
    )
  )
  .settings(lagomForkedTestSettings: _*) // tests must be forked for cassandra
  .dependsOn(itemApi)

//*****************************************************
lazy val userApi = project("user-api")
  .settings(
    version := "1.0-SNAPSHOT",
    libraryDependencies ++= Seq(lagomJavadslApi, immutables,
      lagomJavadslImmutables, lagomJavadslJackson)
  )

  lazy val userImpl = project("user-impl")
  .enablePlugins(LagomJava)
  .settings(
    version := "1.0-SNAPSHOT",
    libraryDependencies ++= Seq(lagomJavadslPersistence, immutables,
      lagomJavadslImmutables, lagomJavadslTestKit
    )
  )
  .settings(lagomForkedTestSettings: _*) // tests must be forked for cassandra
  .dependsOn(userApi)

//*****************************************************
//***********************************************************************************
lazy val messageApi = project("message-api")
  .settings(
    version := "1.0-SNAPSHOT",
    libraryDependencies ++= Seq(lagomJavadslApi, immutables,
      lagomJavadslImmutables, lagomJavadslJackson)
  )

lazy val messageImpl = project("message-impl")
  .enablePlugins(LagomJava)
  .settings(
    version := "1.0-SNAPSHOT",
    libraryDependencies ++= Seq(lagomJavadslPersistence, immutables,
      lagomJavadslImmutables, lagomJavadslTestKit
    )
  )
  .settings(lagomForkedTestSettings: _*) // tests must be forked for cassandra
  .dependsOn(messageApi)
  
//************************************************************************************
  
  lazy val rankingApi = project("ranking-api")
  .settings(
    version := "1.0-SNAPSHOT",
    libraryDependencies ++= Seq(lagomJavadslApi, immutables,
      lagomJavadslImmutables, lagomJavadslJackson)
  )

lazy val rankingImpl = project("ranking-impl")
  .enablePlugins(LagomJava)
  .settings(
    version := "1.0-SNAPSHOT",
    libraryDependencies ++= Seq(lagomJavadslPersistence, immutables,
      lagomJavadslImmutables, lagomJavadslTestKit
    )
  )
  .settings(lagomForkedTestSettings: _*) // tests must be forked for cassandra
  .dependsOn(rankingApi)
    
//****************************************************************************************************
def project(id: String) = Project(id, base = file(id))
  .settings(eclipseSettings: _*)
  .settings(javacOptions in compile ++= Seq("-encoding", "UTF-8", "-source", "1.8", "-target", "1.8", "-Xlint:unchecked", "-Xlint:deprecation"))
  .settings(jacksonParameterNamesJavacSettings: _*) // applying it to every project even if not strictly needed.


// See https://github.com/FasterXML/jackson-module-parameter-names
lazy val jacksonParameterNamesJavacSettings = Seq(
  javacOptions in compile += "-parameters"
)

// Configuration of sbteclipse
// Needed for importing the project into Eclipse
lazy val eclipseSettings = Seq(
  EclipseKeys.projectFlavor := EclipseProjectFlavor.Java,
  EclipseKeys.withBundledScalaContainers := false,
  EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Resource,
  EclipseKeys.eclipseOutput := Some(".target"),
  EclipseKeys.withSource := true,
  EclipseKeys.withJavadoc := true,
  // avoid some scala specific source directories
  unmanagedSourceDirectories in Compile := Seq((javaSource in Compile).value),
  unmanagedSourceDirectories in Test := Seq((javaSource in Test).value)
)
lagomCassandraCleanOnStart in ThisBuild := false