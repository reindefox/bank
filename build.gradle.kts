group = "com.reindefox"
version = "0.0.1-SNAPSHOT"
description = "bank monorepo"

allprojects {
    repositories {
        mavenCentral()
    }
}

tasks.register("testAll") {
    group = "verification"
    description = "Runs all tests in all subprojects"
    dependsOn(allprojects.flatMap { project ->
        project.tasks.matching { it.name == "test" }
    })
}
