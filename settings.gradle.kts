
rootProject.name = "CoApi"

include("api")
include("spring")
include("dependencies")
include("bom")
include("code-coverage-report")
include("spring-boot-starter")
include("example-server")

//region example
include(":example-api")
project(":example-api").projectDir = file("example/example-api")

include(":example-server")
project(":example-server").projectDir = file("example/example-server")

//endregion
